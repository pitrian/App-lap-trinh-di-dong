package com.example.appattt.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String getTimeAgo(long timeInMillis) {
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;

        if (diff < android.text.format.DateUtils.MINUTE_IN_MILLIS) {
            return "just now";
        } else if (diff < 2 * android.text.format.DateUtils.MINUTE_IN_MILLIS) {
            return "1 minute ago";
        } else if (diff < android.text.format.DateUtils.HOUR_IN_MILLIS) {
            return diff / android.text.format.DateUtils.MINUTE_IN_MILLIS + " minutes ago";
        } else if (diff < 2 * android.text.format.DateUtils.HOUR_IN_MILLIS) {
            return "1 hour ago";
        } else if (diff < android.text.format.DateUtils.DAY_IN_MILLIS) {
            return diff / android.text.format.DateUtils.HOUR_IN_MILLIS + " hours ago";
        } else if (diff < 2 * android.text.format.DateUtils.DAY_IN_MILLIS) {
            return "yesterday";
        } else if (diff < android.text.format.DateUtils.WEEK_IN_MILLIS) {
            return diff / android.text.format.DateUtils.DAY_IN_MILLIS + " days ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(new Date(timeInMillis));
        }
    }

    public static String getTimeAgo(Date date) {
        if (date == null) return "";
        return getTimeAgo(date.getTime());
    }
}