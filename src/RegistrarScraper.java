import java.io.IOException;

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

    RegistrarData data;
    
    public RegistrarScraper() {
        data = new RegistrarData();
    }
    
    public RegistrarScraper(RegistrarData data) {
        this.data = data;
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
        }
    }
    
    public void scrapeDepartment(String URL, boolean recursive) throws IOException {
        Document doc = Jsoup.connect(URL).get();
        
        Element table = doc.getElementsByTag(TABLE_TAG).first();
        Elements rows = table.getElementsByTag(TABLE_ROW_TAG);
        rows.remove(0); // first row is bogus
        for (Element row : rows) {
            Elements cells = row.getElementsByTag(TABLE_DATA_TAG);
            
            CourseDetails details = new CourseDetails();
            details.put(CourseDetails.CLASS_NUM, cells.remove(0).text());
            
            Element courseCell = cells.remove(0);
            Element courseLink = courseCell.getElementsByTag(A_TAG).first();
            details.put(CourseDetails.COURSE, courseLink.text());
            details.put(CourseDetails.COURSE_URL, courseLink.attr(HREF_ATTR));

            details.put(CourseDetails.TITLE, cells.remove(0).text());
            details.put(CourseDetails.DIST_AREA, cells.remove(0).text());
            details.put(CourseDetails.SECTION, cells.remove(0).text());
            details.put(CourseDetails.DAYS, cells.remove(0).text());
            details.put(CourseDetails.TIME, cells.remove(0).text());
            details.put(CourseDetails.LOCATION, cells.remove(0).text());
            details.put(CourseDetails.ENROLLED, cells.remove(0).text());
            details.put(CourseDetails.MAX, cells.remove(0).text());
            details.put(CourseDetails.STATUS, cells.remove(0).text());

            Element booksCell = cells.remove(0);
            Element booksLink = booksCell.getElementsByTag(A_TAG).first();
            details.put(CourseDetails.BOOKS_URL, booksLink.attr(HREF_ATTR));

            Element evalCell = cells.remove(0);
            Element evalLink = evalCell.getElementsByTag(A_TAG).first();
            details.put(CourseDetails.EVAL_URL, evalLink.attr(HREF_ATTR));
            
            data.addCourseDetails(details);
            if (recursive) {
                scrapeCourse(details.get(CourseDetails.COURSE_URL));
            }
        }
    }
    
    public void test() {
    	String html = "<html><body><h1>Title</h1>Main Text Body</body></html>";
    	Document doc = Jsoup.parse(html);
    	System.out.println(doc.outerHtml());
    }
    
    public void scrapeCourse(String URL) throws IOException {
        /* TODO fix */
        System.out.println(URL);
        URL = "http://registrar.princeton.edu/course-offerings/" + URL;
        Document doc = Jsoup.connect(URL).get();
        
        CourseDetails details = new CourseDetails();
        
        details.put(CourseDetails.COURSE_URL, URL);
        
        Element descr = doc.getElementById(DESCR_ID);
        String descrStr = descr.text();
        details.put(CourseDetails.DESCRIPTION, descrStr);
        
        String professors = doc.select("p strong").text();
        details.put(CourseDetails.PROFESSORS, professors);
        
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
        details.put(CourseDetails.READING_LIST, sReadList.toString());
        
        data.addCourseDetails(details);
    }
    
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        String URL = "http://registrar.princeton.edu/course-offerings/";
        RegistrarData data = new RegistrarData();
        data.load("coursedata");
        RegistrarScraper rs = new RegistrarScraper(data);

        int count = 100;
        for (CourseDetails details : data.courseDetails()) {
            try {
                rs.scrapeCourse(details.get(CourseDetails.COURSE_URL));
            } catch(Exception e) { System.out.println("minor failure for " + details.get(CourseDetails.COURSE_URL));}
            count--;
            if (count == 0) {
                break;
            }
        }
        
        rs.data.dump("coursedata");
        System.out.println(rs.data);
    }
}
