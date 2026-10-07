package com.example.debtmanager;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/** Small date helpers. All dates are stored as yyyy-MM-dd text. */
public final class Dates {
    private Dates() {}

    static final String[] MONTHS = {"يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"};
    static final String[] DAYS = {"الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"};

    public static String fmt(int y, int m, int d) {
        return String.format(Locale.US, "%04d-%02d-%02d", y, m, d);
    }

    public static String today() {
        Calendar c = Calendar.getInstance();
        return fmt(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    /** Parses yyyy-MM-dd into {year, month, day}; falls back to today if the text is not a valid date. */
    public static int[] parts(String s) {
        try {
            String[] p = s.trim().split("-");
            int y = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            int d = Integer.parseInt(p[2].length() > 2 ? p[2].substring(0, 2) : p[2]);
            if (m < 1 || m > 12 || d < 1 || d > 31) throw new IllegalArgumentException();
            return new int[]{y, m, d};
        } catch (Exception e) {
            Calendar c = Calendar.getInstance();
            return new int[]{c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)};
        }
    }

    /** Whole days since 1970-01-01 (timezone and daylight-saving safe). */
    public static long dayNum(String s) {
        int[] p = parts(s);
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.set(p[0], p[1] - 1, p[2]);
        return c.getTimeInMillis() / 86400000L;
    }

    public static long todayNum() {
        return dayNum(today());
    }

    /** Adds months to a date, keeping the day of month when possible (31 Jan + 1 month = 28/29 Feb). */
    public static String addMonths(String s, int months) {
        int[] p = parts(s);
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(p[0], p[1] - 1, 1);
        c.add(Calendar.MONTH, months);
        int max = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        return fmt(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, Math.min(p[2], max));
    }

    public static String arabicToday() {
        Calendar c = Calendar.getInstance();
        return DAYS[c.get(Calendar.DAY_OF_WEEK) - 1] + "، " + c.get(Calendar.DAY_OF_MONTH) + " "
                + MONTHS[c.get(Calendar.MONTH)] + " " + c.get(Calendar.YEAR);
    }

    public static String daysWord(long n) {
        if (n == 1) return "يوم واحد";
        if (n == 2) return "يومين";
        if (n >= 3 && n <= 10) return n + " أيام";
        return n + " يوماً";
    }
}
