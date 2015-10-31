package com.fsu.tictacnolebt;


public class TicTacToePlayer {

    private TicTacToeGame.PlayerType type; //human or computer
    private TicTacToeGame.Player team; //X or O

    public TicTacToePlayer(TicTacToeGame.PlayerType type, TicTacToeGame.Player team) {
        this.type = type;
        this.team = team;
    }

    public TicTacToeGame.PlayerType getType() {
        return type;
    }

    public void setType(TicTacToeGame.PlayerType type) {
        this.type = type;
    }

    public TicTacToeGame.Player getTeam() {
        return team;
    }

    public void setTeam(TicTacToeGame.Player team) {
        this.team = team;
    }

}
