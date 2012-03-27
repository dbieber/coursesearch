import java.util.HashMap;

// import org.json;

public class CourseSummary extends HashMap<String, String> {
    
    private static final long serialVersionUID = 1L;

    public static final String CLASS_NUM = "classNum";
    public static final String COURSE = "course";
    public static final String TITLE = "title";
    public static final String DIST_AREA = "distArea";
    public static final String SECTION = "sect";
    public static final String DAYS = "days";
    public static final String TIME = "time";
    public static final String LOCATION = "location";
    public static final String ENROLLED = "enrl";
    public static final String MAX = "max";
    public static final String STATUS = "status";
    public static final String COURSE_URL = "courseURL";
    public static final String BOOKS_URL = "booksURL";
    public static final String EVAL_URL = "evalURL";
    
    public static final String PROFESSORS = "professors";
    public static final String READING_LIST = "reading_list";
    public static final String DESCRIPTION = "description";

    public CourseSummary() {
        super();
    }

    public CourseSummary(String summary) {
        super();
    }
}
