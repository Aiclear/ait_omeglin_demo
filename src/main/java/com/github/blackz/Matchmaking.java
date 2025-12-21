package com.github.blackz;

import com.github.blackz.friend.FriendRepository;
import com.github.blackz.security.SecurityContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Matchmaking {
    private static final Logger logger = LoggerFactory.getLogger(Matchmaking.class);
    private static final ConcurrentLinkedQueue<Exchange> queue = new ConcurrentLinkedQueue<>();
    private static final Map<WsContext, String> userCodeMap = new ConcurrentHashMap<>();

    public static void websocket(WsConfig ws) {
        ws.onConnect(ctx -> {
            ctx.enableAutomaticPings();
            // 从WebSocket连接中获取用户信息
            try {
                String token = ctx.queryParam("token");
                if (token != null) {
                    // 这里需要从token或session中获取用户code
                    // 暂时使用简单的用户标识
                    String userCode = ctx.queryParam("userCode");
                    if (userCode != null) {
                        userCodeMap.put(ctx, userCode);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to get user info from WebSocket connection", e);
            }
        });
        ws.onClose(Matchmaking::pairingAbort);
        ws.onMessage(user -> {
            logger.info("Received message: {}", user.message());
            var message = user.messageAsClass(Message.class);
            switch (message.name()) {
                case "PAIRING_START" -> pairingStart(user);
                case "PAIRING_ABORT" -> pairingAbort(user);
                case "PAIRING_DONE" -> pairingDone(user);
                case "FRIEND_REQUEST" -> handleFriendRequest(user, message);
                case "FRIEND_ACCEPT" -> handleFriendAccept(user, message);
                case "SDP_OFFER", "SDP_ANSWER", "SDP_ICE_CANDIDATE" -> {
                    var exchange = findExchange(user);
                    if (exchange != null && exchange.a != null && exchange.b != null) {
                        send(exchange.otherUser(user), message); // forward message to other user
                    } else {
                        logger.warn("Received SDP message from unpaired user");
                    }
                }
            }
        });
    }

    private static void pairingStart(WsContext user) {
        queue.removeIf(ex -> ex.a == user || ex.b == user); // prevent double queueing
        var exchange = queue.stream()
                .filter(ex -> ex.b == null)
                .findFirst()
                .orElse(null);
        if (exchange != null) {
            exchange.b = user;
            send(exchange.a, new Message("PARTNER_FOUND", "GO_FIRST"));
            send(exchange.b, new Message("PARTNER_FOUND"));
        } else {
            queue.add(new Exchange(user));
        }
    }

    private static void pairingAbort(WsContext user) {
        var exchange = findExchange(user);
        if (exchange != null) {
            send(exchange.otherUser(user), new Message("PARTNER_LEFT"));
            queue.remove(exchange);
        }
        userCodeMap.remove(user);
    }

    private static void pairingDone(WsContext user) {
        var exchange = findExchange(user);
        if (exchange != null) {
            exchange.doneCount++;
        }
        queue.removeIf(ex -> ex.doneCount == 2);
    }

    private static Exchange findExchange(WsContext user) {
        return queue.stream()
                .filter(ex -> user.equals(ex.a) || user.equals(ex.b))
                .findFirst()
                .orElse(null);
    }

    private static void handleFriendRequest(WsContext user, Message message) {
        var exchange = findExchange(user);
        if (exchange != null && exchange.a != null && exchange.b != null) {
            var otherUser = exchange.otherUser(user);
            String fromUserCode = userCodeMap.get(user);
            String toUserCode = userCodeMap.get(otherUser);
            
            if (fromUserCode != null && toUserCode != null) {
                // 检查是否已经是好友
                if (!FriendRepository.areFriends(fromUserCode, toUserCode)) {
                    FriendRepository.createFriendRequest(fromUserCode, toUserCode);
                    send(otherUser, new Message("FRIEND_REQUEST", fromUserCode));
                }
            }
        }
    }

    private static void handleFriendAccept(WsContext user, Message message) {
        var exchange = findExchange(user);
        if (exchange != null && exchange.a != null && exchange.b != null) {
            var otherUser = exchange.otherUser(user);
            String acceptingUserCode = userCodeMap.get(user);
            String requestingUserCode = userCodeMap.get(otherUser);
            
            if (acceptingUserCode != null && requestingUserCode != null) {
                FriendRepository.acceptFriendRequest(requestingUserCode, acceptingUserCode);
                send(otherUser, new Message("FRIEND_ACCEPTED", acceptingUserCode));
            }
        }
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
        public int doneCount = 0;

        public Exchange(WsContext a) {
            this.a = a;
        }

        public WsContext otherUser(WsContext user) {
            return user.equals(a) ? b : a;
        }
    }

}
