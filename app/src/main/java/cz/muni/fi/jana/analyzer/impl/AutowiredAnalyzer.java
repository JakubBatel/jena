package cz.muni.fi.jana.analyzer.impl;

import java.util.Optional;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class AutowiredAnalyzer extends Analyzer {

    public AutowiredAnalyzer() {
        super();
    }

    public AutowiredAnalyzer(boolean includeContext) {
        super(includeContext);
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final String fullyQualifiedName = Analyzer.getFullyQualifiedName(compilationUnit);

        compilationUnit.findAll(FieldDeclaration.class).forEach((fieldDeclaration) -> {
            Optional<AnnotationExpr> autowiredAnnotationOptional =
                    fieldDeclaration.getAnnotationByName("Autowired");
            if (autowiredAnnotationOptional.isPresent()) {
                addIssue(fullyQualifiedName, IssueCode.AUTOWIRED, new RawIssue(fieldDeclaration));
            }
        });
    }

}
