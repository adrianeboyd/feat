package org.purl.jh.feat.diffui;

import java.io.File;
import javax.swing.JButton;
import org.openide.filesystems.FileChooserBuilder;
import org.purl.jh.util.io.FileFilters;

/**
 * @todo
 * @author jirka
 */
public class OpenFolderPanel extends javax.swing.JPanel {
    private final static org.purl.jh.util.Logger log = org.purl.jh.util.Logger.getLogger(OpenFolderPanel.class);
    private final static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(OpenFolderPanel.class);

    /**
     * The ok button of the enclosing dialog (to be enabled/disabled based on
     * the correctness of user's input
     */
    private final JButton okButton;

    /** Creates new form ImportPanel
     *
     * @param aOkButton ok button so that it can be enabled/disabled
     *
     * @todo pass list of formats, list of encodings, list of tagsets
     * @todo pass things that determines the auto encoding/format
     * @todo pass default tagset
     * @todo allwo specifying how to remove extension(s) from the in name
     */
    public OpenFolderPanel(JButton aOkButton) {
        okButton = aOkButton;

        initComponents();   // nb basic initialization cocde
        initControls();     // my initialization cocde
    }

    public File getFile1() {
        return new File(inFile1Text.getText());
    }

    public File getFile2() {
        return new File(inFile2Text.getText());
    }


    private void initControls() {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        inFile1Label = new javax.swing.JLabel();
        inFile1Text = new javax.swing.JTextField();
        inFile1BrowseButton = new javax.swing.JButton();
        inFile2Label = new javax.swing.JLabel();
        inFile2Text = new javax.swing.JTextField();
        inFile2BrowseButton = new javax.swing.JButton();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(OpenFolderPanel.class, "OpenFolderPanel.jLabel1.text")); // NOI18N

        inFile1Label.setText(org.openide.util.NbBundle.getMessage(OpenFolderPanel.class, "OpenFolderPanel.inFile1Label.text")); // NOI18N

        inFile1Text.setText(org.openide.util.NbBundle.getMessage(OpenFolderPanel.class, "OpenFolderPanel.inFile1Text.text")); // NOI18N

        inFile1BrowseButton.setText(org.openide.util.NbBundle.getMessage(OpenFolderPanel.class, "OpenFolderPanel.inFile1BrowseButton.text")); // NOI18N
        inFile1BrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inFile1BrowseButtonActionPerformed(evt);
            }
        });

        inFile2Label.setText(org.openide.util.NbBundle.getMessage(OpenFolderPanel.class, "OpenFolderPanel.inFile2Label.text")); // NOI18N

        inFile2Text.setText(org.openide.util.NbBundle.getMessage(OpenFolderPanel.class, "OpenFolderPanel.inFile2Text.text")); // NOI18N

        inFile2BrowseButton.setText(org.openide.util.NbBundle.getMessage(OpenFolderPanel.class, "OpenFolderPanel.inFile2BrowseButton.text")); // NOI18N
        inFile2BrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inFile2BrowseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inFile2Label, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inFile1Label, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inFile1Text, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                    .addComponent(inFile2Text, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(inFile1BrowseButton)
                    .addComponent(inFile2BrowseButton))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {inFile1Label, inFile2Label});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {inFile1BrowseButton, inFile2BrowseButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inFile1Label)
                    .addComponent(inFile1Text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inFile1BrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inFile2Label)
                    .addComponent(inFile2Text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inFile2BrowseButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void inFile1BrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inFile1BrowseButtonActionPerformed
        browse(inFile1Text);
    }//GEN-LAST:event_inFile1BrowseButtonActionPerformed

    private void inFile2BrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inFile2BrowseButtonActionPerformed
        browse(inFile2Text);
    }//GEN-LAST:event_inFile2BrowseButtonActionPerformed

    private void browse(javax.swing.JTextField aTextFld) {
        // todo start with current file
        File file = new FileChooserBuilder("diff.open.dir")
              .setTitle(bundle.getString("open.browse.title"))
              //.setFilesOnly(true)
                //.setDefaultWorkingDirectory(defFile)
              .setApproveText(bundle.getString("open.browse.button"))
              //.addFileFilter(FileFilters.endingFilter("b.xml", bundle.getString("open.browse.filter.b.xml")))
              .showOpenDialog();

        if (file == null) return;
        aTextFld.setText(file.getPath());
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton inFile1BrowseButton;
    private javax.swing.JLabel inFile1Label;
    private javax.swing.JTextField inFile1Text;
    private javax.swing.JButton inFile2BrowseButton;
    private javax.swing.JLabel inFile2Label;
    private javax.swing.JTextField inFile2Text;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

}
