package com.ezequielc.successplanner.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import com.ezequielc.successplanner.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String API_HOST = "quoteai.p.rapidapi.com";
    private static final String URL = "https://"+API_HOST+"/ai-quotes/0";
    private static final String API_KEY = "";
    public static final String DATE_FORMATTED = "dateFormatted";
    public static final String DAY_OF_WEEK = "dayOfWeek";

    private TextView mQuoteTextView;
    private CardView mCardView;
    private CalendarView mCalendarView;
    private SharedPreferences mSharedPreferences;
    private boolean mGotQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // References to Views
        mQuoteTextView = findViewById(R.id.quote_text);
        mCardView =  findViewById(R.id.card_view);
        mCalendarView = findViewById(R.id.calendar_view);

        mSharedPreferences = getSharedPreferences(SettingsActivity.PREFERENCES, Context.MODE_PRIVATE);

        // Shared Preference whether the User wants to receive quote or not
        if (mSharedPreferences.getBoolean(SettingsActivity.PREF_RECEIVE_QUOTE_SWITCH, true)) {
            if (isConnected()) {
                getQuote();
                mGotQuote = true;
            } else {
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_SHORT).show();
                mQuoteTextView.setText("Unable to Receive Quotes...");
                mGotQuote = false;
            }
        } else {
            mCardView.setVisibility(View.GONE);
            mGotQuote = false;
        }

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView calendarView, int year, int month, int dayOfMonth) {
                intentData(year, month, dayOfMonth);
            }
        });
    }

    // Passes intent data to DailyActivity
    public void intentData(int year, int month, int dayOfMonth){
        String month_day_year =  month + 1 + "/" + dayOfMonth + "/" + year;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE: MMM d, yyyy");
        String day_of_week = dateFormat.format(calendar.getTime());

        Intent intent = new Intent(MainActivity.this, DailyActivity.class);
        intent.putExtra(DAY_OF_WEEK, day_of_week);
        intent.putExtra(DATE_FORMATTED, month_day_year);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Maintain the same quote if it was gotten onCreate
        if (mSharedPreferences.getBoolean(SettingsActivity.PREF_RECEIVE_QUOTE_SWITCH, true)) {
            mCardView.setVisibility(View.VISIBLE);
            if (!mGotQuote) {
                getQuote();
                mGotQuote = true;
            }
            if (!isConnected()) {
                Toast.makeText(this, "No Internet Connection!", Toast.LENGTH_LONG).show();
                mQuoteTextView.setText("Unable to Receive Quotes...");
                mGotQuote = false;
            }
        } else {
            mCardView.setVisibility(View.GONE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Intent to Settings Activity
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_vision_board:
                // Intent to Vision Board Activity
                Intent visionBoardIntent = new Intent(MainActivity.this, VisionBoardActivity.class);
                startActivity(visionBoardIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Checks if there is Internet Connection
    private boolean isConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    // Get quote from RapidAPI
    public void getQuote(){
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(URL)
                .addHeader("X-RapidAPI-Host", API_HOST)
                .addHeader("X-RapidAPI-Key", API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code: " + response);
                }
                try {
                    final StringBuilder qod = new StringBuilder();
                    JSONObject object = new JSONObject(response.body().string());
                    String quote = object.getString("quote");
                    String author = object.getString("author");
                    qod.append("\"" + quote + "\"" + "\n- " + author);

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mQuoteTextView.setText(qod.toString());
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
