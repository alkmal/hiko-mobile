package com.codder.ultimate.utils;


import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.codder.ultimate.R;
import com.codder.ultimate.fake.utils.FakeGiftRoot;
import com.codder.ultimate.live.model.LiveStramComment;
import com.codder.ultimate.modelclass.UserRoot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Demo_contents {

    public static ArrayList<String> girlsImage = new ArrayList<>(Arrays.asList(
            "https://images.unsplash.com/photo-1581588636584-5c447d2c9d97?q=80&w=1998&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1467632499275-7a693a761056?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8MjZ8fGhvdCUyMGJpa2luaXxlbnwwfHwwfHx8MA%3D%3D",
            "https://images.unsplash.com/photo-1606792109910-340f5e672ccd?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mjh8fGhvdCUyMGJpa2luaXxlbnwwfHwwfHx8MA%3D%3D",
            "https://images.unsplash.com/photo-1520065949650-380765513210?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Njd8fGhvdCUyMGJpa2luaXxlbnwwfHwwfHx8MA%3D%3D",
            "https://images.unsplash.com/photo-1583058905141-deef2de746bb?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=888&q=80",
            "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1600600423621-70c9f4416ae9?q=80&w=688&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?w=500&auto=format&fit=crop&q=60",
            "https://plus.unsplash.com/premium_photo-1669138512601-e3f00b684edc?q=80&w=685&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1687186954188-76f7f4a3d829?q=80&w=686&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&auto=format&fit=crop&q=60",
            "https://plus.unsplash.com/premium_photo-1673792686302-7555a74de717?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1516195851888-6f1a981a862e?q=80&w=705&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1586907835000-f692bbd4c9e0?q=80&w=1922&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1503342217505-b0a15ec3261c?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1541101767792-f9b2b1c4f127?w=500&auto=format&fit=crop&q=60",
            "https://plus.unsplash.com/premium_photo-1669824376679-268d3739acf3?q=80&w=695&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://plus.unsplash.com/premium_photo-1688676796006-bbd1599bbfb6?q=80&w=687&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
            "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?w=500&auto=format&fit=crop&q=60",
            "https://images.unsplash.com/photo-1568739253582-afa48fbcea47?q=80&w=764&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
    ));

    static {
        for (int i = 0; i < girlsImage.size(); i++) {
            girlsImage.set(i, toDirectUnsplash(girlsImage.get(i)));
        }
    }

    public static String[] fallbackImages = {
            "https://plus.unsplash.com/premium_photo-1669824376679-268d3739acf3?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NXx8Z2lybHN8ZW58MHx8MHx8fDA%3D",
            "https://images.unsplash.com/photo-1581588636584-5c447d2c9d97?q=80&w=1998&auto=format&fit=crop&ixlib=rb-4.0.3",
            "https://images.unsplash.com/photo-1467632499275-7a693a761056?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3",
            "https://images.unsplash.com/photo-1606792109910-340f5e672ccd?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3",
            "https://images.unsplash.com/photo-1520065949650-380765513210?w=500&auto=format&fit=crop&q=60&ixlib=rb-4.0.3",
            "https://images.unsplash.com/photo-1583058905141-deef2de746bb?auto=format&fit=crop&w=888&q=80"
    };

    private static String toDirectUnsplash(String url) {
        if (url == null || url.isEmpty()) return url;
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host != null
                    && host.contains("unsplash.com")
                    && !host.contains("images.unsplash.com")) {
                // Expecting /photos/{photoId}[...]
                java.util.List<String> segs = uri.getPathSegments();
                int p = segs.indexOf("photos");
                if (p != -1 && segs.size() > p + 1) {
                    String photoId = segs.get(p + 1);
                    // Use Unsplash Source hotlink
                    return "https://source.unsplash.com/" + photoId + "?w=800&h=800&fit=crop";
                }
            }
        } catch (Exception ignored) { }
        return url;
    }

    public static List<LiveStramComment> getLiveStreamComment(Context context) {
        List<LiveStramComment> liveStreamCommentDummies = new ArrayList<>();

        liveStreamCommentDummies.add(new LiveStramComment("Please stop looking so hot every time.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Your hotness is just beating me everytime.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Give me your mobile number", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Every single part of your body was made according to my spec.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("9975537455 it is my mobile number", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Please stop looking so hot every time.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Looking very very hot\uD83D\uDD25in summer", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Your queenly smiles are what my eyes have been longing to see.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Too hot for me to handle", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Every single part of your body was made according to my spec.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("I drop my cap for you.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Your hotness is just beating me everytime.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Classy shot and awesome background too.", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Hello dear!", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("Give me your mobile number", getUsers(context,true).get(0), false, null, "", "comment", ""));
        liveStreamCommentDummies.add(new LiveStramComment("9975537455 it is my mobile number", getUsers(context,true).get(0), false, null, "", "comment", ""));
        Collections.shuffle(liveStreamCommentDummies);
        return liveStreamCommentDummies;

    }

    public static List<UserRoot.User> getUsers(Context context,boolean isShuffle) {
        List<UserRoot.User> userDummies = new ArrayList<>(Arrays.asList(
                new UserRoot.User(10, "", false, 0, "India", "", false, "female", 10, 0, "", "Not everyone likes me, but not everyone matters.", true, 0, 0, null, "", 0, "null", "", null, "null", "alisha@gmail.com", "", girlsImage.get(0), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Alisha", "56478965", 19, "Alisha", true, "12345678"),
                new UserRoot.User(20, "", false, 0, "USA", "", false, "female", 10, 0, "", "Mess with me? I dare you.", true, 0, 0, null, "", 0, "null", "", null, "null", "amar@gmail.com", "", girlsImage.get(1), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Amar", "14785236", 20, "amar", true, "45678912"),
                new UserRoot.User(40, "", false, 0, "India", "", false, "female", 10, 0, "", "Built different. Think twice.", true, 0, 0, null, "", 0, "null", "", null, "null", "AaliyaMia@gmail.com", "", girlsImage.get(2), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Aaliya Mia", "98745632", 18, "Aaliya Mia", true, "45678912"),
                new UserRoot.User(30, "", false, 0, "UK", "", false, "female", 10, 0, "", "Soft heart. Steel spine.", true, 0, 0, null, "", 0, "null", "", null, "null", "prisha@gmail.com", "", girlsImage.get(3), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Prisha", "89745632", 25, "Prisha", true, "45678912"),
                new UserRoot.User(50, "", false, 0, "GERMANY", "", false, "male", 10, 0, "", "I don’t follow rules. I make my own.", true, 0, 0, null, "", 0, "null", "", null, "null", "DanielDavidson@gmail.com", "", girlsImage.get(4), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Daniel Davidson", "96325874", 23, "Daniel Davidson", true, "45678912"),
                new UserRoot.User(60, "", false, 0, "FRANCE", "", false, "male", 10, 0, "", "Confidence level: Self-made queen.", true, 0, 0, null, "", 0, "null", "", null, "null", "JamesCarter@gmail.com", "", girlsImage.get(5), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "James Carter", "87459685", 23, "James Carter", true, "45678912"),
                new UserRoot.User(70, "", false, 0, "India", "", false, "male", 10, 0, "", "I’m not sugar and spice—I’m fire and ice.", true, 0, 0, null, "", 0, "null", "", null, "null", "muskan@gmail.com", "", girlsImage.get(6), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Rihan", "58749654", 23, "Rihan", true, "45678912"),
                new UserRoot.User(80, "", false, 0, "GERMANY", "", false, "female", 10, 0, "", "Independent. Unapologetic. Unstoppable.", true, 0, 0, null, "", 0, "null", "", null, "null", "lily@gmail.com", "", girlsImage.get(7), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Lily", "65412398", 23, "Lily", true, "45678912"),
                new UserRoot.User(90, "", false, 0, "USA", "", false, "female", 10, 0, "", "I’m not perfect, but I’m powerful.", true, 0, 0, null, "", 0, "null", "", null, "null", "Kennedy@gmail.com", "", girlsImage.get(8), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Kennedy", "78965487", 23, "Kennedy", true, "45678912"),
                new UserRoot.User(50, "", false, 0, "USA", "", false, "male", 10, 0, "", "Bold mind. Brave soul. Loud dreams.", true, 0, 0, null, "", 0, "null", "", null, "null", "Charlottebailey@gmail.com", "", girlsImage.get(9), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Charlotte Bailey", "36985471", 23, "Charlotte Bailey", true, "45678912"),
                new UserRoot.User(80, "", false, 0, "Canada", "", false, "female", 10, 0, "", "Dream big. Hustle harder.", true, 0, 0, null, "", 0, "null", "", null, "null", "sophia@gmail.com", "", girlsImage.get(10), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Sophia", "25896314", 22, "Sophia", true, "45678912"),
                new UserRoot.User(70, "", false, 0, "Australia", "", false, "male", 10, 0, "", "No guts, no glory.", true, 0, 0, null, "", 0, "null", "", null, "null", "ethan@gmail.com", "", girlsImage.get(11), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Ethan", "14796325", 24, "Ethan", true, "45678912"),
                new UserRoot.User(50, "", false, 0, "India", "", false, "female", 10, 0, "", "Smile, sparkle, shine.", true, 0, 0, null, "", 0, "null", "", null, "null", "meera@gmail.com", "", girlsImage.get(12), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Meera", "95175346", 21, "Meera", true, "45678912"),
                new UserRoot.User(60, "", false, 0, "Brazil", "", false, "male", 10, 0, "", "Courage over comfort.", true, 0, 0, null, "", 0, "null", "", null, "null", "lucas@gmail.com", "", girlsImage.get(13), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Lucas", "75395146", 26, "Lucas", true, "45678912"),
                new UserRoot.User(40, "", false, 0, "Spain", "", false, "female", 10, 0, "", "Stay classy. Sassy. Smart.", true, 0, 0, null, "", 0, "null", "", null, "null", "isabella@gmail.com", "", girlsImage.get(14), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Isabella", "85214796", 20, "Isabella", true, "45678912"),
                new UserRoot.User(10, "", false, 0, "Italy", "", false, "male", 10, 0, "", "Fearless by nature.", true, 0, 0, null, "", 0, "null", "", null, "null", "marco@gmail.com", "", girlsImage.get(15), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Marco", "32165498", 25, "Marco", true, "45678912"),
                new UserRoot.User(20, "", false, 0, "Japan", "", false, "female", 10, 0, "", "Elegance never fades.", true, 0, 0, null, "", 0, "null", "", null, "null", "hana@gmail.com", "", girlsImage.get(16), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Hana", "96374125", 22, "Hana", true, "45678912"),
                new UserRoot.User(30, "", false, 0, "Russia", "", false, "male", 10, 0, "", "Strength in silence.", true, 0, 0, null, "", 0, "null", "", null, "null", "ivan@gmail.com", "", girlsImage.get(17), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Ivan", "15975348", 24, "Ivan", true, "45678912"),
                new UserRoot.User(90, "", false, 0, "Mexico", "", false, "female", 10, 0, "", "Life is short, make it sweet.", true, 0, 0, null, "", 0, "null", "", null, "null", "camila@gmail.com", "", girlsImage.get(18), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Camila", "35795148", 23, "Camila", true, "45678912"),
                new UserRoot.User(50, "", false, 0, "South Africa", "", false, "male", 10, 0, "", "Chasing goals, not people.", true, 0, 0, null, "", 0, "null", "", null, "null", "liam@gmail.com", "", girlsImage.get(19), false, null, getFakeLevel(context), "", false, "null", 10, 1, 2, "Liam", "45698712", 22, "Liam", true, "45678912")

        ));
        if (isShuffle) {
            Collections.shuffle(userDummies);
        }
        return userDummies;
    }

    static List<Integer> levelImages = Arrays.asList(
            R.drawable.ic_level1,
            R.drawable.ic_level_2,
            R.drawable.ic_level_3,
            R.drawable.ic_level_4,
            R.drawable.ic_level_5
    );

    static List<String> levelNames = Arrays.asList(
            "Level 1",
            "Level 2",
            "Level 3",
            "Level 4",
            "Level 5"
    );



    public static UserRoot.Level getFakeLevel(Context context) {

        UserRoot.Level level = new UserRoot.Level();

        // pick random 0-4
        int index = new Random().nextInt(5);

        // set random level name
        level.setName(levelNames.get(index));

        // map id without space
        level.setId("LVL_" + (index + 1));

        // optional coin (example ─ choose based on level)
        level.setCoin((index + 1) * 1000);

        // pick drawable
        int drawableRes = levelImages.get(index);

        // convert drawable to URI
        String uri = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(drawableRes) + "/" +
                context.getResources().getResourceTypeName(drawableRes) + "/" +
                context.getResources().getResourceEntryName(drawableRes);

        level.setImage(uri);

        return level;
    }

}