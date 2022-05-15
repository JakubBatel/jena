package cz.muni.fi.jana.analyzer.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.apache.commons.lang3.tuple.Pair;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.util.Predicates;

public class UselessInjectionAnalyzer extends UnusedAttributesAnalyzer {

    public UselessInjectionAnalyzer() {
        super();
    }

    public UselessInjectionAnalyzer(boolean includeContext) {
        super(includeContext);
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        super.analyze(compilationUnit);
        filterIssues((code, issue) -> {
            if (code != IssueCode.UNUSED_ATTRIBUTE) {
                return false;
            }
            final FieldDeclaration fieldDeclaration =
                    (FieldDeclaration) issue.getRawContext().get(0);
            return Predicates.isInjectedField(fieldDeclaration);
        });
        mapIssues((pair) -> {
            if (pair.getKey() != IssueCode.UNUSED_ATTRIBUTE) {
                return pair;
            }
            return Pair.of(IssueCode.USELESS_INJECTION, pair.getValue());
        });
    }

}
