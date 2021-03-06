package cz.muni.fi.jana.analyzer.impl;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class OldDateTimeAPIAnalyzer extends Analyzer {

    public OldDateTimeAPIAnalyzer() {
        super();
    }

    public OldDateTimeAPIAnalyzer(boolean includeContext) {
        super(includeContext);
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final String fullyQualifiedName = Analyzer.getFullyQualifiedName(compilationUnit);

        final List<ImportDeclaration> imports = compilationUnit.findAll(ImportDeclaration.class);
        for (final ImportDeclaration importDec : imports) {
            final String importName = importDec.getNameAsString();
            if ("java.util.Date".equals(importName) || "java.util.Calendar".equals(importName)
                    || "java.util.Timezone".equals(importName)) {
                addIssue(fullyQualifiedName, IssueCode.OLD_DATE_TIME_API,
                        new RawIssue(importDec));
            }
        }
    }
}
