package com.supinfo.ait.gameMechanics;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.supinfo.ait.players.Player;
import com.supinfo.ait.players.PointAndClickPlayer;
import com.supinfo.ait.players.AdjPlayer;
import com.supinfo.ait.players.randomTurn.R_Path;
import com.supinfo.ait.players.randomTurn.R_Single;
import com.supinfo.ait.hexBoards.GameBoard;
import com.supinfo.ait.hexBoards.Board;
import com.supinfo.ait.players.AdjSeasonPlayer;
import com.supinfo.ait.utilis.HexGameConstantes;

public class GameRunner extends Thread implements Runner {

    private GameBoard board;
    private Player red;
    private Player blue;
    private int currentPlayer = Board.GREEN;
    private boolean finished = false;
    private volatile boolean stop = false;
    private SeasonMechanics seasonPicker;
    private int gameType;
    private String commentary = "";

    public GameRunner(int size, int type, int seasoncount, int redPlayer, String[] redArgs, int bluePlayer, String[] blueArgs) {
        this.seasonPicker = new SeasonMechanics(seasoncount);
        this.board = new GameBoard(size, seasonPicker);
        this.gameType = type;
        this.red = createPlayer(redPlayer, Board.GREEN, redArgs);
        this.blue = createPlayer(bluePlayer, Board.YELLOW, blueArgs);
    }

    public GameBoard getBoard() {
        return board;
    }

    @Override
    public void run() {

        Random coinflip = new Random();

        /*
         * Main running loop
         */
        while (!finished && !stop) {

            if (this.gameType == Runner.RANDOM_TURN) {
                if (coinflip.nextBoolean() == true) {
                    this.currentPlayer = Board.YELLOW;
                } else {
                    this.currentPlayer = Board.GREEN;
                }
            }

            boolean moveAccepted = false;

            Move move = null;
            switch (currentPlayer) {
                case Board.GREEN:
                    seasonPicker.thinkingPlayer(Board.GREEN);
                    move = red.getMove();
                    break;
                case Board.YELLOW:
                    seasonPicker.thinkingPlayer(Board.YELLOW);
                    move = blue.getMove();
                    break;
                default:
                    System.err.println("invoking mystery player");
                    System.exit(1);
                    break;
            }
            try {
                moveAccepted = board.makeMove(move);
            } catch (InvalidMoveException ex) {
                Logger.getLogger(GameRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!moveAccepted) {
                System.out.println("Move was not accepted, passing on...");
            }


            /*
             * move has been accepted
             */
            if (board.checkwin(currentPlayer)) {
                notifyWin(currentPlayer);
                finished = true;
            }

            switch (currentPlayer) {
                case Board.GREEN:
                    seasonPicker.increment(Board.GREEN);
                    this.currentPlayer = Board.YELLOW;
                    break;
                case Board.YELLOW:
                    seasonPicker.increment(Board.YELLOW);
                    this.currentPlayer = Board.GREEN;
                    break;
                default:
                    System.err.println("invoking mystery player");
                    System.exit(1);
                    break;
            }
        }
    }

    public void notifyWin(int player) {
        this.finished = true;
        java.awt.Toolkit.getDefaultToolkit().beep();
        switch (player) {
            case Board.GREEN:
                System.out.println(HexGameConstantes.GREEN_PLAYER_NAME + " wins!");
                announce(HexGameConstantes.GREEN_PLAYER_NAME + " Wins!");
                break;
            case Board.YELLOW:
                System.out.println(HexGameConstantes.YELLOW_PLAYER_NAME + " wins!");
                break;
        }
    }

    public void stopGame() {
        stop = true;
        System.out.println("Stopped!");
    }

    public SeasonMechanics getSeasonPicker() {
        return seasonPicker;
    }

    private Player createPlayer(int type, int colour, String[] args) {
        Player player = null;
        switch (type) {
            case Player.R_PATH:
                player = new R_Path(this, colour, args);
                break;
            case Player.CLICK_PLAYER:
                player = new PointAndClickPlayer(this, colour);
                break;
            case Player.R_POINT:
                player = new R_Single(this, colour, args);
                break;
            case Player.ALL_PATH:
                player = new AdjPlayer(this, colour, args);
                break;
            case Player.SEASON_PATH:
                player = new AdjSeasonPlayer(this, colour, args);
                break;
            default:
                System.out.println("ERROR - no player or exception");
                break;
        }
        return player;
    }

    public Player getPlayerBlue() {
        return blue;
    }

    public Player getPlayerRed() {
        return red;
    }

    private void announce(String announcement) {
        this.commentary = announcement;
    }

    public String getCommentary() {
        return commentary;
    }
}
