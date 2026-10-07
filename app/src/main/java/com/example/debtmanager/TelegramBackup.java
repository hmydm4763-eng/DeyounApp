package com.example.debtmanager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Automatic backup of the local SQLite database to the user's own private Telegram bot chat. */
public final class TelegramBackup {
    private TelegramBackup() {}

    static final String PREFS = "telegram_backup";
    static final int JOB_CHANGE = 7001;
    static final long MIN_GAP_MS = 5L * 60L * 1000L;
    private static final Object LOCK = new Object();

    /** Error that tells whether retrying later can help. */
    public static class TgException extends IOException {
        public final boolean permanent;
        public TgException(String msg, boolean permanent) { super(msg); this.permanent = permanent; }
    }

    static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isConfigured(Context c) {
        SharedPreferences p = prefs(c);
        return p.getBoolean("enabled", false) && !p.getString("token", "").isEmpty() && !p.getString("chat", "").isEmpty();
    }

    /** Pulls the bot token out of pasted text (BotFather's message contains extra words). */
    public static String extractToken(String text) {
        if (text == null) return "";
        Matcher m = Pattern.compile("\\d{6,}:[A-Za-z0-9_-]{30,}").matcher(text);
        return m.find() ? m.group() : text.trim();
    }

    // ---------------------------------------------------------------- scheduling

    /** Call after every data change. Cheap: it only marks the data as changed and schedules a job. */
    public static void changed(Context c) {
        try {
            if (!isConfigured(c)) return;
            Context app = c.getApplicationContext();
            prefs(app).edit().putBoolean("dirty", true).apply();
            schedule(app);
        } catch (Exception ignored) {}
    }

