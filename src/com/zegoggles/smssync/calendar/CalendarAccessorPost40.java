package com.zegoggles.smssync.calendar;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Build;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.Log;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.zegoggles.smssync.App.LOCAL_LOGV;
import static com.zegoggles.smssync.App.TAG;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class CalendarAccessorPost40 implements CalendarAccessor {

    @Override
    public void addEntry(ContentResolver resolver, int calendarId, Date when, int duration, String title,
                         String description) {
        if (LOCAL_LOGV) {
            Log.v(TAG, String.format("addEntry(%d, %s, %d, %s, %s)",
                    calendarId, when.toString(), duration, title, description));
        }

        final ContentValues contentValues = new ContentValues();
        contentValues.put(CalendarContract.Events.TITLE, title);
        contentValues.put(CalendarContract.Events.DESCRIPTION, description);
        contentValues.put(CalendarContract.Events.DTSTART, when.getTime());
        contentValues.put(CalendarContract.Events.DTEND, when.getTime() + duration * 1000);
        contentValues.put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_DEFAULT);
        contentValues.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
        contentValues.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, Time.getCurrentTimezone());

        try {
            resolver.insert(CalendarContract.Events.CONTENT_URI, contentValues);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "could not add calendar entry", e);
        }
    }

    @Override
    public Map<String, String> getCalendars(ContentResolver resolver) {
        final Map<String, String> map = new LinkedHashMap<String, String>();

        Cursor cursor = null;
        try {
            cursor = resolver.query(CalendarContract.Calendars.CONTENT_URI,
                    new String[]{ CalendarContract.Calendars._ID, CalendarContract.Calendars.NAME },
                    null,
                    null,
                    CalendarContract.Calendars.NAME + " ASC");

            while (cursor != null && cursor.moveToNext()) {
                map.put(cursor.getString(0), cursor.getString(1));
            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "calendars not available", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return map;
    }
}
