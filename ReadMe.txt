APT(Annotation Processing Tool)，是javac的一个工具，编译时注解处理器。
APT可以用来在编译时扫描和处理注解，通过APT可以获取到注解和被注解对象的相关信息，
在拿到这些信息后我们可以根据需求来自动的生成一些代码，省去了手动编写。
注意，获取注解以及生成代码都是在代码编译时完成的，相比反射在运行时处理注解大大提高了程序性能。

$$app
借助注解和注解处理两个java库，来完成依赖注入
在apt\app\build\generated\ap_generated_sources\debug\out\com\example\apt目录生成以下文件
DaggerApplicationComponent.java
MainActivity_MembersInjector.java
SecondActivity_MembersInjector.java
Student_Factory.java
User_Factory.java

$$lib_compiler
主要通过AbstractProcessor，实现注解处理相关业务（使用JavaPoet生成Java文件）
生成文件apt\lib_compiler\build\classes\java\main\META-INF\services\javax.annotation.processing.Processor

$$lib_annotation
提供注解类及核心业务实现

$$manualcode
手写Dagger生成的代码
