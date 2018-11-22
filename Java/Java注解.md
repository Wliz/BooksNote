注：本文均是在Spring项目下编写及测试（自带validation）

[注解声明（简单，不定时更新）](#注解声明简单不定时更新)

[注解分类（一般关联Java反射）](#注解分类一般关联java反射)

[重复注解（jdk1.8之后变化）](#重复注解jdk18之后变化)

---

## 注解声明（简单，不定时更新）
```Java
// 一般注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnorationName {}
```
## 注解分类（一般关联Java反射）
- 一般注解
```Java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnorationName {}
```
- 验证注解（依赖validation）
```Java
// 继承已有注解（@Pattern），注解可以被继承，但必须包含message，groups， payload
@Pattern(regexp = "1313897830", message = "继承的格式错误")
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface Phone2 {
    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}

// 无继承，通过Constraint指定对应的注解处理器（可以实现ConstraintValidator接口）
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidate.class)
@Documented
public @interface Phone {
    // 不可为空
    boolean nullAble() default true;

    // 长度必须为11位
    int length() default 11;

    String message() default "手机号码错误";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
```
## 重复注解（jdk1.8之后变化）
- 1.8 before
```Java
// 声明
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleAnnoration {
    String value() default "hi";
}

// 声明重复注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleAnnorations {
    SimpleAnnoration[] value();
}

// 使用
public class User {
    @SimpleAnnorations(value = {@SimpleAnnoration("w"), @SimpleAnnoration("l"), @SimpleAnnoration("z") })
    private String name;
}
```
- 1.8 after
```Java
// 声明
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(SimpleAnnorations.class)
public @interface SimpleAnnoration {
    String value() default "hi";
}

// 声明重复注解
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleAnnorations {
    SimpleAnnoration[] value();
}

// 使用
public class User {
    @SimpleAnnoration("J")
    @SimpleAnnoration("a")
    @SimpleAnnoration("s")
    private Integer age;
}
```
```Java
// 测试
@Test
    public void duplicationTest() throws Exception {
        Class<?> clazz = User.class;
        Field[] fields = clazz.getDeclaredFields();
        // Java 8 之前的重复注解做法
        SimpleAnnorations simpleAnnorations = clazz.getDeclaredField("name").getAnnotation(SimpleAnnorations.class);
        if (simpleAnnorations != null) {
            for (SimpleAnnoration annoration : simpleAnnorations.value()) {
                System.out.println("tag.value: " + annoration.value());
            }
        }

        Annotation[] annotations1 = clazz.getDeclaredField("age").getAnnotationsByType(SimpleAnnoration.class);
        for (Annotation annotation : annotations1) {
            SimpleAnnoration annoration2 = (SimpleAnnoration) annotation;
            System.out.println("tag.value: " + annoration2.value());
        }
    }
    // 结果
    tag.value: w
    tag.value: l
    tag.value: z

    tag.value: J
    tag.value: a
    tag.value: s
```
