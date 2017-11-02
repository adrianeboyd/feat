package cz.cuni.utkl.czesl.data.layerl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import cz.cuni.utkl.czesl.data.layerw.WForm;
import cz.cuni.utkl.czesl.data.layerw.WPara;
import cz.cuni.utkl.czesl.data.layerx.*;
import java.util.*;
import org.openide.filesystems.FileObject;
import org.purl.jh.pml.Element;
import org.purl.jh.pml.IdedElement;
import org.purl.jh.pml.Layer;
import org.purl.jh.pml.RefVar;
import org.purl.jh.pml.event.DataListener;
import org.purl.jh.util.col.Cols;
import org.purl.jh.util.col.Lists;
import org.purl.jh.util.col.SmallSet;
import org.purl.jh.util.col.XCols;
import org.purl.jh.util.err.Err;
import org.purl.jh.util.str.Ids;

/**
 * LLayer (Layer 1 and up).
 *
 * @author Jirka
 */
public class LLayer extends FormsLayer<LDoc> {
    private final static org.purl.jh.util.Logger log = org.purl.jh.util.Logger.getLogger(LLayer.class);

// -----------------------------------------------------------------------------
    
    /**
     * Creates a new Layer.
     *
     * @param aId a unique identifier of the layer
     * @todo unique among layers or generally
     */
    public LLayer(FileObject aFile, String aId) {
        super(aFile, aId);
    }

    /**
     * Returns the error tagset connected with this layer.
     * @return
     */
    public ErrorTagset getTagset() {
        return (ErrorTagset) Cols.one(getTagsets().values());
    }

    // todo make lightweight
    @Override
    public Iterable<LForm> getForms() {
        final List<LForm> forms = new ArrayList<>();
        for (LDoc doc : col()) {
            for (LPara para : doc.col()) {
                forms.addAll(para.getFormsList());
            }
        }
        return forms;
    }

    @Override
    public LForm getFirstForm() {
        return col().iterator().next().iterator().next().getForms().iterator().next();
    }
    
    @Override
    public LForm getLastForm() {
        try {
            // works if non of the last elements is empty
            LDoc lastDoc = Cols.last(col());
            LPara lastPara = Cols.last(lastDoc.getParas());
            Sentence lastSent = Cols.last(lastPara.getSentences());
            return Cols.last(lastSent.col());
        }
        catch(IndexOutOfBoundsException ex) {
            // just collect all the forms and return the last one (todo collect from the end and return the first available form)
            return Iterables.getLast(getForms());
        }
    }
    
    // todo eff - improve
//    public LForm getForm(Form aForm, int aOffset) {
//        LForm form = (LForm) aForm;
//        
//        if (aOffset == 0) {
//            return form;
//        }
//        else if (aOffset < 0) {
//            Sentence s = form.getParent();
//            LPara p = s.getParent();
//            LDoc doc = p.getParent();
//            
//            //return find(form, aOffset, Arrays.asList(doc, p, s), Arrays.asList(doc.col(), p.getSentences(), s.col()) );
//            
//            int idx = s.col().indexOf(form);
//            if (idx >= aOffset) {
//                return s.col().get(idx-aOffset);
//            }
//            aOffset -= idx;
//            
//        }
//        else {
//            // todo go directly to the doc/para/s of aForm
//            boolean counting = false;
//            final List<LForm> forms = new ArrayList<LForm>();
//            for (LDoc doc : col()) {
//                for (LPara para : doc.col()) {
//                    for (Sentence s : para.getSentences()) {
//                        for (LForm form : s.getChildren()) {
//                            if (counting) {
//                                
//                            }
//                            else {
//                                if (form == aForm);
//                            }
//                        }
//                    }
//                }
//            }
//            return forms;
//        }
//    }
    
    
    // todo 
//    private class FormIterator implements Iterator<LForm> {
//        Iterator<LForm> it;
//
//        public FormIterator() {
//            this.it = it;
//        }
//        
//        @Override
//        public boolean hasNext() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public LForm next() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//
//        @Override
//        public void remove() {
//            throw new UnsupportedOperationException("Not supported yet.");
//        }
//        
//    }
    
    
    

// -----------------------------------------------------------------------------
// Fields
// -----------------------------------------------------------------------------


    @Override
    public void addRefVar(final RefVar aRefVar) {
        Err.iAssert(aRefVar.getLayer() != null, "Referenced layer cannot be null (%s)",      aRefVar);
        Err.iAssert(getLowerLayer()    == null, "Cannot add more than one lower layer (%s)", aRefVar);

        super.addRefVar(aRefVar);
        setLowerLayer((FormsLayer)aRefVar.getLayer());
    }

// -----------------------------------------------------------------------------
// TODO: Id support functions
// -----------------------------------------------------------------------------

    private int edgeCounter = -1;
    private final String edgeIdPrefix = idPrefix + "e";

    public String createNewEdgeId() {
        if (edgeCounter == -1) {
            edgeCounter = calculateMaxCounter(edgeIdPrefix, id2element.values());
        }

        String newid = idPrefix + "e" + (++edgeCounter);
        return super.getUniqueId(newid);
    }

