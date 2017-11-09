package org.purl.jh.feat.layered;

import cz.cuni.utkl.czesl.data.layerl.Edge;
import cz.cuni.utkl.czesl.data.layerl.LPara;
import cz.cuni.utkl.czesl.data.layerl.LLayer;
import cz.cuni.utkl.czesl.data.layerl.LForm;
import cz.cuni.utkl.czesl.data.layerl.Sentence;
import cz.cuni.utkl.czesl.data.layerl.Errorr;
import com.google.common.collect.Iterables;
import cz.cuni.utkl.czesl.data.layerx.Position;
import cz.cuni.utkl.czesl.data.layerx.ChangeEvent;
import org.purl.jh.feat.layered.actions.WidgetClickAction;
import org.purl.jh.feat.layered.actions.SceneClickAction;
import org.purl.jh.feat.layered.actions.AnchorAction;
import org.purl.jh.feat.layered.relation.MenuActionErrorDel;
import org.purl.jh.feat.layered.relation.MenuActionErrorAdd;
import org.purl.jh.feat.layered.relation.Form2XConnectProvider;
import org.purl.jh.feat.layered.relation.EdgeLayout;
import cz.cuni.utkl.czesl.data.layerx.FForm;
import cz.cuni.utkl.czesl.data.layerx.FormsLayer;
import cz.cuni.utkl.czesl.data.layerx.Para;
import org.purl.jh.feat.layered.actions.WAction;
import org.purl.jh.feat.layered.util.Util;
import org.purl.jh.feat.util0.visual.XObjectScene;
import org.purl.jh.feat.layered.relation.Edge2XConnectProvider;
import org.purl.jh.feat.layered.actions.EdgeAddDelAction;
import org.purl.jh.feat.layered.actions.EdgeAddInsertAction;
import org.purl.jh.feat.layered.actions.EdgeDelete;
import org.purl.jh.feat.layered.actions.ErrorLinkDel;
import org.purl.jh.feat.layered.relation.ErrorLinkWidget;
import org.purl.jh.feat.layered.actions.LegDel;
import org.purl.jh.feat.layered.relation.RelationWidget;
import org.purl.jh.feat.layered.relation.RelationWidget.LegWidget;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.undo.CannotUndoException;
import lombok.Getter;
import lombok.Setter;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.purl.jh.feat.profiles.Profile;
import org.purl.jh.pml.Element;
import org.purl.jh.pml.event.DataEvent;
import org.purl.jh.pml.event.DataListener;
import org.purl.jh.util.Pair;
import org.purl.jh.util.col.Cols;
import org.purl.jh.util.col.List2D;
import org.purl.jh.util.col.MultiMap;
import org.purl.jh.util.col.XCols;
import org.purl.jh.util.err.Err;
import org.purl.jh.util.err.Logger;

/**
 *
 * Note: A new LayeredGraph is created for each paragraph, it is not redrawn, but thrown away and newly created.
 * (This keeps certain things simple, but other complicated - e.g. moves between paras via keyboard)
 * Todo: separate view and controller, enclose LayeredGraph into an object handling the whole document
 *
 * Any programmatical changes are done via the model.
 * TODO: how about guid induced changes??
 * todo: why not keep cursor in here (focused obj, but not necessarily focused)
 *
 * @author Jirka dot Hana at gmail dot com
 */
//public class LayeredGraph extends ObjectScene implements GraphDataListener {
public class LayeredGraph extends XObjectScene implements DataListener {
    private static final Logger log = Logger.getLogger(LayeredGraph.class);
    private static final Logger uilog = Logger.getLogger("org.netbeans.ui.Main");

    /* todo replace with generic feat layer (i.e. wlayer+llayer) */
    private final LLayer layer;
            
    // --- x ---
    ///* todo drop */ private final VModel model;
    private final ParaModel paraModel;

    /**
     * The current object (todo under development)
     * Cursor points to excactly one widget (except empty graph, where it is null).
     */
    //private Widget cursor =  null;

    /** Nb's node proxying the currently selected widget (todo should proxy cursor */
    private final WidgetNode widgetNode;

    // --- visual options ---
    private final int cXStart = 50;
    private final int cYStart = 50;
    @Getter @Setter private int ySpace = 200;
    @Getter @Setter private int xSpace = 100;
    private boolean doLayout = true;

    // todo under development
    private final boolean readonly;

    // --- X ---
    private final Map<Edge,RelationWidget> edge2widget = new HashMap<>();
    @Getter private final Map<ConnectionWidget,Pair<Edge,FForm>> leg2objs   = XCols.newHashMap();

    /** edge -> all error links pointing to it */
    private final MultiMap<Edge,ErrorLinkWidget> edge2errorLinkEnds = XCols.newMultiHashHashMap();

    protected final LayerWidget backgroundLayer         = new LayerWidget(this);   // for selection
    protected final LayerWidget mainLayer               = new LayerWidget(this);   // for form nodes
    protected final LayerWidget connectionCenterLayer   = new LayerWidget(this);   // for edge center-node
    protected final LayerWidget connectionLegLayer      = new LayerWidget(this);   // for edge legs
    protected final LayerWidget interractionLayer       = new LayerWidget(this);   // arrows, links etc while being drawn
    protected final LayerWidget errorLinkLayer          = new LayerWidget(this);   // for error links
    protected final LayerWidget sLayer                  = new LayerWidget(this);   // sentence (+?layer?)
    protected final LayerWidget infoLayer               = new LayerWidget(this);   // todo: messages, shading


    protected final SceneLayout edgeInitLayout;
    //todo; USE if s movement is required:
    //protected final SceneLayout sentenceInitLayout;

    //private final TextFieldInplaceEditor formEditor     = ;

    private WidgetAction formEditorAction;
    private WidgetAction formMoveAction;
    private WidgetAction moveEdgeAction;

    private WidgetAction form2XConnectAction;
    private WidgetAction edge2XConnectAction;

    private WidgetAction formPopupRoAction;
    private WidgetAction formPopupAction;
    private WidgetAction wFormPopupAction;
    private WidgetAction legPopupAction;
    private WidgetAction errorLinkPopupAction;

    @Getter @Setter private Profile profile;

    public LayeredGraph(final ParaModel aParaModel, final WidgetNode aWidgetNode, LookAndFeel aLookAndFeel) {
        this(aParaModel, aWidgetNode, aLookAndFeel, false);
    }
        
