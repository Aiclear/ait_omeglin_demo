import {bpost, getUserInfo} from "./util.js";

export class Chat {

    #input = document.getElementById("chatInput");
    #sendBtn = document.getElementById("chatSend");
    #log = document.getElementById("chatLog");
    #peerConnection;

    constructor(peerConnection) {
        this.#peerConnection = peerConnection;
        this.updateUi("NOT_CONNECTED");
        this.#sendBtn.addEventListener("click", () => {
            if (this.#peerConnection.dataChannel === null) {
                return console.log(
                    "No data channel");
            }
            if (this.#input.value.trim() === "") {
                return this.#input.value = "";
            }
            this.#addToLog("local", this.#input.value);
            this.#peerConnection.dataChannel.send(
                JSON.stringify({chat: this.#input.value}));
            this.#input.value = "";
        });

        this.#input.addEventListener("keyup", event => {
            if (event.key !== "Enter") {
                return;
            }
            this.#sendBtn.click(); // reuse the click handler
        });
    }

    updateUi(state) {
        if (["NOT_CONNECTED", "CONNECTING", "CONNECTED"].includes(state)) {
            this.#log.innerHTML = "";
        }
        if (state === "NOT_CONNECTED") {
            this.#addToLog("server",
                "Click 'Find Stranger' to connect with a random person!");
        }
        if (state === "CONNECTING") {
            this.#addToLog("server",
                "Finding a stranger for you to chat with...");
        }
        if (state === "CONNECTED") {
            this.#addToLog("server",
                "You're talking to a random person. Say hi!");
        }
        if (state === "DISCONNECTED_LOCAL") {
            this.#addToLog("server",
                "You disconnected");
        }
        if (state === "DISCONNECTED_REMOTE") {
            this.#addToLog("server",
                "Stranger disconnected");
        }
    }

    addRemoteMessage = (message) => this.#addToLog("remote", message)

    #addToLog(owner, message) {
        if ("local" === owner) {
            // 本地消息
            this.#handleLocalLog(owner, message);
        } else if ("remote" === owner) {
            // 对方的消息
            this.#handleRemoteMessage(owner, message);
        } else {
            this.#log.insertAdjacentHTML("beforeend",
                `<div class="message ${owner}">${message}</div>`);
        }

        this.#log.scrollTop = this.#log.scrollHeight;
    }

    // 处理本地消息
    #handleLocalLog(owner, message) {
        // 需要对输入的指令进行处理
        if ("@friend" === message) {
            // 在页面打印发送加好友的请求
            this.#log.insertAdjacentHTML("beforeend",
                `<div class="message ${owner}">已发送好友请求，正在等对方确认... 😊</div>`);
        } else {
            this.#justAddToLog(owner, message);
        }
    }

    // 处理对端消息
    #handleRemoteMessage(owner, message) {
        if ("@friend" === message) {
            // 在页面展示按钮，等待用户确认
            this.#log.insertAdjacentHTML("beforeend",
                `<div class="message ${owner}">
                              对方申请成为好友 
                              <button onclick="agreeFriend()">确认</button> 
                              <button onclick="refuseFriend()">拒绝</button>
                          </div>`);

            let that = this;

            // 绑定按钮事件处理
            if (!window.refuseFriend) {
                window.refuseFriend = function () {
                    // 不同意请求
                    that.#peerConnection.dataChannel.send(
                        JSON.stringify({chat: "@friend.no"}))
                }
            }

            if (!window.agreeFriend) {
                window.agreeFriend = function (event) {
                    let userInfo = getUserInfo()
                    // 不同意请求
                    that.#peerConnection.dataChannel.send(
                        JSON.stringify(
                            {
                                chat: `@friend.ok\$${JSON.stringify({
                                    userCode: userInfo.code,
                                    username: userInfo.username
                                })}`
                            }))
                }
            }
        } else if (message.startsWith("@friend.ok")) {
            // 获取用户信息
            let remoteUserInfo = JSON.parse(message.split("$")[1]);

            let that = this;
            // 对方确认成为好友，则message为对方的user code
            bpost("/api/user_friends/make", {
                userCode: getUserInfo().code,
                friendCode: remoteUserInfo.userCode,
            }, () => {
                that.#log.insertAdjacentHTML("beforeend",
                    `<div class="message ${owner}">${remoteUserInfo.username} 同意了你的申请</div>`);
            }, (err) => {
                that.#log.insertAdjacentHTML("beforeend",
                    `<div class="message ${owner}">添加失败 ${err}</div>`);
            })
        } else if (message.startsWith("@friend.no")) {
            this.#log.insertAdjacentHTML("beforeend",
                `<div class="message ${owner}">对方拒绝了你的请求</div>`);
        } else {
            this.#justAddToLog(owner, message);
        }
    }

    // 辅助函数
    #justAddToLog(owner, message) {
        this.#log.insertAdjacentHTML("beforeend",
            `<div class="message ${owner}">${message}</div>`);
    }
}
