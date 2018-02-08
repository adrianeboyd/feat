package org.purl.jh.feat.layered;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import org.openide.util.Exceptions;

/**
 * Adapted from: https://stackoverflow.com/a/13438455/461847
 *
 * @author adriane
 */
public class FindTextPane extends JPanel {

    private JTextField findField;
    public JButton findButton;
    private JTextArea textArea;
    private String prevFind = null;
    private int pos = 0;

    public FindTextPane(Reader reader) throws IOException {
        setLayout(new BorderLayout());
        findButton = new JButton("Next");
        findField = new JTextField("", 10);

        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setText(String.join("\n\n", HTMLUtils.extractText(reader)));
        textArea.setCaretPosition(0);

        Document document = textArea.getDocument();
        String tempDocumentText = null;
        try {
            tempDocumentText = document.getText(0, document.getLength()).toLowerCase();
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        final String searchableDocumentText;
        if (tempDocumentText == null) {
            searchableDocumentText = "";
        } else {
            searchableDocumentText = tempDocumentText;
        }

        JPanel header = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        header.add(findField, gbc);
        gbc.gridx++;
        header.add(findButton, gbc);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(textArea));

        final Highlighter highlighter = textArea.getHighlighter();
        final HighlightPainter painter
                = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY);

        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the text to find, convert it to lower case for easier comparison
                String find = findField.getText().toLowerCase();
                highlighter.removeAllHighlights();

                // If the search field has changed, reset position to the beginning of the document
                if (!find.equals(prevFind)) {
                    pos = 0;
                    prevFind = find;
                }

                // Make sure we have a valid search term
                if (find.length() > 0) {
                    int matchPos = searchableDocumentText.substring(pos).indexOf(find);

                    if (matchPos >= 0) {
                        try {
                            // Get the rectangle of the where the text would be visible...
                            Rectangle viewRect = textArea.modelToView(pos + matchPos);
                            // Scroll to make the rectangle visible
                            textArea.scrollRectToVisible(viewRect);

                            // Highlight the text
                            highlighter.addHighlight(pos + matchPos, pos + matchPos + find.length(), painter);
                            
                            // Move the search position beyond the current match
                            pos = pos + matchPos + 1;

                            // Skip back to the top if there are no more matches
                            if (!searchableDocumentText.substring(pos).contains(find)) {
                                pos = 0;
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        });
    }
}
