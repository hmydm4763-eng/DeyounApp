package com.example.debtmanager;

import android.content.*;import android.database.*;import android.database.sqlite.*;import java.util.*;

public class Db extends SQLiteOpenHelper {
 public Db(Context c){super(c,"debts.db",null,1);}
 public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE customers(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT,address TEXT)");d.execSQL("CREATE TABLE items(id INTEGER PRIMARY KEY AUTOINCREMENT,customer_id INTEGER,name TEXT, cash REAL, installment REAL, term INTEGER, installment_amount REAL, start_date TEXT)");d.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,item_id INTEGER,amount REAL,date TEXT,note TEXT)");}
 public void onUpgrade(SQLiteDatabase d,int o,int n){d.execSQL("DROP TABLE IF EXISTS payments");d.execSQL("DROP TABLE IF EXISTS items");d.execSQL("DROP TABLE IF EXISTS customers");onCreate(d);}
 public long addCustomer(String n,String p,String a){ContentValues v=new ContentValues();v.put("name",n);v.put("phone",p);v.put("address",a);return getWritableDatabase().insert("customers",null,v);}
 public long addItem(long cid,String n,double cash,double inst,int term,double each,String date){ContentValues v=new ContentValues();v.put("customer_id",cid);v.put("name",n);v.put("cash",cash);v.put("installment",inst);v.put("term",term);v.put("installment_amount",each);v.put("start_date",date);return getWritableDatabase().insert("items",null,v);}
 public long addPayment(long iid,double amount,String date,String note){ContentValues v=new ContentValues();v.put("item_id",iid);v.put("amount",amount);v.put("date",date);v.put("note",note);return getWritableDatabase().insert("payments",null,v);}
 public Cursor customers(){return getReadableDatabase().rawQuery("SELECT c.*, COALESCE((SELECT SUM(i.installment) FROM items i WHERE i.customer_id=c.id),0) total, COALESCE((SELECT SUM(p.amount) FROM payments p JOIN items i ON p.item_id=i.id WHERE i.customer_id=c.id),0) paid FROM customers c ORDER BY c.name",null);}
 public Cursor customer(long id){return getReadableDatabase().rawQuery("SELECT * FROM customers WHERE id=?",new String[]{""+id});}
 public Cursor items(long cid){return getReadableDatabase().rawQuery("SELECT i.*, COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.item_id=i.id),0) paid FROM items i WHERE customer_id=? ORDER BY id DESC",new String[]{""+cid});}
 public Cursor item(long id){return getReadableDatabase().rawQuery("SELECT * FROM items WHERE id=?",new String[]{""+id});}
 public double total(String col){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM("+col+"),0) FROM items",null);c.moveToFirst();double x=c.getDouble(0);c.close();return x;}
 public double paid(){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM payments",null);c.moveToFirst();double x=c.getDouble(0);c.close();return x;}
}
