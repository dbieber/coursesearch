import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

public class SearchClient {
        
    private static final int HITS_PER_PAGE = 10;
    
    private CourseSearcher searcher;
    private Desktop desktop;
    
    public SearchClient() throws CorruptIndexException {
        searcher = new CourseSearcher("AllCourseIndex519");
        desktop = Desktop.getDesktop();
        JFrame f = new SearchFrame();
        f.setVisible(true);
    }
    
    @SuppressWarnings("serial")
    private class SearchFrame extends JFrame implements ActionListener {
        
        private SearchTextField searchField;
        private ResultsPanel resultsPanel;
        
        public SearchFrame() {
            addWindowListener(new SearchFrameTerminator());
            setupWindow();
            setupContent();
        }
        
        public void setupWindow() {
            setTitle("Course Search Engine");
            setMinimumSize(new Dimension(400, 300));
            setSize(800,600);
            setLocation(10, 10);
        }
        
        public void setupContent() {
            setLayout(new GridBagLayout());
            
            Container c = getContentPane();
            c.setBackground(new Color(163, 73, 164));
            
            searchField = new SearchTextField();
            searchField.addActionListener(this);
            searchField.setBorder(new LineBorder(Color.BLACK));
            
            JButton searchButton = new SearchButton();
            searchButton.addActionListener(this);
            
            JPanel panel = new JPanel();
            panel.setBorder(new LineBorder(Color.BLACK));
            panel.add(searchField);
            panel.add(searchButton);
            
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.insets = new Insets(10,10,10,10);
            constraints.gridy = 0;
            
            add(panel, constraints);
        }
        
        private class SearchFrameTerminator extends WindowAdapter {
            public void windowClosing(WindowEvent e) {
                searcher.closeSearcher();
                System.exit(0);
            }
        }

        private void handleSearchClick() throws ParseException, IOException {
            String query = searchField.getText();
            Document[] results = SearchClient.this.search(query);
            if (resultsPanel == null) {
                resultsPanel = new ResultsPanel();
                
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.gridy = 1;
                add(resultsPanel, constraints);
            }
            
            resultsPanel.clear();
            for (Document d : results) {
                resultsPanel.addDocument(d);
            }
            resultsPanel.revalidate();
        }

        public void actionPerformed(ActionEvent e) {
            try {
                handleSearchClick();
            } catch (ParseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        private class SearchTextField extends JTextField {
            public SearchTextField() {
                super(24);
                this.setFont(new Font("", 0, 18));
            }
        }
        
        private class SearchButton extends JButton {
            public SearchButton() {
                super();
                this.setText("Search");
                this.setBackground(Color.WHITE);
            }
        }
        
        private class ResultsPanel extends JPanel {
            
            private JScrollPane resultsPane;
            private JList resultsList;
            private DefaultListModel resultsListModel;
            private ArrayList<Document> documents;
            
            public ResultsPanel() {
                super();
                resultsListModel = new DefaultListModel();
                documents = new ArrayList<Document>();
                
                resultsList = new JList(resultsListModel);
                MouseListener mouseListener = new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            int index = resultsList.locationToIndex(e.getPoint());
                            handleSelect(index);
                        }
                    }
                };
                KeyListener keyListener = new KeyAdapter() {
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            int index = resultsList.getSelectedIndex();
                            if (index >= 0) {
                                handleSelect(index);
                            }
                        }
                    }
                };
                resultsList.addMouseListener(mouseListener);
                resultsList.addKeyListener(keyListener);

                resultsPane = new JScrollPane(resultsList);
                add(resultsPane);
            }
            
            private void handleSelect(int index) {
                Document d = documents.get(index);
                String url = d.get(CourseDetails.COURSE_URL);
                System.out.println(d.get(CourseDetails.COURSE_URL));
                System.out.println(d.get(CourseDetails.COURSE));
                System.out.println(d.get(CourseDetails.PDF));
                try {
                    URI uri = new URI(url);
                    desktop.browse(uri);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (URISyntaxException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            public void addDocument(Document d) {
                String title = d.get(CourseDetails.TITLE);
                resultsListModel.addElement(title);
                documents.add(d);
            }

            public void clear() {
                resultsListModel.clear();
                documents.clear();
            }
        }
    }
    
    private Document[] search(String query) throws ParseException, IOException {
        Document[] results = searcher.search(query, HITS_PER_PAGE);
        return results;
    }
    
    public static void main(String[] args) throws CorruptIndexException {
        new SearchClient();
    }
    
}