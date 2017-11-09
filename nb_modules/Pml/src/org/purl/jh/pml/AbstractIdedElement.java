package org.purl.jh.pml;

import org.purl.jh.pml.location.Location;

/**
 * Superclass of elements with a unique immutable id.
 * Equals, hashCode, compareTo are based on the id.
 */
public class AbstractIdedElement extends AbstractElement implements IdedElement {
    /*@NonNull*/ private final Id id;

    /**
     * Creates a new instance of this element.
     */
    public AbstractIdedElement(Id id) {
        this.id = id;
    }
    
    /**
     * Creates a new instance of Document.
     */
    @Deprecated
    public AbstractIdedElement(/*@NonNull*/ Layer<?> aLayer, /*@NonNull*/ String aLocId) {
        id = new Id(aLayer, aLocId);
    }

    @Override
    public Id getId() {
        return id;
    }
    
    @Override
    public Layer<?> getLayer() {
        return id.getLayer();
    }
    

    /** Convenience method, adding a link to the id. */
    public void addLink(Layer<?> aLayer, Element aSrc) {
        id.addLink(aLayer, aSrc);
    }

    @Override
    public Location location() {
        return Location.of(this);
    }

    @Override
    public final boolean equals(Object that) {
        if (that == null || getClass() != that.getClass()) {
            return false;
        }

        return id.equals( ((AbstractIdedElement) that).id );
    }

    @Override
    public final int hashCode() {
        return id.hashCode();
    }

    @Override
    public final int compareTo(Element that) {
        if (that instanceof IdedElement) {
            return compareTo((IdedElement)that);
        }
        else {
            return super.compareTo(that); 
        }
    }

    public final int compareTo(IdedElement that) {
        return this.getId().compareTo(that.getId());
    }    
    
    @Override
    public String toString() {
        return id.toString();
    }
}
