/*
    Dylan Sprague
    7/7/15

    Project 1 (Tic Tac Nole) for COP 4656.
    All code is original. FSU seal obtained from https://unicomm.fsu.edu/brand/applying/seal/
    No special environment setup is needed.
    This project was tested on a device with API 19 and a 4.5" screen.

    The choice of human or computer players is hardcoded; see line 215 of this file.
    The app logic as written assumes player 1 is human; player 2 can be either human or computer.
    The AI chooses a random unoccupied cell to move to without any strategy.

    Screen rotation is locked via the manifest, using the attribute android:configChanges.
 */

package com.fsu.tictacnolebt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

//this class implements the UI for the Tic Tac Toe game and handles the main game loop
public class TicTacNole extends Activity {

    //game logic
    TicTacToeGame game;
    TicTacToePlayer playerOne;
    TicTacToePlayer playerTwo;
    TicTacToePlayer activePlayer;

    //game parameters
    String role;
    int numHumans;

    //Bluetooth game logic
    boolean btPlay = false;
    BluetoothControl btControl;
    //Received thread boolean statement
    boolean running = false;
    Thread moveReceiver; //receives moves from other device over BT

    //UI elements
    TableLayout board;
    Button[][] boardButtons;
    Button replayButton;
    TextView turnSignifier;

    //debugging
    String myTag = "TTN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tic_tac_nole);

        //get game parameters from the Intent creating this object
        Intent intent = getIntent();
        role = intent.getStringExtra(GameSelectFragment.EXTRA_KEY + ".role");
        numHumans = intent.getIntExtra(GameSelectFragment.EXTRA_KEY + ".numHumans", 1);
        if (numHumans > 1) {
            btPlay = true;
        }

        //initialize UI elements
        board = (TableLayout)findViewById(R.id.game_board);
        boardButtons = new Button[3][3];
        replayButton = (Button)findViewById(R.id.replay_button);
        turnSignifier = (TextView)findViewById(R.id.turn_signifier);

        // Initialize the BluetoothControl object for use to communicate with the other player.
        // TODO: Put a breakpoint here and try to test this with two phones. Make sure 'mSocket' is still there, etc.
        btControl = new BluetoothControl();
        btControl.setupBT();

        //wire up click listener for replay_button
        replayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newGame();

