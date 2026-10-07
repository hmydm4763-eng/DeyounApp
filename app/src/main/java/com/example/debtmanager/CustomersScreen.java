package com.example.debtmanager;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Tab: all customers with their first and last installment dates. */
final class CustomersScreen extends Screen {
    static final String[] FILTERS = {"الكل", "متأخرون", "قيد السداد", "مسددون"};
    static final int[][] AVATARS = {
            {Color.rgb(99, 102, 241), Color.rgb(139, 92, 246)},
            {Color.rgb(59, 130, 246), Color.rgb(56, 189, 248)},
            {Color.rgb(16, 185, 129), Color.rgb(52, 211, 153)},
            {Color.rgb(245, 158, 11), Color.rgb(251, 191, 36)},
            {Color.rgb(236, 72, 153), Color.rgb(244, 114, 182)}};

    final LinearLayout list;
    final EditText search;
    final TextView count;
    final TextView[] chips = new TextView[FILTERS.length];
    int filter = 0;

    CustomersScreen(MainActivity a) {
        super(a);
        LinearLayout add = Ui.button(a, "إضافة عميل", R.drawable.ic_person_add, Ui.INDIGO, Ui.WHITE, Color.TRANSPARENT);
        add.setOnClickListener(v -> Dialogs.addCustomer(a, a.db, a::reload));
        root.addView(Ui.header(a, "العملاء", "اضغط على أي عميل لعرض ملفه وجدول أقساطه", null, add));

        LinearLayout sb = Ui.row(a);
        sb.setBackground(Ui.rbg(a, Ui.WHITE, Ui.BORDER, 16));
        sb.setPadding(dp(14), dp(2), dp(14), dp(2));
        sb.addView(Ui.icon(a, R.drawable.ic_search, 20, Ui.MUTED));
        search = new EditText(a);
        search.setHint("ابحث بالاسم أو رقم الهاتف");
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setBackground(null);
        search.setHintTextColor(Color.rgb(148, 163, 184));
        search.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        search.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        search.setPaddingRelative(dp(10), dp(12), dp(4), dp(12));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int af) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable e) { fill(); }
        });
        sb.addView(search, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.setMargins(0, dp(4), 0, dp(8));
        root.addView(sb, sp);

        LinearLayout fr = Ui.row(a);
        for (int i = 0; i < FILTERS.length; i++) {
            final int idx = i;
            TextView c = Ui.bold(Ui.txt(a, FILTERS[i], 13, Ui.MUTED));
            c.setGravity(Gravity.CENTER);
            c.setPadding(dp(4), dp(9), dp(4), dp(9));
            c.setClickable(true);
            c.setOnClickListener(v -> {
                filter = idx;
                styleChips();
                fill();
            });
            chips[i] = c;
            fr.addView(c, weight(3));
        }
        root.addView(fr);
        styleChips();

        count = Ui.txt(a, "", 12, Ui.MUTED);
        count.setPadding(dp(4), dp(10), dp(4), dp(2));
        root.addView(count);

        list = Ui.col(a);
        root.addView(list);
    }

    void styleChips() {
        for (int i = 0; i < chips.length; i++) {
            boolean sel = i == filter;
            chips[i].setTextColor(sel ? Color.WHITE : Ui.MUTED);
            chips[i].setBackground(sel ? Ui.rbg(a, Ui.INDIGO, Color.TRANSPARENT, 14) : Ui.rbg(a, Ui.WHITE, Ui.BORDER, 14));
        }
    }

    @Override
    void refresh() {
        fill();
    }

    void fill() {
        list.removeAllViews();
        String q = search.getText().toString().trim().toLowerCase();
        int shown = 0;
        for (Data.Cust k : a.snap.customers) {
            if (filter == 1 && k.state != Data.C_OVERDUE) continue;
            if (filter == 2 && !(k.state == Data.C_ACTIVE || k.state == Data.C_TODAY || k.state == Data.C_OVERDUE)) continue;
            if (filter == 3 && k.state != Data.C_PAID) continue;
            if (!q.isEmpty() && !k.name.toLowerCase().contains(q) && !k.phone.contains(q)) continue;
            list.addView(card(k));
            shown++;
        }
        count.setText(shown + " من " + a.snap.customers.size() + " عميل");
        if (shown == 0) {
            boolean none = a.snap.customers.isEmpty();
            list.addView(Ui.empty(a, R.drawable.ic_people, Ui.INDIGO,
                    none ? "لا يوجد عملاء بعد" : "لا توجد نتائج",
                    none ? "اضغط «إضافة عميل» لتسجيل أول عميل." : "جرّب تغيير البحث أو التصفية."));
        }
    }

    private View infoRow(int icon, String text, int color) {
        LinearLayout r = Ui.row(a);
        r.setPadding(0, dp(3), 0, dp(3));
        r.addView(Ui.icon(a, icon, 16, color));
        TextView t = Ui.txt(a, text, 12, color);
        t.setPaddingRelative(dp(6), 0, 0, 0);
        r.addView(t);
        return r;
    }

    private View card(final Data.Cust k) {
        LinearLayout b = Ui.box(a);
        b.setClickable(true);
        b.setOnClickListener(v -> a.openCustomer(k.id));

        LinearLayout top = Ui.row(a);
        int[] g = AVATARS[(int) (k.id % AVATARS.length)];
        FrameLayout av = new FrameLayout(a);
        av.setBackground(Ui.grad(g[0], g[1], 24, a));
        String initial = k.name.isEmpty() ? "؟" : k.name.substring(0, 1);
        TextView ini = Ui.bold(Ui.txt(a, initial, 20, Color.WHITE));
        ini.setGravity(Gravity.CENTER);
        av.addView(ini, new FrameLayout.LayoutParams(-1, -1));
        top.addView(av, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout mid = Ui.col(a);
        mid.addView(Ui.bold(Ui.txt(a, k.name, 16, Ui.TEXT)));
        if (!k.phone.isEmpty()) {
            LinearLayout pr = Ui.row(a);
            pr.addView(Ui.icon(a, R.drawable.ic_phone, 13, Ui.MUTED));
            TextView pt = Ui.txt(a, k.phone, 12, Ui.MUTED);
            pt.setPaddingRelative(dp(4), 0, 0, 0);
            pr.addView(pt);
            mid.addView(pr);
        }
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
        mp.setMargins(dp(12), 0, dp(8), 0);
        top.addView(mid, mp);

        LinearLayout end = Ui.col(a);
        end.setGravity(Gravity.LEFT);
        boolean owes = k.remaining > Sched.EPS;
        end.addView(Ui.bold(Ui.txt(a, Ui.num(k.remaining), 16, owes ? Ui.TEXT : Ui.GREEN)));
        end.addView(Ui.txt(a, "د.ع متبقي", 11, Ui.MUTED));
        top.addView(end);
        top.addView(Ui.icon(a, R.drawable.ic_chevron_left, 22, Ui.MUTED));
        b.addView(top);

        View line = new View(a);
        line.setBackgroundColor(Ui.SOFT);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Math.max(1, dp(1)));
        lp.setMargins(0, dp(10), 0, dp(6));
        b.addView(line, lp);

        if (k.state == Data.C_NONE) {
            b.addView(infoRow(R.drawable.ic_info, "لا توجد سلع بعد — افتح الملف لإضافة سلعة", Ui.MUTED));
        } else if (k.state == Data.C_PAID) {
            b.addView(infoRow(R.drawable.ic_check_circle, "جميع الأقساط مسددة", Ui.GREEN));
            if (k.lastDue != null) b.addView(infoRow(R.drawable.ic_calendar, "آخر قسط: " + k.lastDue.due, Ui.MUTED));
        } else {
            String first = "أول قسط مستحق: " + k.nextDue.due;
            int fc = Ui.MUTED;
            if (k.state == Data.C_OVERDUE) {
                first += "  (متأخر " + Dates.daysWord(k.overdueDays) + ")";
                fc = Ui.RED;
            } else if (k.state == Data.C_TODAY) {
                first += "  (اليوم)";
                fc = Ui.stateColor(Sched.TODAY);
            }
            b.addView(infoRow(R.drawable.ic_calendar, first, fc));
            if (k.lastDue != null) b.addView(infoRow(R.drawable.ic_schedule, "آخر قسط: " + k.lastDue.due, Ui.MUTED));
            int st = k.state == Data.C_OVERDUE ? Sched.OVERDUE : k.state == Data.C_TODAY ? Sched.TODAY : Sched.UPCOMING;
            TextView chip = Ui.chip(a, k.state == Data.C_OVERDUE ? "متأخر" : k.state == Data.C_TODAY ? "مستحق اليوم" : "قيد السداد",
                    Ui.soft(Ui.stateColor(st)), Ui.stateColor(st));
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
            cp.gravity = Gravity.RIGHT;
            cp.setMargins(0, dp(6), 0, 0);
            b.addView(chip, cp);
        }
        return b;
    }
}
