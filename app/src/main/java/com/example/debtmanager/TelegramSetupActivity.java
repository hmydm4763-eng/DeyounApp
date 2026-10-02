package com.example.debtmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** One-time setup screen for automatic Telegram backups. */
public class TelegramSetupActivity extends Activity {
    LinearLayout root;
    EditText tokenEdit;
    TextView status;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Ui.NAVY);
        ScrollView sv = new ScrollView(this);
        root = Ui.root(this);
        sv.addView(root);
        setContentView(sv);
        render();
    }

    int dp(int x) { return Ui.dp(this, x); }

    void add(View v, int h) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, h);
        p.setMargins(0, dp(4), 0, dp(4));
        root.addView(v, p);
    }

    void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_LONG).show(); }

    void setStatus(String s, int color) {
        if (status != null) {
            status.setText(s);
            status.setTextColor(color);
        }
    }

    void render() {
        root.removeAllViews();
        header();
        if (TelegramBackup.isConfigured(this)) renderConfigured(); else renderSetup();
    }

    void header() {
        LinearLayout top = Ui.row(this);
        TextView back = Ui.iconButton(this, "‹");
        back.setTextSize(34);
        back.setOnClickListener(v -> finish());
        top.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout tt = new LinearLayout(this);
        tt.setOrientation(LinearLayout.VERTICAL);
        tt.setGravity(Gravity.RIGHT);
        tt.addView(Ui.title(this, "النسخ التلقائي"));
        tt.addView(Ui.subtitle(this, "حفظ بياناتك على تيليجرام"));
        top.addView(tt, new LinearLayout.LayoutParams(0, dp(58), 1));
        root.addView(top);
    }

    // ------------------------------------------------------------------ setup

    void renderSetup() {
        root.addView(Ui.card(this, "سيرسل التطبيق نسخة من بياناتك تلقائياً إلى محادثتك الخاصة في تيليجرام بعد كل تعديل، فتبقى محفوظة حتى لو ضاع الهاتف.\nالإعداد يتم مرة واحدة فقط (4 خطوات)."));

        root.addView(Ui.card(this, "① افتح BotFather في تيليجرام واضغط Start، ثم أرسل الأمر:\n/newbot\nواختر أي اسم للبوت، ثم اسم مستخدم ينتهي بكلمة bot. سيرسل لك BotFather رسالة فيها رمز طويل (Token)، انسخه."));
        Button open = Ui.btn(this, "فتح BotFather");
        open.setOnClickListener(v -> openLink("https://t.me/BotFather"));
        add(open, dp(48));

        root.addView(Ui.card(this, "② الصق الرمز هنا:"));
        tokenEdit = Ui.edit(this, "123456789:ABC...");
        tokenEdit.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        tokenEdit.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        add(tokenEdit, dp(52));
        Button paste = Ui.outlineBtn(this, "📋  لصق من الحافظة", Ui.BLUE);
        paste.setOnClickListener(v -> pasteToken());
        add(paste, dp(46));

        root.addView(Ui.card(this, "③ اضغط الزر التالي لفتح البوت الذي أنشأته، ثم اضغط Start (أو أرسل له أي رسالة مثل: hi)."));
        Button bot = Ui.btn(this, "فتح البوت الخاص بي");
        bot.setOnClickListener(v -> openMyBot());
        add(bot, dp(48));

        root.addView(Ui.card(this, "④ بعد إرسال الرسالة للبوت ارجع إلى هنا واضغط:"));
        Button link = Ui.btn(this, "ربط واختبار");
        link.setBackground(Ui.bg(Ui.GREEN, Ui.GREEN, 14));
        link.setOnClickListener(v -> linkNow());
        add(link, dp(52));

        status = Ui.txt(this, "", 14, Ui.MUTED);
        status.setPadding(dp(4), dp(8), dp(4), dp(8));
        root.addView(status);

        root.addView(Ui.card(this, "🔒 الرمز يُحفظ داخل التطبيق فقط، لا تشاركه مع أحد. البوت خاص بك ولا يرسل إلا إلى محادثتك."));
    }

    void openLink(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            toast("تعذر فتح الرابط، افتح تيليجرام يدوياً");
        }
    }

    void pasteToken() {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData cd = cm == null ? null : cm.getPrimaryClip();
            if (cd != null && cd.getItemCount() > 0) {
                CharSequence t = cd.getItemAt(0).coerceToText(this);
                tokenEdit.setText(TelegramBackup.extractToken(t == null ? "" : t.toString()));
                return;
            }
        } catch (Exception ignored) {}
        toast("لا يوجد نص منسوخ");
    }

    String token() {
        return TelegramBackup.extractToken(tokenEdit.getText().toString());
    }

    String msg(Exception e) {
        return e.getMessage() == null || e.getMessage().isEmpty() ? "تعذر الاتصال بتيليجرام، تحقق من الإنترنت" : e.getMessage();
    }

    void openMyBot() {
        final String t = token();
        if (t.isEmpty()) {
            setStatus("ألصق الرمز أولاً (الخطوة ②)", Ui.RED);
            return;
        }
        setStatus("جاري التحقق من الرمز...", Ui.MUTED);
        new Thread(() -> {
            try {
                final String u = TelegramBackup.botUsername(t);
                runOnUiThread(() -> {
                    setStatus("تم العثور على البوت @" + u, Ui.GREEN);
                    openLink("https://t.me/" + u);
                });
            } catch (Exception e) {
                final String m = msg(e);
                runOnUiThread(() -> setStatus(m, Ui.RED));
            }
        }).start();
    }

    void linkNow() {
        final String t = token();
        if (t.isEmpty()) {
            setStatus("ألصق الرمز أولاً (الخطوة ②)", Ui.RED);
            return;
        }
        setStatus("جاري البحث عن محادثتك...", Ui.MUTED);
        new Thread(() -> {
            try {
                final String[] chat = TelegramBackup.findChat(t);
                if (chat == null) {
                    runOnUiThread(() -> setStatus("لم أجد رسالة للبوت. افتح البوت في تيليجرام واضغط Start ثم أعد المحاولة.", Ui.RED));
                    return;
                }
                runOnUiThread(() -> confirmChat(t, chat));
            } catch (Exception e) {
                final String m = msg(e);
                runOnUiThread(() -> setStatus(m, Ui.RED));
            }
        }).start();
    }

    void confirmChat(final String t, final String[] chat) {
        setStatus("", Ui.MUTED);
        new AlertDialog.Builder(this)
                .setTitle("تأكيد المحادثة")
                .setMessage("تم العثور على محادثة باسم: " + chat[1] + "\nهل هذه محادثتك أنت؟")
                .setPositiveButton("نعم، هذه محادثتي", (d, w) -> finishLink(t, chat[0]))
                .setNegativeButton("لا", null)
                .show();
    }

    void finishLink(final String t, final String chatId) {
        setStatus("جاري إرسال رسالة الاختبار وأول نسخة...", Ui.MUTED);
        new Thread(() -> {
            try {
                TelegramBackup.save(this, t, chatId);
                TelegramBackup.sendText(t, chatId, "✅ تم ربط النسخ الاحتياطي التلقائي بتطبيق ديوني بنجاح.");
            } catch (Exception e) {
                TelegramBackup.disable(this);
                final String m = msg(e);
                runOnUiThread(() -> setStatus(m, Ui.RED));
                return;
            }
            boolean sent = true;
            try {
                TelegramBackup.uploadNow(this);
            } catch (Exception e) {
                sent = false;
                TelegramBackup.changed(this);
            }
            final boolean ok = sent;
            runOnUiThread(() -> {
                toast(ok ? "تم الربط وإرسال أول نسخة بنجاح" : "تم الربط، وسيتم إرسال النسخة عند توفر الاتصال");
                render();
            });
        }).start();
    }

    // ------------------------------------------------------------- configured

    void renderConfigured() {
        root.addView(Ui.card(this, "✅ النسخ التلقائي مفعّل\nبعد كل تعديل على البيانات تُرسل نسخة جديدة تلقائياً إلى محادثتك مع البوت (بحد أقصى مرة كل 5 دقائق)."));
        status = Ui.txt(this, TelegramBackup.statusDetail(this), 14, Ui.TEXT);
        status.setPadding(dp(4), dp(8), dp(4), dp(8));
        root.addView(status);

        Button now = Ui.btn(this, "إرسال نسخة الآن");
        now.setOnClickListener(v -> sendNow());
        add(now, dp(50));

        root.addView(Ui.card(this, "كيف أستعيد بياناتي؟\n1) افتح محادثة البوت في تيليجرام ونزّل آخر ملف (.db).\n2) في التطبيق: النسخ الاحتياطي ← استعادة نسخة احتياطية، ثم اختر الملف الذي نزّلته."));

        Button off = Ui.outlineBtn(this, "إيقاف النسخ التلقائي", Ui.RED);
        off.setOnClickListener(v -> confirmOff());
        add(off, dp(48));
    }

    void sendNow() {
        setStatus("جاري إرسال النسخة...", Ui.MUTED);
        new Thread(() -> {
            try {
                TelegramBackup.uploadNow(this);
                runOnUiThread(() -> {
                    toast("تم إرسال النسخة بنجاح");
                    render();
                });
            } catch (Exception e) {
                final String m = msg(e);
                runOnUiThread(() -> setStatus(m, Ui.RED));
            }
        }).start();
    }

    void confirmOff() {
        new AlertDialog.Builder(this)
                .setTitle("إيقاف النسخ التلقائي")
                .setMessage("سيتوقف الإرسال التلقائي ويُحذف الرمز من التطبيق. النسخ التي أُرسلت سابقاً تبقى في تيليجرام.")
                .setPositiveButton("إيقاف", (d, w) -> {
                    TelegramBackup.disable(this);
                    render();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }
}
