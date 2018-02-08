package org.purl.jh.feat.layered;


import org.purl.jh.feat.profiles.ProfileRegistry;
import org.purl.jh.feat.profiles.Profile;
import com.google.common.base.Preconditions;
import cz.cuni.utkl.czesl.data.layerl.Edge;
import cz.cuni.utkl.czesl.data.layerl.LPara;
import cz.cuni.utkl.czesl.data.layerw.WForm;
import cz.cuni.utkl.czesl.data.layerw.WPara;
import cz.cuni.utkl.czesl.data.layerx.FForm;
import cz.cuni.utkl.czesl.data.layerx.FormsLayer;
import cz.cuni.utkl.czesl.data.layerx.Para;
import java.awt.BorderLayout;
import org.purl.jh.feat.util0.gui.pager.CurListener;
import java.awt.event.FocusEvent;
import javax.swing.event.ChangeEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyVetoException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.print.ScenePrinter;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.purl.jh.feat.NbData.LLayerDataObject;
import org.purl.jh.feat.NbData.WLayerDataObject;
import org.purl.jh.pml.location.AcceptingLocation;
import org.purl.jh.feat.NbData.view.LLayerView;
import org.purl.jh.feat.NbData.view.WLayerView;
import org.purl.jh.feat.layered.util.FeatDataUtil;
import org.purl.jh.feat.layered.util.ObjectSceneListenerAdapter;
import org.purl.jh.nbpml.LayerDataObject;
import org.purl.jh.nbpml.LayerProvider;
import org.purl.jh.pml.Element;
import org.purl.jh.pml.Layer;
import org.purl.jh.pml.location.Location;
import org.purl.net.jh.nb.img.ImgComponent;
import org.purl.net.jh.nbutil.NbUtil;
import org.purl.net.jh.nbutil.ZoomModel;
import org.purl.net.jh.nbutil.ZoomUtil;
import org.purl.jh.util.col.Cols;
import org.purl.jh.util.gui.list.Combos;
import org.purl.net.jh.nb.html.JHtmlPane;
import org.purl.net.jh.nbutil.visual.SceneSupport;


/**
 * Top component displaying the layered error annotation graph
 */
public final class LayeredViewTopComponent extends CloneableTopComponent implements ExplorerManager.Provider, AcceptingLocation {
    private final static org.purl.jh.util.Logger log = org.purl.jh.util.Logger.getLogger(LayeredViewTopComponent.class);

    // Keys used to store preferences
    private final static String PREF_TWO_VIEWS = "twoViews";
    private final static String PREF_XSPACING = "xSpacing";
    private final static String PREF_YSPACING = "ySpacing";
    private final static String PREF_PROFILE = "profile";
    private final static String PREF_ZOOM_FACTOR = "zoomFactor";


    //@ServiceProvider(service=LLayerView.class)
    public static class LLayerViewSupport implements LLayerView {
        @Override
        public CloneableTopComponent getTopComponent(DataObject aDObj) {
            return (aDObj == null || !(aDObj instanceof LLayerDataObject)) ?
                null : new org.purl.jh.feat.layered.LayeredViewTopComponent( (LayerDataObject<?>)aDObj );
        }
    }

    @ServiceProvider(service=WLayerView.class)
    public static class WLayerViewSupport implements WLayerView {
        @Override
        public CloneableTopComponent getTopComponent(DataObject aDObj) {
            return (aDObj == null || !(aDObj instanceof WLayerDataObject)) ?
                null : new org.purl.jh.feat.layered.LayeredViewTopComponent( (LayerDataObject<?>)aDObj );
        }
    }


    public final int cSliderMax = 50;

    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "LayeredViewTopComponent";
    
    /** Facade for the stack of layers (provides only read-only functions) */
    private VModel pseudoModel = null;
    /** Stack of current paragraphs */
    private ParaModel curParaModel;

    private Profile profile;
    
    //private LayeredXGraph xgraph;
    private LayeredGraph graph, graph2;
    private JScrollPane graphScrollPane;
    private Component graphComponent;
    private ImgComponent img;
    private JEditorPane textView;
    private final PrintWriter out = NbUtil.getOut();

    private int curPara = -1;

    // cursor-like object todo under development
    private Object curObject;        // usually form, possibly edge, etc.

    InstanceContent lookupContent = new InstanceContent();
    InstanceContent nodesLookupContent = new InstanceContent();


