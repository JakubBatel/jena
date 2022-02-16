package cz.muni.fi.jana.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import cz.muni.fi.jana.analyzer.issues.Issue;

public class AnalyzerResult {
    @Override
    public String toString() {
        return issues.toString();
    }

    public List<Issue> getIssues() {
        return Collections.unmodifiableList(issues);
    }

    public void add(Issue issue) {
        issues.add(issue);
    }

    public void add(AnalyzerResult result) {
        this.issues.addAll(result.getIssues());
    }

    private List<Issue> issues = new ArrayList<>();
}
