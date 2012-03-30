import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.util.Collection;

public class CourseIndexer {
    private IndexWriter writer;
    private FSDirectory index;
    private RegistrarData rd;
    private StandardAnalyzer analyzer;

    static final Field.Store YES = Field.Store.YES;
    static final Field.Store NO = Field.Store.NO;
    static final Field.Index ANALYZED = Field.Index.ANALYZED;
    static final Field.Index NOT_ANALYZED = Field.Index.NOT_ANALYZED;

    //
    // indexDir = path where index should be created
    // Written using as reference : http://www.lucenetutorial.com/sample-apps/textfileindexer-java.html
    public CourseIndexer(RegistrarData rd, String indexDir) throws IOException {
        this.rd = rd;

        //index = SimpleFSDirectory.open(new File(indexDir));
        index = FSDirectory.open(new File(indexDir));
        
        analyzer = new StandardAnalyzer(Version.LUCENE_35);

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);

        writer = new IndexWriter(index, config);

        indexRegistrar();

        closeIndex();
    }

    // -----------------------------------------------------------------
    // create another constructor that takes a RegistrarData and another indexer and UPDATES an index
    // -----------------------------------------------------------------	


    private void indexRegistrar() {
        Collection<CourseDetails> courses = rd.courseDetails();
        for (CourseDetails course : courses) {
            indexCourse(course);			
        }
    }

    private boolean addPropToDoc(Document doc, CourseDetails details, String prop, Field.Store store, Field.Index analyzed) {
        String value = details.get(prop);
        if (value != null) {
            doc.add(new Field(prop, value, store, analyzed));
            return true;
        }
        return false;
    }

    private void indexCourse(CourseDetails course) {
        try {
            Document doc = new Document();

            addPropToDoc(doc, course, CourseDetails.CLASS_NUM, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.COURSE, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.TITLE, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.DIST_AREA, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.DAYS, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.TIME, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.LOCATION, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.ENROLLED, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.MAX, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.STATUS, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.COURSE_URL, NO, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.BOOKS_URL, NO, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.EVAL_URL, NO, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.PROFESSORS, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.READING_LIST, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.DESCRIPTION, YES, ANALYZED);

            writer.addDocument(doc);

            //System.out.println("Added " + course.get(CourseDetails.COURSE));
        }
        catch (Exception e) {
            System.out.println("I couldn't index this course:" + course.get(CourseDetails.COURSE) + " " + e);
        } 
    }

    private void closeIndex() throws IOException {
        writer.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        RegistrarData rd = new RegistrarData();

        String filename = "coursedata";
        //rd.dump("temp");

        try {
            rd.load(filename);
            System.out.println("made it here!");
            CourseIndexer indexer = new CourseIndexer(rd, "testIndex");

        } catch (Exception e) {
            System.out.println("Couldn't load the file or couldn't index.");
        }


    }

}