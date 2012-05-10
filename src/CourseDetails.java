import java.util.HashMap;

// import org.json;

public class CourseDetails extends HashMap<String, String> {

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
    
    public static final String PDF = "pdf";
    public static final String AUDIT = "audit";

    public static final String PROFESSORS = "professors";
    public static final String DESCRIPTION = "description";
    
    // HEADERS
    public static final String READING_LIST = "sample reading list";
    public static final String REQUIREMENTS = "requirements/grading";
    public static final String PREREQUISITES = "prerequisites and restrictions";
    public static final String SCHEDULE = "schedule/classroom assignment";
    public static final String OTHER_INFO = "other information";
    public static final String RESERVED_SEATS = "reserved seats";
    public static final String WEBSITE = "website";
    public static final String ASSIGNMENTS = "reading/writing assignments"; // Amount of reading here
    
    public final static String[] HEADERS = {READING_LIST, REQUIREMENTS, PREREQUISITES, 
            SCHEDULE, OTHER_INFO, RESERVED_SEATS, WEBSITE, ASSIGNMENTS};

    public static final String[] TEXT_FIELDS = {COURSE, TITLE, DIST_AREA, LOCATION, PROFESSORS, DESCRIPTION};
    public static final String[] TEXT_HEADERS = {READING_LIST, REQUIREMENTS, PREREQUISITES, OTHER_INFO, RESERVED_SEATS, ASSIGNMENTS};

    public static final int NO_ID = -1;
    public static final int ID_LENGTH = 6;

    public static final String YES = "yes";
    public static final String NO = "no";
    public static final String ONLY = "only";

    private int courseId;

    public CourseDetails() {
        super();
        courseId = NO_ID;
    }

    public int courseId() {
        if (courseId != NO_ID) return courseId;
        String url = get(COURSE_URL);
        String mark = "courseid=";
        int location;
        if (url != null) {
            location = url.indexOf(mark);
            String strId;
            if (location != -1) {
                location += mark.length();
                strId = url.substring(location, location + ID_LENGTH);
                courseId = Integer.parseInt(strId);
            }
        }
        return courseId;
    }
}
