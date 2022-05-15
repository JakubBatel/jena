package cz.muni.fi.jana.runner;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.JSONObject;
import cz.muni.fi.jana.analyzer.impl.FatDIClassAnalyzer;
import cz.muni.fi.jana.analyzer.impl.ServiceSizeAnalyzer;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import net.sourceforge.argparse4j.inf.Namespace;

public class CodeAnalysisRunnerConfig {

    public Path getProjectRootPath() {
        return projectRootPath;
    }

    public boolean getMinify() {
        return minify;
    }

    public boolean getIncludeContext() {
        return includeContext;
    }

    public Optional<String> getOutputFilePathStr() {
        return Optional.ofNullable(outputFilePathStr);
    }

    public Set<String> getExclude() {
        return exclude;
    }

    public Set<IssueCode> getIncludeIssues() {
        return includeIssues;
    }

    public int getServiceMinNumberOfMethods() {
        return serviceMinNumberOfMethods;
    }

    public int getServiceMaxNumberOfMethods() {
        return serviceMaxNumberOfMethods;
    }

    public int getMaxNumberOfInjections() {
        return maxNumberOfInjections;
    }

    public void syncWithJSON(JSONObject json) {
        if (json.has("root")) {
            setProjectRoot(json.getString("root"));
        }
        if (json.has("minify")) {
            setMinify(json.getBoolean("minify"));
        }
        if (json.has("include-context")) {
            setIncludeContext(json.optBoolean("include-context"));
        }
        if (json.has("output")) {
            setOutputPathStr(json.optString("output", null));
        }
        if (json.has("exclude")) {
            final List<String> exclude = json.getJSONArray("exclude").toList().stream()
                    .filter((val) -> val instanceof String).map((val) -> (String) val)
                    .collect(Collectors.toList());
            addExcludedFiles(exclude);

        }
        if (json.has("only-types")) {
            final List<String> onlyTypes = json.getJSONArray("only-types").toList().stream()
                    .filter((val) -> val instanceof String).map((val) -> (String) val)
                    .collect(Collectors.toList());
            setOnlyTypes(onlyTypes);
        }
        if (json.has("service-min-number-of-methods")) {
            serviceMinNumberOfMethods = json.optInt("service-min-number-of-methods",
                    ServiceSizeAnalyzer.DEFAULT_MIN_NUM_OF_METHODS);
        }
        if (json.has("service-max-number-of-methods")) {
            serviceMaxNumberOfMethods = json.optInt("service-max-number-of-methods",
                    ServiceSizeAnalyzer.DEFAULT_MAX_NUM_OF_METHODS);
        }
        if (json.has("max-number-of-injections")) {
            maxNumberOfInjections = json.optInt("max-number-of-injections",
                    FatDIClassAnalyzer.DEFAULT_MAX_NUM_OF_INJECTIONS);
        }
    }

    public void syncWithCommandLineParameters(Namespace ns) {
        final String projectRootPathStr = ns.getString("root");
        final Boolean minify = ns.getBoolean("minified");
        final Boolean includeContext = ns.getBoolean("include-context");
        final String outputPathStr = ns.getString("output");
        final List<String> exclude = ns.getList("exclude");
        final List<String> onlyTypes = ns.getList("only-types");

        setProjectRoot(projectRootPathStr);
        setMinify(minify);
        setIncludeContext(includeContext);
        setOutputPathStr(outputPathStr);
        addExcludedFiles(exclude);
        setOnlyTypes(onlyTypes);
    }

    private void setProjectRoot(String projectRootPathStr) {
        if (projectRootPathStr != null) {
            projectRootPath = FileSystems.getDefault().getPath(projectRootPathStr);
        }
    }

    private void setMinify(Boolean minify) {
        if (minify != null) {
            this.minify = minify;
        }
    }

    private void setIncludeContext(Boolean includeContext) {
        if (includeContext != null) {
            this.includeContext = includeContext;
        }
    }

    private void setOutputPathStr(String outputPathStr) {
        if (outputPathStr != null) {
            this.outputFilePathStr = outputPathStr;
        }
    }

    private void addExcludedFiles(Collection<String> excludedFiles) {
        if (excludedFiles != null) {
            exclude.addAll(excludedFiles);
        }
    }

    private void setOnlyTypes(List<String> onlyTypes) {
        if (onlyTypes == null) {
            return;
        }
        includeIssues = new HashSet<>();
        for (String code : onlyTypes) {
            switch (code) {
                case "g":
                case "G":
                    includeIssues.addAll(IssueCode.g());
                    break;
                case "p":
                case "P":
                    includeIssues.addAll(IssueCode.p());
                    break;
                case "s":
                case "S":
                    includeIssues.addAll(IssueCode.s());
                    break;
                case "d":
                case "D":
                    includeIssues.addAll(IssueCode.d());
                    break;
                case "r":
                case "R":
                    includeIssues.addAll(IssueCode.r());
                    break;
                default:
                    includeIssues.add(IssueCode.fromString(code));
                    break;
            }
        }
    }

    private Path projectRootPath;
    private boolean minify = false;
    private boolean includeContext = false;
    private String outputFilePathStr = null;
    private Set<String> exclude = new HashSet<>();
    private Set<IssueCode> includeIssues = IssueCode.all();
    private int serviceMinNumberOfMethods = ServiceSizeAnalyzer.DEFAULT_MIN_NUM_OF_METHODS;
    private int serviceMaxNumberOfMethods = ServiceSizeAnalyzer.DEFAULT_MAX_NUM_OF_METHODS;
    private int maxNumberOfInjections = FatDIClassAnalyzer.DEFAULT_MAX_NUM_OF_INJECTIONS;

}
