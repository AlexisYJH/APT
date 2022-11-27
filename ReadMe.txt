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