    /** TODO generalize, optimize */
    private int calculateMaxCounter(final String aIdPrefix, final Iterable<IdedElement> aExistingElements) {
        int maxC = -1;

        for (IdedElement e : aExistingElements) {
            if (e instanceof Edge) {
                final String tmpId = e.getId().getIdStr();
                if (tmpId.startsWith(aIdPrefix)) {
                    int c = Ids.getFinalCounter(tmpId);
                    if (c > maxC) maxC = c;
                }
            }
        }
        return maxC;
    }

// =============================================================================
// MVC modifications
// =============================================================================

    @Override
    public boolean formAdd(String aStr, Position aAnchor, DataListener aSrcView, Object aSrcInfo) {
        if (readOnly) return false;
        
        final Sentence s = aAnchor.getForm().getParent();
        final LPara p    = s.getParent();
        final String formId  = getUniqueId(p, "w", 1); //  todo better init counter

        final LForm newForm = new LForm(this, formId, FForm.Type.normal, aStr);
        newForm.setParent(s);

        return formAdd(newForm, aAnchor, aSrcView, aSrcInfo);
    }

    /**
     *
     * @param newForm fully populated form (the parent must be set). Other objects will be connected to it, but not vice versa.
     * @param aAnchor
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean formAdd(final LForm newForm, final Position aAnchor, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        addIdedElement(newForm);

        addToList(newForm.getParent().col(), aAnchor, newForm);

        // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cFormAdd, aSrcView, aSrcInfo);
        event.form = newForm;

        fireEvents(event);
        return true;
    }
    
    @Override
    public boolean formDel(FForm aForm, DataListener aSrcView, Object aSrcInfo) {
        if (readOnly) return false;

        final LForm form = (LForm) aForm;
        final Sentence s = form.getParent();
        final LPara para = s.getParent();

        final Position anchor = Position.of(form);       // used by undo to place it back

        // --- delete legs first ---
        final List<Edge> edges = new ArrayList<>();     // todo support for merging lists into ?iterable
        edges.addAll(form.getHigher());
        edges.addAll(form.getLower());

        for (Edge e : edges) {
            e.getLayer().legDel(e, form, null, null);
        }

        // --- delete the form (or the sentence if it contains a single form) ---
        if (s.size() == 1) {
            //s.getChildren().remove(form);         keep s and forms connected
            removeIdedElement(form);

            // remove s2 from para/layer
            s.getParent().getSentences().remove(s);
            removeIdedElement(s);

            final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cSentenceDel, aSrcView, aSrcInfo);
            event.sentence1 = s;
            fireEvents(event);
        }
        else {
            form.getParent().col().remove(form);

            removeIdedElement(form);

            final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cFormDel, aSrcView, aSrcInfo);
            event.anchor = anchor;
            event.form = aForm;
            fireEvents(event);
        }

        return true;
    }


    /**
     * Form must stay within the same paragraph.
     * 
     * Note: the moved form is always moved into the sentence of the anchor
     * @param movedForm
     * @param aAnchor target anchor. The aMovedForm is moved into the anchor's form's sentence.
     * @param aApproxPos
     * @param aSrcView
     * @param aSrcInfo
     */
    @Override
    public boolean formMove(final FForm aMovedForm, final Position aAnchor, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        log.info("formMoveX: aMovedForm=%s, aAnchor=%s", aMovedForm, aAnchor);
        final LForm movedForm      = (LForm) aMovedForm;
        final Sentence newSentence = aAnchor.getForm().getParent();
        final LPara p              = newSentence.getParent();

        final Position oldAnchor = Position.of(movedForm);

        if (log.info()) log.info("  s=%s", Cols.toString(newSentence.col()) );

        // remove from the old sentence
        final Sentence origSentence = movedForm.getParent();
        origSentence.col().remove(movedForm);


        // --- add to the new sentence (may be the same sentence) ---
        movedForm.setParent(newSentence);
        addToList(newSentence.col(), aAnchor, movedForm);

        //fillForms(p);
        if (log.info()) log.info("  filled forms=%s" + Cols.toString(newSentence.col()));

        // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cFormMove, aSrcView, aSrcInfo);
        event.form = movedForm;
        event.old =  oldAnchor;
        fireEvents(event);

        // remove sentence if empty, todo - single transaction
        if (origSentence.isEmpty()) {
            origSentence.getParent().getSentences().remove(origSentence);
            origSentence.getLayer().removeIdedElement(origSentence);

            final ChangeEvent<LLayer> event2 = new ChangeEvent<>(this, ChangeEvent.cSentenceDel, aSrcView, aSrcInfo);
            event2.sentence1 = origSentence;
            fireEvents(event2);
        }

        log.info("--- end moveForm (%s) ---", movedForm);
        return true;
    }