    public LayeredGraph(final ParaModel aParaModel, final WidgetNode aWidgetNode, LookAndFeel aLookAndFeel, boolean readonly) {
        setLookFeel(aLookAndFeel);

        this.readonly = readonly;
        
        //para = aPara;
        layer = (LLayer) aParaModel.getPseudoModel().getTopLayer();

        paraModel = aParaModel;
//        model = aParaModel.getPseudoModel();
        
        widgetNode = aWidgetNode;

        //addChild(layerLayer);            // shading separating layers
        addChild(backgroundLayer);       // for selection
        addChild(mainLayer);             // for form nodes
        addChild(connectionCenterLayer); // for edge center-node
        addChild(connectionLegLayer);    // for edge legs
        addChild(interractionLayer);
        addChild(errorLinkLayer);
        addChild(sLayer);                // for sentence boxes
        addChild(infoLayer);

        //interractionLayer.getActions().addAction(new KeyboardAction());   // todo temp
        //connectionCenterLayer.setLayout(new EdgeLayout());

        edgeInitLayout = LayoutFactory.createDevolveWidgetLayout(connectionCenterLayer, new EdgeLayout(this), false);
        // sentenceInitLayout = LayoutFactory.createDevolveWidgetLayout(sLayer, new SentenceLayout(), false);

        SentenceLayout layout = new SentenceLayout();
        sLayer.setLayout(layout);

        initActions();

        //setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_CHILDREN);
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);
    }

    /**
     * Modifying actions are created even for read-only views, but are not added to the nodes.
     * Is that a good strategy? Maybe use something like modes instead.
     */
    private void initActions() {
        // --- scene actions ---
        getActions().addAction(ActionFactory.createCenteredZoomAction(1.1));
        getActions().addAction(ActionFactory.createPanAction());
        getActions().addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));
        getActions().addAction(SentenceWidget.createMenu(this));

        // --- shared node actions ---
        formEditorAction = ActionFactory.createInplaceEditorAction(new LabelTextFieldEditor());     // used by non-UI code, so disregard ro flag

        // todo: move multiple
        final MultipleMoveStrategy2 formMoveStrategy = new MultipleMoveStrategy2(this);
        formMoveAction = ActionFactory.createMoveAction(formMoveStrategy, formMoveStrategy);
        // todo ctrl formMoveAction = new CtrlMoveAction(formMoveStrategy, formMoveStrategy);

        // move strategy ensuring that edges cannot be moved between layers
        final MoveStrategy edgeMoveStrategy = new MoveStrategy() {
            public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
                return new Point(suggestedLocation.x, originalLocation.y);
            }
        };

        moveEdgeAction = ActionFactory.createMoveAction(edgeMoveStrategy, ActionFactory.createDefaultMoveProvider());
        //todo ctrl moveEdgeAction = new CtrlMoveAction(edgeMoveStrategy, ActionFactory.createDefaultMoveProvider());

        form2XConnectAction = ActionFactory.createExtendedConnectAction(interractionLayer, new Form2XConnectProvider(this));
        edge2XConnectAction = ActionFactory.createExtendedConnectAction(interractionLayer, new Edge2XConnectProvider(this));
        // todo ctrl form2XConnectAction = ActionFactory.createConnectAction(interractionLayer, new Form2XConnectProvider(this));
        // todo ctrl edge2XConnectAction = ActionFactory.createConnectAction(interractionLayer, new Edge2XConnectProvider(this));


        // --- Popups ---
        formPopupRoAction = WidgetMenuProvider.createAction(this,
                new DisplayPropertiesAction(this)
        );
        formPopupAction = WidgetMenuProvider.createAction(this,
                new EditFormAction(this),
                new FormDeleteAction(this),
                new FormSyncAction(this),
                new EdgeAddInsertAction(this),
                new DisplayPropertiesAction(this),
                new SpellCheckMenu(this));
        wFormPopupAction = WidgetMenuProvider.createAction(this,
                new FormSyncAction(this),
                new EdgeAddDelAction(this),
                new DisplayPropertiesAction(this));
        legPopupAction = WidgetMenuProvider.createAction(this, new LegDel(this));
        errorLinkPopupAction = WidgetMenuProvider.createAction(this, new ErrorLinkDel(this));

    }

    public boolean isReadonly() {
        return readonly || layer.isReadOnly(); //x model.isReadOnly();
    }


    public void initLayout() {
        edgeInitLayout.invokeLayout();
        //sentenceInitLayout.invokeLayout();
    }

    public void clear() {
        final Collection<?> objs = new ArrayList<>(getObjects());
        for (Object obj : objs) {
            super.removeObject(obj);
        }

        edge2widget.clear();
        leg2objs.clear();
        edge2errorLinkEnds.clear();

        backgroundLayer.removeChildren();
        mainLayer.removeChildren();
        connectionCenterLayer.removeChildren();
        connectionLegLayer.removeChildren();
        interractionLayer.removeChildren();
        errorLinkLayer.removeChildren();
        sLayer.removeChildren();
        infoLayer.removeChildren();
    }

    private JViewport getViewport() {
        return getView().getParent() instanceof JViewport ? (JViewport)getView().getParent() : null;
    }

    // combine with TC's equivalent methods?
    private Point rememberPos() {
        JViewport viewPort = getViewport();
        return (viewPort == null) ? new Point(0,0) : viewPort.getViewPosition();
    }

    private void restorePos(final Point aPoint) {
        JViewport viewPort = getViewport();
        if (viewPort != null) viewPort.setViewPosition(aPoint);
    }

    /** TODO remember selection */
    private void redraw() {
        Point pos = rememberPos();
        Object obj = getFocusedObject();
        // todo clean up, this is a little bit over-board

        //getParaModel().fillForms();
        clear();
        super.validate();
        initLayout();
        validate();
        revalidate();
        draw();
        validate();
        revalidate();
        initLayout();
        repaint();

        setFocusedObject(obj);
        restorePos(pos);
    }



// =============================================================================
// objects <-> widgets
// =============================================================================

    public RelationWidget findRelWidget(Edge aRel) {
        return edge2widget.get(aRel);
    }

    public WordWidget findFormWidget(FForm aForm) {
        return (WordWidget) findWidget(aForm);
    }

    public FForm findForm(Widget aWidget) {
        return (FForm) findObject(aWidget);
    }

// =============================================================================    
// Highlighting
// =============================================================================    

    // this is a mess, unify various widges and pseudo widgets
    private void update(Element el) {
        if (el instanceof FForm) {
            WordWidget  w = findFormWidget((FForm)el);
            if (w != null) w.update();
        }
        else if (el instanceof Edge) {
            RelationWidget  w = findRelWidget((Edge)el);
            if (w != null) w.update();
        }
        else {
            log.warning("%s (el) does not support update", el, (el != null ? el.getClass() : el) );
        }
    }

    /**
     * Set of highlighted elements; {@see #getHighlights()}.
     */
    private final Set<Element> xHighlights = new HashSet<>();
    private final Set<Element> xHighlightsUm = Collections.unmodifiableSet (xHighlights);
    
    /**
     * Unmodifiable set of highlighted elements. Note that this is a different 
     * type of highlighting than the highlighting provided by the ObjectScene:
     * 
     * <ul>
     * <li>ObjectScene's highlighting: we use it for a synchronized cursors
     * <li>LayeredGraph's xhighlighting: we use it to draw attention to certain elements.
     * Can be use to highlight search results, differences between two annotations, etc.
     * </ul>
     * 
     * @todo use a different name
     * @todo allow various types (so we can highlight searches, errors and differences at the same time)
     */
    public Set<Element> getXHighlights() {
        return xHighlightsUm;
    }
    
    public void setXHighlights(Collection<? extends Element> els) {
        for (Iterator<Element> it = xHighlights.iterator(); it.hasNext();) {
            Element el = it.next();
            if (!els.contains(el)) {
                it.remove();
                update(el);
            }
        }

        for (Element element : els) {
            if (!xHighlights.contains(element)) {
                xHighlights.add(element);
                update(element);
            }
        }
    }
    
    public void addXHighlights(Collection<? extends Element> els) {
        for (Element element : els) {
            if (!xHighlights.contains(element)) {
                xHighlights.add(element);
                update(element);
            }
        }
    }

    public void removeXHighlights(Collection<? extends Element> els) {
        for (Iterator<Element> it = xHighlights.iterator(); it.hasNext();) {
            Element el = it.next();
            if (els.contains(el)) {
                it.remove();
                update(el);
            }
        }
    }
    
