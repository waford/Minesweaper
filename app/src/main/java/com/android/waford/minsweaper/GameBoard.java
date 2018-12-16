package com.android.waford.minsweaper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class GameBoard extends AppCompatActivity {

    private static final String LOG_TAG = GameBoard.class.getSimpleName();
    private static final String HIGHSCORE_FILENAME = "highScores.txt";
    private int bombDensity; //1 bomb per bombDensity squares,
    private int numCols;
    private int numRows;
    private int boardHeight;
    private TableLayout gameBoard;
    private int tileSize;
    private int bombCount;
    private int unOpened;
    private boolean firstClick;
    private int numCorrectFlagged;
    private int numFlagsLeft;
    private TextView timer;
    private long startTime = 0;
    private TextView flagsLeft;
    private AdView gameBoardAdView;
    private Button resetButton;
    private String[][] highScoreData;
    private int highScoreCol;
    private boolean highScoreClick;
    private AlertDialog.Builder winAlertBuilder;
    private AlertDialog loseAlert;


    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            //Timer function
            long millies = startTime += 500;
            int seconds = (int) (millies / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            timer.setText(String.format("%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);
        gameBoard = findViewById(R.id.GameBoard);
        flagsLeft = findViewById(R.id.Flags_Left);
        timer = findViewById(R.id.Timer);
        resetButton = findViewById(R.id.ResetButton);
        Intent intent = getIntent();
        numCols = intent.getIntExtra("NUM_COLS", 5);
        highScoreCol = intent.getIntExtra("HIGH_SCORE_COL", 0);
        bombDensity = intent.getIntExtra("BOMB_DENSITY", 6);
        flagsLeft = findViewById(R.id.Flags_Left);
        init();
        makeBoard(tileSize);
        alertInit();
        adInit();
    }

    private void init() {
        tileSize = setTileSize();
        bombCount = 0;
        numFlagsLeft = 0;
        boardHeight = 0;
        numCorrectFlagged = 0;
        firstClick = true;
        highScoreClick = true;
        try{
            FileInputStream fis = this.openFileInput(HIGHSCORE_FILENAME);
            ObjectInputStream in = new ObjectInputStream(fis);
            highScoreData = (String[][]) in.readObject();
        } catch(IOException | ClassNotFoundException e) {
            Log.d(LOG_TAG, e.toString());
        }
    }

    private void alertInit() {
        winAlertBuilder = new AlertDialog.Builder(this);
        winAlertBuilder.setTitle("You Won!");
        winAlertBuilder.setMessage("Test");
        AlertDialog.Builder loseAlertBuilder = new AlertDialog.Builder(this);
        loseAlertBuilder.setTitle("Tough Loss");
        loseAlertBuilder.setMessage("Sorry you didn't quite manage to sweep all those mines." + "\nWould you like to play again?"); // Maybe add some random lose messages
        loseAlertBuilder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buttonResetBoard(null);
                dialog.dismiss();
            }
        });
        loseAlertBuilder.setNegativeButton("Not right now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                GameBoard.super.finish();
                //Back naviagtion

            }
        });
        loseAlert = loseAlertBuilder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!firstClick) {
            timerHandler.postDelayed(timerRunnable, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private void adInit() {
        MobileAds.initialize(this, "ca-app-pub-3203445673322999~8135639616");
        gameBoardAdView = findViewById(R.id.gameBoardBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        gameBoardAdView.loadAd(adRequest);
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            boardHeight = gameBoard.getHeight();
        }
    }

    private void makeBoard(int tileSize) {
        // Creates the gameboard imgTile.getCol(), imgTile.getRow()
        int[][] board = generateBombs();
        for (int i = 0; i < numRows; i++) {
            TableRow row = new TableRow(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(tileSize, tileSize);
            for (int j = 0; j < numCols; j++) {
                int numBombs = board[i][j];
                TileButton tile = new TileButton(this, i, j, numBombs);
                tile.setImageResource(R.drawable.tile);
                tile.setLayoutParams(params);
                setTileImageClick(tile, board.length, board[i].length);
                row.addView(tile); //adds tile to row
            }
            gameBoard.addView(row, i); //adds row of tiles to gameboard
        }
        flagsLeft.setText(String.format("%03d", numFlagsLeft));

    }

    private void checkWin(boolean isBomb) {
        //Checks if there is a win based on if the number of correctly flagged bombs is equal to the
        //number of bombs. Should add another win condition not based on flags
        //boolean flagWin = numCorrectFlagged == this.bombCount;
        boolean unOpenedWin = (unOpened == bombCount) && !isBomb;
        if (unOpenedWin) {
            timerHandler.removeCallbacks(timerRunnable);
            for (int i = 0; i < numRows; i++) { //Uncovers all the non-clicked tiles
                for (int j = 0; j < numCols; j++) {
                    TileButton imgTile = (TileButton) ((TableRow) gameBoard.getChildAt(i)).getChildAt(j);
                    if (!imgTile.getIsClicked() && imgTile.getNumBombs() != 9) {
                        imgTile.performClick();
                    } else {
                        imgTile.setIsClicked(true);
                        imgTile.setIsMarked(true);
                    }
                }
            }
            checkHighScore();
        }
    }

    private void checkHighScore() {
        String testTime = timer.getText().toString();
        for(int i = 0; i < highScoreData[highScoreCol].length; i++) {
            if((testTime.compareTo(highScoreData[highScoreCol][i]) < 0 || highScoreData[highScoreCol][i].equals("NaN\n")) && highScoreClick) {



                String[] newHs = (String[]) replaceAtPosition(highScoreData[highScoreCol], testTime, i);
                buildAddHighScoreAlert(newHs, i);
                highScoreData[highScoreCol] = newHs;
                highScoreClick = false;
                try{
                    FileOutputStream fos = this.openFileOutput(HIGHSCORE_FILENAME, MODE_PRIVATE);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(highScoreData);
                    fos.close();
                    oos.close();
                } catch(IOException e) {
                    Log.d(LOG_TAG, e.toString());
                }
                break;
            }
        }

    }

    private Object[] replaceAtPosition(Object[] items, Object replace, int replacePosition) {
        //Places object at index replacePosition, and shifts everything down, removing the last element
        for (int i = items.length - 1; i > replacePosition; i--) {
            items[i] = items[i - 1];
        }
        items[replacePosition] = replace + "\n";
        return items;
    }

    private void buildAddHighScoreAlert(String[] highScores, int index) {
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View newHighScoreLayout = layoutInflater.inflate(R.layout.win_alert, null);
        LinearLayout mainLayout = newHighScoreLayout.findViewById(R.id.MainLeaderBoard);
        for(int i = 0; i< highScores.length; i++) {
            if(i != index){
                TextView tmp = new TextView(this);
                tmp.setText(highScores[i]);
                tmp.setTextColor(getResources().getColor(R.color.blue));
                mainLayout.addView(tmp);
            } else {
                EditText tmp = new EditText(this);
                tmp.setHint("Place Holder"); //Add names to highscore data
            }

        }

        winAlertBuilder.setPositiveButton("Play again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buttonResetBoard(null);
                dialog.dismiss();
            }
        });
        winAlertBuilder.setNegativeButton("No I'm done for now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                GameBoard.super.finish();
            }
        });
    }

    private void lose(TileButton imgTile) {
        //Called when a mine tile is pressed
        timerHandler.removeCallbacks(timerRunnable);
        imgTile.setImageResource(R.drawable.minelost);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                TileButton tile = (TileButton) ((TableRow) gameBoard.getChildAt(i)).getChildAt(j);
                if(!tile.getIsClicked()) {
                    if (tile.getNumBombs() == 9 && !tile.getIsMarked()) {
                        tile.setImageResource(R.drawable.mine);
                    }
                    tile.setIsClicked(true);
                    tile.setIsMarked(false);
                }
            }
        }
        resetButton.setBackgroundResource(R.drawable.red_reset_tile);
        showLoseAlert();
    }

    private void showLoseAlert(){
        //changes a few of the defaul alert view parameters
        loseAlert.show();
        TextView tmp = loseAlert.findViewById(android.R.id.message);
        tmp.setTextSize(15f);
        tmp.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
        Button positiveButton = loseAlert.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.blue));

    }
    public void buttonResetBoard(View view) {
        resetBoard(); //Resets board via button
        unOpened = numRows * numCols;
        numCorrectFlagged = 0;
        resetButton.setBackgroundResource(R.drawable.blue_reset_tile);
        timer.setText(getString(R.string.startTimer));
        timerHandler.removeCallbacks(timerRunnable);
        firstClick = true;

    }

    private void resetBoard() {
        gameBoard.removeAllViews();
        makeBoard(tileSize);
    }

    private void tileClick(TileButton tile, int maxRow, int maxCol) {
        if (tile != null && !tile.getIsClicked()) {
            unOpened--;
        }
        if (firstClick) {
            while (tile.getNumBombs() != 0) {
                resetBoard();
                TableRow tileRow = (TableRow) gameBoard.getChildAt(tile.getRow());
                tile = (TileButton) tileRow.getChildAt(tile.getCol());
                Log.d(LOG_TAG, "First click was a mine");
            }
            firstClick = false;
            startTime = 0;
            timerHandler.postDelayed(timerRunnable, 0);
        }
        Log.d(LOG_TAG, "Unopened: " + Integer.toString(unOpened));
        Log.d(LOG_TAG, "Status of tile [" + Integer.toString(tile.getRow()) + "," + Integer.toString(tile.getCol()) + "] clicked: " + Boolean.toString(tile.getIsClicked()));
        if (tile.getIsMarked()) {
            return;
        } else if (tile.getIsClicked()) {
            int centerRow = tile.getRow();
            int centerCol = tile.getCol();
            int correctNumBombs = tile.getNumBombs();
            int surroundingBombs = 0;
            ArrayList<Integer> rows = new ArrayList<>();
            ArrayList<Integer> cols = new ArrayList<>();
            for (int i = -1; i <= 1; i++) {
                int tmpRow = centerRow + i;
                for (int j = -1; j <= 1; j++) {
                    int tmpCol = centerCol + j;
                    boolean diffSquare = i != 0 || j != 0;
                    try {
                        TableRow tileRow = (TableRow) gameBoard.getChildAt(tmpRow);
                        TileButton testTile = (TileButton) tileRow.getChildAt(tmpCol);
                        boolean isClicked = testTile.getIsClicked();
                        if (diffSquare && !isClicked) {
                            if (testTile.getNumBombs() == 9 && testTile.getIsMarked()) {
                                surroundingBombs++;
                            } else {
                                rows.add(tmpRow);
                                cols.add(tmpCol);
                            }
                        }
                    } catch (Exception e) {
                        Log.d(LOG_TAG, e.toString());
                    }
                }
            }
            if (correctNumBombs == surroundingBombs) {
                for (int i = 0; i < rows.size(); i++) {
                    TableRow tileRow = (TableRow) gameBoard.getChildAt(rows.get(i));
                    TileButton testTile = (TileButton) tileRow.getChildAt(cols.get(i));
                    tileClick(testTile, maxRow, maxCol);
                }
            }
        } else {
            int numBombs = tile.getNumBombs();
            switch (numBombs) {
                case 0:
                    tile.setIsClicked(true);

                    tile.setImageResource(R.drawable.blank_square);
                    int centerCol = tile.getCol();
                    int centerRow = tile.getRow();
                    for (int i = -1; i <= 1; i++) {
                        int tmpRow = centerRow + i;
                        if (tmpRow < 0 || tmpRow > maxRow) {
                            continue;
                        }
                        for (int j = -1; j <= 1; j++) {
                            try {
                                int tmpCol = centerCol + j;
                                if (tmpCol < 0 || tmpCol > maxCol) {
                                    continue;
                                }
                                TableRow tileRow = (TableRow) gameBoard.getChildAt(tmpRow);
                                TileButton testTile = (TileButton) tileRow.getChildAt(tmpCol);
                                int tmpNumBombs = testTile.getNumBombs();
                                if (tmpNumBombs < 9 && (i != 0 || j != 0) && !testTile.getIsClicked()) {
                                    Log.d(LOG_TAG, "Button at row: " + Integer.toString(tmpRow) + " col: " + Integer.toString(tmpCol) + " was clicked");
                                    tileClick(testTile, maxRow, maxCol);
                                }
                            } catch (NullPointerException e) {
                                Log.d(LOG_TAG, "Threw a null pointer exception");
                            }
                        }
                    }
                    break;
                case 1:
                    tile.setImageResource(R.drawable.one_square);
                    tile.setIsClicked(true);
                    break;
                case 2:
                    tile.setImageResource(R.drawable.two_square);
                    tile.setIsClicked(true);
                    break;
                case 3:
                    tile.setImageResource(R.drawable.three_square);
                    tile.setIsClicked(true);
                    break;
                case 4:
                    tile.setImageResource(R.drawable.four_square);
                    tile.setIsClicked(true);
                    break;
                case 5:
                    tile.setImageResource(R.drawable.five_square);
                    tile.setIsClicked(true);
                    break;
                case 6:
                    tile.setImageResource(R.drawable.six_square);
                    tile.setIsClicked(true);
                    break;
                case 7:
                    tile.setImageResource(R.drawable.seven_square);
                    tile.setIsClicked(true);
                    break;
                case 8:
                    tile.setImageResource(R.drawable.eight_square);
                    tile.setIsClicked(true);
                    break;
                case 9:
                    tile.setIsClicked(true);
                    lose(tile);
                    break;
            }
            if (firstClick) {
                Log.d(LOG_TAG, "Setting first click to false");
                firstClick = false;
            }

        }
        checkWin(tile.getNumBombs() == 9);
    }

    private void markTile(TileButton imgTile) {
        if (imgTile.getIsClicked()) {
            return;
        }
        if (imgTile.getIsMarked()) {
            imgTile.setImageResource(R.drawable.tile);
            imgTile.setIsMarked(false);
            if (imgTile.getNumBombs() == 9) {
                numCorrectFlagged--;
            }
            numFlagsLeft++;

        } else {
            imgTile.setImageResource(R.drawable.flag);
            imgTile.setIsMarked(true);
            if (imgTile.getNumBombs() == 9) {
                numCorrectFlagged++;
            }
            numFlagsLeft--;
        }
        flagsLeft.setText(String.format("%03d", numFlagsLeft));
        checkWin(imgTile.getNumBombs() == 9);


    }

    private void setTileImageClick(final TileButton imgTile, final int maxRow, final int maxCol) {
        imgTile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tileClick(imgTile, maxRow, maxCol);
            }
        });

        imgTile.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!imgTile.getIsClicked()) {
                    Log.d(LOG_TAG, "Long click triggered");
                    markTile(imgTile);
                    return true;
                }
                return false;

            }
        });
    }

    private int setTileSize() {
        //Uses the width of the screen and height of the view to determin the the side length for tiles
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int pixelDPI = (int) Resources.getSystem().getDisplayMetrics().density;
        int tileSize = width / numCols;
        int trim = tileSize;
        int dpiThreshold = 100;
        if (trim < dpiThreshold * pixelDPI) {
            trim = dpiThreshold * pixelDPI;
        }
        numRows = (height - trim) / tileSize;
        unOpened = numRows * numCols; //Sets number of unopened tiles
        return tileSize;
    }

    private int[][] generateBombs() {
        int[][] bombs = new int[numRows][numCols];
        int totalSquares = numRows * numCols;
        int bombsLeft = totalSquares / bombDensity;
        bombCount = totalSquares / bombDensity;
        numFlagsLeft = bombCount;
        if (bombsLeft == 0) {
            bombsLeft = 1;
            bombCount = 1;
        }
        Random random = new Random();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                int tmp = random.nextInt(totalSquares);
                if (tmp < bombsLeft) {
                    bombs[i][j] = 9;
                    bombsLeft--;
                } else {
                    bombs[i][j] = 0;
                }
                totalSquares--;

            }
        }
        return fillBoard(bombs);
    }

    private int[][] fillBoard(int[][] board) {
        for (int i = 1; i < board.length - 1; i++) { //fills interior board
            for (int j = 1; j < board[i].length - 1; j++) {
                if (board[i][j] == 9) {
                    continue;
                }
                int bombCount = 0;
                for (int k = -1; k <= 1; k++) {
                    for (int l = -1; l <= 1; l++) {
                        if (board[i + k][j + l] == 9) {
                            bombCount++;
                        }
                    }
                }
                board[i][j] = bombCount;
            }
        }
        for (int i = 1; i < board[i].length - 1; i++) { //fills Top and Bottom rows
            int bombCountTop = 0;
            int bombCountBottom = 0;
            for (int k = 0; k <= 1; k++) {
                for (int l = -1; l <= 1; l++) {
                    if (board[k][i + l] == 9) {
                        bombCountTop++;
                    }
                    if (board[board.length - 1 - k][i + l] == 9) {
                        bombCountBottom++;
                    }

                }
            }
            if (board[0][i] != 9) {
                board[0][i] = bombCountTop;
            }
            if (board[board.length - 1][i] != 9) {
                board[board.length - 1][i] = bombCountBottom;
            }
        }
        for (int i = 1; i < board.length - 1; i++) { //fills Left and Right
            int bombCountLeft = 0;
            int bombCountRight = 0;
            for (int k = 0; k <= 1; k++) {
                for (int l = -1; l <= 1; l++) {
                    if (board[i + l][k] == 9) {
                        bombCountLeft++;
                    }
                    if (board[i + l][board[i].length - 1 - k] == 9) {
                        bombCountRight++;
                    }
                }
            }
            if (board[i][0] != 9) {
                board[i][0] = bombCountLeft;
            }
            if (board[i][board[i].length - 1] != 9) {
                board[i][board[i].length - 1] = bombCountRight;
            }
        }
        int bombCountUpperLeft = 0;
        int bombCountUpperRight = 0;
        int bombCountLowerLeft = 0;
        int bombCountLowerRight = 0;
        for (int k = 0; k <= 1; k++) {
            for (int l = 0; l <= 1; l++) {
                if (board[k][l] == 9) {
                    bombCountUpperLeft++;
                }
                if (board[k][board[0].length - 1 - l] == 9) {
                    bombCountUpperRight++;
                }
                if (board[board.length - 1 - k][l] == 9) {
                    bombCountLowerLeft++;
                }
                if (board[board.length - 1 - k][board[0].length - 1 - l] == 9) {
                    bombCountLowerRight++;
                }
            }
        }
        if (board[0][0] != 9) {
            board[0][0] = bombCountUpperLeft;
        }
        if (board[0][board[0].length - 1] != 9) {
            board[0][board[0].length - 1] = bombCountUpperRight;
        }
        if (board[board.length - 1][0] != 9) {
            board[board.length - 1][0] = bombCountLowerLeft;
        }
        if (board[board.length - 1][board[0].length - 1] != 9) {
            board[board.length - 1][board[0].length - 1] = bombCountLowerRight;
        }
        return board;
    }



    private class TileButton extends android.support.v7.widget.AppCompatImageView {

        private int row;
        private int col;
        private int numBombs;
        private boolean isClicked;
        private boolean isMarked;

        public TileButton(Context context, AttributeSet attrs, int row, int col, int numBombs) {
            super(context, attrs);
            this.row = row;
            this.col = col;
            this.numBombs = numBombs;
            this.isClicked = false;
            this.isMarked = false;
        }

        public TileButton(Context context, int row, int col, int numBombs) {
            super(context);
            this.row = row;
            this.col = col;
            this.numBombs = numBombs;
            this.isClicked = false;
            this.isMarked = false;
        }

        public int getRow() {
            return this.row;
        }

        public int getCol() {
            return this.col;
        }

        public int getNumBombs() {
            return this.numBombs;
        }

        public boolean getIsClicked() {
            return this.isClicked;
        }

        public boolean getIsMarked() {
            return this.isMarked;
        }

        public void setIsClicked(boolean isClicked) {
            this.isClicked = isClicked;
        }

        public void setIsMarked(boolean isMarked) {
            this.isMarked = isMarked;
        }
    }
}