    public LayeredViewTopComponent() {
        initComponents();
        gotoControl1.setOpaque(false);
        gotoControl1.setBorder(new SoftBevelBorder(BevelBorder.RAISED));        

        profileCombo.setModel( Combos.getModel(new ArrayList<>(ProfileRegistry.ids())) );    // todo sort (streams)
        
        //associateLookup(new ProxyLookup(getLookup(),new AbstractLookup(lookupContent)));  // cannot do this as getLookup creates the lookup and then it cannot be changed :)

        // todo tmp trying nodeselection
        //Lookup lookup=ExplorerUtils.createLookup(mngr, getActionMap());
        associateLookup(new ProxyLookup(new AbstractLookup(lookupContent), new AbstractLookup(nodesLookupContent)));

        //gotoControl1.sizeChanged(WIDTH);
        gotoControl1.addListener(new CurListener() {
            @Override
            public void curChanged(int aIdx) {
                requestPara(aIdx);
            }

            @Override
            public void sizeChanged(int aNewSize) {
                log.warning("unhandled size change");
            }
        });

        graphZoomControl.init();
        graphZoomControl.getModel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateZoom();
            }
        });


        setName(NbBundle.getMessage(LayeredViewTopComponent.class, "CTL_LayeredViewTopComponent"));
        setToolTipText(NbBundle.getMessage(LayeredViewTopComponent.class, "HINT_LayeredViewTopComponent"));
    }

    public LayeredViewTopComponent(final LayerDataObject<?> aDObj) {
        this();

        lookupContent.add(aDObj);

        final LayerProvider layerProvider = aDObj.getNodeDelegate().getLookup().lookup(LayerProvider.class);
        final Layer<?> lyr = layerProvider.getLayer(null);
        if (lyr == null || !(lyr instanceof FormsLayer<?>) )  return; // todo not sure this is a good way
        
        //layer = (FormsLayer<?>)lyr;
        
        pseudoModel = new VModel(lyr, undoMngr);

        // todo development
        lookupContent.add(pseudoModel);
        lookupContent.add(pseudoModel.getwLayer());
        lookupContent.add(
                new NavigatorLookupHint() {
                    @Override
                    public String getContentType() {
                        return "text/feat-model";
                    }
                });


        addSaveSupport(aDObj);

        final String profileId = getPrefs().get(PREF_PROFILE, null);
        final Profile tmp = ProfileRegistry.get(profileId);
        setProfile(tmp == null ? ProfileRegistry.get(ProfileRegistry.EMPTY_PROFILE_ID) : tmp);
        
        displaySubViews();

        // initialize controls with saved values (must be the same as in showParaGraph below)
        xSpacingControl.setValue( getPrefs().getInt(PREF_XSPACING, 100) );
        ySpacingControl.setValue( getPrefs().getInt(PREF_YSPACING, 100) );
        graphZoomControl.getModel().setVal( getPrefs().getDouble(PREF_ZOOM_FACTOR, 1.0) );
        splitToggle.setSelected(  getPrefs().getBoolean(PREF_TWO_VIEWS, false) );

        pseudoModel.addChangeListener(widgetNode);
        
        gotoControl1.sizeChanged(pseudoModel.getParas().size()); // could/should be done via listening
        requestPara(0);
        
    }


    private void displaySubViews() {
        if (pseudoModel.getTextLayer() != null) {
            textView = new JHtmlPane();
            ((JHtmlPane)textView).addCursorListener(new JHtmlPane.CursorListener() {
                @Override
                public void cursorChanged(int aPos) {
                    moveToFormWithChar(aPos);
                }
            });  // todo under dev
            ((JHtmlPane)textView).initKeys();
            try {
                pseudoModel.getTextLayer().getDoc().setBase(pseudoModel.getTextLayer().getFile().getURL());
            } catch (FileStateInvalidException ex) {
                log.warning("Cannot obtain Html's base url, do=%s", pseudoModel.getTextLayer().getFile());
            }

            textView.setEditorKit(pseudoModel.getTextLayer().getKit());
            textView.setDocument(pseudoModel.getTextLayer().getDoc());
            textView.setEditable(false);

            jSplitPane2.setLeftComponent(new JScrollPane(textView));
        } else {
            jSplitPane2.setLeftComponent(new JPanel());
        }

        if (pseudoModel.getImgLayer() != null) {
            img = new ImgComponent(pseudoModel.getImgLayer().getImg(), imgZoomControl.getModel());
            imgZoomControl.init();
            imgScrollPane.setViewportView(img);

        } else {
            imgScrollPane.add(new JPanel());
        }
    }

    @Override
    protected void componentOpened() {
//        jSplitPane1.setDividerLocation(getPrefs().getInt("LayeredViewTopComponent.splitPanel1", 400));
//        jSplitPane2.setDividerLocation(getPrefs().getInt("LayeredViewTopComponent.splitPanel2", 600));

// causese freeze??
//            String UI_LOGGER_NAME = "org.netbeans.ui.mymodule";
//            LogRecord record = new LogRecord(Level.INFO, "MY_UI_ACTION_APPEARED");
//            record.setLoggerName(UI_LOGGER_NAME);
//            record.setParameters(new Object[] {
//                   "action param 1", "action param 2"
//            });
//            record.setResourceBundle(NbBundle.getBundle(LayeredViewTopComponent.class));
//            Logger.getLogger(UI_LOGGER_NAME).log(record);

    }

    @Override
    public void componentClosed() {
        // todo do via properties, clone, etc.
        getPrefs().putInt("LayeredViewTopComponent.splitPanel1", jSplitPane1.getDividerLocation());
        getPrefs().putInt("LayeredViewTopComponent.splitPanel2", jSplitPane2.getDividerLocation());
    }

    @Override
    public void addNotify() {
        super.addNotify();

//        jSplitPane1.setDividerLocation(getPrefs().getInt("LayeredViewTopComponent.splitPanel1", 400));
//        jSplitPane2.setDividerLocation(getPrefs().getInt("LayeredViewTopComponent.splitPanel2", 600));
        jSplitPane1.setDividerLocation(400);
        jSplitPane2.setDividerLocation(600);
    }


    @Override
    protected void componentActivated() {
    }

    


