package cz.muni.fi.jana.analyzer.util;

import java.util.Set;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;

/**
 * Detects whether a Lombok library was used in the given compilation unit.
 */
public class LombokDetector {

    /**
     * Verifies if the given class/interface uses Lombok library (has @Data or @Value annotation).
     * @param compilationUnit Compilation unit that contains the declaration of the class/interface.
     * @param classOrInterfaceDeclaration Class/Interface declaration that will be analyzed.
     * @return True if the class uses Lombok, false otherwise.
     */
    public static boolean usesLombok(CompilationUnit compilationUnit,
            ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        final Set<String> lombokImports = getLombokImports(compilationUnit);
        if (lombokImports.isEmpty()) {
            return false;
        }
        return containsAnnotation("Data", classOrInterfaceDeclaration, lombokImports)
                || containsAnnotation("Value", classOrInterfaceDeclaration, lombokImports);
    }

    /**
     * Verifies if the given field uses Lombok library (has @Getter annotation).
     * @param compilationUnit Compilation unit that contains the declaration of the field declaration.
     * @param fieldDeclaration Field declaration that will be analyzed.
     * @return True if the field uses Lombok, false otherwise.
     */
    public static boolean usesLombok(CompilationUnit compilationUnit,
            FieldDeclaration fieldDeclaration) {
        final Set<String> lombokImports = getLombokImports(compilationUnit);
        if (lombokImports.isEmpty()) {
            return false;
        }
        return containsAnnotation("Getter", fieldDeclaration, lombokImports);
    }

    private static Set<String> getLombokImports(CompilationUnit compilationUnit) {
        return compilationUnit.findAll(ImportDeclaration.class, (importDeclaration) -> {
            final var name = importDeclaration.getNameAsString();
            return name.startsWith("lombok");
        }).stream()
                .map((importDeclaration) -> importDeclaration.getNameAsString()
                        + ((importDeclaration.isAsterisk()) ? ".*" : ""))
                .collect(Collectors.toSet());
    }

    private static boolean containsAnnotation(String annotationName,
            NodeWithAnnotations<? extends Node> node, Set<String> lombokImports) {
        if (!node.getAnnotationByName(annotationName).isPresent()) {
            return false;
        }
        switch (annotationName) {
            case "Getter":
            case "Setter":
            case "Data":
                return lombokImports.contains("lombok." + annotationName)
                        || lombokImports.contains("lombok.*");
            case "Value":
                return lombokImports.contains("lombok.experimental." + annotationName)
                        || lombokImports.contains("lombok.experimental.*");
            default:
                return false;
        }
    }
}
