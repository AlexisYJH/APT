package com.example.lib_compiler;

/**
 * @author AlexisYin
 */

import com.example.lib_annotation.Inject;
import com.example.lib_annotation.Provider;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 1.创建注解处理类CustomProcessor，继承AbstractProcessor
 * AbstractProcessor这个类是Java JDK库中自带的
 * AS默认未加载javax包，javax的默认引用居然是android下的javax
 * 解决方法：在当前moudel的build.gradle文件中手动引入jar包
 * implementation files ('C:\\Program Files\\Java\\jre1.8.0_152\\lib\\rt.jar')
 * <p>
 * 2.添加依赖，添加@AutoService(Processor.class)注解，将注解处理器注册到javac中
 * 在类的顶部加入注解：@AutoService(Processor.class)，这个注解处理器是Google开发的，可以用来生成 META-INF/services/javax.annotation.processing.Processor 文件信息。
 * 编译项目后，会项目的目录module/build/classes/java/main/META-INF下生成。
 * 但是在使用 @AutoService 注解的时候发现无法生成对应的文件，这个是因为gradle和apt的版本兼容问题
 * 解决办法是引入如下插件：
 * implementation 'com.google.auto.service:auto-service:1.0-rc6'
 * annotationProcessor 'com.google.auto.service:auto-service:1.0-rc6'
 * <p>
 * 3.使用SupportedAnnotationTypes，指定注解处理类处理哪些注解
 * 3.1 在lib_annotation中添加注解Inject
 * 3.2 添加lib_annotation依赖
 * 3.3 将全类名作为参数
 * <p>
 * 4.定义java版本8，与build.gradle中保持一致
 * <p>
 * 5.重写init方法，用来进行初始化操作
 * <p>
 * 6. 在app模块中添加lib_compiler和lib_annotation依赖
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.example.lib_annotation.Inject")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class CustomProcessor extends AbstractProcessor {
    private static final String PACKAGE_NAME = "com.example.apt";

    private Messager messager;
    private Types typeUtils;
    private Filer filer;

    private static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    /**
     * init...
     * process...
     * element.getSimpleName(): user
     * variableTypeElement: com.example.apt.User
     * parentElement: com.example.apt.MainActivity
     * subElement: <init>
     * subElement: user
     * subElement: onCreate
     * value: name
     * process...
     * process...
     */

    /**
     * 用来进行初始化操作
     *
     * @param processingEnvironment 处理工具集合
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //用于打印日志
        messager = processingEnvironment.getMessager();
        print("init...");
        typeUtils = processingEnvironment.getTypeUtils();
        filer = processingEnvironment.getFiler();
    }

    /**
     * 扫描到指定的注解被使用，就进入该方法
     *
     * @param set              指定注解的集合
     * @param roundEnvironment 提供当前的注解的对象信息获取
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        print("process...");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Inject.class);
        Map<TypeElement, Set<Element>> map = new HashMap<>();


        for (Element element : elements) {
            //print("element.getSimpleName(): " + element.getSimpleName());

            VariableElement variableElement = (VariableElement) element;
            TypeMirror typeMirror = variableElement.asType();
            TypeElement variableTypeElement = (TypeElement) typeUtils.asElement(typeMirror);
            //print("variableTypeElement: " + variableTypeElement.getQualifiedName());

            TypeElement parentElement = (TypeElement) variableElement.getEnclosingElement();
            /*print("parentElement: " + parentElement.getQualifiedName());

            for(Element subElement: parentElement.getEnclosedElements()) {
                print("subElement: " + subElement.getSimpleName());
            }*/

            Inject inject = variableElement.getAnnotation(Inject.class);
            //print("value: " + inject.value());

            Set<Element> children = map.get(parentElement);
            if (children == null) {
                children = new HashSet<>();
                map.put(parentElement, children);
            }
            children.add(element);
            print("parentElement: " + parentElement + ", add: " + variableTypeElement.getQualifiedName() + " " + element);
        }
        print("size: " + map.keySet().size());
        if (map.keySet().size() > 0) {
            //JavaPoet
            //1. 创建Factory
            createFactory(map);
            //2. 创建MembersInjector
            createMembersInjector(map);
            //3. DaggerApplicationComponent
            createDaggerApplicationComponent(map);
        }
        return false;
    }

    private void print(CharSequence msg) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    /**
     * 在app\build\generated\ap_generated_sources\debug\out\com\example\apt中生成User_Factory.java文件
     * 0.添加javapoet依赖
     * 1.创建Factory
     * 2.删除原来手写的com\example\apt\User_Factory.java
     * <p>
     * 在JavaPoet中，format中存在三种特定的占位符：
     * $T 在JavaPoet代指的是TypeName，该模板主要将Class抽象出来，用传入的TypeName指向的Class来代替。
     * addStatement("$T bundle = new $T()",ClassName.get("android.os", "Bundle"))
     * Bundle bundle = new Bundle();
     * <p>
     * $N 在JavaPoet中代指的是一个名称，例如调用的方法名称，变量名称，这一类存在意思的名称
     * addStatement("data.$N()",toString)
     * data.toString();
     * <p>
     * $S 在JavaPoet中就和String.format中S的地方，需要注意的是替换后的内容，默认自带了双引号，如果不需要双引号包裹，需要使用$L.
     * .addStatement("return $S", “name”)即将"name"字符串代替到$S的位置上.
     *
     * @param map
     */
    private void createFactory(Map<TypeElement, Set<Element>> map) {
        Set<String> factory = new HashSet<>();
        for (TypeElement parentElement : map.keySet()) {
            Set<Element> set = map.get(parentElement);
            for (Element item : set) {
                TypeElement element = getTypeElement(item);
                if (factory.add(element.getQualifiedName().toString())) {
                    createFactory(element);
                }
            }
        }
    }

    private void createFactory(TypeElement element) {
        String name = getFactoryName(element);
        MethodSpec methodSpec = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(element.asType()))
                .addStatement("return new $T()", TypeName.get(element.asType()))
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(name)
                .addMethod(methodSpec)
                .addSuperinterface(Provider.class)
                .addModifiers(Modifier.PUBLIC)
                .build();

        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        print("--createFactory: " + name);
    }

    private void createMembersInjector(Map<TypeElement, Set<Element>> map) {
        for (TypeElement parentElement : map.keySet()) {
            String name = getMembersInjectorName(parentElement);
            Set<Element> set = map.get(parentElement);
            List<MethodSpec> methodSpecs = new ArrayList<>();
            for (Element element : set) {
                TypeElement variableElement = getTypeElement(element);
                ParameterSpec parentParameterSpec = getParameterSpec(parentElement);
                ParameterSpec variableParameterSpec = getParameterSpec(variableElement);
                List<ParameterSpec> parameterSpecs = new ArrayList<>();
                parameterSpecs.add(parentParameterSpec);
                parameterSpecs.add(variableParameterSpec);
                //print(parentParameterSpec.name + "-->" + parentParameterSpec.type.toString());

                MethodSpec methodSpec = MethodSpec.methodBuilder("inject" + variableElement.getSimpleName())
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(TypeName.VOID)
                        .addParameters(parameterSpecs)
                        //$T占位符
                        .addStatement("$L.$L = $L", parentParameterSpec.name, element.getSimpleName(), variableParameterSpec.name)
                        .build();
                methodSpecs.add(methodSpec);
            }

            TypeSpec typeSpec = TypeSpec.classBuilder(name)
                    .addMethods(methodSpecs)
                    .addModifiers(Modifier.PUBLIC)
                    .build();
            JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec).build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            print("--createMembersInjector: " + name);
        }
    }

    private ParameterSpec getParameterSpec(TypeElement element) {
        return ParameterSpec.builder(TypeName.get(element.asType()),
                decapitalize(element.getSimpleName().toString())).build();
    }

    private TypeElement getTypeElement(Element element) {
        VariableElement variableElement = (VariableElement) element;
        TypeMirror typeMirror = variableElement.asType();
        return (TypeElement) typeUtils.asElement(typeMirror);
    }

    private void createDaggerApplicationComponent(Map<TypeElement, Set<Element>> map) {
        String name = "DaggerApplicationComponent";
        TypeName typeName = ClassName.get(PACKAGE_NAME, name);

        Set<String> fieldNames = new HashSet<>();
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        List<MethodSpec> methodSpecs = new ArrayList<>();
        MethodSpec createMethodSpec = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(typeName)
                .addStatement("return new $T()", typeName)
                .build();
        methodSpecs.add(createMethodSpec);
        MethodSpec.Builder constructorMethodSpecBuilder = MethodSpec.constructorBuilder();

        for (TypeElement parentElement : map.keySet()) {
            Set<Element> set = map.get(parentElement);
            ParameterSpec parameterSpec = getParameterSpec(parentElement);
            MethodSpec.Builder injectMethodSpecBuilder = MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.VOID)
                    .addParameter(parameterSpec);

            for (Element element : set) {
                TypeElement variableElement = getTypeElement(element);
                TypeName fieldTypeName = ParameterizedTypeName.get(ClassName.get(Provider.class),
                        TypeName.get(variableElement.asType()));
                String fieldName = decapitalize(variableElement.getSimpleName().toString()) + Provider.class.getSimpleName();
                if (fieldNames.add(fieldName)) {
                    FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, fieldName).build();
                    fieldSpecs.add(fieldSpec);

                    constructorMethodSpecBuilder.addStatement("$L = new $T()",
                            fieldName,
                            ClassName.get(PACKAGE_NAME, getFactoryName(variableElement)));
                }

                injectMethodSpecBuilder.addStatement("$L.inject$L($L, $L.get())",
                        getMembersInjectorName(parentElement), variableElement.getSimpleName(),
                        parameterSpec.name, fieldName);
            }
            methodSpecs.add(injectMethodSpecBuilder.build());
        }
        methodSpecs.add(constructorMethodSpecBuilder.build());

        TypeSpec typeSpec = TypeSpec.classBuilder(name)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .addModifiers(Modifier.PUBLIC)
                .build();
        JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, typeSpec).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        print("--createDaggerApplicationComponent: " + name);
    }

    private String getFactoryName(TypeElement element) {
        return element.getSimpleName() + "_Factory";
    }

    private String getMembersInjectorName(TypeElement element) {
        return element.getSimpleName() + "_MembersInjector";
    }
}