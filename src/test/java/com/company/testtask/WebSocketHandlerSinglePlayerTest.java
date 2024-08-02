package com.company.testtask;

import com.company.testtask.handler.CustomWebSocketHandler;
import com.company.testtask.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.company.testtask.constant.Constants.TEST_PLAYER_NAME;
import static com.company.testtask.constant.Constants.GAME_PATH;
import static com.company.testtask.constant.Constants.INVALID_PLAYER_NUMBER_MESSAGE;
import static com.company.testtask.constant.Constants.LOCALHOST;
import static com.company.testtask.constant.Constants.LOSING_MESSAGE;
import static com.company.testtask.constant.Constants.NICKNAME_ALREADY_TAKEN_MESSAGE;
import static com.company.testtask.constant.Constants.SESSION_REMOVED_MESSAGE;
import static com.company.testtask.constant.Constants.TEST_SESSION;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketHandlerSinglePlayerTest extends BaseTest {

    @LocalServerPort
    private int port;

    private final WebSocketClient client = new StandardWebSocketClient();

    @BeforeEach
    public void setUp() {
        removeAllPlayers();
    }


    @Test
    public void testPlayerLossCase() throws Exception {
        Map<String, List<String>> playerMessages = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(1);

        Player player = createPlayer(TEST_PLAYER_NAME, 10, BigDecimal.TEN);

        CustomWebSocketHandler handler = createWebSocketHandler(player, playerMessages, latch);

        WebSocketSession session = client.doHandshake(handler, LOCALHOST + port + GAME_PATH).get();

        latch.await(15, TimeUnit.SECONDS);

        assertEquals(1, playerMessages.get(session.getId()).size());
        assertEquals(LOSING_MESSAGE, playerMessages.get(session.getId()).get(0));

        session.close();

    }


    @Test
    public void testPlayerWithInvalidNumber() throws Exception {
        Map<String, List<String>> playerMessages = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(2);

        Player player = createPlayer(TEST_PLAYER_NAME, 0, BigDecimal.TEN);

        CustomWebSocketHandler handler = createWebSocketHandler(player, playerMessages, latch);

        WebSocketSession session = client.doHandshake(handler, LOCALHOST + port + GAME_PATH).get();

        latch.await(15, TimeUnit.SECONDS);

        assertEquals(2, playerMessages.get(session.getId()).size());
        assertEquals(INVALID_PLAYER_NUMBER_MESSAGE, playerMessages.get(session.getId()).get(0));
        assertEquals(SESSION_REMOVED_MESSAGE, playerMessages.get(session.getId()).get(1));

        session.close();

    }

    @Test
    public void testNicknameAlreadyTakenCase() throws Exception {
        Map<String, List<String>> playerMessages = new HashMap<>();
        CountDownLatch latch = new CountDownLatch(2);

        // Register one player
        Player existingPlayer = createPlayer(TEST_PLAYER_NAME, 1, BigDecimal.TEN);
        registerPlayer(TEST_SESSION, existingPlayer);

        // Create second player and try to register with the same nickname
        Player player = createPlayer(TEST_PLAYER_NAME, 1, BigDecimal.TEN);

        CustomWebSocketHandler handler = createWebSocketHandler(player, playerMessages, latch);

        WebSocketSession session = client.doHandshake(handler, LOCALHOST + port + GAME_PATH).get();

        latch.await(15, TimeUnit.SECONDS);

        assertEquals(2, playerMessages.get(session.getId()).size());
        assertEquals(NICKNAME_ALREADY_TAKEN_MESSAGE, playerMessages.get(session.getId()).get(0));
        assertEquals(SESSION_REMOVED_MESSAGE, playerMessages.get(session.getId()).get(1));

        session.close();
    }
}
