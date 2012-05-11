import java.util.HashMap;

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

    public static final String[] TEXT_HEADERS = {READING_LIST, REQUIREMENTS, PREREQUISITES, OTHER_INFO, RESERVED_SEATS, ASSIGNMENTS};
    public static final String[] TEXT_FIELDS = {COURSE, TITLE, DIST_AREA, LOCATION, PROFESSORS, DESCRIPTION,
        READING_LIST, REQUIREMENTS, PREREQUISITES, OTHER_INFO, RESERVED_SEATS, ASSIGNMENTS}; // includes headers

    public static final int NO_ID = -1;
    public static final int ID_LENGTH = 6;

    // PDF, AUDIT
    public static final String YES = "yesokay";
    public static final String NO = "notokay";
    public static final String ONLY = "only";

    public static final String TBA = "tba";
    public static final String PM = "pm";

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
    
    public void setTime(String time) {
        if (time.toLowerCase().trim().equals(TBA)) {
            this.put(TIME, TBA);
            return;
        }
        String[] times = time.split("-");
        // Error checking: this should never occur
        if (times.length < 2) {
            this.put(TIME, TBA);
            return;
        }
        int startTime = militaryTime(times[0]);
        int endTime = militaryTime(times[1]);

        int t = startTime;
        
        // Only start on 0s and 30s.
        if (startTime % 100 < 30)
            t -= startTime % 100;
        else if (startTime % 100 > 30)
            t -= startTime % 100 - 30;
        
        StringBuilder timeString = new StringBuilder();
        while (t <= endTime) {
            timeString.append(String.format("%4d ", t)); // Leading spaces, not 0s
            if (t % 100 == 0) {
                t += 30;
            } else {
                t += 70; // Increase by 30 minutes to next hour
            }
        }
        this.put(TIME, timeString.toString());
        System.out.println(timeString.toString());
    }
    
    public static int militaryTime(String time) {
        time = time.trim().toLowerCase();
        int ans = 0;
        String[] parts = time.split(" ");
        String[] components = parts[0].split(":");
        int hour = Integer.parseInt(components[0]);
        ans += 100 * hour;
        if (components.length > 1) {
            ans += Integer.parseInt(components[1]);
        }
        if (parts.length == 1 || components.length == 1) {
            // 8 9 10 11 12 are morning searches.
            if (hour <= 7) { // evening search
                ans += 1200;
            }
        }
        else if (parts[1].equals(PM)) {
            ans += 1200;
        }
        
        ans %= 2400;
        if (ans >= 2400) ans -= 1200; // Account for 12:XXpm
        return ans;
    }
    
    public static void main(String[] args) {
        CourseDetails details = new CourseDetails();
        details.setTime("1:27 pm - 4:21 pm");
    }
}
