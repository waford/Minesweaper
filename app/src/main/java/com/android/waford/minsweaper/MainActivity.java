package com.android.waford.minsweaper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void sendBoardData(View view) {
        int numCols = Integer.parseInt(((Button) view).getContentDescription().toString()); // Gets number of colums associated with button
        Intent startGame = new Intent(this, GameBoard.class);
        startGame.putExtra("NUM_COLS", numCols);
        startActivity(startGame);
    }
}
