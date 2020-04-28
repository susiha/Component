package com.susiha.complie;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.susiha.annotation.Aroute;
import com.susiha.annotation.module.ArouteBean;
import com.susiha.complie.Utils.Constants;
import com.susiha.complie.Utils.EmptyUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.susiha.complie.Utils.Constants.GROUP_METHOD_NAME;

@AutoService(Processor.class)
@SupportedAnnotationTypes(Constants.AROUTE_ANNOTATION_TYPES)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({Constants.MODULE_NAME, Constants.APT_PACKAGE})
public class ArouteProcessor extends AbstractProcessor {

    //Element工具类
    private Elements elementUtils;
    //Type (类信息)工具类
    private Types typeUtils;

    //输出错误日志
    private Messager messager;

    //文件生成器
    private Filer filer;

    private String moduleName;
    private String packageNameForApt;

    // key:组名"app", value:"app"组的路由路径"ARouter$$Path$$app.class"
    private Map<String, List<ArouteBean>> tempPathMap = new HashMap<>();
    // 临时map存储，用来存放路由Group信息，生成路由组类文件时遍历
    // key:组名"app", value:类名"ARouter$$Path$$app.class"
    private Map<String, String> tempGroupMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        //获取参数
        Map<String, String> options = processingEnvironment.getOptions();

        if (!EmptyUtils.isEmpty(options)) {
            moduleName = options.get(Constants.MODULE_NAME);
            packageNameForApt = options.get(Constants.APT_PACKAGE);
            messager.printMessage(Diagnostic.Kind.NOTE, "moduleName >>>> " + moduleName == null ? "null" : moduleName);
            messager.printMessage(Diagnostic.Kind.NOTE, "packageNameForApt >>>> " + packageNameForApt == null ? "null" : packageNameForApt);
        }

        if (EmptyUtils.isEmpty(moduleName) || EmptyUtils.isEmpty(packageNameForApt)) {
            throw new RuntimeException("注解处理器需要module传递moduleName和packageName");
        }


//       String note = processingEnvironment.getOptions().get("content");
//        messager.printMessage(Diagnostic.Kind.NOTE,note==null?"":note);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }


