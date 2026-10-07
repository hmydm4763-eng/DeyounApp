package com.example.debtmanager;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Detailed, expandable user guide. */
public class GuideActivity extends Activity {
    private static final Object[][] SECTIONS = {
            {R.drawable.ic_trending_up, Ui.INDIGO, "البدء السريع",
                    "1) من تبويب «العملاء» اضغط «إضافة عميل» وأدخل الاسم ورقم الهاتف.\n"
                            + "2) اضغط على العميل لفتح ملفه، ثم «إضافة سلعة جديدة» وأدخل السعر الأصلي وسعر التقسيط وعدد الأقساط وتاريخ أول قسط.\n"
                            + "3) عند استلام مبلغ من العميل اضغط «تسجيل دفعة».\n"
                            + "4) تابع تبويب «التنبيهات» لمعرفة من حان موعد قسطه أو تأخر.\n"
                            + "5) فعّل «النسخ التلقائي» من تبويب «المزيد» لحماية بياناتك من الضياع."},
            {R.drawable.ic_home, Ui.BLUE, "الشاشة الرئيسية",
                    "تعرض ملخصاً سريعاً لوضعك المالي:\n"
                            + "• البطاقة البنفسجية الكبيرة: إجمالي المتبقي لدى العملاء، وشريط يبيّن نسبة ما تم تحصيله، والمبلغ المستلم اليوم، والأرباح المتوقعة.\n"
                            + "• البطاقات الملوّنة: عدد الأقساط المتأخرة، والمستحقة اليوم، وعدد العملاء، والأقساط القادمة خلال 7 أيام. اضغط أي بطاقة للانتقال إلى القسم المناسب.\n"
                            + "• قسم «تنبيهات الأقساط»: أهم الأقساط التي تحتاج متابعة.\n"
                            + "• أيقونة الجرس أعلى الشاشة تعرض عدد الأقساط المتأخرة والمستحقة اليوم."},
            {R.drawable.ic_people, Ui.PURPLE, "العملاء وجدول الأقساط",
                    "• تبويب «العملاء» يعرض كل العملاء، ولكل عميل: المتبقي عليه، وتاريخ أول قسط مستحق، وتاريخ آخر قسط.\n"
                            + "• استخدم مربع البحث للوصول لعميل بالاسم أو الهاتف، وأزرار التصفية (الكل / متأخرون / قيد السداد / مسددون).\n"
                            + "• عند الضغط على عميل يُفتح ملفه، وفيه لكل سلعة جدول يعرض أول قسط مستحق وآخر قسط مع التاريخ والمبلغ والحالة.\n"
                            + "• اضغط «عرض جدول جميع الأقساط» لرؤية كل الأقساط.\n"
                            + "• ألوان الحالة: مدفوع (أخضر) • متأخر (أحمر) • اليوم (برتقالي) • قادم (أزرق) • جزئي (أصفر)."},
            {R.drawable.ic_cart, Ui.AMBER, "إضافة سلعة وحساب الأقساط",
                    "• السعر الأصلي: ثمن السلعة نقداً (لحساب الربح).\n"
                            + "• سعر التقسيط: المبلغ الكلي الذي سيدفعه العميل على الأقساط.\n"
                            + "• عدد الأقساط: بالأشهر.\n"
                            + "• قيمة القسط: اختيارية، وإذا تركتها فارغة تُحسب تلقائياً (سعر التقسيط ÷ عدد الأقساط).\n"
                            + "• تاريخ أول قسط: اضغط عليه لاختيار التاريخ من التقويم. الأقساط شهرية، فيستحق القسط الأول في هذا التاريخ وكل قسط بعده بعد شهر.\n"
                            + "يمكنك تعديل السلعة أو حذفها في أي وقت من أيقونتي القلم والسلة في بطاقتها."},
            {R.drawable.ic_payments, Ui.GREEN, "تسجيل الدفعات",
                    "• اضغط «تسجيل دفعة» في بطاقة السلعة (أو من بطاقة التنبيه) وأدخل المبلغ. يظهر القسط القادم تلقائياً ويمكنك تغيير المبلغ.\n"
                            + "• تُوزَّع الدفعة على أقدم قسط غير مدفوع أولاً، فإذا زادت عن القسط يُسدَّد الذي يليه.\n"
                            + "• إذا كانت الدفعة أقل من القسط يصبح القسط «جزئي» ويبقى الباقي مطلوباً.\n"
                            + "• أيقونة الساعة في بطاقة السلعة تعرض سجل كل الدفعات."},
            {R.drawable.ic_notifications, Ui.RED, "التنبيهات",
                    "• المتأخرة: أقساط مضى موعدها ولم تُسدَّد.\n"
                            + "• المستحقة اليوم: موعدها اليوم.\n"
                            + "• القريبة: تستحق خلال 7 أيام.\n"
                            + "تظهر كل بطاقة اسم العميل وتاريخ القسط المستحق ومبلغه. ومنها يمكنك تسجيل دفعة أو الاتصال بالعميل أو فتح ملفه مباشرة. وعدد المتأخرة والمستحقة اليوم يظهر على أيقونة التنبيهات في الشريط السفلي."},
            {R.drawable.ic_save, Ui.INDIGO, "النسخ الاحتياطي اليدوي والاستعادة",
                    "• من «المزيد» ← «إنشاء نسخة احتياطية»: يُحفظ ملف نسخة على هاتفك أو في المكان الذي تختاره.\n"
                            + "• «استعادة نسخة احتياطية»: تختار ملف نسخة سابقة فتحلّ بياناته محل البيانات الحالية. أنشئ نسخة من البيانات الحالية قبل الاستعادة."},
            {R.drawable.ic_cloud, Ui.BLUE, "النسخ التلقائي على تيليجرام",
                    "يرسل التطبيق نسخة من بياناتك تلقائياً إلى محادثتك الخاصة في تيليجرام بعد كل تعديل (بحد أقصى مرة كل 5 دقائق).\n"
                            + "الإعداد مرة واحدة من «المزيد» ← «النسخ التلقائي»:\n"
                            + "1) افتح BotFather في تيليجرام وأرسل /newbot ثم انسخ الرمز (Token).\n"
                            + "2) الصق الرمز في التطبيق.\n"
                            + "3) افتح البوت الذي أنشأته واضغط Start.\n"
                            + "4) اضغط «ربط واختبار» وأكّد أن المحادثة محادثتك.\n"
                            + "للاستعادة: نزّل آخر ملف .db من محادثة البوت ثم استخدم «استعادة نسخة احتياطية». لا تشارك رمز البوت مع أحد."},
            {R.drawable.ic_help, Ui.MUTED, "أسئلة شائعة",
                    "• لماذا تظهر أقساط متأخرة لسلعة قديمة؟ لأن تاريخ أول قسط للسلع المضافة في الإصدارات السابقة هو تاريخ إضافتها. افتح تعديل السلعة وحدّد تاريخ أول قسط الصحيح.\n"
                            + "• لماذا لا يظهر قسط في التنبيهات؟ تظهر فقط الأقساط المتأخرة والمستحقة اليوم وخلال 7 أيام.\n"
                            + "• هل تُحفظ بياناتي على الإنترنت؟ لا، تُحفظ على هاتفك فقط، إلا إذا فعّلت النسخ التلقائي فتُرسل نسخة إلى محادثتك في تيليجرام.\n"
                            + "• ماذا أفعل عند تغيير الهاتف؟ ثبّت التطبيق ثم استعد آخر نسخة احتياطية."}
    };

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Ui.BG);
        LinearLayout root = Ui.col(this);
        root.setPadding(Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 28));
        sv.addView(root);
        Ui.window(this, sv);
        setContentView(sv);

        root.addView(Ui.header(this, "دليل الاستخدام", "شرح مفصل لكل أقسام التطبيق",
                Ui.circleButton(this, R.drawable.ic_arrow_forward, v -> finish()), null));
        root.addView(Ui.card(this, "اضغط على أي عنوان لعرض الشرح. كما تجد في كل قسم داخل التطبيق أيقونة (؟) تشرح هذا القسم تحديداً."));
        for (int i = 0; i < SECTIONS.length; i++) {
            Object[] s = SECTIONS[i];
            root.addView(section(((Integer) s[0]).intValue(), ((Integer) s[1]).intValue(), (String) s[2], (String) s[3], i == 0));
        }
    }

    private View section(int icon, int color, String title, String body, boolean open) {
        final LinearLayout b = Ui.box(this);
        b.setClickable(true);
        LinearLayout head = Ui.row(this);
        head.addView(Ui.iconBadge(this, icon, Ui.soft(color), color, 40, 22));
        TextView t = Ui.bold(Ui.txt(this, title, 15, Ui.TEXT));
        t.setPaddingRelative(Ui.dp(this, 12), 0, Ui.dp(this, 8), 0);
        head.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        final ImageView chev = Ui.icon(this, open ? R.drawable.ic_expand_less : R.drawable.ic_expand_more, 24, Ui.MUTED);
        head.addView(chev);
        b.addView(head);

        final TextView text = Ui.txt(this, body, 14, Color.rgb(51, 65, 85));
        text.setLineSpacing(0, 1.35f);
        text.setPadding(Ui.dp(this, 2), Ui.dp(this, 12), Ui.dp(this, 2), Ui.dp(this, 2));
        text.setGravity(android.view.Gravity.RIGHT | android.view.Gravity.TOP);
        text.setVisibility(open ? View.VISIBLE : View.GONE);
        b.addView(text);
        b.setOnClickListener(v -> {
            boolean show = text.getVisibility() != View.VISIBLE;
            text.setVisibility(show ? View.VISIBLE : View.GONE);
            chev.setImageResource(show ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
        });
        return b;
    }
}
