package cz.muni.fi.jana.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import org.apache.commons.lang3.tuple.Pair;
import cz.muni.fi.jana.analyzer.issues.Issue;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

/**
 * Abstract class representing a general Anti-pattern Analyzer.
 * 
 * The subclass should override analyze method and use method addIssue
 * for adding issues that were found. Those issues are then used to build
 * the analysis result. In case a special logic for building the result
 * (e.g., combining multiple issues together, filtering issues or altering
 * issues in any way) is required than the getResults methods have to be override.
 */
public abstract class Analyzer {

    public Analyzer() {
        this(false);
    }

    public Analyzer(boolean includeContext) {
        this.includeContext = includeContext;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Analyzer)) {
            return false;
        }
        Analyzer analyzer = (Analyzer) other;
        return getClass().equals(analyzer.getClass()) && includeContext == analyzer.includeContext;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), includeContext);
    }

    /**
     * Perform the analysis on the given compilation unit.
     * 
     * @param compilationUnit Compilation unit that should be analyzed.
     */
    public abstract void analyze(CompilationUnit compilationUnit);

    /**
     * Build the analysis results from all the previously analyzed compilation units.
     * 
     * @return The analysis result containing all the issues found so far.
     */
    public AnalyzerResult getResult() {
        AnalyzerResult result = new AnalyzerResult();
        for (final var entry : issues.entrySet()) {
            final String className = entry.getKey();
            for (final var subEntry : entry.getValue().entrySet()) {
                final IssueCode code = subEntry.getKey();
                for (final RawIssue issue : subEntry.getValue()) {
                    if (getIncludeContext()) {
                        result.add(new Issue(code, issue.getFirstLineNumber(), className,
                                issue.getContext()));
                    } else {
                        result.add(new Issue(code, issue.getFirstLineNumber(), className));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Convenience method for obtaining the fully qualified name of the compilation unit.
     * 
     * @param compilationUnit Compilation unit to obtain name from.
     * @return Fully qualified name of the compilation unit.
     */
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

    /**
     * Add the detected issue into the list of the detected issues.
     * @param fullyQualifiedName Fully qualified name of the class that contains the issue.
     * @param code Code of the issue that identifies the anti-pattern
     * @param issue The issue data like, context, position in file, etc.
     */
    protected void addIssue(String fullyQualifiedName, IssueCode code, RawIssue issue) {
        Map<IssueCode, List<RawIssue>> issuesOfClass =
                issues.getOrDefault(fullyQualifiedName, new HashMap<>());
        List<RawIssue> issuesList = issuesOfClass.getOrDefault(code, new ArrayList<>());
        issuesList.add(issue);
        issuesOfClass.put(code, issuesList);
        issues.put(fullyQualifiedName, issuesOfClass);
    }

    /**
     * Filters the collected issues based on the predicate function.
     * @param predicate
     */
    protected void filterIssues(BiPredicate<IssueCode, RawIssue> predicate) {
        Map<String, Map<IssueCode, List<RawIssue>>> filteredOuterMap = new HashMap<>();
        for (final var outerEntry : issues.entrySet()) {
            Map<IssueCode, List<RawIssue>> filteredInnerMap = new HashMap<>();
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

    /**
     * Map the collected issues based on the mapper function.
     * @param mapper
     */
    protected void mapIssues(UnaryOperator<Pair<IssueCode, RawIssue>> mapper) {
        Map<String, Map<IssueCode, List<RawIssue>>> mappedOuterMap = new HashMap<>();
        for (final var outerEntry : issues.entrySet()) {
            Map<IssueCode, List<RawIssue>> mappedInnerMap = new HashMap<>();
            for (final var innerEntry : outerEntry.getValue().entrySet()) {
                for (final var issue : innerEntry.getValue()) {
                    final Pair<IssueCode, RawIssue> mappedPair =
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

    protected boolean getIncludeContext() {
        return includeContext;
    }

    private boolean includeContext;
    private Map<String, Map<IssueCode, List<RawIssue>>> issues = new HashMap<>();
}
