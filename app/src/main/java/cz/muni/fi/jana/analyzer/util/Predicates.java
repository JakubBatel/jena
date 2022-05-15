package cz.muni.fi.jana.analyzer.util;

import java.util.Optional;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;

public class Predicates {
    public static boolean isInjectedField(FieldDeclaration fieldDeclaration) {
        final Optional<AnnotationExpr> injectAnnotation =
                fieldDeclaration.getAnnotationByName("Inject");
        final Optional<AnnotationExpr> autowiredAnnotation =
                fieldDeclaration.getAnnotationByName("Autowired");
        return injectAnnotation.isPresent() || autowiredAnnotation.isPresent();
    }

    public static boolean isClassDeclaration(ClassOrInterfaceDeclaration declaration) {
        return !declaration.isInterface();
    }

    public static boolean isInterfaceDeclaration(ClassOrInterfaceDeclaration declaration) {
        return declaration.isInterface();
    }
}
