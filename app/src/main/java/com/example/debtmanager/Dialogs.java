package com.example.debtmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Forms shared by the main screens and the customer file. Each one calls {@code done} after saving. */
final class Dialogs {
    private Dialogs() {}

    /** Builds a dialog whose Save button only closes it when {@code ok.run()} returns true. */
    interface Saver {
        boolean save();
    }

    static void form(final Activity a, String title, View content, String okText, final Saver saver) {
        final AlertDialog dlg = new AlertDialog.Builder(a)
                .setTitle(title)
                .setView(Ui.scroll(a, content))
                .setPositiveButton(okText, null)
                .setNegativeButton("إلغاء", null)
                .create();
        dlg.setOnShowListener(x -> dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (saver.save()) dlg.dismiss();
        }));
        dlg.show();
    }

    // ------------------------------------------------------------ customers

    static void addCustomer(final Activity a, final Db db, final Runnable done) {
        LinearLayout l = Ui.dialogBox(a);
        final EditText n = Ui.edit(a, "اسم العميل *");
        final EditText p = Ui.edit(a, "رقم الهاتف");
        final EditText ad = Ui.edit(a, "العنوان");
        p.setInputType(InputType.TYPE_CLASS_PHONE);
        Ui.addField(l, n);
        Ui.addField(l, p);
        Ui.addField(l, ad);
        l.addView(Ui.hint(a, "الاسم مطلوب، أما الهاتف والعنوان فاختياريان ويمكن تعديلهما لاحقاً من ملف العميل."));
        form(a, "إضافة عميل", l, "حفظ", () -> {
            String nn = n.getText().toString().trim();
            if (nn.isEmpty()) {
                Ui.toast(a, "اسم العميل مطلوب");
                return false;
            }
            db.addCustomer(nn, p.getText().toString().trim(), ad.getText().toString().trim());
            done.run();
            return true;
        });
    }

    static void editCustomer(final Activity a, final Db db, final Data.Cust c, final Runnable done) {
        LinearLayout l = Ui.dialogBox(a);
        final EditText n = Ui.edit(a, "اسم العميل *");
        final EditText p = Ui.edit(a, "رقم الهاتف");
        final EditText ad = Ui.edit(a, "العنوان");
        p.setInputType(InputType.TYPE_CLASS_PHONE);
        n.setText(c.name);
        p.setText(c.phone);
        ad.setText(c.address);
        Ui.addField(l, n);
        Ui.addField(l, p);
        Ui.addField(l, ad);
        form(a, "تعديل بيانات العميل", l, "حفظ", () -> {
            String nn = n.getText().toString().trim();
            if (nn.isEmpty()) {
                Ui.toast(a, "اسم العميل مطلوب");
                return false;
            }
            db.updateCustomer(c.id, nn, p.getText().toString().trim(), ad.getText().toString().trim());
            done.run();
            return true;
        });
    }

    static void deleteCustomer(final Activity a, final Db db, final Data.Cust c, final Runnable done) {
        new AlertDialog.Builder(a)
                .setTitle("حذف العميل")
                .setMessage("سيتم حذف «" + c.name + "» مع كل سلعه ودفعاته نهائياً. هل أنت متأكد؟")
                .setPositiveButton("حذف", (d, w) -> {
                    db.deleteCustomer(c.id);
                    done.run();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    // ------------------------------------------------------------ items

    static void itemDialog(final Activity a, final Db db, final long cid, final Sched.Item it, final Runnable done) {
        final boolean editing = it != null;
        LinearLayout l = Ui.dialogBox(a);
        final EditText n = Ui.edit(a, "اسم السلعة *");
        final EditText cv = Ui.numberEdit(a, "السعر الأصلي (د.ع)", true);
        final EditText iv = Ui.numberEdit(a, "سعر التقسيط الإجمالي (د.ع) *", true);
        final EditText tv = Ui.numberEdit(a, "عدد الأقساط (بالأشهر) *", false);
        final EditText ev = Ui.numberEdit(a, "قيمة القسط الشهري (اختياري)", true);
        final String[] date = {editing && !it.firstDue.isEmpty() ? it.firstDue : Dates.addMonths(Dates.today(), 1)};
        if (editing) {
            n.setText(it.name);
            cv.setText(Ui.plain(it.cash));
            iv.setText(Ui.plain(it.total));
            tv.setText(String.valueOf(it.term));
            ev.setText(Ui.plain(it.each));
        }
        Ui.addField(l, n);
        Ui.addField(l, cv);
        Ui.addField(l, iv);
        Ui.addField(l, tv);
        Ui.addField(l, ev);

        TextView lab = Ui.txt(a, "تاريخ أول قسط", 12, Ui.MUTED);
        l.addView(lab);
        LinearLayout dr = Ui.row(a);
        dr.setPadding(Ui.dp(a, 14), Ui.dp(a, 12), Ui.dp(a, 14), Ui.dp(a, 12));
        dr.setBackground(Ui.ripple(Ui.rbg(a, Ui.WHITE, Ui.BORDER, 12)));
        dr.setClickable(true);
        dr.addView(Ui.icon(a, R.drawable.ic_calendar, 20, Ui.INDIGO));
        final TextView dt = Ui.bold(Ui.txt(a, date[0], 15, Ui.TEXT));
        dt.setPaddingRelative(Ui.dp(a, 10), 0, 0, 0);
        dr.addView(dt, new LinearLayout.LayoutParams(0, -2, 1));
        dr.setOnClickListener(v -> {
            int[] p = Dates.parts(date[0]);
            new DatePickerDialog(a, (view, y, m, d) -> {
                date[0] = Dates.fmt(y, m + 1, d);
                dt.setText(date[0]);
            }, p[0], p[1] - 1, p[2]).show();
        });
        Ui.addField(l, dr);
        l.addView(Ui.hint(a, "الأقساط شهرية: يستحق القسط الأول في التاريخ المحدد، وكل قسط بعده بعد شهر من سابقه. إذا تركت قيمة القسط فارغة تُحسب تلقائياً (سعر التقسيط ÷ عدد الأقساط)."));

        form(a, editing ? "تعديل السلعة" : "إضافة سلعة", l, "حفظ", () -> {
            String nn = n.getText().toString().trim();
            double c = Ui.num(cv), i = Ui.num(iv), e = Ui.num(ev);
            int t = Ui.integer(tv);
            if (nn.isEmpty()) {
                Ui.toast(a, "اسم السلعة مطلوب");
                return false;
            }
            if (i <= 0) {
                Ui.toast(a, "أدخل سعر التقسيط الإجمالي");
                return false;
            }
            if (t <= 0) {
                Ui.toast(a, "أدخل عدد الأقساط");
                return false;
            }
            if (e <= 0) e = i / t;
            if (editing) db.updateItem(it.id, nn, c, i, t, e, date[0]);
            else db.addItem(cid, nn, c, i, t, e, date[0]);
            done.run();
            return true;
        });
    }

    static void deleteItem(final Activity a, final Db db, final Sched.Item it, final Runnable done) {
        new AlertDialog.Builder(a)
                .setTitle("حذف السلعة")
                .setMessage("سيتم حذف «" + it.name + "» وجميع دفعاتها نهائياً. هل أنت متأكد؟")
                .setPositiveButton("حذف", (d, w) -> {
                    db.deleteItem(it.id);
                    done.run();
                })
                .setNegativeButton("إلغاء", null)
                .show();
    }

    // ------------------------------------------------------------ payments

    static void payment(final Activity a, final Db db, final Sched.Item it, final Runnable done) {
        LinearLayout l = Ui.dialogBox(a);
        StringBuilder sb = new StringBuilder();
        sb.append("السلعة: ").append(it.name).append("\nالمتبقي: ").append(Ui.money(it.remaining()));
        if (it.next != null) {
            sb.append("\nالقسط القادم: رقم ").append(it.next.no).append(" بتاريخ ").append(it.next.due)
                    .append(" (").append(Ui.money(it.next.remaining())).append(")");
        }
        TextView info = Ui.txt(a, sb.toString(), 13, Ui.TEXT);
        info.setLineSpacing(0, 1.3f);
        info.setBackground(Ui.rbg(a, Ui.SOFT, Ui.TRANSPARENT_COLOR, 12));
        info.setPadding(Ui.dp(a, 12), Ui.dp(a, 10), Ui.dp(a, 12), Ui.dp(a, 10));
        l.addView(info);
        final EditText amount = Ui.numberEdit(a, "مبلغ الدفعة (د.ع) *", true);
        final EditText note = Ui.edit(a, "ملاحظة (اختياري)");
        if (it.next != null) amount.setText(Ui.plain(Math.round(it.next.remaining())));
        Ui.addField(l, amount);
        Ui.addField(l, note);
        l.addView(Ui.hint(a, "تُسدَّد الدفعة على أقدم قسط غير مدفوع أولاً، وإذا كانت أكبر من القسط فيُسدَّد التالي وهكذا."));
        form(a, "تسجيل دفعة", l, "حفظ الدفعة", () -> {
            double v = Ui.num(amount);
            if (v <= 0) {
                Ui.toast(a, "أدخل مبلغاً صحيحاً");
                return false;
            }
            db.addPayment(it.id, v, Dates.today(), note.getText().toString().trim());
            Ui.toast(a, "تم تسجيل الدفعة");
            done.run();
            return true;
        });
    }

    static void history(final Activity a, final Db db, final Sched.Item it) {
        Cursor c = db.getReadableDatabase().rawQuery("SELECT date,amount,note FROM payments WHERE item_id=? ORDER BY id DESC", new String[]{"" + it.id});
        StringBuilder sb = new StringBuilder();
        while (c.moveToNext()) {
            sb.append(c.getString(0)).append("  —  ").append(Ui.money(c.getDouble(1)));
            String note = c.getString(2);
            if (note != null && !note.isEmpty()) sb.append("  (").append(note).append(")");
            sb.append("\n");
        }
        c.close();
        new AlertDialog.Builder(a)
                .setTitle("سجل دفعات: " + it.name)
                .setMessage(sb.length() == 0 ? "لا توجد دفعات مسجلة بعد." : sb.toString())
                .setPositiveButton("إغلاق", null)
                .show();
    }
}
