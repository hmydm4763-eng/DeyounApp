package com.example.debtmanager;

import android.database.Cursor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One consistent snapshot of everything the screens need (customers, items, schedules, alerts, totals). */
public final class Data {
    private Data() {}

    public static final int C_NONE = 0, C_PAID = 1, C_ACTIVE = 2, C_TODAY = 3, C_OVERDUE = 4;

    public static class Cust {
        public long id;
        public String name = "", phone = "", address = "";
        public List<Sched.Item> items = new ArrayList<>();
        public double total, paid, remaining;
        public int state = C_NONE;
        public Sched.Inst nextDue, lastDue;
        public long overdueDays;
    }

    public static class Snap {
        public long today;
        public List<Cust> customers = new ArrayList<>();
        public List<Sched.Alert> alerts = new ArrayList<>();
        public double total, paid, remaining, profit, receivedToday;
        public int overdueCount, todayCount, soonCount;

        public Cust find(long id) {
            for (Cust c : customers) if (c.id == id) return c;
            return null;
        }
    }

    public static Snap load(Db db) {
        Snap s = new Snap();
        s.today = Dates.todayNum();
        Map<Long, Cust> map = new LinkedHashMap<>();
        Cursor c = db.customers();
        while (c.moveToNext()) {
            Cust k = new Cust();
            k.id = c.getLong(0);
            k.name = Sched.nz(c.getString(1));
            k.phone = Sched.nz(c.getString(2));
            k.address = Sched.nz(c.getString(3));
            map.put(k.id, k);
        }
        c.close();

        Cursor ic = db.itemsJoined();
        while (ic.moveToNext()) {
            Sched.Item it = Sched.item(ic, s.today);
            Cust k = map.get(it.customerId);
            if (k != null) k.items.add(it);
        }
        ic.close();

        for (Cust k : map.values()) {
            for (Sched.Item it : k.items) {
                k.total += it.total;
                k.paid += it.paid;
                k.remaining += it.remaining();
                s.profit += it.profit();
                if (it.next != null && (k.nextDue == null || it.next.dueDay < k.nextDue.dueDay)) k.nextDue = it.next;
                if (it.last != null && (k.lastDue == null || it.last.dueDay > k.lastDue.dueDay)) k.lastDue = it.last;
                Sched.Alert a = Sched.alert(it, s.today);
                if (a != null) {
                    s.alerts.add(a);
                    if (a.state == Sched.OVERDUE) s.overdueCount++;
                    else if (a.state == Sched.TODAY) s.todayCount++;
                    else s.soonCount++;
                }
            }
            if (k.items.isEmpty()) {
                k.state = C_NONE;
            } else if (k.remaining <= Sched.EPS) {
                k.state = C_PAID;
            } else if (k.nextDue != null && k.nextDue.dueDay < s.today) {
                k.state = C_OVERDUE;
                k.overdueDays = s.today - k.nextDue.dueDay;
            } else if (k.nextDue != null && k.nextDue.dueDay == s.today) {
                k.state = C_TODAY;
            } else {
                k.state = C_ACTIVE;
            }
            s.total += k.total;
            s.paid += k.paid;
            s.remaining += k.remaining;
            s.customers.add(k);
        }

        Collections.sort(s.alerts, new Comparator<Sched.Alert>() {
            @Override
            public int compare(Sched.Alert a, Sched.Alert b) {
                if (a.state != b.state) return a.state < b.state ? -1 : 1;
                return a.dueDay < b.dueDay ? -1 : (a.dueDay == b.dueDay ? 0 : 1);
            }
        });
        s.receivedToday = db.receivedOn(Dates.today());
        return s;
    }
}
