package com.example.gameca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LeaderBoardActivity extends AppCompatActivity {

    MediaPlayer lb;
    TextView tv_score;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        lb = MediaPlayer.create(this,R.raw.wleader);
        lb.start();

        tv_score = findViewById(R.id.tv_score);
        preferences = getSharedPreferences("PREFS",0);
        editor = preferences.edit();

        int lastScore = preferences.getInt("lastScore",-1);
        int[] highScores = combineAndSortHighScores(lastScore);
        int scoresToDisplay = 3;
        String textToDisplay = "";

        //The default high scores are set to -1. They will be overwritten by the user in time.
        for (int i = 0; i < highScores.length; i++) {
            //Do not display default scores
            if (highScores[i] == -1)
                continue;

            //Display only top n scores
            if (i > scoresToDisplay - 1)
                break;
            int rankPosition = i + 1;
            textToDisplay += "POSITION " + rankPosition + " : " + highScores[i] / 1000 + "s";
            textToDisplay += "\n";
        }

        tv_score.setText(textToDisplay);

        Button redirectHomeButton = findViewById(R.id.redirectToHome);
        redirectHomeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View btn) {
                Intent intent = new Intent(LeaderBoardActivity.this, DownloadActivity.class);
                startActivity(intent);
            }
        });
    }

    public int[] combineAndSortHighScores (int lastScore) {
        String highScores = preferences.getString("highScores", "-1#-1#-1");

        int[] highScoresArr = Arrays.stream(highScores.split("#"))
                .mapToInt(n -> Integer.parseInt(n))
                .toArray();

        //if user access leader board directly, he would not have a score to save in shared preference
        if (lastScore == -1) {
            return highScoresArr;
        }

        //Add the latestScore to the record of high scores
        List<Integer> highScoresList = Arrays.stream(highScoresArr)
                .boxed()
                .collect(Collectors.toList());

        highScoresList.add(lastScore);

        //sort the scores in ascending order, omitting -1 default values
        int[] highScoresIntArr = highScoresList.stream()
                .sorted()
                .filter(n -> n > 0)
                .mapToInt(num -> num)
                .toArray();

        //Store the array back to shared preference, as a string
        String[] highScoresStringArr = Arrays.stream(highScoresIntArr)
                .mapToObj(num -> String.valueOf(num))
                .toArray(String[]::new);
        editor.putString("highScores", String.join("#", highScoresStringArr));
        editor.commit();

        return highScoresIntArr;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}

