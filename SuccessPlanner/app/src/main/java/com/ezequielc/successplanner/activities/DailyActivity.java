package com.ezequielc.successplanner.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import java.util.Locale;

public class DailyActivity extends AppCompatActivity {
    TextView mCurrentDate;
    FloatingActionButton mFAB;
    RecyclerView mGoalsRecyclerView, mAffirmationsRecyclerView, mScheduleRecyclerView;

    GoalRecyclerViewAdapter mGoalAdapter;
    AffirmationRecyclerViewAdapter mAffirmationAdapter;
    ScheduleRecyclerViewAdapter mScheduleAdapter;

    List<Goal> mGoalList;
    List<Affirmation> mAffirmationList;
    List<Schedule> mScheduleList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        // References to Views
        mCurrentDate = (TextView) findViewById(R.id.current_date);
        mGoalsRecyclerView = (RecyclerView) findViewById(R.id.goals_recycler_view);
        mAffirmationsRecyclerView = (RecyclerView) findViewById(R.id.affirmations_recycler_view);
        mScheduleRecyclerView = (RecyclerView) findViewById(R.id.schedule_recycler_view);
        mFAB = (FloatingActionButton) findViewById(R.id.fab);

        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(getApplicationContext());
        String currentDate = getIntent().getStringExtra(MainActivity.DATE_FORMATTED);
        mCurrentDate.setText(currentDate);

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

    public void alertDialog(int layout, final int id){
        AlertDialog.Builder builder = new AlertDialog.Builder(DailyActivity.this);
        LayoutInflater inflater = LayoutInflater.from(DailyActivity.this);
        View view = inflater.inflate(layout, null);
        builder.setView(view);

        final EditText editText = (EditText) view.findViewById(id);

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
                        String time = TimePickerFragment.getTime();
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
        fragment.show(getSupportFragmentManager(), "timePicker");
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

        public static String getTimeFromTimePicker(TimePicker timePicker){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Calendar time = Calendar.getInstance();
                time.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                time.set(Calendar.MINUTE, timePicker.getMinute());

                String format = "hh:mm a";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
                String formatted_time = simpleDateFormat.format(time.getTime());

                return formatted_time + " ";
            }
            return "";
        }

        public static String getTime(){
            return getTimeFromTimePicker(mTimePicker);
        }
    }
}
