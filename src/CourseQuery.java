import java.util.HashMap;
import java.util.Map;

public class CourseQuery {
    
    private String textQuery;
    private String pdf; // CourseDetails: YES, NO, ONLY
    private String audit;
    private String reading;
    private Map<String, String> fieldQueries;
    
    private static final String ANY = "any";
    
    public CourseQuery(String query) {
        fieldQueries = new HashMap<String, String>();
        textQuery = query;
        pdf = ANY;
        audit = ANY;
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
        String query = textQuery;
        // TODO construct query
        return query;
    }
}
