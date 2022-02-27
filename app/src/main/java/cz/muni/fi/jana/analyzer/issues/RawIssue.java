package cz.muni.fi.jana.analyzer.issues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.github.javaparser.ast.Node;

public class RawIssue {
    public RawIssue(Node node) {
        nodes = new ArrayList<>();
        nodes.add(node);
    }

    public RawIssue(Collection<? extends Node> nodes) {
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Nodes can not be empty.");
        }
        this.nodes = new ArrayList<>(nodes);
    }

    public int getFirstLineNumber() {
        final Node node = nodes.get(0);
        return node.getBegin().get().line;
    }

    public String getContext() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Node node : nodes) {
            stringBuilder.append(node.toString());
        }
        return stringBuilder.toString();
    }
    
    public List<Node> getRawContext() {
        return Collections.unmodifiableList(nodes);
    }

    private List<Node> nodes;
}
