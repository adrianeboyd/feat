package org.purl.jh.speedsupport.speedPanel;

import org.purl.jh.speedsupport.nodes.BundleNode;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;
import javax.xml.soap.SOAPException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressRunnable;
import org.netbeans.api.progress.ProgressUtils;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupListener;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.purl.jh.speedsupport.data.CmdLayer;
import org.purl.jh.speedsupport.Conf;
import org.purl.jh.speedsupport.Main;
import org.purl.jh.speedsupport.conf.SyncPanel;
import org.purl.jh.util.err.Logger;

/**
 * Main Gui for interacting with the Speed server.
 */
@ConvertAsProperties(dtd = "-//org.purl.jh.speedsupport.speedPanel//SpeedPanel//EN", autostore = false)
@TopComponent.Description(
        preferredID = "SpeedPanel",
        iconBase="org/purl/jh/speedsupport/speedPanel/speedPanel.gif",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = true)
@ActionID(category = "Window", id = "org.purl.jh.speedsupport.speedPanel.SpeedPanel")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 380 ),
    @ActionReference(path = "Toolbars/Window", position = 380)
})
@TopComponent.OpenActionRegistration(displayName = "#CTL_ShowSpeedPanelAction", preferredID = "SpeedPanel")
public final class SpeedPanel extends TopComponent implements LookupListener {
    private static final Logger log = Logger.getLogger(SpeedPanel.class);
    //private static final Logger uilog = Logger.getLogger("org.netbeans.ui.Main");


    private ExplorerManager em = new ExplorerManager();
    private InstanceContent ic = new InstanceContent();

    Main main;

