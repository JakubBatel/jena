package cz.muni.fi.jana.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import org.apache.commons.lang3.tuple.Pair;
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
        final Optional<PackageDeclaration> packageDeclaration =
                compilationUnit.getPackageDeclaration();
        String packageName;
        if (packageDeclaration.isPresent()) {
            packageName = packageDeclaration.get().getNameAsString();
        } else {
            System.out.println("Warn - no package name using '(unknown)' instead");
            packageName = "(unknown)";
        }
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

    protected void filterIssues(BiPredicate<String, RawIssue> predicate) {
        Map<String, Map<String, List<RawIssue>>> filteredOuterMap = new HashMap<>();
        for (final var outerEntry : issues.entrySet()) {
            Map<String, List<RawIssue>> filteredInnerMap = new HashMap<>();
            for (final var innerEntry : outerEntry.getValue().entrySet()) {
                List<RawIssue> filteredList = new ArrayList<>();
                for (final var issue : innerEntry.getValue()) {
                    if (predicate.test(innerEntry.getKey(), issue)) {
                        filteredList.add(issue);
                    }
                }
                if (!filteredList.isEmpty()) {
                    filteredInnerMap.put(innerEntry.getKey(), filteredList);
                }
            }
            if (!filteredInnerMap.isEmpty()) {
                filteredOuterMap.put(outerEntry.getKey(), filteredInnerMap);
            }
        }
        issues = filteredOuterMap;
    }

    protected void mapIssues(UnaryOperator<Pair<String, RawIssue>> mapper) {
        Map<String, Map<String, List<RawIssue>>> mappedOuterMap = new HashMap<>();
        for (final var outerEntry : issues.entrySet()) {
            Map<String, List<RawIssue>> mappedInnerMap = new HashMap<>();
            for (final var innerEntry : outerEntry.getValue().entrySet()) {
                for (final var issue : innerEntry.getValue()) {
                    final Pair<String, RawIssue> mappedPair =
                            mapper.apply(Pair.of(innerEntry.getKey(), issue));
                    List<RawIssue> issues =
                            mappedInnerMap.getOrDefault(mappedPair.getKey(), new ArrayList<>());
                    issues.add(mappedPair.getValue());
                    mappedInnerMap.put(mappedPair.getKey(), issues);
                }
            }
            mappedOuterMap.put(outerEntry.getKey(), mappedInnerMap);
        }
        issues = mappedOuterMap;
    }

    private Map<String, Map<String, List<RawIssue>>> issues = new HashMap<>();
}