    @Override
    public boolean formMove(final List<? extends FForm> aMovedForms, final Position aAnchor, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        if (!allHere(aMovedForms)) return false; 
        final List<LForm> movedForms = (List<LForm>)aMovedForms;

        log.info("formsMove: aMovedForms=%s, aAnchor=%s", movedForms, aAnchor);
        final Sentence targetSentence = aAnchor.getForm().getParent();
        final LPara p      = targetSentence.getParent();

        if (log.info()) log.info("  target s=%s", Cols.toString(targetSentence.col()) );

        final LForm marker = new LForm(this, "", FForm.Type.normal, "");
        addToList(targetSentence.col(), aAnchor, marker);

        // --- remove forms from their original place (possibly in different sentences) ---
        final Set<Sentence> origSentences = XCols.newHashSet();
        for (LForm form : movedForms) {
            final Sentence origSentence = form.getParent();
            origSentence.col().remove(form);
            origSentences.add(origSentence);

            form.setParent(targetSentence);
        }
        addToList(targetSentence.col(), marker, movedForms);

        //fillForms(p);
        if (log.info()) log.info("  filled forms=%s" + Cols.toString(targetSentence.col()));

        // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cFormsMove, aSrcView, aSrcInfo);
        event.forms = movedForms;
        fireEvents(event);

        // remove sentence if empty, todo - single transaction
        for (Sentence origSentence : origSentences) {
            if (origSentence.isEmpty()) {
                origSentence.getParent().getSentences().remove(origSentence);
                origSentence.getLayer().removeIdedElement(origSentence);

                final ChangeEvent<LLayer> event2 = new ChangeEvent<>(this, ChangeEvent.cSentenceDel, aSrcView, aSrcInfo);
                event2.sentence1 = origSentence;
                fireEvents(event2);
            }
        }

        return true;
        // todo aggregate undo (composite? brackets?)
//        if (isReadOnly(aMovedForm)) return false;
//
//        log.info("formMove: aMovedForm=%s, aAnchor=%s", aMovedForm, aAnchor);
//        final Sentence newSentence = aAnchor.getForm().getParent();
//        final LPara p      = newSentence.getParent();
//
//        final Position oldAnchor = Position.of(aMovedForm);
//
//        if (log.info()) log.info("  s=%s", Cols.toString(newSentence.getChildren()) );
//
//        // --- move forms from their old sentence to the new one ---
//        for (LForm form : aMovedForms) {
//            final Sentence origSentence = form.getParent();
//            origSentence.getChildren().remove(form);
//
//            form.setParent(newSentence);
//        }
//        addToList(newSentence.getChildren(), aAnchor, aMovedForms);
//
//
//        // need to reconstruct the sublist, because concurrent modification broke them
//        // todo this is horrible it must be done in a more robust way
//        fillForms();
//        if (log.info()) log.info("  filled forms=%s" + Cols.toString(newSentence.getChildren()));
//
//        // fire updates
//        final ChangeEvent event = new ChangeEvent(this, ChangeEvent.cFormMove, aSrcView, aSrcInfo);
//        event.form = aMovedForm;
//        event.old =  oldAnchor;
//        fireEvents(event);
//
//        // remove sentence if empty, todo - single transaction
//        if (origSentence.getChildren().isEmpty()) {
//            origSentence.getParent().getSentences().remove(origSentence);
//            origSentence.getLayer().removeIdedElement(origSentence);
//
//            final ChangeEvent event2 = new ChangeEvent(this, ChangeEvent.cSentenceDel, aSrcView, aSrcInfo);
//            event2.sentence1 = origSentence;
//            fireEvents(event2);
//        }
//
//        log.info("--- end moveForm (%s) ---", aMovedForm);
//        return true;

    }


// -----------------------------------------------------------------------------
// Modifications - edges
// -----------------------------------------------------------------------------

    public boolean edgeAdd(final FForm aLowerForm, final LForm aHigherForm, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        if (isDtForm(aLowerForm)) return false;

        // TODO nodes not empty
        // TODO all aLowerNodes nodes on the same layer
        // TODO all aHigherNodes nodes on the same layer
        // TODO the layers are adjacent
        //Cols.getFirstElement(aHigherNodes).getParent()
        LPara para   = aHigherForm.getAncestor(LPara.class);
        LLayer layer = aHigherForm.getAncestor(LLayer.class);

        String edgeId = layer.createNewEdgeId();

        Edge edge = new Edge(layer, edgeId);
        edge.setParent(para);
        para.add(edge);

        edge.getLower().add(aLowerForm);
        edge.getHigher().add(aHigherForm);

        aLowerForm.getHigher().add(edge);
        aHigherForm.getLower().add(edge);

     // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cEdgeAdd, aSrcView, aSrcInfo);
        event.edge = edge;

        fireEvents(event);
        return true;
    }

