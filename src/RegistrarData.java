import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;

public class RegistrarData {
    
    public static final String URL = "http://registrar.princeton.edu/course-offerings/";

    private HashMap<Integer, CourseDetails> courses; //maps courseId to CourseDetails
    
    public RegistrarData() {
        courses = new HashMap<Integer, CourseDetails>();
    }
    
    public void addCourseDetails(CourseDetails details) {
        int courseId = details.courseId();
        CourseDetails oldDetails = courses.get(courseId);
        if (oldDetails == null) {
            courses.put(courseId, details);
        } else {
            for (String key : details.keySet()) {
                oldDetails.put(key, details.get(key));
            }
        }
    }
    
    public Collection<CourseDetails> courseDetails() {
        return courses.values();
    }

    public CourseDetails courseDetails(int courseId) {
        return courses.get(courseId);
    }
    
    @SuppressWarnings("unchecked")
    public void load(String filename) throws IOException, ClassNotFoundException {
    	InputStream file = new FileInputStream( filename );
    	InputStream buffer = new BufferedInputStream( file );
        ObjectInput input = new ObjectInputStream ( buffer );
        
        courses = (HashMap<Integer, CourseDetails>)input.readObject();
        input.close();
    }

    public void dump(String filename) throws IOException {
        /* TODO Better to write to temporary file first, then replace
         * old file with temporary one, since dump may not complete. */
        OutputStream file = new FileOutputStream( filename );
        OutputStream buffer = new BufferedOutputStream( file );
        ObjectOutput output = new ObjectOutputStream( buffer );
        output.writeObject( courses );
        output.close();
    }
}
