
package cz.cuni.utkl.czesl.data.layerl;

import java.util.HashSet;
import java.util.Set;
import org.purl.jh.pml.AbstractCommentedIdedElement;
import org.purl.jh.pml.location.Location;
import org.purl.jh.util.col.Mapper;

/**
 * Object describing a particular error.
 *
 * Note: Errorr is a syntagmatic object, while ErrorSpec is a paradigmatic unit.
 *
 * @author Jirka dot Hana at gmail dot com
 */
public class Errorr extends AbstractCommentedIdedElement  {
    private String tag;

    public final static ErrorInfo2TagStr cErrorInfo2TagStr = new ErrorInfo2TagStr();
    
    public static class ErrorInfo2TagStr implements Mapper<Errorr, String> {
        @Override
        public String map(Errorr aOrigItem) {
            return aOrigItem.getTag();
        }
    }
    
    /**
     * Links to the sources of error (e.g. used for agreement errors, pointing to subject)
     */
    final private Set<Edge> links = new HashSet<>();  // todo use some cheaper default

    public Errorr(/*@NonNull*/ LLayer aLayer, /*@NonNull*/ String aLocId, /*@NonNull*/ String tag) {
        super(aLayer, aLocId);
        
        this.tag = tag;
    }

    @Override
    public Edge getParent() {
        return (Edge)super.getParent(); 
    }

    
    
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }


    public Set<Edge> getLinks() {
        return links;
    }

    public void addLink(Edge aEdge) {
        links.add(aEdge);
    }

    /**
     * Location object identifying this error within a layer.
     * @return 
     */
    @Override
    public Location location() {
        return Location.of(getParent(), getTag());  
    }

    @Override
    public String toString() {
        return "Error " + tag + " comment:" + getComment();
    }

}
