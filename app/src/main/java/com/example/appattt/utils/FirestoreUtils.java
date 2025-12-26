package com.example.appattt.utils;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FirestoreUtils {

    public static long getTimestamp(Object timestampObj) {
        if (timestampObj == null) {
            return System.currentTimeMillis();
        }

        if (timestampObj instanceof Long) {
            return (Long) timestampObj;
        } else if (timestampObj instanceof Timestamp) {
            return ((Timestamp) timestampObj).toDate().getTime();
        } else if (timestampObj instanceof Date) {
            return ((Date) timestampObj).getTime();
        }

        return System.currentTimeMillis();
    }

    public static List<String> toStringList(Object listObj) {
        List<String> result = new ArrayList<>();
        if (listObj instanceof List) {
            for (Object item : (List<?>) listObj) {
                if (item instanceof String) {
                    result.add((String) item);
                }
            }
        }
        return result;
    }
}