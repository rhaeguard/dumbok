package io.rhaeguard;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(value = {"io.rhaeguard.WithTypeAlias", "io.rhaeguard.WithTypeAliases"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class WithTypeAliasProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);

            for (Element annotatedElement : annotatedElements) {

                for (WithTypeAlias anno : annotatedElement.getAnnotationsByType(WithTypeAlias.class)) {
                    List<ConstructorData> constructors = getConstructorData(anno);

                    String className = ((TypeElement) annotatedElement).getQualifiedName().toString();
                    String aliasName = anno.alias();

                    try {
                        writeToFile(className, aliasName, constructors);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }

        return true;
    }

    private List<ConstructorData> getConstructorData(WithTypeAlias anno) {
        List<ConstructorData> constructors = new ArrayList<>();

        try {
            anno.aliasFor();
        } catch (MirroredTypeException mte) {
            TypeMirror typeMirror = mte.getTypeMirror();
            Types TypeUtils = this.processingEnv.getTypeUtils();
            TypeElement typeElement = (TypeElement) TypeUtils.asElement(typeMirror);
            for (Element enclosedElement : typeElement.getEnclosedElements()) {
                if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                    ExecutableElement exec = (ExecutableElement) enclosedElement;

                    var className = typeElement.getQualifiedName().toString();
                    var modifiers = exec.getModifiers().stream().map(Modifier::toString).toList();
                    var params = exec.getParameters().stream().map(varElem -> {
                        var name = varElem.getSimpleName().toString();
                        var type = varElem.asType().toString();

                        return new ConstructorParam(name, type);
                    }).toList();

                    ConstructorData constructorData = new ConstructorData(
                            className,
                            modifiers,
                            params
                    );

                    constructors.add(constructorData);
                }
            }
        }
        return constructors;
    }

    private void writeToFile(String className, String aliasName, List<ConstructorData> constructors) throws IOException {
        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

        String classFileName = packageName + "." + aliasName;

        String targetClassName = constructors.get(0).className;

        JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(classFileName);
        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
            out.print("package ");
            out.print(packageName);
            out.println(";");

            out.print("public class ");
            out.print(aliasName);
            out.print(" extends ");
            out.print(targetClassName);
            out.println("{ ");

            for (ConstructorData constructor : constructors) {
                out.print(String.join(" ", constructor.modifiers));
                out.print(" ");
                out.print(aliasName);
                out.print("(");
                String params = constructor.params.stream().map(param -> String.format("%s %s", param.type, param.name)).collect(Collectors.joining(", "));
                out.print(params);
                out.print(")");
                out.println("{ ");
                out.print("super(");
                String paramNames = constructor.params.stream().map(param -> String.format("%s", param.name)).collect(Collectors.joining(", "));
                out.print(paramNames);
                out.print(");");
                out.println("}");
            }

            out.println("}");
        }
    }


    private record ConstructorData(String className, List<String> modifiers, List<ConstructorParam> params) {}

    private record ConstructorParam(String name, String type) {}
}