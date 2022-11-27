package com.example.manualcode;

import com.example.lib_annotation.Provider;

/**
 * @author AlexisYin
 *
 * 手写Dagger生成代码（JavaPoet实现）
 * 1. 在lib_annotation中创建Provider接口
 * 2. 创建User_Factory类实现Provider接口
 * 3. 创建MainActivity_MembersInjector类，提供注入方法
 * 4. 创建DaggerApplicationComponent类，提供create方法，在构造方法中初始化Provider对象，提供inject方法
 */
public class DaggerApplicationComponent {

    Provider<User> userProvider;

    public static DaggerApplicationComponent create() {
        return new DaggerApplicationComponent();
    }

    public DaggerApplicationComponent() {
        userProvider = new User_Factory();
    }

    public void inject(MainActivity mainActivity) {
        MainActivity_MembersInjector.injectUser(mainActivity, userProvider.get());
    }
}
