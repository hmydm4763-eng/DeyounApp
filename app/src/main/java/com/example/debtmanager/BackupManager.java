package com.example.debtmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.*;

/** Handles user-visible SQLite backups through Android's Storage Access Framework. */
public final class BackupManager {
    private BackupManager() {}

    public static void exportDatabase(Context context, Uri destination) throws IOException {
        Db db = new Db(context);
        SQLiteDatabase database = db.getWritableDatabase();
        try {
            database.rawQuery("PRAGMA wal_checkpoint(FULL)", null).close();
        } catch (Exception ignored) {}
        db.close();

        File source = context.getDatabasePath("debts.db");
        if (!source.exists()) throw new FileNotFoundException("قاعدة البيانات غير موجودة");
        copy(new FileInputStream(source), context.getContentResolver().openOutputStream(destination));
    }

    public static void importDatabase(Context context, Uri sourceUri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        File temp = new File(context.getCacheDir(), "debts_restore.tmp");
        try (InputStream in = resolver.openInputStream(sourceUri); OutputStream out = new FileOutputStream(temp)) {
            if (in == null) throw new IOException("تعذر فتح ملف النسخة الاحتياطية");
            copy(in, out);
        }

        // Validate that the selected file is a readable SQLite database before replacing live data.
        SQLiteDatabase check = null;
        try {
            check = SQLiteDatabase.openDatabase(temp.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            Cursor c = check.rawQuery("PRAGMA integrity_check", null);
            boolean ok = c.moveToFirst() && "ok".equalsIgnoreCase(c.getString(0));
            c.close();
            if (!ok) throw new IOException("ملف النسخة الاحتياطية تالف أو غير صالح");
        } catch (Exception e) {
            if (e instanceof IOException) throw (IOException)e;
            throw new IOException("الملف المحدد ليس نسخة احتياطية صالحة", e);
        } finally {
            if (check != null) check.close();
        }

        // Close the app's helper before replacing the database file.
        Db db = new Db(context);
        db.close();
        File target = context.getDatabasePath("debts.db");
        File wal = new File(target.getPath() + "-wal");
        File shm = new File(target.getPath() + "-shm");
        if (wal.exists()) wal.delete();
        if (shm.exists()) shm.delete();
        if (!temp.renameTo(target)) {
            copy(new FileInputStream(temp), new FileOutputStream(target));
            temp.delete();
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        if (in == null || out == null) throw new IOException("تعذر فتح ملف النسخة الاحتياطية");
        try (InputStream i = in; OutputStream o = out) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = i.read(buffer)) != -1) o.write(buffer, 0, n);
            o.flush();
        }
    }
}
