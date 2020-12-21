package com.ezequielc.successplanner.models;

/**
 * Created by Ezequiel on 12/20/16.
 */

public class Schedule {
    private long mID;
    private String mDate;
    private String mSchedule;

    public Schedule(String mDate, String mSchedule) {
        this.mDate = mDate;
        this.mSchedule = mSchedule;
    }

    public Schedule(long mID, String mDate, String mSchedule) {
        this.mID = mID;
        this.mDate = mDate;
        this.mSchedule = mSchedule;
    }

    public long getID() {
        return mID;
    }

    public void setID(long mID) {
        this.mID = mID;
    }

    public String getDate() {
        return mDate;
    }

    public String getSchedule() {
        return mSchedule;
    }

    public void setSchedule(String mSchedule) {
        this.mSchedule = mSchedule;
    }
}
