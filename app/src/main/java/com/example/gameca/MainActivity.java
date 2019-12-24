package com.example.gameca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button playGame;
    Button leaderBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       //final EditText uName = findViewById(R.id.editText1);
        playGame = findViewById(R.id.btnPlayGame);

        playGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gameScreen = new Intent(MainActivity.this, DownloadActivity.class);
                startActivity(gameScreen);

            }
        });

        leaderBoard = findViewById(R.id.LeaderB);
        leaderBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LeaderBoardActivity.class);
                startActivity(intent);
            }
        });
    }
}
