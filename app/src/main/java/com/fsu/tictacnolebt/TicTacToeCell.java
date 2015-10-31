package com.fsu.tictacnolebt;

import android.widget.Button;

public class TicTacToeCell {

    private final TicTacToeGame.CellPosition pos;
    private TicTacToeGame.Player owner;
    private Button uiCell; //UI element associated with this cell; added to allow mapping from TicTacToeCell to Button

    public TicTacToeCell(TicTacToeGame.CellPosition pos, TicTacToeGame.Player owner) {
        this.pos = pos;
        this.owner = owner;
    }

    public TicTacToeCell(TicTacToeGame.CellPosition pos, TicTacToeGame.Player owner, Button uiCell) {
        this.pos = pos;
        this.owner = owner;
        this.uiCell = uiCell;
    }

    public TicTacToeGame.CellPosition getPos() {
        return pos;
    }

    public void setOwner(TicTacToeGame.Player owner) {
        this.owner = owner;
    }

    public TicTacToeGame.Player getOwner() {
        return owner;
    }

    public Button getUiCell() {
        return uiCell;
    }

    public void setUiCell(Button uiCell) {
        this.uiCell = uiCell;
    }
}
