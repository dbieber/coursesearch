import java.io.IOException;


public class RegistrarData {
    
    private String registrarURL = "http://registrar.princeton.edu/course-offerings/";
    
    public RegistrarData() {
        
    }
    
    public void scrape() {
        RegistrarScraper rs = new RegistrarScraper();
        try {
            rs.scrapeRegistrar(registrarURL);
        } catch (IOException e) {}
    }
    
    public void load(String filename) {
    }
    
    public void dump(String filename) {
    }
}