// =============================================================================
// Saving associated dataobjets
// =============================================================================
    /** Saves all layers that need saving (todo should probably work with databoject not w/ layers)*/
    private SaveCookie combinedSaveCookie = new SaveCookie() {
        @Override
        public void save() throws IOException {
            LayerModelSupport.save(pseudoModel.getTopLayer());
        }
    };


    //private List<Lookup> dobjslookups;

//    /** TODO: currently not registered to any source and the cookie is always available */
//    class ModificationListener implements LayerListener {
//        public void onBasicUpdate(BasicLayerEvent aE) {
//            if (aE.getEventId() == BasicLayerEvent.EventId.modifiedChange) {
//                if (aE.getSrc().isModified()) {
//                    lookupContent.add(combinedSaveCookie);
//                }
//                else {
//                    // check if remove or keep cookie
//                    boolean modified = false;
//                    for (Layer<?> layer : layers) {
//                        modified |= layer.isModified();
//                    }
//                    if (modified) {
//                        lookupContent.add(combinedSaveCookie);
//                    }
//                    else {
//                        lookupContent.remove(combinedSaveCookie);
//                    }
//                }
//            }
//        }
//    }



    // TODO better
    private void addSaveSupport(final LayerDataObject<?> aDObj) {
        lookupContent.add(combinedSaveCookie);
        //associateLookup( Lookups.fixed(combinedSaveCookie, aDObj));
    }

    // TODO use this instead of the save-always enabled above
//    private void addSaveSupportX(final LayerDataObject<?> aDObj) {
//        out.println("TC.addSaveSupport " + aDObj);
//        associateLookup(new AbstractLookup(lookupContent));     // place for the combined save cookie
//
//        dobjslookups = collectLookups(aDObj);
//
//        Lookup combinedLookup = new ProxyLookup(dobjslookups.toArray(new Lookup[0]));
//        Lookup.Result<SaveCookie> saveCookieResult = combinedLookup.lookupResult(SaveCookie.class);
//        saveCookieResult.allItems();
//
//        saveCookieListener = new SaveCookieListener(saveCookieResult);
//        saveCookieResult.addLookupListener(saveCookieListener);
//    }

//    private void collectLookups(final List<Lookup> aLookups, final LayerDataObject<?> adobj) {
//        log.info("Adding lookup of " + adobj);
//        aLookups.add(adobj.getLookup());
//        for (LayerDataObject<?> dobj : adobj.getLinkedDObjs()) {
//            collectLookups(aLookups, dobj);
//        }
//    }
//
//    private List<Lookup> collectLookups(final LayerDataObject<?> aDObj) {
//        log.info("Starting lookup collection (" + aDObj + ")");
//        final List<Lookup> lookups = new ArrayList<Lookup>();
//        collectLookups(lookups, aDObj);
//        return lookups;
//    }

    class SaveCookieListener implements LookupListener {
        private final Lookup.Result<SaveCookie> lookupResult;

        SaveCookieListener(Lookup.Result<SaveCookie> aLookupResult) {
            lookupResult = aLookupResult;
        }

        @Override
        public void resultChanged(LookupEvent e) {
            final Collection<? extends SaveCookie> saveCookies = lookupResult.allInstances();
            log.info("SaveCookieListener.resultChanged");

            if (saveCookies.isEmpty()) {
                assert combinedSaveCookie != null;
                lookupContent.remove(combinedSaveCookie);
            } else {
                combinedSaveCookie = new SaveCookie() {
                    @Override
                    public void save() throws IOException {
                        for (Layer<?> l : pseudoModel.getLayers()) {
                            try {
                                DataObject dobj = DataObject.find(l.getFile());
                                log.info(" saveLayer-referenced: layer=%s, dobj=%s\n", l, dobj);
                                SaveCookie save = dobj.getCookie(SaveCookie.class);
                                if (save != null) {
                                    save.save();
                                }
                            } catch (DataObjectNotFoundException ex) {
                                throw new RuntimeException(ex); // DataObject must exist
                            }
                        }
                    }
                };
//                        lookupResult.removeLookupListener(SaveCookieListener.this);
//                        for (SaveCookie layerCookie : saveCookies) {
//                            layerCookie.save();
//                        }
//                        lookupResult.allItems();
//                        lookupResult.addLookupListener(SaveCookieListener.this);
//                        resultChanged(null);
//                    }
//                };
//                lookupContent.add(combinedSaveCookie);
            }
        }
    }

