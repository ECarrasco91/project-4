package com.ezequielc.successplanner.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.ezequielc.successplanner.DatabaseHelper;
import com.ezequielc.successplanner.R;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String PREFERENCES = "preferences";
    public static final String PREF_RECEIVE_QUOTE_SWITCH = "receiveQuoteSwitch";

    private SharedPreferences mSharedPreferences;
    private Switch mReceiveQuotesSwitch;
    private TextView mDeleteAllTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setTitle("Settings");

        // References to Views
        mReceiveQuotesSwitch = findViewById(R.id.get_quote);
        mDeleteAllTextView = findViewById(R.id.delete_all_entries);

        mSharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        mReceiveQuotesSwitch.setChecked(mSharedPreferences.getBoolean(PREF_RECEIVE_QUOTE_SWITCH, true));

        mReceiveQuotesSwitch.setOnClickListener(this);
        mDeleteAllTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Receive Quote Switch
        switch (view.getId()) {
            case R.id.get_quote:
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(PREF_RECEIVE_QUOTE_SWITCH, mReceiveQuotesSwitch.isChecked());
                editor.commit();
                break;

            // Delete Cloud Storage
            case R.id.delete_all_entries:
                new AlertDialog.Builder(SettingsActivity.this)
                        .setMessage("Delete All Entries?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
                                databaseHelper.deleteAll();
                            }
                        })
                        .setNegativeButton("No", null)
                        .setCancelable(false)
                        .create().show();
                break;

            default:
                break;
        }
    }
}
