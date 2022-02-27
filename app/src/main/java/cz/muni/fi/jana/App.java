package cz.muni.fi.jana;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.impl.*;
import cz.muni.fi.jana.analyzer.util.CombinedAnalyzer;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class App {

    public static void main(String[] args) {
        ArgumentParser argumentParser =
                ArgumentParsers.newFor("jana").build().description("Analyze Java project.");
        argumentParser.addArgument("--project-root").type(String.class).required(true).dest("root");
        argumentParser.addArgument("--exclude").nargs("+").type(String.class).dest("exclude");

        try {
            Namespace argNamespace = argumentParser.parseArgs(args);
            final List<String> exclude = argNamespace.getList("exclude");
            final Set<String> excludeSet =
                    (exclude != null) ? new HashSet<>(exclude) : new HashSet<>();
            final Path projectRootPath =
                    FileSystems.getDefault().getPath(argNamespace.getString("root"));
            final Analyzer analyzer = CombinedAnalyzer.of(new AutowiredAnalyzer(),
                    new OneToManyOneSidedAnalyzer(), new CommentedOutCodeAnalyzer(),
                    new TODOCommentAnalyzer(), new FIXMECommentAnalyzer(),
                    new OldDateTimeAPIAnalyzer(), new UnusedImportsAnalyzer(),
                    new UnusedVariablesAnalyzer(), new UnusedAttributesAnalyzer(),
                    new ServiceSizeAnalyzer(4, 16), new RESTRequestMethodAnalyzer(),
                    new ConcreteClassInjectionAnalyzer(), new UselessInjectionAnalyzer());

            CodeAnalyzisRunner runner =
                    new CodeAnalyzisRunner(projectRootPath, excludeSet, analyzer);
            runner.run();
        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
        }
    }
}
