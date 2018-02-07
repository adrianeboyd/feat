package org.purl.jh.feat.diffui;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import cz.cuni.utkl.czesl.data.layerl.Edge;
import cz.cuni.utkl.czesl.data.layerl.Errorr;
import cz.cuni.utkl.czesl.data.layerl.LForm;
import cz.cuni.utkl.czesl.data.layerl.LLayer;
import cz.cuni.utkl.czesl.data.layerl.LPara;
import cz.cuni.utkl.czesl.data.layerw.WPara;
import cz.cuni.utkl.czesl.data.layerx.FForm;
import cz.cuni.utkl.czesl.data.layerx.FormsLayer;
import cz.cuni.utkl.czesl.data.layerx.Para;
import cz.cuni.utkl.czesl.data.layerx.Position;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.widget.Widget;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.WindowManager;
import org.purl.jh.feat.diffui.diff.*;
import org.purl.jh.feat.diffui.util.DataCols;
import org.purl.jh.feat.diffui.util.Util;
import org.purl.jh.feat.layered.LayeredGraph;
import org.purl.jh.feat.layered.LookAndFeel;
import org.purl.jh.feat.layered.NodeLayout;
import org.purl.jh.feat.layered.ParaModel;
import org.purl.jh.feat.layered.VModel;
import org.purl.jh.feat.layered.util.ObjectSceneListenerAdapter;
import org.purl.jh.feat.profiles.Profile;
import org.purl.jh.feat.profiles.ProfileRegistry;
import org.purl.jh.feat.util0.gui.pager.CurListener;
import org.purl.jh.nbpml.LayerDataObject;
import org.purl.jh.nbpml.LayerProvider;
import org.purl.jh.pml.Element;
import org.purl.jh.pml.IdedElement;
import org.purl.jh.pml.location.AcceptingLocation;
import org.purl.jh.pml.location.Location;
import org.purl.jh.util.Pair;
import org.purl.jh.util.col.Cols;
import org.purl.jh.util.err.Err;
import org.purl.jh.util.gui.list.Combos;

/**
 * 1 (sometimes A) refers to the result (a copy of the first annotator's annotation), 
 * while 2 (sometimes B) refers to the source (the second annotator's annotation).
 * <p>
 * The 1-panel, i.e. with the resulting annotation, can be edited, 
 * the 2-panel, i.e. the panel source annotation = 2nd annotator's annotation) is read-only.
 * <p>
 * Information can be 
 * <ul>
 * <li><i>pulled</i> to the 1-panel from the 2-panel (focus is in the 1-panel),
 * <li><i>pushed</i> from the 2-panel to the 1-panel (focus is in the 2-panel).
 * 
 * @author j
 */
public class DiffPanel extends javax.swing.JPanel implements Lookup.Provider, AcceptingLocation {
    private final static org.purl.jh.util.Logger log = org.purl.jh.util.Logger.getLogger(DiffPanel.class);

    private final static String PREF_PROFILE = "profile";
    
    // options
    private boolean optCompareComments;
    
    private Profile profile;
    
    private VModel pseudoModel1 = null;
    //private LLayer layer1;
    private ParaModel paraModel1;
    private LayeredGraph graph1;
    private JScrollPane graphScrollPane1;
    private Component graphComponent1;

    private VModel pseudoModel2 = null;
    //private LLayer layer2;
    private ParaModel paraModel2;
    private LayeredGraph graph2;
    private JScrollPane graphScrollPane2;
    private Component graphComponent2;

    //private Map<FormsLayer<?>, Matching> layer2matching;
    private CombinedMatching matching;
    
    private List<Location> diffs1;
    private List<Location> diffs2;
    
    private int curPara = -1;

    // cursor-like object todo under development
    //private Object curObject;        // usually form, possibly edge, etc.

    /**
     * Publishes currently selected widget node.
     * 
     * Can be incorporated into the encapsulating topcomponent.
     */
    private final Lookup lookup;
        
    /** 
     * A node standing for any selectable widget in either graph. 
     * It is in the lookup so that other components can access/observe the 
     * current object.
     */
    private final DiffWidgetNode widgetNode = new DiffWidgetNode();
    
