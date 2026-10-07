package com.example.debtmanager;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Tab: the home dashboard. */
final class HomeScreen extends Screen {
    HomeScreen(MainActivity a) {
        super(a);
    }

    @Override
    void refresh() {
        root.removeAllViews();
        Data.Snap s = a.snap;

        int badgeN = s.overdueCount + s.todayCount;
        FrameLayout bell = Ui.circleButton(a, R.drawable.ic_notifications, v -> a.show(2));
        if (badgeN > 0) {
            TextView b = Ui.txt(a, badgeN > 99 ? "99+" : String.valueOf(badgeN), 10, Color.WHITE);
            b.setGravity(Gravity.CENTER);
            b.setBackground(Ui.rbg(a, Ui.RED, Color.WHITE, 9));
            b.setMinWidth(dp(18));
            b.setPadding(dp(4), 0, dp(4), 0);
            bell.addView(b, new FrameLayout.LayoutParams(-2, dp(18), Gravity.TOP | Gravity.LEFT));
        }
        root.addView(Ui.header(a, "ديوني", Dates.arabicToday(), null, bell));

        root.addView(hero(s));

        root.addView(Ui.section(a, "نظرة عامة", "ملخص سريع لحالة الأقساط",
                "عن النظرة العامة",
                "كل بطاقة ملوّنة قابلة للضغط:\n• المتأخرة: أقساط مضى موعدها ولم تُسدَّد.\n• مستحقة اليوم: أقساط موعدها اليوم.\n• العملاء: عدد العملاء المسجلين.\n• قادمة: أقساط تستحق خلال 7 أيام."));
        LinearLayout r1 = Ui.row(a);
        r1.addView(tile(R.drawable.ic_warning, String.valueOf(s.overdueCount), "المتأخرة", "أقساط متأخرة عن السداد",
                Ui.RED, Color.rgb(248, 113, 113), v -> a.show(2)), weight(4));
        r1.addView(tile(R.drawable.ic_calendar, String.valueOf(s.todayCount), "مستحقة اليوم", "أقساط يجب تسديدها اليوم",
                Ui.AMBER, Color.rgb(251, 191, 36), v -> a.show(2)), weight(4));
        root.addView(r1);
        LinearLayout r2 = Ui.row(a);
        r2.addView(tile(R.drawable.ic_people, String.valueOf(s.customers.size()), "العملاء", "إجمالي العملاء المسجلين",
                Ui.BLUE, Color.rgb(96, 165, 250), v -> a.show(1)), weight(4));
        r2.addView(tile(R.drawable.ic_schedule, String.valueOf(s.soonCount), "أقساط قادمة", "تستحق خلال 7 أيام",
                Ui.GREEN, Color.rgb(52, 211, 153), v -> a.show(2)), weight(4));
        root.addView(r2);

        root.addView(Ui.section(a, "تنبيهات الأقساط", "المتأخرة والمستحقة قريباً",
                "عن التنبيهات", "تعرض هنا أهم الأقساط التي تحتاج متابعة: من عليه قسط متأخر أو مستحق اليوم أو خلال 7 أيام. افتح تبويب «التنبيهات» لتسجيل الدفعات والاتصال بالعملاء."));
        if (s.alerts.isEmpty()) {
            root.addView(Ui.empty(a, R.drawable.ic_check_circle, Ui.GREEN, "كل شيء على ما يرام",
                    "لا توجد أقساط متأخرة أو مستحقة قريباً."));
        } else {
            int n = Math.min(3, s.alerts.size());
            for (int i = 0; i < n; i++) root.addView(AlertsScreen.card(a, s.alerts.get(i), false));
            if (s.alerts.size() > n) {
                LinearLayout all = Ui.button(a, "عرض كل التنبيهات (" + s.alerts.size() + ")", R.drawable.ic_notifications,
                        Ui.WHITE, Ui.INDIGO, Ui.BORDER);
                all.setOnClickListener(v -> a.show(2));
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(46));
                p.setMargins(0, dp(6), 0, 0);
                root.addView(all, p);
            }
        }

