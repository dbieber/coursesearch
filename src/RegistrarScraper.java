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

    HashMap<Integer, CourseSummary> summaries; //maps classNum to courseSummary
    HashMap<Integer, CourseDetails> allDetails; //maps classNum to courseDetails
    
    public RegistrarScraper() {
        summaries = new HashMap<Integer, CourseSummary>();
    }
    
    public void scrapeRegistrar(String URL) throws IOException {
        Document doc = Jsoup.connect(URL).get();
        
        Element table = doc.getElementsByTag(TABLE_TAG).first();
        Elements links = table.getElementsByTag(A_TAG);
        for (Element link : links) {
            System.out.println("Scraping " + link.text());
            scrapeDepartment(URL + link.attr(HREF_ATTR)); 
        }
    }
    
    public void scrapeDepartment(String URL) throws IOException {
        Document doc = Jsoup.connect(URL).get();
        
        Element table = doc.getElementsByTag(TABLE_TAG).first();
        Elements rows = table.getElementsByTag(TABLE_ROW_TAG);
        rows.remove(0); // first row is bogus
        for (Element row : rows) {
            Elements cells = row.getElementsByTag(TABLE_DATA_TAG);
            
            CourseSummary summary = new CourseSummary();
            summary.put(CourseSummary.CLASS_NUM, cells.remove(0).text());
            
            Element courseCell = cells.remove(0);
            Element courseLink = courseCell.getElementsByTag(A_TAG).first();
            summary.put(CourseSummary.COURSE, courseLink.text());
            summary.put(CourseSummary.COURSE_URL, courseLink.attr(HREF_ATTR));

            summary.put(CourseSummary.TITLE, cells.remove(0).text());
            summary.put(CourseSummary.DIST_AREA, cells.remove(0).text());
            summary.put(CourseSummary.SECTION, cells.remove(0).text());
            summary.put(CourseSummary.DAYS, cells.remove(0).text());
            summary.put(CourseSummary.TIME, cells.remove(0).text());
            summary.put(CourseSummary.LOCATION, cells.remove(0).text());
            summary.put(CourseSummary.ENROLLED, cells.remove(0).text());
            summary.put(CourseSummary.MAX, cells.remove(0).text());
            summary.put(CourseSummary.STATUS, cells.remove(0).text());

            Element booksCell = cells.remove(0);
            Element booksLink = booksCell.getElementsByTag(A_TAG).first();
            summary.put(CourseSummary.BOOKS_URL, booksLink.attr(HREF_ATTR));

            Element evalCell = cells.remove(0);
            Element evalLink = evalCell.getElementsByTag(A_TAG).first();
            summary.put(CourseSummary.EVAL_URL, evalLink.attr(HREF_ATTR));
            
            Integer classNum = Integer.parseInt(
                    summary.get(CourseSummary.CLASS_NUM));
            summaries.put(classNum, summary);
        }
    }
    
    public void scrapeCourse(String URL) throws IOException {
        Document doc = Jsoup.connect(URL).get();
        
        CourseDetails details = new CourseDetails();
        
        allDetails.put(classNum, details);
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
        String URL = "http://registrar.princeton.edu/course-offerings/";
        RegistrarScraper rs = new RegistrarScraper();
        rs.scrapeRegistrar(URL);
        
        System.out.println(rs.courseSummary(43217));
    }
}
