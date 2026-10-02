package com.example.debtmanager;

import android.app.*;import android.os.*;import android.content.*;import android.database.*;import android.graphics.Color;import android.graphics.drawable.ColorDrawable;import android.net.Uri;import android.view.*;import android.widget.*;import java.text.SimpleDateFormat;import java.util.*;

public class MainActivity extends Activity{
 Db db; TextView autoStatus; LinearLayout list; EditText search; TextView debtV,paidV,remainV,profitV; FrameLayout frame; View drawerShade; LinearLayout drawer; ScrollView contentScroll;
 static final int REQ_CREATE_BACKUP=1001, REQ_OPEN_BACKUP=1002;
 @Override public void onCreate(Bundle b){super.onCreate(b);db=new Db(this);getWindow().setStatusBarColor(Ui.NAVY);build();}
 String fmt(double x){return String.format(Locale.US,"%,.0f",x);}
 int dp(int x){return Ui.dp(this,x);}
 void build(){
  frame=new FrameLayout(this); frame.setBackgroundColor(Ui.BG);
  contentScroll=new ScrollView(this); LinearLayout root=Ui.root(this); contentScroll.addView(root); frame.addView(contentScroll,new FrameLayout.LayoutParams(-1,-1));
  buildHeader(root); buildDashboard(root); buildCustomers(root); buildDrawer(); setContentView(frame); refresh();
 }
 void buildHeader(LinearLayout root){
  LinearLayout header=Ui.row(this); TextView menu=Ui.iconButton(this,"☰"); menu.setOnClickListener(v->toggleDrawer(true));
  header.addView(menu,new LinearLayout.LayoutParams(dp(48),dp(48)));
  LinearLayout ht=new LinearLayout(this);ht.setOrientation(LinearLayout.VERTICAL);ht.setGravity(Gravity.RIGHT);TextView t=Ui.title(this,"الرئيسية");ht.addView(t);ht.addView(Ui.subtitle(this,"إدارة الديون والأقساط"));
  header.addView(ht,new LinearLayout.LayoutParams(0,dp(60),1)); TextView bell=Ui.iconButton(this,"🔔"); header.addView(bell,new LinearLayout.LayoutParams(dp(48),dp(48))); root.addView(header);
 }
 void buildDashboard(LinearLayout root){
  LinearLayout r1=Ui.row(this),r2=Ui.row(this);LinearLayout a=Ui.stat(this,"إجمالي الديون","0 د.ع",Ui.BLUE),b=Ui.stat(this,"المبلغ المسدد","0 د.ع",Ui.GREEN),c=Ui.stat(this,"المبلغ المتبقي","0 د.ع",Ui.RED),d=Ui.stat(this,"إجمالي الأرباح","0 د.ع",Ui.AMBER);
  debtV=(TextView)a.getChildAt(1);paidV=(TextView)b.getChildAt(1);remainV=(TextView)c.getChildAt(1);profitV=(TextView)d.getChildAt(1);r1.addView(a,new LinearLayout.LayoutParams(0,-2,1));r1.addView(b,new LinearLayout.LayoutParams(0,-2,1));r2.addView(c,new LinearLayout.LayoutParams(0,-2,1));r2.addView(d,new LinearLayout.LayoutParams(0,-2,1));root.addView(r1);root.addView(r2);
  LinearLayout due=Ui.panel(this); due.setOrientation(LinearLayout.VERTICAL); TextView dt=Ui.txt(this,"📅  الأقساط والمتابعة",16,Ui.TEXT);dt.setTypeface(null,1);due.addView(dt);due.addView(Ui.txt(this,"راجع العملاء والسلع قيد السداد من قائمة الأقساط.",13,Ui.MUTED));root.addView(due);
  LinearLayout backupBar=Ui.row(this); Button export=Ui.btn(this,"⬇  نسخة احتياطية"),restore=Ui.outlineBtn(this,"⬆  استعادة",Ui.BLUE);export.setOnClickListener(v->createBackup());restore.setOnClickListener(v->openBackup());backupBar.addView(export,new LinearLayout.LayoutParams(0,dp(48),1));backupBar.addView(restore,new LinearLayout.LayoutParams(0,dp(48),1));root.addView(backupBar);
  LinearLayout auto=Ui.panel(this);autoStatus=Ui.txt(this,"",13,Ui.TEXT);auto.addView(autoStatus,new LinearLayout.LayoutParams(0,-2,1));auto.setOnClickListener(v->openTelegramSetup());root.addView(auto);
 }
 void buildCustomers(LinearLayout root){
  LinearLayout bar=Ui.row(this); LinearLayout tt= new LinearLayout(this);tt.setOrientation(LinearLayout.VERTICAL);tt.addView(Ui.title(this,"العملاء"));tt.addView(Ui.subtitle(this,"اضغط على العميل لعرض ملفه الكامل"));bar.addView(tt,new LinearLayout.LayoutParams(0,-2,1));Button add=Ui.btn(this,"＋ إضافة عميل");add.setOnClickListener(v->addCustomer());bar.addView(add,new LinearLayout.LayoutParams(dp(145),dp(48)));root.addView(bar);
  search=Ui.edit(this,"🔎  البحث بالاسم أو رقم الهاتف..."); search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){refreshList(s.toString());}public void afterTextChanged(android.text.Editable e){}});root.addView(search);
  list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);root.addView(list);
 }
 LinearLayout customerCard(long id,String name,String phone,double total,double paid){
  double rem=Math.max(0,total-paid); LinearLayout box=Ui.panel(this); box.setPadding(dp(12),dp(12),dp(12),dp(12));
  TextView avatar=Ui.txt(this,name==null||name.isEmpty()?"؟":name.substring(0,1),19,Color.WHITE);avatar.setGravity(Gravity.CENTER);avatar.setBackground(Ui.bg(Ui.BLUE,Ui.BLUE,60));box.addView(avatar,new LinearLayout.LayoutParams(dp(50),dp(50)));
  LinearLayout main=new LinearLayout(this);main.setOrientation(LinearLayout.VERTICAL);main.setPadding(dp(12),0,dp(8),0); TextView n=Ui.txt(this,name,17,Ui.TEXT);n.setTypeface(null,1);main.addView(n);main.addView(Ui.txt(this,phone==null?"":phone,12,Ui.MUTED));TextView val=Ui.txt(this,"المتبقي  "+fmt(rem)+" د.ع",14,rem<=0?Ui.GREEN:Ui.TEXT);val.setTypeface(null,1);main.addView(val);box.addView(main,new LinearLayout.LayoutParams(0,-2,1));box.addView(Ui.chip(this,rem<=0?"مسدد":"قيد السداد",rem<=0?Color.rgb(220,252,231):Color.rgb(219,234,254),rem<=0?Ui.GREEN:Ui.BLUE));
  box.setOnClickListener(v->{Intent i=new Intent(this,CustomerActivity.class);i.putExtra("id",id);startActivity(i);}); return box;
 }
 void refresh(){updateAutoStatus();double total=db.total("installment"),paid=db.paid();debtV.setText(fmt(total)+" د.ع");paidV.setText(fmt(paid)+" د.ع");remainV.setText(fmt(Math.max(0,total-paid))+" د.ع");profitV.setText(fmt(db.profit())+" د.ع");refreshList(search==null?"":search.getText().toString());}
 void refreshList(String q){if(list==null)return;list.removeAllViews();Cursor c=db.customers();String needle=q==null?"":q.trim().toLowerCase(Locale.ROOT);int count=0;while(c.moveToNext()){long id=c.getLong(0);String n=c.getString(1),phone=c.getString(2);if(!needle.isEmpty()&&!n.toLowerCase(Locale.ROOT).contains(needle)&&!(phone==null?"":phone).contains(needle))continue;list.addView(customerCard(id,n,phone,c.getDouble(c.getColumnIndexOrThrow("total")),c.getDouble(c.getColumnIndexOrThrow("paid"))));count++;}c.close();if(count==0)list.addView(Ui.card(this,"لا توجد نتائج مطابقة للبحث."));}
 void addCustomer(){LinearLayout l=Ui.root(this);EditText n=Ui.edit(this,"اسم العميل *"),p=Ui.edit(this,"رقم الهاتف"),a=Ui.edit(this,"العنوان");l.addView(n);l.addView(p);l.addView(a);new AlertDialog.Builder(this).setTitle("إضافة عميل").setView(l).setPositiveButton("حفظ",(d,w)->{if(!n.getText().toString().trim().isEmpty()){db.addCustomer(n.getText().toString().trim(),p.getText().toString().trim(),a.getText().toString().trim());refresh();}}).setNegativeButton("إلغاء",null).show();}
 void openTelegramSetup(){startActivity(new Intent(this,TelegramSetupActivity.class));}
 void updateAutoStatus(){if(autoStatus!=null)autoStatus.setText(TelegramBackup.statusLine(this));}
 void buildDrawer(){
  drawerShade=new View(this);drawerShade.setBackgroundColor(0x66000000);drawerShade.setVisibility(View.GONE);drawerShade.setOnClickListener(v->toggleDrawer(false));frame.addView(drawerShade,new FrameLayout.LayoutParams(-1,-1));
  drawer=new LinearLayout(this);drawer.setOrientation(LinearLayout.VERTICAL);drawer.setPadding(dp(18),dp(24),dp(18),dp(20));drawer.setBackgroundColor(Color.WHITE);drawer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(dp(300),-1,Gravity.RIGHT);p.setMargins(0,0,0,0);drawer.setVisibility(View.GONE);frame.addView(drawer,p);fillDrawer();
 }
 void fillDrawer(){
  LinearLayout brand=Ui.panel(this);brand.setOrientation(LinearLayout.VERTICAL);TextView b=Ui.txt(this,"💼  ديوني",22,Ui.NAVY);b.setTypeface(null,1);brand.addView(b);brand.addView(Ui.txt(this,"إدارة الديون والأقساط",13,Ui.MUTED));drawer.addView(brand);
  addMenu("⌂","الرئيسية",()->{toggleDrawer(false);contentScroll.smoothScrollTo(0,0);});
  addMenu("👥","العملاء",()->{toggleDrawer(false);contentScroll.postDelayed(()->contentScroll.smoothScrollTo(0,dp(360)),150);});
  addMenu("🧾","الأقساط",()->{toggleDrawer(false);showInstallments();});
  addMenu("📊","التقارير",()->{toggleDrawer(false);showReports();});
  addMenu("💾","النسخ الاحتياطي",()->{toggleDrawer(false);backupMenu();});
  addMenu("☁","النسخ التلقائي (تيليجرام)",()->{toggleDrawer(false);openTelegramSetup();});
  View line=new View(this);line.setBackgroundColor(Ui.BORDER);drawer.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
  addMenu("ℹ","عن التطبيق",()->{toggleDrawer(false);new AlertDialog.Builder(this).setTitle("ديوني").setMessage("إدارة الديون والأقساط\nنسخة 1.4").setPositiveButton("حسنًا",null).show();});
 }
 void addMenu(String icon,String title,final Runnable action){TextView t=Ui.txt(this,icon+"    "+title,16,Ui.TEXT);t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);t.setPadding(dp(12),0,dp(12),0);t.setBackground(Ui.bg(Color.WHITE,Color.TRANSPARENT,14));t.setOnClickListener(v->action.run());drawer.addView(t,new LinearLayout.LayoutParams(-1,dp(56)));}
 void toggleDrawer(boolean show){drawer.setVisibility(show?View.VISIBLE:View.GONE);drawerShade.setVisibility(show?View.VISIBLE:View.GONE);}
 @Override public void onBackPressed(){if(drawer!=null&&drawer.getVisibility()==View.VISIBLE){toggleDrawer(false);return;}super.onBackPressed();}
 void backupMenu(){new AlertDialog.Builder(this).setTitle("النسخ الاحتياطي").setItems(new String[]{"إنشاء نسخة احتياطية","استعادة نسخة احتياطية","النسخ التلقائي على تيليجرام"},(d,w)->{if(w==0)createBackup();else if(w==1)openBackup();else openTelegramSetup();}).show();}
 void showReports(){new AlertDialog.Builder(this).setTitle("التقارير").setMessage("إجمالي التقسيط: "+debtV.getText()+"\nإجمالي المدفوع: "+paidV.getText()+"\nإجمالي المتبقي: "+remainV.getText()+"\nإجمالي الأرباح: "+profitV.getText()).setPositiveButton("إغلاق",null).show();}
 void showInstallments(){StringBuilder s=new StringBuilder();Cursor c=db.itemsAll();if(!c.moveToFirst())s.append("لا توجد أقساط مسجلة.");else do{s.append(c.getString(c.getColumnIndexOrThrow("customer_name"))).append("\n");s.append(c.getString(c.getColumnIndexOrThrow("item_name"))).append(" — المتبقي ").append(fmt(c.getDouble(c.getColumnIndexOrThrow("remaining")))).append(" د.ع\n\n");}while(c.moveToNext());c.close();new AlertDialog.Builder(this).setTitle("الأقساط الحالية").setMessage(s.toString()).setPositiveButton("إغلاق",null).show();}
 void createBackup(){String name="ديوني_نسخة_"+new SimpleDateFormat("yyyy-MM-dd_HHmm",Locale.US).format(new Date())+".db";Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/octet-stream");i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,REQ_CREATE_BACKUP);}
 void openBackup(){new AlertDialog.Builder(this).setTitle("استعادة نسخة احتياطية").setMessage("سيتم استبدال البيانات الحالية بالبيانات الموجودة في النسخة المختارة. يُنصح بإنشاء نسخة حالية أولاً.").setPositiveButton("اختيار النسخة",(d,w)->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/octet-stream");startActivityForResult(i,REQ_OPEN_BACKUP);}).setNegativeButton("إلغاء",null).show();}
 @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();try{if(requestCode==REQ_CREATE_BACKUP){BackupManager.exportDatabase(this,uri);Toast.makeText(this,"تم إنشاء النسخة الاحتياطية بنجاح",Toast.LENGTH_LONG).show();}else if(requestCode==REQ_OPEN_BACKUP){BackupManager.importDatabase(this,uri);db=new Db(this);TelegramBackup.changed(this);Toast.makeText(this,"تمت استعادة البيانات بنجاح",Toast.LENGTH_LONG).show();refresh();}}catch(Exception e){new AlertDialog.Builder(this).setTitle("تعذر تنفيذ العملية").setMessage(e.getMessage()==null?"حدث خطأ غير معروف":e.getMessage()).setPositiveButton("حسنًا",null).show();}}
 @Override protected void onResume(){super.onResume();TelegramBackup.onAppStart(this);if(db!=null&&debtV!=null)refresh();}
}
