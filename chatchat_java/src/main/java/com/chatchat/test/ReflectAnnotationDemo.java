package com.chatchat.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

// ReflectAnnotationDemo.java
@AnyAnnotation(order = 1, desc = "我是类上的注释")
public class ReflectAnnotationDemo {

    @AnyAnnotation(order = 2, desc = "我是成员属性")
    private String name;

    @AnyAnnotation(order = 3, desc = "我是构造器")
    public ReflectAnnotationDemo(@AnyAnnotation(order = 4, desc = "我是构造器参数") String name) {
        this.name = name;
    }

    @AnyAnnotation(order = 45, desc = "我是方法")
    public void method(@AnyAnnotation(order = 6, desc = "我是方法参数") String msg) {
        @AnyAnnotation(order = 7, desc = "我是方法内部变量") String prefix = "I am ";
        System.out.println(prefix + msg);
    }

    public static void main(String[] args) throws NoSuchFieldException, NoSuchMethodException {
        Class<ReflectAnnotationDemo> clazz = ReflectAnnotationDemo.class;
        // 获取包上的注解，声明在package-info.java文件中
        Package packagee = Package.getPackage("com.chatchat.test");
        printAnnotation(packagee.getAnnotations());
        // 获取类上的注解
        Annotation[] annotations = clazz.getAnnotations();
        printAnnotation(annotations);
        // 获取成员属性注解
        Field name = clazz.getDeclaredField("name");
        Annotation[] annotations1 = name.getAnnotations();
        printAnnotation(annotations1);
        //获取构造器上的注解
        Constructor<ReflectAnnotationDemo> constructor = clazz.getConstructor(String.class);
        AnyAnnotation[] annotationsByType = constructor.getAnnotationsByType(AnyAnnotation.class);
        printAnnotation(annotationsByType);
        // 获取构造器参数上的注解
        Parameter[] parameters = constructor.getParameters();
        for (Parameter parameter : parameters) {
            Annotation[] annotations2 = parameter.getAnnotations();
            printAnnotation(annotations2);
        }
        // 获取方法上的注解
        Method method = clazz.getMethod("method", String.class);
        AnyAnnotation annotation = method.getAnnotation(AnyAnnotation.class);
        printAnnotation(annotation);
        // 获取方法参数上的注解
        Parameter[] parameters1 = method.getParameters();
        for (Parameter parameter : parameters1) {
            printAnnotation(parameter.getAnnotations());
        }
        // 获取局部变量上的注解
        /**
         * 查了一些资料，是无法获取局部变量的注解的，且局部变量的注解仅保留到Class文件中，运行时是没有的。
         * 这个更多是给字节码工具使用的，例如lombok可以嵌入编译流程，检测到有对应注解转换成相应的代码，
         * 而反射是无法进行操作的。当然也可以利用asm等工具在编译器完成你要做的事情
         */
    }

    public static void printAnnotation(Annotation... annotations) {
        for (Annotation annotation : annotations) {
            System.out.println(annotation);
        }
    }
}