    static void schedule(Context app) {
        JobScheduler js = (JobScheduler) app.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js == null) return;
        long since = System.currentTimeMillis() - prefs(app).getLong("last_ok", 0);
        long delay = Math.max(15000L, MIN_GAP_MS - since);
        JobInfo job = new JobInfo.Builder(JOB_CHANGE, new ComponentName(app, BackupJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(delay)
                .setPersisted(true)
                .setBackoffCriteria(60000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .build();
        js.schedule(job);
    }

    /** Call when the app opens: re-schedules an upload if some changes were never sent. */
    public static void onAppStart(Context c) {
        try {
            if (isConfigured(c) && prefs(c).getBoolean("dirty", false)) schedule(c.getApplicationContext());
        } catch (Exception ignored) {}
    }

    /** Runs on a background thread from BackupJobService. */
    public static void runIfNeeded(Context c) throws IOException {
        if (isConfigured(c) && prefs(c).getBoolean("dirty", false)) uploadNow(c);
    }

    // ---------------------------------------------------------------- setup

    public static void save(Context c, String token, String chat) {
        prefs(c).edit().putBoolean("enabled", true).putString("token", token).putString("chat", chat)
                .putBoolean("dirty", true).remove("last_error").apply();
    }

    public static void disable(Context c) {
        prefs(c).edit().clear().apply();
        try {
            JobScheduler js = (JobScheduler) c.getApplicationContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (js != null) js.cancel(JOB_CHANGE);
        } catch (Exception ignored) {}
    }

    public static String botUsername(String token) throws IOException {
        JSONObject r = get(token, "getMe", null).optJSONObject("result");
        String u = r == null ? "" : r.optString("username", "");
        if (u.isEmpty()) throw new TgException("تعذر قراءة اسم البوت", true);
        return u;
    }

    /** Returns {chatId, displayName} of the newest private chat that wrote to the bot, or null. */
    public static String[] findChat(String token) throws IOException {
        JSONArray arr = get(token, "getUpdates", "limit=50").optJSONArray("result");
        if (arr == null) return null;
        for (int i = arr.length() - 1; i >= 0; i--) {
            JSONObject u = arr.optJSONObject(i);
            if (u == null) continue;
            JSONObject msg = u.optJSONObject("message");
            if (msg == null) continue;
            JSONObject chat = msg.optJSONObject("chat");
            if (chat == null || !"private".equals(chat.optString("type"))) continue;
            String name = chat.optString("first_name", "");
            String user = chat.optString("username", "");
            if (!user.isEmpty()) name = name + " (@" + user + ")";
            return new String[]{String.valueOf(chat.optLong("id")), name.isEmpty() ? "غير معروف" : name};
        }
        return null;
    }

    public static void sendText(String token, String chat, String text) throws IOException {
        get(token, "sendMessage", "chat_id=" + chat + "&text=" + URLEncoder.encode(text, "UTF-8"));
    }

    // ---------------------------------------------------------------- upload

    /** Takes a safe snapshot of the database and sends it to Telegram. Blocking: never call on the UI thread. */
    public static void uploadNow(Context c) throws IOException {
        synchronized (LOCK) {
            Context app = c.getApplicationContext();
            SharedPreferences p = prefs(app);
            String token = p.getString("token", ""), chat = p.getString("chat", "");
            if (token.isEmpty() || chat.isEmpty()) throw new TgException("لم يتم ربط تيليجرام بعد", true);
            p.edit().putBoolean("dirty", false).apply();
            File snap = new File(app.getCacheDir(), "telegram_snapshot.db");
            try {
                snapshot(app, snap);
                String stamp = new SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(new Date());
                sendDocument(token, chat, snap, "deyoun_backup_" + stamp + ".db",
                        "💾 نسخة احتياطية - ديوني\n" + stamp.replace('_', ' '));
                p.edit().putLong("last_ok", System.currentTimeMillis()).remove("last_error").apply();
            } catch (TgException e) {
                p.edit().putBoolean("dirty", true).putString("last_error", e.getMessage()).apply();
                throw e;
            } catch (IOException e) {
                p.edit().putBoolean("dirty", true)
                        .putString("last_error", "تعذر إتمام النسخ، ستتم إعادة المحاولة تلقائياً").apply();
                throw e;
            } finally {
                snap.delete();
            }
        }
    }

    static void snapshot(Context app, File out) throws IOException {
        Db db = new Db(app);
        try {
            SQLiteDatabase d = db.getWritableDatabase();
            boolean done = false;
            for (int i = 0; i < 4 && !done; i++) {
                Cursor cur = null;
                try {
                    cur = d.rawQuery("PRAGMA wal_checkpoint(FULL)", null);
                    done = !cur.moveToFirst() || cur.getInt(0) == 0;
                } catch (Exception e) {
                    done = true;
                } finally {
                    if (cur != null) cur.close();
                }
                if (!done) {
                    try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                }
            }
            if (!done) throw new IOException("قاعدة البيانات مشغولة حالياً");
        } finally {
            db.close();
        }

        File src = app.getDatabasePath("debts.db");
        if (!src.exists()) throw new FileNotFoundException("قاعدة البيانات غير موجودة");
        try (InputStream in = new FileInputStream(src); OutputStream o = new FileOutputStream(out)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) o.write(buf, 0, n);
        }

        // Never send a damaged copy.
        SQLiteDatabase v = null;
        Cursor c = null;
        try {
            v = SQLiteDatabase.openDatabase(out.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
            c = v.rawQuery("PRAGMA quick_check", null);
            if (!(c.moveToFirst() && "ok".equalsIgnoreCase(c.getString(0)))) {
                throw new IOException("فشل فحص النسخة قبل الإرسال");
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("تعذر التحقق من النسخة قبل الإرسال", e);
        } finally {
            if (c != null) c.close();
            if (v != null) v.close();
        }
    }

    // ---------------------------------------------------------------- HTTP

    static void sendDocument(String token, String chat, File file, String fileName, String caption) throws IOException {
        String boundary = "----DeyounBoundary" + System.currentTimeMillis();
        HttpURLConnection conn = (HttpURLConnection) new URL("https://api.telegram.org/bot" + token + "/sendDocument").openConnection();
        try {
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(60000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(16384);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            try (OutputStream os = new BufferedOutputStream(conn.getOutputStream())) {
                field(os, boundary, "chat_id", chat);
                field(os, boundary, "caption", caption);
                os.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"document\"; filename=\""
                        + fileName + "\"\r\nContent-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                try (InputStream in = new FileInputStream(file)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) os.write(buf, 0, n);
                }
                os.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }
            checkResponse(conn);
        } finally {
            conn.disconnect();
        }
    }

    private static void field(OutputStream os, String boundary, String name, String value) throws IOException {
        os.write(("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" + value + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
    }

    static JSONObject get(String token, String method, String query) throws IOException {
        String url = "https://api.telegram.org/bot" + token + "/" + method + (query == null ? "" : "?" + query);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(30000);
            return checkResponse(conn);
        } finally {
            conn.disconnect();
        }
    }

    private static JSONObject checkResponse(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String body = readAll(is);
        JSONObject j;
        try {
            j = new JSONObject(body);
        } catch (Exception e) {
            throw new IOException("رد غير متوقع من تيليجرام (" + code + ")");
        }
        if (j.optBoolean("ok")) return j;
        int ec = j.optInt("error_code", code);
        throw new TgException(describe(ec), ec == 400 || ec == 401 || ec == 403 || ec == 404);
    }

    private static String describe(int ec) {
        switch (ec) {
            case 401:
            case 404: return "رمز البوت غير صحيح أو تم إلغاؤه";
            case 403: return "البوت محظور أو لم تبدأ محادثة معه";
            case 400: return "تعذر الإرسال، تحقق من المحادثة المرتبطة";
            case 429: return "تيليجرام يطلب الانتظار قليلاً، ستتم إعادة المحاولة";
            default: return "خطأ من تيليجرام (" + ec + ")";
        }
    }

    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (InputStream in = is) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) bo.write(buf, 0, n);
            return new String(bo.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    // ---------------------------------------------------------------- status text

    static String fmtTime(long t) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(t));
    }

    public static boolean hasError(Context c) {
        return isConfigured(c) && !prefs(c).getString("last_error", "").isEmpty();
    }

    /** Short status text without symbols, for cards. */
    public static String statusText(Context c) {
        if (!isConfigured(c)) return "غير مفعّل — اضغط لتفعيله وحماية بياناتك";
        SharedPreferences p = prefs(c);
        String err = p.getString("last_error", "");
        if (!err.isEmpty()) return err + " — اضغط للتفاصيل";
        long t = p.getLong("last_ok", 0);
        return t > 0 ? "مفعّل • آخر نسخة: " + fmtTime(t) : "مفعّل • بانتظار إرسال أول نسخة";
    }

    public static String statusLine(Context c) {
        return (hasError(c) ? "⚠  " : "☁  ") + statusText(c);
    }

    public static String statusDetail(Context c) {
        SharedPreferences p = prefs(c);
        long t = p.getLong("last_ok", 0);
        String s = t > 0 ? "آخر نسخة ناجحة: " + fmtTime(t) : "لم تُرسل أي نسخة بعد";
        String err = p.getString("last_error", "");
        if (!err.isEmpty()) s += "\n⚠  " + err;
        if (p.getBoolean("dirty", false)) s += "\nيوجد تعديلات بانتظار الإرسال";
        return s;
    }
}
