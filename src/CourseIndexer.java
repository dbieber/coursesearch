/*
 * Name: David Bieber, Abbi Ward
 * COS 435  - Final Project
 * 
 * Description: indexes given RegistrarData to a given index indexDir
 * 
 * 
 * Reference:
 * http://www.lucenetutorial.com/sample-apps/textfileindexer-java.html
 * 
 */

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
    
    public static final boolean BOOST = true;

    static final Field.Store YES = Field.Store.YES;
    static final Field.Store NO = Field.Store.NO;
    static final Field.Index ANALYZED = Field.Index.ANALYZED;
    static final Field.Index NOT_ANALYZED = Field.Index.NOT_ANALYZED;

    /* Creates an index for the given registrar data in directory indexDir
     * 
     * @param rd : RegistrarData we want to index
     * @param indexDir : directory to put the index in
     */
    public CourseIndexer(RegistrarData rd, String indexDir) throws IOException {
        this.rd = rd;

        index = FSDirectory.open(new File(indexDir));
        analyzer = new StandardAnalyzer(Version.LUCENE_35);

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        //config.setSimilarity(new IsolationSimilarity());
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        writer = new IndexWriter(index, config);
        
        indexRegistrar();

        closeIndex();
    }

    // cycles through courses and indexes each course
    private void indexRegistrar() {
        Collection<CourseDetails> courses = rd.courseDetails();
        for (CourseDetails course : courses) {
            indexCourse(course);			
        }
    }
    
    private double getBoostFor(String fieldType) {
        if (BOOST) {
            if (fieldType.equals(CourseDetails.COURSE)) return 1.07;
            if (fieldType.equals(CourseDetails.DIST_AREA)) return 1.07;
            if (fieldType.equals(CourseDetails.TITLE)) return 1.07;
            if (fieldType.equals(CourseDetails.PDF)) return 1.09;
        }
        return 1.0;
    }
    
    // helper method: creates a field based on String prop and adds the proper information
    //    in CourseDetails details to the doc
    private boolean addPropToDoc(Document doc, CourseDetails details, String prop, Field.Store store, Field.Index analyzed) {
        String value = details.get(prop);
        if (value != null && !value.equals("")) {
            Field f = new Field(prop, value, store, analyzed);
            f.setBoost((float) getBoostFor(prop));
            doc.add(f);
            return true;
        }
        return false;
    }
    
    // creates a new document for the given CourseDetails course, adds several
    // fields and then adds it to the IndexWriter writer
    private void indexCourse(CourseDetails course) {
        try {
            Document doc = new Document();

            addPropToDoc(doc, course, CourseDetails.CLASS_NUM, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.COURSE, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.TITLE, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.DIST_AREA, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.DAYS, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.TIME, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.LOCATION, YES, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.ENROLLED, YES, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.MAX, YES, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.STATUS, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.COURSE_URL, NO, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.BOOKS_URL, NO, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.EVAL_URL, NO, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.PROFESSORS, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.DESCRIPTION, YES, ANALYZED);
            addPropToDoc(doc, course, CourseDetails.PDF, YES, NOT_ANALYZED);
            addPropToDoc(doc, course, CourseDetails.AUDIT, YES, NOT_ANALYZED);
            for (String header : CourseDetails.TEXT_HEADERS) {
                // Only adds fields for headers that are present
                addPropToDoc(doc, course, header, YES, ANALYZED);
            }
            addPropToDoc(doc, course, CourseDetails.READING_AMT, YES, ANALYZED);
            writer.addDocument(doc);
        }
        catch (Exception e) {
            System.out.println("I couldn't index this course:" + course.get(CourseDetails.COURSE) + " " + e);
        } 
    }
    
    //closes the IndexWriter
    private void closeIndex() throws IOException {
        writer.close();
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        RegistrarData rd = new RegistrarData();
        String filename = "coursedata";
        
        try {
            rd.load(filename);
            System.out.println("made it here!");
            new CourseIndexer(rd, "AllCourseIndex");
        } catch (Exception e) {
            System.out.println("Couldn't load the file or couldn't index.");
        }
        System.out.println("Indexer finished.");
    }
}