// =============================================================================
// drawing
// =============================================================================

    public void draw() {
        final List<List<? extends FForm>> nodes = paraModel.getNodes();
        log.fine("IDrawing. No of layers %s", nodes.size());
        
        if (nodes.get(0).isEmpty()) return;

        // --- draw nodes ---
        drawNodes();
        setCursor(nodes.get(0).get(0));

        // --- draw sentences ---
        for (int layerIdx = 0; layerIdx < nodes.size(); layerIdx++) {
            drawSentences(layerIdx);
        }

        // --- draw edges ---
        log.info("=== Drawing edges ===");
        for (int layerIdx = 1; layerIdx < nodes.size(); layerIdx++) {
            drawEdges(layerIdx);
        }
        log.info("=== Done Drawing edges ===");

        // --- draw error links (edges must exist already) ---
        for (int layerIdx = 1; layerIdx < nodes.size(); layerIdx++) {
            drawErrorLinks(layerIdx);
        }
    }
    
    protected NodeLayout getNodeLayout() {
        final GraphLayout2 layout = new GraphLayout2(paraModel);
        layout.go();
        return layout;
    }

    protected void drawNodes() {
        final List2D<? extends FForm> placedNodes = getNodeLayout().getArrangedForms();

        for (int rowIdx = 0; rowIdx < placedNodes.size(); rowIdx++) {
            final List<? extends FForm> row = placedNodes.getRow(rowIdx);
            for (int colIdx = 0; colIdx < row.size(); colIdx++) {
                FForm form = row.get(colIdx);
                if (form == null) continue;
                drawNode(form, pos2place(rowIdx, colIdx));
            }
        }
    }

    protected void drawSentences(final int aLayerIdx) {
        final Para para = paraModel.getParas().get(aLayerIdx);

        if (para instanceof LPara) {
            for(Sentence s : ((LPara)para).getSentences()) {
                drawSentence(s);
            }
//            if (((LPara)para).getSentences().isEmpty()) {
//                infoLayer.addChild(new LabelWidget("Click!"));
//            }

        }
    }

    protected void drawSentence(final Sentence aSentence) {
        final SentenceWidget swidget = new SentenceWidget(this, aSentence);

        sLayer.addChild(swidget);
        addObject(aSentence, swidget);

        for (LForm f : aSentence.col()) {
            swidget.getFormWidgets().add(findFormWidget(f));
        }
    }

    protected WordWidget drawNode(final FForm node, final Point aLoc) {
        final WordWidget nodeWidget = createNode(node);

        mainLayer.addChild(nodeWidget);

        Err.iAssert(node !=null, "Node null (%d:%d)");
        Err.iAssert(! getObjects().contains(node), "Node (%s, id=%s) already in objects", node, node.getId());
        addObject(node, nodeWidget);

        nodeWidget.setPreferredLocation(aLoc);

        return nodeWidget;
    }

    /**
     * Does not draw error links (during initial setup, target edges of error links might not exist yet. */
    protected RelationWidget drawEdge(final Edge aRel) {
        //log.info("Drawing edge: " + aRel);
        final RelationWidget edgeWidget = createEdge(aRel);     // also draws errors if any
        //connectionLegLayer.addChild(edgeWidget);

        edge2widget.put(aRel, edgeWidget);
        addObject(aRel, edgeWidget.getCenterNode());

        for (FForm node : aRel.getTo()) {
            drawLeg(aRel, edgeWidget, node);
        }
        for (FForm node : aRel.getFrom()) {
            drawLeg(aRel, edgeWidget, node);
        }

        //if (positioned do layout)
        if (mainLayer.isValidated()) {
            Point point = edgeWidget.getCenterNode().getBestLocation();
            edgeWidget.getCenterNode().setPreferredLocation(point);
        }

        return edgeWidget;
    }

    protected LegWidget drawLeg(Edge aRel, RelationWidget edgeWidget, FForm node) {
        final LabelWidget nodeWidget  = (LabelWidget) findWidget(node);
        final LegWidget legw = edgeWidget.addLeg(nodeWidget);
        leg2objs.put(legw, new Pair<Edge,FForm>(aRel,node) );

        return legw;
    }

    private void drawEdges(final int aHigherLayerIdx) {
        log.info("   Drawing edges layer %d: # of edges = %d", aHigherLayerIdx, paraModel.getEdges(aHigherLayerIdx).size());
        for (Edge edge : paraModel.getEdges(aHigherLayerIdx)) {
            drawEdge(edge);
        }
    }

    private void drawErrorLinks(final int aHigherLayerIdx) {
        for (Edge edge : paraModel.getEdges(aHigherLayerIdx)) {
            RelationWidget relW = findRelWidget(edge);
            relW.drawErrorLinks(errorLinkPopupAction);
        }
    }