// =============================================================================


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imgZoomButtonGroup = new javax.swing.ButtonGroup();
        jLabel2 = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        gotoControl1 = new org.purl.jh.feat.util0.gui.pager.GotoControl();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        splitToggle = new javax.swing.JToggleButton();
        refreshViewButton = new javax.swing.JButton();
        exportButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        textSearchButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        jLabel1 = new javax.swing.JLabel();
        xSpacingControl = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        ySpacingControl = new javax.swing.JSpinner();
        graphZoomControl = new org.purl.net.jh.nbutil.ZoomControl();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        profileLabel = new javax.swing.JLabel();
        profileCombo = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        imgScrollPane = new javax.swing.JScrollPane();
        imgZoomControl = new org.purl.net.jh.nbutil.ZoomControl();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.jLabel2.text")); // NOI18N

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        gotoControl1.setFloatable(false);
        jToolBar1.add(gotoControl1);
        jToolBar1.add(jSeparator4);

        splitToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/purl/jh/feat/layered/icons/application_split.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(splitToggle, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.splitToggle.text_1")); // NOI18N
        splitToggle.setToolTipText(org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.splitToggle.toolTipText")); // NOI18N
        splitToggle.setFocusable(false);
        splitToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        splitToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        splitToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitToggleActionPerformed(evt);
            }
        });
        jToolBar1.add(splitToggle);

        refreshViewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/purl/jh/feat/layered/icons/arrow_refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refreshViewButton, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.refreshViewButton.text_1")); // NOI18N
        refreshViewButton.setToolTipText(org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.refreshViewButton.toolTipText")); // NOI18N
        refreshViewButton.setFocusable(false);
        refreshViewButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshViewButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshViewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshViewButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(refreshViewButton);

        exportButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/purl/jh/feat/layered/icons/picture_save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(exportButton, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.exportButton.text_1")); // NOI18N
        exportButton.setToolTipText(org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.exportButton.toolTipText")); // NOI18N
        exportButton.setFocusable(false);
        exportButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        exportButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        exportButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(exportButton);

        printButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/purl/jh/feat/layered/icons/document-print.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(printButton, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.printButton.text_1")); // NOI18N
        printButton.setToolTipText(org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.printButton.toolTipText")); // NOI18N
        printButton.setFocusable(false);
        printButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        printButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(printButton);

        textSearchButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/purl/jh/feat/layered/icons/research.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(textSearchButton, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.textSearchButton.text")); // NOI18N
        textSearchButton.setToolTipText(org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.textSearchButton.toolTipText")); // NOI18N
        textSearchButton.setFocusable(false);
        textSearchButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        textSearchButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        textSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textSearchButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(textSearchButton);
        jToolBar1.add(jSeparator3);
        jToolBar1.add(jSeparator2);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.jLabel1.text_1")); // NOI18N
        jToolBar1.add(jLabel1);

        xSpacingControl.setToolTipText(org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.xSpacingControl.toolTipText")); // NOI18N
        xSpacingControl.setMaximumSize(new java.awt.Dimension(40, 20));
        xSpacingControl.setPreferredSize(new java.awt.Dimension(60, 20));
        xSpacingControl.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                xSpacingControlStateChanged(evt);
            }
        });
        jToolBar1.add(xSpacingControl);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.jLabel3.text_1")); // NOI18N
        jToolBar1.add(jLabel3);

        ySpacingControl.setToolTipText(org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.ySpacingControl.toolTipText")); // NOI18N
        ySpacingControl.setMaximumSize(new java.awt.Dimension(40, 20));
        ySpacingControl.setPreferredSize(new java.awt.Dimension(60, 20));
        ySpacingControl.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                ySpacingControlStateChanged(evt);
            }
        });
        jToolBar1.add(ySpacingControl);

        graphZoomControl.setFloatable(false);
        graphZoomControl.setRollover(true);
        jToolBar1.add(graphZoomControl);
        jToolBar1.add(jSeparator1);

        org.openide.awt.Mnemonics.setLocalizedText(profileLabel, org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.profileLabel.text")); // NOI18N
        jToolBar1.add(profileLabel);

        profileCombo.setToolTipText(org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.toolTipText")); // NOI18N
        profileCombo.setMaximumSize(new java.awt.Dimension(40, 20));
        profileCombo.setMinimumSize(new java.awt.Dimension(40, 20));
        profileCombo.setName(""); // NOI18N
        profileCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileComboActionPerformed(evt);
            }
        });
        jToolBar1.add(profileCombo);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jSplitPane2.setMinimumSize(new java.awt.Dimension(0, 0));

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(imgScrollPane, java.awt.BorderLayout.CENTER);

        imgZoomControl.setRollover(true);
        jPanel2.add(imgZoomControl, java.awt.BorderLayout.PAGE_START);

        jSplitPane2.setBottomComponent(jPanel2);

        jSplitPane1.setBottomComponent(jSplitPane2);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshViewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshViewButtonActionPerformed
        fullRefresh();
    }//GEN-LAST:event_refreshViewButtonActionPerformed

    private void exportButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportButtonActionPerformed
        SceneSupport.exportImg(graph);
    }//GEN-LAST:event_exportButtonActionPerformed

    // todo put into the zoom util class (register each button?)
    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        ScenePrinter.print(graph);
    }//GEN-LAST:event_printButtonActionPerformed

    private void xSpacingControlStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_xSpacingControlStateChanged
        final Point p = rememberPos();
        final int oldXSpace = (graph == null) ? 0 : graph.getXSpace();

        Integer val = (Integer) xSpacingControl.getValue();


        if (graph != null && val != null) {
            getPrefs().putInt(PREF_XSPACING, val);
            graph.setXSpace(val);
            showParaGraph();

            Point p2 = new Point(adjustByRatio(p.x, oldXSpace, val), p.y);
            restorePos(p2);
        }
    }//GEN-LAST:event_xSpacingControlStateChanged





    private int adjustByRatio(int aVal, int aBefore, int aAfter) {
        return (int) Math.round( aVal * ((aAfter * 1.0) / (aBefore * 1.0)) );
    }

    private Point rememberPos() {
        return (graphScrollPane == null || graphScrollPane.getViewport() == null) ?
                new Point(0,0) : graphScrollPane.getViewport().getViewPosition();
    }

    private void restorePos(final Point aPoint) {
        if (graphScrollPane != null && graphScrollPane.getViewport() != null) {
            graphScrollPane.getViewport().setViewPosition(aPoint);
        }
    }


    private void ySpacingControlStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_ySpacingControlStateChanged
        final Point p = rememberPos();
        final int oldYSpace = (graph == null) ? 0 : graph.getYSpace();

        Integer val = (Integer) ySpacingControl.getValue();
        if (graph != null && val != null) {
            getPrefs().putInt(PREF_YSPACING, val);
            graph.setYSpace(val);
            showParaGraph();

            Point p2 = new Point(p.x, adjustByRatio(p.y, oldYSpace, val));
            restorePos(p2);
        }
    }//GEN-LAST:event_ySpacingControlStateChanged

    private void splitToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitToggleActionPerformed
        getPrefs().putBoolean(PREF_TWO_VIEWS, splitToggle.isSelected());
        showParaGraph(); // refresh
    }//GEN-LAST:event_splitToggleActionPerformed

    

    private void profileComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileComboActionPerformed
        String id = profileCombo.getSelectedItem().toString();
        Profile tmp = ProfileRegistry.get(id);
        Preconditions.checkNotNull(tmp, "Unknown profile %s", id);  // todo or just warn and tmp = ProfileRegistry.EMPTY_PROFILE
        setProfile(tmp);
        fullRefresh();
    }//GEN-LAST:event_profileComboActionPerformed

    private void textSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textSearchButtonActionPerformed
        JFrame frame = new JFrame("Text Search");
        frame.setLayout(new BorderLayout());

        try {
            FindTextPane findTextPane = new FindTextPane(new FileReader(Utilities.toFile(pseudoModel.getTextLayer().getFile().toURI())));
            frame.add(findTextPane);
            frame.getRootPane().setDefaultButton(findTextPane.findButton);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        frame.setSize(600, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }//GEN-LAST:event_textSearchButtonActionPerformed

    private void setProfile(Profile profile) {
        if (this.profile == profile ) return;
        
        this.profile = profile;
        profileCombo.setSelectedItem(profile.getId());
        getPrefs().put(PREF_PROFILE, profile.getId());
    }
    
    /** todo under contruction */
    int curForm = 0;

    /** Note: works only with graph1, graph2 is unchanged */
    private void updateZoom() {
        setZoomFactor();

        if (graphComponent != null && graphComponent.isVisible()) {
            graphComponent.repaint();
            log.finer("]zoom-stateChanged: graphComponent: %s, w=%d, h=%d", graphComponent.getClass(), graphComponent.getWidth(), graphComponent.getHeight()  );
            log.finer("]zoom-stateChanged: parent:  %s, w=%d, h=%d",  graphScrollPane.getViewport().getClass(),
                    graphScrollPane.getViewport().getWidth(), graphScrollPane.getViewport().getHeight());
        }
    }

    private void setZoomFactor() {
        if (graphComponent == null) return;
        ZoomModel zoomModel = graphZoomControl.getModel();
        log.finer("updateZoom: model = %s", zoomModel);

        Rectangle r = graph.getBounds();
        Preconditions.checkNotNull(r, "graph bounds are null");

        double zoomFactor = ZoomUtil.zoomFactor(
                r.width, r.height,
                zoomModel.getType(), zoomModel.getVal(), graphScrollPane.getViewport().getSize());
        log.finer("zoom-stateChanged: graphComponent: %s, w=%d, h=%d", graphComponent.getClass(), graphComponent.getWidth(), graphComponent.getHeight()  );
        log.finer("zoom-stateChanged: parent:  %s, w=%d, h=%d",  graphScrollPane.getViewport().getClass(),
                graphScrollPane.getViewport().getWidth(), graphScrollPane.getViewport().getHeight());

        getPrefs().putDouble(PREF_ZOOM_FACTOR, zoomFactor);
        graph.setZoomFactor(zoomFactor);
        log.fine("zoom-stateChanged: zoomFactor: %f", zoomFactor );
    }


// =============================================================================



    public void requestPara(int aIdx) {
        if (aIdx < 0 || aIdx >= pseudoModel.getParas().size()) {
            return;
        }
        curPara = aIdx;

        gotoControl1.curChanged(aIdx); // could/should be done via listeners
        
        if (aIdx == pseudoModel.getParas().size()-1) {
            log.info();
            tmp();
        }
        else 
            showParaGraph();
    }

    private void tmp() {
        showParaGraph();
    }


    private void showParaGraph() {
        if (curPara < 0 || curPara >= pseudoModel.getParas().size()) return;

        // deregister the old view
        if (graph  != null) deregisterView(graph);
        if (graph2 != null) deregisterView(graph2);

        int splitPos = jSplitPane1.getDividerLocation();
        int extraSplitPos = (jSplitPane1.getTopComponent() instanceof JSplitPane) ?
                ((JSplitPane)jSplitPane1.getTopComponent()).getDividerLocation() : (splitPos/2);

        
        pseudoModel.getUndoMngr().discardAllEdits(); // remove undo history (currently model is tied to the current paragraph and view), todo keep
        curParaModel = pseudoModel.getParaModel(curPara);

        graph = createView();
        graphComponent = graph.getView();

        graphScrollPane = new JScrollPane(graphComponent);
        // todo temporary

        //paraModel.addDataListener(widgetNode);

        // --- initialize controls according to saved preferences (post component) ---
        //final String tooltip = org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.zoomSlider.toolTipText");
        // todo zoom ZoomModel.initZoom(zoomSlider, 50, 10, tooltip, getPrefs(), "graphZoom", cSliderMax );

        if (getPrefs().getBoolean(PREF_TWO_VIEWS, false)) {
            graph2 = createView();
            JScrollPane graphScrollPane2 = new JScrollPane(graph2.getView());

            final JSplitPane extraPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

            extraPanel.setTopComponent(graphScrollPane);
            extraPanel.setBottomComponent(graphScrollPane2);
            extraPanel.setDividerLocation(extraSplitPos);
            jSplitPane1.setTopComponent(extraPanel);
        }
        else {
            jSplitPane1.setTopComponent(graphScrollPane);
        }
        jSplitPane1.setDividerLocation(splitPos);

        //log.info("updateZoom (curPara=" +curPara+")");
        //setZoomFactor();      // todo keep zoom persistent across paragraphs, currently does not work
    }

    private void deregisterView(final LayeredGraph aView) {
        pseudoModel.removeChangeListener(aView);
        aView.removeObjectSceneListener(objListener, ObjectSceneEventType.values());
        aView.getView().removeMouseWheelListener(mouseWheelListener);
        WindowManager.getDefault().getMainWindow().removeKeyListener((KeyListener)aView.getView());
    }
    
    /** todo: Under construction, share with other components using the layeredGraph view (diff) */
    private LayeredGraph createView() {
        final LayeredGraph view = new LayeredGraph(curParaModel, widgetNode, new LookAndFeel());
        pseudoModel.addChangeListener(view);

        // --- initialize graph values according to saved preferences (pre drawing/component); must be the same as above ---
        view.setXSpace(getPrefs().getInt(PREF_XSPACING, 100));
        view.setYSpace(getPrefs().getInt(PREF_YSPACING, 100));
        view.setZoomFactor(getPrefs().getDouble(PREF_ZOOM_FACTOR, 1.0));
        view.setProfile(profile);

        view.initLayout();
        view.draw();

        final JComponent component = view.createView();

        // todo tmp
        view.addObjectSceneListener(objListener, ObjectSceneEventType.OBJECT_FOCUS_CHANGED);
        view.addObjectSceneListener(objListener, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);


        // --- initialize controls according to saved preferences (post component) ---
        //final String tooltip = org.openide.util.NbBundle.getMessage(LayeredViewTopComponent.class, "LayeredViewTopComponent.zoomSlider.toolTipText");
        // todo zoom ZoomModel.initZoom(zoomSlider, 50, 10, tooltip, getPrefs(), "graphZoom", cSliderMax );



//        WindowManager.getDefault().getMainWindow().addKeyListener((KeyListener)component);  // needed for some reason, otherwise teh scene does not get keyboard events
        component.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                log.info("Focus gained");
                component.setBorder(BorderFactory.createLineBorder(Color.black, 3));
//                view.addObjectSceneListener(objListener, ObjectSceneEventType.OBJECT_FOCUS_CHANGED);
//                view.addObjectSceneListener(objListener, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
                component.addMouseWheelListener(mouseWheelListener);

                cursorChanged(view.getFocusedObject());
                //WindowManager.getDefault().getMainWindow().addKeyListener((KeyListener)component);  // needed for some reason, otherwise teh scene does not get keyboard events
            }

            @Override
            public void focusLost(FocusEvent e) {
                log.info("Focus lost");
                component.setBorder(BorderFactory.createLineBorder(Color.black, 0));
//                view.removeObjectSceneListener(objListener, ObjectSceneEventType.values());
                // todo why do I remove it? Properties are not updated when changed from other view. The TC should probably have its own current object
                component.removeMouseWheelListener(mouseWheelListener);
                //WindowManager.getDefault().getMainWindow().removeKeyListener((KeyListener)component);  // needed for some reason, otherwise teh scene does not get keyboard events
            }
        });

        return view;
    }

    /*
     * todo this messes up undo - the old para model with its undo stays somewhere,
     * a new is created with a new undorecorder. But the toolbar udno button triggers
     * the old undorecorder which probably modifies the old model, which writes thru to the data
     * withtout the new model or view knowing.
     */
    protected void fullRefresh() {
        final Point p = rememberPos();
        showParaGraph();
        restorePos(p);
    }

    /** Mouse wheel -> horizontal scroll */
    private final MouseWheelListener mouseWheelListener = new MouseWheelListener() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            final int notches = e.getWheelRotation();
            final JScrollBar scrollbar = graphScrollPane.getHorizontalScrollBar();
            final int curVal = scrollbar.getValue();

            final int delta =
                (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) ?
                    e.getUnitsToScroll() :
                    scrollbar.getBlockIncrement(notches);

            scrollbar.setValue(curVal + delta * 50);    // todo conf
        }
    };

