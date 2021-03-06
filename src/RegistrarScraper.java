/*
 * Name: David Bieber, Abbi Ward
 * COS 435  - Final Project
 * 
 * File: RegistrarScraper.java
 * Description: scrapes the course registrar
 *          Goes within each department, and from there within
 *          each course
 * 
 * 
 * 
 */

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

public class RegistrarScraper {
    
    public static final String URL = "http://registrar.princeton.edu/course-offerings/";
    
    // HTML tags/IDs
    private final String TABLE_TAG = "table";
    private final String TABLE_ROW_TAG = "tr";
    private final String TABLE_DATA_TAG = "td";
    private final String A_TAG = "a";
    private final String H2_TAG = "h2";
    private final String STRONG_TAG = "strong";
    private final String HREF_ATTR = "href";
    private final String DESCR_ID = "descr";

    // Season
    private final static String SEASON = "Fall 2012-2013";

    // Ways that npdf, na, and pdf only appear
    private final static String[] NPDF = {"No Pass/D/Fail",  "npdf"};
    private final static String[] PDFONLY = {"P/D/F Only"};
    private final static String[] NAUDIT = {"na", "No Audit"};    

    private Queue<DepartmentURL> departmentURLS;
    private Queue<CourseURL> courseURLS;

    private class DepartmentURL {
        public String name;
        public String url;
    }
    
    private class CourseURL {
        public String name;
        public String url;
    }
    
    // All course information
    RegistrarData data;

    public RegistrarScraper() throws IOException {
        data = new RegistrarData();
        departmentURLS = new LinkedList<DepartmentURL>();
        courseURLS = new LinkedList<CourseURL>();
    }

    public RegistrarScraper(RegistrarData data) {
        this.data = data;
        departmentURLS = new LinkedList<DepartmentURL>();
        courseURLS = new LinkedList<CourseURL>();
    }
    
    // Scrapes the registrar.
    public void scrapeRegistrar() {
        scrapeMainPage();
        
        while (!departmentURLS.isEmpty() || !courseURLS.isEmpty()) {
            if (!departmentURLS.isEmpty()) {
                scrapeDepartment(departmentURLS.remove());
            }
            if (!courseURLS.isEmpty()) {
                scrapeCourse(courseURLS.remove());
            }
        }
    }
    
    // Performs tier 1 of scraping.
    public boolean scrapeMainPage() {
        Document doc = null;
        try {
            doc = Jsoup.connect(URL).get();
        }
        catch (Exception e){
            System.out.println("Failed to load registrar: " + URL);
            return false;
        }

        Element table = doc.getElementsByTag(TABLE_TAG).first();
        Elements links = table.getElementsByTag(A_TAG);
        for (Element link : links) {
            DepartmentURL u = new DepartmentURL();
            u.name = link.text();
            u.url = link.attr(HREF_ATTR);
            departmentURLS.add(u);
        }
        return true;
    }
    
    public void scrapeDepartments() {
        while (!departmentURLS.isEmpty()) {
            DepartmentURL dept = departmentURLS.remove();
            scrapeDepartment(dept);
        }
    }

