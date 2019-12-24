package com.example.gameca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends AppCompatActivity implements DownloaderAsyncTask.ICallback {


    private GridView gridView = null;
    private DownloadImageAdapter imageAdapter;
    private ProgressBar bar;
    private TextView progressText;
    private List<Integer> gridPositionsSelected = new ArrayList<Integer>();

    AsyncTask asyncTask = null;

    //objects returned from AsyncTask
    private List<Bitmap> bitmaps = new ArrayList<Bitmap>();
    private List<String> downloadedFileUrls = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen1);

        //Instantiate the progress bar and progress text
        bar = findViewById(R.id.progressbar);
        progressText = findViewById(R.id.progressText);

        //Configuring the GridView
        gridView = findViewById(R.id.gridview);
        imageAdapter = new DownloadImageAdapter(DownloadActivity.this, bitmaps);
        gridView.setAdapter(imageAdapter);

        //Preparation for AsyncTask
        final String fileLocation = getFilesDir().toString();
        final EditText editText = findViewById(R.id.searchBox);

        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View btn) {

                //Refresh state if user enters a new search
                resetState();

                //Execute async get request
                String url = editText.getText().toString();

                if (!URLUtil.isValidUrl(url)) {
                    Toast toast = Toast.makeText(DownloadActivity.this, "url is invalid", Toast.LENGTH_SHORT);
                    toast.show();
                    return;
                }
                Toast toast = Toast.makeText(DownloadActivity.this, "downloading from " + url + " ...", Toast.LENGTH_LONG);
                toast.show();
                asyncTask = new DownloaderAsyncTask(DownloadActivity.this).execute(url, fileLocation);
            }
        });

        //set click listener for gridView
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View cell, int gridPosition, long id) {
                toggleGridSelected(gridPosition, cell);
            }
        });

        //Start the game after selection
        Button buttonStartGame = findViewById(R.id.startGame);
        buttonStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View btn) {

                if (gridPositionsSelected.size() < 6) {
                    Toast toast = Toast.makeText(DownloadActivity.this, "you need to select 6 items", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                //intent.putExtra("bitmaps", (Serializable) persistBitmaps);   //TransactionTooLargeException
                List<String> persistFileUrls = new ArrayList<String>();

                for (Integer position : gridPositionsSelected){
                    int index = (int)position;

                    String fileUrlToAdd = downloadedFileUrls.get(index);
                    persistFileUrls.add(fileUrlToAdd);
                }

                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                intent.putExtra("fileUrls", (Serializable) persistFileUrls);
                startActivity(intent);

            }
        });

    }

    //Helper method to toggle grid selected
    public void toggleGridSelected(int gridPosition, View cell) {

        int index = gridPositionsSelected.indexOf(gridPosition);
        //if position not found
        if (index == -1 ) {
            if (gridPositionsSelected.size() <= 5) {
                gridPositionsSelected.add(gridPosition);
                cell.setBackgroundColor(Color.GREEN);
            } else {
                Toast toast = Toast.makeText(DownloadActivity.this, "You must select 6 images", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        } else {
            //must distinguish between remove(int) and remove(Object)
            gridPositionsSelected.remove((int)index);
            cell.setBackgroundColor(Color.WHITE);
        }
    }

    //Helper method to reset state
    public void resetState() {
        //Redisplay the progress bar
        bar.setVisibility(View.VISIBLE);
        setProgressBar(0);

        //Redisplay the text view
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("");

        //clear the state
        if (asyncTask != null) {
            asyncTask.cancel(true);
            //asyncTask = null;
            bitmaps.clear();
            gridPositionsSelected.clear();
        }

        //reset bg color for clicked grids
        try {
            for (int i = 0; i < gridView.getCount(); i++) {
                View cell = gridView.getChildAt(i);
                cell.setBackgroundColor(Color.WHITE);
            }
        } catch (Exception error) {
            //in case index out of range
            error.printStackTrace();
        }
    }

    //Enforced interface methods
    @Override
    public void setProgressBar(int progress) {
        bar.setProgress(progress);
    }

    @Override
    public void updateBitmapLists(Bitmap bitmap) {
        if (bitmap != null) {
            bitmaps.add(bitmap);
            imageAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void removeProgressBar() {
        bar.setVisibility(View.GONE);
    }

    @Override
    public void addSuccessfulFileUrls(String fileUrl) {
        downloadedFileUrls.add(fileUrl);
    }

    @Override
    public void setProgressText(int fileNum) {
        TextView textView = findViewById(R.id.progressText);
        textView.setText("Downloading " + fileNum + " of 20 images...");
    }

    @Override
    public void removeProgressText(){

        progressText.setVisibility(View.GONE);
    }


}



