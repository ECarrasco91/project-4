package com.ezequielc.successplanner.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ezequielc.successplanner.DatabaseHelper;
import com.ezequielc.successplanner.models.Goal;
import com.ezequielc.successplanner.recyclerviews.GoalRecyclerViewAdapter;
import com.ezequielc.successplanner.R;
import com.ezequielc.successplanner.models.Schedule;
import com.ezequielc.successplanner.recyclerviews.ScheduleRecyclerViewAdapter;
import com.ezequielc.successplanner.models.Affirmation;
import com.ezequielc.successplanner.recyclerviews.AffirmationRecyclerViewAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DailyActivity extends AppCompatActivity {
    public static final String DIALOG_ID = "timePicker";

    private FloatingActionButton mFAB;
    private RecyclerView mGoalsRecyclerView, mAffirmationsRecyclerView, mScheduleRecyclerView;

    private GoalRecyclerViewAdapter mGoalAdapter;
    private AffirmationRecyclerViewAdapter mAffirmationAdapter;
    private ScheduleRecyclerViewAdapter mScheduleAdapter;

    private List<Goal> mGoalList;
    private List<Affirmation> mAffirmationList;
    private List<Schedule> mScheduleList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        // References to Views
        mGoalsRecyclerView = findViewById(R.id.goals_recycler_view);
        mAffirmationsRecyclerView = findViewById(R.id.affirmations_recycler_view);
        mScheduleRecyclerView = findViewById(R.id.schedule_recycler_view);
        mFAB = findViewById(R.id.fab);

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

        // Gets day of week string from MainActivity Intent ex. Sunday, January 8, 2017
        String dayOfWeek = getIntent().getStringExtra(MainActivity.DAY_OF_WEEK);

        // Gets the date string from MainActivity Intent ex. 1/8/2017
        String currentDate = getIntent().getStringExtra(MainActivity.DATE_FORMATTED);

        getSupportActionBar().setTitle(dayOfWeek);

        // RecyclerView for Goals
        mGoalList = databaseHelper.getGoalsForDate(currentDate);
        mGoalAdapter = new GoalRecyclerViewAdapter(mGoalList);
        LinearLayoutManager goalLinearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mGoalsRecyclerView.setLayoutManager(goalLinearLayoutManager);
        mGoalsRecyclerView.setAdapter(mGoalAdapter);

        // RecyclerView for Affirmations
        mAffirmationList = databaseHelper.getAffirmationsForDate(currentDate);
        mAffirmationAdapter = new AffirmationRecyclerViewAdapter(mAffirmationList);
        LinearLayoutManager affirmationLinearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAffirmationsRecyclerView.setLayoutManager(affirmationLinearLayoutManager);
        mAffirmationsRecyclerView.setAdapter(mAffirmationAdapter);

        // RecyclerView for Schedules
        mScheduleList = databaseHelper.getScheduleForDate(currentDate);
        mScheduleAdapter = new ScheduleRecyclerViewAdapter(mScheduleList);
        LinearLayoutManager ScheduleLinearLayoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mScheduleRecyclerView.setLayoutManager(ScheduleLinearLayoutManager);
        mScheduleRecyclerView.setAdapter(mScheduleAdapter);

        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Presents an AlertDialog listing entries to add
                CharSequence[] options = {"Goal", "Affirmation", "Schedule"};
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext())
                        .setTitle("Choose:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0: // Goals
                                        alertDialog(R.layout.dialog_add_goals, R.id.goal_edit_text);
                                        break;

                                    case 1: // Affirmations
                                        alertDialog(R.layout.dialog_add_affirmations, R.id.affirmations_edit_text);
                                        break;

                                    case 2: // Schedule
                                        alertDialog(R.layout.dialog_add_schedule, R.id.schedule_edit_text);
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                            showTimePickerDialog();
                                        }
                                        break;

                                    default:
                                        break;
                                }
                            }
                        });
                builder.create().show();
            }
        });
    }

    /**
     * Presents an AlertDialog based on params
     * @param layout takes in the resource layout to present specific dialog
     * @param id takes in the resource id which is the edit text of the resource layout
     */
    public void alertDialog(int layout, final int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(DailyActivity.this);
        LayoutInflater inflater = LayoutInflater.from(DailyActivity.this);
        View view = inflater.inflate(layout, null);
        builder.setView(view);

        final EditText editText = view.findViewById(id);

        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (editText.getText().toString().trim().length() == 0) {
                    Toast.makeText(DailyActivity.this, "Please fill field!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String input = editText.getText().toString();
                String currentDate = getIntent().getStringExtra(MainActivity.DATE_FORMATTED);
                DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());

                // Switch statement to present a specific edit text based on resource id
                switch (id) {
                    case R.id.goal_edit_text:
                        Goal goal = new Goal(currentDate, input);

                        databaseHelper.insertGoals(goal);
                        mGoalList.add(goal);
                        mGoalAdapter.notifyItemInserted(mGoalList.size() - 1);
                        break;

                    case R.id.affirmations_edit_text:
                        Affirmation affirmation = new Affirmation(currentDate, input);

                        databaseHelper.insertAffirmations(affirmation);
                        mAffirmationList.add(affirmation);
                        mAffirmationAdapter.notifyItemInserted(mAffirmationList.size() - 1);
                        break;

                    case R.id.schedule_edit_text:
                        String time = TimePickerFragment.getTimeFromTimePicker();
                        Schedule schedule = new Schedule(currentDate, time + input);

                        databaseHelper.insertSchedule(schedule);
                        mScheduleList.add(schedule);
                        mScheduleAdapter.notifyItemInserted(mScheduleList.size() - 1);
                        break;

                    default:
                        break;
                }
            }
        })
                .setNegativeButton("Cancel", null);
        builder.create().show();
    }

    public void showTimePickerDialog(){
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.show(getSupportFragmentManager(), DIALOG_ID);
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        int mHour, mMinute;
        static TimePicker mTimePicker;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new TimePickerDialog(getContext(), this, mHour, mMinute, false);
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            mTimePicker = timePicker;
        }

        /**
         * With the Calendar util, hour and minute is set and formatted based on the timePicker
         * @return if the build version is 23 or greater formatted time is returned, if not an empty string
         */
        public static String getTimeFromTimePicker(){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Calendar time = Calendar.getInstance();
                time.set(Calendar.HOUR_OF_DAY, mTimePicker.getHour());
                time.set(Calendar.MINUTE, mTimePicker.getMinute());

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                String formatted_time = simpleDateFormat.format(time.getTime());

                return formatted_time + " ";
            } return "";
        }
    }
}
