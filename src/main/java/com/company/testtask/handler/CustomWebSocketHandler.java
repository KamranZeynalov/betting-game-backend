package com.company.testtask.handler;

import com.company.testtask.exception.NicknameIsTakenException;
import com.company.testtask.exception.PlayerNotFoundException;
import com.company.testtask.model.Player;
import com.company.testtask.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.company.testtask.constant.Constants.SESSION_REMOVED_MESSAGE;
import static com.company.testtask.constant.Constants.FAILED_SENDING_MESSAGE;
import static com.company.testtask.constant.Constants.VALIDATION_FAILED;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new HashSet<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final GameService gameService;

    private final Validator validator;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        gameService.registerPlayer(session.getId(), new Player());
        startRound();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        try {
            Player player = mapper.readValue(message.getPayload(), Player.class);
            validatePlayer(player);
            gameService.addPlayerData(session.getId(), player);
        } catch (ConstraintViolationException e) {
            sendValidationErrorMessage(session, e);
        } catch (PlayerNotFoundException | NicknameIsTakenException e) {
            sendMessage(session, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        gameService.removePlayer(session.getId());
    }

    private void startRound() {
        scheduler.schedule(this::endRound, GameService.ROUND_DURATION, TimeUnit.MILLISECONDS);
    }

    private void endRound() {
        removePlayerSessionWithInvalidData();
        sessions.forEach(session -> {
            String playerId = session.getId();
            String resultMessage = gameService.endRound(playerId);
            sendMessage(session, resultMessage);
        });

        String winnersList = gameService.getWinnersList();
        sessions.forEach(session -> sendMessage(session, winnersList));
    }

    private void removePlayerSessionWithInvalidData() {
        Set<WebSocketSession> invalidSessions = sessions.stream()
                .filter(session -> {
                    Player player = gameService.getPlayer(session.getId());
                    boolean isValid = player != null && player.getNickname() != null && player.getBet() != null;
                    if (!isValid) {
                        sendMessage(session, SESSION_REMOVED_MESSAGE);
                        return true;
                    }
                    return false;
                }).collect(Collectors.toSet());

        sessions.removeAll(invalidSessions);

        invalidSessions.stream()
                .map(WebSocketSession::getId)
                .forEach(gameService::removePlayer);
    }

    private void sendMessage(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            log.error(FAILED_SENDING_MESSAGE, session.getId(), e);
        }
    }

    private void validatePlayer(Player player) {
        Set<ConstraintViolation<Player>> violations = validator.validate(player);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void sendValidationErrorMessage(WebSocketSession session, ConstraintViolationException e) {
        StringBuilder errorMessage = new StringBuilder(VALIDATION_FAILED);
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            errorMessage.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append("; ");
        }
        sendMessage(session, errorMessage.toString());
    }

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}

