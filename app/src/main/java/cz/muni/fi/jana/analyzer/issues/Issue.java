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
        return toJSON().toString(2);
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

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("class", fullyQualifiedName);
        json.put("line", lineNumber);
        json.put("context", context);
        return json;
    }

    private final String name;
    private final Integer lineNumber;
    private final String fullyQualifiedName;
    private final String context;
}