        root.addView(Ui.section(a, "حماية البيانات", "النسخ الاحتياطي التلقائي", null, null));
        root.addView(backupPanel());
    }

    private View hero(Data.Snap s) {
        LinearLayout h = Ui.col(a);
        h.setPadding(dp(20), dp(18), dp(20), dp(18));
        h.setBackground(Ui.grad(Ui.INDIGO, Ui.PURPLE, 26, a));
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.setMargins(0, dp(4), 0, dp(6));
        h.setLayoutParams(hp);

        LinearLayout top = Ui.row(a);
        top.addView(Ui.iconBadge(a, R.drawable.ic_wallet, Color.argb(60, 255, 255, 255), Color.WHITE, 42, 22));
        LinearLayout t = Ui.col(a);
        t.addView(Ui.bold(Ui.txt(a, "إجمالي المتبقي لدى العملاء", 15, Color.WHITE)));
        t.addView(Ui.txt(a, "مجموع المبالغ التي لم تُسدَّد بعد", 12, Color.argb(210, 255, 255, 255)));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.setMargins(dp(10), 0, 0, 0);
        top.addView(t, tp);
        h.addView(top);

        TextView big = Ui.bold(Ui.txt(a, Ui.money(s.remaining), 30, Color.WHITE));
        big.setPadding(0, dp(12), 0, 0);
        h.addView(big);

        double frac = s.total > 0 ? s.paid / s.total : 0;
        h.addView(Ui.progress(a, frac, Color.WHITE, Color.argb(70, 255, 255, 255)));
        h.addView(Ui.txt(a, "تم تحصيل " + Ui.money(s.paid) + " من " + Ui.money(s.total) + "  (" + Math.round(frac * 100) + "%)",
                12, Color.argb(220, 255, 255, 255)));

        LinearLayout g = Ui.row(a);
        g.setPadding(0, dp(12), 0, 0);
        g.addView(glass("المستلم اليوم", Ui.money(s.receivedToday), R.drawable.ic_payments), weight(3));
        g.addView(glass("الأرباح المتوقعة", Ui.money(s.profit), R.drawable.ic_trending_up), weight(3));
        h.addView(g);
        return h;
    }

    private View glass(String label, String value, int icon) {
        LinearLayout b = Ui.col(a);
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        b.setBackground(Ui.rbg(a, Color.argb(45, 255, 255, 255), Color.TRANSPARENT, 16));
        LinearLayout r = Ui.row(a);
        r.addView(Ui.icon(a, icon, 16, Color.WHITE));
        TextView l = Ui.txt(a, label, 12, Color.argb(220, 255, 255, 255));
        l.setPaddingRelative(dp(6), 0, 0, 0);
        r.addView(l);
        b.addView(r);
        b.addView(Ui.bold(Ui.txt(a, value, 15, Color.WHITE)));
        return b;
    }

    private View tile(int icon, String value, String title, String sub, int c1, int c2, View.OnClickListener click) {
        LinearLayout t = Ui.col(a);
        t.setPadding(dp(14), dp(14), dp(14), dp(14));
        t.setBackground(Ui.grad(c1, c2, 22, a));
        t.setClickable(true);
        t.setOnClickListener(click);
        LinearLayout top = Ui.row(a);
        top.addView(Ui.iconBadge(a, icon, Color.argb(60, 255, 255, 255), Color.WHITE, 38, 20));
        top.addView(new View(a), new LinearLayout.LayoutParams(0, 1, 1));
        top.addView(Ui.icon(a, R.drawable.ic_chevron_left, 20, Color.argb(210, 255, 255, 255)));
        t.addView(top);
        TextView v = Ui.bold(Ui.txt(a, value, 28, Color.WHITE));
        v.setPadding(0, dp(8), 0, 0);
        t.addView(v);
        t.addView(Ui.bold(Ui.txt(a, title, 14, Color.WHITE)));
        TextView sb = Ui.txt(a, sub, 11, Color.argb(220, 255, 255, 255));
        sb.setLineSpacing(0, 1.1f);
        t.addView(sb);
        return t;
    }

    private View backupPanel() {
        boolean err = TelegramBackup.hasError(a);
        boolean on = TelegramBackup.isConfigured(a);
        int color = err ? Ui.RED : on ? Ui.GREEN : Ui.BLUE;
        LinearLayout b = Ui.box(a);
        b.setClickable(true);
        b.setOnClickListener(v -> a.openTelegramSetup());
        LinearLayout r = Ui.row(a);
        r.addView(Ui.iconBadge(a, err ? R.drawable.ic_warning : R.drawable.ic_cloud, Ui.soft(color), color, 44, 24));
        LinearLayout t = Ui.col(a);
        t.addView(Ui.bold(Ui.txt(a, on ? "النسخ التلقائي على تيليجرام" : "فعّل النسخ التلقائي", 15, Ui.TEXT)));
        TextView st = Ui.txt(a, TelegramBackup.statusText(a), 12, err ? Ui.RED : Ui.MUTED);
        st.setLineSpacing(0, 1.15f);
        t.addView(st);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.setMargins(dp(12), 0, dp(8), 0);
        r.addView(t, tp);
        r.addView(Ui.icon(a, R.drawable.ic_chevron_left, 22, Ui.MUTED));
        b.addView(r);
        return b;
    }
}
