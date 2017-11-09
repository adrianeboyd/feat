package org.purl.jh.feat.layered;

import cz.cuni.utkl.czesl.data.layerx.Position;
import org.purl.jh.feat.layered.actions.AnchorAction;
import cz.cuni.utkl.czesl.data.layerl.LForm;
import cz.cuni.utkl.czesl.data.layerl.LLayer;
import cz.cuni.utkl.czesl.data.layerl.Sentence;
import cz.cuni.utkl.czesl.data.layerx.FForm;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.Widget;
import org.purl.jh.util.col.Cols;

public class SentenceWidget extends Widget {
    private final static org.purl.jh.util.Logger log = org.purl.jh.util.Logger.getLogger(SentenceWidget.class);
    
    private final LayeredGraph graph;
    private final Sentence sentence;
    
    private final List<Widget> formWidgets = new ArrayList<>();
    private final MoverHandler moveHandler = new MoverHandler();

    private static class MoverHandler implements MoveStrategy, MoveProvider {
        private SentenceWidget sentenceWidget;
        private Point prevLoc;

        public void movementStarted(Widget aWidget) {
            sentenceWidget = (SentenceWidget) aWidget.getParentWidget();
            //origLoc = widget.getLocation();
            prevLoc = sentenceWidget.getLocation();
        }

        public Point locationSuggested(Widget aWidget, Point aOriginalLocation, Point aSuggestedLocation) {
                final int delta = aSuggestedLocation.x - prevLoc.x;
                prevLoc.x = aSuggestedLocation.x;

                // move the whole sentence
                final Point swLoc = sentenceWidget.getLocation();
                swLoc.move(swLoc.x + delta, swLoc.y);
                sentenceWidget.setPreferredLocation(swLoc);

                // move forms
                for (Widget fw : sentenceWidget.getFormWidgets()) {
                    final Point fwLoc = fw.getLocation();
                    fwLoc.move(fwLoc.x + delta, fwLoc.y);
                    fw.setPreferredLocation(fwLoc);
                }

                return new Point(aSuggestedLocation.x, aOriginalLocation.y);
        }

        public void movementFinished(Widget widget) {
//            final LForm form = (LForm)findObject(widget);
//            final Point newLoc = widget.getLocation();
//            final Anchor anchor = place2anchor(newLoc);
//
//            log.info("MoveProvider.movementFinished: widget=%s, form=%s, anchor=%s",
//                    widget, form, anchor);
//
//            if (anchor == null) return;
//            if (anchor.getForm() == form) return;  // did not move
//            // how about anchor of x = after(x+1)
//
//            getModel().formMove(form, anchor, LayeredGraph.this, newLoc);
        }


        public Point getOriginalLocation(Widget widget) {
            return widget.getPreferredLocation ();
        }
        public void setNewLocation (Widget widget, Point location) {
            //if (place2sentence(location) == null) return;   // to prevent moving into inter-sentential space

            widget.setPreferredLocation(location);

            // todo move forms ???

        }

    }




    public SentenceWidget(final LayeredGraph graph, final Sentence sentence) {
        super(graph);
        this.graph = graph;
        this.sentence = sentence;

        //setLayout(LayoutFactory.createVerticalFlowLayout ());
        setBorder(BorderFactory.createLineBorder());


//        handle = new LabelWidget(scene, "==");
//        handle.setOpaque (true);
//        handle.setBackground(Color.LIGHT_GRAY);
//        //handle.setPr
//        handle.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
//        addChild(handle);
//
//        WidgetAction moveAction = ActionFactory.createMoveAction(moveHandler, moveHandler); // todo share
//        handle.getActions().addAction( moveAction );
//
//        main = new Widget(scene);
//        main.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
//        addChild(main);


        //        LabelWidget switchButton = new LabelWidget (scene, "Click me to switch card.");
        //        switchButton.setOpaque (true);
        //        switchButton.setBackground (Color.LIGHT_GRAY);
        //        switchButton.setBorder (BorderFactory.createBevelBorder (true));
        //        addChild (switchButton);
        //        container = new Widget (scene);
        //        container.setBorder (BorderFactory.createLineBorder ());
        //        addChild (container);
        //
        //        container.setLayout (LayoutFactory.createCardLayout (container));
        //
        //        switchButton.getActions ().addAction (ActionFactory.createSwitchCardAction (container));
    }