// =============================================================================
// Responding to model modifications
// reflects the change on the screen
// =============================================================================
    
    @Override
    public void handleChange(final DataEvent aE) {
        if (aE instanceof ChangeEvent) handleChange((ChangeEvent)aE);
    }
    
    public void handleChange(final ChangeEvent aE) {
        log.info("handleChange: %s", aE);

        if (! relevant(aE)) return;  // not affecting this paragraph stack

        final boolean iAmSrc = aE.getSrcView() == this;
        final Object srcInfo = iAmSrc ? aE.getSrcViewInfo() : null;

        if (aE.getId().equals(ChangeEvent.cFormEdit)) {   handle_formEdit(aE.form, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cFormAdd)) {    handle_formAdd (aE.form, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cFormDel)) {    handle_formDel (aE.form, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cFormMove)) {   handle_formMove(aE.form, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cFormsMove)) {  handle_formsMove(aE.forms, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cFormChange)) { handle_formChange(aE.form, iAmSrc, srcInfo); }

        else if (aE.getId().equals(ChangeEvent.cEdgeAdd)) {    handle_edgeAdd(   aE.edge, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cEdgeDel)) {    handle_edgeDel(   aE.edge, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cEdgeChange)) { handle_edgeChange(aE.edge, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cLegAdd)) {     handle_legAdd (aE.edge, aE.form, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cLegDel)) {     handle_legDel (aE.edge, aE.form, iAmSrc, srcInfo); }

        else if (aE.getId().equals(ChangeEvent.cErrorAttrChange)) { handle_errorAttrChange(aE.error,  iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cErrorAdd)) {        handle_errorAdd       (aE.error,  iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cErrorDel)) {        handle_errorDel       (aE.error,  iAmSrc, srcInfo); }

        else if (aE.getId().equals(ChangeEvent.cErrorLinkAdd)) {  handle_errorLinkAdd(aE.error, aE.edge, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cErrorLinkDel)) {  handle_errorLinkDel(aE.error, aE.edge, iAmSrc, srcInfo); }

        else if (aE.getId().equals(ChangeEvent.cSentenceMerge)) { handle_sentenceMerge(aE.sentence1, aE.sentence2, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cSentenceSplit)) { handle_sentenceSplit(aE.sentence1, aE.sentence2, iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cSentenceDel)) {   handle_sentenceDel  (aE.sentence1,               iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cSentenceAdd)) {   handle_sentenceAdd  (aE.sentence1,               iAmSrc, srcInfo); }
        else if (aE.getId().equals(ChangeEvent.cSentenceCopy)) {  handle_sentenceCopy (aE.sentence1, aE.sentence2, iAmSrc, srcInfo); }

        validate();
        log.info("Finished handleChange: %s", aE);
    }

    /** Checks if relevant, i.e. if affecting this paragraph stack */
    public boolean relevant(final ChangeEvent aE) {
        return paraModel.getParas().contains(aE.getPara());
    }
    
    private WordWidget getFormWidget(FForm aForm, final boolean iAmSrc, final Object aSrcInfo) {
        return iAmSrc ? (WordWidget)aSrcInfo : this.findFormWidget(aForm);
//        WordWidget w = iAmSrc ? (WordWidget)aSrcInfo : this.findFormWidget(aForm);
//        
//        log.info("getFormWidget: %s %s %s", iAmSrc, (WordWidget)aSrcInfo, this.findFormWidget(aForm));
//        log.info("getFormWidget: form id %s (%s)", aForm.getId().getIdStr(), aForm);
//        
//        Iterable<Form> forms = Iterables.filter(this.getObjects(), (Class<Form>)(Object)Form.class);
//        log.info("getFormWidget: forms %s", Iterables.toString(Id.e2ids(forms)));
//
//        if (w == null) throw new NullPointerException();
//        
//        return w;
    }
    
    

    
    
    
//  public static <T> Iterable<T> filterX(final Iterable<?> unfiltered, final Class<T> type) {
//        return null;
//  }
//    
//  public static <T> Iterable<T> filterY(final Iterable<?> unfiltered, final Class<T> type) {
//        return null;
//  }
    

    private void handle_formEdit(FForm aForm, final boolean iAmSrc, final Object aSrcInfo) {
        getFormWidget(aForm, iAmSrc, aSrcInfo).update();
        // todo redo layout
    }

    private void handle_formAdd(FForm newNode, final boolean iAmSrc, final Object aSrcInfo) {
        if (iAmSrc) {
            Point loc = (Point)aSrcInfo;
            Widget formW = drawNode(newNode, layerRound(loc));
            validate();
            ActionFactory.getInplaceEditorController(formEditorAction).openEditor(formW);
        }
        else {
            // todo remember and restore position
            // redraw the whole thing
            // todo half of this validate/revalidate is not needed
            redraw();
            //graphComponent.repaint();

        }
    }

    private void handle_formDel(FForm aForm, final boolean iAmSrc, final Object aSrcInfo) {
        log.info("(%s) handle_formDel %s", this.hashCode(), aForm);

        WordWidget formW = getFormWidget(aForm, iAmSrc, aSrcInfo);
        log.info("handle_formDel > formW=%s", formW);
        removeObject(aForm);
        formW.removeFromParent();
        log.info("handle_formDel > formW=%s", findWidget(aForm));

        validate();
    }




    private void handle_formMove(FForm newNode, final boolean iAmSrc, final Object aSrcInfo) {
        if (iAmSrc) {
//            Point orig = ...
//            if (orig < newPos) {
//
//            }
//            else {
//
//            }
//            WordWidget formW = this.findFormWidget(newNode);
//            final int x = formW.getLocation().x;
//            final int deltaX = 50;
//            for (Widget nodeW : this.mainLayer.getChildren()) {
//                if (nodeW.getLocation().x > x) {
//                    Point loc = nodeW.getLocation();
//                    loc.translate(deltaX, 0);
//
//                    nodeW.setPreferredLocation(loc);
//                }
//            }

            //redraw();
//            Point loc = (Point)aSrcInfo;
//            drawNode(newNode, layerRound(loc));
            // do nothing - already moved
        }
        else {
            // todo find node position
            redraw();
        }
    }

    private void handle_formsMove(final List<? extends FForm> aMovedNodes, final boolean iAmSrc, final Object aSrcInfo) {
        if (iAmSrc) {
            redraw();
        }
        else {
            // todo find node position
            redraw();
        }
    }

    /** Other change (e.g. comment). */
    private void handle_formChange(FForm aForm, final boolean iAmSrc, final Object aSrcInfo) {
        getFormWidget(aForm, iAmSrc, aSrcInfo).update();
    }

    private void handle_legAdd(final Edge aRel, final FForm aForm, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_addLeg");
        RelationWidget relW = this.findRelWidget(aRel);

        drawLeg(aRel, relW, aForm);
    }

    private void handle_edgeAdd(final Edge aRel, final boolean iAmSrc, final Object aSrcInfo) {
        final RelationWidget relW = drawEdge(aRel);
        validate();
        //relW.drawErrorLinks(errorLinkPopupAction);
    }

    private void handle_edgeDel(final Edge aRel, final boolean iAmSrc, final Object aSrcInfo) {
        final RelationWidget relW = edge2widget.remove(aRel);

        removeObject(aRel);
        relW.removeFromParent();    // removes centernode, legs and errors (inc. error links)
    }


    private void handle_edgeChange(final Edge aRel, final boolean iAmSrc, final Object aSrcInfo) {
        final RelationWidget relW = this.findRelWidget(aRel);
        log.info("handle_edgeChange rel=%s, relW=%s", aRel, relW);
        Err.iAssert(relW != null, "null widget");
        relW.otherChange();
        validate();

    }


    private void handle_legDel(final Edge aRel, final FForm aForm, final boolean iAmSrc, final Object aSrcInfo) {
        final RelationWidget relW = this.findRelWidget(aRel);
        LabelWidget formW = this.findFormWidget(aForm);

        relW.removeLeg(formW);
    }

    private void handle_errorAdd(final Errorr aError, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_errorX");

        final Edge rel = aError.getParent();
        Err.iAssert(rel != null, "null parent (error=%s)", aError);
        final RelationWidget relW = this.findRelWidget(rel);
        log.info("handle_errorAdd rel=%s, relW=%s", rel, relW);
        Err.iAssert(relW != null, "null widget");
        relW.errorAdd(aError);

        for (Edge targetEdge : aError.getLinks()) {
            relW.drawErrorLink(aError, targetEdge, errorLinkLayer, errorLinkPopupAction);
        }

        validate();
        //log.info("selected3: " + getSelectedObjects());
    }

    private void handle_errorAttrChange(final Errorr aError, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_errorAttrChange");

        Edge rel = aError.getParent();
        Err.iAssert(rel != null, "null parent (error=%s)", aError);
        final RelationWidget relW = this.findRelWidget(rel);
        log.info("handle_errorAttrChange rel=%s, relW=%s", rel, relW);
        Err.iAssert(relW != null, "null widget");
        relW.otherChange();     // todo ?? more nuanced method?
        validate();
    }

    private void handle_errorDel(final Errorr aError, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_errorDel");

        final Edge rel = aError.getParent();
        Err.iAssert(rel != null, "null parent (error=%s)", aError);
        final RelationWidget relW = this.findRelWidget(rel);
        log.info("handle_errorX rel=%s, relW=%s", rel, relW);
        Err.iAssert(relW != null, "null widget (error=%s, rel=%s)", aError, rel);
        relW.errorDel(aError);
        validate();
        log.info("selected: " + getSelectedObjects());
    }


    private void handle_errorLinkAdd(final Errorr aError, final Edge aEdge, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_errorLinkAdd");
        final RelationWidget srcRelW = findRelWidget(aError.getParent());

        srcRelW.drawErrorLink(aError, aEdge, errorLinkLayer, errorLinkPopupAction);
    }

    private void handle_errorLinkDel(final Errorr aError, final Edge aEdge, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_errorLinkDel");
        final RelationWidget srcRelW = findRelWidget(aError.getParent());
        srcRelW.errorLinkDel(aError, aEdge);
    }


    private void handle_sentenceAdd(final Sentence aSentence, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_sentenceAdd");
        redraw();
    }

    private void handle_sentenceDel(final Sentence aSentence, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_sentenceDel");

        // remove all forms (note: the forms are detached from all structures except the sentence object)
        for (FForm form : aSentence.col()) {
            LabelWidget formw = this.findFormWidget(form);
            removeObject(form);
            formw.removeFromParent();
        }

        // remove old sentence widgets
        Widget sw = findWidget(aSentence);
        removeObject(aSentence);
        sw.removeFromParent();
    }

    private void handle_sentenceCopy(final Sentence aSentence1,final Sentence aSentence2, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_sentenceCopy");

        final int y = layer2y(aSentence2.getLayer().getLayerIdx());
        // TODO make space if necessary

        // add forms
        for (int i = 0; i < aSentence2.col().size(); i++) {
            final LForm form2 = aSentence2.col().get(i);

            final LForm form1 = aSentence1.col().get(i);
            final Point form1Loc = getLocation(form1);

            drawNode(form2, new Point(form1Loc.x, y));
        }
        validate();

        // add sentence (probably does not do any positioning)
        drawSentence(aSentence2);
        validate();

        // add edges
//        final List<CentralNode> cnodes = new ArrayList<>();
//        for (LForm form : aSentence2.col()) {
//            for (Edge e : form.getLower()) {
//                RelationWidget w = drawEdge(e);
//                cnodes.add(w.getCenterNode());
//            }
//        }

        validate();

        if (true) return;

        JScrollPane pane = null;
        JViewport viewPort = null;
        if (getView().getParent() instanceof JScrollPane ) {
            pane = (JScrollPane) getView().getParent();
            viewPort = pane.getViewport();
        }
        else if (getView().getParent().getParent() instanceof JScrollPane ) {
            pane = (JScrollPane) getView().getParent().getParent();
            viewPort = pane.getViewport();
        }

        if (viewPort == null) {
            redraw();
            return;
        }

        Point pos = viewPort.getViewPosition();
        redraw();
        viewPort.setViewPosition(pos);
    }


    private void handle_sentenceMerge(final Sentence aSentence, final Sentence aSentence2, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_sentenceMerge");

        // remove old sentence widgets
        Widget w1 = findWidget(aSentence);
        removeObject(aSentence);
        w1.removeFromParent();

        Widget w2 = findWidget(aSentence2);
        removeObject(aSentence2);
        w2.removeFromParent();

        validate();

        // draw the new merged sentence
        drawSentence(aSentence);
        validate();

    }

    private void handle_sentenceSplit(final Sentence aSentence1, final Sentence aSentence2, final boolean iAmSrc, final Object aSrcInfo) {
        log.fine("handle_sentenceSplit");

        // remove old sentence widget
        Widget w = findWidget(aSentence1);
        removeObject(aSentence1);
        w.removeFromParent();
        validate();

        drawSentence(aSentence1);
        drawSentence(aSentence2);
        revalidate();
        repaint();
    }



