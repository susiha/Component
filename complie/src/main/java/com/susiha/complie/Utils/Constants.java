package com.susiha.complie.Utils;

public class Constants {
    //注解的类型
    public static final String AROUTE_ANNOTATION_TYPES ="com.susiha.annotation.Aroute";
    public static final String PARAMETER_ANNOTATION_TYPES ="com.susiha.annotation.Parameter";

    //每一个子模块的名称
    public static final String MODULE_NAME = "moduleName";

    public static final String APT_PACKAGE = "packageNameForAPT";

    //Activity 全类名
    public static final String ACTIVITY = "android.app.Activity";

    // 包名前缀封装
    static final String BASE_PACKAGE = "com.susiha.arouteapi";
    // 路由组Group加载接口
    public static final String AROUTE_GROUP = BASE_PACKAGE + ".ArouteLoadGroup";
    // 路由组Group对应的详细Path加载接口
    public static final String AROUTE_PATH = BASE_PACKAGE + ".ArouteLoadPath";
    public static final String AROUTE_PARAMETER = BASE_PACKAGE + ".ParameterLoad";

    public static final String AROUTE_PATH_NAME = "loadPath";
    // 路由组Group对应的详细Path，参数名
    public static final String AROUTE_PATH_PARAMETER_NAME = "pathMap";
    public static final String AROUTE_PATH_FILE_NAME = "ARouter$$Path$$";

    public static final String GROUP_METHOD_NAME = "loadGroup";
    public static final String AROUTE_GROUP_PARAMETER_NAME = "groupMap";
    // APT生成的路由组Group源文件名
    public static final String GROUP_FILE_NAME = "ARouter$$Group$$";

    public static final String PARAMETER_NAMR = "target";
    public static final String PARAMETER_METHOD_NAME = "loadParameter";
    // String全类名
    public static final String STRING = "java.lang.String";
    public static final String PARAMETER_FILE_NAME = "$$Parameter";







}