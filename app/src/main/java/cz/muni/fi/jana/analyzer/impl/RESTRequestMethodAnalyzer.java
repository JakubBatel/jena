package cz.muni.fi.jana.analyzer.impl;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class RESTRequestMethodAnalyzer extends Analyzer {

    public RESTRequestMethodAnalyzer() {
        super();
    }

    public RESTRequestMethodAnalyzer(boolean includeContext) {
        super(includeContext);
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final String fullyQualifiedName = Analyzer.getFullyQualifiedName(compilationUnit);
        compilationUnit
                .findAll(MethodDeclaration.class,
                        (method) -> method.getAnnotationByName("RequestMapping").isPresent())
                .forEach((method) -> {
                    final AnnotationExpr annotation =
                            method.getAnnotationByName("RequestMapping").get();
                    final List<MemberValuePair> httpMethodList =
                            annotation.findAll(MemberValuePair.class,
                                    (pair) -> pair.getNameAsString().equals("method"));

                    CRUDActionType requestMethodActionType;
                    if (httpMethodList.isEmpty()) {
                        requestMethodActionType = CRUDActionType.READ;
                    } else {
                        final String httpMethod = httpMethodList.get(0).getValue().toString();
                        requestMethodActionType = CRUDActionType.fromRequestMethod(httpMethod);
                        if (requestMethodActionType == CRUDActionType.UNKNOWN) {
                            return;
                        }
                    }

                    final String methodName = method.getNameAsString();
                    final CRUDActionType methodActionType =
                            CRUDActionType.fromMethodName(methodName);

                    if (methodActionType != CRUDActionType.UNKNOWN
                            && methodActionType != requestMethodActionType) {
                        addIssue(fullyQualifiedName, IssueCode.INCORRECT_REQUEST_METHOD,
                                new RawIssue(method));
                    }
                });
    }

    private enum CRUDActionType {
        CREATE, READ, UPDATE, DELETE, UNKNOWN;

        public static CRUDActionType fromRequestMethod(String method) {
            switch (method) {
                case "RequestMethod.POST":
                    return CRUDActionType.CREATE;
                case "RequestMethod.GET":
                    return CRUDActionType.READ;
                case "RequestMethod.PUT":
                    return CRUDActionType.UPDATE;
                case "RequestMethod.DELETE":
                    return CRUDActionType.DELETE;
                default:
                    return CRUDActionType.UNKNOWN;
            }
        }

        public static CRUDActionType fromMethodName(String name) {
            if (name.startsWith("create")) {
                return CRUDActionType.CREATE;
            }
            if (name.startsWith("get") || name.startsWith("find")) {
                return CRUDActionType.READ;
            }
            if (name.startsWith("update")) {
                return CRUDActionType.UPDATE;
            }
            if (name.startsWith("delete")) {
                return CRUDActionType.DELETE;
            }
            return CRUDActionType.UNKNOWN;
        }
    }


}