    /**
     *
     * New ided object created.
     *
     * @param aLowerForms
     * @param aHigherForms
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean edgeAdd(final Collection<FForm> aLowerForms, final Collection<LForm> aHigherForms, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        Err.iAssert(aLowerForms.size() + aHigherForms.size() > 0, "Nullary edges are not allowed.");
        
        final LPara  para  = getHigherPara(aLowerForms, aHigherForms);

        for (FForm form : aLowerForms) {
            if (isDtForm(form)) return false;
        }


        // TODO the layers are adjacent
        //Cols.getFirstElement(aHigherNodes).getParent()

//        for (LForm form : aHigherForms) {
//            Err.iAssert( form.getLayer() == layer, "All higher forms must be on the same layer");
//        }
//        for (Form form : aLowerForms) {
//            Err.iAssert( form.getLayer() == lowerLayer, "All lower forms must be on the same layer");
//        }

        final String edgeId = createNewEdgeId();

        final Edge edge = new Edge(this, edgeId);
        edge.setParent(para);
        para.add(edge);

        edge.getLower().addAll(aLowerForms);
        edge.getHigher().addAll(aHigherForms);

        for (FForm form : aLowerForms) {
            form.getHigher().add(edge);
        }
        for (LForm form : aHigherForms) {
            form.getLower().add(edge);
        }

        // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cEdgeAdd, aSrcView, aSrcInfo);
        event.edge = edge;

        fireEvents(event);
        return true;
    }

    /** Trial - for undo mngr only */
    public boolean edgeAdd(final Edge edge, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        edge.getParent().add(edge);

        for (FForm form : edge.getLower()) {
            form.getHigher().add(edge);
        }
        for (LForm form : edge.getHigher()) {
            form.getLower().add(edge);
        }

        // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cEdgeAdd, aSrcView, aSrcInfo);
        event.edge = edge;

        fireEvents(event);
        return true;
    }


    /**
     * Gets a paragraph corresponding to the higher form or lower forms, if there are no higher forms.
     * <p>
     * All the higher forms should be in the same paragraph, and all the lower 
     * forms should be in the same paragraph; the two paragraphs should match and 
     * the higher paragraph should be at this layer. None of this is checked.
     * 
     * @param aLowerForms
     * @param aHigherForms
     * @return 
     */
    private LPara getHigherPara(final Collection<FForm> aLowerForms, final Collection<LForm> aHigherForms) {
        final LForm higherForm = aHigherForms.iterator().next();

        // --- get the paragraph at this layer ---
        if (higherForm != null) {
            return higherForm.getAncestor(LPara.class);
        }
        else {
            final FForm lowerForm = aLowerForms.iterator().next();
            final Para lowerPara = lowerForm.getAncestor(LPara.class);

            return getParaForLowerPara(lowerPara);
        }
    }

    /**
     * Adds delete unary relation (relation having no members on the higher layer and exactly one at the lower w-layer).
     *
     * @param aLowerForm
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean edgeAddRemoveWord(final WForm aLowerForm, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

//        final FormsLayer<?> lowerLayer = (FormsLayer<?>) aLowerForm.getLayer();
//        final LLayer             layer = (LLayer) getLayerAbove( lowerLayer );

        if (isDtForm(aLowerForm)) return false;

        WPara lowerPara = aLowerForm.getAncestor(WPara.class);
        LPara      para = getParaForLowerPara(lowerPara);


        String edgeId = createNewEdgeId();

        Edge edge = new Edge(this, edgeId);
        edge.setParent(para);
        para.add(edge);

        edge.getLower().add(aLowerForm);

        aLowerForm.getHigher().add(edge);

     // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cEdgeAdd, aSrcView, aSrcInfo);
        event.edge = edge;

        fireEvents(event);
        return true;
    }

    /**
     * Adds insert unary relation (relation having no members on the lower layer and exactly one at the higher).
     * @param aHigherForm
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean edgeAddInsertWord(final LForm aHigherForm, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        // TODO nodes not empty
        // TODO all aLowerNodes nodes on the same layer
        // TODO all aHigherNodes nodes on the same layer
        // TODO the layers are adjacent
        //Cols.getFirstElement(aHigherNodes).getParent()
        LPara para   = aHigherForm.getAncestor(LPara.class);
        LLayer layer = aHigherForm.getAncestor(LLayer.class);

        String edgeId = layer.createNewEdgeId();

        Edge edge = new Edge(layer, edgeId);
        edge.setParent(para);
        para.add(edge);

        edge.getHigher().add(aHigherForm);

        aHigherForm.getLower().add(edge);

     // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cEdgeAdd, aSrcView, aSrcInfo);
        event.edge = edge;

        fireEvents(event);
        return true;
    }

    public boolean edgeDel(final Edge aEdge, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;


        log.info("Removing edge = %s", aEdge);

        final LPara para = aEdge.getParent();

        // remove all error links ending here (not aEdge's errorlinks!!)
        for (Edge e : para.getEdges()) {
            if (e.getErrors().isEmpty()) continue;

            for (Errorr error : e.getErrors()) {
                if ( !error.getLinks().isEmpty() && error.getLinks().contains(aEdge)) {
                    errorLinkDel(error, aEdge, aSrcView, aSrcInfo); // todo induced change => drop src/info???
                }
            }
        }


        para.getEdges().remove(aEdge);

        // remove references to this edge from all forms
        for (FForm form : aEdge.getLower()) {
            form.getHigher().remove(aEdge);
            form.getLower() .remove(aEdge);
        }
        for (FForm form : aEdge.getHigher()) {
            form.getHigher().remove(aEdge);
            form.getLower() .remove(aEdge);
        }

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cEdgeDel, aSrcView, aSrcInfo);
        event.edge = aEdge;

        fireEvents(event);      // todo undo does not record error's attached to the edge
        return true;
    }

    /**
     *
     *
     * No objects introduced.
     *
     * @param aEdge
     * @param aForm
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean legAdd(final Edge aEdge, final FForm aForm, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        if (isDtForm(aForm)) return false;

        final Layer<?> formlayer = aForm.getLayer();
        final Layer<?> edgelayer = aEdge.getLayer();

        if (formlayer == edgelayer) {
            aEdge.getHigher()   .add((LForm)aForm);
            aForm.getLower().add(aEdge);
        }
        else {
            aEdge.getLower()  .add(aForm);
            aForm.getHigher().add(aEdge);
        }


        // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cLegAdd, aSrcView, aSrcInfo);
        event.form = aForm;
        event.edge = aEdge;

        fireEvents(event);
        return true;
    }


    public boolean legDel(final Edge aEdge, final FForm aForm, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;
        Preconditions.checkArgument(aEdge.getLayer() == this, "Wrong layer");

        log.info("Removing leg. Edge = %s; form=%s", aEdge, aForm);
        //final Layer<?> layer = aForm.getLayer();

        if (aEdge.getLower().size() + aEdge.getHigher().size() == 1) {
            LPara para = aEdge.getParent();
            para.getEdges().remove(aEdge);

            aForm.getHigher().remove(aEdge);
            aForm.getLower() .remove(aEdge);

            final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cEdgeDel, aSrcView, aSrcInfo);
            event.edge = aEdge;

            fireEvents(event);
        }
        else {
            aEdge.getLower().remove(aForm);
            aEdge.getHigher().remove(aForm);

            aForm.getHigher().remove(aEdge);
            aForm.getLower() .remove(aEdge);

            log.info("   -> Edge = %s; form=%s", aEdge, aForm);

            // fire updates
            final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cLegDel, aSrcView, aSrcInfo);
            event.form = aForm;
            event.edge = aEdge;

            fireEvents(event);
        }
        return true;
    }

    /**
     * Any change to the edge's properties except error and legs (currently only comment).
     *
     * TODO Currently requires write thru. */
    public boolean edgeChange(final Edge aEdge,  final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;


        // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cEdgeChange, aSrcView, aSrcInfo);
        event.edge = aEdge;

        fireEvents(event);
        return true;
    }

// -----------------------------------------------------------------------------
// Modifications - edges.errors
// -----------------------------------------------------------------------------