    /** Lookup containing the widgetNode (changing each time the content of the node changes) */
    private final InstanceContent nodesLookupContent = new InstanceContent();
    
    
    /** Creates new form DiffPanel */
    public DiffPanel() {
        lookup = new AbstractLookup(nodesLookupContent);
        initComponents();

        profileCombo.setModel( Combos.getModel(new ArrayList<>(ProfileRegistry.ids())) );    // todo sort (streams)
    }

    public DiffPanel(final LayerDataObject<?> aDObj1, final LayerDataObject<?> aDObj2) {
        this();

        // todo load optCompareComments = \
        jCheckBox1.setSelected(optCompareComments);
        
        
        final LayerProvider layerProvider1 = aDObj1.getNodeDelegate().getLookup().lookup(LayerProvider.class);
        Err.fAssert(layerProvider1 != null, "%s provides no layer", aDObj1);
        pseudoModel1 = new VModel(layerProvider1.getLayer(null), undoMngr);

        final LayerProvider layerProvider2 = aDObj2.getNodeDelegate().getLookup().lookup(LayerProvider.class);
        Err.fAssert(layerProvider2 != null, "%s provides no layer", aDObj2);
        pseudoModel2 = new VModel(layerProvider2.getLayer(null), undoMngr);
        
        //gotoControl1.sizeChanged(WIDTH);
        gotoControl1.addListener(new CurListener() {
            @Override
            public void curChanged(int aIdx) {
                requestPara(aIdx);
            }

            @Override
            public void sizeChanged(int aNewSize) {
                log.warning("unhandled size change");
                //throw new UnsupportedOperationException("Not supported yet.");
            }
        });

        gotoControl1.sizeChanged(pseudoModel1.getParas().size()); // could/should be done via listening
        
        final String profileId = NbPreferences.forModule(LayeredGraph.class).get(PREF_PROFILE, null);
        final Profile tmp = ProfileRegistry.get(profileId);
        setProfile(tmp == null ? ProfileRegistry.get(ProfileRegistry.EMPTY_PROFILE_ID) : tmp);

        
        
        requestPara(0);     // todo after constructor
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }
    
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton3 = new javax.swing.JButton();
        markStripe1 = new org.purl.jh.feat.diffui.MarkStripe();
        layersViewSplit = new javax.swing.JSplitPane();
        jToolBar1 = new javax.swing.JToolBar();
        gotoControl1 = new org.purl.jh.feat.util0.gui.pager.GotoControl();
        refreshDiffButton = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        pullChangeButton = new javax.swing.JButton();
        pushChangeButton = new javax.swing.JButton();
        testButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        profileLabel = new javax.swing.JLabel();
        profileCombo = new javax.swing.JComboBox();

        jButton3.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.jButton3.text")); // NOI18N

        setLayout(new java.awt.BorderLayout());
        add(markStripe1, java.awt.BorderLayout.PAGE_END);

        layersViewSplit.setDividerLocation(320);
        layersViewSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        add(layersViewSplit, java.awt.BorderLayout.CENTER);

        jToolBar1.setRollover(true);
        jToolBar1.add(gotoControl1);

        refreshDiffButton.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.refreshDiffButton.text")); // NOI18N
        refreshDiffButton.setActionCommand(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.refreshDiffButton.actionCommand")); // NOI18N
        refreshDiffButton.setFocusable(false);
        refreshDiffButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshDiffButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshDiffButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshDiffButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(refreshDiffButton);

        jCheckBox1.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.jCheckBox1.text")); // NOI18N
        jCheckBox1.setToolTipText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.jCheckBox1.toolTipText")); // NOI18N
        jCheckBox1.setFocusable(false);
        jCheckBox1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jCheckBox1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBox1);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.jLabel1.toolTipText")); // NOI18N
        jToolBar1.add(jLabel1);

        pullChangeButton.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.pullChangeButton.text")); // NOI18N
        pullChangeButton.setToolTipText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.pullChangeButton.toolTipText")); // NOI18N
        pullChangeButton.setFocusable(false);
        pullChangeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pullChangeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pullChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pullChangeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(pullChangeButton);

        pushChangeButton.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.pushChangeButton.text")); // NOI18N
        pushChangeButton.setToolTipText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.pushChangeButton.toolTipText")); // NOI18N
        pushChangeButton.setFocusable(false);
        pushChangeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        pushChangeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        pushChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pushChangeButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(pushChangeButton);

        testButton.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.testButton.text")); // NOI18N
        testButton.setFocusable(false);
        testButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        testButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        testButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(testButton);
        jToolBar1.add(jSeparator1);

