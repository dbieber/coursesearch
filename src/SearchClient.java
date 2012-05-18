import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class SearchClient {
    
    @SuppressWarnings("serial")
    private static class SearchFrame extends JFrame {
        public SearchFrame() {
            addWindowListener(new SearchFrameTerminator());
            setupWindow();
            setupContent();
        }
        
        public void setupWindow() {
            setTitle("Course Search Engine");
            setMinimumSize(new Dimension(300, 200));
            setSize(600,400);
            setLocation(10, 10);
        }
        
        public void setupContent() {
            Container c = getContentPane();
            setLayout(new GridBagLayout());
            c.setBackground(new Color(163, 73, 164));
            JPanel panel = new JPanel();
            panel.setBorder(new LineBorder(Color.BLACK));
            JTextField searchField = new SearchTextField();
            searchField.setBorder(new LineBorder(Color.BLACK));
            panel.add(searchField);
            panel.add(new SearchButton());
            add(panel, new GridBagConstraints());
        }
        
        private class SearchFrameTerminator extends WindowAdapter {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
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
    }
    
    public static void main(String[] args) {
        JFrame f = new SearchFrame();
        f.setVisible(true);
    }
    
}