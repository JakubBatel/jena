package cz.muni.fi.jana.analyzer.issues;

import java.util.Set;

public enum IssueCode {
    OLD_DATE_TIME_API("G01", "Usage of old Java DateTime API"),
    UNUSED_IMPORT("G02", "Unused import"),
    UNUSED_VARIABLE("G03", "Unused variable"),
    UNUSED_ATTRIBUTE("G04", "Unused attribute"),
    COMMENTED_OUT_CODE("G05", "Commented out code"),
    FIXME_COMMENT("G06", "FIXME comment"),
    TODO_COMMENT("G07", "TODO comment"),
    ONE_SIDED_ONE_TO_MANY("P01", "One sided @OneToMany"),
    MISSING_MAPPED_BY("P02", "Missing mappedBy attribute"),
    TINY_SERVICE("S01", "Tiny service"),
    MULTI_SERVICE("S02", "Multi service"),
    AUTOWIRED("D01", "@Autowired"),
    CONCRETE_CLASS_INJECTION("D02", "Concrete class injection"),
    USELESS_INJECTION("D03", "Unused injection"),
    FAT_DI_CLASS("D04", "Fat DI class"),
    INCORRECT_REQUEST_METHOD("R01", "Incorrect request method");

    private String code;
    private String description;
    
    IssueCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getLabel() {
        return code + " - " + description;
    }

    public static IssueCode fromString(String code) {
        IssueCode[] values = IssueCode.values();
        for (final IssueCode value : values) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknow issue code " + code);
    }

    public static Set<IssueCode> g() {
        return Set.of(
            IssueCode.OLD_DATE_TIME_API,
            IssueCode.UNUSED_IMPORT,
            IssueCode.UNUSED_VARIABLE,
            IssueCode.UNUSED_ATTRIBUTE,
            IssueCode.COMMENTED_OUT_CODE,
            IssueCode.FIXME_COMMENT,
            IssueCode.TODO_COMMENT
        );
    }

    public static Set<IssueCode> p() {
        return Set.of(
            IssueCode.ONE_SIDED_ONE_TO_MANY,
            IssueCode.MISSING_MAPPED_BY
        );
    }
    
    public static Set<IssueCode> s() {
        return Set.of(
            IssueCode.TINY_SERVICE,
            IssueCode.MULTI_SERVICE
        );
    }

    public static Set<IssueCode> d() {
        return Set.of(
            AUTOWIRED,
            CONCRETE_CLASS_INJECTION,
            USELESS_INJECTION,
            FAT_DI_CLASS
        );
    }

    public static Set<IssueCode> r() {
        return Set.of(
            INCORRECT_REQUEST_METHOD
        );
    }

    public static Set<IssueCode> all() {
        return Set.of(IssueCode.values());
    }
}