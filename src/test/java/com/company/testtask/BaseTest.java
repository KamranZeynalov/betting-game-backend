package com.company.testtask;

import com.company.testtask.constant.Constants;
import com.company.testtask.handler.CustomWebSocketHandler;
import com.company.testtask.model.Player;
import com.company.testtask.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@SpringBootTest
public class BaseTest {

    @Autowired
    private GameService gameService;

    @Autowired
    private LocalValidatorFactoryBean validator;

    private final ObjectMapper mapper = new ObjectMapper();

    public Player createPlayer(String nickname, int number, BigDecimal bet) {
        Player player = new Player();
        player.setNickname(nickname);
        player.setNumber(number);
        player.setBet(bet);
        return player;
    }

    public CustomWebSocketHandler createWebSocketHandler(Player player, Map<String, List<String>> playerMessages,
                                                         CountDownLatch latch) {
        return new CustomWebSocketHandler(gameService, validator) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                session.sendMessage(new TextMessage(mapper.writeValueAsString(player)));
            }

            @Override
            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
                synchronized (playerMessages) {
                    String sessionId = session.getId();
                    playerMessages.computeIfAbsent(sessionId, k -> new ArrayList<>())
                            .add(message.getPayload().toString());
                }
                latch.countDown();
            }
        };
    }

    public String makeWinnersListMessage(Map<String, BigDecimal> playerPayouts) {
        StringBuilder sb = new StringBuilder(Constants.WINNERS_LIST_MESSAGE);
        playerPayouts.forEach((player, payout) ->
                sb.append(player).append(": ").append(payout).append("\n")
        );
        return sb.toString().trim();
    }

    public void closeSessions(List<WebSocketSession> sessions) throws Exception {
        for (WebSocketSession session : sessions) {
            session.close();
        }
    }

    public void registerPlayer(String sessionId, Player player) {
        gameService.registerPlayer(sessionId, player);
    }

    public void removeAllPlayers() {
        gameService.removeAllPlayers();
    }
}
