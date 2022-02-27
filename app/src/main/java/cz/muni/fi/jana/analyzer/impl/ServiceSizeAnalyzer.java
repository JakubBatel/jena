package cz.muni.fi.jana.analyzer.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssuesCodes;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class ServiceSizeAnalyzer extends Analyzer {

    public ServiceSizeAnalyzer(int minNumOfMethods, int maxNumOfMethods) {
        this.minNumOfMethods = minNumOfMethods;
        this.maxNumOfMethods = maxNumOfMethods;
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
                        addIssue(fullyQualifiedName, IssuesCodes.TINY_SERVICE,
                                new RawIssue(serviceDeclaration));
                    }
                    if (maxNumOfMethods != -1 && numberOfMethods > maxNumOfMethods) {
                        addIssue(fullyQualifiedName, IssuesCodes.MULTI_SERVICE,
                                new RawIssue(serviceDeclaration));
                    }
                });
    }

    private int minNumOfMethods;
    private int maxNumOfMethods;


}
