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
import java.util.HashMap;

public class RegistrarData {
    
    public static final String URL = "http://registrar.princeton.edu/course-offerings/";

    HashMap<Integer, CourseDetails> courses; //maps classNum to CourseDetails
    
    public RegistrarData() {
        courses = new HashMap<Integer, CourseDetails>();
    }
    
    public void addCourseDetails(CourseDetails details) {
        Integer classNum = Integer.parseInt(
                details.get(CourseDetails.CLASS_NUM));
        //TODO merge
        courses.put(classNum, details);
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
        OutputStream file = new FileOutputStream( filename );
        OutputStream buffer = new BufferedOutputStream( file );
        ObjectOutput output = new ObjectOutputStream( buffer );
        output.writeObject( courses );
        output.close();
    }
}
