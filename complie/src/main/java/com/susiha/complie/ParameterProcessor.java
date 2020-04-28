package com.susiha.complie;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.susiha.annotation.Parameter;
import com.susiha.complie.Utils.Constants;
import com.susiha.complie.Utils.EmptyUtils;
import com.susiha.complie.Utils.ParameterFactory;

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
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes(Constants.PARAMETER_ANNOTATION_TYPES)
public class ParameterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Filer filer;

    Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if(EmptyUtils.isEmpty(set)) return false;

        Set<? extends Element> elements =  roundEnvironment.getElementsAnnotatedWith(Parameter.class);

        if(!EmptyUtils.isEmpty(elements)){

            valueOfParameterMap(elements);

            try {
                createParameterFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private void createParameterFile() throws IOException {

        if(EmptyUtils.isEmpty(tempParameterMap)) return;
        TypeElement activityType = elementUtils.getTypeElement(Constants.ACTIVITY);
        TypeElement parameterType = elementUtils.getTypeElement(Constants.AROUTE_PARAMETER);

        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT,Constants.PARAMETER_NAMR).build();

        for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
            TypeElement typeElement = entry.getKey();

            if(!typeUtils.isSubtype(typeElement.asType(),activityType.asType())){
                throw new RuntimeException("@Parameter注解目前仅限用于Activity中的变量");
            }

            ClassName className = ClassName.get(typeElement);


            ParameterFactory factory = new ParameterFactory.Build(parameterSpec)
                    .setMessager(messager).setClassName(className).build();

            factory.addFistStatement();
            for (Element element : entry.getValue()) {
                factory.buildStatement(element);
            }

            // 最终生成的类文件名（类名$$Parameter）
            String finalClassName = typeElement.getSimpleName() + Constants.PARAMETER_FILE_NAME;
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                    className.packageName() + "." + finalClassName);

            // MainActivity$$Parameter
            JavaFile.builder(className.packageName(), // 包名
                    TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(parameterType)) // 实现ParameterLoad接口
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(factory.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件
        }
    }

    private void valueOfParameterMap(Set<? extends Element> elements) {

        for (Element element : elements) {

          TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
           List<Element> list = tempParameterMap.get(enclosingElement);
           if(EmptyUtils.isEmpty(list)){
              list = new ArrayList<>();
              list.add(element);
               tempParameterMap.put(enclosingElement,list);
           }else{
               list.add(element);
           }



        }
        



    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }
}
