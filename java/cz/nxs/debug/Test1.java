/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.debug;

import java.io.PrintStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Test1 {
    public Lock lock1 = new ReentrantLock();
    public Lock lock2 = new ReentrantLock();

    public void test() {
        Thread t1 = new Thread(new Runnable(){

            @Override
            public void run() {
                Test1.this.zazpivat("T1: ");
            }
        });
        Thread t2 = new Thread(new Runnable(){

            @Override
            public void run() {
                Test1.this.zazpivat("T2: ");
            }
        });
        t1.start();
        t2.start();
    }

    public void zazpivat(String s) {
        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            if (i != 1073741823) continue;
            System.out.println(s + "JDEME TLESKAT!!");
        }
        this.zatleskat(s);
    }

    public synchronized void zatleskat(String s) {
        System.out.println(s + "TLESKAM!!");
    }

    public static void main(String[] args) {
        boolean result = false;
        int currentPlayers = 50;
        int currentMutants = 3;
        if (currentMutants == 0) {
            result = currentPlayers >= 3;
        } else if (currentMutants == 1) {
            result = currentPlayers >= 2;
        } else if (currentPlayers + currentMutants >= 3) {
            int players = 10;
            boolean mutants = true;
            int countToHaveMutants = (int)Math.floor((double)currentPlayers / (double)players * (double)mutants ? 1 : 0);
            if (countToHaveMutants < 1) {
                countToHaveMutants = 1;
            }
            int toUntransform = 0;
            if (currentMutants > countToHaveMutants) {
                toUntransform = currentMutants - countToHaveMutants;
            }
            System.out.println("toUntransform = " + toUntransform);
            result = true;
        } else {
            result = false;
        }
        System.out.println(result);
    }

}

