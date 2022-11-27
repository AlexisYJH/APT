package com.example.manualcode;

/**
 * @author AlexisYin
 */
public class MainActivity_MembersInjector {
    public static void injectUser(MainActivity activity, User user) {
        activity.user = user;
    }
}
