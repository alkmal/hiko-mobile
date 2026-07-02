package com.codder.ultimate.agora.token;


import android.util.Log;

public class RtcTokenBuilderSample {


    static int uid = 2082341273;
    static int expirationTimeInSeconds = 360000;

//    public static String main(String channelName, String appId, String appCerti) throws Exception {
//        RtcTokenBuilder token = new RtcTokenBuilder();
//        int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
//        String result = token.buildTokenWithUserAccount(appId, appCerti,
//                channelName, "0", RtcTokenBuilder.Role.Role_Publisher, timestamp);
//        System.out.println(result);
//        Log.d("liveact", "main: tkn == " + result);
//        return result;
//
//       /* result = token.buildTokenWithUid(appId, appCertificate,
//                channelName, uid, RtcTokenBuilder.Role.Role_Publisher, timestamp);
//        System.out.println(result);
//        Log.d("TAG", "main: tkn22===  "+result);*/
//    }

    public static String main(String chennalName, String appId, String appCerti) throws Exception {
        RtcTokenBuilder token = new RtcTokenBuilder();
        int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        String result = token.buildTokenWithUserAccount(appId, appCerti,
                chennalName, "0", RtcTokenBuilder.Role.Role_Publisher, timestamp);
        System.out.println(result);
        Log.d("liveact", "main: tkn == " + result);
        return result;

       /* result = token.buildTokenWithUid(appId, appCertificate,
                chennalName, uid, RtcTokenBuilder.Role.Role_Publisher, timestamp);
        System.out.println(result);
        Log.d("TAG", "main: tkn22===  "+result);*/
    }

//    public static String main(String chennalName, String appId, String appCerti, int uid) throws Exception {
//        RtcTokenBuilder token = new RtcTokenBuilder();
//        int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
//        String result1 = token.buildTokenWithUserAccount(appId, appCerti,
//                chennalName, String.valueOf(uid), RtcTokenBuilder.Role.Role_Publisher, timestamp);
//        System.out.println(result1);
//        Log.d("liveact", "main: tkn == " + result1);
//
//
//        String result2 = token.buildTokenWithUid(appId, appCerti,
//                chennalName, uid, RtcTokenBuilder.Role.Role_Publisher, timestamp);
//        System.out.println(result2);
//        Log.d("TAG", "main: tkn22===  " + result2);
//
//        return result2;
//    }

    public static String main_Attendee(String chennalName, String appId, String appCerti, int uid) throws Exception {
        RtcTokenBuilder token = new RtcTokenBuilder();
        int timestamp = (int) (System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        String result1 = token.buildTokenWithUserAccount(appId, appCerti,
                chennalName, String.valueOf(uid), RtcTokenBuilder.Role.Role_Publisher, timestamp);
        System.out.println(result1);
        Log.d("liveact", "main: tkn == " + result1);


        String result2 = token.buildTokenWithUid(appId, appCerti,
                chennalName, uid, RtcTokenBuilder.Role.Role_Attendee, timestamp);
        System.out.println(result2);
        Log.d("TAG", "main: tkn22===  " + result2);

        return result2;
    }
}