    /**
     * 接收参数
     *
     * @return
     */
    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (EmptyUtils.isEmpty(set)) {
            return false;
        }

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Aroute.class);

        if (EmptyUtils.isEmpty(elements)) {
            return false;
        }

        parseElements(elements);
        return true;
    }

    /**
     * 解析Elements
     *
     * @param elements
     */
    private void parseElements(Set<? extends Element> elements) {

        TypeElement activityType = elementUtils.getTypeElement(Constants.ACTIVITY);
        TypeMirror activityMirror = activityType.asType();

        messager.printMessage(Diagnostic.Kind.NOTE, "当前module是 >>>>" + moduleName
                + " elements 的 size = >>>> " + elements.size());


        for (Element element : elements) {

            //获取Element的元素类型 比如MainActivity的类型
            TypeMirror elementMirror = element.asType();
            messager.printMessage(Diagnostic.Kind.NOTE, "遍历元素 >>>> " + elementMirror.toString());

            Aroute aroute = element.getAnnotation(Aroute.class);

            //构造一个ArouteBean
            ArouteBean bean = new ArouteBean.Builder()
                    .setGroup(aroute.group())
                    .setPath(aroute.path())
                    .setElement(element)
                    .build();

            /**
             * 相当于instance一样
             */
            if (typeUtils.isSubtype(elementMirror, activityMirror)) {
                bean.setType(ArouteBean.Type.ACTIVITY);
            } else {

                throw new RuntimeException("@Arote注解仅限于Activity之上");
            }

            valueOfPathMap(bean);







        }

        //group接口类型
        TypeElement groupType = elementUtils.getTypeElement(Constants.AROUTE_GROUP);
        //path 接口类型
        TypeElement pathType = elementUtils.getTypeElement(Constants.AROUTE_PATH);
//            TypeElement pathType = elementUtils.getTypeElement("com.susiha.complie.Utils.EmptyUtils");
        messager.printMessage(Diagnostic.Kind.NOTE, "pathType >>>> " + pathType);



        try {
            //第一步 生成Group 对应的Path类文件
            createPathFile(pathType);
            // 第二步 生成路由Group类文件
            createGroupFile(groupType,pathType);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * public interface ArouteLoadPath {
     *
     *     Map<String, ArouteBean> loadpath();
     * }
     * 生成Path类文件
     * @param pathType
     */
    private void createPathFile(TypeElement pathType) throws IOException {
        if(EmptyUtils.isEmpty(tempPathMap)) return; //没有需要生成的类文件
        //返回值类型
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(ArouteBean.class));


        for (Map.Entry<String, List<ArouteBean>> entry : tempPathMap.entrySet()) {

            //首先创建好方法签名
            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(Constants.AROUTE_PATH_NAME)
                    .addAnnotation(Override.class) //因为是重写了ArouteLoadPath接口中的loadPath方法
                    .addModifiers(Modifier.PUBLIC)
                    .returns(methodReturns);

            //添加内容
            //1. 创建一个返回对象
            methodSpec.addStatement("$T<$T,$T> $N = new $T()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(ArouteBean.class),
                    Constants.AROUTE_PATH_PARAMETER_NAME,
                    ClassName.get(HashMap.class));

            List<ArouteBean> arouteBeans =entry.getValue();



            for (ArouteBean bean : arouteBeans) {

                //2. 为返回对象添加数据
                //    pathMap.put("/app/MainActivity", ArouteBean.create(
                //               RouterBean.Type.ACTIVITY, MainActivity.class, "/app/MainActivity", "app"));
                methodSpec.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        Constants.AROUTE_PATH_PARAMETER_NAME,
                        bean.getPath(),
                        ClassName.get(ArouteBean.class),
                        ClassName.get(ArouteBean.Type.class),
                        bean.getType(),
                        ClassName.get((TypeElement) bean.getElement()),
                        bean.getPath(),
                        bean.getGroup()
                        );
            }

            methodSpec.addStatement("return $N",Constants.AROUTE_PATH_PARAMETER_NAME);

            String finalClassName = Constants.AROUTE_PATH_FILE_NAME+entry.getKey();

            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                    packageNameForApt + "." + finalClassName);


            JavaFile.builder(packageNameForApt,//包名
                    TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(pathType))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(methodSpec.build())
                            .build())
                    .build()
                    .writeTo(filer);

            // 非常重要一步！！！！！路径文件生成出来了，才能赋值路由组tempGroupMap
            tempGroupMap.put(entry.getKey(), finalClassName);
        }
        
    }
    private void createGroupFile(TypeElement groupType,TypeElement pathType) throws IOException {

        if(EmptyUtils.isEmpty(tempGroupMap)||EmptyUtils.isEmpty(tempPathMap)) return;
        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))
                        )
        );


        MethodSpec.Builder methodBuild = MethodSpec.methodBuilder(Constants.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturns);

        methodBuild.addStatement("$T<$T,$T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))
                ),
                Constants.AROUTE_GROUP_PARAMETER_NAME,
                ClassName.get(HashMap.class)
                );

        for (Map.Entry<String, String> entry : tempGroupMap.entrySet()) {
            methodBuild.addStatement("$N.put($S,$T.class)",
                    Constants.AROUTE_GROUP_PARAMETER_NAME,
                    entry.getKey(),
                    ClassName.get(packageNameForApt,entry.getValue())
                    );
        }

        methodBuild.addStatement("return $N", Constants.AROUTE_GROUP_PARAMETER_NAME);

        String finalClassName =Constants.GROUP_FILE_NAME+moduleName;
        JavaFile.builder(packageNameForApt,
                TypeSpec.classBuilder(finalClassName)
                        .addSuperinterface(ClassName.get(groupType))
                         . addModifiers(Modifier.PUBLIC)
                         .addMethod(methodBuild.build())
                         .build()

                ).build()
                .writeTo(filer);
    }

    /**
     * 构建临时数据Map
     *
     * @param bean
     */
    private void valueOfPathMap(ArouteBean bean) {
        if (checkRoutePath(bean)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "ArouteBean >>>> " + bean.toString());
            List<ArouteBean> arouteBeans = tempPathMap.get(bean.getGroup());
            //找不到表示在Map中没有对应的额分组
            if (EmptyUtils.isEmpty(arouteBeans)) {
                arouteBeans = new ArrayList<>();
                arouteBeans.add(bean);
                tempPathMap.put(bean.getGroup(), arouteBeans);
            } else {
                arouteBeans.add(bean);
            }


        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "@Aroute 必须按照规范配置");
        }
    }

    /**
     * 检查AroutePath的规范
     *
     * @param bean
     * @return
     */
    private boolean checkRoutePath(ArouteBean bean) {
        String group = bean.getGroup();
        String path = bean.getPath();


        if (EmptyUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.NOTE, "@Aroute 注解的path值必须以'/'开头");
            return false;
        }


        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.NOTE, "@Aroute 至少需要两个'/'如 '/app/MainActivity'");
            return false;
        }


        //第一个/与第二个/之间的部分作为Group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (!EmptyUtils.isEmpty(group) && !group.equalsIgnoreCase(moduleName)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "@Aroute group要与module名称一致");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }
        return true;
    }
}
