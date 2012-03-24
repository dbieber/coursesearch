import java.util.ArrayList;

public class RegistrarScraper {
    
    String URL;
    String filename;
    ArrayList<CourseSummary> summaries;
    
    public void scrape(String URL) {
        this.URL = URL;
        
    }
    
    public void load(String filename) {
        
    }
    
    public void dump(String filename) {
        
    }
    
    public Object courseList() {
        return summaries;
    }
    
    public Object courseDetails(String course) {
        return null;
    }
    
    public static void main() {
        String URL = "http://registrar.princeton.edu/course-offerings/search_results.xml?submit=Search&term=1124&coursetitle=&instructor=&distr_area=&level=&cat_number=&sort=SYN_PS_PU_ROXEN_SOC_VW.SUBJECT%2C+SYN_PS_PU_ROXEN_SOC_VW.CATALOG_NBR%2CSYN_PS_PU_ROXEN_SOC_VW.CLASS_SECTION%2CSYN_PS_PU_ROXEN_SOC_VW.CLASS_MTG_NBR";
        RegistrarScraper rs = new RegistrarScraper();
        rs.scrape(URL);
    }
}
