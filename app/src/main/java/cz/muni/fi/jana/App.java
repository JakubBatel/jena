package cz.muni.fi.jana;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.JSONException;
import org.json.JSONObject;
import cz.muni.fi.jana.runner.CodeAnalysisRunner;
import cz.muni.fi.jana.runner.CodeAnalysisRunnerConfig;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class App {

    public static void main(String[] args) {
        ArgumentParser argumentParser = initArgumentParser();

        try {
            Namespace argNamespace = argumentParser.parseArgs(args);

            CodeAnalysisRunnerConfig config = new CodeAnalysisRunnerConfig();

            final String configPathStr = argNamespace.getString("config");
            if (configPathStr != null) {
                Path configPath = Path.of(configPathStr);
                try {
                    String jsonString = Files.readString(configPath);
                    JSONObject json = new JSONObject(jsonString);
                    config.syncWithJSON(json);
                } catch (IOException | JSONException e) {
                    System.err.println("WARN - failed to read config file");
                }
            }

            config.syncWithCommandLineParameters(argNamespace);

            CodeAnalysisRunner runner = new CodeAnalysisRunner(config);

            runner.run();
        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
        }
    }

    private static ArgumentParser initArgumentParser() {
        ArgumentParser argumentParser =
                ArgumentParsers.newFor("jana").build().description("Analyze Enterprise Java project.");
        argumentParser.addArgument("--project-root").type(String.class).required(true).dest("root");
        argumentParser.addArgument("--exclude").nargs("+").type(String.class).dest("exclude");
        argumentParser.addArgument("--only-types").nargs("+").type(String.class).dest("only-types");
        argumentParser.addArgument("--minified").nargs("?").type(Boolean.class).setConst(true)
                .dest("minified");
        argumentParser.addArgument("--include-context").nargs("?").type(Boolean.class)
                .setConst(true).dest("include-context");
        argumentParser.addArgument("--output").type(String.class).dest("output");
        argumentParser.addArgument("--config").type(String.class).dest("config");
        return argumentParser;
    }

}
