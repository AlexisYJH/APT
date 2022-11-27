package com.example.manualcode;

import com.example.lib_annotation.Provider;

/**
 * @author AlexisYin
 */
public class User_Factory implements Provider<User> {
    @Override
    public User get() {
        return new User();
    }
}
