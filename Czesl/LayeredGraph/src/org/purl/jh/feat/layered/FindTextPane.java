/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.purl.jh.feat.layered;

import java.awt.BorderLayout;
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
import javax.swing.text.Document;
import org.openide.util.Exceptions;

/**
 * Adapted from: https://stackoverflow.com/a/13438455/461847
 *
 * @author adriane
 */
public class FindTextPane extends JPanel {

    private JTextField findField;
    private JButton findButton;
    private JTextArea textArea;
    private int pos = 0;

    public FindTextPane(Reader reader) throws IOException {
        setLayout(new BorderLayout());
        findButton = new JButton("Next");
        findField = new JTextField("", 10);
        textArea = new JTextArea();
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        textArea.setText(String.join("\n\n", HTMLUtils.extractText(reader)));
        textArea.setCaretPosition(0);
        textArea.setFont(new java.awt.Font("Tahoma", 0, 12));

        reader.close();

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

        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Get the text to find...convert it to lower case for eaiser comparision
                String find = findField.getText().toLowerCase();
                // Focus the text area, otherwise the highlighting won't show up
                textArea.requestFocusInWindow();
                // Make sure we have a valid search term
                if (find != null && find.length() > 0) {
                    Document document = textArea.getDocument();
                    int findLength = find.length();
                    boolean found = false;
                    // Rest the search position if we're at the end of the document
                    if (pos + findLength > document.getLength()) {
                        pos = 0;
                    }
                        // While we haven't reached the end...
                    // "<=" Correction
                    while (pos + findLength <= document.getLength()) {
                        try {
                            // Extract the text from the docuemnt
                            String match = document.getText(pos, findLength).toLowerCase();
                            // Check to see if it matches or request
                            if (match.equals(find)) {
                                found = true;
                                break;
                            }
                            pos++;
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                    // Did we find something...
                    if (found) {
                        try {
                            // Get the rectangle of the where the text would be visible...
                            Rectangle viewRect = textArea.modelToView(pos);
                            // Scroll to make the rectangle visible
                            textArea.scrollRectToVisible(viewRect);
                            // Highlight the text
                            textArea.setCaretPosition(pos + findLength);
                            textArea.moveCaretPosition(pos);
                            // Move the search position beyond the current match
                            pos += findLength;
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }

                }
            }
        });

    }
}
