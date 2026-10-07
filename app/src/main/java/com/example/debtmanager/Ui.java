package com.example.debtmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

/** Look and feel: colors, icons, cards, buttons and the bottom navigation bar. */
public class Ui {
    public static final int NAVY = Color.rgb(15, 23, 42);
    public static final int INDIGO = Color.rgb(79, 70, 229);
    public static final int PURPLE = Color.rgb(124, 58, 237);
    public static final int BLUE = Color.rgb(59, 130, 246);
    public static final int BLUE2 = Color.rgb(30, 64, 175);
    public static final int GREEN = Color.rgb(16, 185, 129);
    public static final int RED = Color.rgb(239, 68, 68);
    public static final int AMBER = Color.rgb(245, 158, 11);
    public static final int BG = Color.rgb(244, 246, 251);
    public static final int TEXT = Color.rgb(15, 23, 42);
    public static final int MUTED = Color.rgb(100, 116, 139);
    public static final int BORDER = Color.rgb(226, 232, 240);
    public static final int SOFT = Color.rgb(241, 245, 249);
    public static final int WHITE = Color.WHITE;
    public static final int TRANSPARENT_COLOR = Color.TRANSPARENT;

    // ------------------------------------------------------------ basics

    public static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return l;
    }

    public static LinearLayout root(Context c) {
        LinearLayout l = col(c);
        l.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 24));
        l.setBackgroundColor(BG);
        return l;
    }

    public static TextView txt(Context c, String s, int sp, int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return t;
    }

    public static TextView bold(TextView t) {
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    public static TextView title(Context c, String s) {
        return bold(txt(c, s, 22, TEXT));
    }

    public static TextView subtitle(Context c, String s) {
        return txt(c, s, 13, MUTED);
    }

    public static void toast(Context c, String s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }

    public static String norm(String s) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '\u0660' && ch <= '\u0669') b.append((char) ('0' + (ch - '\u0660')));
            else if (ch >= '\u06F0' && ch <= '\u06F9') b.append((char) ('0' + (ch - '\u06F0')));
            else if (ch == '\u066B') b.append('.');
            else if (ch == ',' || ch == '\u066C' || ch == ' ') continue;
            else b.append(ch);
        }
        return b.toString();
    }

    public static double num(EditText e) {
        try {
            return Double.parseDouble(norm(e.getText().toString()));
        } catch (Exception x) {
            return 0;
        }
    }

    public static int integer(EditText e) {
        try {
            return Integer.parseInt(norm(e.getText().toString()));
        } catch (Exception x) {
            return 0;
        }
    }

    public static String num(double x) {
        return String.format(Locale.US, "%,.0f", x);
    }

    public static String money(double x) {
        return num(x) + " د.ع";
    }

    public static String plain(double x) {
        return x == Math.rint(x) ? String.format(Locale.US, "%.0f", x) : String.format(Locale.US, "%.2f", x);
    }

    // ------------------------------------------------------------ drawables

    public static GradientDrawable rbg(Context c, int fill, int stroke, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(c, radiusDp));
        if (stroke != Color.TRANSPARENT) g.setStroke(Math.max(1, dp(c, 1)), stroke);
        return g;
    }

    public static GradientDrawable grad(int a, int b, int radiusDp, Context c) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a, b});
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    /** Old helper kept for the Telegram setup screen: radius in pixels. */
    public static GradientDrawable bg(int fill, int stroke, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(radius);
        g.setStroke(2, stroke);
        return g;
    }

    public static Drawable ripple(Drawable content) {
        if (Build.VERSION.SDK_INT >= 21) {
            return new RippleDrawable(ColorStateList.valueOf(Color.argb(40, 0, 0, 0)), content, null);
        }
        return content;
    }

    public static int soft(int color) {
        return Color.rgb(Color.red(color) + (255 - Color.red(color)) * 88 / 100,
                Color.green(color) + (255 - Color.green(color)) * 88 / 100,
                Color.blue(color) + (255 - Color.blue(color)) * 88 / 100);
    }

    // ------------------------------------------------------------ icons

    public static ImageView icon(Context c, int res, int sizeDp, int tint) {
        ImageView v = new ImageView(c);
        v.setImageResource(res);
        v.setColorFilter(tint, PorterDuff.Mode.SRC_IN);
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp)));
        return v;
    }

    public static FrameLayout iconBadge(Context c, int res, int bgColor, int tint, int boxDp, int iconDp) {
        FrameLayout f = new FrameLayout(c);
        f.setBackground(rbg(c, bgColor, Color.TRANSPARENT, boxDp * 32 / 100));
        f.addView(icon(c, res, iconDp, tint), new FrameLayout.LayoutParams(dp(c, iconDp), dp(c, iconDp), Gravity.CENTER));
        f.setLayoutParams(new LinearLayout.LayoutParams(dp(c, boxDp), dp(c, boxDp)));
        return f;
    }

    public static FrameLayout circleButton(Context c, int res, View.OnClickListener click) {
        FrameLayout f = new FrameLayout(c);
        f.setBackground(ripple(rbg(c, WHITE, BORDER, 22)));
        f.addView(icon(c, res, 22, TEXT), new FrameLayout.LayoutParams(dp(c, 22), dp(c, 22), Gravity.CENTER));
        f.setLayoutParams(new LinearLayout.LayoutParams(dp(c, 44), dp(c, 44)));
        f.setClickable(true);
        if (click != null) f.setOnClickListener(click);
        return f;
    }

    // ------------------------------------------------------------ cards and buttons

    public static LinearLayout box(Context c) {
        LinearLayout l = col(c);
        l.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14));
        l.setBackground(rbg(c, WHITE, BORDER, 20));
        if (Build.VERSION.SDK_INT >= 21) l.setElevation(dp(c, 1));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(c, 6), 0, dp(c, 6));
        l.setLayoutParams(p);
        return l;
    }

    /** Text inside a rounded card (used by the Telegram setup screen). */
    public static LinearLayout card(Context c, String s) {
        LinearLayout l = box(c);
        TextView t = txt(c, s, 14, TEXT);
        t.setLineSpacing(0, 1.2f);
        l.addView(t);
        return l;
    }

    public static LinearLayout button(Context c, String text, int iconRes, int bg, int fg, int stroke) {
        LinearLayout b = row(c);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(c, 14), 0, dp(c, 14), 0);
        b.setMinimumHeight(dp(c, 46));
        b.setBackground(ripple(rbg(c, bg, stroke, 14)));
        b.setClickable(true);
        if (iconRes != 0) b.addView(icon(c, iconRes, 18, fg));
        if (text != null && !text.isEmpty()) {
            TextView t = bold(txt(c, text, 14, fg));
            t.setGravity(Gravity.CENTER);
            if (iconRes != 0) t.setPaddingRelative(dp(c, 8), 0, 0, 0);
            b.addView(t);
        }
        return b;
    }

    public static LinearLayout iconButton(Context c, int res, int bg, int fg, int stroke) {
        LinearLayout b = button(c, "", res, bg, fg, stroke);
        b.setPadding(0, 0, 0, 0);
        b.setLayoutParams(new LinearLayout.LayoutParams(dp(c, 46), dp(c, 46)));
        return b;
    }

    /** Old-style text view button, kept for compatibility. */
    public static Button btn(Context c, String s) {
        Button b = new Button(c);
        b.setText(s);
        b.setTextColor(WHITE);
        b.setBackground(rbg(c, INDIGO, Color.TRANSPARENT, 14));
        return b;
    }

    public static TextView chip(Context c, String s, int bg, int fg) {
        TextView t = bold(txt(c, s, 11, fg));
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(c, 10), dp(c, 3), dp(c, 10), dp(c, 3));
        t.setBackground(rbg(c, bg, Color.TRANSPARENT, 12));
        return t;
    }

    public static int stateColor(int st) {
        switch (st) {
            case Sched.PAID: return GREEN;
            case Sched.PARTIAL: return AMBER;
            case Sched.OVERDUE: return RED;
            case Sched.TODAY: return Color.rgb(234, 88, 12);
            default: return BLUE;
        }
    }

    public static String stateLabel(int st) {
        switch (st) {
            case Sched.PAID: return "مدفوع";
            case Sched.PARTIAL: return "جزئي";
            case Sched.OVERDUE: return "متأخر";
            case Sched.TODAY: return "مستحق اليوم";
            default: return "قادم";
        }
    }

    public static TextView stateChip(Context c, int st) {
        return chip(c, stateLabel(st), soft(stateColor(st)), stateColor(st));
    }

    public static View progress(Context c, double frac, int color, int track) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        l.setBackground(rbg(c, track, Color.TRANSPARENT, 4));
        float f = (float) Math.max(0, Math.min(1, frac));
        View fill = new View(c);
        fill.setBackground(rbg(c, color, Color.TRANSPARENT, 4));
        l.addView(fill, new LinearLayout.LayoutParams(0, -1, f));
        l.addView(new View(c), new LinearLayout.LayoutParams(0, -1, 1f - f));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(c, 8));
        p.setMargins(0, dp(c, 8), 0, dp(c, 6));
        l.setLayoutParams(p);
        return l;
    }

    // ------------------------------------------------------------ headers and sections

    public static LinearLayout header(Context c, String title, String sub, View lead, View trail) {
        LinearLayout r = row(c);
        r.setPadding(0, dp(c, 4), 0, dp(c, 10));
        if (lead != null) {
            r.addView(lead);
            ((LinearLayout.LayoutParams) lead.getLayoutParams()).setMargins(0, 0, 0, 0);
        }
        LinearLayout t = col(c);
        t.setGravity(Gravity.RIGHT);
        t.addView(title(c, title));
        if (sub != null && !sub.isEmpty()) t.addView(subtitle(c, sub));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        if (lead != null) tp.setMargins(dp(c, 6), 0, dp(c, 6), 0);
        r.addView(t, tp);
        if (trail != null) r.addView(trail);
        return r;
    }

    public static LinearLayout section(final Context c, String title, String sub, final String helpTitle, final String help) {
        LinearLayout r = row(c);
        r.setPadding(dp(c, 2), dp(c, 14), dp(c, 2), dp(c, 6));
        LinearLayout t = col(c);
        t.addView(bold(txt(c, title, 17, TEXT)));
        if (sub != null && !sub.isEmpty()) t.addView(txt(c, sub, 12, MUTED));
        r.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        if (help != null) {
            ImageView h = icon(c, R.drawable.ic_help, 24, MUTED);
            h.setPadding(dp(c, 2), dp(c, 2), dp(c, 2), dp(c, 2));
            h.setOnClickListener(v -> help(c, helpTitle, help));
            r.addView(h);
        }
        return r;
    }

    public static void help(Context c, String title, String msg) {
        new AlertDialog.Builder(c).setTitle(title).setMessage(msg).setPositiveButton("حسنًا", null).show();
    }

    public static LinearLayout empty(Context c, int iconRes, int color, String title, String msg) {
        LinearLayout l = box(c);
        l.setGravity(Gravity.CENTER_HORIZONTAL);
        l.setPadding(dp(c, 20), dp(c, 22), dp(c, 20), dp(c, 22));
        FrameLayout ic = iconBadge(c, iconRes, soft(color), color, 60, 30);
        ((LinearLayout.LayoutParams) ic.getLayoutParams()).setMargins(0, 0, 0, dp(c, 10));
        l.addView(ic);
        TextView a = bold(txt(c, title, 16, TEXT));
        a.setGravity(Gravity.CENTER);
        l.addView(a);
        TextView b = txt(c, msg, 13, MUTED);
        b.setGravity(Gravity.CENTER);
        l.addView(b);
        return l;
    }

    // ------------------------------------------------------------ dialogs and forms

    public static LinearLayout dialogBox(Context c) {
        LinearLayout l = col(c);
        l.setPadding(dp(c, 20), dp(c, 8), dp(c, 20), dp(c, 8));
        return l;
    }

    public static ScrollView scroll(Context c, View v) {
        ScrollView s = new ScrollView(c);
        s.addView(v);
        return s;
    }

    public static EditText edit(Context c, String hint) {
        EditText e = new EditText(c);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setTextSize(15);
        e.setTextColor(TEXT);
        e.setHintTextColor(Color.rgb(148, 163, 184));
        e.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        e.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        e.setPadding(dp(c, 14), dp(c, 6), dp(c, 14), dp(c, 6));
        e.setMinHeight(dp(c, 50));
        e.setBackground(rbg(c, WHITE, BORDER, 12));
        return e;
    }

    public static EditText numberEdit(Context c, String hint, boolean decimal) {
        EditText e = edit(c, hint);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | (decimal ? InputType.TYPE_NUMBER_FLAG_DECIMAL : 0));
        return e;
    }

    public static void addField(LinearLayout parent, View v) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, dp(parent.getContext(), 6), 0, dp(parent.getContext(), 6));
        parent.addView(v, p);
    }

    public static TextView hint(Context c, String s) {
        TextView t = txt(c, s, 12, MUTED);
        t.setLineSpacing(0, 1.2f);
        t.setPadding(dp(c, 2), dp(c, 6), dp(c, 2), dp(c, 2));
        return t;
    }

    // ------------------------------------------------------------ window

    public static void window(Activity a, View shell) {
        if (Build.VERSION.SDK_INT >= 21) {
            a.getWindow().setStatusBarColor(BG);
            a.getWindow().setNavigationBarColor(WHITE);
        }
        View decor = a.getWindow().getDecorView();
        int f = decor.getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= 23) f |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (Build.VERSION.SDK_INT >= 26) f |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        decor.setSystemUiVisibility(f);
        if (Build.VERSION.SDK_INT >= 35 && shell != null) {
            shell.setOnApplyWindowInsetsListener((v, ins) -> {
                v.setPadding(0, ins.getSystemWindowInsetTop(), 0, ins.getSystemWindowInsetBottom());
                return ins;
            });
        }
    }

    // ------------------------------------------------------------ bottom navigation

    public interface OnTab {
        void onTab(int index);
    }

    public static class Nav {
        public final LinearLayout view;
        final Context c;
        final int n;
        final ImageView[] ic;
        final TextView[] lb, badge;
        final LinearLayout[] item;

        public Nav(Context c, String[] labels, int[] icons, final OnTab cb) {
            this.c = c;
            n = labels.length;
            ic = new ImageView[n];
            lb = new TextView[n];
            badge = new TextView[n];
            item = new LinearLayout[n];
            view = row(c);
            view.setBackgroundColor(WHITE);
            view.setPadding(dp(c, 8), dp(c, 6), dp(c, 8), dp(c, 6));
            for (int i = 0; i < n; i++) {
                final int idx = i;
                LinearLayout it = col(c);
                it.setGravity(Gravity.CENTER_HORIZONTAL);
                it.setPadding(dp(c, 4), dp(c, 8), dp(c, 4), dp(c, 8));
                it.setClickable(true);
                FrameLayout wrap = new FrameLayout(c);
                ImageView im = icon(c, icons[i], 24, MUTED);
                wrap.addView(im, new FrameLayout.LayoutParams(dp(c, 24), dp(c, 24), Gravity.CENTER));
                TextView b = txt(c, "", 10, Color.WHITE);
                b.setGravity(Gravity.CENTER);
                b.setBackground(rbg(c, RED, Color.TRANSPARENT, 9));
                b.setMinWidth(dp(c, 18));
                b.setPadding(dp(c, 4), 0, dp(c, 4), 0);
                b.setVisibility(View.GONE);
                wrap.addView(b, new FrameLayout.LayoutParams(-2, dp(c, 18), Gravity.TOP | Gravity.LEFT));
                it.addView(wrap, new LinearLayout.LayoutParams(dp(c, 44), dp(c, 28)));
                TextView l = txt(c, labels[i], 11, MUTED);
                l.setGravity(Gravity.CENTER);
                it.addView(l);
                it.setOnClickListener(v -> cb.onTab(idx));
                ic[i] = im;
                lb[i] = l;
                badge[i] = b;
                item[i] = it;
                view.addView(it, new LinearLayout.LayoutParams(0, -2, 1));
            }
        }

        public void select(int i) {
            for (int j = 0; j < n; j++) {
                boolean sel = j == i;
                item[j].setBackground(sel ? rbg(c, Color.rgb(238, 242, 255), Color.TRANSPARENT, 16) : null);
                ic[j].setColorFilter(sel ? INDIGO : MUTED, PorterDuff.Mode.SRC_IN);
                lb[j].setTextColor(sel ? INDIGO : MUTED);
                lb[j].setTypeface(Typeface.DEFAULT, sel ? Typeface.BOLD : Typeface.NORMAL);
            }
        }

        public void setBadge(int i, int count) {
            badge[i].setText(count > 99 ? "99+" : String.valueOf(count));
            badge[i].setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        }
    }
}
