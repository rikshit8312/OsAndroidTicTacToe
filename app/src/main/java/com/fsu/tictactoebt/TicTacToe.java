

package com.fsu.tictactoebt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TicTacToe extends Activity {

    TicTacToeGame game;
    TicTacToePlayer playerOne;
    TicTacToePlayer playerTwo;
    TicTacToePlayer activePlayer;

    String role;
    int numHumans;

    boolean btPlay = false;
    BluetoothControl btControl;
    boolean running = false;
    Thread moveReceiver;

    TableLayout board;
    Button[][] boardButtons;
    Button replayButton;
    TextView turnSignifier;

    String myTag = "TTN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_toe);

        Intent intent = getIntent();
        role = intent.getStringExtra(GameSelectFragment.EXTRA_KEY + ".role");
        numHumans = intent.getIntExtra(GameSelectFragment.EXTRA_KEY + ".numHumans", 1);
        if (numHumans > 1) {
            btPlay = true;
        }

        board = (TableLayout) findViewById(R.id.game_board);
        boardButtons = new Button[3][3];
        replayButton = (Button) findViewById(R.id.replay_button);
        turnSignifier = (TextView) findViewById(R.id.turn_signifier);

        btControl = new BluetoothControl();
        btControl.setupBT();

        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();

                btControl.sendMove("N");

            }
        });

        int buttonSize;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                boardButtons[i][j] = (Button) ((TableRow) board.getChildAt(i)).getChildAt(j);
                boardButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeMove(v, true);
                        if (btPlay) {
                            giveControl();
                        }
                    }
                });
            }
        }

        newGame();

        if (btPlay) {
            receivedThread();
        }
    }

    private void makeMove(View b, boolean isSendingPlayer) {
        Log.d(myTag, "Making move - " + activePlayer.getTeam());

        TicTacToeGame.CellPosition movePos;

        switch (b.getId()) {
            case R.id.game_top_left:
                movePos = TicTacToeGame.CellPosition.TOP_LEFT;
                markButton(boardButtons[0][0], "00", isSendingPlayer);
                break;
            case R.id.game_top_center:
                movePos = TicTacToeGame.CellPosition.TOP_CENTER;
                markButton(boardButtons[0][1], "01", isSendingPlayer);
                break;
            case R.id.game_top_right:
                movePos = TicTacToeGame.CellPosition.TOP_RIGHT;
                markButton(boardButtons[0][2], "02", isSendingPlayer);
                break;
            case R.id.game_mid_left:
                movePos = TicTacToeGame.CellPosition.MID_LEFT;
                markButton(boardButtons[1][0], "10", isSendingPlayer);
                break;
            case R.id.game_mid_center:
                movePos = TicTacToeGame.CellPosition.MID_CENTER;
                markButton(boardButtons[1][1], "11", isSendingPlayer);
                break;
            case R.id.game_mid_right:
                movePos = TicTacToeGame.CellPosition.MID_RIGHT;
                markButton(boardButtons[1][2], "12", isSendingPlayer);
                break;
            case R.id.game_bot_left:
                movePos = TicTacToeGame.CellPosition.BOT_LEFT;
                markButton(boardButtons[2][0], "20", isSendingPlayer);
                break;
            case R.id.game_bot_center:
                movePos = TicTacToeGame.CellPosition.BOT_CENTER;
                markButton(boardButtons[2][1], "21", isSendingPlayer);
                break;
            case R.id.game_bot_right:
                movePos = TicTacToeGame.CellPosition.BOT_RIGHT;
                markButton(boardButtons[2][2], "22", isSendingPlayer);
                break;
            default:
                Log.e("myTag", "Erroneous button press captured");
                return;
        }

        game.makeMove(game.getCurrentPlayer(), movePos);


        game.checkForWin();

        if (game.getWinningPlayer() != TicTacToeGame.Player.NEITHER) {
            handleVictory(game.getWinningPlayer());
        } else {
            game.nextPlayer();
            if (activePlayer == playerOne) {
                activePlayer = playerTwo;
            } else if (activePlayer == playerTwo) {
                activePlayer = playerOne;
            }

            Log.d(myTag, "Waiting for human turn " + activePlayer.getTeam());


            if (game.getWinningPlayer() == TicTacToeGame.Player.NEITHER) {
                if (activePlayer.getTeam() == TicTacToeGame.Player.X) {
                    turnSignifier.setText(R.string.x_prompt);
                } else {
                    turnSignifier.setText(R.string.o_prompt);
                }
            }

        }
    }

    private void newGame() {
        Log.d(myTag, "Setting up game as role: " + role);

        game = new TicTacToeGame();

        turnSignifier.setText(R.string.x_prompt);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                boardButtons[i][j].setClickable(true);
                boardButtons[i][j].setText(R.string.blank_cell);
            }
        }

        if (numHumans == 2) {
            playerOne = new TicTacToePlayer(TicTacToeGame.PlayerType.HUMAN, TicTacToeGame.Player.X);
            playerTwo = new TicTacToePlayer(TicTacToeGame.PlayerType.HUMAN, TicTacToeGame.Player.O);

        } else {
            Log.e(myTag, "Incorrect game parameter: numHumans");

        }

        activePlayer = playerOne;

        if (role.equals("host")) {
            receiveControl();
        } else if (role.equals("client")) {
            giveControl();
        } else {
            Log.e(myTag, "Incorrect game parameter: role");
        }
    }

    private void handleVictory(TicTacToeGame.Player winningPlayer) {

        Log.d(myTag, "Victory found for player " + winningPlayer);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                boardButtons[i][j].setClickable(false);
            }
        }

        if (winningPlayer == TicTacToeGame.Player.X) {
            turnSignifier.setText(R.string.x_victory);
        } else if (winningPlayer == TicTacToeGame.Player.O) {
            turnSignifier.setText(R.string.o_victory);
        } else if (winningPlayer == TicTacToeGame.Player.TIE) {
            turnSignifier.setText(R.string.tie_game);
        } else {
            Log.e(myTag, "Error: handleVictory() called with no winning player");
        }
    }

    public void markButton(Button btn, String buttonNumber, boolean isSendingPlayer) {

        if (activePlayer.getTeam() == TicTacToeGame.Player.X) {
            btn.setText(R.string.x_cell);
            btn.setTextColor(getResources().getColor(R.color.colorX));
            if (btPlay && isSendingPlayer) {
                btControl.sendMove("X" + buttonNumber);
            }
        } else if (activePlayer.getTeam() == TicTacToeGame.Player.O) {
            btn.setText(R.string.o_cell);
            btn.setTextColor(getResources().getColor(R.color.color0));
            if (btPlay && isSendingPlayer) {
                btControl.sendMove("O" + buttonNumber);
            }
        }

        btn.setClickable(false);

    }



    public void receivedThread() {
        running = true;

        moveReceiver = new Thread() {

            public void run() {

                while (running) {
                    char[] rMove;
                    rMove = btControl.receiveMove().toCharArray();
                    receiveMove(rMove);

                }
            }
        };
        moveReceiver.start();
    }

    public void receiveMove(char[] move) {
        Log.d(myTag, "Receiving move: " + move);

        if (move[0] == 'N') {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    newGame();
                }
            });
        } else {

            final int moveRow, moveCol;


            if (Character.isDigit(move[1])) {
                moveRow = Character.getNumericValue(move[1]);
            } else {
                Log.e(myTag, "Malformatted move string - cannot parse row");
                return;
            }

            if (Character.isDigit(move[2])) {
                moveCol = Character.getNumericValue(move[2]);
            } else {
                Log.e(myTag, "Malformatted move string - cannot parse column");
                return;
            }

            Log.d(myTag, "Making received move");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    makeMove(boardButtons[moveRow][moveCol], false);

                    if (btPlay) {
                        receiveControl();
                    }
                }
            });
        }

    }

    @Override
    protected void onDestroy() {
        running = false;
        super.onDestroy();
    }

    private void giveControl() {

        turnSignifier.setText(turnSignifier.getText() + "; Waiting for other player");

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                boardButtons[i][j].setClickable(false);
            }
        }

    }

    private void receiveControl() {

        if (game.getWinningPlayer() == TicTacToeGame.Player.NEITHER) {

            turnSignifier.setText(turnSignifier.getText() + "; Your turn!");

            for (int i = 0; i < 3; ++i) {
                for (int j = 0; j < 3; ++j) {
                    if (boardButtons[i][j].getText().equals("")) {
                        boardButtons[i][j].setClickable(true);
                    }


                }
            }
        }
    }

}
