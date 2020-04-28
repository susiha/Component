package com.susiha.annotation.module;

import javax.lang.model.element.Element;

public class ArouteBean {



    public enum Type{
        ACTIVITY
    }

    public Type getType() {
        return type;
    }

    public Element getElement() {
        return element;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getGroup() {
        return group;
    }

    public String getPath() {
        return path;
    }

    private Type type;
    private Element element;
    private Class clazz;

    public void setGroup(String group) {
        this.group = group;
    }

    private String group;
    private String path;

    public void setType(Type type){
        this.type = type;
    }

    private ArouteBean(Builder builder) {
        this.path = builder.path;
        this.group = builder.group;
        this.element = builder.element;
    }

    private ArouteBean(Type type, Class clazz, String path, String group) {
        this.type = type;
        this.path  = path;
        this.group = group;
        this.clazz = clazz;
    }


    public static ArouteBean create(Type type,Class clazz,String path,String group){

        return new ArouteBean(type,clazz,path,group);
    }


    public static final class Builder{
        private Element element;
        private String group;
        private String path;

        public Builder setElement(Element element) {
            this.element = element;
            return this;
        }

        public Builder setGroup(String group) {
            this.group = group;
            return this;
        }

        public Builder setPath(String path) {
            this.path = path;
            return this;
        }

        public ArouteBean build(){
            if(path==null||path.length()==0){
                throw new IllegalArgumentException("path是必填项，格式如'/app/MainActivity'");
            }

            return new ArouteBean(this);
        }
    }

    @Override
    public String toString() {
        return "ArouteBean{" +
                "type=" + type +
                ", element=" + element +
                ", clazz=" + clazz +
                ", group='" + group + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
