package cz.muni.fi.jana.runner;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.impl.*;
import cz.muni.fi.jana.analyzer.issues.IssueCode;
import cz.muni.fi.jana.analyzer.util.CombinedAnalyzer;

public class CodeAnalysisRunner {

    public CodeAnalysisRunner(CodeAnalysisRunnerConfig config) {
        initParser(config.getProjectRootPath());
        exclude = config.getExclude();
        minify = config.getMinify();
        outputFilePath = config.getOutputFilePathStr();
        includeIssues = config.getIncludeIssues();
        initAnalyzers(config);
    }

    public void run() {
        final Analyzer analyzer = CombinedAnalyzer.of(analyzers);
        for (SourceRoot sr : projectRoot.getSourceRoots()) {
            try {
                // CompilationUnit == ExampleFile.java
                List<ParseResult<CompilationUnit>> parseResults = sr.tryToParse();
                for (ParseResult<CompilationUnit> parseResult : parseResults) {
                    if (!parseResult.isSuccessful()) {
                        System.err.println("WARN - failed to parse file");
                        continue;
                    }
                    CompilationUnit compilationUnit = parseResult.getResult().get();

                    final Optional<PackageDeclaration> packageDeclaration =
                            compilationUnit.getPackageDeclaration();

                    if (!packageDeclaration.isPresent()) {
                        System.err.println("Warn - no package");
                    } else {
                        final String packageName = packageDeclaration.get().getNameAsString();
                        final String className = compilationUnit.getPrimaryTypeName().get();
                        final String fullName = packageName + "." + className;

                        if (exclude.contains(fullName)) {
                            continue;
                        }
                    }

                    analyzer.analyze(compilationUnit);
                }
            } catch (IOException ex) {
                System.err.println(ex.toString());
            }
        }
        processResults(analyzer);
    }

    private void initParser(Path projectRootPath) {
        List<SourceRoot> sourceRoots =
                new SymbolSolverCollectionStrategy().collect(projectRootPath).getSourceRoots();
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        sourceRoots.forEach(sr -> typeSolver.add(new JavaParserTypeSolver(sr.getRoot())));
        ParserConfiguration parserConfig = new ParserConfiguration();
        parserConfig.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        this.projectRoot =
                new SymbolSolverCollectionStrategy(parserConfig).collect(projectRootPath);
    }

    private void initAnalyzers(CodeAnalysisRunnerConfig config) {
        final boolean includeContext = config.getIncludeContext();
        final int minNumOfMethods = config.getServiceMinNumberOfMethods();
        final int maxNumOfMethods = config.getServiceMinNumberOfMethods();
        final int maxNumOfInjections = config.getMaxNumberOfInjections();

        for (final var code : includeIssues) {
            switch (code) {
                case OLD_DATE_TIME_API:
                    analyzers.add(new OldDateTimeAPIAnalyzer(includeContext));
                    continue;
                case UNUSED_IMPORT:
                    analyzers.add(new UnusedImportsAnalyzer(includeContext));
                    continue;
                case UNUSED_VARIABLE:
                    analyzers.add(new UnusedVariablesAnalyzer(includeContext));
                    continue;
                case UNUSED_ATTRIBUTE:
                    analyzers.add(new UnusedAttributesAnalyzer(includeContext));
                    continue;
                case COMMENTED_OUT_CODE:
                    analyzers.add(new CommentedOutCodeAnalyzer(includeContext));
                    continue;
                case FIXME_COMMENT:
                    analyzers.add(new FIXMECommentAnalyzer(includeContext));
                    continue;
                case TODO_COMMENT:
                    analyzers.add(new TODOCommentAnalyzer(includeContext));
                    continue;
                case ONE_SIDED_ONE_TO_MANY:
                case MISSING_MAPPED_BY:
                    analyzers.add(new OneToManyOneSidedAnalyzer(includeContext));
                    continue;
                case TINY_SERVICE:
                case MULTI_SERVICE:
                    analyzers.add(new ServiceSizeAnalyzer(minNumOfMethods, maxNumOfMethods,
                            includeContext));
                    continue;
                case AUTOWIRED:
                    analyzers.add(new AutowiredAnalyzer(includeContext));
                    continue;
                case CONCRETE_CLASS_INJECTION:
                    analyzers.add(new ConcreteClassInjectionAnalyzer(includeContext));
                    continue;
                case USELESS_INJECTION:
                    analyzers.add(new UselessInjectionAnalyzer(includeContext));
                    continue;
                case FAT_DI_CLASS:
                    analyzers.add(new FatDIClassAnalyzer(maxNumOfInjections, includeContext));
                    continue;
                case INCORRECT_REQUEST_METHOD:
                    analyzers.add(new RESTRequestMethodAnalyzer(includeContext));
                    continue;
            }
        }
    }

    private void processResults(Analyzer analyzer) {
        final var result = analyzer.getResult();
        result.filterIssues((issue) -> includeIssues.contains(issue.getCode()));
        final String resultStr = result.toJSON().toString((minify) ? 0 : 2);

        if (outputFilePath.isEmpty()) {
            System.out.println(resultStr);
        } else {
            try (final var fileStream = new FileOutputStream(outputFilePath.get());
                    final var streamWriter =
                            new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
                    final var writer = new PrintWriter(streamWriter)) {
                writer.println(resultStr);
            } catch (FileNotFoundException e) {
                System.out.println("ERROR - can not write to output file '" + outputFilePath + "'");
            } catch (IOException e) {
                System.err.println("WARN - failed to close output stream");
            }
        }
    }

    private ProjectRoot projectRoot;
    private boolean minify;
    private Optional<String> outputFilePath = Optional.empty();
    private Set<IssueCode> includeIssues;
    private Set<String> exclude = new HashSet<>();
    private Set<Analyzer> analyzers = new HashSet<>();

}
