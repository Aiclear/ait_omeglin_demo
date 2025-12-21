package com.github.blackz;

import com.github.blackz.auth.dao.UserRepository;
import com.github.blackz.db.entity.User;
import com.github.blackz.user.dao.FriendsRepository;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Matchmaking {

    private static final Logger logger = LoggerFactory.getLogger(Matchmaking.class);
    private static final ConcurrentLinkedQueue<Exchange> queue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentHashMap<String, Exchange> activeExchanges = new ConcurrentHashMap<>();

    public static void websocket(WsConfig ws) {
        ws.onConnect(WsContext::enableAutomaticPings);
        ws.onClose(Matchmaking::pairingAbort);
        ws.onMessage(user -> {
            logger.info("Received message: {}", user.message());

            // 尝试解析为Message对象
            try {
                var message = user.messageAsClass(Message.class);
                switch (message.name()) {
                    case "PAIRING_START" -> pairingStart(user);
                    case "PAIRING_ABORT" -> pairingAbort(user);
                    case "PAIRING_DONE" -> pairingDone(user);
                    case "SDP_OFFER", "SDP_ANSWER", "SDP_ICE_CANDIDATE" -> {
                        var exchange = findExchange(user);
                        if (exchange != null && exchange.a != null && exchange.b != null) {
                            send(exchange.otherUser(user), message); // forward message to other user
                        } else {
                            logger.warn("Received SDP message from unpaired user");
                        }
                    }
                    case "FRIEND_REQUEST" -> handleFriendRequest(user, message);
                    case "FRIEND_ACCEPT" -> handleFriendAccept(user, message);
                }
            } catch (Exception e) {
                // 如果不是标准Message对象，检查是否是文本消息
                String text = user.message();
                if (text.equals("@friend")) {
                    handleFriendCommand(user);
                }
            }
        });
    }

    /**
     * 处理@friend指令
     */
    private static void handleFriendCommand(WsContext user) {
        var exchange = findExchange(user);
        if (exchange != null && exchange.a != null && exchange.b != null) {
            // 通知双方显示好友请求按钮
            send(user, new Message("SHOW_FRIEND_BUTTON", null));
            // 在用户会话中存储对方信息，以便后续处理
            if (user.equals(exchange.a)) {
                exchange.userACode = (String) user.attribute("userCode");
                exchange.userBCode = (String) exchange.b.attribute("userCode");
            } else {
                exchange.userACode = (String) exchange.a.attribute("userCode");
                exchange.userBCode = (String) user.attribute("userCode");
            }
        }
    }

    /**
     * 处理好友请求
     */
    private static void handleFriendRequest(WsContext user, Message message) {
        var exchange = findExchange(user);
        if (exchange != null && exchange.a != null && exchange.b != null) {
            String requesterCode = (String) user.attribute("userCode");
            String receiverCode = user.equals(exchange.a) ? exchange.userBCode : exchange.userACode;

            // 通知接收方有好友请求
            send(exchange.otherUser(user), new Message("FRIEND_REQUEST_RECEIVED", requesterCode));
        }
    }

    /**
     * 处理好友接受请求
     */
    private static void handleFriendAccept(WsContext user, Message message) {
        var exchange = findExchange(user);
        if (exchange != null && exchange.a != null && exchange.b != null) {
            // 从活动匹配集合中移除
            activeExchanges.values().remove(exchange);

            String accepterCode = (String) user.attribute("userCode");
            String requesterCode = message.data();

            // 创建好友关系
            FriendsRepository.createFriendship(accepterCode, requesterCode, requesterCode);

            // 通知双方好友添加成功
            send(user, new Message("FRIEND_ADDED_SUCCESS", null));

            // 查找请求方的WebSocket连接并通知
            WsContext requesterWs = null;
            if (requesterCode.equals(exchange.userACode)) {
                requesterWs = exchange.a;
            } else if (requesterCode.equals(exchange.userBCode)) {
                requesterWs = exchange.b;
            }

            if (requesterWs != null) {
                send(requesterWs, new Message("FRIEND_ADDED_SUCCESS", null));
            }
        }
    }

    private static void pairingStart(WsContext user) {
        // 从队列和活动匹配中移除用户，防止重复匹配
        queue.removeIf(ex -> ex.a == user || ex.b == user);

        // 查找活动匹配中的用户并移除
        for (Map.Entry<String, Exchange> entry : activeExchanges.entrySet()) {
            Exchange ex = entry.getValue();
            if (ex.a == user || ex.b == user) {
                activeExchanges.remove(entry.getKey());
                break;
            }
        }

        var exchange = queue.stream()
            .filter(ex -> ex.b == null)
            .findFirst()
            .orElse(null);
        if (exchange != null) {
            exchange.b = user;
            send(exchange.a, new Message("PARTNER_FOUND", "GO_FIRST"));
            send(exchange.b, new Message("PARTNER_FOUND"));

            // 将配对成功的交换对象从队列移到活动匹配集合
            queue.remove(exchange);
            // 为交换对象生成唯一ID并添加到活动匹配集合
            String exchangeId = "exchange_" + System.currentTimeMillis();
            activeExchanges.put(exchangeId, exchange);
        } else {
            queue.add(new Exchange(user));
        }
    }

    private static void pairingAbort(WsContext user) {
        var exchange = findExchange(user);
        if (exchange != null) {
            send(exchange.otherUser(user), new Message("PARTNER_LEFT"));
            queue.remove(exchange);
            // 从活动匹配集合中移除
            activeExchanges.values().remove(exchange);
        }
    }

    private static void pairingDone(WsContext user) {
        var exchange = findExchange(user);
        if (exchange != null) {
            exchange.doneCount++;
            if (exchange.doneCount == 2) {
                queue.remove(exchange);
            }
        }
    }

    private static Exchange findExchange(WsContext user) {
        // 首先在活动匹配集合中查找
        for (Exchange exchange : activeExchanges.values()) {
            if (user.equals(exchange.a) || user.equals(exchange.b)) {
                return exchange;
            }
        }

        // 如果活动匹配中没有找到，再在队列中查找
        return queue.stream()
            .filter(ex -> user.equals(ex.a) || user.equals(ex.b))
            .findFirst()
            .orElse(null);
    }

    private static void send(WsContext user, Message message) { // null safe send method
        if (user != null) {
            user.send(message);
        }
    }

    record Message(String name, String data) {

        public Message(String name) {
            this(name, null);
        }
    }

    static class Exchange {

        public WsContext a;
        public WsContext b;
        public String userACode;
        public String userBCode;
        public int doneCount = 0;

        public Exchange(WsContext a) {
            this.a = a;
        }

        public WsContext otherUser(WsContext user) {
            return user.equals(a) ? b : a;
        }
    }

}
