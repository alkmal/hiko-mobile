package com.codder.ultimate.fake.utils;

import android.content.Context;

import com.codder.ultimate.R;
import com.codder.ultimate.leaderboard.LeaderboardDataRoot;
import com.codder.ultimate.modelclass.UserRoot;
import com.codder.ultimate.utils.Demo_contents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.Comparator;
import java.util.Collections;

public final class FakeLeaderboardFactory {
    private FakeLeaderboardFactory() {}

    public static List<LeaderboardDataRoot.DataItem> makeUsers(Context context, int count) {
        List<UserRoot.User> demoUsers = Demo_contents.getUsers(context,true);
        Random r = new Random();
        List<LeaderboardDataRoot.DataItem> list = new ArrayList<>();

        for (int i = 0; i < Math.min(count, demoUsers.size()); i++) {
            UserRoot.User u = demoUsers.get(i);

            LeaderboardDataRoot.DataItem d = new LeaderboardDataRoot.DataItem();
            d.setId(u.getId());
            d.setName(u.getName());
            d.setImage(String.valueOf(fakeHostImage(i)));

            // “Top Users” → rank by spent diamonds
            d.setTotalSpentDiamond(100_000 + r.nextInt(200_000));
            d.setTotalEarnrCoin(50_000 + r.nextInt(150_000));
            d.setFinalTotalAmount(0);

            list.add(d);
        }

        // Sort: highest spent first
        list.sort(Comparator.comparing(LeaderboardDataRoot.DataItem::getTotalSpentDiamond).reversed());
        return list;
    }

    public static List<LeaderboardDataRoot.DataItem> makeHosts(int count) {
        Random r = new Random();
        List<LeaderboardDataRoot.DataItem> list = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            LeaderboardDataRoot.DataItem d = new LeaderboardDataRoot.DataItem();
            d.setId("host_" + (i + 1));
            d.setName(fakeName(i));

            // Assign from static pool
            d.setImage(String.valueOf(fakeHostImage(i)));

            // “Top Creators” → rank by earned coins
            d.setTotalEarnrCoin(5_000 + r.nextInt(800_000));
            d.setTotalSpentDiamond(0);
            d.setFinalTotalAmount(0);
            list.add(d);
        }

        // Sort: highest earned first
        list.sort(Comparator.comparing(LeaderboardDataRoot.DataItem::getTotalEarnrCoin).reversed());
        return list;
    }

    public static List<LeaderboardDataRoot.DataItem> makeAgencies(int count) {
        Random r = new Random();
        List<LeaderboardDataRoot.DataItem> list = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            LeaderboardDataRoot.DataItem d = new LeaderboardDataRoot.DataItem();
            d.setId("agency_" + (i + 1));

            LeaderboardDataRoot.Agency agency = new LeaderboardDataRoot.Agency();
            agency.setName(fakeName(i));

            // Assign from static pool
            agency.setImage(String.valueOf(fakeHostImage(i)));
            d.setAgency(agency);

            // “Top Agen” → rank by finalTotalAmount
            d.setFinalTotalAmount(50_000 + r.nextInt(2_000_000));
            d.setTotalEarnrCoin(0);
            d.setTotalSpentDiamond(0);
            list.add(d);
        }

        // Sort: highest total amount first
        list.sort(Comparator.comparing(LeaderboardDataRoot.DataItem::getFinalTotalAmount).reversed());
        return list;
    }

    private static int fakeHostImage(int i) {
        int[] pool = {
                R.drawable.s1,
                R.drawable.s2,
                R.drawable.s3,
                R.drawable.s4,
                R.drawable.s5,
                R.drawable.s6,
                R.drawable.s7,
                R.drawable.s8,
                R.drawable.s9,
                R.drawable.s10,
                R.drawable.s11,
                R.drawable.s12,
                R.drawable.s13,
                R.drawable.s14,
                R.drawable.s15,
                R.drawable.s16,
                R.drawable.s17,
                R.drawable.s18,
                R.drawable.s19,
                R.drawable.s20,
                R.drawable.s21,
                R.drawable.s22,
                R.drawable.s23,
                R.drawable.s24,
                R.drawable.s25,
                R.drawable.s26,
                R.drawable.s27,
                R.drawable.s28,
                R.drawable.s29,
                R.drawable.s30
        };
        return pool[i % pool.length];
    }

    private static String fakeName(int i) {
        String[] pool = {
                // Indian
                "Riya", "Shruti", "Kavya", "Anaya", "Simran", "Dhruvi",
                // Western
                "Emma", "Olivia", "Sophia", "Amelia", "Charlotte", "Grace",
                // Spanish / Latin
                "Camila", "Valentina", "Lucia", "Gabriela", "Elena", "Isla",
                // Arabic
                "Layla", "Zara", "Aaliyah", "Noor", "Yasmin", "Salma",
                // Japanese / Korean
                "Sakura", "Hana", "Emi", "Mika", "Jiwoo", "Aya"
        };
        return pool[i % pool.length];
    }
}