    // Scrapes a department at the given URL
    public void scrapeDepartment(DepartmentURL u) {
        Document doc = null;
        try {
            doc = Jsoup.connect(URL + u.url).get();
            System.out.println("Loaded department: " + u.name);
        }
        catch (Exception e){
            System.out.println("Failed to load department: " + u.name);
            departmentURLS.add(u);
            return;
        }

        // Each row in the table has the most basic course information
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
            details.setDays(cells.remove(0).text());            
            details.setTime(cells.remove(0).text());
            details.put(CourseDetails.LOCATION, cells.remove(0).text());
            details.put(CourseDetails.ENROLLED, cells.remove(0).text());
            details.put(CourseDetails.MAX, cells.remove(0).text());
            details.put(CourseDetails.STATUS, cells.remove(0).text());
            details.put(CourseDetails.SEASON, SEASON); // Only one season

            Element booksCell = cells.remove(0);
            Element booksLink = booksCell.getElementsByTag(A_TAG).first();
            details.put(CourseDetails.BOOKS_URL, booksLink.attr(HREF_ATTR));

            Element evalCell = cells.remove(0);
            Element evalLink = evalCell.getElementsByTag(A_TAG).first();
            details.put(CourseDetails.EVAL_URL, evalLink.attr(HREF_ATTR));

            data.addCourseDetails(details);
            
            CourseURL c = new CourseURL();
            c.name = details.get(CourseDetails.TITLE);
            c.url = details.get(CourseDetails.COURSE_URL);
            courseURLS.add(c);
        }
    }

    // processes a header h for comparison
    private String processHeader(String h) {
        return h.toLowerCase().trim().replaceAll(":", "");
    }
    
    // Checks if the given headers matches our predetermined header list
    private String matchHeader(String header) {
        for (String h : CourseDetails.HEADERS) {
            if (processHeader(h).equals(processHeader(header))) {
                return h;
            }
        }
        return null;
    }

    // Returns true if our haystack contains one of the given needles
    private boolean containsOneOf(String haystack, String[] needles) {
        for (String needle : needles) {
            if (haystack.contains(needle)) {
                return true;
            }
        }
        return false;
    }
    
    // Scrapes a course at the given URL
    // Specially process grading restrictions and reading amount
    public void scrapeCourse(CourseURL c) {
        String myURL = URL + c.url;
        Document doc = null;
        try {
            doc = Jsoup.connect(myURL).get();
            System.out.println("Loaded course: " + c.name);
        }
        catch (Exception e){
            System.out.println("Failed to load course: " + c.name);
            courseURLS.add(c);
            return;
        }
        CourseDetails details = new CourseDetails();

        details.put(CourseDetails.COURSE_URL, myURL);

        Element timetable = doc.getElementById("timetable");
        
        //title
        String title = timetable.getElementsByTag(H2_TAG).first().text();
        details.put(CourseDetails.TITLE, title);
        
        // grading restrictions - npdf, pdfonly, na
        String gradingRestrictions = timetable.select("strong + em").first().text();
        if (containsOneOf(gradingRestrictions, NPDF)) {
            details.put(CourseDetails.PDF, CourseDetails.NO);
        } else if (containsOneOf(gradingRestrictions, PDFONLY)) {
            details.put(CourseDetails.PDF, CourseDetails.ONLY);
        } else {            
            details.put(CourseDetails.PDF, CourseDetails.YES);
        }
        if (containsOneOf(gradingRestrictions, NAUDIT)) {            
            details.put(CourseDetails.AUDIT, CourseDetails.NO);
        } else {
            details.put(CourseDetails.AUDIT, CourseDetails.YES);
        }
        
        // description
        Element descr = doc.getElementById(DESCR_ID);
        if (descr != null) {
            String descrStr = descr.text();
            details.put(CourseDetails.DESCRIPTION, descrStr);
        }

        // professors
        String professors = doc.select("p strong").text();
        details.put(CourseDetails.PROFESSORS, professors);

        // Checks if headers are present and if so, assigns appropriate CourseDetails
        //      field the appropriate data
        Elements headers = doc.getElementsByTag(STRONG_TAG);
        for (Element header : headers) {
            // headerText \in HEADERS
            String headerText = matchHeader(header.text());
            if (headerText == null) {
                continue;
            }
            
            Element sibling = header.nextElementSibling();                  
            
            String text = "";
            while (sibling != null && matchHeader(sibling.text()) == null) {
                String elementText = sibling.text().trim();
                if (elementText.length() > 0) {
                    text += elementText + " ";
                }
                Node nextSibling = sibling.nextSibling();
                if (nextSibling != null) {
                    String nodeText = sibling.nextSibling().attr("text").trim();
                    if (nodeText.length() > 0) {
                        text += nodeText + '\n';
                    }
                }
                sibling = sibling.nextElementSibling();
            }

            text = text.trim();
            details.put(headerText, text);
        }
        
        String readingsText = details.get(CourseDetails.ASSIGNMENTS);
        if (readingsText != null) {
            details.setReadingAmt(readingsText);
        }
        
        data.addCourseDetails(details);
    }

    // Scrape the Registrar
    public static void test() throws IOException, ClassNotFoundException {
        RegistrarData data = new RegistrarData();
        RegistrarScraper rs = new RegistrarScraper(data);
        rs.scrapeRegistrar();
        data.dump("CourseData519");
        System.out.println(rs.data);
    }

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        test();
    }
}
