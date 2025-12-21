import {Chat} from './chat.js';
import {PeerConnection} from "./peer-connection.js";

// 先创建Chat实例的引用
let chat;

const peerConnection = new PeerConnection({
    onLocalMedia: stream => document.getElementById("localVideo").srcObject = stream,
    onRemoteMedia: stream => document.getElementById("remoteVideo").srcObject = stream,
    onChatMessage: message => chat.addRemoteMessage(message),
    onStateChange: state => {
        document.body.dataset.state = state;
        chat.updateUi(state);
    },
    // 添加handleWsMessage回调，处理好友相关的消息
    onWsMessage: message => chat.handleWsMessage(message)
});

// 创建Chat实例
chat = new Chat(peerConnection);

// 在PeerConnection初始化完成后，将WebSocket连接传递给Chat实例
setTimeout(() => {
    if (peerConnection.sdpExchange) {
        chat.setWsConnection(peerConnection.sdpExchange);
    }
}, 5000);

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
