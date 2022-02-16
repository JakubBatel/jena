package cz.muni.fi.jana.analyzer.issues;

import java.util.Optional;
import org.json.JSONObject;

public class Issue {
    public Issue(String name, Integer lineNumber, String fullyQualifiedName, String context) {
        this.name = name;
        this.lineNumber = lineNumber;
        this.fullyQualifiedName = fullyQualifiedName;
        this.context = context;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("class", fullyQualifiedName);
        object.put("line", lineNumber);
        object.put("context", context);
        return object.toString();
    }

    public String getName() {
        return name;
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

    private final String name;
    private final Integer lineNumber;
    private final String fullyQualifiedName;
    private final String context;
}
