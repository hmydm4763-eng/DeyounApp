package com.example.debtmanager;
import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.graphics.Color;import android.view.*;import android.widget.*;
public class MainActivity extends Activity{
 Db db; LinearLayout root,list; TextView summary;
 public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);build();}
 TextView money(String s){TextView t=Ui.card(this,s);t.setTextSize(18);return t;}
 void build(){root=Ui.root(this);ScrollView sv=new ScrollView(this);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);root.addView(Ui.title(this,"إدارة الديون والأقساط"));summary=money("");root.addView(summary);Button add=Ui.btn(this,"＋ إضافة عميل جديد");add.setOnClickListener(v->addCustomer());root.addView(add);root.addView(Ui.label(this,"العملاء"));root.addView(list);sv.addView(root);setContentView(sv);refresh();}
 void refresh(){double total=db.total("installment"),paid=db.paid();summary.setText("إجمالي الديون: "+fmt(total)+" د.ع\nالمسدّد: "+fmt(paid)+" د.ع\nالمتبقي: "+fmt(Math.max(0,total-paid))+" د.ع");list.removeAllViews();Cursor c=db.customers();while(c.moveToNext()){long id=c.getLong(0);String n=c.getString(1);double t=c.getDouble(c.getColumnIndexOrThrow("total")),p=c.getDouble(c.getColumnIndexOrThrow("paid"));TextView card=Ui.card(this,n+"\nإجمالي التقسيط: "+fmt(t)+" د.ع   |   المتبقي: "+fmt(Math.max(0,t-p))+" د.ع");card.setOnClickListener(v->{Intent i=new Intent(this,CustomerActivity.class);i.putExtra("id",id);startActivity(i);});list.addView(card);}c.close();}
 void addCustomer(){LinearLayout l=Ui.root(this);EditText n=Ui.edit(this,"اسم العميل *"),p=Ui.edit(this,"رقم الهاتف"),a=Ui.edit(this,"العنوان");l.addView(Ui.label(this,"بيانات العميل"));l.addView(n);l.addView(p);l.addView(a);new AlertDialog.Builder(this).setTitle("إضافة عميل").setView(l).setPositiveButton("حفظ",(d,w)->{if(n.getText().toString().trim().isEmpty())return;db.addCustomer(n.getText().toString(),p.getText().toString(),a.getText().toString());refresh();}).setNegativeButton("إلغاء",null).show();}
 String fmt(double x){return String.format(java.util.Locale.US,"%,.0f",x);}
 protected void onResume(){super.onResume();if(db!=null)refresh();}
}