    public boolean errorAdd(final Edge aEdge, final DataListener aSrcView, final Object aSrcInfo) {
        return errorAdd("?", aEdge, aSrcView, aSrcInfo);
    }

    /**
     * Add and error with a specified tag to the specified edge.
     *
     * @param aTag tag of the error
     * @param aEdge edge to add the error to
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean errorAdd(final String aTag, final Edge aEdge, final DataListener aSrcView, final Object aSrcInfo) {
        final String errorId  = getUniqueId(aEdge, "r", 1); //  todo better init counter

        final Errorr error = new Errorr(this, errorId, aTag);
        error.setParent(aEdge);

        return errorAdd(error, aSrcView, aSrcInfo);
    }

    /**
     * Add a complete error object.
     * @param aError the error to add; its parent edge must be set.
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean errorAdd(final Errorr aError, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        aError.getParent().getErrors().add(aError);

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cErrorAdd, aSrcView, aSrcInfo);
        event.error = aError;
        fireEvents(event);

        return true;
    }


    public boolean errorDel(final Errorr aError, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        aError.getParent().getErrors().remove(aError);

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cErrorDel, aSrcView, aSrcInfo);
        event.error = aError;

        fireEvents(event);
        return true;
    }



    /**
     * Change of error tag.
     *
     * @param aErrorInfo
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean errorTagChange(final Errorr aErrorInfo, final String aNewTag, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;


        aErrorInfo.setTag(aNewTag);

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cErrorAttrChange, aSrcView, aSrcInfo);
        event.error = aErrorInfo;

        fireEvents(event);
        return true;
    }

    /**
     * Any change to the error attributes except the tag and links.
     * TODO currently aErrorInfo is already modified - not very clean, use "attr name", "value"?
     *
     * @param aErrorInfo
     * @param aSrcView
     * @param aSrcInfo
     * @return
     */
    public boolean errorAttrChange(final Errorr aErrorInfo, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;


        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cErrorAttrChange, aSrcView, aSrcInfo);
        event.error = aErrorInfo;

        fireEvents(event);
        return true;
    }

    public boolean errorLinkAdd(final Errorr aSrcErrorInfo, final Edge aTargetEdge, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

//        if (aSrcErrorInfo.getTag().getMaxLinks() == 0 ||
//            aSrcErrorInfo.getLinks().contains(aTargetEdge) ) return false;

        aSrcErrorInfo.addLink(aTargetEdge);

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cErrorLinkAdd, aSrcView, aSrcInfo);
        event.error = aSrcErrorInfo;
        event.edge = aTargetEdge;

        fireEvents(event);

        return true;
    }


    public boolean errorLinkDel(Errorr aSrcErrorInfo, Edge aTargetEdge, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;


        aSrcErrorInfo.getLinks().remove(aTargetEdge);

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cErrorLinkDel, aSrcView, aSrcInfo);
        event.error = aSrcErrorInfo;
        event.edge = aTargetEdge;

        fireEvents(event);

        return true;
    }

