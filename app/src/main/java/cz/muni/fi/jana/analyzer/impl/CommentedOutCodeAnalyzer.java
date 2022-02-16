package cz.muni.fi.jana.analyzer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssuesCodes;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class CommentedOutCodeAnalyzer extends Analyzer {

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        commentGroups = new ArrayList<>();

        final List<Comment> comments = compilationUnit.getAllComments();
        groupComments(comments);

        final String fullyQualifiedName = Analyzer.getFullyQualifiedName(compilationUnit);
        analyzeGroups(fullyQualifiedName);
    }

    private List<List<Comment>> commentGroups;

    private static final List<Function<String, Object>> parserFunctions = List.of(
            StaticJavaParser::parseAnnotation, StaticJavaParser::parseAnnotationBodyDeclaration,
            StaticJavaParser::parseBlock, StaticJavaParser::parseBodyDeclaration,
            StaticJavaParser::parseExplicitConstructorInvocationStmt,
            StaticJavaParser::parseExpression, StaticJavaParser::parseImport,
            StaticJavaParser::parseModuleDeclaration, StaticJavaParser::parseModuleDirective,
            StaticJavaParser::parsePackageDeclaration, StaticJavaParser::parseStatement,
            StaticJavaParser::parseTypeDeclaration, StaticJavaParser::parseTypeParameter,
            StaticJavaParser::parseVariableDeclarationExpr);


    private void groupComments(List<Comment> comments) {
        List<Comment> wholeComment = new ArrayList<>();
        Integer previousCommentLine = null;
        for (final Comment comment : comments) {
            if (comment.isJavadocComment()) {
                continue;
            }
            final int commentLine = comment.getBegin().get().line;

            if (previousCommentLine != null && previousCommentLine != commentLine - 1) {
                commentGroups.add(wholeComment);
                wholeComment = new ArrayList<>();
            }
            wholeComment.add(comment);
            previousCommentLine = commentLine;
        }
        if (!wholeComment.isEmpty()) {
            commentGroups.add(wholeComment);
        }
    }

    private static boolean tryParse(String code, Function<String, Object> parserFunction) {
        try {
            parserFunction.apply(code);
            return true;
        } catch (ParseProblemException ex) {
            return false;
        }
    }

    private static boolean isValidJavaCode(String code) {
        for (final var f : parserFunctions) {
            final boolean result = tryParse(code, f);
            if (result) {
                return true;
            }
        }
        return false;
    }

    private static String commentGroupToString(List<Comment> group) {
        String wholeContent = "";
        for (final Comment comment : group) {
            final String content = comment.getContent();
            wholeContent += "\n" + content.strip();
        }
        return wholeContent;
    }

    private void analyzeGroups(String fullyQualifiedName) {
        for (final List<Comment> group : commentGroups) {
            final int size = group.size();
            boolean result = false;
            for (int beginning = 0; beginning < size; beginning++) {
                for (int end = size; end > beginning; end--) {
                    final List<Comment> subGroup = group.subList(beginning, end);
                    result = analyzeGroup(subGroup);
                    if (result) {
                        addIssue(fullyQualifiedName, IssuesCodes.COMMENTED_OUT_CODE,
                                new RawIssue(subGroup));
                        break;
                    }
                }
                if (result) {
                    break;
                }
            }
        }
    }

    private static boolean analyzeGroup(List<Comment> group) {
        final String content = commentGroupToString(group);
        return isValidJavaCode(content);
    }
}
