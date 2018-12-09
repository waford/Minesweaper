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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    private static final String DIFFICULTY_KEY = "difficulty";
    private static final int EASY_DENSITY = 6;
    private static final int MEDIUM_DENSITY = 5;
    private static final int HARD_DENSITY = 4;
    LinearLayout menuBar;
    Button showInstructions;
    PopupWindow instructionsPopUp;
    View instructionsLayout;
    private SharedPreferences sharedPreferences;
    private View infoLayout;
    private AlertDialog settings;
    private AdView mainActivityAdView;
    private int mineDensity;
    private File directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        popUpInit();
        settingsInit();
        adInit();
    }

    private void init() {
        showInstructions = findViewById(R.id.leftMenuButton);
        menuBar = findViewById(R.id.MenuBar);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        instructionsLayout = layoutInflater.inflate(R.layout.instruction_popup, null);
        layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        infoLayout = layoutInflater.inflate(R.layout.settings_info, null);
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        mineDensity = sharedPreferences.getInt(DIFFICULTY_KEY, 6);

        //Get highscore data
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(infoLayout);
        builder.setTitle("Info");
        settings = builder.create();
    }

    private void adInit() {
        MobileAds.initialize(this, "ca-app-pub-3203445673322999~8135639616");
        mainActivityAdView = findViewById(R.id.mainScreenBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mainActivityAdView.loadAd(adRequest);
    }


    public void sendBoardData(View view) {
        int numCols = Integer.parseInt((view).getContentDescription().toString()); // Gets number of columns associated with button
        Intent startGame = new Intent(this, GameBoard.class);
        startGame.putExtra("NUM_COLS", numCols);
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