// =============================================================================
// Actions
// =============================================================================

// -----------------------------------------------------------------------------
// Actions - moves
// -----------------------------------------------------------------------------

    // todo experimental, probably split into several fncs, some in the model, can go across paragraphs!!
//    public void moveToForm(int aPos) {
//        log.fine("moveToForm: pos=%d", aPos);
//        // todo use binary search
//        WLayer wlayer = (WLayer) getModel().getFormLayers().get(0);
//        for (WForm form : wlayer.getForms()) {
//            log.fine("moveToForm: form: %s %d", form.getToken(), form.getDocOffset());
//            if (form.getDocOffset() <= aPos && aPos <= form.getDocOffset() + form.getLen()) {
//                if (form.getParent() == getParaModel().getParas().get(0)) {
//                    log.fine("moveToForm: movingg");
//                    moveToForm(form, 0, true);
//                    break;
//                }
//                else {
//                    log.fine("Out of the current paragraph, not handled yet");
//                }
//            }
//        }
//    }



    private void moveToFormVert(FForm aForm, boolean aToHigherLayer, boolean aResetSelection) {
        final Point curLoc = findFormWidget(aForm).getLocation();
        final Point newLoc = new Point(curLoc.x, curLoc.y + (aToHigherLayer ? ySpace : -ySpace) );
        final FForm newForm = findForm(place2formW(newLoc));

        if (newForm != null) setCursor(newForm, aResetSelection);
    }

    /**
     * Set cursor absolutely.
     * @param aLayer
     * @param aIdx
     */
    private void moveToForm(FormsLayer<?> aLayer, int aIdx, boolean aBackward, boolean aResetSelection) {
        final List<FForm> forms = getParaModel().getNodes(aLayer);
        if (aBackward) {
            aIdx = forms.size() - aIdx - 1;
        }

        final FForm newForm = forms.get(aIdx);
        if (newForm != null) setCursor(newForm, aResetSelection);
    }

    /**
     * Set cursor relatively to a form
     * @param aForm
     * @param aOffset
     */
    private void moveToForm(FForm aForm, int aOffset, boolean aResetSelection) {
        final List<FForm> forms = getParaModel().getNodes((FormsLayer<?>)aForm.getLayer());
        final FForm newForm = Util.getOffset(forms, aForm, aOffset);
        if (newForm != null) setCursor(newForm, aResetSelection);
    }

    /**
     * Moves to an edge absolutely.
     * @todo incredibly inefficient, sorts edges by x position on each call, but seems to work fast enough
     *
     * @param aPara
     * @param aIdx index relative to the start or end of the list of edges
     * @param aBackward count index from the end?
     * @param aResetSelection
     */
    private void moveToEdge(LPara aPara, int aIdx, boolean aBackward, boolean aResetSelection) {
        final List<Edge> edges = sortByXPos(aPara.getEdges());

        if (aBackward) {
            aIdx = edges.size() - aIdx - 1;
        }

        final Edge newEdge = edges.get(aIdx);
        if (newEdge != null) setCursor(newEdge, aResetSelection);
    }

    /**
     * Moves to an edge relative to this one.
     * @todo incredibly inefficient, sorts edges by x position on each call, but seems to work fast enough
     *
     * @param aEdge
     * @param aOffset
     * @param aResetSelection
     */
    private void moveToEdge(Edge aEdge, int aOffset, boolean aResetSelection) {
        final List<Edge> edges = sortByXPos(aEdge.getParent().getEdges());

        int curIdx = edges.indexOf(aEdge);
        int newIdx = curIdx + aOffset;

        if (0 <= newIdx && newIdx < edges.size()) {
            setCursor(edges.get(newIdx), aResetSelection);
        }
    }

    /**
     * @todo incredibly inefficient, sorts edges by x position on each call, but seems to work fast enough
     *
     * @param aPara
     * @return
     */
    private List<Edge> sortByXPos(final List<Edge> aEdges) {
        Collections.sort(aEdges, new Comparator<Edge>() {
            public int compare(Edge o1, Edge o2) {
                return xPos(o1) - xPos(o2);
            }

            private int xPos(Edge aE) {
                return findWidget(aE).getLocation().x;
            }

        });
        return aEdges;
    }

