import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
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


    /**
     * @param querystr
     */
    public void search(String querystr) throws ParseException, IOException {
        Query q = new QueryParser(Version.LUCENE_35, CourseDetails.TITLE, analyzer).parse(querystr);
        int hitsPerPage = 10;
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        System.out.println("Found " + hits.length + " hits.");

        for(int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get(CourseDetails.COURSE));
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
        String filename = "coursedata";
        String indexDir = "testIndex";
        RegistrarData rd = new RegistrarData();
        rd.load(filename);
        //System.out.println("made it here!");
        //CourseIndexer indexer = new CourseIndexer(rd, indexDir);
        CourseSearcher mysearch = new CourseSearcher(indexDir);

        mysearch.search("music course:mus");
        mysearch.search("course:cos time:W");
        //mysearch.search("linear algebra");

        mysearch.closeSearcher();
    }
}
