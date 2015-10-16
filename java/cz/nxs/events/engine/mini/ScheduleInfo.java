/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.mini;

import cz.nxs.events.engine.base.EventType;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class ScheduleInfo {
    private Map<Integer, RunTime> _times = new FastMap();
    private int currentRunTimeUsed = 0;
    private boolean defaultTimeUsed;

    public ScheduleInfo(EventType type, String modeName) {
        RunTime defaultTime = this.addTime();
        for (Day d : Day.values()) {
            defaultTime.addDay(d.prefix);
        }
        this.defaultTimeUsed = true;
    }

    public String decrypt() {
        TextBuilder tb = new TextBuilder();
        for (RunTime time : this._times.values()) {
            tb.append(time.from + "-" + time.to + "_" + time.getDaysString(false) + ";");
        }
        String result = tb.toString();
        if (result.length() > 0) {
            return result.substring(0, result.length() - 1);
        }
        return result;
    }

    public void encrypt(String data) {
        if (data.length() == 0) {
            return;
        }
        try {
            String[] runtimes;
            for (String runtime : runtimes = data.split(";")) {
                String hours = runtime.split("_")[0];
                String daysString = runtime.split("_")[1];
                String from = hours.split("-")[0];
                String to = hours.split("-")[1];
                String[] days = daysString.split(",");
                RunTime time = this.addTime();
                time.from = from;
                time.to = to;
                if (days.length == 1 && days[0].equals("AllDays")) {
                    for (String d : Day.values()) {
                        time.addDay(d.prefix);
                    }
                    continue;
                }
                for (String s : days) {
                    time.addDay(s);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getNextStart(boolean test) {
        if (this._times.size() == 0) {
            return -1;
        }
        long lowestValue = Long.MAX_VALUE;
        long temp = 0;
        for (Map.Entry<Integer, RunTime> time : this._times.entrySet()) {
            temp = time.getValue().getNext(true, test);
            if (temp == -1 || temp >= lowestValue) continue;
            lowestValue = temp;
            this.currentRunTimeUsed = time.getKey();
        }
        return lowestValue;
    }

    public long getEnd(boolean test) {
        if (this._times.size() == 0) {
            return -1;
        }
        return this._times.get(this.currentRunTimeUsed).getNext(false, test);
    }

    public boolean isNonstopRun() {
        if (this._times.size() == 1) {
            for (RunTime time : this._times.values()) {
                if (time.days.size() != Day.values().length || !time.from.equals("00:00") && !time.from.equals("0:00") || !time.to.equals("23:59")) continue;
                return true;
            }
        }
        return false;
    }

    public RunTime addTime() {
        if (this.defaultTimeUsed) {
            this._times.clear();
            this.currentRunTimeUsed = 0;
            this.defaultTimeUsed = false;
        }
        int lastId = 0;
        Iterator<Integer> i$ = this._times.keySet().iterator();
        while (i$.hasNext()) {
            int id = i$.next();
            if (id <= lastId) continue;
            lastId = id;
        }
        RunTime time = new RunTime(++lastId);
        this._times.put(lastId, time);
        return time;
    }

    public Map<Integer, RunTime> getTimes() {
        return this._times;
    }

    public static void main(String[] args) {
        ScheduleInfo info = new ScheduleInfo(EventType.Classic_1v1, "HeyTest");
        RunTime time1 = info.addTime();
        time1.from = "14:00";
        time1.to = "20:00";
        time1.addDay("m");
        time1.addDay("tu");
        time1.addDay("su");
        RunTime time2 = info.addTime();
        time2.from = "20:30";
        time2.to = "21:00";
        time2.addDay("m");
        RunTime time3 = info.addTime();
        time3.from = "14:00";
        time3.to = "14:30";
        time3.addDay("su");
        long l = info.getNextStart(false);
        System.out.println("Starting in " + l);
        System.out.println("Days: " + l / 86400000);
        System.out.println("Hours: " + l / 3600000);
        System.out.println("Minutes: " + l / 60000);
    }

    public static enum Day {
        Monday("m", 2, "Monday"),
        Tuesday("tu", 3, "Tuesday"),
        Wednesday("w", 4, "Wednesday"),
        Thursday("th", 5, "Thursday"),
        Friday("f", 6, "Friday"),
        Saturday("sa", 7, "Saturday"),
        Sunday("su", 1, "Sunday");
        
        public String prefix;
        int dayId;
        public String fullName;

        private Day(String s, int id, String fullName) {
            this.prefix = s;
            this.dayId = id;
            this.fullName = fullName;
        }

        public static Day getDayByName(String name) {
            for (Day d : Day.values()) {
                if (!d.fullName.equalsIgnoreCase(name)) continue;
                return d;
            }
            return null;
        }

        public static Day getDay(String prefix) {
            for (Day d : Day.values()) {
                if (!d.prefix.equals(prefix)) continue;
                return d;
            }
            return null;
        }
    }

    public class RunTime {
        public int id;
        public List<Day> days;
        public String from;
        public String to;

        public RunTime(int id) {
            this.days = new ArrayList<Day>();
            this.id = id;
            this.from = "00:00";
            this.to = "23:59";
        }

        private Calendar getNextRun(boolean start) {
            if (this.days.isEmpty()) {
                return null;
            }
            Calendar current = Calendar.getInstance();
            FastList times = new FastList();
            for (Day day : this.days) {
                Calendar time = Calendar.getInstance();
                time.set(7, day.dayId);
                if (start) {
                    time.set(11, Integer.parseInt(this.from.split(":")[0]));
                    time.set(12, Integer.parseInt(this.from.split(":")[1]));
                } else {
                    time.set(11, Integer.parseInt(this.to.split(":")[0]));
                    time.set(12, Integer.parseInt(this.to.split(":")[1]));
                }
                times.add(time);
            }
            Calendar runTime = null;
            Calendar temp = null;
            for (Calendar time2 : times) {
                if (time2.getTimeInMillis() <= current.getTimeInMillis()) continue;
                if (temp == null) {
                    temp = time2;
                    continue;
                }
                if (time2.getTimeInMillis() - current.getTimeInMillis() >= temp.getTimeInMillis() - current.getTimeInMillis()) continue;
                temp = time2;
            }
            if (temp != null) {
                runTime = temp;
            } else {
                for (Calendar time2 : times) {
                    time2.add(10, 168);
                    if (time2.getTimeInMillis() <= current.getTimeInMillis()) continue;
                    if (temp == null) {
                        temp = time2;
                        continue;
                    }
                    if (time2.getTimeInMillis() - current.getTimeInMillis() >= temp.getTimeInMillis() - current.getTimeInMillis()) continue;
                    temp = time2;
                }
            }
            if (temp == null) {
                System.out.println("No time found!! RunTime ID = " + this.id + ", from - " + this.from + ", to " + this.to);
                return null;
            }
            runTime = temp;
            runTime.set(13, 1);
            return runTime;
        }

        public long getNext(boolean start, boolean test) {
            Calendar currentTime = Calendar.getInstance();
            Calendar runTime = this.getNextRun(start);
            if (runTime == null) {
                return -1;
            }
            if (test) {
                return runTime.getTimeInMillis();
            }
            long delay = runTime.getTimeInMillis() - currentTime.getTimeInMillis();
            if (delay < 0) {
                delay = 0;
            }
            return delay;
        }

        public String getDaysString(boolean html) {
            TextBuilder tb = new TextBuilder();
            int i = 1;
            if (this.days.size() == Day.values().length) {
                if (html) {
                    return "All days";
                }
                return "AllDays";
            }
            for (Day day : this.days) {
                tb.append(html ? day.prefix.toUpperCase() : day.prefix);
                if (i < this.days.size()) {
                    tb.append(",");
                }
                ++i;
            }
            return tb.toString();
        }

        public boolean isActual() {
            Calendar current = Calendar.getInstance();
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            start.set(11, Integer.parseInt(this.from.split(":")[0]));
            start.set(12, Integer.parseInt(this.from.split(":")[1]));
            end.set(11, Integer.parseInt(this.to.split(":")[0]));
            end.set(12, Integer.parseInt(this.to.split(":")[1]));
            if (start.getTimeInMillis() > current.getTimeInMillis() || end.getTimeInMillis() < current.getTimeInMillis()) {
                return false;
            }
            for (Day day : this.days) {
                if (day.dayId != current.get(7)) continue;
                return true;
            }
            return false;
        }

        public void addDay(String prefix) {
            for (Day day : Day.values()) {
                if (!prefix.equalsIgnoreCase(day.prefix)) continue;
                this.days.add(day);
                break;
            }
        }
    }

}

