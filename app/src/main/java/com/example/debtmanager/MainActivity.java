package com.example.debtmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Hosts the four tabs (home, customers, alerts, more) and the backup/restore file pickers. */
public class MainActivity extends Activity {
    static final int REQ_CREATE_BACKUP = 1001, REQ_OPEN_BACKUP = 1002;
    static final int TAB_HOME = 0, TAB_CUSTOMERS = 1, TAB_ALERTS = 2, TAB_MORE = 3;

    Db db;
    Data.Snap snap;
    FrameLayout content;
    Ui.Nav nav;
    Screen[] screens;
    int tab = TAB_HOME;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        db = new Db(this);
        snap = Data.load(db);

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        shell.setBackgroundColor(Ui.BG);
        content = new FrameLayout(this);
        shell.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        View line = new View(this);
        line.setBackgroundColor(Ui.BORDER);
        shell.addView(line, new LinearLayout.LayoutParams(-1, Math.max(1, Ui.dp(this, 1))));
        nav = new Ui.Nav(this,
                new String[]{"الرئيسية", "العملاء", "التنبيهات", "المزيد"},
                new int[]{R.drawable.ic_home, R.drawable.ic_people, R.drawable.ic_notifications, R.drawable.ic_apps},
                i -> show(i));
        shell.addView(nav.view, new LinearLayout.LayoutParams(-1, -2));
        Ui.window(this, shell);
        setContentView(shell);

        screens = new Screen[]{new HomeScreen(this), new CustomersScreen(this), new AlertsScreen(this), new MoreScreen(this)};
        show(TAB_HOME);
    }

    void show(int i) {
        tab = i;
        content.removeAllViews();
        content.addView(screens[i].view(), new FrameLayout.LayoutParams(-1, -1));
        nav.select(i);
        nav.setBadge(TAB_ALERTS, snap.overdueCount + snap.todayCount);
        safeRefresh(i);
    }

    private void safeRefresh(int i) {
        try {
            screens[i].refresh();
        } catch (Exception e) {
            Ui.toast(this, "تعذر عرض هذا القسم، تحقق من بيانات العملاء");
        }
    }

    /** Reloads all data and redraws the current tab. */
    void reload() {
        snap = Data.load(db);
        nav.setBadge(TAB_ALERTS, snap.overdueCount + snap.todayCount);
        safeRefresh(tab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TelegramBackup.onAppStart(this);
        if (screens != null) reload();
    }

    @Override
    public void onBackPressed() {
        if (tab != TAB_HOME) {
            show(TAB_HOME);
            return;
        }
        super.onBackPressed();
    }

    void openCustomer(long id) {
        Intent i = new Intent(this, CustomerActivity.class);
        i.putExtra("id", id);
        startActivity(i);
    }

    void openTelegramSetup() {
        startActivity(new Intent(this, TelegramSetupActivity.class));
    }

    // ------------------------------------------------------------ reports

    void showReports() {
        Data.Snap s = snap;
        String msg = "إجمالي التقسيط: " + Ui.money(s.total)
                + "\nالمبلغ المسدد: " + Ui.money(s.paid)
                + "\nالمتبقي: " + Ui.money(s.remaining)
                + "\nالأرباح المتوقعة: " + Ui.money(s.profit)
                + "\nالمستلم اليوم: " + Ui.money(s.receivedToday)
                + "\n\nعدد العملاء: " + s.customers.size()
                + "\nأقساط متأخرة: " + s.overdueCount
                + "\nأقساط مستحقة اليوم: " + s.todayCount
                + "\nأقساط قادمة (7 أيام): " + s.soonCount;
        new AlertDialog.Builder(this).setTitle("التقارير المالية").setMessage(msg).setPositiveButton("إغلاق", null).show();
    }

    void showInstallments() {
        StringBuilder sb = new StringBuilder();
        for (Data.Cust k : snap.customers) {
            for (Sched.Item it : k.items) {
                if (it.done()) continue;
                sb.append(k.name).append("  —  ").append(it.name).append("\n   المتبقي: ").append(Ui.money(it.remaining()));
                if (it.next != null) sb.append("  •  القسط القادم: ").append(it.next.due);
                sb.append("\n\n");
            }
        }
        new AlertDialog.Builder(this).setTitle("الأقساط الحالية")
                .setMessage(sb.length() == 0 ? "لا توجد أقساط حالية، جميع السلع مسددة." : sb.toString().trim())
                .setPositiveButton("إغلاق", null).show();
    }

    void showAbout() {
        new AlertDialog.Builder(this).setTitle("عن التطبيق")
                .setMessage("تطبيق إدارة الديون والأقساط\nالإصدار 1.5.1\n\nيعمل بالكامل على هاتفك، وبياناتك محفوظة محلياً مع إمكانية النسخ الاحتياطي التلقائي عبر تيليجرام.")
                .setPositiveButton("حسنًا", null).show();
    }

    // ------------------------------------------------------------ backup / restore (unchanged behavior)

    void createBackup() {
        String name = "ديوني_نسخة_" + new SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(new Date()) + ".db";
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/octet-stream");
        i.putExtra(Intent.EXTRA_TITLE, name);
        startActivityForResult(i, REQ_CREATE_BACKUP);
    }

    void openBackup() {
        new AlertDialog.Builder(this).setTitle("استعادة نسخة احتياطية")
                .setMessage("سيتم استبدال البيانات الحالية بالبيانات الموجودة في النسخة المختارة. يُنصح بإنشاء نسخة حالية أولاً.")
                .setPositiveButton("اختيار النسخة", (d, w) -> {
                    Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("application/octet-stream");
                    startActivityForResult(i, REQ_OPEN_BACKUP);
                })
                .setNegativeButton("إلغاء", null).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            if (requestCode == REQ_CREATE_BACKUP) {
                BackupManager.exportDatabase(this, uri);
                Toast.makeText(this, "تم إنشاء النسخة الاحتياطية بنجاح", Toast.LENGTH_LONG).show();
            } else if (requestCode == REQ_OPEN_BACKUP) {
                BackupManager.importDatabase(this, uri);
                db = new Db(this);
                TelegramBackup.changed(this);
                Toast.makeText(this, "تمت استعادة البيانات بنجاح", Toast.LENGTH_LONG).show();
                reload();
            }
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("تعذر تنفيذ العملية")
                    .setMessage(e.getMessage() == null ? "حدث خطأ غير معروف" : e.getMessage())
                    .setPositiveButton("حسنًا", null).show();
        }
    }
}
