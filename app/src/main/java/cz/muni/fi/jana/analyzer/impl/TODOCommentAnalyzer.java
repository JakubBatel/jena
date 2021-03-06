package cz.muni.fi.jana.analyzer.impl;

import java.util.List;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.issues.RawIssue;

public class TODOCommentAnalyzer extends Analyzer {

    public TODOCommentAnalyzer() {
        super();
    }

    public TODOCommentAnalyzer(boolean includeContext) {
        super(includeContext);
    }


    @Override
    public void analyze(CompilationUnit compilationUnit) {
        final String fullyQualifiedName = Analyzer.getFullyQualifiedName(compilationUnit);

        final List<Comment> comments = compilationUnit.getAllComments();
        for (final Comment comment : comments) {
            String content = comment.getContent();
            if (content.contains(" TODO ") || content.startsWith("TODO ")) {
                addIssue(fullyQualifiedName, IssueCode.TODO_COMMENT, new RawIssue(comment));
            }
        }
    }
}
