import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseQuery {
    
    private String textQuery;
    private String pdf; // CourseDetails: YES, NO, ONLY
    private String audit;
    private String reading;
    private String times;
    private Map<String, String> fieldQueries;
    private String queryToSearch;
    
    // QQ will we remove slashes and dashes from query before searching for terms?
    private String[] PDF = {"pdfable", "pdf", "pdfing"};
    private String[] AUDIT = {"auditable", "audit", "auditing"};
    private String[] NAUDIT = {"notauditable", "naudit", "noaudit", "nostrangers", "withoutstrangers"}; // na should be strict
    private String[] PDFONLY = {"pdfonly", "pdfonlyable"};
    private String[] NPDF = {"npdf", "nopdf", "notpdfable"};

    private static final String ANY = "any";
    
    public CourseQuery(String query) {
        fieldQueries = new HashMap<String, String>();
        textQuery = parseQuery(query);
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
        query = parseTime(query);
        query = parsePdfAudit(query);
        return query;
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
            times += String.format("%4d ", mTime);
        }
        compressSpaces(newQuery);
        return newQuery.toString();
    }
    
    private String parsePdfAudit(String query) {
        // TODO replace word NA with NOTAUDITABLE
        StringBuilder newQuery = new StringBuilder(query);                      
        StringBuilder cleanQueryBuilder = new StringBuilder();
        int[] lookup= new int[query.length()];
        int i = 0, j = 0;
        for (char c : query.toCharArray()) {
            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')) {
                cleanQueryBuilder.append(c);
                lookup[i++] = j;
            }
            j++;
        }
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
    
    public void setPDF(String status) {
        pdf = status;
    }
    
    public void setAudit(String status) {
        audit = status;
    }
    
    public void setTextField(String field, String value) {
        fieldQueries.put(field, value);
    }
    
    public void setReading(String amt) {
        // TODO magic
    }
    
    public String getQueryString() {
        String query = textQuery + " ";
        if (!pdf.equals(ANY)) {
            query += CourseDetails.PDF + ": " + pdf;
        }
        else if (!audit.equals(ANY)) {
            query += CourseDetails.AUDIT + ": " + audit;
        }
        
        if (!times.equals("")) {
            query += CourseDetails.TIME + ": " + times;
        }
        return query;
    }
    
    public String toString() {
        return queryToSearch;
    }
    
    public static void main(String[] args) {
        CourseQuery q = new CourseQuery("i want course at 4:15pm or 3 pleaseeee");
        System.out.println(q.toString());
    }
    
}
