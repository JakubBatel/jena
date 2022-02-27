package cz.muni.fi.jana.analyzer.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssuesCodes;
import cz.muni.fi.jana.analyzer.issues.RawIssue;
import cz.muni.fi.jana.analyzer.util.LombokDetector;

public class UnusedAttributesAnalyzer extends Analyzer {

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final List<ClassOrInterfaceDeclaration> classes =
                compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        for (final var classOrInterfaceDec : classes) {
            if (classOrInterfaceDec.isInterface()
                    || LombokDetector.usesLombok(compilationUnit, classOrInterfaceDec)) {
                continue;
            }

            final String fullyQualifiedName = classOrInterfaceDec.resolve().getQualifiedName();

            final Set<String> usedNames = getUsedNames(classOrInterfaceDec);

            classOrInterfaceDec
                    .findAll(FieldDeclaration.class, (fieldDeclaration) -> fieldDeclaration
                            .getAccessSpecifier() == AccessSpecifier.PRIVATE
                            && !LombokDetector.usesLombok(compilationUnit, fieldDeclaration))
                    .forEach((fieldDeclaration) -> {
                        final List<String> variablesNames = fieldDeclaration.getVariables().stream()
                                .map(VariableDeclarator::getNameAsString)
                                .collect(Collectors.toList());
                        if (!usedNames.containsAll(variablesNames)) {
                            addIssue(fullyQualifiedName, IssuesCodes.UNUSED_ATTRIBUTE,
                                    new RawIssue(fieldDeclaration));
                        }
                    });
        }
    }

    private Set<String> getUsedNames(ClassOrInterfaceDeclaration classDeclaration) {
        Set<String> usedNames = new HashSet<>();
        final List<NameExpr> nameExpressions = classDeclaration.findAll(NameExpr.class);
        for (final var nameExpr : nameExpressions) {
            try {
                final ResolvedValueDeclaration resolvedValueDec = nameExpr.resolve();
                if (resolvedValueDec.isField()) {
                    usedNames.add(resolvedValueDec.asField().getName());
                }
            } catch (UnsolvedSymbolException | UnsupportedOperationException ex) {
                continue;
            }
        }
        return usedNames;
    }
}
