package cz.cuni.utkl.czesl.data.layerl;

import com.google.common.collect.Sets;
import cz.cuni.utkl.czesl.data.layerx.FForm;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.purl.jh.pml.AbstractIdedElement;
import org.purl.jh.pml.Commented;
import org.purl.jh.pml.Layer;
import org.purl.jh.pml.location.Location;
import org.purl.jh.util.col.Cols;

/**
 * Super-Edge connecting forms on adjacent layers.
 *
 * Set of all connected forms has to contain at least one form.
 * Usually, there is one form on the lower layer and one on this layer.
 *
 * @author Jirka
 */
public class Edge extends AbstractIdedElement implements Commented {
    /** Forms on the lower layer. Can be empty. */
    private final Set<FForm> lower = new HashSet<>();      // todo use small set

    /** Forms on this layer. Can be empty. */
    private final Set<LForm> higher = new HashSet<>();         // todo use small set

    /** Errors associated with this layer */  // todo use small set  
    private final Set<Errorr> errors = new HashSet<>();

    /**
     * Any comment an annotator may assign to this form. Can be null.
     */
    protected String comment;


    public Edge(LLayer aLayer, String aId) {
        super(aLayer, aId);
    }

    @Override
    public LPara getParent() {
        return (LPara) super.getParent(); 
    }
    

    @Override
    public LLayer getLayer() {
        return (LLayer) super.getLayer();
    }

    /**
     * Convenience function returning all forms this edge is connected to.
     */
    public Set<? extends FForm> getForms() {
        return Sets.union(lower, higher);
    }    
    /**
     * Link to forms on the lower (i.e. the other) layer.
     * @return
     */
    public Set<FForm> getLower() {
        return lower;
    }

    /**
     * Returns a single form on the lower (i.e. the other) layer.
     * Does not check if there are other forms.
     * @return
     */
    public FForm getOneLower() {
        return lower.iterator().next();
    }
    
    /**
     * Link to forms on this (i.e. the higher) layer.
     * @return
     */
    public Set<LForm> getHigher() {
        return higher;
    }

    /**
     * Returns a single form on this (i.e. the higher) layer.
     * Does not check if there are other forms.
     * @return
     */
    public LForm getOneHigher() {
        return higher.iterator().next();
    }

    /**
     * Link to forms on the lower (i.e. the other) layer.
     * @return
     * @Deprecated use getLower
     */
    @Deprecated
    public Set<FForm> getFrom() {
        return lower;
    }

    /**
     * Link to forms on this (i.e. the higher) layer.
     * @Deprecated
     * @return use getHigher
     */
    @Deprecated
    public Set<LForm> getTo() {
        return higher;
    }


    /**
     * Errors related to this edge. The set should be empty if there is no
     * error.
     *
     * @return errors
     */
    public Set<Errorr> getErrors() {
        return errors;
    }

    // todo return a list with predictable and consistent order
    public List<Errorr> getErrorsList() {
        return new ArrayList<>(getErrors());
    }


    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public Edge setComment(String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Is this an insert edge, i.e. edge connected to one word on this layer and no word on the lower layer.
     */
    public boolean isInsert() {
        return getLower().isEmpty() && getHigher().size() == 1;
    }

    /**
     * Is this a delete edge, i.e. edge connected to one word on the lower layer and no word on this layer.
     */
    public boolean isDelete() {
        return getLower().size() == 1 && getHigher().isEmpty();
    }

    /** 
     * Is this a simple (i.e. 1:1) edge. A simple edge connects one word on the 
     * lower layer with one word on the higher layer.
     */
    public boolean isSimple() {
        return getLower().size() == 1 && getHigher().size() == 1;
    }

    /**
     * Location object identifying a leg within a layer.
     * @return 
     */
    public Location location(FForm aForm) {
        return Location.of(this, "leg:" + aForm.getId().getIdStr());
    }
    
            
    @Override
    public String toString() {
        return super.toString() +
            "; lower=" + Cols.toString(lower) +
            "; higher=" + Cols.toString(higher);
    }

}
