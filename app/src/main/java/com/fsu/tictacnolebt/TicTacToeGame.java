/*
    Dylan Sprague
    7/7/15

 */



package com.fsu.tictacnolebt;


import android.util.Log;

import java.util.Random;

//handles game logic and state for TicTacToe
//this class assumes there are two players, X and O. It does not interact with the UI at all.
//clients interact with the game via the public methods of this class
//the game is represented by the internal state (gameBoard, winningPlayer, currentPlayer).
public class TicTacToeGame {

    public enum Player {X, O, TIE, NEITHER}
    public enum PlayerType {HUMAN, COMPUTER}
    public enum CellPosition {
        TOP_LEFT(0), TOP_CENTER(1), TOP_RIGHT(2),
        MID_LEFT(3), MID_CENTER(4), MID_RIGHT(5),
        BOT_LEFT(6), BOT_CENTER(7), BOT_RIGHT(8);

        private final int index;

        CellPosition(int i) {
            index = i;
        }

        public int getIndex() {
            return index;
        }
    }

    //fields
    private TicTacToeCell[][] gameBoard;
    private Player winningPlayer;
    private Player currentPlayer;
    private Random rng; //used for picking random CPU moves

    //constructor
    public TicTacToeGame() {

        rng = new Random();

        currentPlayer = Player.X; //start with X to move
        winningPlayer = Player.NEITHER;

        //initialize game board
        gameBoard = new TicTacToeCell[3][3];
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                gameBoard[i][j] = new TicTacToeCell(CellPosition.values()[3 * i + j], Player.NEITHER);
            }
        }
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getWinningPlayer() {
        return winningPlayer;
    }

    //switch current player to the other player
    public void nextPlayer() {
        if (currentPlayer == Player.X)
            currentPlayer = Player.O;
        else if (currentPlayer == Player.O)
            currentPlayer = Player.X;
    }

    public boolean makeMove (Player p, TicTacToeCell c) {
        //Log.d("myTag", "game logic - player " + p +
               // " making move to (" + (c.index / 3) + "," + (c.index % 3) + ")");

        if (c.getOwner() == Player.NEITHER) {
            c.setOwner(p);
            return true;
        } else {
            Log.e("myTag", "cell " + c.toString() + " already owned");
            return false;
        }

    }

    public boolean makeMove (Player p, CellPosition c) {
        TicTacToeCell cell = gameBoard[c.getIndex() / 3][c.getIndex() % 3];
        return makeMove(p, cell);
    }

    //check if a player has won, setting winningPlayer appropriately
    public void checkForWin() {

        //check if a player has already won; if so, exit
        if (winningPlayer != Player.NEITHER)
            return;

        /* we can now assume neither player has a winning position;
        as soon as a win is found for either player, the checks for winningPlayer == NEITHER
        will make the method skip to the end
        */

        //check rows
        for (int i = 0; i < 3 && winningPlayer == Player.NEITHER; ++i) {
            winningPlayer = checkCellSet(gameBoard[0][i], gameBoard[1][i], gameBoard[2][i]);
        }

        //check cols
        for (int i = 0; i < 3 && winningPlayer == Player.NEITHER; ++i) {
            winningPlayer = checkCellSet(gameBoard[i][0], gameBoard[i][1], gameBoard[i][2]);
        }

        //check diags
        if (winningPlayer == Player.NEITHER)
            winningPlayer = checkCellSet(gameBoard[0][0], gameBoard[1][1], gameBoard[2][2]);

        if (winningPlayer == Player.NEITHER)
            winningPlayer = checkCellSet(gameBoard[0][2], gameBoard[1][1], gameBoard[2][0]);

        //if no winning player has been found, check for ties
        //if no unoccupied cell is found, game is tied
        boolean isTie = true;
        if (winningPlayer == Player.NEITHER) {
            for (int i = 0; i < 3 && isTie; ++i) {
                for (int j = 0; j < 3 && isTie; ++j) {
                    if (gameBoard[i][j].getOwner() == Player.NEITHER) {
                        isTie = false;
                    }
                }
            }
        } else {
            isTie = false;
        }

        if(isTie)
            winningPlayer = Player.TIE;


    } //end checkForWin()

    private Player checkCellSet (TicTacToeCell cell1, TicTacToeCell cell2, TicTacToeCell cell3) {
        if ((cell1.getOwner() == cell2.getOwner()) && (cell2.getOwner() == cell3.getOwner()))
            return cell1.getOwner(); //even if all three cells are unclaimed, NEITHER will be (correctly) returned
        else
            return Player.NEITHER;

    }

    //find the best move for the given player
    //TODO - replace with proper AI logic
    public TicTacToeCell findBestMove (Player p) {

        //randomly pick cells until an unoccupied cell is found
        int randRow, randCol;

        do {
            randRow = rng.nextInt(3);
            randCol = rng.nextInt(3);
        } while(gameBoard[randRow][randCol].getOwner() != Player.NEITHER);
        Log.d("myTag", "Moving to (" + randRow + "," + randCol + ")");

        return gameBoard[randRow][randCol];
    }
}
