package cz.muni.fi.jana.analyzer.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class ServiceSizeAnalyzer extends Analyzer {
    public static final int DEFAULT_MIN_NUM_OF_METHODS = 4;
    public static final int DEFAULT_MAX_NUM_OF_METHODS = 16;

    public ServiceSizeAnalyzer() {
        this(DEFAULT_MIN_NUM_OF_METHODS, DEFAULT_MAX_NUM_OF_METHODS);
    }

    public ServiceSizeAnalyzer(boolean includeContext) {
        this(DEFAULT_MIN_NUM_OF_METHODS, DEFAULT_MAX_NUM_OF_METHODS, includeContext);
    }

    public ServiceSizeAnalyzer(int minNumOfMethods, int maxNumOfMethods) {
        super();
        initMinAndMaxAttributes(minNumOfMethods, maxNumOfMethods);
    }

    public ServiceSizeAnalyzer(int minNumOfMethods, int maxNumOfMethods, boolean includeContext) {
        super(includeContext);
        initMinAndMaxAttributes(minNumOfMethods, maxNumOfMethods);
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        compilationUnit
                .findAll(ClassOrInterfaceDeclaration.class,
                        (classOrInterfaceDeclaration) -> classOrInterfaceDeclaration
                                .getAnnotationByName("Service").isPresent())
                .forEach((serviceDeclaration) -> {
                    final String fullyQualifiedName =
                            serviceDeclaration.resolve().getQualifiedName();
                    final int numberOfMethods =
                            serviceDeclaration.findAll(MethodDeclaration.class).size();
                    if (minNumOfMethods != -1 && numberOfMethods < minNumOfMethods) {
                        addIssue(fullyQualifiedName, IssueCode.TINY_SERVICE,
                                new RawIssue(serviceDeclaration));
                    }
                    if (maxNumOfMethods != -1 && numberOfMethods > maxNumOfMethods) {
                        addIssue(fullyQualifiedName, IssueCode.MULTI_SERVICE,
                                new RawIssue(serviceDeclaration));
                    }
                });
    }

    private void initMinAndMaxAttributes(int minNumOfMethods, int maxNumOfMethods) {
        this.minNumOfMethods = minNumOfMethods;
        this.maxNumOfMethods = maxNumOfMethods;
    }

    private int minNumOfMethods;
    private int maxNumOfMethods;
}