//    public boolean propertyChange(final Element aElement, final String aProperty, final DataListener aSrcView, final Object aSrcInfo) {
//        if (aElement instanceof Form) {
//            log.info("propertyChange - form");
//            //if (aProperty.equals("comment"))
//        }
//        else if (aElement instanceof Edge) {
//            // comment, add/remove error info
//            log.info("propertyChange - edge");
//        }
//        else if (aElement instanceof ErrorInfo) {
//
//        }
//
//        final ChangeEvent event = new ChangeEvent(this, ChangeEvent.cPropertyChange, aSrcView, aSrcInfo);
//        event.element = aElement;
//        event.property = aProperty;
//
//        fireEvents(event);
//        return true;
//    }

    public boolean addLayer() {
        throw new UnsupportedOperationException("Not supported yet - addLayer.");
    }


// -----------------------------------------------------------------------------
// Modifications - sentence
// -----------------------------------------------------------------------------

    // todo unfinished, experimental, not used yet
    public boolean sentenceAdd(final Sentence aNewsentence, final Sentence aPrevSentence, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;

        final LLayer layer = aNewsentence.getLayer();

        layer.addIdedElement(aNewsentence);

        Lists.addAfter(aPrevSentence.getParent().getSentences(), aPrevSentence, aNewsentence);

        //fillForms(aNewsentence.getParent());

        // fire updates
        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cSentenceAdd, aSrcView, aSrcInfo);
        event.sentence1 = aNewsentence;

        fireEvents(event);
        return true;
    }

    /** todo - experimental. Trying to move enabling code here, but some code is then executed twice (e.g. sentences.indexOf()*/
    public boolean sentenceMergePossible(final Sentence aSentence1) {
        if (readOnly) return false;

        final List<Sentence> sentences = aSentence1.getParent().getSentences();
        final int s1idx = sentences.indexOf(aSentence1);
        return s1idx + 1 < sentences.size();
    }

    /**
     * Merge the specified sentence with the following sentence.
     *
     * @param aSentence1 sentence to merge with the following sentence. Cannot be
     * paragraph final. This object remains valid.
     *
     * @param aSrcView
     * @param aSrcInfo
     * @return false if the sentence is not merged.
     */
    public boolean sentenceMerge(final Sentence aSentence1, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;


        final List<Sentence> sentences = aSentence1.getParent().getSentences();
        final int s1idx = sentences.indexOf(aSentence1);
        if ( assertE(s1idx != -1, "Sentence %s does not exist.", "sentenceMerge", aSentence1) ) return false;
        if ( assertW(s1idx + 1 < sentences.size(), "Sentence %s is para-final.", "sentenceMerge", aSentence1) ) return false;

        final Sentence s2 = sentences.get(s1idx+1);

        // Move forms from s2 to s1
        aSentence1.addAll(s2.col());
        for(LForm form : s2.col()) {
            form.setParent(aSentence1);
        }

        // remove s2 from para/layer
        s2.getParent().getSentences().remove(s2);
        s2.getLayer().removeIdedElement(s2);

        //fillForms();    // is this needed?

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cSentenceMerge, aSrcView, aSrcInfo);
        event.sentence1 = aSentence1;
        event.sentence2 = s2;

        fireEvents(event);
        return true;
    }

    /**
     * todo consider returning sentenceSplitIdx and if -1 returned then it is impossible
     * @param aAnchor
     * @return
     */
    public boolean sentenceSplitPossible(final Position aAnchor) {
        if (readOnly) return false;

        final Sentence s1   = aAnchor.getForm().getParent();

        int idx = s1.col().indexOf(aAnchor.getForm());
        if (aAnchor.isAfterForm()) idx++;

        return 0 < idx && idx < s1.size();
    }

    public boolean sentenceSplit(final Position aAnchor, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly) return false;
        log.info("sentenceSplit: anchor=%s", aAnchor);

        final Sentence s1   = aAnchor.getForm().getParent();
        final LPara p      = s1.getParent();


        // Spliting point: get the index of the first form to be in the second sentence
        // before (.. | a ..): anchor's f in s2; after (.. a | ..): anchor's f in s1
        int idx = s1.col().indexOf(aAnchor.getForm());
        if (aAnchor.isAfterForm()) idx++;

        if ( assertW(idx < s1.size(), "Cannot create empty sentence (%d)", "sentenceSplit", idx) ) return false;
        if ( assertW(0 < idx, "Cannot create empty sentence (%d)", "sentenceSplit", idx) ) return false;

        // Create new empty sentence
        final String s2Id = getUniqueId(p, "s", 1); //  todo better init counter
        final Sentence s2 = new Sentence(this, s2Id);
        Lists.addAfter(p.getSentences(), s1, s2);
        s2.setParent(p);
        addIdedElement(s2);

        // move relevant forms from s1 to s2
        final List<LForm> s2forms = s1.col().subList(idx, s1.size());
        s2.addAll(s2forms);
        for (LForm form : s2forms) {
            form.setParent(s2);
        }
        s2forms.clear();

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cSentenceSplit, aSrcView, aSrcInfo);
        event.sentence1 = s1;
        event.sentence2 = s2;

        fireEvents(event);
        return true;
    }

    /**
     * Finds the place on the higher layer for a new sentence corresponding to a
     * sentence on the lower layer.
     *
     * @param aL1S lower-layer sentence
     * @param aL2Ss list of higher-layer sentences
     * @return
     */
    private int findPlace(final Sentence aL1S, final List<Sentence> aL2Ss) {
        if (aL2Ss.isEmpty()) return 0;

        final LForm form = Cols.last(aL1S.col());
        final LPara para = aL1S.getParent();

        for (int i= 0; i < aL2Ss.size(); i++) {  // todo go from size to 0??
            // finds some lower form
            final FForm l1form = lowerForm(aL2Ss.get(i).col());

            // skips unconnected sentences;
            if (l1form == null) continue;

            // relative position of form and lowerForm
            if (para.before(form, l1form) ) return i;
        }

        return aL2Ss.size();
    }

    /**
     * Finds some form on lower level corresponding to a list of forms
     *  considers only one form of the sentence (usually the last one).
     *  We could consider all or majority, but that would be too costly and in most actual cases would give the same result
     *
     * @return null if the list of forms is not connected to the lower layer.
     */
    private FForm lowerForm(final List<LForm> aForms) {
        for (int i = aForms.size()-1; i >= 0; i--) {
            final LForm l2form = aForms.get(i);
            for (FForm l1form : l2form.getLowerForms()) {
                if (l1form != null) return l1form;
            }
        }
        return null;
    }

    public boolean sentenceCopyFromLowerPossible(final Sentence aS1) {
        if (readOnly || aS1.isEmpty()) return false;
        if (!noEdgeHigher(aS1) ) return false;       // a new sentence is added only if S1 is not linking to anything on the higher layer yet.

        final LPara p      = aS1.getParent();
        final LPara p2     = getParaForLowerPara(p);
        if (p2 == null) return false;       // no higher para

        return true;
    }

    /** 
     * Adds a copy of a sentence from the lower layer 
     * @param aS1 sentence on the lower layer that should be copied to this one
     */
    public boolean sentenceCopyFromLower(final Sentence aS1, final DataListener aSrcView, final Object aSrcInfo) {
        if (readOnly || aS1.isEmpty()) return false;
        if (!noEdgeHigher(aS1) ) return false;       // a new sentence is added only if S1 is not linking to anything on the higher layer yet.

        final LPara p1     = aS1.getParent();
        final LPara p2     = getParaForLowerPara(p1);
        if (p2 == null) return false;       // no higher layer/para

        final String sid  = getUniqueId(p2, "s", 1);
        final Sentence s2 = new Sentence(this, sid);

        final int idx2 = findPlace(aS1, p2.getSentences());
        p2.getSentences().add(idx2, s2);
        //p2.getSentences().add(s2);

        s2.setParent(p2);
        addIdedElement(s2);

        for (LForm form1 : aS1.col()) {
            final String wid = getUniqueId(p2, "w", 1); //  todo better init counter

            final LForm form2 = new LForm(this, wid, form1.getType(), form1.getToken());
            form2.setParent(s2);
            s2.col().add(form2);
            addIdedElement(form2);

            //formAdd(wid, null, aSrcView, aSrcInfo);
            //edgeAdd(form, form2, aSrcView, null);

            // todo edges
            String eid = createNewEdgeId();
            Edge edge = new Edge(this, eid);
            edge.setParent(p2);
            p2.add(edge);

            edge.getLower().add(form1);
            edge.getHigher().add(form2);

            form1.getHigher().add(edge);
            form2.getLower().add(edge);
        }


        //fillForms(p2);

        final ChangeEvent<LLayer> event = new ChangeEvent<>(this, ChangeEvent.cSentenceCopy, aSrcView, aSrcInfo);
        event.sentence1 = aS1;
        event.sentence2 = s2;
        fireEvents(event);

        return true;
    }



    /**
     * Note: The lower layer must contain sentence, i.e. it must be LLayer.
     * @param aS2
     * @return
     */
    private Set<Sentence> getLowerSentences(final Sentence aS2) {
        Err.iAssert(aS2.getLayer().getLowerLayer() instanceof LLayer, "No sentences to look for on lower layer (%s)", aS2);
        Set<Sentence> s1s = new SmallSet<>();
        for (LForm f2 : aS2.col()) {
            for (Edge e : f2.getLower()) {
                for (FForm f1 : e.getLower()) {
                    s1s.add((Sentence)f1.getParent());
                }
            }

        }

        return s1s;
    }

    // todo optimize
    public int findPlaceForS2(final Sentence aS1, final LPara aP2) {
        // todo check the most probable position first
        final int anchor1idx = indexOf(aS1);

        final List<Sentence> s1s = aS1.getParent().getSentences();
        final List<Sentence> s2s = aP2.getSentences();

        final int size1 = s1s.size();

        int start=0;
//        for (int i = 0; i < s2s.size(); i++) {
//            Sentence s2 = s2s.get(i);
//            Set<Sentence> s1 = getLowerSentences(s2);
//
//            for (.,/?..
//                    .
//                    ??.aP2l;.aP2;l.
//                    Sentence s : s1) {
//                .
//            }
//
//        }
//            todo
//
//        int s1idx =


        return -1;
    }

    /**
     * Returns the index of a sentence within its paragraph.
     * @param aS
     * @return
     */
    public int indexOf(final Sentence aS) {
        return aS.getParent().getSentences().indexOf(aS);
    }

    public <T> int indexOf(final List<T>  aList, final int aSize, final T aElement, final int aStart) {
        return aList.subList(aStart, aSize).indexOf(aElement);
    }
