package com.company.testtask.constant;

public class Constants {

    private Constants (){}

    public static final String LOCALHOST = "ws://localhost:";
    public static final String GAME_PATH = "/game";

    public static final String WINNING_MESSAGE = "You won! Payout: ";
    public static final String LOSING_MESSAGE = "You lost.";
    public static final String WINNERS_LIST_MESSAGE = "Winners:\n";
    public static final String INVALID_PLAYER_NUMBER_MESSAGE = "Validation failed: number must be greater than or equal to 1; ";
    public static final String NICKNAME_ALREADY_TAKEN_MESSAGE = "Nickname is already taken";
    public static final String SESSION_REMOVED_MESSAGE = "Invalid player data. You have been removed from the game.";
    public static final String FAILED_SENDING_MESSAGE = "Failed sending message to session: {}";
    public static final String VALIDATION_FAILED = "Validation failed: ";
    public static final String PLAYER_NOT_FOUND = "Player not found";

    public static final String TEST_PLAYER_NAME = "Player_";
    public static final String TEST_PLAYER_ID = "playerId";
    public static final String TEST_SESSION = "session1";
}
