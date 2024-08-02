package com.company.testtask;

import com.company.testtask.exception.PlayerNotFoundException;
import com.company.testtask.model.Player;
import com.company.testtask.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.company.testtask.constant.Constants.TEST_PLAYER_ID;
import static com.company.testtask.constant.Constants.TEST_PLAYER_NAME;

@SpringBootTest
public class GameServiceTest extends BaseTest {

    @MockBean
    private Random random;

    @Autowired
    private GameService gameService;

    @BeforeEach
    public void setUp() {
        Mockito.when(random.nextInt(10)).thenReturn(0);
    }

    @Test
    public void testPlayerWonCase() {
        gameService.registerPlayer(TEST_PLAYER_ID,
                createPlayer(TEST_PLAYER_NAME, 1, BigDecimal.TEN));

        gameService.endRound(TEST_PLAYER_ID);

        Player player1 = gameService.getPlayer(TEST_PLAYER_ID);

        assertTrue(player1.isWon());
        assertEquals(player1.getPayout(), BigDecimal.valueOf(99.0));
    }

    @Test
    public void testPlayerLossCase() {
        Player player = createPlayer(TEST_PLAYER_NAME, 2, BigDecimal.TEN);
        gameService.registerPlayer(TEST_PLAYER_ID, player);

        gameService.endRound(TEST_PLAYER_ID);

        Player player1 = gameService.getPlayer(TEST_PLAYER_ID);

        assertFalse(player1.isWon());
        assertEquals(player1.getPayout(), BigDecimal.ZERO);
    }

    @Test
    public void testPlayerNotFoundCase() {
        Player player = createPlayer(TEST_PLAYER_NAME, 2, BigDecimal.TEN);
        gameService.registerPlayer(TEST_PLAYER_ID, player);

        gameService.removePlayer(TEST_PLAYER_ID);

        try {
            gameService.endRound(TEST_PLAYER_ID);
        } catch (Exception e) {
            assertInstanceOf(PlayerNotFoundException.class, e);
        }
    }
}
