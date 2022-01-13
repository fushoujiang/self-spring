package com.spring;

import java.util.Map;

public class BeanDefinition {

    private Class type;
    /**
     * 作用域
     */
    private String scope;

    /**
     * 是否是懒加载
     */
    private boolean isLazy;



    public Class getType() {
        return type;
    }

    public BeanDefinition setType(Class type) {
        this.type = type;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public BeanDefinition setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public boolean isLazy() {
        return isLazy;
    }

    public BeanDefinition setLazy(boolean lazy) {
        isLazy = lazy;
        return this;
    }
}
