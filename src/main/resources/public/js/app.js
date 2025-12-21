import {Chat} from './chat.js';
import {PeerConnection} from "./peer-connection.js";

const peerConnection = new PeerConnection({
    onLocalMedia: stream => document.getElementById("localVideo").srcObject = stream,
    onRemoteMedia: stream => document.getElementById("remoteVideo").srcObject = stream,
    onChatMessage: message => chat.addRemoteMessage(message),
    onStateChange: state => {
        document.body.dataset.state = state;
        chat.updateUi(state);
    }
});

let chat = new Chat(peerConnection, peerConnection.sdpExchange);

// 全局函数供HTML按钮调用
window.acceptFriendRequest = function() {
    peerConnection.sdpExchange.send(JSON.stringify({name: "FRIEND_ACCEPT"}));
};

// 等待sdpExchange初始化完成后设置消息处理器
function setupFriendMessageHandler() {
    if (!peerConnection.sdpExchange) {
        // 如果sdpExchange还未初始化，等待后重试
        setTimeout(setupFriendMessageHandler, 100);
        return;
    }
    
    // 监听WebSocket消息以处理好友相关事件
    const originalOnMessage = peerConnection.sdpExchange.onmessage;
    peerConnection.sdpExchange.onmessage = function(event) {
        const message = JSON.parse(event.data);
        
        if (message.name === "FRIEND_REQUEST") {
            chat.handleFriendRequest(message.data);
        } else if (message.name === "FRIEND_ACCEPTED") {
            chat.handleFriendAccepted(message.data);
        }
        
        // 调用原始的消息处理器
        if (originalOnMessage) {
            originalOnMessage.call(this, event);
        }
    };
}

// 开始设置消息处理器
setupFriendMessageHandler();

document.getElementById("startPairing").addEventListener("click", async () => {
    peerConnection.setState("CONNECTING");
    peerConnection.sdpExchange.send(JSON.stringify({name: "PAIRING_START"}))
});

document.getElementById("abortPairing").addEventListener("click", () => {
    peerConnection.sdpExchange.send(JSON.stringify({name: "PAIRING_ABORT"}))
    peerConnection.disconnect("LOCAL");
})

document.getElementById("leavePairing").addEventListener("click", () => {
    peerConnection.sendBye();
});

window.addEventListener("beforeunload", () => {
    if (peerConnection.state === "CONNECTED") {
        peerConnection.sendBye();
    }
});