// -----------------------------------------------------------------------------
//
// -----------------------------------------------------------------------------

    @Override
    public JComponent createView() {
        super.createView();
        getView().setFocusable(true);
        getActions().addAction(new SceneClickAction(this));
        addKeyboardActions();
        return getView();
    }




// =============================================================================
// Keyboard
// =============================================================================

    /**
     * Action performed on some object (form, edge)
     */
    public class ObjAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            Object obj = getFocusedObject();
            actionPerformed(obj);
        }
        public void actionPerformed(Object obj) {
            if (obj instanceof FForm) {
                formAction((FForm)obj);
            }
            else if (obj instanceof Edge) {
                relAction((Edge)obj);
            }
        }
        protected void formAction(FForm aForm) {}
        protected void relAction(Edge aRel) {}
    }

    public class EditObjAction extends ObjAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isReadonly()) {Toolkit.getDefaultToolkit().beep();return;}
            super.actionPerformed(e);
        }
    }

    private LayeredViewTopComponent getLayeredViewTc() {
        return (LayeredViewTopComponent) SwingUtilities.getAncestorOfClass(LayeredViewTopComponent.class, getView());
    }

    /** todo under development */
    public void addKeyboardActions() {
        // todo experimetal intra paragraph move
        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, java.awt.event.KeyEvent.CTRL_DOWN_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
               LayeredViewTopComponent tc = getLayeredViewTc();
               if (tc == null) return;
               tc.requestPara(0);
            }
        });


        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_END, java.awt.event.KeyEvent.CTRL_DOWN_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
               LayeredViewTopComponent tc = getLayeredViewTc();
               if (tc == null) return;
               tc.requestPara(paraModel.getParas().size() - 1 );  // todo last para
            }
        });


        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, 0), new ObjAction() {
            public void formAction(FForm aForm) {
                moveToForm( (FormsLayer<?>) aForm.getLayer(), 0, false, true);
            }
            public void relAction(Edge aRel) {
                moveToEdge(aRel.getParent(), 0, false, true);
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_END, 0), new ObjAction() {
            public void formAction(FForm aForm) {
                moveToForm( (FormsLayer<?>) aForm.getLayer(), 0, true, true);
            }
            public void relAction(Edge aRel) {
                moveToEdge(aRel.getParent(), 0, true, true);
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0), new ObjAction() {
            public void formAction(FForm aForm) {
                moveToForm(aForm, -1, true);
            }
            public void relAction(Edge aRel) {
                moveToEdge(aRel, -1, true);
            }

        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, java.awt.event.KeyEvent.CTRL_DOWN_MASK), new ObjAction() {
            public void formAction(FForm aForm) {
                moveToForm(aForm, -1, false);
            }
            public void relAction(Edge aRel) {
                moveToEdge(aRel, -1, false);
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0), new ObjAction() {
            public void formAction(FForm aForm) {
                moveToForm(aForm, 1, true);
            }
            public void relAction(Edge aRel) {
                moveToEdge(aRel, 1, true);
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.KeyEvent.CTRL_DOWN_MASK), new ObjAction() {
            public void formAction(FForm aForm) {
                moveToForm(aForm, 1, false);
            }
            public void relAction(Edge aRel) {
                moveToEdge(aRel, 1, false);
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0), new ObjAction() {
            public void formAction(FForm aForm) {
                final Set<Edge> edges = aForm.getHigher();
                if (edges.isEmpty()) {
                    moveToFormVert(aForm, true, true);
                }
                else {
                    setCursor(edges.iterator().next()); // TODO FIRST BY WO1
                }
            }

            public void relAction(Edge aRel) {
                Set<LForm> higherForms = aRel.getHigher();  // todo higher within the view
                if (higherForms.isEmpty()) return;
                setCursor(higherForms.iterator().next()); // TODO FIRST BY WO
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0), new ObjAction() {
            public void formAction(FForm aForm) {
                final Set<Edge> edges = aForm.getLower();
                if (edges.isEmpty()) {
                    moveToFormVert(aForm, false, true);
                }
                else {
                    setCursor(edges.iterator().next()); // TODO FIRST BY WO1
                }
            }

            public void relAction(Edge aRel) {
                Set<FForm> lowerForms = aRel.getLower();  // todo higher within the view
                if (lowerForms.isEmpty()) return;
                setCursor(lowerForms.iterator().next()); // TODO FIRST BY WO
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0), new EditObjAction() {
            @Override
            public void formAction(FForm aForm) {
                Widget widget = getFocusedWidget();
                if (findObject(widget) instanceof LForm) {
                    WAction action = new EditFormAction(LayeredGraph.this);
                    if (action.isEnabled()) action.actionPerformed(widget);
                }
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0), new EditObjAction() {
            @Override
            public void formAction(FForm aForm) {

                Widget widget = findWidget(aForm);
                WAction action = new FormDeleteAction(LayeredGraph.this);
                if (action.isEnabled()) action.actionPerformed(widget);
            }

            @Override
            protected void relAction(Edge aRel) {
                Widget widget = findWidget(aRel);
                new FormDeleteAction(LayeredGraph.this).actionPerformed(widget);
            }
        });

        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                log.info("test f4");
            }
        });


        addKey(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK), new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (isReadonly()) {Toolkit.getDefaultToolkit().beep(); return;}
                try {
                    getLayeredViewTc().getUndoRedo().undo();
                }
                catch(CannotUndoException ex) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        });
    }


    public void addKey(KeyStroke aKeyStroke, Action aAction) {
        final String id = "" + getView().getInputMap().size();
        getView().getInputMap().put(aKeyStroke, id);
        getView().getActionMap().put(id, aAction);
    }

// =============================================================================
// Modifications 
// todo make all modifications via these actions
// they all have to respect readonly flag (WAction, GraphAction, AnchorAction do automatically)    
// =============================================================================
    

    public static class EditFormAction extends WAction {
        EditFormAction(LayeredGraph aView) {
            super(aView, "Edit");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("F2"));
        }

        public void actionPerformed(Widget aWidget) {
            //log.info("Edit form " + findForm(aWidget));
            ActionFactory.getInplaceEditorController(view.formEditorAction).openEditor(aWidget);
        }
    }

    private class LabelTextFieldEditor implements TextFieldInplaceEditor {
        public boolean isEnabled (Widget widget) {
            return findObject(widget) instanceof FForm && !LayeredGraph.this.isReadonly();
        }

        public String getText (Widget widget) {
            return ((LabelWidget) widget).getLabel ();
        }

        public void setText (Widget widget, String text) {
            FForm form = findForm(widget);
            FormsLayer<?> layer = (FormsLayer<?>) form.getLayer();
            // todo read-only
            layer.formEdit(form, text, LayeredGraph.this, widget);
        }
    }

    public static class FormAddAction extends AnchorAction {
        FormAddAction(LayeredGraph aView) {
            super(aView, "Create new form");
        }

        public void actionPerformed(Position aAnchor, Point aPoint) {
            aAnchor.getForm().getLayer().formAdd( "?", aAnchor, view, aPoint);
        }
    }


    public static class FormDeleteAction extends WAction {
        FormDeleteAction(LayeredGraph aView) {
            super(aView, "Delete");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
        }

        public void actionPerformed(Widget nodeWidget) {
                    
            for (Object obj : new ArrayList<>(view.getSelectedObjects()) ) {
                if (!(obj instanceof FForm)) continue;
                FForm form = (FForm) obj;

                final FormsLayer<?> layer = form.getLayer();

                if (!layer.isReadOnly()) {
                    log.info("Deleting form " + form);

                    // if this was the cursor, move it
                    if (form == view.getFocusedObject()) {
                        int offset = ( getParaModel().getNodes((FormsLayer<?>)form.getLayer()).get(0) == form ) ? 1 : -1;
                        view.moveToForm(form, offset, true);
                    }

                    layer.formDel(form, view, nodeWidget);
                }
                else {
                    log.info("Skipping r/o form " + form);
                }
            }
        }

//        // todo old - remove?
//        protected void actionPerformed1(Widget nodeWidget) {
//            final Form form = (Form)view.findObject(nodeWidget);
//
//            log.info("Deleting form " + form);
//
//            final Layer<?> layer = form.getLayer();
//
//            if (layer.isReadOnly()) {
//                log.info("  Read only layer: " + layer);
//                return;
//            }
//
//            getModel().formDel(form, view, nodeWidget);
//        }
    }

    public class FormSyncAction extends WAction {
        FormSyncAction(LayeredGraph aView) {
            super(aView, "Sync with higher layer");
        }

        public void actionPerformed(Widget nodeWidget) {
            final FForm form = (FForm)findObject(nodeWidget);

            final FForm higherForm = getSimpleHigherForm(form);
            if (higherForm == null || higherForm.getLayer().isReadOnly()) return;

            higherForm.getLayer().formEdit(higherForm, form.getToken(), LayeredGraph.this, findWidget(higherForm));
        }

        @Override
        public boolean isEnabled(Widget aWidget) {
            final FForm form = (FForm)findObject(aWidget);
            final FForm higherForm = getSimpleHigherForm(form);
            return higherForm != null && !higherForm.getLayer().isReadOnly();
        }

        private FForm getSimpleHigherForm(FForm form) {
            if (form.getHigherForms().size() != 1) return null;
            final FForm higherForm = Cols.getFirstElement(form.getHigherForms());
            if (higherForm.getLowerForms().size() != 1) return null;
            return higherForm;
        }



    }

    // todo enable for read-only graphs, but make the property sheet readonly
    public static class DisplayPropertiesAction extends WAction {
        DisplayPropertiesAction(LayeredGraph aView) {
            super(aView, "Properties", true);
        }

        public  void actionPerformed(Widget nodeWidget) {
            NodeOperation.getDefault().showProperties(new Node[]{ view.widgetNode } );      // todo it should probably call some function of the enclosing TC

            log.info("Properties");
        }
    }

    /**
     *
     * Also ensures that forms cannot be moved only into a sentence
     */
    private final MoveProvider formMoveProvider = new MoveProvider() {
        public void movementStarted(Widget widget) {
            log.info("MoveProvider.movementStarted: widget=%s", widget );
        }
        public void movementFinished(Widget widget) {
            final FForm form = findForm(widget);
            final Point newLoc = widget.getLocation();
            final Position anchor = place2anchor(newLoc);

            log.info("MoveProvider.movementFinished: widget=%s, form=%s, anchor=%s",
                    widget, form, anchor);

            if (anchor == null) return;
            if (anchor.getForm() == form) return;  // did not move
            // how about anchor of x = after(x+1)

            FormsLayer<?> layer = (FormsLayer<?>) form.getLayer();
            // check readonly
            layer.formMove(form, anchor, LayeredGraph.this, newLoc);
        }
        public Point getOriginalLocation(Widget widget) {
            return widget.getPreferredLocation ();
        }
        public void setNewLocation (Widget widget, Point location) {
            if (place2sentence(location) == null) return;   // to prevent moving into inter-sentential space

            widget.setPreferredLocation(location);
        }
    };




