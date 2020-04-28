package com.susiha.complie.Utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.susiha.annotation.Parameter;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class ParameterFactory {

    private Messager messager;
    private ClassName className;
    private MethodSpec.Builder methodBuild;

    private ParameterFactory(Build build){

        this.className = build.className;
        this.messager = build.messager;

        methodBuild = MethodSpec.methodBuilder(Constants.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.VOID)
                .addParameter(build.parameterSpec);
    }

    public void addFistStatement(){
        methodBuild.addStatement("$T t = ($T)target",className,className);
    }

    public MethodSpec build(){
       return  methodBuild.build();
    }


    public void buildStatement(Element element){
        TypeMirror typeMirror = element.asType();
        int type = typeMirror.getKind().ordinal();

        String fieldName = element.getSimpleName().toString();

        String annotationValue = element.getAnnotation(Parameter.class).name();


        annotationValue = EmptyUtils.isEmpty(annotationValue)?fieldName:annotationValue;

        String finalValue = "t."+fieldName;

        String methodContent = finalValue +" = t.getIntent().";

        if(type == TypeKind.INT.ordinal()){
            methodContent += "getIntExtra($S, "+finalValue+")";
        }else if(type == TypeKind.BOOLEAN.ordinal()){
            methodContent += "getBooleanExtra($S, " + finalValue + ")";
        }else {
            // t.s = t.getIntent.getStringExtra("s");
            if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
                methodContent += "getStringExtra($S)";
            }
        }

        // 健壮代码
        if (methodContent.endsWith(")")) {
            // 添加最终拼接方法内容语句
            methodBuild.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String、int、boolean传参");
        }










    }


    public static class Build{

        private Messager messager;


        private ClassName className;

        private ParameterSpec parameterSpec;


        public Build(ParameterSpec parameterSpec){
            this.parameterSpec = parameterSpec;
        }

        public Build setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Build setClassName(ClassName className) {
            this.className = className;
            return this;
        }


        public ParameterFactory build(){
            if (parameterSpec == null) {
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (className == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (messager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }

            return new ParameterFactory(this);

        }



    }

}
