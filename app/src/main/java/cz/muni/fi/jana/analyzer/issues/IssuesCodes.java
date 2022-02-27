package cz.muni.fi.jana.analyzer.issues;

public class IssuesCodes {
    public static final String OLD_DATE_TIME_API = "G01 - Usage of old Java DateTime API";
    public static final String UNUSED_IMPORT = "G02 - Unused import";
    public static final String UNUSED_VARIABLE = "G03 - Unused variable";
    public static final String UNUSED_ATTRIBUTE = "G04 - Unused attribute";
    public static final String COMMENTED_OUT_CODE = "G05 - Commented out code";
    public static final String FIXME_COMMENT = "G06 - FIXME comment";
    public static final String TODO_COMMENT = "G07 - TODO comment";
    public static final String ONE_SIDED_ONE_TO_MANY = "P01 - One sided @OneToMany";
    public static final String MISSING_MAPPED_BY = "P02 - Missing mappedBy attribute";
    public static final String TINY_SERVICE = "S01 Tiny service";
    public static final String MULTI_SERVICE = "S02 Multi service";
    public static final String AUTOWIRED = "D01 - @Autowired";
    public static final String CONCRETE_CLASS_INJECTION = "D02 - Concrete class injection";
    public static final String USELESS_INJECTION = "D03 - Unused injection";
    public static final String INCORRECT_REQUEST_METHOD = "R01 - Incorrect request method";
}
