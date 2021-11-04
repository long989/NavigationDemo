package com.qkl.nav_compiler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.auto.service.AutoService;
import com.qkl.nav_annotation.Destination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * @author qiukailong
 * @title:
 * @projectName NewDriverSchool
 * @description:
 * @date 2021/11/3
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"com.qkl.nav_annotation.Destination"})
public class NavProcessor extends AbstractProcessor {
    private static final String PAGE_TYPE_ACTIVITY = "Activity";
    private static final String PAGE_TYPE_FRAGMENT = "Fragment";
    private static final String PAGE_TYPE_DIALOG = "dialog";
    private static final String OUTPUT_FILE_NAME = "destination.json";
    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        //用来打印日志的
        messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "enter init......");
        //用来创建文件的
        filer = processingEnv.getFiler();

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        //获取所有标记Destination注解的集合
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Destination.class);
        if (!elements.isEmpty()) {
            HashMap<String, JSONObject> destMap = new HashMap<>();
            handleDestination(elements, Destination.class, destMap);
            try {
                FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME);
                // /app/build/intermediates/javac/debug/classes/目录下
                // 我们想要这个文件/app/main/assets
                String resourcePath = resource.toUri().getPath();
                String appPath = resourcePath.substring(0, resourcePath.indexOf("app") + 4);
                String assetsPath = appPath + "src/main/assets";
                File file = new File(assetsPath);
                if (file.exists()) {
                    file.mkdirs();
                }
                String content = JSON.toJSONString(destMap);
                File outputFile = new File(assetsPath, OUTPUT_FILE_NAME);
                if (outputFile.exists()) {
                    outputFile.delete();
                }
                outputFile.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
                writer.write(content);
                writer.flush();
                fileOutputStream.close();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void handleDestination(Set<? extends Element> elements, Class<Destination> destinationClass, HashMap<String, JSONObject> destMap) {
        for (Element element : elements) {
            //因为是标记在类上面的注解，所以是TypeElement
            /**
             * TypeElement 代表该元素是一个类或者接口
             * VariableElement 代表该元素是字段，方法入参，枚举常量
             * PackageElement 代表该元素是包级别
             * ExecutableElement 代表方法，构造方法
             *  TypeParameterElement 代表类型，接口，方法入参的泛型参数
             */
            TypeElement typeElement = (TypeElement) element;
            //获取全类名
            String clazzName = typeElement.getQualifiedName().toString();
            //获取注解
            Destination annotation = typeElement.getAnnotation(destinationClass);
            String pageUrl = annotation.pageUrl();
            boolean asStarter = annotation.asStarter();
            int id = Math.abs(clazzName.hashCode());
            //Activity,Dialog,Fragment
            String destType = getDestinationType(typeElement);
            if (destMap.containsKey(pageUrl)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "不同的页面不允许使用相同额pageUrl：" + pageUrl);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("clazzName", clazzName);
                jsonObject.put("pageUrl", pageUrl);
                jsonObject.put("asStarter", asStarter);
                jsonObject.put("id", id);
                jsonObject.put("destType", destType);
                destMap.put(pageUrl, jsonObject);
            }
        }
    }

    //获取类的类型（是activity还是fragment或者dialog）
    private String getDestinationType(TypeElement typeElement) {
        TypeMirror typeMirror = typeElement.getSuperclass();
        //就是全类名，例如androidx.fragment.app.fragment
        String superClazzName = typeMirror.toString();
        if (superClazzName.contains(PAGE_TYPE_ACTIVITY.toLowerCase())) {
            return PAGE_TYPE_ACTIVITY.toLowerCase();
        } else if (superClazzName.contains(PAGE_TYPE_FRAGMENT.toLowerCase())) {
            return PAGE_TYPE_FRAGMENT.toLowerCase();
        } else if (superClazzName.contains(PAGE_TYPE_DIALOG.toLowerCase())) {
            return PAGE_TYPE_DIALOG.toLowerCase();
        }
        //这个父类的类型是类的类型，或者是接口的类型
        if (typeMirror instanceof DeclaredType) {
            Element element = ((DeclaredType) typeMirror).asElement();
            if (element instanceof TypeElement) {
                return getDestinationType((TypeElement) element);
            }
        }
        return null;
    }
}
