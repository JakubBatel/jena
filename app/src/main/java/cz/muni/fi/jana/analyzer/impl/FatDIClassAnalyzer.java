package cz.muni.fi.jana.analyzer.impl;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;
import cz.muni.fi.jana.analyzer.util.Predicates;

public class FatDIClassAnalyzer extends Analyzer {
    public static final int DEFAULT_MAX_NUM_OF_INJECTIONS = 6;

    public FatDIClassAnalyzer() {
        this(DEFAULT_MAX_NUM_OF_INJECTIONS);
    }

    public FatDIClassAnalyzer(boolean includeContext) {
        this(DEFAULT_MAX_NUM_OF_INJECTIONS, includeContext);
    }

    public FatDIClassAnalyzer(int maxNumOfInjections) {
        super();
        this.maxNumOfInjections = maxNumOfInjections;
    }

    public FatDIClassAnalyzer(int maxNumOfInjections, boolean includeContext) {
        super(includeContext);
        this.maxNumOfInjections = maxNumOfInjections;
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        compilationUnit.findAll(ClassOrInterfaceDeclaration.class, Predicates::isClassDeclaration)
                .forEach((classDeclaration) -> {
                    final List<FieldDeclaration> injectedFields = classDeclaration
                            .findAll(FieldDeclaration.class, Predicates::isInjectedField);
                    if (injectedFields.size() > maxNumOfInjections) {
                        addIssue(
                                classDeclaration.getFullyQualifiedName()
                                        .orElse(classDeclaration.getNameAsString()),
                                IssueCode.FAT_DI_CLASS, new RawIssue(compilationUnit));
                    }
                });
    }

    private final int maxNumOfInjections;
}
