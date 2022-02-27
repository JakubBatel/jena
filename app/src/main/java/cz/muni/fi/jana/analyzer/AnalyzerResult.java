package cz.muni.fi.jana.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import cz.muni.fi.jana.analyzer.issues.Issue;

public class AnalyzerResult {
    @Override
    public String toString() {
        return toJSON().toString(2);
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        issues.forEach((issue) -> {
            json.append("issues", issue.toJSON());
        });
        return json;
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