    // for some reason this cannot be added to the sentence widget, but must be added to the scene's actions
    public static WidgetAction createMenu(final LayeredGraph graph) {
        final WidgetMenuProvider.DisplayQ displayQ = new WidgetMenuProvider.DisplayQ() {
            public boolean displayQ(Widget aWidget, Point aPoint) {  // unfortunately, the widget is the whole scene, not the most specific one
                final Sentence s = graph.place2sentence(aPoint);
                return s != null && !s.getLayer().isReadOnly();
            }
        };

        if (graph.isReadonly()) {
            return WidgetMenuProvider.createAction(graph, displayQ);
        }
        else {
            return WidgetMenuProvider.createAction(graph, displayQ,
                    new LayeredGraph.FormAddAction(graph),
                    new SentenceSplitAction(graph),
                    new SentenceMergeAction(graph),
                    new SentenceCopyHigherAction(graph),
                    new SentenceSyncFormsAction(graph));
        }
    }


    /**
     * @return
     */
    public List<Widget> getFormWidgets() {
        return formWidgets;
    }


    public abstract static class SentenceAction extends AnchorAction {
        SentenceAction(LayeredGraph aView, String aStr) {
            super(aView, aStr);
        }

        @Override
        public boolean isEnabled(Widget aWidget, Point aLoc, final Position aAnchor) {
            final Sentence s = aAnchor.getForm().getParent();

            return isEnabled(s);
        }

        public boolean isEnabled(Sentence aSentence) {
            return true;
        }

        @Override
        public void actionPerformed(Position aAnchor, Point aPoint) {
            final Sentence s = aAnchor.getForm().getParent();
            actionPerformed(s);
        }

        public abstract void actionPerformed(Sentence aSentence);
    }

    public static class SentenceMergeAction extends SentenceAction {
        SentenceMergeAction(LayeredGraph aView) {
            super(aView, "Merge With Following Sentence");
        }

        /** todo determine anchor for the whole manu once, */
        @Override
        public boolean isEnabled(Sentence aSentence) {
            return aSentence.getLayer().sentenceMergePossible(aSentence);
        }


        @Override
        public void actionPerformed(Sentence aSentence) {
            aSentence.getLayer().sentenceMerge(aSentence, view, null);
        }
    }


    public static class SentenceSplitAction extends AnchorAction {
        SentenceSplitAction(LayeredGraph aView) {
            super(aView, "Split Sentence Here");
        }

        /** todo determine anchor for the whole menu once, */
        @Override
        public boolean isEnabled(Widget aWidget, Point aLoc, final Position aAnchor) {
            final Sentence s = aAnchor.getForm().getParent();
            return s.getLayer().sentenceSplitPossible(aAnchor);
        }

        public void actionPerformed(Position aAnchor, Point aPoint) {
            log.info("SentenceSplitAction: anchor: %s", aAnchor);
            final Sentence s = aAnchor.getForm().getParent();
            s.getLayer().sentenceSplit(aAnchor, view, aPoint);
        }
    }

    public static class SentenceSyncFormsAction extends SentenceAction {
        SentenceSyncFormsAction(LayeredGraph aView) {
            super(aView, "Sync Forms on Higher Layer");
        }

        @Override
        public boolean isEnabled(Sentence s) {
            if (s.getLayer().isReadOnly()) return false;

            for (LForm form : s.col()) {
                // todo there might be a fnc - like simple connection
                if (form.getHigherForms().size() != 1) continue;
                final FForm higherForm = Cols.getFirstElement(form.getHigherForms());
                if (higherForm.getLowerForms().size() != 1) continue;

                return true;
            }
            return false;
        }

        public void actionPerformed(Sentence s) {
            if (s.getLayer().isReadOnly()) return;

            for (LForm form : s.col()) {
                if (form.getHigherForms().size() != 1) continue;
                final FForm higherForm = Cols.getFirstElement(form.getHigherForms());
                if (higherForm.getLowerForms().size() != 1) continue;

                s.getLayer().formEdit(higherForm, form.getToken(), view, view.findWidget(higherForm));
            }
        }
    }



    public static class SentenceCopyHigherAction extends SentenceAction {
        SentenceCopyHigherAction(LayeredGraph aView) {
            super(aView, "Copy Sentence To Higher Layer");
        }

        @Override
        public boolean isEnabled(Sentence aSentence) {
            final LLayer lowerLayer = aSentence.getLayer();
            final LLayer higherLayer = view.getParaModel().getPseudoModel().getLayerAbove(lowerLayer);

            return higherLayer  != null && higherLayer.sentenceCopyFromLowerPossible(aSentence);
        }

        @Override
        public void actionPerformed(Sentence aSentence) {
            final LLayer lowerLayer = aSentence.getLayer();
            final LLayer higherLayer = view.getParaModel().getPseudoModel().getLayerAbove(lowerLayer);
            higherLayer.sentenceCopyFromLower(aSentence, view, null);
            ((LayeredViewTopComponent) SwingUtilities.getAncestorOfClass(LayeredViewTopComponent.class, view.getView())).fullRefresh();
        }
    }


}
