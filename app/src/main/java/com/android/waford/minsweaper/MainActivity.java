package com.android.waford.minsweaper;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    LinearLayout menuBar;
    Button showInstructions;
    PopupWindow instructionsPopUp;
    View instructionsLayout;
    TextView instructionsText;
    Button instructionsExit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        popUpInit();
    }

    private void init() {
        showInstructions = findViewById(R.id.leftMenuButton);
        menuBar = findViewById(R.id.MenuBar);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        instructionsLayout = layoutInflater.inflate(R.layout.instruction_popup, null);
//        instructionsText = new TextView(this);
//        instructionsExit = new Button(this);
//        instructionsScroll= new ScrollView(this);
//        //Settings for text portion of instructions
//        instructionsText.setText(getString(R.string.instructions));
//        instructionsText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//        //Settings for instructionsExit
//
//        //Scroll View
//        instructionsScroll.addView(instructionsText);
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

    public void sendBoardData(View view) {
        int numCols = Integer.parseInt(((Button) view).getContentDescription().toString()); // Gets number of columns associated with button
        Intent startGame = new Intent(this, GameBoard.class);
        startGame.putExtra("NUM_COLS", numCols);
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
}
