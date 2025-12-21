export class Chat {

    #input = document.getElementById("chatInput");
    #sendBtn = document.getElementById("chatSend");
    #log = document.getElementById("chatLog");
    #peerConnection;
    #websocket;
    #friendRequestPending = false;

    constructor(peerConnection, websocket) {
        this.#peerConnection = peerConnection;
        this.#websocket = websocket;
        this.updateUi("NOT_CONNECTED");
        this.#sendBtn.addEventListener("click", () => {
            if (this.#peerConnection.dataChannel === null) return console.log("No data channel");
            if (this.#input.value.trim() === "") return this.#input.value = "";
            
            // 检查特殊指令
            if (this.#handleSpecialCommands(this.#input.value.trim())) {
                this.#input.value = "";
                return;
            }
            
            this.#addToLog("local", this.#input.value);
            this.#peerConnection.dataChannel.send(JSON.stringify({chat: this.#input.value}));
            this.#input.value = "";
        });

        this.#input.addEventListener("keyup", event => {
            if (event.key !== "Enter") return;
            this.#sendBtn.click(); // reuse the click handler
        });
    }

    #handleSpecialCommands(message) {
        if (message === "@friend") {
            if (!this.#friendRequestPending) {
                this.#addToLog("server", "Friend request sent! Waiting for response...");
                this.#websocket.send(JSON.stringify({name: "FRIEND_REQUEST"}));
                this.#friendRequestPending = true;
            } else {
                this.#addToLog("server", "Friend request already sent!");
            }
            return true;
        }
        
        if (message === "#my-friends") {
            this.#showFriendsList();
            return true;
        }
        
        return false;
    }

    #showFriendsList() {
        // 调用API获取好友列表
        const userCode = localStorage.getItem('userCode') || 'testuser';
        fetch('/api/friends/list', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userCode: userCode })
        })
        .then(response => response.json())
        .then(data => {
            if (data.friends && data.friends.length > 0) {
                this.#addToLog("server", "Your friends:");
                data.friends.forEach(friend => {
                    this.#addToLog("server", `- ${friend.friendCode}`);
                });
            } else {
                this.#addToLog("server", "You have no friends yet. Use @friend to make friends!");
            }
        })
        .catch(error => {
            console.error('Error fetching friends list:', error);
            this.#addToLog("server", "Error loading friends list. Please try again.");
        });
    }

    handleFriendRequest(fromUser) {
        this.#addToLog("server", `Friend request from ${fromUser}! Click to accept: <button onclick="window.acceptFriendRequest()" style="margin-left: 10px; padding: 5px 10px; background: #4CAF50; color: white; border: none; border-radius: 3px; cursor: pointer;">Accept</button>`);
    }

    handleFriendAccepted(fromUser) {
        this.#addToLog("server", `You are now friends with ${fromUser}!`);
        this.#friendRequestPending = false;
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

    #addToLog(owner, message) {
        this.#log.insertAdjacentHTML("beforeend", `<div class="message ${owner}">${message}</div>`);
        this.#log.scrollTop = this.#log.scrollHeight;
    }
}