// =============================================================================
// Primary UI responses that are not triggered by menu
// Can be called only by friendly code    
// =============================================================================

    public void addEdge(FForm aLowerForm, LForm aHigherForm) {
        log.info("Scene: addEdge " + aLowerForm.getId() + " -> " + aHigherForm.getId());
//        getModel().edgeAdd(aLowerForm, aHigherForm, this, null);
        LLayer layer = aHigherForm.getLayer();
        // check ro
       
        layer.edgeAdd(Arrays.<FForm>asList(aLowerForm), Arrays.asList(aHigherForm), LayeredGraph.this, null);
    }

    public void addLeg(Edge aRel, RelationWidget edgeWidget, FForm aNode, LabelWidget nodeWidget) {
        LLayer layer = aRel.getLayer();
        layer.legAdd(aRel, aNode, this, aNode);
    }

    public void addErrorLink(Errorr aSrcErrorInfo, Edge aTargetEdge) {
        LLayer layer = (LLayer) aSrcErrorInfo.getLayer();
        // check ro
        layer.errorLinkAdd(aSrcErrorInfo, aTargetEdge, this, null);
    }


// =============================================================================
//
// =============================================================================

//    private class SceneCreateAction extends WidgetAction.Adapter {
//        public State mousePressed (Widget widget, WidgetMouseEvent event) {
//            log.info("Mouse pressed");
//            if (event.getClickCount () == 1)
//                if (event.getButton () == MouseEvent.BUTTON1 /*|| event.getButton () == MouseEvent.BUTTON3*/) {
//                    Point point = widget.convertLocalToScene(event.getPoint());
//                    Point pos = place2pos(point);
//                    log.info("  Pos " + pos);
//                    if (pos.x == -1 || pos.y == -1) return State.REJECTED;
//
//                    Form node = getModel().getNodes().get(pos.y).get(pos.x);
//                    log.info("  form " + node);
//
//                    return State.CONSUMED;
//                }
//            return State.REJECTED;
//        }
//    }


    protected WordWidget createNode(final FForm aForm) {
        // todo experimetal
        final WordWidget formW = new WordWidget(this, aForm);

        //formW.getActions().addAction(new KeyboardAction()); /// todo temporary
        formW.getActions().addAction(new WidgetClickAction(this));  // todo make singleton
        formW.getActions().addAction(createSelectAction());
        formW.getActions().addAction(createWidgetHoverAction/*createObjectHoverAction*/ ());


        if (isReadonly()) {
            formW.getActions().addAction(formPopupRoAction);
        }
        else {
            formW.getActions().addAction(form2XConnectAction);

            if (aForm.getLayer().isReadOnly()) {    //todo this is not nice
                formW.getActions().addAction(wFormPopupAction);
            }
            else {
                formW.getActions().addAction(formPopupAction);
                formW.getActions().addAction(formMoveAction);
                formW.getActions().addAction(formEditorAction);
            }
        }

        return formW;
    }


    protected RelationWidget createEdge(final Edge aRel) {
        final RelationWidget connection = new RelationWidget(aRel, this, connectionCenterLayer, connectionLegLayer, errorLinkLayer);

        connection.addAction(createSelectAction());
        connection.addAction(createWidgetHoverAction());

        if (isReadonly()) {
            connection.addCenterAction(getEdgePopupAction(aRel));
        }
        else {
            connection.addCenterAction(edge2XConnectAction);
            connection.addCenterAction(moveEdgeAction);
            connection.addCenterAction(getEdgePopupAction(aRel));
        }
        //TODO connection.addLegAction( legReconnectAction );
        connection.addLegAction( legPopupAction );

        return connection;
    }

    // TODO reuse the non parametric ones
    private WidgetAction getEdgePopupAction(final Edge aEdge) {
        if (isReadonly()) {
            return WidgetMenuProvider.createAction(this,
                    new DisplayPropertiesAction(this));
        }
        else {
            return WidgetMenuProvider.createAction(this,
                    new EdgeDelete(this),
                    new MenuActionErrorAdd(this, aEdge),
                    new MenuActionErrorDel(this, aEdge),
                    new DisplayPropertiesAction(this));
        }
    }



    public ParaModel getParaModel() {
        return paraModel;
    }