// =============================================================================
// Current object/Cursor/Focus
// =============================================================================

        // update all views, and all listeners

    @Override
    public void goToLoc(Location aLocation) {
        log.info("goToLoc %s", aLocation);
        if (aLocation.getElement() instanceof FForm || aLocation.getElement() instanceof Edge) {
            // ignoring path 
            moveToElement(aLocation.getElement());
        }
    }

    
    
    public void moveToFormWithChar(int aPos) {
        WForm wform = pseudoModel.getwLayer().findForm(aPos);
        if (wform == null) {
            log.warning("Cannot find the form at the requested position, pos=%d", aPos);
            return;
        }

        moveToElement(wform);
    }

//    private void requestSentence(int aIdx) {
//        if (graphScrollPane == null || graphScrollPane.getViewport() == null) return;
//
//        curForm += aIdx;
//
//        final List<Form> forms = model.getNodes(0);
//        if (curForm < 0) {curForm = 0; return; }
//        if (forms.size() <= curForm) {curForm = forms.size()-1; return;}
//        if (forms.isEmpty() || curForm < 0 || forms.size() <= curForm  ) return;
//
//
//        Form form = forms.get(curForm);
//        moveToForm(form);
//
//    }

    private void moveToElement(Element aObject) {
        if (graphScrollPane == null || graphScrollPane.getViewport() == null) return;

        // if form is not in the current para, show its para
        if (graph.findWidget(aObject) == null) {
            log.info("Switching paragraph");
            // todo create projection utilities
            Para para = aObject.getAncestor(Para.class);
            WPara wpara = (para instanceof WPara) ? ((WPara)para) : ((LPara)para).getWPara();
            int paraPos = pseudoModel.getWparas().indexOf(wpara);
            requestPara(paraPos);
            Preconditions.checkState(paraPos == curPara, "Current para mismatch");
            log.info("Switched paragraph");
        }

        graph.setCursor(aObject, true);
    }


    private final ObjectSceneListener objListener = new ObjectSceneListenerAdapter() {
        @Override
        public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
            log.fine("objListener focusChanged %s", newFocusedObject);
            cursorChanged(newFocusedObject);
        }

        @Override
        public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
            log.fine("objListener selectionChanged %s", newSelection);
            Object obj = (newSelection.size() == 1) ? Cols.getFirstElement(newSelection) : null;
            cursorChanged(obj);
        }
    };

    private void cursorChanged(Object aCur) {
        log.fine("cursorChanged %s", aCur);

        removeOldHighlight();
        displayProperties(aCur);
        if (aCur != null) {
            highlightInText(Collections.singleton(aCur));
        }
    }
    
