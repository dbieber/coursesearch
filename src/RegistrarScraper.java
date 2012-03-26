import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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
        scrapeRegistrar(URL, false);
    }
    public void scrapeRegistrar(String URL, boolean recursive) throws IOException {
        Document doc = Jsoup.connect(URL).get();
        
        Element table = doc.getElementsByTag(TABLE_TAG).first();
        Elements links = table.getElementsByTag(A_TAG);
        for (Element link : links) {
            System.out.println("Scraping " + link.text());
            scrapeDepartment(URL + link.attr(HREF_ATTR), recursive);
            break; //TODO remove
        }
    }
    
    public void scrapeDepartment(String URL, boolean recursive) throws IOException {
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
            if (recursive) {
                scrapeCourse(summary.get(CourseSummary.COURSE_URL));
            }
        }
    }
    
    public void scrapeCourse(String URL) throws IOException {

        /* TODO fix */
        URL = "http://registrar.princeton.edu/course-offerings/" + URL;
        System.out.println(URL);
        Document doc = Jsoup.connect(URL).get();
        
        System.out.println(doc.text());
        
        CourseDetails details = new CourseDetails();
        /* TODO */
        
        //Integer classNum = Integer.parseInt(
        //        details.get(CourseDetails.CLASS_NUM));
        //allDetails.put(classNum, details);
    }
    
    public Map<Integer, CourseSummary> courseSummaries() {
        return summaries;
    }

    public CourseSummary courseSummary(Integer classNum) {
        return summaries.get(classNum);
    }
    
    public CourseSummary courseDetails(Integer classNum) {
        return allDetails.get(classNum);
    }
    
    public void dump(String filename) throws IOException {
        OutputStream file = new FileOutputStream( filename );
        OutputStream buffer = new BufferedOutputStream( file );
        ObjectOutput output = new ObjectOutputStream( buffer );
        output.writeObject( summaries );
        output.close();
    }
    
    @SuppressWarnings("unchecked")
    public void load(String filename) throws IOException, ClassNotFoundException {
        InputStream file = new FileInputStream( filename );
        InputStream buffer = new BufferedInputStream( file );
        ObjectInput input = new ObjectInputStream ( buffer );
        
        summaries = (HashMap<Integer, CourseSummary>)input.readObject();
        input.close();
    }
    
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        String URL = "http://registrar.princeton.edu/course-offerings/";
        RegistrarScraper rs = new RegistrarScraper();
        rs.scrapeRegistrar(URL);
        rs.dump("temp.txt");

        RegistrarScraper rs2 = new RegistrarScraper();
        rs2.load("temp.txt");

        System.out.println("B");
        System.out.println(rs2.courseSummary(43217));
    }
}
