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
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;
import cz.muni.fi.jana.analyzer.util.LombokDetector;
import cz.muni.fi.jana.analyzer.util.Predicates;

public class UnusedAttributesAnalyzer extends Analyzer {

    public UnusedAttributesAnalyzer() {
        super();
    }

    public UnusedAttributesAnalyzer(boolean includeContext) {
        super(includeContext);
    }
    
    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final List<ClassOrInterfaceDeclaration> classDeclarations = compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class, Predicates::isClassDeclaration);
        for (final var classDeclaration : classDeclarations) {
            if (LombokDetector.usesLombok(compilationUnit, classDeclaration)) {
                continue;
            }

            final String fullyQualifiedName = classDeclaration.resolve().getQualifiedName();

            final Set<String> usedNames = getUsedNames(classDeclaration);

            classDeclaration
                    .findAll(FieldDeclaration.class, (fieldDeclaration) -> fieldDeclaration
                            .getAccessSpecifier() == AccessSpecifier.PRIVATE
                            && !LombokDetector.usesLombok(compilationUnit, fieldDeclaration))
                    .forEach((fieldDeclaration) -> {
                        final List<String> variablesNames = fieldDeclaration.getVariables().stream()
                                .map(VariableDeclarator::getNameAsString)
                                .collect(Collectors.toList());
                        if (!usedNames.containsAll(variablesNames)) {
                            addIssue(fullyQualifiedName, IssueCode.UNUSED_ATTRIBUTE,
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