        profileLabel.setText(org.openide.util.NbBundle.getMessage(DiffPanel.class, "DiffPanel.profileLabel.text")); // NOI18N
        jToolBar1.add(profileLabel);

        profileCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        profileCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profileComboActionPerformed(evt);
            }
        });
        jToolBar1.add(profileCombo);

        add(jToolBar1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshDiffButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshDiffButtonActionPerformed
        refreshDiff();
    }//GEN-LAST:event_refreshDiffButtonActionPerformed

    private void pullChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pullChangeButtonActionPerformed
        pull();
    }//GEN-LAST:event_pullChangeButtonActionPerformed

    private void pushChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pushChangeButtonActionPerformed
        push();
    }//GEN-LAST:event_pushChangeButtonActionPerformed

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testButtonActionPerformed
        updateMarkStripe();
    }//GEN-LAST:event_testButtonActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        optCompareComments = jCheckBox1.isSelected();
        refreshDiff();
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void profileComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profileComboActionPerformed
        String id = profileCombo.getSelectedItem().toString();
        Profile tmp = ProfileRegistry.get(id);
        Preconditions.checkNotNull(tmp, "Unknown profile %s", id);  // todo or just warn and tmp = ProfileRegistry.EMPTY_PROFILE
        setProfile(tmp);
        showParaGraph();  //refresh
    }//GEN-LAST:event_profileComboActionPerformed

    private void setProfile(Profile profile) {
        if (this.profile == profile ) return;
        
        this.profile = profile;
        profileCombo.setSelectedItem(profile.getId());
        NbPreferences.forModule(LayeredGraph.class).put(PREF_PROFILE, profile.getId());
    }
    
    private void refreshDiff() {
        //layer2matching = null;
        matching = null;
        highlightCurPara();
    }
    
    private void ensureMatching() {
        if (matching == null) {
            log.info("ensureMatching - redo optCompareComments=%s", optCompareComments);
            matching = new Diff3((LLayer)pseudoModel1.getTopLayer(), (LLayer)pseudoModel2.getTopLayer(), 0, 2, optCompareComments).calculateDiff();
    
            
            
//            diffs1 = sortLocations(matching.getDifferences1(), null);
//            diffs2 = sortLocations(matching.getDifferences2(), null);
        } 
    }
    
    private Comparator<Element> widgetComp(final LayeredGraph graph) {
        return new Comparator<Element>() {
            @Override
            public int compare(Element o1, Element o2) {
                final Widget w1 = graph.findWidget(o1);
                final Widget w2 = graph.findWidget(o2);
                
                if (w1 == null && w2 == null) {
                    return o1.hashCode() - o2.hashCode();
                }
                else if (w1 == null) {
                    return -1;
                } 
                else if (w2 == null) {
                    return 1;
                } 
                else {
                    int xdelta = w1.getLocation().x - w2.getLocation().x;
                    return xdelta == 0 ? (w1.getLocation().y - w2.getLocation().y) : xdelta;
                }
            }
        
        };
    }
    
    
    
    private void highlightCurPara() {
        ensureMatching();
            
        // todo filter by curr paragraph
        List<Element> els1 = new ArrayList<>();
        List<Element> els2 = new ArrayList<>();
        
        for (Location difference : matching.getDifferences1()) {
            els1.add(difference.getElement());          // todo pass location
        }
        for (Location difference : matching.getDifferences2()) {
            els2.add(difference.getElement());
        }
        
        updateMarkStripe();
     
        //
        
        graph1.setXHighlights(els1);
        graph2.setXHighlights(els2);        
    }

    
    
    /**
     * Pairs with a point are ordered by the points (first its x than y coordinate).
     * 
     * @param <T> type of the first element of the pair
     * @todo comparator for pairs on first and second projection, comparator on points
     * @return 
     */
    private static <T> Comparator<Pair<T,Point>> pair_pointComp() {
        return new Comparator<Pair<T,Point>>() {
            @Override
            public int compare(Pair<T,Point> o1, Pair<T,Point> o2) {
                final int xdelta = o1.mSecond.x - o2.mSecond.x;
                return xdelta == 0 ? (o1.mSecond.y - o2.mSecond.y) : xdelta;
            }
        };
    }
                
    /**
     * For each location finds the position of the corresponding widget.
     * Locations whose element has not associated widget are ignored (they are not in the current paragraph).
     * 
     * @param graph
     * @param locs
     * @return list of pairs, each containing location and its position in the graph.
     */
    public static List<Pair<Location,Point>> locs2loc_points(final LayeredGraph graph, Collection<Location> locs) {
        final List<Pair<Location,Point>> locpoints = new ArrayList<>(locs.size());
        for (Location loc : locs) {
            final Widget w = graph.findWidget(loc.getElement());
            if (w != null && w.getLocation() != null) {
                locpoints.add(new Pair<>(loc, w.getLocation()));
            }
        }
        
        return locpoints;
    }
    
