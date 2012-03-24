import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RegistrarScraper {

    private final String TABLE_TAG = "table";
    private final String TABLE_ROW_TAG = "tr";
    private final String TABLE_DATA_TAG = "td";
    private final String A_TAG = "a";
    private final String HREF_ATTR = "href";
    
    String URL;
    String filename;
    HashMap<Integer, CourseSummary> summaries; //maps classNum to courseSummary
    
    public RegistrarScraper() {
        summaries = new HashMap<Integer, CourseSummary>();
    }
    
    public void scrape(String URL) throws IOException {
        this.URL = URL;
        
        Document doc = Jsoup.connect(URL).get();
        Element table = doc.getElementsByTag(TABLE_TAG).first();
        Elements rows = table.getElementsByTag(TABLE_ROW_TAG);
        rows.remove(0); // first row is bogus
        for (Element row : rows) {
            Elements cells = row.getElementsByTag(TABLE_DATA_TAG);
            
            CourseSummary summary = new CourseSummary();
            summary.classNum = cells.remove(0).text();
            
            Element courseCell = cells.remove(0);
            Element courseLink = courseCell.getElementsByTag(A_TAG).first();
            summary.course = courseLink.text();
            summary.courseURL = courseLink.attr(HREF_ATTR);
            
            summary.title = cells.remove(0).text();
            summary.distArea = cells.remove(0).text();
            summary.sect = cells.remove(0).text();
            summary.days = cells.remove(0).text();
            summary.time = cells.remove(0).text();
            summary.location = cells.remove(0).text();
            summary.enrl = cells.remove(0).text();
            summary.max = cells.remove(0).text();
            summary.status = cells.remove(0).text();

            Element booksCell = cells.remove(0);
            Element booksLink = booksCell.getElementsByTag(A_TAG).first();
            summary.booksURL = booksLink.attr(HREF_ATTR);

            Element evalCell = cells.remove(0);
            Element evalLink = evalCell.getElementsByTag(A_TAG).first();
            summary.evalURL = evalLink.attr(HREF_ATTR);
            
            Integer classNum = Integer.parseInt(summary.classNum);
            summaries.put(classNum, summary);
        }
    }
    
    public void load(String filename) {
        
    }
    
    public void dump(String filename) {
        
    }
    
    public Map<Integer, CourseSummary> courseSummaries() {
        return summaries;
    }
    
    public CourseSummary courseSummary(Integer classNum) {
        return summaries.get(classNum);
    }
    
    public Object courseDetails(String course) {
        return null;
    }
    
    public static void main(String args[]) throws IOException {
        String URL = "http://registrar.princeton.edu/course-offerings/search_results.xml?submit=Search&term=1124&coursetitle=&instructor=&distr_area=&level=&cat_number=&sort=SYN_PS_PU_ROXEN_SOC_VW.SUBJECT%2C+SYN_PS_PU_ROXEN_SOC_VW.CATALOG_NBR%2CSYN_PS_PU_ROXEN_SOC_VW.CLASS_SECTION%2CSYN_PS_PU_ROXEN_SOC_VW.CLASS_MTG_NBR";
        URL = "http://registrar.princeton.edu/course-offerings/search_results.xml?term=1124&subject=AAS";
        RegistrarScraper rs = new RegistrarScraper();
        rs.scrape(URL);
        System.out.println(rs.courseSummary(43217));
    }
}