// =============================================================================
// Highlighting of the current form in the html context view
// todo this should be part of the component displaying the html layer
// =============================================================================
    private final Highlighter.HighlightPainter myHighlightPainter = new MyHighlightPainter(Css.selectHighlight);

    static class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
        public MyHighlightPainter(Color color) {
            super(color);
        }
    }

    private void highlightInText(final Set<Object> aObjs) {
        for (Object obj : aObjs) {
            if (obj instanceof FForm) highlightInText((FForm) obj);
        }
    }

    private void highlightInText(final FForm aForm) {
        highlightInText(FeatDataUtil.getWForms(aForm));
    }

    private void highlightInText(final Collection<WForm> aForms) {
        final Highlighter hiliter = textView.getHighlighter();

        for (WForm form : aForms) {
            int start = form.getDocOffset();
            int end   = start + form.getLen();


            // --- add extra visibility for short words ---
            if (form.getLen() < 3) {
                if (start > 0) start--;
                if (end < textView.getDocument().getLength() - 1) end++;
            }

            try {
                hiliter.addHighlight(start, end, myHighlightPainter);
                textView.scrollRectToVisible( textView.modelToView(end) );
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }


    // Removes only our private highlights
    public void removeOldHighlight() {
        Highlighter hiliter = textView.getHighlighter();

        for (Highlighter.Highlight hilite : hiliter.getHighlights()) {
            if (hilite.getPainter() instanceof MyHighlightPainter) {
                hiliter.removeHighlight(hilite);
            }
        }
    }

// =============================================================================
// Synchronizing with properties
// =============================================================================

    private void displayProperties(final Object aObject) {
        log.info("displayProperties %s", aObject);
        if (aObject == null) {
            try {
                mngr.setSelectedNodes(new Node[]{});
                widgetNode.setProp(aObject);
                mngr.setRootContext(widgetNode);
                setActivatedNodes(new Node[]{});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            widgetNode.setProp(aObject);
            mngr.setRootContext(widgetNode);
            setActivatedNodes(new Node[]{widgetNode});
            nodesLookupContent.remove(widgetNode);
            nodesLookupContent.add(widgetNode);  /// todo temporary
        }
    }

    /** 
     * A node standing for any selectable widget in the graph. 
     * It is in the lookup so that other components can access/observe the 
     * current object.
     */
    final WidgetNode widgetNode = new WidgetNode();
    /** Manager exposing the currently selected object (as the widgetNode) */
    final ExplorerManager mngr = new ExplorerManager();


    public WidgetNode getWidgetNode() {
        return widgetNode;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return mngr;
    }

// =============================================================================
// Undo/Redo
// =============================================================================

    private final UndoRedo.Manager undoMngr = new UndoRedo.Manager();

    @Override
    public UndoRedo getUndoRedo() {
        return undoMngr;
    }

// =============================================================================





    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exportButton;
    private org.purl.jh.feat.util0.gui.pager.GotoControl gotoControl1;
    private org.purl.net.jh.nbutil.ZoomControl graphZoomControl;
    private javax.swing.JScrollPane imgScrollPane;
    private javax.swing.ButtonGroup imgZoomButtonGroup;
    private org.purl.net.jh.nbutil.ZoomControl imgZoomControl;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton printButton;
    private javax.swing.JComboBox profileCombo;
    private javax.swing.JLabel profileLabel;
    private javax.swing.JButton refreshViewButton;
    private javax.swing.JToggleButton splitToggle;
    private javax.swing.JButton textSearchButton;
    private javax.swing.JSpinner xSpacingControl;
    private javax.swing.JSpinner ySpacingControl;
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;


    }


    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    } 
    
    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;

    }

    private Preferences getPrefs() {
        return NbPreferences.forModule(getClass());
    }

}
