package cz.muni.fi.jana.analyzer.issues;

import java.util.Optional;
import org.json.JSONObject;

public class Issue {
    public Issue(IssueCode code, Integer lineNumber, String fullyQualifiedName) {
        this(code, lineNumber, fullyQualifiedName, null);
    }

    public Issue(IssueCode code, Integer lineNumber, String fullyQualifiedName, String context) {
        this.code = code;
        this.lineNumber = lineNumber;
        this.fullyQualifiedName = fullyQualifiedName;
        this.context = context;
    }

    @Override
    public String toString() {
        return toJSON().toString(2);
    }

    public IssueCode getCode() {
        return code;
    }

    public String getName() {
        return code.getLabel();
    }

    public Optional<Integer> getLineNumber() {
        return Optional.ofNullable(lineNumber);
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getContext() {
        return context;
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", code.getLabel());
        json.put("class", fullyQualifiedName);
        json.put("line", lineNumber);
        if (context != null) {
            json.put("context", context);
        }
        return json;
    }

    private final IssueCode code;
    private final Integer lineNumber;
    private final String fullyQualifiedName;
    private final String context;
}