    public SpeedPanel() {
        initComponents();
        setName(NbBundle.getMessage(SpeedPanel.class, "CTL_SpeedPanel"));

        main = new Main(this); // find via lookup

        //refresh(speedInbox.getPath());
        xBoxPanel1.setPaths(main);
        xBoxPanel1.setTc(this);

        associateLookup(new ProxyLookup(new AbstractLookup(ic), ExplorerUtils.createLookup(em, getActionMap())));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        sync = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        toOutboxButton = new javax.swing.JButton();
        toInboxButton = new javax.swing.JButton();
        cleanupButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        xBoxPanel1 = new org.purl.jh.speedsupport.speedPanel.XBoxPanel();

        setPreferredSize(new java.awt.Dimension(400, 411));

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(sync, org.openide.util.NbBundle.getMessage(SpeedPanel.class, "SpeedPanel.sync.text_1")); // NOI18N
        sync.setToolTipText(org.openide.util.NbBundle.getMessage(SpeedPanel.class, "SpeedPanel.sync.toolTipText")); // NOI18N
        sync.setFocusable(false);
        sync.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        sync.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        sync.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncActionPerformed(evt);
            }
        });
        jToolBar1.add(sync);
        jToolBar1.add(jSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(toOutboxButton, org.openide.util.NbBundle.getMessage(SpeedPanel.class, "SpeedPanel.toOutboxButton.text")); // NOI18N
        toOutboxButton.setToolTipText(org.openide.util.NbBundle.getMessage(SpeedPanel.class, "SpeedPanel.toOutboxButton.toolTipText")); // NOI18N
        toOutboxButton.setFocusable(false);
        toOutboxButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toOutboxButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toOutboxButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toOutboxButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(toOutboxButton);

        org.openide.awt.Mnemonics.setLocalizedText(toInboxButton, org.openide.util.NbBundle.getMessage(SpeedPanel.class, "SpeedPanel.toInboxButton.text")); // NOI18N
        toInboxButton.setToolTipText(org.openide.util.NbBundle.getMessage(SpeedPanel.class, "SpeedPanel.toInboxButton.toolTipText")); // NOI18N
        toInboxButton.setFocusable(false);
        toInboxButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toInboxButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toInboxButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toInboxButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(toInboxButton);

        org.openide.awt.Mnemonics.setLocalizedText(cleanupButton, org.openide.util.NbBundle.getMessage(SpeedPanel.class, "SpeedPanel.cleanupButton.text")); // NOI18N
        cleanupButton.setFocusable(false);
        cleanupButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cleanupButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cleanupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cleanupButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(cleanupButton);

        jScrollPane1.setViewportView(xBoxPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    ProgressHandle progressHandle = null;

    public ProgressHandle getProgressHandle() {
        return progressHandle;
    }



    private void syncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncActionPerformed
        final SyncPanel confPanel = new SyncPanel();
        confPanel.setServerUrl(main.getConf().getSpeedUrl());
        confPanel.setUserName(main.getConf().getUserName());
        confPanel.setUserPassword(main.getConf().getUserPassword());
        confPanel.setSpeedFolder(main.getConf().getSpeedRootFile().getPath());

        confPanel.setInboxSyncEnabled(main.getConf().isInboxSyncEnabled());
        confPanel.setInboxCheckEnabled(main.getConf().isInboxCheckEnabled());
        confPanel.setReadAnySpeedFile(main.getConf().isReadAnySpeedFileEnabled());
        confPanel.setOutboxSyncEnabled(main.getConf().isOutboxSyncEnabled());

        DialogDescriptor descriptor = new DialogDescriptor(confPanel,
                "Config", // NbBundle.getMessage(BRCustomizer.class, "LBL_Customizer_InsertPrefix") + " " + displayName,
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION, null
        );

        descriptor.setValid(false);
        confPanel.setDialogDescriptor(descriptor);
        if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.OK_OPTION) return;

        // todo move to conf
        getPrefs().put(Conf.cSpeedUrl,     confPanel.getServerUrl());
        getPrefs().put(Conf.cUserName,     confPanel.getUserName());        // the user should not be allowed to easily change this, if yes, delete everything.
        getPrefs().put(Conf.cUserPassword, confPanel.getUserPassword());
        getPrefs().putBoolean(Conf.cReadAnySpeedFileEnabled, confPanel.getReadAnySpeedFile());
        getPrefs().putBoolean(Conf.cInboxSyncEnabled, confPanel.isInboxSyncEnabled());
        getPrefs().putBoolean(Conf.cInboxCheckEnabled, confPanel.isInboxCheckEnabled());
        getPrefs().putBoolean(Conf.cOutboxSyncEnabled, confPanel.isOutboxSyncEnabled());

        final ProgressRunnable<Void> syncRunnable = new ProgressRunnable<Void>() {
            @Override
            public Void run(ProgressHandle handle) {
                try {
                    SpeedPanel.this.progressHandle = handle;
                    main.sync(); // todo pass progress listener
                    handle.finish();
                    SpeedPanel.this.progressHandle = null;
                    return null;
                }
                catch(Throwable ex) {
                    log.info("syncActionPerformed - some error " + ex);
                    // todo under development, some of it needs to be moved lower, some will be reported via user logger
                    if (ex.getCause() instanceof SOAPException) {
                        if (ex.getCause().getCause() != null && ex.getCause().getCause().getCause() instanceof java.net.UnknownHostException) {
                            throw new RuntimeException("Speed server todo is not known. Check the url and your internet connection.");
                        }
                    }
                    if (ex.getCause() instanceof MalformedURLException) {
                        throw new RuntimeException("The speed url todo has a wrong format.");
                    }
                    else if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    }
                    else {
                        throw new RuntimeException(ex);
                    }
                }
            }
        };

        ProgressUtils.showProgressDialogAndRun(syncRunnable, "Synchronizing with the Speed server", true);

        xBoxPanel1.refresh();

    }//GEN-LAST:event_syncActionPerformed

   /**
     * Move selected items in inbox into outbox. (They must be closed)
     * @param evt
     */
    private void toOutboxButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toOutboxButtonActionPerformed
        for (Node node : xBoxPanel1.getExplorerManager().getSelectedNodes()) {
            if (node.getParentNode() != xBoxPanel1.getInboxNode()) continue;

            final BundleNode bundle = (BundleNode) node;
            // todo save&close???
            
            // todo !
            //final CmdLayer commandProvider = bundle.getLookup().lookup(CmdLayer.class);     // todo
            
            final CmdLayer commandProvider = bundle.getModel().getCmdLayer();
            
            if (commandProvider != null) {
                final List<Command> commands = commandProvider.getCommands();

                final CommandPanel p = new CommandPanel(commands, commandProvider.getCommand(), commandProvider.getComment());

                final NotifyDescriptor nd = new NotifyDescriptor.Confirmation(p, "Choose speed action for " + bundle.getName(), NotifyDescriptor.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) continue;

                commandProvider.setCommand(p.getCommand());
                commandProvider.setComment(p.getComment());
            }

            // todo save 
            main.moveToOutbox(bundle.getModel());
        }
        xBoxPanel1.getInboxNode().refresh();
        xBoxPanel1.getOutboxNode().refresh();
    }//GEN-LAST:event_toOutboxButtonActionPerformed

    private void toInboxButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toInboxButtonActionPerformed
        for (Node node : xBoxPanel1.getExplorerManager().getSelectedNodes()) {
            if (node.getParentNode() != xBoxPanel1.getOutboxNode()) continue;

            BundleNode bundle = (BundleNode) node;
            main.moveToInbox(bundle.getModel());
        }
        xBoxPanel1.getInboxNode().refresh();
        xBoxPanel1.getOutboxNode().refresh();
    }//GEN-LAST:event_toInboxButtonActionPerformed

    private void cleanupButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cleanupButtonActionPerformed
        main.cleanup();
        xBoxPanel1.getInboxNode().refresh();
        xBoxPanel1.getOutboxNode().refresh();
    }//GEN-LAST:event_cleanupButtonActionPerformed

    /**
     * Move selected items in inbox into outbox. (They must be closed)
     * @param evt
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cleanupButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton sync;
    private javax.swing.JButton toInboxButton;
    private javax.swing.JButton toOutboxButton;
    private org.purl.jh.speedsupport.speedPanel.XBoxPanel xBoxPanel1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void resultChanged(LookupEvent ev) {
        Collection<? extends FileObject> allInstances = result.allInstances();
        for (FileObject o : allInstances) {
            setDisplayName(o.getName());
            StatusDisplayer.getDefault().setStatusText("Selected: "+o.getPath());
        }
    }

    Result<FileObject> result;

    @Override
    public void componentOpened() {
        result = Utilities.actionsGlobalContext().lookupResult(FileObject.class);
        result.addLookupListener(this);
    }

    @Override
    public void componentClosed() {
        result.removeLookupListener(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private Preferences getPrefs() {
        return NbPreferences.forModule(getClass());
    }

}