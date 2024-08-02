package com.company.testtask.service;

import com.company.testtask.exception.NicknameIsTakenException;
import com.company.testtask.exception.PlayerNotFoundException;
import com.company.testtask.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.company.testtask.constant.Constants.LOSING_MESSAGE;
import static com.company.testtask.constant.Constants.NICKNAME_ALREADY_TAKEN_MESSAGE;
import static com.company.testtask.constant.Constants.PLAYER_NOT_FOUND;
import static com.company.testtask.constant.Constants.WINNERS_LIST_MESSAGE;
import static com.company.testtask.constant.Constants.WINNING_MESSAGE;

@Service
@RequiredArgsConstructor
public class GameService {

    private final Map<String, Player> players = new HashMap<>();

    public static final long ROUND_DURATION = 10000;

    private final Random random;

    public void registerPlayer(String playerId, Player player) {
        players.put(playerId, player);
    }

    private void calculatePayout(Player model) {
        BigDecimal payout = BigDecimal.ZERO;

        if (model.getNumber() == generateRandomNumber()) {
            payout = model.getBet().multiply(BigDecimal.valueOf(9.9));
            model.setWon(true);
        }

        model.setPayout(payout);
    }

    public String endRound(String playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new PlayerNotFoundException(PLAYER_NOT_FOUND);
        }

        calculatePayout(player);
        return player.isWon()
                ? WINNING_MESSAGE + player.getPayout()
                : LOSING_MESSAGE;
    }

    public String getWinnersList() {
        StringBuilder winnersList = new StringBuilder(WINNERS_LIST_MESSAGE);
        players.values().stream()
                .filter(Player::isWon)
                .sorted(Comparator.comparing(Player::getPayout).reversed())
                .forEach(player -> winnersList.append(player.getNickname())
                        .append(": ")
                        .append(player.getPayout())
                        .append("\n"));
        return winnersList.toString().trim();
    }

    public void addPlayerData(String playerId, Player model) {
        if (!isNicknameUnique(model.getNickname())) {
            throw new NicknameIsTakenException(NICKNAME_ALREADY_TAKEN_MESSAGE);
        }
        Player player = players.get(playerId);
        if (player == null) {
            throw new PlayerNotFoundException(PLAYER_NOT_FOUND);
        }
        player.setNickname(model.getNickname());
        player.setNumber(model.getNumber());
        player.setBet(model.getBet());
    }

    public boolean isNicknameUnique(String nickname) {
        return players.values().stream()
                .noneMatch(player -> nickname.equals(player.getNickname()));
    }

    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }

    public void removePlayer(String playerId) {
        players.remove(playerId);
    }

    public void removeAllPlayers() {
        players.clear();
    }

    private int generateRandomNumber() {
        return random.nextInt(10) + 1;
    }
}
