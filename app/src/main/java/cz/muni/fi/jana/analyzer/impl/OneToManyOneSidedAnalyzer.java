package cz.muni.fi.jana.analyzer.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.AnalyzerResult;
import cz.muni.fi.jana.analyzer.issues.Issue;
import cz.muni.fi.jana.analyzer.issues.IssuesCodes;

public class OneToManyOneSidedAnalyzer extends Analyzer {

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        List<ClassOrInterfaceDeclaration> classes =
                compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        for (ClassOrInterfaceDeclaration classDeclaration : classes) {
            final String qualifiedName = classDeclaration.resolve().getQualifiedName();

            final List<FieldDeclaration> oneToManyDeclarationsList = classDeclaration
                    .findAll(FieldDeclaration.class, getPredicate(MappingType.ONE_TO_MANY));
            final List<FieldDeclaration> manyToOneDeclarationsList = classDeclaration
                    .findAll(FieldDeclaration.class, getPredicate(MappingType.MANY_TO_ONE));

            analyzeOneToMany(qualifiedName, oneToManyDeclarationsList);
            analyzeManyToOne(qualifiedName, manyToOneDeclarationsList);
        }
    }

    @Override
    public AnalyzerResult getResult() {
        AnalyzerResult result = new AnalyzerResult();
        for (final var declarationsEntries : declarations.entrySet()) {
            final var key = declarationsEntries.getKey();
            final var value = declarationsEntries.getValue();

            final FieldDeclaration oneDeclaration = value.getLeft();
            final FieldDeclaration manyDeclaration = value.getRight();
            if (oneDeclaration == null) {
                final var position = manyDeclaration.getBegin();
                result.add(new Issue(IssuesCodes.ONE_SIDED_ONE_TO_MANY,
                        (position.isPresent()) ? position.get().line : null, key.getRight(),
                        manyDeclaration.toString()));
            }
            if (manyDeclaration == null) {
                final var position = oneDeclaration.getBegin();
                result.add(new Issue(IssuesCodes.ONE_SIDED_ONE_TO_MANY,
                        (position.isPresent()) ? position.get().line : null, key.getLeft(),
                        oneDeclaration.toString()));
            }
        }
        for (final var pair : missingMappedByAttributeDeclarations) {
            final FieldDeclaration declaration = pair.getRight();
            final var position = declaration.getBegin();
            result.add(new Issue(IssuesCodes.MISSING_MAPPED_BY,
                    (position.isPresent()) ? position.get().line : null, pair.getLeft(),
                    declaration.toString()));
        }
        return result;
    }

    private enum MappingType {
        ONE_TO_MANY, MANY_TO_ONE
    }

    // Map<Triple<One, mappedBy, Many>, Pair<OneFD, ManyFD>>
    private Map<Triple<String, String, String>, Pair<FieldDeclaration, FieldDeclaration>> declarations =
            new HashMap<>();

    private List<Pair<String, FieldDeclaration>> missingMappedByAttributeDeclarations =
            new ArrayList<>();

    private Predicate<FieldDeclaration> getPredicate(MappingType type) {
        switch (type) {
            case ONE_TO_MANY:
                return field -> field.getAnnotations().stream()
                        .anyMatch(annotation -> annotation.getNameAsString().equals("OneToMany"));

            case MANY_TO_ONE:
                return field -> field.getAnnotations().stream()
                        .anyMatch(annotation -> annotation.getNameAsString().equals("ManyToOne"));
            default:
                throw new RuntimeException("Unknow type.");
        }
    }

    private Optional<String> getMappedByFromOneToMany(AnnotationExpr annotation) {
        for (Node childNode : annotation.getChildNodes()) {
            if (childNode.getClass() == MemberValuePair.class) {
                MemberValuePair memberValuePair = (MemberValuePair) childNode;
                if (memberValuePair.getNameAsString().equals("mappedBy")) {
                    return Optional.of(memberValuePair.getValue().asStringLiteralExpr().getValue());
                }
            }
        }
        return Optional.empty();
    }

    private void analyzeOneToMany(String oneQualifiedName,
            List<FieldDeclaration> oneToManyDeclarationsList) {
        for (FieldDeclaration fd : oneToManyDeclarationsList) {
            final ResolvedFieldDeclaration resolvedFieldDeclaration = fd.resolve();
            final Optional<String> mappedByOptional =
                    getMappedByFromOneToMany(fd.getAnnotationByName("OneToMany").get());

            final ResolvedReferenceType resolvedCollectionType =
                    resolvedFieldDeclaration.getType().asReferenceType();
            final Optional<ResolvedType> resolvedTypeOptional = resolvedCollectionType
                    .getTypeParametersMap().stream().map(pair -> pair.b).findFirst();
            final ResolvedReferenceType resolvedType = resolvedTypeOptional.get().asReferenceType();

            if (!mappedByOptional.isPresent()) {
                missingMappedByAttributeDeclarations.add(Pair.of(oneQualifiedName, fd));
                continue;
            }

            final String mappedBy = mappedByOptional.get();
            final String manyQualifiedName = resolvedType.getQualifiedName();

            // Add field declaration to map which is used for obtaining results of analyzis
            final Triple<String, String, String> declarationsKey =
                    Triple.of(oneQualifiedName, mappedBy, manyQualifiedName);
            final Pair<FieldDeclaration, FieldDeclaration> declarationsPair =
                    declarations.get(declarationsKey);
            declarations.put(declarationsKey,
                    Pair.of(fd, (declarationsPair != null) ? declarationsPair.getRight() : null));
        }
    }

    private void analyzeManyToOne(String manyQualifiedName,
            List<FieldDeclaration> manyToOneDeclarationsList) {
        for (FieldDeclaration fd : manyToOneDeclarationsList) {
            final ResolvedFieldDeclaration resolvedFieldDeclaration = fd.resolve();
            final ResolvedReferenceType resolvedType =
                    resolvedFieldDeclaration.getType().asReferenceType();
            final String mappedBy = fd.getVariable(0).getNameAsString();
            final String oneQualifiedName = resolvedType.getQualifiedName();

            // Add field declaration to map which is used for obtaining results of analyzis
            final Triple<String, String, String> declarationsKey =
                    Triple.of(oneQualifiedName, mappedBy, manyQualifiedName);
            final Pair<FieldDeclaration, FieldDeclaration> declarationsPair =
                    declarations.get(declarationsKey);
            declarations.put(declarationsKey,
                    Pair.of((declarationsPair != null) ? declarationsPair.getLeft() : null, fd));
        }
    }
}
