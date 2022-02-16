package cz.muni.fi.jana.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.javaparser.ast.CompilationUnit;
import cz.muni.fi.jana.analyzer.issues.Issue;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public abstract class Analyzer {

    public abstract void analyze(CompilationUnit compilationUnit);

    public AnalyzerResult getResult() {
        AnalyzerResult result = new AnalyzerResult();
        for (final var entry : issues.entrySet()) {
            final String className = entry.getKey();
            for (final var subEntry : entry.getValue().entrySet()) {
                final String issueName = subEntry.getKey();
                for (final RawIssue issue : subEntry.getValue()) {
                    result.add(new Issue(issueName, issue.getFirstLineNumber(), className,
                            issue.getContext()));
                }
            }
        }
        return result;
    }

    protected static String getFullyQualifiedName(CompilationUnit compilationUnit) {
        final String packageName = compilationUnit.getPackageDeclaration().get().getNameAsString();
        final String className = compilationUnit.getPrimaryTypeName().get();
        return packageName + "." + className;
    }

    protected void addIssue(String fullyQualifiedName, String issueName, RawIssue issue) {
        Map<String, List<RawIssue>> issuesOfClass =
                issues.getOrDefault(fullyQualifiedName, new HashMap<>());
        List<RawIssue> issuesList = issuesOfClass.getOrDefault(issueName, new ArrayList<>());
        issuesList.add(issue);
        issuesOfClass.put(issueName, issuesList);
        issues.put(fullyQualifiedName, issuesOfClass);
    }

    private Map<String, Map<String, List<RawIssue>>> issues = new HashMap<>();
}
