package com.example.debtmanager;
import android.graphics.Color;import android.graphics.Typeface;import android.view.*;import android.widget.*;import android.content.*;import android.text.InputType;
public class Ui{
 public static LinearLayout root(Context c){LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(28,24,28,24);l.setBackgroundColor(Color.rgb(245,247,250));return l;}
 public static TextView title(Context c,String s){TextView t=new TextView(c);t.setText(s);t.setTextSize(23);t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setTextColor(Color.rgb(13,71,161));t.setPadding(0,0,0,20);return t;}
 public static TextView label(Context c,String s){TextView t=new TextView(c);t.setText(s);t.setTextSize(16);t.setTextColor(Color.DKGRAY);t.setPadding(0,8,0,8);return t;}
 public static EditText edit(Context c,String hint){EditText e=new EditText(c);e.setHint(hint);e.setTextSize(16);e.setSingleLine(true);e.setPadding(20,8,20,8);e.setBackgroundColor(Color.WHITE);return e;}
 public static Button btn(Context c,String s){Button b=new Button(c);b.setText(s);b.setTextSize(15);return b;}
 public static TextView card(Context c,String s){TextView t=new TextView(c);t.setText(s);t.setTextSize(16);t.setTextColor(Color.rgb(40,40,40));t.setPadding(24,22,24,22);t.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,6,0,6);t.setLayoutParams(p);return t;}
 public static double num(EditText e){try{return Double.parseDouble(e.getText().toString().replace(",",""));}catch(Exception x){return 0;}}
 public static int integer(EditText e){try{return Integer.parseInt(e.getText().toString());}catch(Exception x){return 0;}}
}
