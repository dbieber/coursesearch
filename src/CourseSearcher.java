/*
 * Name: David Bieber, Abbi Ward
 * COS 435 - Final Project
 * 
 * Description: Data type for searching a given course index
 * 
 */

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
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

    public CourseSearcher(String indexDir) throws CorruptIndexException {
        try {
            index = FSDirectory.open(new File(indexDir));
            IndexReader reader = IndexReader.open(index);
            searcher = new IndexSearcher(reader);
            analyzer = new StandardAnalyzer(Version.LUCENE_35);
        } catch (IOException e) {
            System.err.println("Could not create CourseSearcher.");
            e.printStackTrace();
        }
    }
    
    /*
     * Searches using a plain text query and prints the top scoring result
     */
    public Document[] search(String queryString, int hitsPerPage) throws ParseException, IOException {
        if (queryString.trim().isEmpty()) {
            return new Document[0];
        }
        Document[] hits = search(new CourseQuery(queryString), hitsPerPage);
        return hits;
    }
    
    /* 
     * Searches from a CourseQuery object
     */
    public Document[] search(CourseQuery query, int hitsPerPage) throws ParseException, IOException {
        String queryString = query.getQueryString();

        System.out.println("Query: " + queryString);

        Similarity me = Similarity.getDefault();
        Similarity.setDefault(me);
                
        Query q = new MultiFieldQueryParser(Version.LUCENE_35,
                CourseDetails.TEXT_FIELDS,
                analyzer).parse(queryString);

        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(q, collector);

        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        Document[] results = new Document[hits.length];

        for(int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            results[i] = d;
        }
        return results;
    }

    public boolean closeSearcher() {
        try {
            searcher.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
