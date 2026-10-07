package com.example.debtmanager;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Tab: installments that are overdue, due today, or due within the next 7 days. */
final class AlertsScreen extends Screen {
    AlertsScreen(MainActivity a) {
        super(a);
    }

    @Override
    void refresh() {
        root.removeAllViews();
        Data.Snap s = a.snap;
        root.addView(Ui.header(a, "التنبيهات", "الأقساط المتأخرة والمستحقة", null, null));

        LinearLayout sum = Ui.row(a);
        sum.addView(summary(s.overdueCount, "متأخرة", Ui.RED), weight(4));
        sum.addView(summary(s.todayCount, "اليوم", Color.rgb(234, 88, 12)), weight(4));
        sum.addView(summary(s.soonCount, "قريباً", Ui.BLUE), weight(4));
        root.addView(sum);

        root.addView(Ui.section(a, "قائمة التنبيهات", "مرتبة من الأقدم تأخراً إلى الأقرب موعداً",
                "عن التنبيهات",
                "• متأخر: قسط مضى موعده ولم يُسدَّد.\n• مستحق اليوم: موعده اليوم.\n• قريباً: يستحق خلال 7 أيام.\n\nاضغط «تسجيل دفعة» عند استلام المبلغ، أو «اتصال» للتواصل مع العميل مباشرة. يظهر عدد المتأخرة والمستحقة اليوم على أيقونة التنبيهات في الشريط السفلي."));

        if (s.alerts.isEmpty()) {
            root.addView(Ui.empty(a, R.drawable.ic_check_circle, Ui.GREEN, "لا توجد أقساط مستحقة",
                    "لا يوجد قسط متأخر أو مستحق خلال الأيام السبعة القادمة."));
            return;
        }
        int last = -1;
        for (Sched.Alert al : s.alerts) {
            if (al.state != last) {
                last = al.state;
                root.addView(groupTitle(al.state));
            }
            root.addView(card(a, al, true));
        }
    }

    private View summary(int n, String label, int color) {
        LinearLayout b = Ui.col(a);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(8), dp(12), dp(8), dp(12));
        b.setBackground(Ui.rbg(a, Ui.soft(color), Color.TRANSPARENT, 18));
        TextView v = Ui.bold(Ui.txt(a, String.valueOf(n), 24, color));
        v.setGravity(Gravity.CENTER);
        b.addView(v);
        TextView l = Ui.txt(a, label, 12, color);
        l.setGravity(Gravity.CENTER);
        b.addView(l);
        return b;
    }

    private View groupTitle(int state) {
        String t = state == Sched.OVERDUE ? "متأخرة" : state == Sched.TODAY ? "مستحقة اليوم" : "تستحق قريباً (خلال 7 أيام)";
        int col = state == Sched.OVERDUE ? Ui.RED : state == Sched.TODAY ? Color.rgb(234, 88, 12) : Ui.BLUE;
        LinearLayout r = Ui.row(a);
        r.setPadding(dp(4), dp(12), dp(4), dp(2));
        View dot = new View(a);
        dot.setBackground(Ui.rbg(a, col, Color.TRANSPARENT, 5));
        r.addView(dot, new LinearLayout.LayoutParams(dp(10), dp(10)));
        TextView x = Ui.bold(Ui.txt(a, t, 14, col));
        x.setPaddingRelative(dp(8), 0, 0, 0);
        r.addView(x);
        return r;
    }

    /** One alert card, also used by the home screen. */
    static View card(final MainActivity a, final Sched.Alert al, boolean actions) {
        final Sched.Item it = al.item;
        int color = al.state == Sched.OVERDUE ? Ui.RED : al.state == Sched.TODAY ? Color.rgb(234, 88, 12) : Ui.BLUE;
        int icon = al.state == Sched.OVERDUE ? R.drawable.ic_warning : al.state == Sched.TODAY ? R.drawable.ic_calendar : R.drawable.ic_schedule;

        LinearLayout b = Ui.box(a);
        LinearLayout top = Ui.row(a);
        top.addView(Ui.iconBadge(a, icon, Ui.soft(color), color, 46, 24));

        LinearLayout mid = Ui.col(a);
        mid.addView(Ui.bold(Ui.txt(a, it.customerName, 16, Ui.TEXT)));
        String msg;
        if (al.state == Sched.OVERDUE) {
            msg = "عليه قسط مستحق بتاريخ " + al.due;
            if (al.count > 1) msg += " (" + al.count + " أقساط متأخرة)";
        } else if (al.state == Sched.TODAY) {
            msg = "عليه قسط مستحق اليوم (" + al.due + ")";
        } else {
            msg = "عليه قسط يستحق بتاريخ " + al.due;
        }
        TextView m = Ui.txt(a, msg, 13, color);
        m.setLineSpacing(0, 1.15f);
        mid.addView(m);
        mid.addView(Ui.txt(a, it.name + "  •  القسط رقم " + al.no, 12, Ui.MUTED));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
        mp.setMargins(dp(a, 12), 0, dp(a, 8), 0);
        top.addView(mid, mp);

        LinearLayout end = Ui.col(a);
        end.setGravity(Gravity.LEFT);
        end.addView(Ui.bold(Ui.txt(a, Ui.num(al.amount), 16, Ui.TEXT)));
        end.addView(Ui.txt(a, "د.ع", 11, Ui.MUTED));
        String when = al.state == Sched.OVERDUE ? "متأخر " + Dates.daysWord(al.days)
                : al.state == Sched.TODAY ? "اليوم" : "بعد " + Dates.daysWord(al.days);
        end.addView(Ui.chip(a, when, Ui.soft(color), color));
        top.addView(end);
        b.addView(top);

        if (actions) {
            LinearLayout r = Ui.row(a);
            r.setPadding(0, dp(a, 12), 0, 0);
            LinearLayout pay = Ui.button(a, "تسجيل دفعة", R.drawable.ic_payments, Ui.INDIGO, Ui.WHITE, Color.TRANSPARENT);
            pay.setOnClickListener(v -> Dialogs.payment(a, a.db, it, a::reload));
            LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0, dp(a, 44), 2);
            r.addView(pay, pp);
            if (!it.phone.isEmpty()) {
                LinearLayout call = Ui.button(a, "اتصال", R.drawable.ic_phone, Ui.WHITE, Ui.GREEN, Ui.BORDER);
                call.setOnClickListener(v -> {
                    try {
                        a.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + it.phone)));
                    } catch (Exception e) {
                        Ui.toast(a, "تعذر فتح تطبيق الاتصال");
                    }
                });
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(a, 44), 1);
                cp.setMargins(dp(a, 8), 0, 0, 0);
                r.addView(call, cp);
            }
            LinearLayout file = Ui.button(a, "الملف", R.drawable.ic_person, Ui.WHITE, Ui.INDIGO, Ui.BORDER);
            file.setOnClickListener(v -> a.openCustomer(it.customerId));
            LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(0, dp(a, 44), 1);
            fp.setMargins(dp(a, 8), 0, 0, 0);
            r.addView(file, fp);
            b.addView(r);
        } else {
            b.setClickable(true);
            b.setOnClickListener(v -> a.show(2));
        }
        return b;
    }

    private static int dp(MainActivity a, int v) {
        return Ui.dp(a, v);
    }
}
