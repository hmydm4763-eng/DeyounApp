package com.example.debtmanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

public class Ui {
    public static final int NAVY=Color.rgb(18,53,91), BLUE=Color.rgb(37,99,235), BLUE2=Color.rgb(30,64,175);
    public static final int BG=Color.rgb(246,248,252), TEXT=Color.rgb(31,41,55), MUTED=Color.rgb(100,116,139);
    public static final int GREEN=Color.rgb(16,185,129), RED=Color.rgb(239,68,68), AMBER=Color.rgb(245,158,11);

    public static LinearLayout root(Context c){
        LinearLayout l=new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(18,16,18,20);
        l.setBackgroundColor(BG); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l;
    }
    public static LinearLayout row(Context c){ LinearLayout l=new LinearLayout(c); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }
    public static TextView title(Context c,String s){ TextView t=txt(c,s,23,TEXT); t.setTypeface(Typeface.DEFAULT,Typeface.BOLD); t.setGravity(Gravity.RIGHT); t.setPadding(0,8,0,4); return t; }
    public static TextView subtitle(Context c,String s){ TextView t=txt(c,s,13,MUTED); t.setGravity(Gravity.RIGHT); t.setPadding(0,0,0,10); return t; }
    public static TextView txt(Context c,String s,float size,int color){ TextView t=new TextView(c); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setFontFeatureSettings("kern"); t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return t; }
    public static TextView label(Context c,String s){ return txt(c,s,14,MUTED); }
    public static EditText edit(Context c,String hint){ EditText e=new EditText(c); e.setHint(hint); e.setTextSize(15); e.setSingleLine(true); e.setPadding(16,4,16,4); e.setTextColor(TEXT); e.setHintTextColor(Color.rgb(148,163,184)); e.setBackground(bg(Color.WHITE,Color.rgb(226,232,240),14)); e.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return e; }
    public static Button btn(Context c,String s){ Button b=new Button(c); b.setText(s); b.setTextSize(14); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setPadding(18,4,18,4); b.setBackground(bg(BLUE,BLUE,14)); return b; }
    public static TextView card(Context c,String s){ TextView t=txt(c,s,15,TEXT); t.setPadding(18,16,18,16); t.setBackground(bg(Color.WHITE,Color.rgb(226,232,240),16)); margin(t,0,6,0,6); return t; }
    public static LinearLayout panel(Context c){ LinearLayout l=row(c); l.setPadding(14,14,14,14); l.setBackground(bg(Color.WHITE,Color.rgb(226,232,240),16)); margin(l,0,6,0,6); return l; }
    public static TextView chip(Context c,String s,int bgColor,int fg){ TextView t=txt(c,s,12,fg); t.setGravity(Gravity.CENTER); t.setPadding(12,5,12,5); t.setBackground(bg(bgColor,bgColor,30)); return t; }
    public static LinearLayout stat(Context c,String title,String value,int iconColor){
        LinearLayout box=new LinearLayout(c); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(14,12,14,12); box.setBackground(bg(Color.WHITE,Color.rgb(226,232,240),16));
        TextView a=txt(c,title,12,MUTED), v=txt(c,value,18,TEXT); v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); a.setGravity(Gravity.RIGHT);v.setGravity(Gravity.RIGHT); box.addView(a);box.addView(v);box.setGravity(Gravity.RIGHT); margin(box,4,4,4,4); return box;
    }
    public static void margin(View v,int l,int t,int r,int b){ if(v.getLayoutParams()==null) v.setLayoutParams(new LinearLayout.LayoutParams(-1,-2)); LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)v.getLayoutParams(); p.setMargins(l,t,r,b); v.setLayoutParams(p); }
    public static GradientDrawable bg(int fill,int stroke,int radius){ GradientDrawable g=new GradientDrawable();g.setColor(fill);g.setCornerRadius(radius);g.setStroke(1,stroke);return g; }
    public static double num(EditText e){try{return Double.parseDouble(e.getText().toString().replace(",",""));}catch(Exception x){return 0;}}
    public static int integer(EditText e){try{return Integer.parseInt(e.getText().toString().trim());}catch(Exception x){return 0;}}
}
