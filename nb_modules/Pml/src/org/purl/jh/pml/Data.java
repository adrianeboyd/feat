package org.purl.jh.pml;

import javax.swing.event.EventListenerList;
import org.openide.filesystems.FileObject;
import org.purl.jh.pml.event.DataEvent;
import org.purl.jh.pml.event.DataListener;
import org.purl.jh.util.Util;
import org.purl.jh.util.err.Err;

/**
 * Superclass of layer and tagset.
 * @author jirka
 */
@lombok.Getter
@lombok.Setter
public class Data<C extends Element> extends AbstractListElement<C> {
    private final static org.purl.jh.util.Logger log = org.purl.jh.util.Logger.getLogger(Data.class);

    /** File associated with this data. */
    protected FileObject file;

    /**
     * A short id of the layer.
     * This id is used to
     * <ul>
     * <li> identify the layer to the user (usually displayed with the layer's file)
     * <li> create ids of objects within the layer.
     * </ul>
     *
     * Note: That there is no guarantee that this id is unique. However,
     * when a new id is created, LAW creates an id that is not used by any loaded
     * layer.
     * PML requires ids to be unique within a single layer.
     * Convention - if possible use these ids:
     * <ul>
     * <li>w - w layer
     * <li>m - morph. golden standard
     * <li>mm - morph. ma-ed
     * <li>md(x) - morph. tagger (x is as a source)
     * </ul>
     * @return
     */
    protected String id;

    /** Human readable description of this layer */
    protected String description;

    /**
     * Has this layer been modified since last save?
     * Note: Often overriden to reflect children's status TODO? then fire would nto work
     * @return flag marking the layer as modified
     */
    protected boolean modified;

    protected boolean readOnly;


    public Data() {
    }

    /**
     *
     * @return
     */
    public String getInfo() {
        return String.format("Layer %s  - %s\n\n", id, description);
    }


    public void setFile(FileObject aFile) {
        if (!Util.eq(aFile, file)) {
            file = aFile;
            fireEvents(new DataEvent<>(this, DataEvent.cFileNameChange, null, null));
        }
    }

    /**
     * Sets the modified flag.
     */
    public void setModified() {
        setModified(true);
    }

    /**
     * Sets the modified flag to the specified value.
     *
     * @param aModified
     */
    public void setModified(final boolean aModified) {
        Err.iAssert(!aModified || !readOnly, "Layer %s is readonly and cannot be modified", this);
        if (aModified != modified) {
            log.fine("setModified = change old=%s, new=%s\n", modified, aModified);
            modified = aModified;
            //todo: it is needed in feat, where individual events are fired by the model; but is it needed in general?
            //no event is fired from this method, as the caller is responsible for firing specific event
            //fireBasicUpdate(new BasicLayerEvent(this, BasicLayerEvent.EventId.modifiedChange));     //
        } else {
            log.fine("setModified ignored (%s)\n", aModified);
        }
    }

// -----------------------------------------------------------------------------
//  Event handling
// -----------------------------------------------------------------------------
    protected final EventListenerList listeners = new EventListenerList();

    /**
     * Registers the given observer to begin receiving notifications
     * when changes are made to the document.
     *
     * @param aListener the observer to register
     */
    public void addChangeListener(DataListener aListener) {
        listeners.add(DataListener.class, aListener);
    }
    
    /**
     * Removes a listener that's notified each time a change to the data model occurs.
     * @param aView the <code>DataListener</code> to be removed
     */
    public void removeChangeListener(DataListener aView) {
        listeners.remove(DataListener.class, aView);
    }
    
    public void fireEvents(final DataEvent<?> aDataEvent) {
        log.fine("Firing %s", aDataEvent);
        
        setModified();
        final Object[] list = listeners.getListenerList();
        // Process the listeners last to first, notifying those that are interested in this event
        for (int i = list.length - 2; i >= 0; i -= 2) {
            if (list[i] == DataListener.class) {
                ((DataListener) list[i + 1]).handleChange(aDataEvent);
            }
        }

        log.fine("Done Firing %s", aDataEvent);
    }    
}