import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;


public class ScrapeTest {
    private static final String TABLE_TAG = "table";
    private static final String TABLE_ROW_TAG = "tr";
    private static final String TABLE_DATA_TAG = "td";
    private static final String A_TAG = "a";
    private static final String HREF_ATTR = "href";
    private static final String DESCR_ID = "descr";

    public static int getMin(int[] indexes) {
        int min = 1000000000;
        for (int i = 1; i < indexes.length; i++) {
            if (indexes[i] == 0) {
                continue;
            }
            if (indexes[i] < min) {
                min = indexes[i];
            }
        }
        return min;
    }
    
    public static void getReadList(Document doc, String URL) {
        CourseDetails details = new CourseDetails();

        details.put(CourseDetails.COURSE_URL, URL);

        Element descr = doc.getElementById(DESCR_ID);
        String descrStr = descr.text();
        details.put(CourseDetails.DESCRIPTION, descrStr);

        String professors = doc.select("p strong a").text();
        System.out.println("Professors: " + professors);
        details.put(CourseDetails.PROFESSORS, professors);            
        
        // Search and record indexes
        
        int[] indexes = new int[7];
        Elements allHeaders = doc.select("strong, em");
        int i = 0;
        for (Element header : allHeaders) {
            if (header.text().toLowerCase().contains("sample reading list")) {
                indexes[0] = i;
            }
            else if (header.text().toLowerCase().contains("requirements/grading")) {
                indexes[1] = i;
            }
            else if (header.text().toLowerCase().contains("prerequisites and restrictions")) {
                indexes[2] = i;
            }
            else if (header.text().toLowerCase().contains("schedule/classroom assignment")) {
                indexes[3] = i;
            }
            else if (header.text().toLowerCase().contains("other information")) {
                indexes[4] = i;           
            }
            else if (header.text().toLowerCase().contains("other requirements")) {
                indexes[5] = i;           
            }
            else if (header.text().toLowerCase().contains("website")) {
                indexes[5] = i;           
            }
           
            i++;
        }
        // gather the sample reading list, if one exists
        if (indexes[0] != 0) {
            int min = getMin(indexes);            
            StringBuilder readinglist = new StringBuilder();
            for (i = indexes[0] + 1; i < min - 1; i++) {
                readinglist.append(" " + allHeaders.get(i).text());
            }
            System.out.println("Reading list: " + readinglist);
            details.put(CourseDetails.READING_LIST, readinglist.toString());
        }
        
        
        Elements stuff = doc.getElementsMatchingText("[0-100]%");
        System.out.println("Things!\n");
        for (Element thing : stuff) {
            System.out.println(thing.text());
        }
        
    }
    
    public static void test2() {
        String html = "<strong>" + 
                "Requirements/Grading:" + 
                "</strong><br>" + 
                "Final Exam  - 20% <br>" + 
                "Quizzes  - 10%<br>" +
                "Oral Presentation(s) -  20%<br>" +
                "Class/Precept Participation  - 15%<br>" +
                "Problem set(s) -  15%<br>" +
                "Other (See Instructor)  - 20%<br>" +
                "<br>" +
                "<strong>Prerequisites and Restrictions:</strong><br>" +
                "No credit is given for ARA 101 unless it is followed by ARA 102.." +
                "<br>" +
                "<br>" +
                "<strong>Other information:</strong><br>" +
                "This is the first semester of a two-term language course.  Please note that those who arrive late during \"shopping period\" (the first week or two of the course) are responsible for making up all missed classes on their own. Students are required to attend an additional one-hour long drill section each week."
                 + "<br>" + 
                "<br>";
        Whitelist wl = new Whitelist();
        wl.addTags("p", "a", "strong", "em", "i", "u");
        System.out.println(Jsoup.clean(html, wl));
        
    }
    
    public static void main(String[] args) throws IOException {
    
        String URL = "http://registrar.princeton.edu/course-offerings/";
        
        //File.createTempFile("lalala", ".txt", "/testingggg/");
        
        File input = new File("//C:/Users/Abbi/introcs/cos435/project/testData/SWA200.html");
        Document doc = Jsoup.parse(input, "UTF-8", URL);
        
        getReadList(doc, URL);
        
        
        
    }
}
