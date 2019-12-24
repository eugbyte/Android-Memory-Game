package com.example.gameca;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class GameActivity extends AppCompatActivity {
    private Context context;

    Button b_end;
    MediaPlayer flip;
    MediaPlayer win;

    //The GridView
    GridView gridView = null;
    GameImageAdapter adapter=null;
    ImageView cell = null;
    private Bitmap[] bitmaps = null;

    //The timer
    Chronometer simpleChronometer = null;
    boolean mIsStarted=false;

    //clicks can be differentiated between 2 states - odd clicks and even clicks
    //odd clicks set the answer, i.e. answerPos
    //even clicks is the user's attempt, i.e. attemptPos
    int answerPos = -1;
    private int correctPairs = 0;
    ArrayList<Integer> generatedRandomSequence = null;

    //0 if not matched
    //1 if matched
    static ArrayList<Integer> matchedStatusPositions =new ArrayList<Integer>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //initialize the match status for each position
        for (int i=0;i<12;i++) {
            matchedStatusPositions.add(i,0);
        }

        //generate the random sequence
        generatedRandomSequence = transformRandomSequence(generateRanNums());

        //Animations
        b_end = findViewById(R.id.buttonEnd);
        flip = MediaPlayer.create(this, R.raw.flip);
        win = MediaPlayer.create(this, R.raw.bell);

        //initialise the bitmaps
        Intent intent = getIntent();
        List<String> fileUrls = (ArrayList<String>) intent.getSerializableExtra("fileUrls");
        bitmaps = getBitmaps(fileUrls);

        //initialize the timer
        simpleChronometer = (Chronometer) findViewById(R.id.simpleChronometer);
        simpleChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                int ms = (int) (time - h * 3600000 - m * 60000 - s * 1000) / 10;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                cArg.setText(hh + ":" + mm + ":" + ss);
            }
        });

        //GridView
        adapter = new GameImageAdapter(context);
        gridView = (GridView) findViewById(R.id.gridView);
        final GameImageAdapter imageAdapter = new GameImageAdapter(this);
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                //Start timer
                if(!mIsStarted)
                    simpleChronometer.setBase(SystemClock.elapsedRealtime());
                simpleChronometer.start();
                mIsStarted=true;

                flip.start();

                //if positions have already been matched, disable clicks and event listener
                for (int i=0; i<12; i++) {
                    if (matchedStatusPositions.get(i)==1)
                        adapter.isEnabled(i);
                }

                Log.v("currentPosition", String.valueOf(position));
                Log.v("isEnabled", String.valueOf(view.isEnabled()) );
                Log.v("isClickable", String.valueOf(view.isClickable()) );
                //On the odd clicks, e.g. setting the answer to be attempted later
                if (answerPos < 0) {
                    answerPos = position;
                    cell = (ImageView) view;
                    ((ImageView) view).setImageBitmap(bitmaps[generatedRandomSequence.get(position)]);
                    Log.v("drawable:", String.valueOf(bitmaps[0]));

                } else {
                    //If you click on the same icon, hide it
                    if (answerPos == position) {
                        ((ImageView) view).setImageResource(R.drawable.hidden);
                        //If the attempt does not match the answer
                    } else if (generatedRandomSequence.get(answerPos) != generatedRandomSequence.get(position)) {
                        onMismatch(view, position);

                        //If the attempt matches the answer
                    } else {
                        onMatch(view, position);

                        if (correctPairs == 6) {
                            updateScoreAndRedirectOnWin();
                        }
                    }
                    //after each even click, go back to odd state
                    answerPos = -1;
                }
            }
        });


        //Leader board button
        b_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateScoreAndRedirectOnWin();
            }
        });
    }

    //Helper functions depending on match, mismatch, and win

        //Overall logic:
        //let's say random sequence is [0, 1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6]
        //if user click at grid 11 for odd click, he will get 6 as answer pos (index 11)
        //if user click at grid 5 for even click, he will get 6 as the attempt pos (index 5)

    public void onMismatch(View view, int position) {
        ((ImageView) view).setImageBitmap(bitmaps[generatedRandomSequence.get(position)]);
        Toast.makeText(getApplicationContext(), "NOT MATCH! PLEASE TRY AGAIN", Toast.LENGTH_SHORT).show();

        //Delay
        final long changeTime = 600L;
        ((ImageView) view).postDelayed(new Runnable() {
            @Override
            public void run() {
                cell.setImageResource(R.drawable.hidden);
            }
        }, changeTime);
        ((ImageView) view).postDelayed(new Runnable() {
            @Override
            public void run() {
                ((ImageView) view).setImageResource(R.drawable.hidden);
            }
        }, changeTime);
    }

    public void onMatch (View view, int position) {

        win.start();
        ((ImageView) view).setImageBitmap(bitmaps[generatedRandomSequence.get(position)]);
        Toast.makeText(GameActivity.this, "Correct!", Toast.LENGTH_SHORT).show();

        adapter.setItemClickable(answerPos, false);
        adapter.setItemClickable(position, false);
        matchedStatusPositions.set(position,1);
        matchedStatusPositions.set(answerPos,1);

        correctPairs++;

      TextView  score = findViewById(R.id.score);
        score.setText(String.valueOf(correctPairs));
        //Disable click event of matched pairs
        View attemptCell = view;
        attemptCell.setEnabled(false);
        attemptCell.setClickable(false);

        View answerCell = gridView.getChildAt(answerPos);
        answerCell.setEnabled(false);
        answerCell.setClickable(false);

        Log.v("answerCell", String.valueOf(answerPos));
        Log.v("answerCell is enabled?", String.valueOf(answerCell.isEnabled()));
        Log.v("attemptCell", String.valueOf(position) );
        Log.v("attemptCell is enabled?", String.valueOf(attemptCell.isEnabled()));
    }

    public void updateScoreAndRedirectOnWin(){

        //stop the chronometer and get the time elapsed
        simpleChronometer.stop();
        int elapsedSeconds = (int) (SystemClock.elapsedRealtime() - simpleChronometer.getBase());

        //Save state
        Toast.makeText(GameActivity.this, "YOU WON! Redirecting...", Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = getSharedPreferences("PREFS",0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("lastScore", elapsedSeconds);

        editor.commit();

        //Redirect
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), LeaderBoardActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000);


    }

    //Generate bitmaps from fileUrls passed by previous intent
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Bitmap[] getBitmaps(List<String> fileUrls) {
        Bitmap[] bitmaps = fileUrls.stream()
                .map(url -> {
                    File file = new File(url);
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                    } catch (Exception error) {
                        error.printStackTrace();
                    }
                    return bitmap;
                })
                .collect(Collectors.toList())
                .toArray(new Bitmap[0]);

        return bitmaps;
    }

    //Generate int[12] of random numbers, from range 0 -12
    public ArrayList<Integer> generateRanNums() {
        ArrayList<Integer> generatedNumbers = new ArrayList<>();
        while (generatedNumbers.size() < 12) {
            Random randnum = new Random();
            int num = randnum.nextInt(12);
            while (!generatedNumbers.contains(num)) {
                generatedNumbers.add(num);
            }
        }
        return generatedNumbers;
    }

    //reduce all numbers greater than 6 to below 6
    public ArrayList<Integer> transformRandomSequence(ArrayList<Integer> pos_2) {
        ArrayList<Integer> pos = new ArrayList<>();
        for (int i : pos_2) {
            if (i > 5) {
                i = i - 6;
                pos.add(i);
            } else {
                pos.add(i);
            }
        }
        return pos;
    }
}

