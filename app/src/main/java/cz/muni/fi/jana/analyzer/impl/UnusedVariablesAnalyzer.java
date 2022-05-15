package cz.muni.fi.jana.analyzer.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class UnusedVariablesAnalyzer extends Analyzer {

    public UnusedVariablesAnalyzer() {
        super();
    }

    public UnusedVariablesAnalyzer(boolean includeContext) {
        super(includeContext);
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final String fullyQualifiedName = Analyzer.getFullyQualifiedName(compilationUnit);
        compilationUnit.findAll(VariableDeclarationExpr.class).forEach((varDec) -> {
            var parent = varDec.getParentNode().get();
            if (parent instanceof ExpressionStmt) {
                parent = parent.getParentNode().get();
            }
            final List<String> variablesNames = varDec.getVariables().stream()
                    .map(VariableDeclarator::getNameAsString).collect(Collectors.toList());
            final Set<String> usedNames = parent.findAll(NameExpr.class).stream()
                    .map(NameExpr::getNameAsString).collect(Collectors.toSet());

            if (!usedNames.containsAll(variablesNames)) {
                addIssue(fullyQualifiedName, IssueCode.UNUSED_VARIABLE, new RawIssue(varDec));
            }
        });
    }

}
