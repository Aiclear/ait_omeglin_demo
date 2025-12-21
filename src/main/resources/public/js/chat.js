export class Chat {

    #input = document.getElementById("chatInput");
    #sendBtn = document.getElementById("chatSend");
    #log = document.getElementById("chatLog");
    #peerConnection;
    #wsConnection; // WebSocket连接
    #currentFriendRequestUser = null;

    constructor(peerConnection) {
        this.#peerConnection = peerConnection;
        this.updateUi("NOT_CONNECTED");
        this.#sendBtn.addEventListener("click", () => {
            if (this.#peerConnection.dataChannel === null) return console.log("No data channel");
            const message = this.#input.value.trim();
            if (message === "") return this.#input.value = "";
            
            // 检查是否是@friend指令或查看好友列表指令
            if (message === "@friend") {
                this.#handleFriendCommand();
                this.#input.value = "";
                return;
            } else if (message === "@friends") {
                this.#showFriendsList();
                this.#input.value = "";
                return;
            }
            
            this.#addToLog("local", message);
            this.#peerConnection.dataChannel.send(JSON.stringify({chat: message}));
            this.#input.value = "";
        });

        this.#input.addEventListener("keyup", event => {
            if (event.key !== "Enter") return;
            this.#sendBtn.click(); // reuse the click handler
        });
    }
    
    // 设置WebSocket连接
    setWsConnection(ws) {
        this.#wsConnection = ws;
    }
    
    // 处理@friend指令
    #handleFriendCommand() {
        if (this.#wsConnection) {
            // 直接发送@friend文本，后端会识别并处理
            this.#wsConnection.send("@friend");
        }
    }
    
    // 显示好友请求按钮
    showFriendRequestButton() {
        this.#addToLog("server", "\n<button id=\"sendFriendRequest\" class=\"friend-request-btn\">申请添加好友</button>");
        
        // 添加按钮点击事件
        setTimeout(() => {
            const btn = document.getElementById("sendFriendRequest");
            if (btn) {
                btn.addEventListener("click", () => {
                    this.#sendFriendRequest();
                    btn.disabled = true;
                    btn.textContent = "已发送请求";
                });
            }
        }, 100);
    }
    
    // 发送好友请求
    #sendFriendRequest() {
        if (this.#wsConnection) {
            this.#wsConnection.send(JSON.stringify({name: "FRIEND_REQUEST", data: null}));
            this.#addToLog("local", "好友请求已发送");
        }
    }
    
    // 显示收到的好友请求
    showFriendRequestReceived(userCode) {
        this.#currentFriendRequestUser = userCode;
        this.#addToLog("server", `\n收到好友请求！<button id=\"acceptFriendRequest\" class=\"friend-accept-btn\">接受好友</button>`);
        
        // 添加接受按钮点击事件
        setTimeout(() => {
            const btn = document.getElementById("acceptFriendRequest");
            if (btn) {
                btn.addEventListener("click", () => {
                    this.#acceptFriendRequest();
                    btn.disabled = true;
                    btn.textContent = "已接受";
                });
            }
        }, 100);
    }
    
    // 接受好友请求
    #acceptFriendRequest() {
        if (this.#wsConnection && this.#currentFriendRequestUser) {
            this.#wsConnection.send(JSON.stringify({name: "FRIEND_ACCEPT", data: this.#currentFriendRequestUser}));
            this.#addToLog("local", "已接受好友请求");
        }
    }
    
    // 显示好友添加成功
    showFriendAddedSuccess() {
        this.#addToLog("server", "🎉 好友添加成功！");
    }
    
    // 显示好友列表
    async #showFriendsList() {
        try {
            const response = await fetch('/api/user/friendsList', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include' // 包含cookie以传递token
            });
            
            if (response.ok) {
                const friends = await response.json();
                if (friends.length > 0) {
                    let friendsListHtml = "<div class='friends-list'>";
                    friendsListHtml += "<h4>你的好友列表：</h4><ul>";
                    friends.forEach(friend => {
                        friendsListHtml += `<li>${friend.username} (${friend.code})</li>`;
                    });
                    friendsListHtml += "</ul></div>";
                    this.#addToLog("server", friendsListHtml);
                } else {
                    this.#addToLog("server", "你还没有添加任何好友。");
                }
            } else {
                this.#addToLog("server", "获取好友列表失败，请稍后再试。");
            }
        } catch (error) {
            console.error("获取好友列表错误:", error);
            this.#addToLog("server", "获取好友列表时出错。");
        }
    }

    updateUi(state) {
        if (["NOT_CONNECTED", "CONNECTING", "CONNECTED"].includes(state)) {
            this.#log.innerHTML = "";
        }
        if (state === "NOT_CONNECTED") this.#addToLog("server", "Click 'Find Stranger' to connect with a random person!");
        if (state === "CONNECTING") this.#addToLog("server", "Finding a stranger for you to chat with...");
        if (state === "CONNECTED") this.#addToLog("server", "You're talking to a random person. Say hi!");
        if (state === "DISCONNECTED_LOCAL") this.#addToLog("server", "You disconnected");
        if (state === "DISCONNECTED_REMOTE") this.#addToLog("server", "Stranger disconnected");
    }

    addRemoteMessage = (message) => this.#addToLog("remote", message)
    
    // 处理WebSocket消息
    handleWsMessage(message) {
        if (message.name === "SHOW_FRIEND_BUTTON") {
            this.showFriendRequestButton();
        } else if (message.name === "FRIEND_REQUEST_RECEIVED") {
            this.showFriendRequestReceived(message.data);
        } else if (message.name === "FRIEND_ADDED_SUCCESS") {
            this.showFriendAddedSuccess();
        }
    }

    #addToLog(owner, message) {
        // 对于服务器消息，允许HTML内容（用于按钮等）
        const content = owner === "server" ? message : message.replace(/</g, "&lt;").replace(/>/g, "&gt;");
        this.#log.insertAdjacentHTML("beforeend", `<div class=\"message ${owner}\">${content}</div>`);
        this.#log.scrollTop = this.#log.scrollHeight;
    }
}
