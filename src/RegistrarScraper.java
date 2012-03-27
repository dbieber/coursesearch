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
    private final String DESCR_ID = "descr";
    private final String STRONG_TAG = "strong";

    HashMap<Integer, CourseSummary> courses; //maps classNum to courseSummary
    
    public RegistrarScraper() {
        courses = new HashMap<Integer, CourseSummary>();
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
            courses.put(classNum, summary);
            if (recursive) {
                scrapeCourse(classNum, summary.get(CourseSummary.COURSE_URL));
            }
        }
    }
    
    public void test() {
    	String html = "<html><body><h1>Title</h1>Main Text Body</body></html>";
    	Document doc = Jsoup.parse(html);
    	System.out.println(doc.outerHtml());
    }
    
    public void scrapeCourse(Integer classNum, String URL) throws IOException {

        /* TODO fix */
        URL = "http://registrar.princeton.edu/course-offerings/" + URL;
        Document doc = Jsoup.connect(URL).get();
        
        CourseSummary summary = courseSummary(classNum);
        
        Element descr = doc.getElementById(DESCR_ID);
        String descrStr = descr.text();
        summary.put(CourseSummary.DESCRIPTION, descrStr);
        
        String professors = doc.select("p strong").text();
        summary.put(CourseSummary.PROFESSORS, professors);
        
        Elements allHeaders = doc.select("strong, em");
        int numBefore = 0;
        for (Element sectHeader : allHeaders) {        	
        	if (sectHeader.text().equals("Sample reading list:")) {
        		// cycle through and remove 
        		// can we remove elements of the iterator we're in?
        		break;
        	}
        	numBefore++;       
        }
        for (int j = 0; j < numBefore; j++) {
        	allHeaders.remove(0);
        }
        allHeaders.remove(0);

        StringBuilder sReadList = new StringBuilder();
        
        int numAfter = 0;
        for (Element sectHeader : allHeaders) {
        	if (sectHeader.text().equals("Reading/Writing assignments:")) {
        		break;        	
        	}
        	numAfter++;
        	sReadList = sReadList.append(" " + sectHeader.text());
        	
        }
        for (int j = 0; j < numAfter; j++) {
        	allHeaders.remove(0);
        }
        summary.put(CourseSummary.READING_LIST, sReadList.toString());
        
        courses.put(classNum, summary);
    }
    
    public Map<Integer, CourseSummary> courseSummaries() {
        return courses;
    }

    public CourseSummary courseSummary(Integer classNum) {
        CourseSummary s = courses.get(classNum);
        if (s == null) return new CourseSummary();
        return s;
    }
    
    public void dump(String filename) throws IOException {
        OutputStream file = new FileOutputStream( filename );
        OutputStream buffer = new BufferedOutputStream( file );
        ObjectOutput output = new ObjectOutputStream( buffer );
        output.writeObject( courses );
        output.close();
    }
    
    @SuppressWarnings("unchecked")
    public void load(String filename) throws IOException, ClassNotFoundException {
        InputStream file = new FileInputStream( filename );
        InputStream buffer = new BufferedInputStream( file );
        ObjectInput input = new ObjectInputStream ( buffer );
        
        courses = (HashMap<Integer, CourseSummary>)input.readObject();
        input.close();
    }
    
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        String URL = "http://registrar.princeton.edu/course-offerings/";
        /*
        RegistrarScraper rs = new RegistrarScraper();
        rs.scrapeRegistrar(URL);
        rs.dump("temp.txt");

        RegistrarScraper rs2 = new RegistrarScraper();
        rs2.load("temp.txt");

        System.out.println(rs2.courseSummary(43217));
        */
        RegistrarScraper rs = new RegistrarScraper();
        rs.scrapeCourse(42510, "course_details.xml?courseid=004899&term=1124");
        System.out.println(rs.courseSummary(42510).get(CourseSummary.PROFESSORS));
    }
}
