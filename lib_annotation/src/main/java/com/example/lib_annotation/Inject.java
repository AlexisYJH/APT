package com.example.lib_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author AlexisYin
 */

/**
 * Target同样会导入失败，手动引入jar包
 *
 * 注解生命周期即表明注解在代码的什么阶段生效，通过@Retention来指定，其值可以为以下三种：
 *     SOURCE,源码注解， 注解只在源码中存在，javac在编译成class时会把Java源程序上的源码注解给去掉编译成.class文件就将相应的注解去掉。
 *     CLASS,编译时注解，注解在源码、.class文件里面存在。
 *     RUNTIME,运行时注解，在运行阶段还起作用
 * 三个阶段简单表示为：java源文件–>class文件–>内存中的字节码
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
    String value() default "";
}
