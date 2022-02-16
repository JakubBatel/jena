package cz.muni.fi.jana;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import cz.muni.fi.jana.analyzer.Analyzer;

public class CodeAnalyzisRunner {
    private ProjectRoot projectRoot;
    private Set<String> excluded;
    private Analyzer analyzer;

    public CodeAnalyzisRunner(Path projectRootPath, Set<String> exclude, Analyzer analyzer) {
        List<SourceRoot> sourceRoots =
                new SymbolSolverCollectionStrategy().collect(projectRootPath).getSourceRoots();
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        sourceRoots.forEach(sr -> typeSolver.add(new JavaParserTypeSolver(sr.getRoot())));
        ParserConfiguration parserConfig = new ParserConfiguration();
        parserConfig.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        this.projectRoot =
                new SymbolSolverCollectionStrategy(parserConfig).collect(projectRootPath);
        this.excluded = exclude;
        this.analyzer = analyzer;
    }

    public void run() {
        for (SourceRoot sr : projectRoot.getSourceRoots()) {
            try {
                // CompilationUnit == ExampleFile.java
                List<ParseResult<CompilationUnit>> parseResults = sr.tryToParse();
                for (ParseResult<CompilationUnit> parseResult : parseResults) {
                    CompilationUnit compilationUnit = parseResult.getResult().get();

                    final String packageName =
                            compilationUnit.getPackageDeclaration().get().getNameAsString();
                    final String className = compilationUnit.getPrimaryTypeName().get();
                    final String fullName = packageName + "." + className;

                    if (excluded.contains(fullName)) {
                        continue;
                    }

                    analyzer.analyze(compilationUnit);
                }
            } catch (IOException ex) {
                System.err.println(ex.toString());
            }
        }
        final var result = analyzer.getResult();
        System.out.println(result);
    }
}
