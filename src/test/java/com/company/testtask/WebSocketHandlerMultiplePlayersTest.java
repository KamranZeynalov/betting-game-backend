package com.company.testtask;

import com.company.testtask.handler.CustomWebSocketHandler;
import com.company.testtask.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.company.testtask.constant.Constants.GAME_PATH;
import static com.company.testtask.constant.Constants.LOCALHOST;
import static com.company.testtask.constant.Constants.TEST_PLAYER_NAME;
import static com.company.testtask.constant.Constants.WINNING_MESSAGE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketHandlerMultiplePlayersTest extends BaseTest {

    @LocalServerPort
    private int port;

    @MockBean
    private Random random;

    private final CountDownLatch latch = new CountDownLatch(4);

    private final WebSocketClient client = new StandardWebSocketClient();

    @BeforeEach
    public void setUp() {
        Mockito.when(random.nextInt(10)).thenReturn(0);
    }

    @Test
    public void testTwoPlayersWinningCase() throws Exception {
        List<WebSocketSession> sessions = new ArrayList<>();
        Map<String, Player> players = new HashMap<>();
        Map<String, List<String>> playerMessages = new HashMap<>();
        Map<String, BigDecimal> playerPayouts = new HashMap<>();

        // Register players and start the game
        for (int i = 0; i < 2; i++) {
            Player player = createPlayer(TEST_PLAYER_NAME + i, 1, BigDecimal.valueOf(i + 10));

            CustomWebSocketHandler handler = createWebSocketHandler(player, playerMessages, latch);

            WebSocketSession session = client.doHandshake(handler, LOCALHOST + port + GAME_PATH).get();
            sessions.add(session);
            players.put(session.getId(), player);
        }

        latch.await(15, TimeUnit.SECONDS);


        // Verify each player got a proper message
        for (String sessionId : playerMessages.keySet()) {
            List<String> messages = playerMessages.get(sessionId);
            Player player = players.get(sessionId);
            BigDecimal expectedPayout = player.getBet().multiply(BigDecimal.valueOf(9.9));
            String expectedWinningMessage = WINNING_MESSAGE + expectedPayout;

            assertEquals(2, messages.size());
            assertEquals(expectedWinningMessage, messages.get(0));

            playerPayouts.put(player.getNickname(), expectedPayout);
        }

        String winnersListMessage = makeWinnersListMessage(playerPayouts);

        // Verify each player got winners list message
        for (String sessionId : playerMessages.keySet()) {
            List<String> messages = playerMessages.get(sessionId);
            assertTrue(messages.contains(winnersListMessage));
        }

        closeSessions(sessions);
    }

}
