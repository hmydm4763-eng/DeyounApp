package com.example.debtmanager;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;

/**
 * Installment schedule logic. Nothing here is stored: the schedule is calculated from the item
 * (total, installment amount, number of installments, first installment date) and the payments made.
 * Installment k is due k-1 months after the first installment date. Payments settle the oldest installment first.
 */
public final class Sched {
    private Sched() {}

    public static final int PAID = 0, PARTIAL = 1, OVERDUE = 2, TODAY = 3, UPCOMING = 4;
    public static final double EPS = 0.5;
    public static final int HORIZON_DAYS = 7;

    public static class Inst {
        public int no;
        public String due;
        public long dueDay;
        public double amount, paid;
        public int state;

        public double remaining() { return Math.max(0, amount - paid); }
    }

    public static class Item {
        public long id, customerId;
        public String name, customerName, phone, firstDue;
        public double cash, total, each, paid;
        public int term;
        public List<Inst> list = new ArrayList<>();
        public Inst next, last;
        public int overdueCount;
        public double overdueAmount;

        public double remaining() { return Math.max(0, total - paid); }
        public double profit() { return total - cash; }
        public boolean done() { return remaining() <= EPS; }
    }

    public static class Alert {
        public Item item;
        public int state, count, no;
        public String due;
        public long dueDay, days;
        public double amount;
    }

    /** Reads one row of Db.itemsJoined() and builds its schedule. */
    public static Item item(Cursor c, long today) {
        Item it = new Item();
        it.id = c.getLong(c.getColumnIndexOrThrow("id"));
        it.customerId = c.getLong(c.getColumnIndexOrThrow("customer_id"));
        it.name = nz(c.getString(c.getColumnIndexOrThrow("name")));
        it.cash = c.getDouble(c.getColumnIndexOrThrow("cash"));
        it.total = c.getDouble(c.getColumnIndexOrThrow("installment"));
        it.term = c.getInt(c.getColumnIndexOrThrow("term"));
        it.each = c.getDouble(c.getColumnIndexOrThrow("each_amount"));
        it.firstDue = nz(c.getString(c.getColumnIndexOrThrow("start_date")));
        it.customerName = nz(c.getString(c.getColumnIndexOrThrow("cname")));
        it.phone = nz(c.getString(c.getColumnIndexOrThrow("cphone")));
        it.paid = c.getDouble(c.getColumnIndexOrThrow("paid"));
        build(it, today);
        return it;
    }

    static String nz(String s) { return s == null ? "" : s; }

    public static void build(Item it, long today) {
        it.list.clear();
        it.next = null;
        it.last = null;
        it.overdueCount = 0;
        it.overdueAmount = 0;
        if (it.term <= 0 || it.total <= 0) return;
        double each = it.each > 0 ? it.each : it.total / it.term;
        String first = it.firstDue.isEmpty() ? Dates.today() : it.firstDue;
        double prevCum = 0;
        for (int k = 1; k <= Math.min(it.term, 600); k++) {
            double cum = (k == it.term) ? it.total : Math.min(k * each, it.total);
            if (k == 600) cum = it.total;
            double amt = cum - prevCum;
            if (amt <= 0.0001) break;
            Inst in = new Inst();
            in.no = k;
            in.due = Dates.addMonths(first, k - 1);
            in.dueDay = Dates.dayNum(in.due);
            in.amount = amt;
            in.paid = Math.min(amt, Math.max(0, it.paid - prevCum));
            if (in.paid >= amt - EPS) in.state = PAID;
            else if (in.dueDay < today) in.state = OVERDUE;
            else if (in.dueDay == today) in.state = TODAY;
            else if (in.paid > EPS) in.state = PARTIAL;
            else in.state = UPCOMING;
            if (in.state != PAID) {
                if (it.next == null) it.next = in;
                if (in.dueDay < today) it.overdueCount++;
                if (in.dueDay <= today) it.overdueAmount += in.remaining();
            }
            it.list.add(in);
            prevCum = cum;
        }
        if (!it.list.isEmpty()) it.last = it.list.get(it.list.size() - 1);
    }

    /** The alert for an item: overdue, due today, or due within the next 7 days. Null when nothing is near. */
    public static Alert alert(Item it, long today) {
        Inst n = it.next;
        if (n == null) return null;
        long diff = n.dueDay - today;
        Alert a = new Alert();
        a.item = it;
        a.due = n.due;
        a.dueDay = n.dueDay;
        a.no = n.no;
        if (diff < 0) {
            a.state = OVERDUE;
            a.days = -diff;
            a.count = it.overdueCount;
            a.amount = it.overdueAmount;
        } else if (diff == 0) {
            a.state = TODAY;
            a.days = 0;
            a.count = 1;
            a.amount = n.remaining();
        } else if (diff <= HORIZON_DAYS) {
            a.state = UPCOMING;
            a.days = diff;
            a.count = 1;
            a.amount = n.remaining();
        } else {
            return null;
        }
        return a;
    }
}
