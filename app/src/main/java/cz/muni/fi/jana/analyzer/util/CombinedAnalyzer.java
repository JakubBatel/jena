package cz.muni.fi.jana.analyzer.util;

import java.util.Arrays;
import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.AnalyzerResult;
import cz.muni.fi.jana.analyzer.impl.*;

public class CombinedAnalyzer extends Analyzer {

    private CombinedAnalyzer(Collection<Analyzer> analyzers) {
        this.analyzers = analyzers;
    }

    public static CombinedAnalyzer of(Analyzer... analyzers) {
        return new CombinedAnalyzer(Arrays.asList(analyzers));
    }

    public static CombinedAnalyzer of(Collection<Analyzer> analyzers) {
        return new CombinedAnalyzer(analyzers);
    }

    public static CombinedAnalyzer g(boolean includeContext) {
        return CombinedAnalyzer.of(
                new OldDateTimeAPIAnalyzer(includeContext),
                new UnusedImportsAnalyzer(includeContext),
                new UnusedVariablesAnalyzer(includeContext),
                new UnusedAttributesAnalyzer(includeContext),
                new CommentedOutCodeAnalyzer(includeContext),
                new FIXMECommentAnalyzer(includeContext),
                new TODOCommentAnalyzer(includeContext)
        );
    }

    public static CombinedAnalyzer p(boolean includeContext) {
        return CombinedAnalyzer.of(
                new OneToManyOneSidedAnalyzer(includeContext)
        );
    }

    public static CombinedAnalyzer s(boolean includeContext) {
        return CombinedAnalyzer.of(
                new ServiceSizeAnalyzer(includeContext)
        );
    }

    public static CombinedAnalyzer d(boolean includeContext) {
        return CombinedAnalyzer.of(
                new AutowiredAnalyzer(includeContext),
                new ConcreteClassInjectionAnalyzer(includeContext),
                new UselessInjectionAnalyzer(includeContext),
                new FatDIClassAnalyzer(includeContext)
        );
    }

    public static CombinedAnalyzer r(boolean includeContext) {
        return CombinedAnalyzer.of(
                new RESTRequestMethodAnalyzer(includeContext)
        );
    }

    @Override
    public void analyze(CompilationUnit compilationUnit) {
        for (final Analyzer analyzer : analyzers) {
            analyzer.analyze(compilationUnit);
        }
    }

    @Override
    public AnalyzerResult getResult() {
        AnalyzerResult result = new AnalyzerResult();
        for (final Analyzer analyzer : analyzers) {
            result.add(analyzer.getResult());
        }
        return result;
    }


    private final Collection<Analyzer> analyzers;
}