//    public List<Pair<Location,Integer>> getSortedLocations(Collection<Location> locs) {
//        for (Location loc : locs)
//        
//        return null;
//    }
//
//    private Comparator<Location> widgetCompAbt(final LayeredGraph graph) {
//        return new Comparator<Location>() {
//            @Override
//            public int compare(Element o1, Element o2) {
//                Widget w1 = graph1.findWidget(o1);
//                Widget w2 = graph1.findWidget(o2);
//                if (w1 == null) w1 = graph2.findWidget(o1);
//                
//                if (w1 == null && w2 == null) {
//                    return o1.hashCode() - o2.hashCode();
//                }
//                else if (w1 == null) {
//                    return -1;
//                } 
//                else if (w2 == null) {
//                    return 1;
//                } 
//                else {
//                    int xdelta = w1.getLocation().x - w2.getLocation().x;
//                    return xdelta == 0 ? (w1.getLocation().y - w2.getLocation().y) : xdelta;
//                }
//            }
//        
//        };
//    }
//    
//    private Comparator<Element> widgetCompAbt(final LayeredGraph graph) {
//        return new Comparator<Element>() {
//            @Override
//            public int compare(Element o1, Element o2) {
//                Widget w1 = graph1.findWidget(o1);
//                Widget w2 = graph1.findWidget(o2);
//                if (w1 == null) w1 = graph2.findWidget(o1);
//                
//                if (w1 == null && w2 == null) {
//                    return o1.hashCode() - o2.hashCode();
//                }
//                else if (w1 == null) {
//                    return -1;
//                } 
//                else if (w2 == null) {
//                    return 1;
//                } 
//                else {
//                    int xdelta = w1.getLocation().x - w2.getLocation().x;
//                    return xdelta == 0 ? (w1.getLocation().y - w2.getLocation().y) : xdelta;
//                }
//            }
//        
//        };
//    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.purl.jh.feat.util0.gui.pager.GotoControl gotoControl1;
    private javax.swing.JButton jButton3;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSplitPane layersViewSplit;
    private org.purl.jh.feat.diffui.MarkStripe markStripe1;
    private javax.swing.JComboBox profileCombo;
    private javax.swing.JLabel profileLabel;
    private javax.swing.JButton pullChangeButton;
    private javax.swing.JButton pushChangeButton;
    private javax.swing.JButton refreshDiffButton;
    private javax.swing.JButton testButton;
    // End of variables declaration//GEN-END:variables


    public final void requestPara(int aIdx) {
        if (aIdx < 0 || aIdx >= pseudoModel1.getParas().size()) {
            return;
        }
        curPara = aIdx;

        gotoControl1.curChanged(aIdx); // could/should be done via listeners
        showParaGraph();
        highlightCurPara();
    }

    
    private void showParaGraph() {
        if (curPara < 0 || curPara >= pseudoModel1.getParas().size()) return;
 
        int splitPos = layersViewSplit.getDividerLocation();

        // totod remove from all views
        if (graph1 != null) {
            removeView(graph1);
            removeView(graph2);
        }

 
        paraModel1 = pseudoModel1.getParaModel(curPara);
        paraModel2 = pseudoModel2.getParaModel(curPara);
        pseudoModel1.getUndoMngr().discardAllEdits(); // remove undo history (currently model is tied to the current paragraph and view), todo keep
        pseudoModel2.getUndoMngr().discardAllEdits(); // remove undo history (currently model is tied to the current paragraph and view), todo keep

        SyncLayout layout = new SyncLayout(paraModel1, paraModel2);
        
        graph1 = createView(pseudoModel1, paraModel1, false, layout.model1layout());
        graph2 = createView(pseudoModel2, paraModel2, true,  layout.model2layout());

        graph1.createView();
        graph2.createView();
        
        graphComponent1 = graph1.getView();
        graphComponent2 = graph2.getView();

        graphScrollPane1 = new JScrollPane(graphComponent1);
        graphScrollPane2 = new JScrollPane(graphComponent2);
        graphScrollPane1.getHorizontalScrollBar().setModel(graphScrollPane2.getHorizontalScrollBar().getModel());   // synchronize scrolling
 
        layersViewSplit.setDividerLocation(splitPos);
        layersViewSplit.setTopComponent(graphScrollPane1);
        layersViewSplit.setBottomComponent(graphScrollPane2);

    
        graph1.initLayout();
        graph2.initLayout();

        layout.go();


        addKeyStrokes();

        graph1.draw();
        graph2.draw();

        graph1.initLayout();
        graph2.initLayout();

    }

    private void addKeyStrokes() {
        
        final Action pullAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graph1.isReadonly()) {Toolkit.getDefaultToolkit().beep(); return;}
                try {
                    pull();
                }
                catch(CannotUndoException ex) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        };

        final Action pushAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (graph1.isReadonly()) {Toolkit.getDefaultToolkit().beep(); return;}
                try {
                    push();
                }
                catch(CannotUndoException ex) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        };
        
