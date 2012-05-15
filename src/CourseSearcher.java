/*
 * Name: David Bieber, Abbi Ward
 * COS 435 - Final Project
 * 
 * Description: Data type for searching a given course index
 * 
 */

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class CourseSearcher {
    private FSDirectory index;
    private IndexSearcher searcher;
    private StandardAnalyzer analyzer;

    public CourseSearcher(String indexDir) throws CorruptIndexException, IOException {
        index = FSDirectory.open(new File(indexDir));
        IndexReader reader = IndexReader.open(index);
        searcher = new IndexSearcher(reader);
        analyzer = new StandardAnalyzer(Version.LUCENE_35);
    }
    

    /* Creates a search from a CourseQuery object
     * 
     */
    public ScoreDoc[] search(CourseQuery query, int hitsPerPage) throws ParseException, IOException {
        ScoreDoc[] hits = search(query.getQueryString(), hitsPerPage);
        
        return hits;
    }
    
    /*
     * Searches using a plaintext query and prints the top scoring result
     */
    public ScoreDoc[] search(String query, int hitsPerPage) throws ParseException, IOException {
        Similarity me = Similarity.getDefault();
        Similarity.setDefault(me);        
        System.out.println(me.idfExplain(new Term("Roman"), searcher, 1).explain());
                
        Query q = new MultiFieldQueryParser(Version.LUCENE_35,
                CourseDetails.TEXT_FIELDS,
                analyzer).parse(query);
        //int hitsPerPage = 14;
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        System.out.println("Found " + hits.length + " hits.");

        for(int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + hits[i].score + " " + d.get(CourseDetails.COURSE) + ": " + d.get(CourseDetails.DAYS)  
                        + " " + d.get(CourseDetails.TIME) + " " + d.get(CourseDetails.PDF) + " " + d.get(CourseDetails.MAX) + " " 
                        + d.get(CourseDetails.DESCRIPTION));
        }
        return hits;
    }
    


    public void closeSearcher() throws IOException {
        searcher.close();
    }

    /**
     * @param args
     * @throws IOException, ParseException, ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException {
        // directory of index to search
        //String indexDir = "testIndex";
        //String indexDir = "testHisIndex";
        String indexDir = "AllCourseIndexBoost";
        CourseSearcher mysearch = new CourseSearcher(indexDir);
        // brings up any course with even a precept at 300?
        //mysearch.searchTime("lala");
        
        /*mysearch.search("MUS");
        mysearch.search("217");
        mysearch.search("Programming");
        mysearch.search("Architecture");
        mysearch.search("title:Architecture");*/

        CourseQuery q = new CourseQuery("T Th 1:30");
        System.out.println("Query:" + q.toString());
        mysearch.search(q, 10);
        
        String indexDir2 = "AllCourseIndexNoBoost";
        CourseSearcher mysearch2 = new CourseSearcher(indexDir2);
        //CourseQuery q2 = new CourseQuery("ceramics");
        System.out.println("Query:" + q.toString());
        mysearch2.search(q, 10);
        
//        mysearch.search(CourseDetails.COURSE +": PHI " + CourseDetails.DIST_AREA + ": QR", 100);

        //mysearch.search("time: thirteenthir pdf: only");
        //mysearch.search(CourseDetails.PDF + ": only");
       // mysearch.search(CourseDetails.DAYS + ":thursday");
       // q = new CourseQuery("course 1:30 ");
        //mysearch.search(q);
        
        // when done using search, need to close the searcher                        
        mysearch.closeSearcher();
    }
}
