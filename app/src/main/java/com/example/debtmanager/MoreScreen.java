package com.example.debtmanager;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Tab: tools — backups, reports, guide and about. */
final class MoreScreen extends Screen {
    MoreScreen(MainActivity a) {
        super(a);
    }

    @Override
    void refresh() {
        root.removeAllViews();
        root.addView(Ui.header(a, "المزيد", "الأدوات والمساعدة", null, null));

        root.addView(Ui.section(a, "حماية البيانات", "احفظ بياناتك من الضياع",
                "عن النسخ الاحتياطي",
                "• النسخ التلقائي: يرسل التطبيق نسخة من بياناتك إلى محادثتك الخاصة في تيليجرام بعد كل تعديل.\n• النسخة اليدوية: تحفظ ملفاً على هاتفك أو في أي مكان تختاره.\n• الاستعادة: تعيد البيانات من ملف نسخة احتياطية سابقة، وتستبدل البيانات الحالية."));
        boolean err = TelegramBackup.hasError(a);
        boolean on = TelegramBackup.isConfigured(a);
        root.addView(tile(R.drawable.ic_cloud, err ? Ui.RED : on ? Ui.GREEN : Ui.BLUE, "النسخ التلقائي (تيليجرام)",
                TelegramBackup.statusText(a), v -> a.openTelegramSetup()));
        root.addView(tile(R.drawable.ic_save, Ui.INDIGO, "إنشاء نسخة احتياطية", "احفظ ملف نسخة على هاتفك الآن", v -> a.createBackup()));
        root.addView(tile(R.drawable.ic_history, Ui.AMBER, "استعادة نسخة احتياطية", "أعد بياناتك من ملف نسخة سابقة", v -> a.openBackup()));

        root.addView(Ui.section(a, "التقارير", "أرقام ومتابعة", null, null));
        root.addView(tile(R.drawable.ic_chart, Ui.PURPLE, "التقارير المالية", "إجمالي الديون والمسدد والمتبقي والأرباح", v -> a.showReports()));
        root.addView(tile(R.drawable.ic_receipt, Ui.BLUE, "قائمة الأقساط الحالية", "السلع غير المسددة بالكامل والمتبقي منها", v -> a.showInstallments()));

        root.addView(Ui.section(a, "المساعدة", "", null, null));
        root.addView(tile(R.drawable.ic_book, Ui.GREEN, "دليل الاستخدام", "شرح مفصل لكل أقسام التطبيق",
                v -> a.startActivity(new Intent(a, GuideActivity.class))));
        root.addView(tile(R.drawable.ic_info, Ui.MUTED, "عن التطبيق", "الإصدار 1.5.1", v -> a.showAbout()));
    }

    private View tile(int icon, int color, String title, String sub, View.OnClickListener click) {
        LinearLayout b = Ui.box(a);
        b.setClickable(true);
        b.setOnClickListener(click);
        LinearLayout r = Ui.row(a);
        r.addView(Ui.iconBadge(a, icon, Ui.soft(color), color, 44, 24));
        LinearLayout t = Ui.col(a);
        t.addView(Ui.bold(Ui.txt(a, title, 15, Ui.TEXT)));
        TextView s = Ui.txt(a, sub, 12, Ui.MUTED);
        s.setLineSpacing(0, 1.15f);
        t.addView(s);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.setMargins(dp(12), 0, dp(8), 0);
        r.addView(t, tp);
        r.addView(Ui.icon(a, R.drawable.ic_chevron_left, 22, Ui.MUTED));
        b.addView(r);
        return b;
    }
}
