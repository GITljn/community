package com.nowcoder.community.utils;

//import org.springframework.util.DigestUtils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class MD5Util {
    private static String salt = "1a2b3c4d";

    public static String md5(String pass) {
        return DigestUtils.md5DigestAsHex(pass.getBytes(StandardCharsets.UTF_8));
    }

    public static String inputPassToMidPass(String pass) {
        String passAddSalt = "" + salt.charAt(0) + salt.charAt(2) + pass + salt.charAt(5) + salt.charAt(4);
        return md5(passAddSalt);
    }

    public static String midPassToDBPass(String MidPass, String salt) {
        String midPassAddSalt = "" + salt.charAt(0) + salt.charAt(2) + MidPass + salt.charAt(5) + salt.charAt(4);
        return md5(midPassAddSalt);
    }

    public static String inputPassToDBPass(String pass, String salt) {
        String midPass = inputPassToMidPass(pass);
        String dbPass = midPassToDBPass(midPass, salt);
        return dbPass;
    }

    public static void main(String[] args) {
        String midPass = inputPassToMidPass("123");
        System.out.println(midPass);
        String dbPass = midPassToDBPass(midPass, "1a2b3c4d");
        System.out.println(dbPass);
        System.out.println(inputPassToDBPass("123", "1a2b3c4d"));
    }
}
