package cz.muni.fi.jana.analyzer.util;

import java.util.Arrays;
import java.util.Collection;
import com.github.javaparser.ast.CompilationUnit;
import cz.muni.fi.jana.analyzer.Analyzer;
import cz.muni.fi.jana.analyzer.AnalyzerResult;

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
