package org.purl.jh.feat.iaa;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JButton;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileChooserBuilder;
import org.purl.jh.util.gui.SimpleDocListener;

/**
 *
 * @author j
 */
public class IaaConfPanel extends javax.swing.JPanel {
    private final static org.purl.jh.util.Logger log = org.purl.jh.util.Logger.getLogger(IaaConfPanel.class);
    private final static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(IaaConfPanel.class);

    /** 
     * The ok button of the enclosing dialog (to be enabled/disabled based on 
     * the correctness of user's input 
     */
    private final JButton okButton;

    private final Conf data;
    
    /**
     * Creates new form IaaConfPanel
     */
    public IaaConfPanel(JButton aOkButton, Conf data) {
        this.okButton = aOkButton;
        this.data = data;

        initComponents();   // nb basic initialization cocde

        this.set1fld.setText(   data.getSet1().getAbsolutePath());
        this.set2fld.setText(   data.getSet2().getAbsolutePath());
        this.filterFld.setText( data.getFilter().toString() );
        this.lodzCompatibilityCheck.setSelected( data.isLodzCompatibility() );
        
        initControls();     // my initialization cocde
        
        final Color normalFilterBoxColor = filterFld.getForeground();

        filterFld.getDocument().addDocumentListener(new SimpleDocListener() {
            @Override
            public void textUpdated() {
                try {
                    Pattern.compile(filterFld.getText());
                    filterFld.setForeground(normalFilterBoxColor);   
                    okButton.setEnabled(true);
                }
                catch(PatternSyntaxException e) {
                    StatusDisplayer.getDefault().setStatusText(e.getMessage());
                    filterFld.setForeground(Color.red);             // todo get from config
                    okButton.setEnabled(false);
                }
            }
        });
        
    }

    private boolean retrieveData() {
        try {
            data.setFilter(Pattern.compile(filterFld.getText()));
        }
        catch(PatternSyntaxException e) {
            return false;
        }

        // todo temporary hack
        if (filterFld.getText().endsWith("a\\.xml")) {
            data.setOut_TagOrder(Arrays.asList(
                "incor*", "incorBase", "incorInfl", "wbd*", "wbdPre", "wbdOther", "wbdComp", 
                "fw*", "fwNc", "fwFab", "stylColl"));
        }
        else {
            data.setOut_TagOrder(Arrays.asList(
                "agr", "dep", "rflx", "lex", "neg", "ref", "sec", "stylColl", "use", "vbx"));
        }
        
        data.setLodzCompatibility(this.lodzCompatibilityCheck.isSelected());
        return 
                data.setSet1( this.set1fld.getText() ) &&
                data.setSet2( this.set2fld.getText() );
    }
    
    
    public Conf getData() {
        retrieveData();
        return data;
    }

    
    private void initControls() {
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        set1label = new javax.swing.JLabel();
        set1fld = new javax.swing.JTextField();
        set2label = new javax.swing.JLabel();
        set2fld = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        filterFld = new javax.swing.JTextField();
        lodzCompatibilityCheck = new javax.swing.JCheckBox();
        set1Browse = new javax.swing.JButton();
        set2Browse = new javax.swing.JButton();
        bLayerFilterBtn = new javax.swing.JButton();
        aLayerFilterBtn = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(set1label, org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.set1label.text")); // NOI18N

        set1fld.setText(org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.set1fld.text")); // NOI18N
        set1fld.setEnabled(false);
        set1fld.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set1fldActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(set2label, org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.set2label.text")); // NOI18N

        set2fld.setText(org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.set2fld.text")); // NOI18N
        set2fld.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.jLabel1.text")); // NOI18N

        filterFld.setText(org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.filterFld.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lodzCompatibilityCheck, org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.lodzCompatibilityCheck.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(set1Browse, org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.set1Browse.text")); // NOI18N
        set1Browse.setEnabled(false);
        set1Browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set1BrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(set2Browse, org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.set2Browse.text")); // NOI18N
        set2Browse.setEnabled(false);
        set2Browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                set2BrowseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bLayerFilterBtn, org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.bLayerFilterBtn.text")); // NOI18N
        bLayerFilterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bLayerFilterBtnActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(aLayerFilterBtn, org.openide.util.NbBundle.getMessage(IaaConfPanel.class, "IaaConfPanel.aLayerFilterBtn.text")); // NOI18N
        aLayerFilterBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aLayerFilterBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(set2label, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 75, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(filterFld, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(aLayerFilterBtn)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bLayerFilterBtn))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(set2fld))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(set1label, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(set1fld))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lodzCompatibilityCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(set1Browse)
                    .addComponent(set2Browse))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(set1label)
                    .addComponent(set1fld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(set1Browse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(set2label)
                    .addComponent(set2fld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(set2Browse))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(filterFld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bLayerFilterBtn)
                    .addComponent(aLayerFilterBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lodzCompatibilityCheck)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void set1BrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set1BrowseActionPerformed
        File inFile = new FileChooserBuilder("IaaConfPanel.open.dir")
              .setTitle(bundle.getString("IaaConfPanel.browse1.title"))
              .setFilesOnly(false)   
                //.setCurrentDirectory
              //.setDefaultWorkingDirectory(defFile)
              .setApproveText(bundle.getString("IaaConfPanel.open.button"))
              //.addFileFilter(FileFilters.endingFilter("*.xml", bundle.getString("IaaConfPanel.open.filter.xml")))
              .showOpenDialog();

        if (inFile != null) {
            set1fld.setText(inFile.getPath());
            //data.setSet1(inFile.getPath());
        }
    }//GEN-LAST:event_set1BrowseActionPerformed

    private void set2BrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set2BrowseActionPerformed
        File inFile = new FileChooserBuilder("IaaConfPanel.open.dir")
              .setTitle(bundle.getString("IaaConfPanel.browse2.title"))
              .setFilesOnly(false)   
                //.setCurrentDirectory
              //.setDefaultWorkingDirectory(defFile)
              .setApproveText(bundle.getString("IaaConfPanel.open.button"))
              //.addFileFilter(FileFilters.endingFilter("*.xml", bundle.getString("IaaConfPanel.open.filter.xml")))
              .showOpenDialog();

        if (inFile != null) {
            set2fld.setText(inFile.getPath());
            //data.setSet1(inFile.getPath());
        }
    }//GEN-LAST:event_set2BrowseActionPerformed

    private void set1fldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_set1fldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_set1fldActionPerformed

    private void aLayerFilterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aLayerFilterBtnActionPerformed
        filterFld.setText(".*\\.a\\.xml");
    }//GEN-LAST:event_aLayerFilterBtnActionPerformed

    private void bLayerFilterBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bLayerFilterBtnActionPerformed
        filterFld.setText(".*\\.b\\.xml");
    }//GEN-LAST:event_bLayerFilterBtnActionPerformed

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton aLayerFilterBtn;
    private javax.swing.JButton bLayerFilterBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField filterFld;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JCheckBox lodzCompatibilityCheck;
    private javax.swing.JButton set1Browse;
    private javax.swing.JTextField set1fld;
    private javax.swing.JLabel set1label;
    private javax.swing.JButton set2Browse;
    private javax.swing.JTextField set2fld;
    private javax.swing.JLabel set2label;
    // End of variables declaration//GEN-END:variables
}
