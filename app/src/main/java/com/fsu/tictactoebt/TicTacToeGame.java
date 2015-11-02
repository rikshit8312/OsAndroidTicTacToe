

package com.fsu.tictactoebt;


import android.util.Log;

public class TicTacToeGame {

    public enum Player {X, O, TIE, NEITHER}

    public enum PlayerType {HUMAN}

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

    private TicTacToeCell[][] gameBoard;
    private Player winningPlayer;
    private Player currentPlayer;

    public TicTacToeGame() {


        currentPlayer = Player.X; //start with X to move
        winningPlayer = Player.NEITHER;

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

    public void nextPlayer() {
        if (currentPlayer == Player.X)
            currentPlayer = Player.O;
        else if (currentPlayer == Player.O)
            currentPlayer = Player.X;
    }

    public boolean makeMove(Player p, TicTacToeCell c) {

        if (c.getOwner() == Player.NEITHER) {
            c.setOwner(p);
            return true;
        } else {
            Log.e("myTag", "cell " + c.toString() + " already owned");
            return false;
        }

    }

    public boolean makeMove(Player p, CellPosition c) {
        TicTacToeCell cell = gameBoard[c.getIndex() / 3][c.getIndex() % 3];
        return makeMove(p, cell);
    }

    public void checkForWin() {

        if (winningPlayer != Player.NEITHER)
            return;

        for (int i = 0; i < 3 && winningPlayer == Player.NEITHER; ++i) {
            winningPlayer = checkCellSet(gameBoard[0][i], gameBoard[1][i], gameBoard[2][i]);
        }

        for (int i = 0; i < 3 && winningPlayer == Player.NEITHER; ++i) {
            winningPlayer = checkCellSet(gameBoard[i][0], gameBoard[i][1], gameBoard[i][2]);
        }

        if (winningPlayer == Player.NEITHER)
            winningPlayer = checkCellSet(gameBoard[0][0], gameBoard[1][1], gameBoard[2][2]);

        if (winningPlayer == Player.NEITHER)
            winningPlayer = checkCellSet(gameBoard[0][2], gameBoard[1][1], gameBoard[2][0]);

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

        if (isTie)
            winningPlayer = Player.TIE;


    }

    private Player checkCellSet(TicTacToeCell cell1, TicTacToeCell cell2, TicTacToeCell cell3) {
        if ((cell1.getOwner() == cell2.getOwner()) && (cell2.getOwner() == cell3.getOwner())) {
            return cell1.getOwner();
        } else
            return Player.NEITHER;

    }

}