// =============================================================================
// Support for views
// =============================================================================


//        undoManager.addEdit(new AbstractUndoableEdit() {
//            public void undo() throws CannotUndoException {
//                super.undo();
//                formDel(newForm, null, null);
//            }
//            public void redo() throws CannotUndoException {
//                super.redo();
//                formAdd(aStr, aAnchor, aSrcView, aSrcInfo);
//            }
//        });


//    }


    public static boolean isDtForm(final FForm aForm) {
        return aForm instanceof WForm && ((WForm)aForm).getType().dt();
    }

    public static boolean noEdgeHigher(final Sentence aS) {
        for (LForm f : aS.col()) {
            if (!f.getHigher().isEmpty()) return false;
        }

        return true;
    }



     protected static void addToList(final List<LForm> aList, final Position aPosition, final LForm aEl) {
        int idx = aList.indexOf(aPosition.getForm());
        Err.iAssert(idx != -1, "Cannot find position %s\n%s", aPosition, aList);

        if (aPosition.isAfterForm()) {
            idx++;
        }

        if (idx < aList.size()) {
            //log.info("adding form to " + idx);
            aList.add(idx, aEl);
        } else {
            //log.info("adding form to the end of sentence" + idx);
            aList.add(aEl);
        }
        //log.info("  added=" + Cols.toString(newSentence.getChildren()));
    }

     protected static void addToList(final List<LForm> aList, final LForm aMarker, final List<LForm> aEls) {
        final int idx = aList.indexOf(aMarker);
        Err.iAssert(idx != -1, "Cannot find marker %s\n%s", aMarker, aList);
        aList.remove(idx);

        if (idx < aList.size() - 1) {
            //log.info("adding form to " + idx);
            aList.addAll(idx, aEls);
        } else {
            //log.info("adding form to the end of sentence" + idx);
            aList.addAll(aEls);
        }
        //log.info("  added=" + Cols.toString(newSentence.getChildren()));
    }

     protected static void addToList(final List<LForm> aList, final Position aPosition, final List<LForm> aEls) {
        int idx = aList.indexOf(aPosition.getForm());
        Err.iAssert(idx != -1, "Cannot find position %s\n%s", aPosition, aList);

        if (aPosition.isAfterForm()) {
            idx++;
        }

        if (idx < aList.size()) {
            //log.info("adding form to " + idx);
            aList.addAll(idx, aEls);
        } else {
            //log.info("adding form to the end of sentence" + idx);
            aList.addAll(aEls);
        }
        //log.info("  added=" + Cols.toString(newSentence.getChildren()));
    }

    private boolean assertW(boolean aTest, String aFormat, Object ... aParams) {
        if ( aTest ) return false;
        log.info("Warning: [Model.%s] " + aFormat, aParams);
        return true;
   }

    private boolean assertE(boolean aTest, String aFormat, Object ... aParams) {
        if ( aTest ) return false;

        log.info("Error: [Model.%s] " + aFormat, aParams);
        return true;
   }

