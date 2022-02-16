package cz.muni.fi.jana.analyzer.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssuesCodes;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class UnusedImportsAnalyzer extends Analyzer {

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        final String className = compilationUnit.getPrimaryTypeName().get();
        final String fullyQualifiedName = packageName + "." + className;

        final List<ImportDeclaration> imports = compilationUnit.findAll(ImportDeclaration.class);

        addTypes(compilationUnit);
        analyzeImports(fullyQualifiedName, imports);
    }

    private void addTypes(CompilationUnit compilationUnit) {
        usedTypes = new HashSet<>();
        compilationUnit.findAll(ClassOrInterfaceType.class).forEach((type) -> {
            try {
                final var resolvedType = type.resolve();
                usedTypes.add(resolvedType.getQualifiedName());
            } catch (UnsolvedSymbolException | UnsupportedOperationException ex) {
                usedTypes.add(type.getNameAsString());
            }
        });
        compilationUnit.findAll(FieldAccessExpr.class).forEach((fieldAccessExpr) -> {
            fieldAccessExpr.getChildNodes().stream().filter(NameExpr.class::isInstance)
                    .map(NameExpr.class::cast).forEach((nameExpr) -> {
                        usedTypes.add(nameExpr.getNameAsString());
                    });
        });
        compilationUnit.findAll(MethodCallExpr.class).forEach((methodCallExpr) -> {
            methodCallExpr.getChildNodes().stream().filter(NameExpr.class::isInstance)
                    .map(NameExpr.class::cast).forEach((nameExpr) -> {
                        usedTypes.add(nameExpr.getNameAsString());
                    });
        });
        compilationUnit.findAll(AnnotationExpr.class).forEach((annotation) -> {
            try {
                final var resolvedAnnotation = annotation.resolve();
                usedTypes.add(resolvedAnnotation.getQualifiedName());
            } catch (UnsolvedSymbolException | UnsupportedOperationException ex) {
                usedTypes.add(annotation.getNameAsString());
            }
        });
    }

    private void analyzeImports(String fullyQualifiedName, List<ImportDeclaration> imports) {
        for (final ImportDeclaration importDec : imports) {
            final Name name = importDec.getName();
            if (importDec.isAsterisk()) {
                continue;
            }
            final String fullName = name.asString();
            final String identifier = name.getIdentifier();
            if (usedTypes.contains(fullName)) {
                continue;
            }
            if (usedTypes.contains(identifier)) {
                continue;
            }
            addIssue(fullyQualifiedName, IssuesCodes.UNUSED_IMPORT, new RawIssue(importDec));
        }
    }

    private Set<String> usedTypes;
}
