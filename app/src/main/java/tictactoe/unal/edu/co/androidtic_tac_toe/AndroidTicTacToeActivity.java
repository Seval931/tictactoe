package tictactoe.unal.edu.co.androidtic_tac_toe;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.harding.tictactoe.BoardView;
import edu.harding.tictactoe.TicTacToeGame;

public class AndroidTicTacToeActivity extends AppCompatActivity {

    static final int DIALOG_QUIT_ID = 1;
    static final int DIALOG_ABOUT_ID = 2;

    private TicTacToeGame mGame;

    //Buttons making up the board
    private Button mBoardButtons[];

    //Various text displayed
    private TextView mInfoTextView;
    private TextView mScoreTextView;

    private BoardView mBoardView;

    private int humanScore = 0;
    private int androidScore = 0;
    private int tieScore = 0;
    private int selected = 0;
    private char turn;
    private boolean mGameOver;
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;
    boolean mSoundOn;
    Handler handler;
    private SharedPreferences mPrefs;
    final Runnable run = new Runnable() {
        @Override
        public void run() {
            mComputerMediaPlayer.start();
        }
    };



    /*private Firebase firebase;
    //FirebaseStorage storage = FirebaseStorage.getInstance();*/
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private  DatabaseReference mDatabase, mDatabaseSingle;
    private String gameList="Juegos";
    private String game="1";
    private char identity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mInfoTextView = (TextView) findViewById(R.id.information);
        mScoreTextView = (TextView) findViewById(R.id.score);
        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        mBoardView.setOnTouchListener(mTouchListener);

        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);

        // Restore the scores from the persistent preference data source
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundOn = mPrefs.getBoolean("sound", true);
        String difficultyLevel = mPrefs.getString("difficulty_level",
                getResources().getString(R.string.difficulty_harder));
        if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        else
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

        // Restore the scores
        humanScore = mPrefs.getInt("mHumanWins", 0);
        androidScore = mPrefs.getInt("mComputerWins", 0);
        tieScore = mPrefs.getInt("mTies", 0);
        //mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.valueOf(mPrefs.getString("Difficulty","EXPERT")));

        mDatabase=database.getReference(gameList+"/"+game);
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                                     @Override
                                                     public void onDataChange(DataSnapshot snapshot) {
                                                         JSONObject array=snapshot.getValue(JSONObject.class);
                                                         int players=0;
                                                         try {
                                                             players = array.getInt("usuarios");
                                                         }catch (Exception e){

                                                         }
                                                         switch (players){
                                                             case 11: players=21;
                                                                 identity=mGame.HUMAN_PLAYER;
                                                                 mDatabase.child("usuarios").setValue(players);
                                                                 break;
                                                             case 21: players=22;
                                                                 identity=mGame.COMPUTER_PLAYER;
                                                                 mDatabase.child("usuarios").setValue(players);
                                                                 break;
                                                             case 12: players=22;
                                                                 identity=mGame.HUMAN_PLAYER;
                                                                 mDatabase.child("usuarios").setValue(players);
                                                                 break;
                                                             default:
                                                                 identity='E';

                                                         }
                                                     }

                                                     @Override
                                                     public void onCancelled(DatabaseError databaseError) {

                                                     }
                                                 }
        );

        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                JSONObject array=dataSnapshot.getValue(JSONObject.class);
                try {
                    mGame.setBoardState(array.getString("tablero").toCharArray());
                    turn=array.getString("turno").toCharArray()[0];
                    mGameOver=array.getBoolean("gameOver");
                }
                catch (Exception e){}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
            }
        }
        );
        displayScores();

    }

    public void displayScores(){
        mScoreTextView.setText("Human:" + humanScore + "   Ties:" + tieScore + "   Android:" + androidScore);
    }

    private void startNewGame() {
        displayScores();
        mGame.clearBoard();
        mBoardView.invalidate(); //Redraw the board
        mGameOver = false;

        //Human goes first
        mInfoTextView.setText(R.string.first_human);
        turn=mGame.HUMAN_PLAYER;
        //End of startNewGame
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        //menu.add("New Game");
        //getMenuInflater().inflate(R.menu.menu_android_tic_tac_toe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        /*int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
        */

        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.settings:
                startActivityForResult(new Intent(this, Settings.class), 0);
                return true;
            case R.id.about:
                showDialog(DIALOG_ABOUT_ID);
                return true;
            case R.id.reset:
                humanScore=0;
                androidScore=0;
                tieScore=0;
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
            default:
                return false;
        }

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch (id) {

            case DIALOG_ABOUT_ID:

                Context context = getApplicationContext();
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.about_dialog, null);
                builder.setPositiveButton("OK", null);
                builder.setView(layout);
                dialog = builder.create();
                break;

            case DIALOG_QUIT_ID:
                //Create de quit confirmation dialog

                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int id) {
                                AndroidTicTacToeActivity.this.finish();
                            }
                        }).setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    private boolean setMove(char player, int location) {

        if (mGame.setMove(player, location)) {
            if(player==mGame.HUMAN_PLAYER&&mSoundOn){
                mHumanMediaPlayer.start();
            }
            if(player==mGame.COMPUTER_PLAYER&&mSoundOn) {
                mComputerMediaPlayer.start();
            }
            mBoardView.invalidate(); //Redraw the board
            return true;
        }

        return false;
    }

    //Listen for touches on the board
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            //while(turnHuman) {
            //Determine which cell was touched
            int col = (int) motionEvent.getX() / mBoardView.getBoardCellWidth();
            int row = (int) motionEvent.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;


            if (!mGameOver) {
                int winner = 0;
                if (turn == identity) {
                    if (setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
                        turn = mGame.COMPUTER_PLAYER;
                        winner = mGame.checkForWinner();
                    }
                }


                if (winner == 0 && turn != identity) {
                    mInfoTextView.setText(R.string.turn_computer);
                    winner = mGame.checkForWinner();
                }

                if (winner == 0) {
                    mInfoTextView.setText(R.string.turn_human);
                    //turnHuman = true;
                } else{
                   mGameOver=true;
                    messagesAndScore(winner);
                    //lockCells();
                }

            }

            return false;
        }
    };

    private void messagesAndScore(int winner){
        if(winner==1){
            mInfoTextView.setText(R.string.result_tie);
            tieScore += 1;
        }
        else if (winner == 2) {
            if (identity == mGame.HUMAN_PLAYER) {
                humanScore++;
                String defaultMessage = getResources().getString(R.string.result_human_wins);
                mInfoTextView.setText(mPrefs.getString("victory_message", defaultMessage));
                //lockCells();
            }
            else {
                mInfoTextView.setText(R.string.result_computer_wins);
                androidScore ++;
            }
        } else if (winner == 3) {
            if (identity == mGame.HUMAN_PLAYER) {
                humanScore++;
                String defaultMessage = getResources().getString(R.string.result_human_wins);
                mInfoTextView.setText(mPrefs.getString("victory_message", defaultMessage));
                //lockCells();
            }
            else {
                mInfoTextView.setText(R.string.result_computer_wins);
                androidScore ++;
            }
            //lockCells();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.android);

    }


    @Override
    protected void onPause() {
        super.onPause();

        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putCharArray("board", mGame.getBoardState());
        outState.putBoolean("mGameOver", mGameOver);
        outState.putInt("mHumanWins", Integer.valueOf(humanScore));
        outState.putInt("mComputerWins", Integer.valueOf(androidScore));
        outState.putInt("mTies", Integer.valueOf(tieScore));
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putChar("mGoFirst", turn);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", humanScore);
        ed.putInt("mComputerWins", androidScore);
        ed.putInt("mTies", tieScore);
        ed.putString("Difficulty",mGame.getDifficultyLevel()+"");
        ed.commit();
    }


}





