package com.ezequielc.successplanner.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ezequielc.successplanner.models.Affirmation;
import com.ezequielc.successplanner.models.Goal;
import com.ezequielc.successplanner.models.Schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ezequiel on 12/21/16.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "successplanner.db";

    private static final String COL_ID = "id";
    private static final String COL_DATE = "date";

    private static final String GOALS_TABLE = "goals_list";
    private static final String COL_GOALS = "goals";

    private static final String AFFIRMATIONS_TABLE = "affirmations_list";
    private static final String COL_AFFIRMATIONS = "affirmations";

    private static final String SCHEDULE_TABLE = "schedule_list";
    private static final String COL_SCHEDULE = "schedule";

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static DatabaseHelper mInstance;

    public static DatabaseHelper getInstance(Context context){
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context.getApplicationContext());
        } return mInstance;
    }

    private static final String CREATE_GOALS_TABLE =
            "CREATE TABLE " + GOALS_TABLE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DATE + " TEXT, " +
                    COL_GOALS + " TEXT)";

    private static final String CREATE_AFFIRMATIONS_TABLE =
            "CREATE TABLE " + AFFIRMATIONS_TABLE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DATE + " TEXT, " +
                    COL_AFFIRMATIONS + " TEXT)";

    private static final String CREATE_SCHEDULE_TABLE =
            "CREATE TABLE " + SCHEDULE_TABLE + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_DATE + " TEXT, " +
                    COL_SCHEDULE + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_GOALS_TABLE);
        db.execSQL(CREATE_AFFIRMATIONS_TABLE);
        db.execSQL(CREATE_SCHEDULE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + GOALS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + AFFIRMATIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + SCHEDULE_TABLE);
        onCreate(db);
    }

    // Insert Goals into database for specific date
    public void insertGoals(Goal goal){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, goal.getDate());
        values.put(COL_GOALS, goal.getGoal());
        long id = db.insert(GOALS_TABLE, null, values);
        goal.setID(id);
        db.close();
    }

    // Insert Affirmations into database for specific date
    public void insertAffirmations(Affirmation affirmation){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, affirmation.getDate());
        values.put(COL_AFFIRMATIONS,affirmation.getAffirmation());
        long id = db.insert(AFFIRMATIONS_TABLE, null, values);
        affirmation.setID(id);
        db.close();
    }

    // Insert Schedule into database for specific date
    public void insertSchedule(Schedule schedule){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DATE, schedule.getDate());
        values.put(COL_SCHEDULE,schedule.getSchedule());
        long id = db.insert(SCHEDULE_TABLE, null, values);
        schedule.setID(id);
        db.close();
    }

    // List Goals for the specific date it was inserted
    public List<Goal> getGoalsForDate(String currentDate){
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = new String[]{COL_ID, COL_DATE, COL_GOALS};
        String selection = COL_DATE + " = ?";
        String[] selectionArgs = new String[]{currentDate};

        Cursor cursor = db.query(GOALS_TABLE, columns, selection, selectionArgs, null, null, null, null);

        List<Goal> goalList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(cursor.getColumnIndex(COL_ID));
                String date = cursor.getString(cursor.getColumnIndex(COL_DATE));
                String goal = cursor.getString(cursor.getColumnIndex(COL_GOALS));

                Goal goals = new Goal(id, date, goal);
                goalList.add(goals);

                cursor.moveToNext();
            }
        }
        return goalList;
    }

    // List Affirmations for the specific date it was inserted
    public List<Affirmation> getAffirmationsForDate(String currentDate){
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = new String[]{COL_ID, COL_DATE, COL_AFFIRMATIONS};
        String selection = COL_DATE + " = ?";
        String[] selectionArgs = new String[]{currentDate};

        Cursor cursor = db.query(AFFIRMATIONS_TABLE, columns, selection, selectionArgs, null, null, null, null);

        List<Affirmation> affirmationList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(cursor.getColumnIndex(COL_ID));
                String date = cursor.getString(cursor.getColumnIndex(COL_DATE));
                String affirmation = cursor.getString(cursor.getColumnIndex(COL_AFFIRMATIONS));

                Affirmation affirmations = new Affirmation(id, date, affirmation);
                affirmationList.add(affirmations);

                cursor.moveToNext();
            }
        }
        return affirmationList;
    }

    // List Schedule for the specific date it was inserted
    public List<Schedule> getScheduleForDate(String currentDate){
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = new String[]{COL_ID, COL_DATE, COL_SCHEDULE};
        String selection = COL_DATE + " = ?";
        String[] selectionArgs = new String[]{currentDate};

        Cursor cursor = db.query(SCHEDULE_TABLE, columns, selection, selectionArgs, null, null, null, null);

        List<Schedule> scheduleList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                long id = cursor.getLong(cursor.getColumnIndex(COL_ID));
                String date = cursor.getString(cursor.getColumnIndex(COL_DATE));
                String schedule = cursor.getString(cursor.getColumnIndex(COL_SCHEDULE));

                Schedule schedules = new Schedule(id, date, schedule);
                scheduleList.add(schedules);

                cursor.moveToNext();
            }
        }
        return scheduleList;
    }

    // Delete Goal from database based on ID
    public void deleteGoal(Goal goal){
        SQLiteDatabase db = getWritableDatabase();
        String selection = COL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(goal.getID())};
        db.delete(GOALS_TABLE, selection, selectionArgs);
        db.close();
    }

    // Delete Affirmation from database based on ID
    public void deleteAffirmations(Affirmation affirmation){
        SQLiteDatabase db = getWritableDatabase();
        String selection = COL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(affirmation.getID())};
        db.delete(AFFIRMATIONS_TABLE, selection, selectionArgs);
        db.close();
    }

    // Delete Schedule from database based on ID
    public void deleteSchedule(Schedule schedule){
        SQLiteDatabase db = getWritableDatabase();
        String selection = COL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(schedule.getID())};
        db.delete(SCHEDULE_TABLE, selection, selectionArgs);
        db.close();
    }

    // Update Goal from database based on ID
    public void updateGoal(Goal goal){
        SQLiteDatabase db = getWritableDatabase();
        String selection = COL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(goal.getID())};
        ContentValues values = new ContentValues();
        values.put(COL_GOALS, goal.getGoal());
        db.update(GOALS_TABLE, values, selection, selectionArgs);
        db.close();
    }

    // Update Affirmation from database based on ID
    public void updateAffirmation(Affirmation affirmation){
        SQLiteDatabase db = getWritableDatabase();
        String selection = COL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(affirmation.getID())};
        ContentValues values = new ContentValues();
        values.put(COL_AFFIRMATIONS, affirmation.getAffirmation());
        db.update(AFFIRMATIONS_TABLE, values, selection, selectionArgs);
        db.close();
    }

    // Update Schedule from database based on ID
    public void updateSchedule(Schedule schedule){
        SQLiteDatabase db = getWritableDatabase();
        String selection = COL_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(schedule.getID())};
        ContentValues values = new ContentValues();
        values.put(COL_SCHEDULE, schedule.getSchedule());
        db.update(SCHEDULE_TABLE, values, selection, selectionArgs);
        db.close();
    }

    // Deletes all data in all tables
    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + GOALS_TABLE);
        db.execSQL("DELETE FROM " + AFFIRMATIONS_TABLE);
        db.execSQL("DELETE FROM " + SCHEDULE_TABLE);
        db.close();
    }
}
