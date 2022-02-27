package cz.muni.fi.jana.analyzer.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import cz.muni.fi.jana.analyzer.issues.IssuesCodes;

public class ConcreteClassInjectionAnalyzer extends Analyzer {

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
                result.add(new Issue(IssuesCodes.CONCRETE_CLASS_INJECTION, lineNumber,
                        fullyQualifiedName, field.toString()));
            }
        }
        return result;
    }

    private List<Pair<String, FieldDeclaration>> getInjectedFields(
            CompilationUnit compilationUnit) {
        return compilationUnit
                .findAll(FieldDeclaration.class,
                        (field) -> field.getAnnotationByName("Inject").isPresent()
                                || field.getAnnotationByName("Autowired").isPresent())
                .stream().map((field) -> {
                    try {
                        return Pair.of(
                                field.resolve().getType().asReferenceType().getQualifiedName(),
                                field);
                    } catch (UnsolvedSymbolException ex) {
                        return null;
                    }
                }).filter((resolvedField) -> resolvedField != null).collect(Collectors.toList());
    }

    private Set<String> getInterfaces(CompilationUnit compilationUnit) {
        return compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class,
                        (classOrInterface) -> classOrInterface.isInterface())
                .stream().map((interfaceDeclaration) -> {
                    try {
                        return interfaceDeclaration.resolve().getQualifiedName();
                    } catch (UnsolvedSymbolException ex) {
                        return null;
                    }
                }).filter((interfaceDeclaration) -> interfaceDeclaration != null)
                .collect(Collectors.toSet());
    }

    private Map<String, List<Pair<String, FieldDeclaration>>> injectedFields = new HashMap<>();
    private Set<String> interfaces = new HashSet<>();
}