//    public PseudoModel getModel() {
//        return paraModel.getPseudoModel();
//    }

// =============================================================================
//
// =============================================================================



    /**
     * Called by the removeNode method to notify that a node is removed from the graph model.
     * The default implementation removes the node widget from its parent widget.
     * @param node the removed node
     * @param widget the removed node widget; null if the node is non-visual
     */
    protected void removeNode(FForm node, LabelWidget widget) {
        if (widget != null) {
            widget.removeFromParent();
        }
    }

    /**
     * Called by the edgeDel method to notify that an edge is removed from the graph model.
     * The default implementation removes the edge widget from its parent widget.
     * @param edge the removed edge
     * @param widget the removed edge widget; null if the edge is non-visual
     */
    protected void removeEdge(Edge edge, RelationWidget widget) {
        if (widget != null) {
            widget.removeFromParent();
        }
    }

    /**
     * Returns location of the widget associated with an object.
     * @param aObj object associated with the widget
     * @return location of the widget (the point object is a copy of the widget's
     *    location object)
     */
    protected Point getLocation(final Object aObj) {
        final Widget widget = findWidget(aObj);
        Err.iAssert(widget != null, "No widget associated with object ", aObj);

        return widget.getLocation();
    }


// =============================================================================
// Layout: location/place (X x Y) <-> position (idx x layeridx)
// =============================================================================

    public WordWidget place2formW(Point aPoint) {
        for (Widget w : mainLayer.getChildren()) {
            Rectangle r = w.getBounds();
            r.setLocation(w.getLocation());
            if (r.contains(aPoint)) return (WordWidget) w;
        }

        return null;
    }

    public Sentence place2sentence(Point aPoint) {
        for (Widget widget : sLayer.getChildren()) { // sentences
            log.finer("  place2sentence: widget=%s, loc=%s, size=%s", widget, widget.getLocation(), widget.getBounds());
            if (contains(widget,aPoint)) {
                Object obj = findObject(widget);
                //log.info("hit sobj=%s", obj);
                if (obj instanceof Sentence) return (Sentence)obj;
            }
        }
        return null;
    }


    /**
     *
     * @param aPoint
     * @return
     */
    public Point anchor2place(final Position aAnchor) {
        throw new UnsupportedOperationException("");
    }

    /**
     *
     * @param aPoint
     * @return
     */
    public Position place2anchor(final Point aPoint) {
        final Sentence s = place2sentence(aPoint);
        log.finer("place2anchor: point=%, s=%s", aPoint, s);
        if (s == null) return null;

        for (LForm form : s.col()) {    // todo faster directly thru sw's formw.
            final Point wLoc = getLocation(form);

            if (aPoint.x < wLoc.x) {
                log.finer("  place2anchor2: .. | form=%s .. ; wloc=%s", form, wLoc);
                return Position.before(form);
            }
        }

        final Position anchor = Position.after(  Cols.last( s.col() ) );

        log.finer("  place2anchor3(final): %s", anchor);

        return anchor;
    }

    protected int place2layer(Point aLoc) {
        return (aLoc.y - cYStart + (ySpace/2)) / ySpace;
    }

    public Point pos2place(int aLayer, int aPos) {
        return new Point(cXStart + xSpace * aPos, layer2y(aLayer));
    }

    /**
     * Vertical position of forms on the specified layer.
     * @param aLayer
     * @return
     */
    public int layer2y(int aLayer) {
        return ySpace * aLayer + cYStart;
    }

    /**
     * Vertical position of forms on the specified layer.
     * @param aLayer
     * @return
     */
    public int layer2y(FormsLayer<?> aLayer) {
        return ySpace * aLayer.getLayerIdx() + cYStart;
    }

    /**
     * Vertical position of the relation's central nodes on the specified layer.
     * @param aLayer
     * @return
     */
    public int layer2relY(int aLayer) {
        return ySpace * aLayer - ySpace/2 + cYStart;
    }

    /**
     * Vertical position of the relation's central nodes on the specified layer.
     * @param aLayer
     * @return
     */
    public int layer2relY(FormsLayer<?> aLayer) {
        return ySpace * aLayer.getLayerIdx() - ySpace/2 + cYStart;
    }

    public Point layerRound(Point aLoc) {
        return new Point(aLoc.x, layer2y(place2layer(aLoc)) );
    }

    public static boolean contains(Widget aWidget, Point aPoint) {
        Point loc = aWidget.getLocation();
        Rectangle bounds = aWidget.getBounds();
        bounds.translate(loc.x, loc.y);

        log.finer("  contains: point %s in w %s (%s)?", aPoint, bounds, bounds.contains(aPoint) );


        return bounds.contains(aPoint);
    }

    /** 
     * Used for debugging.
     * 
     * To print their ids, use:
     * log.info("Known forms %s", Iterables.toString(Id.e2ids(knownForms())));
     * @return 
     */
    private Iterable<FForm> knownForms() {
        return Iterables.filter(getObjects(), (Class<FForm>)(Object)FForm.class);
    }
    
    
}
