import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseQuery {
    
    private String textQuery;
    private String pdf; // CourseDetails: YES, NO, ONLY
    private String audit;
    private String distAreas;
    private String readingAmt;
    private String specialTimes;
    private String classSize;
    private String times;
    private String days;
    private String abbr;
    private Map<String, String> fieldQueries;
    private String queryToSearch;
    
    // QQ will we remove slashes and dashes from query before searching for terms?
    private String[] PDF = {"pdfable", "pdf", "pdfing", "easy"};
    private String[] AUDIT = {"auditable", "audit", "auditing"};
    private String[] NAUDIT = {"notauditable", "naudit", "noaudit", "nostrangers", "withoutstrangers", "not auditable", "no audit", "no strangers", "without strangers"}; // na should be strict
    private String[] PDFONLY = {"pdfonly", "pdfonlyable", "pdf only"};
    private String[] NPDF = {"npdf", "nopdf", "notpdfable", "npdf", "no pdf", "not pdfable"};

    private String[] DAYS = {"monday", "tuesday", "wednesday", "thursday", "friday"}; // weekend
    private String[] SPECIAL_TIMES = {"afternoon", "late afternoon", "early", "morning", "night", "noon"}; // weekend
    private String[] SPECIAL_TIME_VALS = {"1:00 pm - 4:30 pm", "3:00 pm - 8:00 pm", "7:00 am - 12:00 pm", "7:00 am - 12:00 pm", "7:00 pm - 11:00 pm", "12:00 pm - 12:30 pm"};

    private String[] DIST_AREAS = {"EM", "LA", "SA", "EC", "HA", "QR", "STN", "STL"};

    private String[] SMALL = {"small", "tiny", "personal", "one on one", "engaging", "discussion based", "close knit"};
    private String[] LARGE = {"large", "lecture", "crowded", "McCosh 50", "big"};
    
    private String[] DEPARTMENTS = {"AAS", "AFS", "AMS", "ANT", "AOS", "APC", "ARA", "ARC",
            "ART", "AST", "ATL", "BCS","CBE", "CEE", "CHI", "CHM", "CHV", "CLA", "CLG", "COM", "COS", "CWR", "CZE",
            "DAN", "EAP", "EAS", "ECO", "ECS", "EEB", "EGR", "ELE", "ENG", "ENV", "EPS", "FIN", "FRE", "FRS", "GEO",
            "GER", "GHP", "GLS", "GSS", "HEB", "HIN", "HIS", "HLS", "HOS", "HUM", "ISC", "ITA", "JDS", "JPN", "JRN",
            "KOR", "LAO", "LAS", "LAT", "LIN", "MAE", "MAT", "MED", "MOD", "MOG", "MOL", "MSE", "MUS", "NES", "NEU",
            "ORF", "PAW", "PER", "PHI", "PHY", "PLS", "POL", "POP", "POR", "PSY", "QCB", "REL",
            "RUS", "SAS", "SLA", "SOC", "SPA", "STC", "SWA", "THR", "TPP", "TRA", "TUR", "URB", "URD", "VIS", "WRI", "WWS"};
    
    private static final String ANY = "any";
    
    public CourseQuery(String query) {
        fieldQueries = new HashMap<String, String>();
        textQuery = parseQuery(query.toLowerCase().trim());
        queryToSearch = getQueryString();
    }
    
    private void spacify(StringBuilder s, int a, int b) {
        for (int i = a; i < b; i++) {
            s.setCharAt(i, ' ');
        }
    }
    
    private void compressSpaces(StringBuilder s) {
        int i = 1;
        while (i < s.length()) {
            if (s.charAt(i) == ' ' && s.charAt(i-1) == ' ') {
                s.deleteCharAt(i);
                continue;
            }
            i++;
        }
    }
    
    private String parseQuery(String query) {
        
        parseSpecialTimes(query);
        parseClassSize(query);
        query = parseDistributionAreas(query);
        query = parseReadingAmt(query);
        parseCourseAbbr(query);
        query = parseTime(query);
        query = parsePdfAudit(query);
        query = parseDays(query);
        return query;
    }

    private void parseClassSize(String query) {
        classSize = "";
        if (containsOneOf(query, SMALL) != null) {
            classSize = CourseDetails.SMALL;
        } else if (containsOneOf(query, LARGE) != null) {
            classSize = CourseDetails.LARGE;
        }
    }
    
    private String parseDistributionAreas(String query) {
        distAreas = "";
        StringBuilder newQuery = new StringBuilder(query);
        for (String distArea : DIST_AREAS) {
            Pattern p = Pattern.compile(String.format("\\b%s\\b", distArea));
            Matcher m = p.matcher(query);
            if (m.find()) {
                distAreas += distArea + " ";
                spacify(newQuery, m.start(), m.end());
            }
        }
        compressSpaces(newQuery);
        return newQuery.toString();
    }
    
    private String parseReadingAmt(String query) {
        readingAmt = "";
        StringBuilder newQuery = new StringBuilder(query);
        for (String page : CourseDetails.READPERWK) {
            Pattern p = Pattern.compile(String.format(CourseDetails.rangeRegex, page));
            Matcher m = p.matcher(query);
            while(m.find()) {
                String result = query.substring(m.start(), m.end());
                spacify(newQuery, m.start(), m.end());
                Matcher numMatcher = CourseDetails.numberPattern.matcher(result);
                int numbers[] = new int[2];
                int i = 0;
                while (numMatcher.find()) {
                    numbers[i++] = Integer.parseInt(result.substring(numMatcher.start(), numMatcher.end()));
                }
                if (i == 1) {
                    numbers[1] = numbers[0];
                }
                int pages = numbers[0] - numbers[0] % 10;
                while (pages <= numbers[1]) {
                    readingAmt += pages + " ";
                    pages += 10;
                }
            }
        }
        compressSpaces(newQuery);
        return newQuery.toString();
    }
    
    private String parseDays(String query) {
        StringBuilder newQuery = new StringBuilder(query);
        
        days = "";
        for (String day : DAYS) {
            for (int i = day.length(); i >= 1; i--) {
                Pattern p = Pattern.compile(String.format("\\b%s\\b", day.substring(0, i)));
                Matcher m = p.matcher(query);
                if (m.find()) {
                    spacify(newQuery, m.start(), m.end());
                    days += day + " ";
                    break;
                }
            }
        }
        compressSpaces(newQuery);
        return newQuery.toString();
    }
    
    private String parseTime(String query) {
        StringBuilder newQuery = new StringBuilder(query);
        Pattern time = Pattern.compile("\\b\\d\\d?(:\\d\\d)?(\\s[ap]m\\b)?");
        Matcher m = time.matcher(query);
        times = "";
        while (m.find()) {
            spacify(newQuery, m.start(), m.end());
            String militaryTime = query.substring(m.start(), m.end());
            int mTime = CourseDetails.militaryTime(militaryTime);
            // Only search on 0s and 30s.
            if (mTime % 100 < 30) {
                mTime -= mTime % 100;
            }
            else if (mTime % 100 > 30) {
                mTime -= mTime % 100 - 30;
            }
            times += CourseDetails.timeToString(mTime) + " ";
        }
        compressSpaces(newQuery);
        return newQuery.toString();
    }
    
    private void parseSpecialTimes(String query) {
        specialTimes = "";
        for (int i = 0; i < SPECIAL_TIMES.length; i++) {
            int index = query.indexOf(SPECIAL_TIMES[i]);
            if (index != -1) {
                specialTimes += CourseDetails.timesToString(SPECIAL_TIME_VALS[i]) + " ";
            }
        }
    }
    
    private void parseCourseAbbr(String query) {
        abbr = "";
        Pattern abbrPat = Pattern.compile("\\b\\w\\w\\w\\s*\\d\\d\\d\\b");
        Matcher m = abbrPat.matcher(query);
        if (m.find()) {
            abbr = query.substring(m.start(), m.start() + 3) + " " + query.substring(m.end() - 3, m.end());
            return;
        }

        Pattern deptPattern = Pattern.compile("\\b\\w\\w\\w\\b");
        Pattern courseNumPattern = Pattern.compile("\\b\\w\\w\\w\\b");
        Matcher deptMatcher = deptPattern.matcher(query);
        Matcher courseNumMatcher = courseNumPattern.matcher(query);
        while (deptMatcher.find()) {
            if (containsOneOf(query.substring(deptMatcher.start(), deptMatcher.end()), DEPARTMENTS) != null) {
                abbr = query.substring(deptMatcher.start(), deptMatcher.end()) + " ";
                break;
            }
        }
        if (courseNumMatcher.find()) {
            abbr += query.substring(courseNumMatcher.start(), courseNumMatcher.end());
        }
    }
    
    private String parsePdfAudit(String query) {
        // TODO replace word NA with NOTAUDITABLE
        StringBuilder newQuery = new StringBuilder(query);                      
        StringBuilder cleanQueryBuilder = new StringBuilder();
        int[] lookup = new int[query.length() + 1];
        int i = 0, j = 0;
        for (char c : query.toCharArray()) {
            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c == ' ') {
                cleanQueryBuilder.append(c);
                lookup[i++] = j;
            }
            j++;
        }
        lookup[i] = j;
        String cleanQuery = cleanQueryBuilder.toString();

        String[][] pdfTypes = new String[][] {PDFONLY, NPDF, PDF};
        String[] pdfValues = new String[] {CourseDetails.ONLY, CourseDetails.NO, CourseDetails.YES};
        String[][] auditTypes = new String[][] {NAUDIT, AUDIT};
        String[] auditValues = new String[] {CourseDetails.NO, CourseDetails.YES};

        pdf = ANY;
        for (int cv = 0; cv < pdfTypes.length; cv++) {
            int[] indices = containsOneOf(cleanQuery, pdfTypes[cv]);
            if (indices != null) {
                pdf = pdfValues[cv];
                spacify(newQuery, lookup[indices[0]], lookup[indices[1]]);
                break;
            }
        }

        audit = ANY;
        for (int cv = 0; cv < auditTypes.length; cv++) {
            int[] indices = containsOneOf(cleanQuery, auditTypes[cv]);
            if (indices != null) {
                audit = auditValues[cv];
                spacify(newQuery, lookup[indices[0]], lookup[indices[1]]);
                break;
            }
        }
        compressSpaces(newQuery);
        return newQuery.toString();
    }

    private int[] containsOneOf(String haystack, String[] needles) {
        for (String needle : needles) {
            int id = haystack.indexOf(needle);
            if (id != -1) {
                return new int[] {id, id + needle.length()};
            }
        }
        return null;
    }
    
    public void setTextField(String field, String value) {
        fieldQueries.put(field, value);
    }
     
    public String getQueryString() {
        String query = textQuery + " ";
        if (!pdf.equals(ANY)) {
            query += CourseDetails.PDF + ": " + pdf + " ";
        }
        else if (!audit.equals(ANY)) {
            query += CourseDetails.AUDIT + ": " + audit + " ";
        }

        if (!times.equals("")) {
            query += CourseDetails.TIME + ": " + times + " ";
        }
        if (!specialTimes.equals("")) {
            query += CourseDetails.TIME + ": " + specialTimes + " ";
        }

        if (!classSize.equals("")) {
            query += CourseDetails.SIZE + ": " + classSize + " ";
        }
        if (!distAreas.equals("")) {
            query += CourseDetails.DIST_AREA + ": " + distAreas + " ";
        }
        
        if (!days.equals("")) {
            query += CourseDetails.DAYS + ": " + days + " ";
        }
        if (!abbr.equals("")) {
            query += CourseDetails.COURSE + ": " + abbr + " ";
        }
        if (!readingAmt.equals("")) {
            query += CourseDetails.READING_AMT + ": " + readingAmt +" ";
        }
        return query;
    }
    
    public String toString() {
        return queryToSearch;
    }
    
    public static void main(String[] args) {
        CourseQuery q = new CourseQuery("pdfable");
        System.out.println(q.toString());
    }
    
}
