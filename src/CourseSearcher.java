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
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.sun.xml.internal.ws.util.xml.CDATA;

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

    /**
     * @param querystr
     */
    public void search(String querystr) throws ParseException, IOException {
        Query q = new MultiFieldQueryParser(Version.LUCENE_35,
                new String[] {CourseDetails.TITLE, CourseDetails.DESCRIPTION, CourseDetails.COURSE},
                analyzer).parse(querystr);
        int hitsPerPage = 14;
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        System.out.println("Found " + hits.length + " hits.");

        for(int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get(CourseDetails.COURSE) + ": " + d.get(CourseDetails.TITLE) + " - " + d.get(CourseDetails.DESCRIPTION));
        }
    }
    
    public void searchTime(String querystr) throws ParseException, IOException {
        /*
         * TODO: parse into days and times and then do specific field searches??
         */
        Query q = new MultiFieldQueryParser(Version.LUCENE_35,
                new String[] {CourseDetails.DAYS, CourseDetails.TIME},
                analyzer).parse(querystr);
        int hitsPerPage = 20;
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        
        System.out.println("Found " + hits.length + " hits.");

        for(int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get(CourseDetails.COURSE) + ": " + d.get(CourseDetails.TITLE) + " - " + d.get(CourseDetails.DESCRIPTION));
        }
        
    }

    public void closeSearcher() throws IOException {
        searcher.close();
    }

    /**
     * @param args
     * @throws IOException 
     * @throws ParseException 
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException {
        // directory of index to search
        String indexDir = "testIndex";
        CourseSearcher mysearch = new CourseSearcher(indexDir);
        // brings up any course with even a precept at 300?
        //mysearch.searchTime("lala");
        
        mysearch.search("MUS");
        mysearch.search("217");
        mysearch.search("Programming");
        mysearch.search("Architecture");
        mysearch.search("title:Architecture");
        // need to deal with the fact that cannot search 9:00
        // when done using search, need to close the searcher
        mysearch.closeSearcher();
    }
}
