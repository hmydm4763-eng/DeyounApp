package com.example.debtmanager;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/** A tab of the main screen. {@code refresh()} redraws it from the latest data snapshot. */
abstract class Screen {
    final MainActivity a;
    final ScrollView scroll;
    final LinearLayout root;

    Screen(MainActivity a) {
        this.a = a;
        scroll = new ScrollView(a);
        scroll.setBackgroundColor(Ui.BG);
        scroll.setFillViewport(true);
        root = Ui.col(a);
        root.setPadding(dp(16), dp(12), dp(16), dp(24));
        scroll.addView(root);
    }

    int dp(int v) {
        return Ui.dp(a, v);
    }

    View view() {
        return scroll;
    }

    LinearLayout.LayoutParams weight(int margin) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, -2, 1);
        p.setMargins(dp(margin), dp(margin), dp(margin), dp(margin));
        return p;
    }

    abstract void refresh();
}
