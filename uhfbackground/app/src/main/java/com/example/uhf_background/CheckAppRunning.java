package com.example.uhf_background;

import static android.os.Environment.getExternalStorageDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.Math;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;

import java.util.Arrays;
import java.util.List;

public class CheckAppRunning extends JobIntentService {

    private Handler handler = new Handler();
    private static final long CHECK_INTERVAL = 30000;
    private static final int JOB_ID = 1000;

    private static final String FILE_PATH = String.valueOf(getExternalStorageDirectory()) + "/uhf_last_running.txt";
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, CheckAppRunning.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        handler.postDelayed(checkAppRunnable, CHECK_INTERVAL);
    }

    private Runnable checkAppRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTimestampOlderThanMinute(getApplicationContext(), FILE_PATH)) {
                restartMainApp();
            }
            handler.postDelayed(this, CHECK_INTERVAL);
        }
    };

    public static boolean isTimestampOlderThanMinute(Context context, String filePath) {
        try {
            File file = new File(filePath);

            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String timestampString = reader.readLine();
                reader.close();

                if (timestampString != null) {
                    long currentTime = System.currentTimeMillis();
                    long writtenTime = Long.parseLong(timestampString);
                    long timeDiff = Math.abs(currentTime - writtenTime);

                    // Check if the timestamp is older than 2 minutes
                    return (timeDiff > (120 * 1000));
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // File doesn't exist or error occurred
        return true;
    }

    private void restartMainApp() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.uhf");

        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launchIntent);
        }
    }
}