//        final Action xAction = new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Object obj1 = graph1.getFocusedObject();
//                Object obj2 = graph2.getFocusedObject();
//                log.info("XAction focused objs: %s / %s", obj1, obj2);
//                if (obj1 != null) { 
//                    pullAction.actionPerformed(e);
//                }
//                else if (obj2 != null) {
//                    pushAction.actionPerformed(e);
//                }
//            }        
//        };
        
        graph1.addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0),  pullAction);
        graph2.addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0),  pushAction);
        graph1.addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0),  pullAction);
        graph2.addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F9, 0),  pushAction);
        graph1.addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK),  pullAction);
        graph2.addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK),  pushAction);

        graph1.addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK),  null);
        graph2.addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK),  null);
        
    }
    
    /** Under construction, todo share  */
    private LayeredGraph createView(final VModel aPseudoModel, final ParaModel aParaModel, final boolean readonly, final NodeLayout layout) {
        final LayeredGraph view = new LayeredGraph(aParaModel, widgetNode, new LookAndFeel(), readonly) {
            @Override
            protected NodeLayout getNodeLayout() {
                return layout;
            }
        };

        aPseudoModel.addChangeListener(view);

        view.addObjectSceneListener(objListener, ObjectSceneEventType.OBJECT_FOCUS_CHANGED);
        view.addObjectSceneListener(objListener, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        
        // --- initialize graph values according to saved preferences (pre drawing/component); must be the same as above ---
        view.setXSpace(100);
        view.setYSpace(100);
        view.setZoomFactor(1.0);
        view.setProfile(profile);

//        view.initLayout();
//        view.draw();

//        final JComponent component = view.createView();
        
        return view;
    }

    private void removeView(LayeredGraph aView) {
        aView.getParaModel().getPseudoModel().removeChangeListener(aView);
        aView.removeObjectSceneListener(objListener, ObjectSceneEventType.values());
        //aView.getView().removeMouseWheelListener(mouseWheelListener);
        WindowManager.getDefault().getMainWindow().removeKeyListener((KeyListener)aView.getView());
    }


    // todo create an adapter
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

    /**
     * @param el
     * @return 
     */
    protected int docIdx(Element el) {
        if (el == null) {
            return -1;
        }
        else if (graph1.getObjects().contains(el)) {
            return 0;   // merging document
        }
        else if (graph2.getObjects().contains(el)) {
            return 2;   // 1,2 possibly 3 are source documents (currently we do not use 1 and 3)
        }
        // todo it could be in a different paragraph, look into the doc maybe split into graphIdx and docIdx
        throw new IllegalArgumentException("Unknown element " + el);
    }
    

    /**
     * React to cursor placement (update widgetNode, synchronize cursor in the other view)
     * @todo highlight the other views do not put there any cursor like thing, esp not selection or something
     * that could trigger any events
     * @param aCur 
     */
    private void cursorChanged(Object aCur) {
        log.info("cursorChanged %s", aCur);

        nodesLookupContent.remove(widgetNode);

        if (aCur == null) {
            widgetNode.setProp(null);
            nodesLookupContent.add(widgetNode); 
            return;
        }

        if (matching == null) {
            widgetNode.setProp(aCur);
            nodesLookupContent.add(widgetNode); 
            log.info("cursorChanged - no matching");
            return; 
        }

        Preconditions.checkArgument(aCur instanceof FForm || aCur instanceof Edge);
        
        final Element el = ((Element)aCur);
        final int docIdx = docIdx(el);
        final LayeredGraph thisGraph = docIdx == 0 ? graph1 : graph2;
        final LayeredGraph thatGraph = docIdx == 0 ? graph2 : graph1;
        
        //removeOldHighlight();

        widgetNode.setMatching(matching, docIdx);
        widgetNode.setProp(aCur);
        nodesLookupContent.add(widgetNode);  /// todo temporary

        Element thatEl = matching.getMatching(el, docIdx);
        Set<?> curHighlighted = thatGraph.getHighlightedObjects();  // what if user switches graphs then there would be a highligt left in the other graph, keep track of highlights, rmove them
        Set<?> toHighlight = thatEl == null ? ImmutableSet.of() : ImmutableSet.of(thatEl);

        thisGraph.setHighlightedObjects(ImmutableSet.of());
        if (!curHighlighted.equals(toHighlight)) {
            try {
                    thatGraph.setHighlightedObjects(toHighlight);
            }
            catch(Throwable e) {
                log.severe("Error HIG:", e);
                Widget w = toHighlight.isEmpty() ? null : thatGraph.findWidget(toHighlight.iterator().next());
                log.info("HighlightingObj: " + toHighlight);
                log.info("HighlightingW:   " + w);
                log.info("HIGS:" + thatGraph.getHighlightedObjects());
                log.info("State:" + thatGraph.getObjectState(aCur));
            }
            
            thatGraph.validate();
        }
        
    }

// =============================================================================
// Undo/Redo
// =============================================================================

    private final UndoRedo.Manager undoMngr = new UndoRedo.Manager();

// =============================================================================
// Navigation
// =============================================================================
// add general support for moving among objects with certain properties    
// todo for now ignoring sigle differences (deletes, inserts)
    
    
    private void nextDiff() {
        LayeredGraph curGraph = graph1; // todo
        
        final Object obj = curGraph.getFocusedObject();
        
        ensureMatching();
        

//        find diff first location after obj
//        for (Location difference : matching.getDifferences1()) {
//            // todo if location before obj 
//        }

        // todo diffs in 2 without 1 match!!
//                matching.getDifferences1();
//                        matching.getDifferences2();
//        
//        for (Location difference : ) {
//        for (Location difference : matching.getDifferences2()) {
        
         
//        graph1.highlight(els1);
//        graph2.highlight(els2);        
                
        // 0
    }
    
    private void prevDiff() {
        // 0
    }
    
// =============================================================================
//
// =============================================================================
    private void updateDiff(IdedElement el1, IdedElement el2) {
        updateDiff(Arrays.asList(el1), Arrays.asList(el2));
//        updateDiff(el1 == null ? ImmutableSet.of() : ImmutableSet.of(el1), // generics problems
//                   el2 == null ? ImmutableSet.of() : ImmutableSet.of(el2));
    }
    
    private void updateDiff(Collection<? extends IdedElement> els1, Collection<? extends IdedElement> els2) {
        // todo add dedicated method to Diff3
        for (IdedElement e : els1) {
            if (e != null) Diff3.refreshDiff(matching, e);
        }
        for (IdedElement e : els2) {
            if (e != null) Diff3.refreshDiff(matching, e);
        }
        
        for (IdedElement e : els1) {
            if (e == null) continue;
            if (matching.getDifferences1().contains(Location.of(e)))  {
                graph1.addXHighlights(ImmutableSet.of(e));
            }
            else {
                graph1.removeXHighlights(ImmutableSet.of(e));
            } 
        }
        
        for (IdedElement e : els2) {
            if (e == null) continue;
            if (matching.getDifferences2().contains(Location.of(e)))  {
                graph2.addXHighlights(ImmutableSet.of(e));
            }
            else {
                graph2.removeXHighlights(ImmutableSet.of(e));
            } 
        }

        graph1.validate();
        graph2.validate();
    }
    
    /**
     * Pulls change from lower graph.
     * 
     * The selected object in the higher graph is modified to match its 
     * matching object (if any) in the lower graph.
     * 
     * @todo add optional GUI for selecting what to copy
     * @todo remove pulling/pushing comments into a separate method
     */
    private void pull() {
        final Object obj = graph1.getFocusedObject();
        
        if (obj instanceof LForm) {
            pullForm((LForm)obj);
        }
        else if (obj instanceof Edge) {
            pullEdge((Edge)obj);
        }
        
        // todo get the distinguishing characteristics
        // todo apply them on the currently selected element
    }
        
    private void pullForm(LForm lform1) {
        log.info("Pulling to form (%s)", lform1);
        final FormsLayer<?> layer1 = lform1.getLayer();
        Err.iAssert(matching != null, "Matching null");

        final FForm lform2 = matching.getBMatching(lform1);

        log.info("Matching forms :" + lform2 + " <- " + lform1);
        if (lform2 == null) {
            layer1.formDel(lform1, null, null);
        }
        else {
            layer1.formEdit(lform1, lform2.getToken(), null, null);
        }

        updateDiff(lform1, lform2);
    }
    
    

    /**
     * Updates edge in the resulting file by the annotator's  matching edge.
     * @todo allow individual components (tags, legs, comments, links) to be combined/replaced 
     * @param edge1 C
     */
    private void pullEdge(Edge edge1) {
        final Edge edge2 = matching.getBMatching(edge1);

        log.info("Matching edges :" + edge2 + " <- " + edge1);
        if (edge2 == null) {
            edge1.getLayer().edgeDel(edge1, graph1, matching); 
        }
        else {
            transferEdge(edge1, edge2);
        }
        updateDiff(edge1, edge2);
    }
    
    
    

    private void push() {
        final Object obj = graph2.getFocusedObject();
        
        if (obj instanceof LForm) {
            pushForm((LForm)obj);
        }
        else if (obj instanceof Edge) {
            pushEdge((Edge)obj);
        }
        
        // todo get the distinguishing characteristics
        // todo apply them on the currently selected element
    }
    
    private void pushForm(final LForm lform2) {
        log.info("Pushing from form (%s)", lform2);

        final LForm lform1 = (LForm) matching.getAMatching(lform2);

        log.info("Matching forms :" + lform2 + " -> " + lform1);

        // insert form
        if (lform1 == null) {
            if (true) {
                log.info("Sorry! Not supported yet.");
                return;
            }
            
            // find anchor for inserting lform1
            final Predicate<LForm> hasMatching = new Predicate<LForm>() {
                @Override
                public boolean apply(LForm form2) {
                    return matching.getAMatching(form2) != null;
                }
            };
            LForm anchorForm2 = Util.move(this.paraModel2, lform2, hasMatching, false);
        
            log.info("FOund anchor2=%s", anchorForm2);
            Position anchor1;
            if (anchorForm2 == null) { // insert initially?
                anchorForm2 = Util.move(this.paraModel2, lform2, hasMatching, true);

                if (anchorForm2 == null) return;
                LForm anchorForm1 = (LForm)matching.getAMatching(anchorForm2);
                Err.iAssert(anchorForm1 != null, "Null anchorForm1");

                anchor1 = Position.before( anchorForm1 );
            }
            else {
                LForm anchorForm1 = (LForm)matching.getAMatching(anchorForm2);
                Err.iAssert(anchorForm1 != null, "Null anchorForm1");
                anchor1 = Position.after(anchorForm1);
            }

            // find closest matching form before lform2
            log.info("formAdd layer1=%s, lform2=%s, anchor=%s", anchor1.getForm().getLayer(), lform2, anchor1);

            anchor1.getForm().getLayer().formAdd(lform2.getToken(), anchor1, null, null);
        }
        // modify form
        else {
            lform1.getLayer().formEdit(lform1, lform2.getToken(), null, null);
        }

        updateDiff(lform1, lform2);
    }

    private void pushEdge(Edge edge2) {
        final Edge edge1 = matching.getAMatching(edge2);

        log.info("Matching edges :" + edge1 + " <- " + edge2);
        if (edge1 == null) {
            log.info("Pushed insert not supported yet (%s)", edge2);
            //layer1.edgeDel(edge1, graph1, matching); 
        }
        else {
            transferEdge(edge1, edge2);
        }
        updateDiff(edge1, edge2);
    }
    
    // todo!! (works only for cur para)
    private int whichDocument(Element aObject) {
       if (graph1.findWidget(aObject) != null) {
            return 0;  // merged
       }
       else if (false) {
           return 1;    // doc 1
       }
       else if (graph2.findWidget(aObject) != null) {
           return 2;    // doc 2     
       }
       else return -1;  // error
    }
    
    private LayeredGraph whichGraph(Element aObject) {
        final int docIdx = whichDocument(aObject);
        Preconditions.checkArgument(docIdx >= 0);
        
        switch (docIdx) {
            case 0: return graph1;
            case 1: throw new UnsupportedOperationException();
            case 2: return graph2;
            default: throw Err.iErr();
        }
    }

    // todo into DocGraph
    private void moveToElement(Element element) {
        //if (graphScrollPane == null || graphScrollPane.getViewport() == null) return;

        final LayeredGraph graph = whichGraph(element);
        
        // if form is not in the current para, show its para
        if (graph.findWidget(element) == null) {
            log.info("Switching paragraph");
            // todo create projection utilities
            Para para = element.getAncestor(Para.class);
            WPara wpara = (para instanceof WPara) ? ((WPara)para) : ((LPara)para).getWPara();
            int paraPos = graph.getParaModel().getPseudoModel().getWparas().indexOf(wpara);
            requestPara(paraPos);
            Err.iAssert(paraPos == curPara, "Current para mismatch");
            log.info("Switched paragraph");
        }

        graph.setCursor(element, true);
        
        // todo make sure the element is at the same position as the triggering element
        
        
    }

    private void updateMarkStripe() {
        // --- get a sorted list of locations (for the cur para, not very effective) ---
        final List<Pair<Location,Point>> locPts = new ArrayList<>();
        locPts.addAll(locs2loc_points(graph1, matching.getDifferences1()));
        locPts.addAll(locs2loc_points(graph2, matching.getDifferences2()));
        Collections.sort(locPts, DiffPanel.<Location>pair_pointComp());

        // --- get x of the last wform (todo use the size of the graph, but graph1.getBounds().width does not work)
        final FForm lastWform1 = Cols.last(paraModel1.getNodes(0));
        final FForm lastWform2 = Cols.last(paraModel2.getNodes(0));
        final int maxX = Math.max(
                graph1.findWidget(lastWform1).getLocation().x, 
                graph2.findWidget(lastWform2).getLocation().x);
        
        markStripe1.setMarks(this, locPts, maxX);
        markStripe1.repaint();
    }

    @Override
    public void goToLoc(Location aLocation) {
        moveToElement(aLocation.getElement());
    }

    private void transferEdge(Edge edge1, final Edge edge2) {
        final LLayer layer1 = edge1.getLayer();
        
        // handle tags
        for (Errorr err1 : new ArrayList<>(edge1.getErrors())) {
            layer1.errorDel(err1, graph1, null);
        }

        for (Errorr err2 : edge2.getErrors()) {
            layer1.errorAdd(err2.getTag(), edge1, graph1, null);
            // add links if there are any
            for (Edge linkE2 : err2.getLinks()) {
                Edge linkE1 = matching.getAMatching(linkE2);
                if (linkE1 != null) {
                    Errorr err1 = DataCols.find(edge1, err2.getTag());       
                    layer1.errorLinkAdd(err1, linkE1, graph1, null);
                }
                else {
                    // todo collect things that was not possible to copy and display them in some tooltip 
                }
            }
        }
        
        // handle comment
        edge1.setComment(edge2.getComment());
        layer1.edgeChange(edge1, graph1, null);

        final Set<FForm> matchingForms1  = matching.getAFormMatching(edge2.getForms());
        log.info("Matching forms %s < %s", matchingForms1, edge2.getForms());
        
        // remove legs not in 2
        final Set<? extends FForm> toDel = Sets.difference(edge1.getForms(), matchingForms1);
        for (FForm form : toDel) {
            layer1.legDel(edge1, form, graph1, null);
        }
        
        // add legs not in 1
        final Set<FForm> toAdd = Sets.difference(matchingForms1, edge2.getForms());
        for (FForm form : toAdd) {
            layer1.legAdd(edge1, form, graph1, null);
        }
    }

//    private Preferences getPrefs() {
//        return NbPreferences.forModule(getClass());
//    }
    
}
