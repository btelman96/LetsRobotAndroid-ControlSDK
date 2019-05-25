package org.btelman.pref.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.btelman.prefs.annotation.PreferenceBool;
import org.btelman.prefs.annotation.PreferenceEnum;
import org.btelman.prefs.annotation.PreferenceInt;
import org.btelman.prefs.annotation.PreferenceString;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


public class PreferenceProcessor extends AbstractProcessor {
    private static final String RANDOMIZER_SUFFIX = "_Preference";
    private static final String TARGET_STATEMENT_FORMAT = "target.%1$s = %2$s";
    private static final String CONST_PARAM_TARGET_NAME = "target";

    private static final char CHAR_DOT = '.';

    private static final List<Class<? extends Annotation>> RANDOMIZER_TYPES
            = Arrays.asList(PreferenceBool.class, PreferenceEnum.class, PreferenceInt.class, PreferenceString.class);

    private Messager messager;
    private Types typesUtil;
    private Elements elementsUtil;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        typesUtil = processingEnv.getTypeUtils();
        elementsUtil = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        Map<String, List<AnnotatedPreference>> annotatedElementMap = new LinkedHashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(PreferenceBool.class)) {
            AnnotatedPreferenceBoolean prefElement = new AnnotatedPreferenceBoolean(element);
            messager.printMessage(Diagnostic.Kind.NOTE, prefElement.toString());
            if (!prefElement.isTypeValid(elementsUtil, typesUtil)) {
                messager.printMessage(Diagnostic.Kind.ERROR, prefElement.getSimpleClassName().toString() + "#"
                        + prefElement.getElementName().toString() + " is not in valid type int");
            }
            addAnnotatedElement(annotatedElementMap, prefElement);
        }

        if (annotatedElementMap.size() == 0) {
            return true;
        }

        try {
            for (Map.Entry<String, List<AnnotatedPreference>> entry : annotatedElementMap.entrySet()) {
                MethodSpec constructor = createConstructor(entry.getValue());
                TypeSpec binder = createClass(getClassName(entry.getKey()), constructor);
                JavaFile javaFile = JavaFile.builder(getPackage(entry.getKey()), binder).build();
                javaFile.writeTo(filer);
            }

        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Error on creating java file");
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : RANDOMIZER_TYPES) {
            annotations.add(annotation.getCanonicalName());
        }
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private MethodSpec createConstructor(List<AnnotatedPreference> prefElements) {
        AnnotatedPreference firstElement = prefElements.get(0);
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(firstElement.getElement().getEnclosingElement().asType()), CONST_PARAM_TARGET_NAME);
        for (int i = 0; i < prefElements.size(); i++) {
            addStatement(builder, prefElements.get(i));

        }
        return builder.build();
    }

    private void addStatement(MethodSpec.Builder builder, AnnotatedPreference randomElement) {
        builder.addStatement(String.format(
                TARGET_STATEMENT_FORMAT,
                randomElement.getElementName().toString(),
                randomElement.getDefaultValue())
        );
    }

    private TypeSpec createClass(String className, MethodSpec constructor) {
        return TypeSpec.classBuilder(className + RANDOMIZER_SUFFIX)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .build();
    }

    private String getPackage(String qualifier) {
        return qualifier.substring(0, qualifier.lastIndexOf(CHAR_DOT));
    }

    private String getClassName(String qualifier) {
        return qualifier.substring(qualifier.lastIndexOf(CHAR_DOT) + 1);
    }

    private void addAnnotatedElement(Map<String, List<AnnotatedPreference>> map, AnnotatedPreference randomElement) {
        String qualifier = randomElement.getQualifiedClassName().toString();
        if (map.get(qualifier) == null) {
            map.put(qualifier, new ArrayList<AnnotatedPreference>());
        }
        map.get(qualifier).add(randomElement);
    }
}
