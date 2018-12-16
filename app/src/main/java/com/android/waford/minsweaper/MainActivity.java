package com.android.waford.minsweaper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class MainActivity extends AppCompatActivity implements Serializable {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DIFFICULTY_KEY = "difficulty";
    private static final String HIGHSCORE_FILENAME = "highScores.txt";
    private static final int EASY_DENSITY = 6;
    private static final int MEDIUM_DENSITY = 5;
    private static final int HARD_DENSITY = 4;
    private LinearLayout menuBar;
    private Button showInstructions;
    private PopupWindow instructionsPopUp;
    private View instructionsLayout;
    private SharedPreferences sharedPreferences;
    private View infoLayout;
    private AlertDialog settings;
    private AlertDialog leaderBoard;
    private AdView mainActivityAdView;
    private int mineDensity;
    private View leaderBoardView;
    private File directory;
    private int highScoreCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        popUpInit();
        settingsInit();
        adInit();
        getLeaderBoardData();
    }

    @Override
    protected void onResume(){
        super.onResume();
        getLeaderBoardData();
    }

    private void init() {
        showInstructions = findViewById(R.id.leftMenuButton);
        menuBar = findViewById(R.id.MenuBar);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        instructionsLayout = layoutInflater.inflate(R.layout.instruction_popup, null);
        infoLayout = layoutInflater.inflate(R.layout.settings_info, null);
        leaderBoardView = layoutInflater.inflate(R.layout.leader_board, null);
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        mineDensity = sharedPreferences.getInt(DIFFICULTY_KEY, 6);
        directory = this.getFilesDir();

    }

    private void popUpInit() {
        int instructionsWidth = (int) (0.75 * (double) Resources.getSystem().getDisplayMetrics().widthPixels);
        int instructionsHeight = (int) (0.66 * (double) Resources.getSystem().getDisplayMetrics().heightPixels);
        instructionsPopUp = new PopupWindow(instructionsLayout, instructionsWidth , instructionsHeight);
        instructionsPopUp.setContentView(instructionsLayout);
        instructionsPopUp.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        instructionsPopUp.setFocusable(true);
        instructionsPopUp.update();
        instructionsPopUp.setOutsideTouchable(false);
    }

    private void settingsInit() {
        //Builds the setting dialog
        AlertDialog.Builder settingAlert = new AlertDialog.Builder(this);
        settingAlert.setView(infoLayout);
        settingAlert.setTitle("Info");
        settings = settingAlert.create();
        AlertDialog.Builder leaderBoardAlert = new AlertDialog.Builder(this);
        leaderBoardAlert.setView(leaderBoardView);
        leaderBoardAlert.setTitle("High Scores");
        leaderBoard = leaderBoardAlert.create();
    }

    private void adInit() {
        MobileAds.initialize(this, "ca-app-pub-3203445673322999~8135639616");
        mainActivityAdView = findViewById(R.id.mainScreenBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mainActivityAdView.loadAd(adRequest);
    }

    private void getLeaderBoardData() {
        String[][] highScoreData = new String[4][5];
        try {
            FileInputStream fis = this.openFileInput(HIGHSCORE_FILENAME);
            ObjectInputStream out = new ObjectInputStream(fis);
            highScoreData = (String[][]) out.readObject();
            fillLeaderBoard(highScoreData);
            out.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e){
            try {
                FileOutputStream fw = this.openFileOutput(HIGHSCORE_FILENAME, MODE_PRIVATE);
                ObjectOutputStream in = new ObjectOutputStream(fw);
                for (int i = 0; i < highScoreData.length; i++) {
                    for (int j = 0; j < highScoreData[i].length; j++) {
                        String value = "NaN\n";
                        highScoreData[i][j] = value;
                    }
                }
                in.writeObject(highScoreData);
                in.close();
                fw.close();
            } catch (IOException q) {
                Log.d(LOG_TAG, q.toString());
            }
        }
        fillLeaderBoard(highScoreData);

    }

    private void fillLeaderBoard(String[][] data) {
        LinearLayout mainLeaderBoard = leaderBoardView.findViewById(R.id.MainLeaderBoard);
        for(int i = 0; i < 4; i++) {
            LinearLayout tmp = (LinearLayout) mainLeaderBoard.getChildAt(i);
            for(int j = 0; j < data[i].length; j++) {
                String value = data[i][j];
                if(tmp.getChildAt(j + 1) != null) {
                    ((TextView) tmp.getChildAt(j + 1)).setText(value);
                } else {
                    TextView tmpText = new TextView(this);
                    tmpText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    tmpText.setTextSize(15f);
                    tmpText.setText(value);
                    tmp.addView(tmpText);
                }
            }
        }

        }

    public void sendBoardData(View view) {
        int numCols = Integer.parseInt((view).getContentDescription().toString()); // Gets number of columns associated with button
        if (numCols == Integer.parseInt(getString(R.string.tinyNumCols))) {
            highScoreCol = 0;
        } else if(numCols == Integer.parseInt(getString(R.string.smallNumCols))) {
            highScoreCol = 1;
        } else if(numCols == Integer.parseInt(getString(R.string.mediumNumCols))) {
            highScoreCol = 2;
        } else if(numCols == Integer.parseInt(getString(R.string.largeNumCols))) {
            highScoreCol = 3;
        }
        Intent startGame = new Intent(this, GameBoard.class);
        startGame.putExtra("NUM_COLS", numCols);
        startGame.putExtra("HIGH_SCORE_COL", highScoreCol);
        startGame.putExtra("BOMB_DENSITY", mineDensity);
        startActivity(startGame);
    }


    public void toggleInstructionsPopUp(View view) {
        //Toggles instructions Popup
        if(view.getId() == R.id.leftMenuButton) {
            instructionsPopUp.showAtLocation(menuBar, 1, 0,0);
        } else {
            instructionsPopUp.dismiss();
        }
    }

    public void toggleSettingsDialog(View view) {
        //Toggles settings popup
        if(view.getId() == R.id.rightMenuButton) {
            RadioGroup radioGroup = infoLayout.findViewById(R.id.difficultyButtons);
            switch(mineDensity) {
                case EASY_DENSITY:
                    radioGroup.check(radioGroup.getChildAt(0).getId());
                    break;
                case MEDIUM_DENSITY:
                    radioGroup.check(radioGroup.getChildAt(1).getId());
                    break;
                case HARD_DENSITY:
                    radioGroup.check(radioGroup.getChildAt(2).getId());
                    break;
            }
            settings.show();
        } else {
            settings.dismiss();
        }
    }

    public void showHighScores(View view) {
        if(view.getId() == R.id.HighScoreButton){
            leaderBoard.show();
        } else {
            leaderBoard.dismiss();
        }
    }

    public void changeDifficulty(View view) {
        String level = ((RadioButton) view).getText().toString();
        switch (level) {
            case "Easy":
                mineDensity = EASY_DENSITY;
                break;
            case "Medium":
                mineDensity = MEDIUM_DENSITY;
                break;
            case "Hard":
                mineDensity = HARD_DENSITY;
                break;
        }
        sharedPreferences.edit().putInt(DIFFICULTY_KEY, mineDensity).apply();
    }
}
