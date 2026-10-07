package com.example.debtmanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.HashSet;
import java.util.Set;

/** A customer's file: profile, purchases, and each purchase's installment table. */
public class CustomerActivity extends Activity {
    Db db;
    long cid;
    LinearLayout root;
    final Set<Long> expanded = new HashSet<>();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        cid = getIntent().getLongExtra("id", 0);
        db = new Db(this);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Ui.BG);
        sv.setFillViewport(true);
        root = Ui.col(this);
        root.setPadding(dp(16), dp(12), dp(16), dp(28));
        sv.addView(root);
        Ui.window(this, sv);
        setContentView(sv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    int dp(int v) {
        return Ui.dp(this, v);
    }

    void render() {
        root.removeAllViews();
        Data.Snap snap = Data.load(db);
        final Data.Cust c = snap.find(cid);
        if (c == null) {
            finish();
            return;
        }
        final Runnable again = this::render;

        root.addView(Ui.header(this, "ملف العميل", c.name, Ui.circleButton(this, R.drawable.ic_arrow_forward, v -> finish()), null));
        root.addView(profile(c, again));

        root.addView(Ui.section(this, "المشتريات والأقساط", "جدول أقساط كل سلعة وحالتها",
                "عن جدول الأقساط",
                "لكل سلعة جدول يعرض:\n• أول قسط مستحق: أقدم قسط لم يُسدَّد بعد.\n• آخر قسط: القسط الأخير في الجدول.\n• الحالة: مدفوع (أخضر) • متأخر (أحمر) • اليوم (برتقالي) • قادم (أزرق) • جزئي (أصفر).\n\nاضغط «عرض جدول جميع الأقساط» لرؤية كل الأقساط. الدفعات تُوزَّع على أقدم قسط غير مدفوع أولاً."));
        LinearLayout add = Ui.button(this, "إضافة سلعة جديدة", R.drawable.ic_add, Ui.INDIGO, Ui.WHITE, Color.TRANSPARENT);
        add.setOnClickListener(v -> Dialogs.itemDialog(this, db, cid, null, again));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(-1, dp(48));
        ap.setMargins(0, dp(4), 0, dp(6));
        root.addView(add, ap);

        if (c.items.isEmpty()) {
            root.addView(Ui.empty(this, R.drawable.ic_cart, Ui.INDIGO, "لا توجد سلع لهذا العميل",
                    "اضغط «إضافة سلعة جديدة» لتسجيل أول عملية تقسيط."));
        }
        for (Sched.Item it : c.items) {
            try {
                root.addView(itemCard(it, again));
            } catch (Exception e) {
                LinearLayout fb = Ui.box(this);
                fb.addView(Ui.bold(Ui.txt(this, it.name, 16, Ui.TEXT)));
                fb.addView(Ui.txt(this, "المتبقي: " + Ui.money(it.remaining()), 12, Ui.MUTED));
                root.addView(fb);
            }
        }
    }

    // ------------------------------------------------------------ profile

    private View profile(final Data.Cust c, final Runnable again) {
        LinearLayout p = Ui.col(this);
        p.setPadding(dp(18), dp(16), dp(18), dp(16));
        p.setBackground(Ui.grad(Ui.INDIGO, Ui.PURPLE, 26, this));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(-1, -2);
        pp.setMargins(0, dp(4), 0, dp(6));
        p.setLayoutParams(pp);

        LinearLayout top = Ui.row(this);
        FrameLayout av = new FrameLayout(this);
        av.setBackground(Ui.rbg(this, Color.argb(60, 255, 255, 255), Color.TRANSPARENT, 26));
        TextView ini = Ui.bold(Ui.txt(this, c.name.isEmpty() ? "؟" : c.name.substring(0, 1), 22, Color.WHITE));
        ini.setGravity(Gravity.CENTER);
        av.addView(ini, new FrameLayout.LayoutParams(-1, -1));
        top.addView(av, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout mid = Ui.col(this);
        mid.addView(Ui.bold(Ui.txt(this, c.name, 19, Color.WHITE)));
        if (!c.phone.isEmpty()) {
            LinearLayout pr = Ui.row(this);
            pr.setClickable(true);
            pr.setPadding(0, dp(3), 0, dp(3));
            pr.addView(Ui.icon(this, R.drawable.ic_phone, 15, Color.WHITE));
            TextView pt = Ui.txt(this, c.phone, 13, Color.WHITE);
            pt.setPaddingRelative(dp(6), 0, 0, 0);
            pr.addView(pt);
            pr.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.phone)));
                } catch (Exception e) {
                    Ui.toast(this, "تعذر فتح تطبيق الاتصال");
                }
            });
            mid.addView(pr);
        }
        if (!c.address.isEmpty()) {
            LinearLayout ar = Ui.row(this);
            ar.addView(Ui.icon(this, R.drawable.ic_location, 15, Color.argb(220, 255, 255, 255)));
            TextView at = Ui.txt(this, c.address, 12, Color.argb(220, 255, 255, 255));
            at.setPaddingRelative(dp(6), 0, 0, 0);
            ar.addView(at);
            mid.addView(ar);
        }
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
        mp.setMargins(dp(12), 0, dp(6), 0);
        top.addView(mid, mp);

        LinearLayout edit = Ui.iconButton(this, R.drawable.ic_edit, Color.argb(55, 255, 255, 255), Color.WHITE, Color.TRANSPARENT);
        edit.setOnClickListener(v -> Dialogs.editCustomer(this, db, c, again));
        top.addView(edit);
        LinearLayout del = Ui.iconButton(this, R.drawable.ic_delete, Color.argb(55, 255, 255, 255), Color.WHITE, Color.TRANSPARENT);
        del.setOnClickListener(v -> Dialogs.deleteCustomer(this, db, c, this::finish));
        LinearLayout.LayoutParams dlp = (LinearLayout.LayoutParams) del.getLayoutParams();
        dlp.setMargins(dp(6), 0, 0, 0);
        top.addView(del, dlp);
        p.addView(top);

        LinearLayout g = Ui.row(this);
        g.setPadding(0, dp(14), 0, 0);
        g.addView(glass("إجمالي التقسيط", Ui.num(c.total)), weight(3));
        g.addView(glass("المدفوع", Ui.num(c.paid)), weight(3));
        g.addView(glass("المتبقي", Ui.num(c.remaining)), weight(3));
        p.addView(g);

        String line;
        if (c.items.isEmpty()) line = "لا توجد سلع مسجلة بعد";
        else if (c.state == Data.C_PAID) line = "جميع الأقساط مسددة";
        else if (c.nextDue != null && c.lastDue != null) line = "أول قسط مستحق: " + c.nextDue.due + "   •   آخر قسط: " + c.lastDue.due;
        else line = "لا يوجد جدول أقساط — حدّد عدد الأقساط من تعديل السلعة";
        TextView sum = Ui.txt(this, line, 12, Color.argb(230, 255, 255, 255));
        sum.setPadding(0, dp(10), 0, 0);
        p.addView(sum);
        return p;
    }

    private LinearLayout.LayoutParams weight(int margin) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(dp(margin), 0, dp(margin), 0);
        return p;
    }

    private View glass(String label, String value) {
        LinearLayout b = Ui.col(this);
        b.setPadding(dp(10), dp(8), dp(10), dp(8));
        b.setBackground(Ui.rbg(this, Color.argb(45, 255, 255, 255), Color.TRANSPARENT, 14));
        b.addView(Ui.txt(this, label, 11, Color.argb(220, 255, 255, 255)));
        b.addView(Ui.bold(Ui.txt(this, value, 14, Color.WHITE)));
        return b;
    }

    // ------------------------------------------------------------ purchases

    private View mini(String label, String value) {
        LinearLayout b = Ui.col(this);
        b.setPadding(dp(10), dp(8), dp(10), dp(8));
        b.setBackground(Ui.rbg(this, Ui.SOFT, Color.TRANSPARENT, 12));
        b.addView(Ui.txt(this, label, 11, Ui.MUTED));
        b.addView(Ui.bold(Ui.txt(this, value, 13, Ui.TEXT)));
        return b;
    }

    private View itemCard(final Sched.Item it, final Runnable again) {
        LinearLayout b = Ui.box(this);

        LinearLayout top = Ui.row(this);
        top.addView(Ui.iconBadge(this, R.drawable.ic_cart, Ui.soft(Ui.INDIGO), Ui.INDIGO, 44, 24));
        LinearLayout t = Ui.col(this);
        t.addView(Ui.bold(Ui.txt(this, it.name, 17, Ui.TEXT)));
        double each = it.list.isEmpty() ? it.each : it.list.get(0).amount;
        t.addView(Ui.txt(this, "قسط شهري " + Ui.money(each) + "  •  " + it.term + " قسط", 12, Ui.MUTED));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.setMargins(dp(12), 0, dp(8), 0);
        top.addView(t, tp);
        int st;
        String label;
        if (it.done()) {
            st = Sched.PAID;
            label = "مسدد";
        } else if (it.next != null && it.next.state == Sched.OVERDUE) {
            st = Sched.OVERDUE;
            label = "متأخر";
        } else if (it.next != null && it.next.state == Sched.TODAY) {
            st = Sched.TODAY;
            label = "مستحق اليوم";
        } else {
            st = Sched.UPCOMING;
            label = "قيد التقسيط";
        }
        top.addView(Ui.chip(this, label, Ui.soft(Ui.stateColor(st)), Ui.stateColor(st)));
        b.addView(top);

        double frac = it.total > 0 ? it.paid / it.total : 0;
        b.addView(Ui.progress(this, frac, Ui.GREEN, Ui.SOFT));
        LinearLayout pr = Ui.row(this);
        pr.addView(Ui.txt(this, "المدفوع " + Ui.money(it.paid) + " (" + Math.round(Math.min(1, frac) * 100) + "%)", 12, Ui.MUTED),
                new LinearLayout.LayoutParams(0, -2, 1));
        pr.addView(Ui.bold(Ui.txt(this, "المتبقي " + Ui.money(it.remaining()), 12, Ui.TEXT)));
        b.addView(pr);

        LinearLayout g = Ui.row(this);
        g.setPadding(0, dp(10), 0, dp(4));
        g.addView(mini("السعر الأصلي", Ui.num(it.cash)), weight(3));
        g.addView(mini("سعر التقسيط", Ui.num(it.total)), weight(3));
        g.addView(mini("الربح", Ui.num(it.profit())), weight(3));
        b.addView(g);

        TextView th = Ui.bold(Ui.txt(this, "جدول الأقساط", 14, Ui.TEXT));
        th.setPadding(dp(2), dp(10), dp(2), dp(6));
        b.addView(th);
        final boolean full = expanded.contains(it.id);
        b.addView(table(it, full));
        if (it.list.size() > 2) {
            LinearLayout tg = Ui.button(this, full ? "إخفاء الجدول الكامل" : "عرض جدول جميع الأقساط (" + it.list.size() + ")",
                    full ? R.drawable.ic_expand_less : R.drawable.ic_expand_more, Ui.WHITE, Ui.INDIGO, Ui.BORDER);
            tg.setOnClickListener(v -> {
                if (full) expanded.remove(it.id);
                else expanded.add(it.id);
                again.run();
            });
            LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(-1, dp(42));
            gp.setMargins(0, dp(8), 0, 0);
            b.addView(tg, gp);
        }

        LinearLayout act = Ui.row(this);
        act.setPadding(0, dp(12), 0, 0);
        LinearLayout pay = Ui.button(this, "تسجيل دفعة", R.drawable.ic_payments, Ui.INDIGO, Ui.WHITE, Color.TRANSPARENT);
        pay.setOnClickListener(v -> Dialogs.payment(this, db, it, again));
        act.addView(pay, new LinearLayout.LayoutParams(0, dp(46), 1));
        act.addView(small(R.drawable.ic_history, Ui.WHITE, Ui.INDIGO, Ui.BORDER, v -> Dialogs.history(this, db, it)));
        act.addView(small(R.drawable.ic_edit, Ui.WHITE, Ui.INDIGO, Ui.BORDER, v -> Dialogs.itemDialog(this, db, cid, it, again)));
        act.addView(small(R.drawable.ic_delete, Ui.soft(Ui.RED), Ui.RED, Color.TRANSPARENT, v -> Dialogs.deleteItem(this, db, it, again)));
        b.addView(act);
        return b;
    }

    private View small(int icon, int bg, int fg, int stroke, View.OnClickListener click) {
        LinearLayout bt = Ui.iconButton(this, icon, bg, fg, stroke);
        bt.setOnClickListener(click);
        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) bt.getLayoutParams();
        p.setMargins(dp(8), 0, 0, 0);
        return bt;
    }

    // ------------------------------------------------------------ installment table

    private static String shortLabel(int st) {
        return st == Sched.TODAY ? "اليوم" : Ui.stateLabel(st);
    }

    private TextView cell(String s, int color, boolean bold, float weight, int gravity) {
        TextView t = Ui.txt(this, s, 12, color);
        if (bold) Ui.bold(t);
        t.setGravity(gravity);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, -2, weight));
        return t;
    }

    private View headerRow() {
        LinearLayout r = Ui.row(this);
        r.setBackgroundColor(Ui.SOFT);
        r.setPadding(dp(10), dp(8), dp(10), dp(8));
        r.addView(cell("القسط", Ui.MUTED, true, 1.7f, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
        r.addView(cell("التاريخ", Ui.MUTED, true, 1.2f, Gravity.CENTER));
        r.addView(cell("المبلغ", Ui.MUTED, true, 1.1f, Gravity.CENTER));
        r.addView(cell("الحالة", Ui.MUTED, true, 0.9f, Gravity.CENTER));
        return r;
    }

    private View instRow(String label, Sched.Inst in, boolean highlight) {
        LinearLayout r = Ui.row(this);
        r.setPadding(dp(10), dp(9), dp(10), dp(9));
        if (highlight) r.setBackgroundColor(Color.rgb(248, 250, 255));
        int col = Ui.stateColor(in.state);
        r.addView(cell(label, Ui.TEXT, highlight, 1.7f, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
        r.addView(cell(in.due, in.state == Sched.OVERDUE ? Ui.RED : Ui.TEXT, false, 1.2f, Gravity.CENTER));
        double amount = in.state == Sched.PAID ? in.amount : in.remaining();
        r.addView(cell(Ui.num(amount), Ui.TEXT, true, 1.1f, Gravity.CENTER));
        TextView chip = Ui.chip(this, shortLabel(in.state), Ui.soft(col), col);
        chip.setTextSize(10);
        chip.setPadding(dp(4), dp(3), dp(4), dp(3));
        chip.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 0.9f));
        r.addView(chip);
        return r;
    }

    private View divider() {
        View v = new View(this);
        v.setBackgroundColor(Ui.SOFT);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, Math.max(1, dp(1))));
        return v;
    }

    private View table(Sched.Item it, boolean full) {
        LinearLayout t = Ui.col(this);
        t.setBackground(Ui.rbg(this, Ui.WHITE, Ui.BORDER, 14));
        t.setClipToOutline(true);
        t.addView(headerRow());
        if (it.list.isEmpty()) {
            TextView e = Ui.txt(this, "لا توجد أقساط مجدولة لهذه السلعة", 12, Ui.MUTED);
            e.setPadding(dp(12), dp(12), dp(12), dp(12));
            t.addView(e);
            return t;
        }
        if (full) {
            for (Sched.Inst in : it.list) {
                t.addView(divider());
                String label = "#" + in.no;
                if (in == it.next) label += " (الحالي)";
                t.addView(instRow(label, in, in == it.next));
            }
        } else {
            Sched.Inst first = it.next != null ? it.next : it.list.get(0);
            Sched.Inst last = it.last;
            t.addView(divider());
            if (first.no == last.no) {
                t.addView(instRow("القسط الأخير (#" + last.no + ")", last, true));
            } else {
                t.addView(instRow(it.next != null ? "أول قسط مستحق (#" + first.no + ")" : "أول قسط (#1)", first, true));
                t.addView(divider());
                t.addView(instRow("آخر قسط (#" + last.no + ")", last, false));
            }
        }
        return t;
    }
}
