package cz.muni.fi.jana.analyzer.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import org.apache.commons.lang3.tuple.Pair;
import cz.muni.fi.jana.analyzer.issues.IssuesCodes;

public class UselessInjectionAnalyzer extends UnusedAttributesAnalyzer {

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        super.analyze(compilationUnit);
        filterIssues((issueName, issue) -> {
            if (!IssuesCodes.UNUSED_ATTRIBUTE.equals(issueName)) {
                return false;
            }
            final FieldDeclaration fieldDeclaration =
                    (FieldDeclaration) issue.getRawContext().get(0);
            return fieldDeclaration.getAnnotationByName("Inject").isPresent()
                    || fieldDeclaration.getAnnotationByName("Autowired").isPresent();
        });
        mapIssues((pair) -> {
            if (!IssuesCodes.UNUSED_ATTRIBUTE.equals(pair.getKey())) {
                return pair;
            }
            return Pair.of(IssuesCodes.USELESS_INJECTION, pair.getValue());
        });
    }

}
