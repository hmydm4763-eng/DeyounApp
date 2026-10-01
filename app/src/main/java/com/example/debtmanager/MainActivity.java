package com.example.debtmanager;

import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.graphics.Color;import android.view.*;import android.widget.*;import android.net.Uri;import java.util.*;

public class MainActivity extends Activity{
 Db db; LinearLayout list; EditText search; TextView debtV,paidV,remainV,profitV; static final int REQ_CREATE_BACKUP=1001, REQ_OPEN_BACKUP=1002;
 public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);build();}
 String fmt(double x){return String.format(Locale.US,"%,.0f",x);}
 void build(){
  ScrollView sv=new ScrollView(this); LinearLayout root=Ui.root(this);sv.addView(root);setContentView(sv);
  LinearLayout header=Ui.row(this); LinearLayout ht=new LinearLayout(this);ht.setOrientation(LinearLayout.VERTICAL);ht.addView(Ui.title(this,"الرئيسية"));ht.addView(Ui.subtitle(this,"إدارة الديون والأقساط"));header.addView(ht,new LinearLayout.LayoutParams(0,-2,1));TextView bell=Ui.txt(this,"🔔",22,Ui.NAVY);bell.setGravity(Gravity.CENTER);header.addView(bell,new LinearLayout.LayoutParams(48,58));root.addView(header);
  LinearLayout r1=Ui.row(this),r2=Ui.row(this);LinearLayout a=Ui.stat(this,"إجمالي الديون","0 د.ع",Ui.BLUE),b=Ui.stat(this,"المبلغ المسدد","0 د.ع",Ui.GREEN),c=Ui.stat(this,"المبلغ المتبقي","0 د.ع",Ui.RED),d=Ui.stat(this,"إجمالي الأرباح","0 د.ع",Ui.AMBER);debtV=(TextView)a.getChildAt(1);paidV=(TextView)b.getChildAt(1);remainV=(TextView)c.getChildAt(1);profitV=(TextView)d.getChildAt(1);r1.addView(a,new LinearLayout.LayoutParams(0,-2,1));r1.addView(b,new LinearLayout.LayoutParams(0,-2,1));r2.addView(c,new LinearLayout.LayoutParams(0,-2,1));r2.addView(d,new LinearLayout.LayoutParams(0,-2,1));root.addView(r1);root.addView(r2);
  root.addView(Ui.card(this,"📅  الأقساط المستحقة هذا الشهر\nيمكن إضافة مواعيد الاستحقاق لكل عقد في النسخة القادمة."));
  LinearLayout backupBar=Ui.row(this);
  Button export=Ui.btn(this,"⬇ نسخة احتياطية"); export.setOnClickListener(v->createBackup());
  Button restore=Ui.btn(this,"⬆ استعادة"); restore.setOnClickListener(v->openBackup());
  backupBar.addView(export,new LinearLayout.LayoutParams(0,52,1)); backupBar.addView(restore,new LinearLayout.LayoutParams(0,52,1)); root.addView(backupBar);
  root.addView(Ui.card(this,"💾  النسخة الاحتياطية تحفظ جميع العملاء والسلع والدفعات في ملف مستقل يمكنك نقله إلى هاتف آخر."));
  LinearLayout bar=Ui.row(this);TextView ct=Ui.title(this,"العملاء");ct.setTextSize(19);bar.addView(ct,new LinearLayout.LayoutParams(0,-2,1));Button add=Ui.btn(this,"＋ إضافة عميل");add.setOnClickListener(v->addCustomer());bar.addView(add,new LinearLayout.LayoutParams(-2,52));root.addView(bar);
  search=Ui.edit(this,"🔎  البحث بالاسم أو رقم الهاتف...");search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){refreshList(s.toString());}public void afterTextChanged(android.text.Editable e){}});root.addView(search);list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);root.addView(list);refresh();
 }
 LinearLayout customerCard(long id,String name,String phone,double total,double paid){double rem=Math.max(0,total-paid);LinearLayout box=Ui.panel(this);LinearLayout main=new LinearLayout(this);main.setOrientation(LinearLayout.VERTICAL);TextView n=Ui.txt(this,name,16,Ui.TEXT);n.setTypeface(null,1);main.addView(n);main.addView(Ui.txt(this,phone==null?"":phone,12,Ui.MUTED));TextView val=Ui.txt(this,"المتبقي: "+fmt(rem)+" د.ع",14,rem<=0?Ui.GREEN:Ui.TEXT);val.setTypeface(null,1);main.addView(val);box.addView(main,new LinearLayout.LayoutParams(0,-2,1));TextView chip=Ui.chip(this,rem<=0?"مسدد بالكامل":"قيد السداد",rem<=0?Color.rgb(220,252,231):Color.rgb(219,234,254),rem<=0?Ui.GREEN:Ui.BLUE);box.addView(chip,new LinearLayout.LayoutParams(-2,36));box.setOnClickListener(v->{Intent i=new Intent(this,CustomerActivity.class);i.putExtra("id",id);startActivity(i);});return box;}
 void refresh(){double total=db.total("installment"),paid=db.paid();debtV.setText(fmt(total)+" د.ع");paidV.setText(fmt(paid)+" د.ع");remainV.setText(fmt(Math.max(0,total-paid))+" د.ع");profitV.setText(fmt(db.profit())+" د.ع");refreshList(search==null?"":search.getText().toString());}
 void refreshList(String q){if(list==null)return;list.removeAllViews();Cursor c=db.customers();String needle=q==null?"":q.trim().toLowerCase(Locale.ROOT);while(c.moveToNext()){long id=c.getLong(0);String n=c.getString(1),phone=c.getString(2);if(!needle.isEmpty()&&!n.toLowerCase(Locale.ROOT).contains(needle)&&!(phone==null?"":phone).contains(needle))continue;list.addView(customerCard(id,n,phone,c.getDouble(c.getColumnIndexOrThrow("total")),c.getDouble(c.getColumnIndexOrThrow("paid"))));}c.close();}
 void addCustomer(){LinearLayout l=Ui.root(this);EditText n=Ui.edit(this,"اسم العميل *"),p=Ui.edit(this,"رقم الهاتف"),a=Ui.edit(this,"العنوان");l.addView(n);l.addView(p);l.addView(a);new AlertDialog.Builder(this).setTitle("إضافة عميل").setView(l).setPositiveButton("حفظ",(d,w)->{if(!n.getText().toString().trim().isEmpty()){db.addCustomer(n.getText().toString().trim(),p.getText().toString().trim(),a.getText().toString().trim());refresh();}}).setNegativeButton("إلغاء",null).show();}
 void createBackup(){
  String name="ديوني_نسخة_"+new java.text.SimpleDateFormat("yyyy-MM-dd_HHmm",Locale.US).format(new Date())+".db";
  Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("application/octet-stream"); i.putExtra(Intent.EXTRA_TITLE,name); startActivityForResult(i,REQ_CREATE_BACKUP);
 }
 void openBackup(){
  new AlertDialog.Builder(this).setTitle("استعادة نسخة احتياطية").setMessage("سيتم استبدال البيانات الحالية بالبيانات الموجودة في النسخة المختارة. يُنصح بإنشاء نسخة احتياطية حالية أولاً. هل تريد المتابعة؟").setPositiveButton("اختيار النسخة",(d,w)->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/octet-stream");startActivityForResult(i,REQ_OPEN_BACKUP);}).setNegativeButton("إلغاء",null).show();
 }
 @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();try{if(requestCode==REQ_CREATE_BACKUP){BackupManager.exportDatabase(this,uri);Toast.makeText(this,"تم إنشاء النسخة الاحتياطية بنجاح",Toast.LENGTH_LONG).show();}else if(requestCode==REQ_OPEN_BACKUP){BackupManager.importDatabase(this,uri);db=new Db(this);Toast.makeText(this,"تمت استعادة البيانات بنجاح",Toast.LENGTH_LONG).show();refresh();}}catch(Exception e){new AlertDialog.Builder(this).setTitle("تعذر تنفيذ العملية").setMessage(e.getMessage()==null?"حدث خطأ غير معروف":e.getMessage()).setPositiveButton("حسنًا",null).show();}}

 protected void onResume(){super.onResume();if(db!=null&&debtV!=null)refresh();}
}