                //send message to indicate new game
                if (btPlay) {
                    btControl.sendMove("N");
                }
            }
        });

        //set up buttons that represent game board
        int buttonSize;
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                boardButtons[i][j] = (Button)((TableRow)board.getChildAt(i)).getChildAt(j);
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

        //initialize game logic
        newGame();

        //start move receiver thread for BT games
        if (btPlay) {
            receivedThread();
        }
    }

    private void makeMove(View b, boolean isSendingPlayer) {
        Log.d(myTag, "Making move - " + activePlayer.getTeam());

        TicTacToeGame.CellPosition movePos;

        switch (b.getId()){
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

        //make move in game's logic
        //the non-UI code from here down should probably be refactored into a separate method
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
            if (activePlayer.getType() == TicTacToeGame.PlayerType.COMPUTER) {
                //handle computer turn

                Log.d(myTag, "Starting computer turn - " + activePlayer.getTeam());

                //find the comp's best move, then recursively call this method with the comp's move;
                //computer move will happen and finish, then the original call will finish
                //should end with control being returned to human player
                TicTacToeCell compMove = game.findBestMove(activePlayer.getTeam());


                int compMoveRow = compMove.getPos().getIndex() / 3;
                int compMoveCol = compMove.getPos().getIndex() % 3;
                makeMove(boardButtons[compMoveRow][compMoveCol], isSendingPlayer);


            } else {
                //wait for human player to make a move
                Log.d(myTag, "Waiting for human turn " + activePlayer.getTeam());
            }

            //set turn_signifier appropriately
            if (game.getWinningPlayer() == TicTacToeGame.Player.NEITHER) {
                if (activePlayer.getTeam() == TicTacToeGame.Player.X) {
                    turnSignifier.setText(R.string.x_prompt);
                } else {
                    turnSignifier.setText(R.string.o_prompt);
                }
            }

        }
    } //end makeMove()


    //note: this may not be necessary
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_tic_tac_nole, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void newGame () {
        Log.d(myTag, "Setting up game as role: " + role);

        game = new TicTacToeGame();

        turnSignifier.setText(R.string.x_prompt);

        //reset game board display
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                boardButtons[i][j].setClickable(true);
                boardButtons[i][j].setText(R.string.blank_cell);
            }
        }

        //set player settings based on parameters passed to intent
        if (numHumans == 2) {
            playerOne = new TicTacToePlayer(TicTacToeGame.PlayerType.HUMAN, TicTacToeGame.Player.X);
            playerTwo = new TicTacToePlayer(TicTacToeGame.PlayerType.HUMAN, TicTacToeGame.Player.O);

        } else if (numHumans == 1) {
            playerOne = new TicTacToePlayer(TicTacToeGame.PlayerType.HUMAN, TicTacToeGame.Player.X);
            playerTwo = new TicTacToePlayer(TicTacToeGame.PlayerType.COMPUTER, TicTacToeGame.Player.O);
        } else {
            Log.e(myTag, "Incorrect game parameter: numHumans");
            //TODO - display Toast w/ error message
            //TODO - close activity
        }

        //X always plays first
        activePlayer = playerOne;

        //assume the host is always X, playing first
        if (role.equals("host")) {
            receiveControl();
        } else if (role.equals("client")) {
            giveControl();
        } else {
            Log.e(myTag, "Incorrect game parameter: role");
            //TODO - display Toast w/ error message
            //TODO - close activity
        }
    }

    private void handleVictory(TicTacToeGame.Player winningPlayer) {

        Log.d(myTag, "Victory found for player " + winningPlayer);

        //lock all buttons
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                boardButtons[i][j].setClickable(false);
            }
        }

        //display victory text
        if (winningPlayer == TicTacToeGame.Player.X) {
            turnSignifier.setText(R.string.x_victory);
        } else if (winningPlayer == TicTacToeGame.Player.O){
            turnSignifier.setText(R.string.o_victory);
        } else if (winningPlayer == TicTacToeGame.Player.TIE) {
            turnSignifier.setText(R.string.tie_game);
        } else {
            Log.e(myTag, "Error: handleVictory() called with no winning player");
        }
    }
    //////////////////////////////////////////////////////////////////////////
    //ADDED GAME SUPPORT LOGIC FOR BLUETOOTH - Daniel Carroll
    /////////////////////////////////////////////////////////////////////////

    /**
     * Marks the button with an X or an O and sends move if connected to bluetooth
     * @param btn
     */
    public void markButton(Button btn, String buttonNumber, boolean isSendingPlayer){

        if (activePlayer.getTeam() == TicTacToeGame.Player.X) {
            btn.setText(R.string.x_cell);
            if (btPlay && isSendingPlayer) {
                btControl.sendMove("X" + buttonNumber);
            }
        }
        else if (activePlayer.getTeam() == TicTacToeGame.Player.O) {
            btn.setText(R.string.o_cell);
            if (btPlay && isSendingPlayer) {
                btControl.sendMove("O" + buttonNumber);
            }
        }

        btn.setClickable(false);

    }


    /**
     * thread to actively get move. Splits the move into 2 part array.  1st part = mark / 2nd part =  button number
     * then passes new array to receivedMove method for part handling
     */
    public void receivedThread(){
        running =  true;

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

    /**
     * receives move char array from thread, then can mark oppenents board and lock button
     * @param move
     */
    public void receiveMove(char[] move){
        Log.d(myTag, "Receiving move: " + move);

        if (move[0] == 'n') {
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
    protected void onDestroy () {
        running = false; //stop moveReceiver thread
        super.onDestroy();
    }

    //give control to the other player, wait for input from them
    private void giveControl() {

        turnSignifier.setText(turnSignifier.getText() + "; Waiting for other player");

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                boardButtons[i][j].setClickable(false);
            }
        }

    }

    //receive control from other player
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
