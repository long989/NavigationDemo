package com.qkl.nav_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * @author qiukailong
 * @title:
 * @projectName NewDriverSchool
 * @description:
 * @date 2021/11/3
 */
@Target(ElementType.TYPE)
public @interface Destination {
    /**
     * 保存在路由中页面的url
     *
     * @return
     */
    String pageUrl();

    /**
     * 是否是第一次启动页面
     *
     * @return
     */
    boolean isStarter() default false;
}
