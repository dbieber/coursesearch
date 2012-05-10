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
    private final String STRONG_TAG = "strong";
    private final String HREF_ATTR = "href";
    private final String DESCR_ID = "descr";

    private final static String[] HEADERS = {"sample reading list", "requirements/grading", "prerequisites and restrictions", 
            "schedule/classroom assignment", "other information", "other requirements", "reserved seats", "website", "reading/writing assignments"};
    private final static String SEASON = "Fall 2012-2013";
    
    // npdf and na are <em> right after <strong> in id="timetable"
    private final static String[] NPDF = {"No Pass/D/Fail",  "npdf"};
    private final static String[] NAUDIT = {"na", "No Audit"};

    RegistrarData data;

    public RegistrarScraper() throws IOException {
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

    private String processHeader(String h) {
        return h.toLowerCase().trim().replaceAll(":", "");
    }
    
    private String matchHeader(String header) {
        for (String h : HEADERS) {
            if (processHeader(h).equals(processHeader(header))) {
                return h;
            }
        }
        return null;
    }

    public void scrapeCourse(String URL) throws IOException {
        /* TODO fix */
        /* TODO -- need to scrape title from the details, not the summary*/
        
        System.out.println(URL);
        String base = "http://registrar.princeton.edu/course-offerings/";
        URL = base + URL;
        Document doc = Jsoup.connect(URL).get();
        /*Tidy htmlSanitizer = new Tidy();
        htmlSanitizer.setEncloseText(true);
        htmlSanitizer.setXmlTags(false);
        htmlSanitizer.setShowWarnings(false);
        htmlSanitizer.setInputEncoding("UTF-8");
        htmlSanitizer.setOutputEncoding("UTF-8");
        htmlSanitizer.setXHTML(true);
        htmlSanitizer.setMakeClean(true);
        htmlSanitizer.setEncloseBlockText(true);
        
        URL url = new URL(URL);
        URLConnection site = url.openConnection();
        InputStream input = site.getInputStream(); 
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        org.w3c.dom.Document doc2 = htmlSanitizer.parseDOM(input, out);                
        Document doc = Jsoup.parse(new ByteArrayInputStream(out.toByteArray()), "UTF-8", base);
        System.out.println(out);
        htmlSanitizer.getConfiguration().printConfigOptions(new BufferedWriter(new OutputStreamWriter(System.out)), true);*/
        
        CourseDetails details = new CourseDetails();

        details.put(CourseDetails.COURSE_URL, URL);

        String gradingRestrictions = doc.getElementById("timetable").select("strong + em").first().text();
        // TODO add to index properly. And coursedetails
        
        Element descr = doc.getElementById(DESCR_ID);
        String descrStr = descr.text();
        details.put(CourseDetails.DESCRIPTION, descrStr);

        String professors = doc.select("p strong").text();
        details.put(CourseDetails.PROFESSORS, professors);

        Elements headers = doc.getElementsByTag(STRONG_TAG);
        for (Element header : headers) {
            String headerText = matchHeader(header.text());
            if (headerText == null) {
                continue;
            }

            Element sibling = header.nextElementSibling();
            System.out.println(header.text());
            
            String text = "";
            while (sibling != null && matchHeader(sibling.text()) == null) {
                String elementText = sibling.text().trim();
                String nodeText = sibling.nextSibling().attr("text").trim();
                if (elementText.length() > 0) {
                    text += elementText + " ";
                }
                if (nodeText.length() > 0) {
                    text += nodeText + '\n';
                }
                sibling = sibling.nextElementSibling();
            }
            text = text.trim();
        }
        
        
        Elements allHeaders = doc.select("strong, em");
        int numBefore = 0;
        for (Element sectHeader : allHeaders) {        	
            if (sectHeader.text().toLowerCase().equals("sample reading list:")) {
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
    
    public static void test1() throws IOException, ClassNotFoundException {
        String URL = "http://registrar.princeton.edu/course-offerings/";
        RegistrarData data = new RegistrarData();
        data.load("coursedata");
        RegistrarScraper rs = new RegistrarScraper(data);

        int count = 100;
        for (CourseDetails details : data.courseDetails()) {
            try {
                //rs.scrapeCourse(details.get(CourseDetails.COURSE_URL));
            } catch(Exception e) { System.out.println("minor failure for " + details.get(CourseDetails.COURSE_URL));}
            count--;
            if (count == 0) {
                break;
            }
        }
        rs.data.dump("coursedata");
        System.out.println(rs.data);
    }
    
    public static void test() throws IOException {
        String URL = "http://registrar.princeton.edu/course-offerings/search_results.xml?term=1132&subject=COS";
        RegistrarData data = new RegistrarData();
        RegistrarScraper rs = new RegistrarScraper(data);
        rs.scrapeDepartment(URL, true);
    }

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        test();
    }
}