// =============================================================================    
// Querries about links etc.
// =============================================================================    
    
    /**
     * Returns a paragraph of a particular element.
     * @param aElement
     * @return 
     */
    public Para getPara(Element aElement) {
        return aElement.getAncestor(Para.class);
    }

    /**
     * Find the corresponding paragraph to a paragraph from a lower level.
     *
     * This means, finds a paragraph at this layer with a link to the
     * specified paragraph on the lower layer. 
     *
     * @param aPara paragraph at a lower layer to find the higher counterpart for.
     * @return the corresponding higher paragraph, or null if there is none.
     */
    private LPara getParaForLowerPara(final Para aPara) {
        final Doc lowerDoc = aPara.getParent();

        for(LDoc doc : col() ) {
            if (doc.getLowerDoc() == lowerDoc) {
                for(LPara para : doc.getParas() ) {
                    if (para.getLowerPara() == aPara) return para;
                }
            }
        }

        return null;

    }
    
    /**
     * Not very effective. Calculate the up-pointing links for all elements (possibly lazily).
     * @param aPara
     * @return 
     */
    private Para getTopPara(final Para aPara) {
        Para cur = aPara;
        while (true) {
            LPara next = getParaForLowerPara(cur);
            if (next == null) return cur;
            cur = next;
        }
    }
    
    /**
     * @param aPara 
     * @todo not very effective, redesign completely.
     * Todo 
     */
//    private void fillForms(Para aPara) {
//        final Para topPara = getTopPara(aPara);
//        final ParaModel paraModel = getParaModel(topParas.indexOf(topPara));
//        paraModel.fillForms(aPara);
//    }
    
    
    
    

}

