package cz.muni.fi.jana.analyzer.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import org.apache.commons.lang3.tuple.Pair;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.AnalyzerResult;
import cz.muni.fi.jana.analyzer.issues.Issue;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.util.Predicates;

public class ConcreteClassInjectionAnalyzer extends Analyzer {

    public ConcreteClassInjectionAnalyzer() {
        super();
    }

    public ConcreteClassInjectionAnalyzer(boolean includeContext) {
        super(includeContext);
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final String fullyQualifiedName = Analyzer.getFullyQualifiedName(compilationUnit);
        injectedFields.put(fullyQualifiedName, getInjectedFields(compilationUnit));
        interfaces.addAll(getInterfaces(compilationUnit));
    }

    @Override
    public AnalyzerResult getResult() {
        AnalyzerResult result = new AnalyzerResult();
        for (final var entry : injectedFields.entrySet()) {
            final String fullyQualifiedName = entry.getKey();
            final List<Pair<String, FieldDeclaration>> fields = entry.getValue();
            for (final var pair : fields) {
                final String injectedType = pair.getLeft();
                if (interfaces.contains(injectedType)) {
                    continue;
                }
                final FieldDeclaration field = pair.getRight();
                final int lineNumber = field.getBegin().get().line;
                if (getIncludeContext()) {
                    result.add(new Issue(IssueCode.CONCRETE_CLASS_INJECTION, lineNumber,
                            fullyQualifiedName, field.toString()));
                } else {
                    result.add(new Issue(IssueCode.CONCRETE_CLASS_INJECTION, lineNumber,
                            fullyQualifiedName));
                }
            }
        }
        return result;
    }

    private List<Pair<String, FieldDeclaration>> getInjectedFields(
            CompilationUnit compilationUnit) {
        return compilationUnit.findAll(FieldDeclaration.class, Predicates::isInjectedField).stream()
                .map((field) -> {
                    try {
                        return Pair.of(
                                field.resolve().getType().asReferenceType().getQualifiedName(),
                                field);
                    } catch (UnsolvedSymbolException ex) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Set<String> getInterfaces(CompilationUnit compilationUnit) {
        return compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class, Predicates::isInterfaceDeclaration)
                .stream().map((interfaceDeclaration) -> {
                    try {
                        return interfaceDeclaration.resolve().getQualifiedName();
                    } catch (UnsolvedSymbolException ex) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    private Map<String, List<Pair<String, FieldDeclaration>>> injectedFields = new HashMap<>();
    private Set<String> interfaces = new HashSet<>();
}
