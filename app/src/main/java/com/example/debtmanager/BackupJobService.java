package com.example.debtmanager;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;

/** Runs the Telegram upload in the background when the phone has an Internet connection. */
public class BackupJobService extends JobService {
    @Override
    public boolean onStartJob(final JobParameters params) {
        final Context app = getApplicationContext();
        new Thread(() -> {
            boolean retry = false;
            try {
                TelegramBackup.runIfNeeded(app);
            } catch (TelegramBackup.TgException e) {
                retry = !e.permanent;
            } catch (Exception e) {
                retry = true;
            }
            jobFinished(params, retry);
        }).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
