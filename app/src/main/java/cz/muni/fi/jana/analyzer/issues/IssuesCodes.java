package cz.muni.fi.jana.analyzer.issues;

public class IssuesCodes {
    // Main categories
    private static final String GENERAL = "GENERAL:";
    private static final String PERSISTENCE = "PERSISTENCE:";
    private static final String DI = "DI:";

    private static final String DEPRECATED = GENERAL + " DEPRECATED";
    public static final String OLD_DATE_TIME_API = DEPRECATED + " - Usage of old Java DateTime API";
    private static final String UNUSED = GENERAL + " UNUSED";
    public static final String UNUSED_IMPORT = UNUSED + " - Unused import";

    private static final String COMMENTS = GENERAL + " COMMENTS";
    public static final String COMMENTED_OUT_CODE = COMMENTS + " - Commented out code";
    public static final String FIXME_COMMENT = COMMENTS + " - FIXME comment";
    public static final String TODO_COMMENT = COMMENTS + " - TODO comment";

    private static final String HIBERNATE = PERSISTENCE + " HIBERNATE";
    public static final String ONE_SIDED_ONE_TO_MANY = HIBERNATE + " - One sided @OneToMany";
    public static final String MISSING_MAPPED_BY = HIBERNATE + " - Missing mappedBy attribute";


    private static final String FRAMEWORK_COUPLING = DI + " FRAMEWORK COUPLING";
    public static final String AUTOWIRED = FRAMEWORK_COUPLING + " - @Autowired";
}
