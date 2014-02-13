package org.purl.net.jh.nbutil;

import javax.swing.event.ChangeListener;
import org.purl.net.jh.nbutil.ZoomUtil.Type;

/**
 * This composite zoom control:
 * <ul>
 *   <li>Listens to changes of individual controls
 *   <li>Modifies the model directly
 *   <li>listens to the model's changes
 * </ul>
 * 
 * @author jirka
 */
public class ZoomControl extends javax.swing.JToolBar {
    private final static org.purl.jh.util.Logger log = org.purl.jh.util.Logger.getLogger(ZoomControl.class);
    private final static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(ZoomControl.class);

    public final int cSliderMax = 50;  // remove


    /** Model */
    private ZoomModel model;


    final ChangeListener modelChangeListener = new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            modelChanged();
        }
    };

    public ZoomControl() {
        initComponents();

        model = new ZoomModel();
        model.addChangeListener(modelChangeListener);

        //init();
    }

    /*
     * This should probably be in the control
     */
    public void init() {
        slider.setMajorTickSpacing(50);
        slider.setMaximum(50);
        slider.setMinimum(-50);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setToolTipText(bundle.getString("ZoomControl.slider.toolTipText"));
        slider.setValue(0);
    }

    public ZoomModel getModel() {
        return model;
    }

    public void setModel(ZoomModel zoom) {
        this.model = zoom;
        getControlsState();
    }


    protected void modelChanged() {
        factor2slider(model.getVal(), cSliderMax);
        setControlsState();
    }

// =============================================================================
// Responding to controls
// =============================================================================
    
    protected void getControlsState() {
        if (fitButton.isSelected()) {
            model.setType(Type.pageFit);
        } else if (wfitButton.isSelected()) {
            model.setType(Type.widthFit);
        } else if (origButton.isSelected()) {
            model.setType(Type.orig);
        } else if (customButton.isSelected()) {
            model.setValues(Type.custom, slider2factor(slider.getValue(), cSliderMax));
        }
    }

    protected void setControlsState() {
        slider.setValue(factor2slider(model.getVal(), cSliderMax));
        switch(model.getType()) {
            case pageFit:   fitButton.setSelected(true); break;
            case widthFit: wfitButton.setSelected(true); break;
            case orig:     origButton.setSelected(true); break;
            case custom: customButton.setSelected(true); break;
        }
    }

    protected int factor2slider(double aFactor, int aMax) {
        return (int) Math.round( Math.log(aFactor) * (aMax*1.0) );
    }

    protected double slider2factor(int aVal, int aMax) {
        return Math.exp((aVal*1.0)/(aMax*1.0));
    }


//        scene.setZoomFactor(z);
//        if (aRepaint) {
//            treeComponent.repaint();
//        }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        zoomTypeGroup = new javax.swing.ButtonGroup();
        fitButton = new javax.swing.JToggleButton();
        wfitButton = new javax.swing.JToggleButton();
        origButton = new javax.swing.JToggleButton();
        customButton = new javax.swing.JToggleButton();
        slider = new javax.swing.JSlider();

        setPreferredSize(new java.awt.Dimension(697, 36));

        zoomTypeGroup.add(fitButton);
        fitButton.setText(org.openide.util.NbBundle.getMessage(ZoomControl.class, "ZoomControl.fitButton.text")); // NOI18N
        fitButton.setFocusable(false);
        fitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        fitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        fitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fitButtonimgZoomPressed(evt);
            }
        });

        zoomTypeGroup.add(wfitButton);
        wfitButton.setSelected(true);
        wfitButton.setText(org.openide.util.NbBundle.getMessage(ZoomControl.class, "ZoomControl.wfitButton.text")); // NOI18N
        wfitButton.setFocusable(false);
        wfitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        wfitButton.setMaximumSize(new java.awt.Dimension(39, 22));
        wfitButton.setMinimumSize(new java.awt.Dimension(39, 22));
        wfitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        wfitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wfitButtonimgZoomPressed(evt);
            }
        });

        zoomTypeGroup.add(origButton);
        origButton.setText(org.openide.util.NbBundle.getMessage(ZoomControl.class, "ZoomControl.origButton.text")); // NOI18N
        origButton.setFocusable(false);
        origButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        origButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        origButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                origButtonimgZoomPressed(evt);
            }
        });

        zoomTypeGroup.add(customButton);
        customButton.setText(org.openide.util.NbBundle.getMessage(ZoomControl.class, "ZoomControl.customButton.text")); // NOI18N
        customButton.setFocusable(false);
        customButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        customButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        customButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customButtonimgZoomPressed(evt);
            }
        });

        slider.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        slider.setMajorTickSpacing(100);
        slider.setMinimum(-100);
        slider.setMinorTickSpacing(20);
        slider.setPaintTicks(true);
        slider.setToolTipText(org.openide.util.NbBundle.getMessage(ZoomControl.class, "ZoomControl.slider.toolTipText")); // NOI18N
        slider.setValue(0);
        slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(origButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(wfitButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fitButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(customButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(slider, javax.swing.GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(customButton, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(fitButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(wfitButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(origButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(slider, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fitButtonimgZoomPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fitButtonimgZoomPressed
        getControlsState();
}//GEN-LAST:event_fitButtonimgZoomPressed

    private void wfitButtonimgZoomPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wfitButtonimgZoomPressed
        getControlsState();
}//GEN-LAST:event_wfitButtonimgZoomPressed

    private void origButtonimgZoomPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_origButtonimgZoomPressed
        getControlsState();
}//GEN-LAST:event_origButtonimgZoomPressed

    private void customButtonimgZoomPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customButtonimgZoomPressed
        getControlsState();
}//GEN-LAST:event_customButtonimgZoomPressed

    private void sliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderStateChanged
        customButton.setSelected(true);
        getControlsState();
}//GEN-LAST:event_sliderStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton customButton;
    private javax.swing.JToggleButton fitButton;
    private javax.swing.JToggleButton origButton;
    private javax.swing.JSlider slider;
    private javax.swing.JToggleButton wfitButton;
    private javax.swing.ButtonGroup zoomTypeGroup;
    // End of variables declaration//GEN-END:variables
}