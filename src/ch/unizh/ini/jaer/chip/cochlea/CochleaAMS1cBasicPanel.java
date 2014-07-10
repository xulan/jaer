/*
 * CochleaAMS1cControlPanel.java
 *
 * Created on October 24, 2008, 5:39 PM
 */
package ch.unizh.ini.jaer.chip.cochlea;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import net.sf.jaer.biasgen.IPot;
import net.sf.jaer.biasgen.PotTweaker;
import net.sf.jaer.biasgen.VDAC.VPot;

/**
 * The basic control panel for CochleaAMS1c, includes Vbias1, Vbias2, Vq, Vgain and NeuronVLeak.
 * Automatic scaling to predefined slider values, see method ScalingFactor
 * 
 * @author philipp
 */
public final class CochleaAMS1cBasicPanel extends javax.swing.JPanel implements PropertyChangeListener {

    Preferences prefs = Preferences.userNodeForPackage(CochleaAMS1cBasicPanel.class);
    Logger log = Logger.getLogger("CochleaAMS1cBasicPanel");
    CochleaAMS1c chip;
    private CochleaAMS1c.Biasgen biasgen;

    // tweaking around stored configuration
    final IPot Vbias1;
    final IPot Vbias2;
    final VPot Vq;
    final VPot NeuronVLeak;
    final VPot Vgain;
    final float Vbias1_MIN = 192976;      //the desired MIN and MAX bit value for each bias
    final float Vbias1_MAX = 1206109;
    final float Vbias2_MIN = 10372;
    final float Vbias2_MAX = 1255156;
    final float Vq_MIN = 2352;
    final float Vq_MAX = 2366;
    final float NeuronVLeak_MIN = 2107;
    final float NeuronVLeak_MAX = 2149;
    final float Vgain_MIN = 2149;
    final float Vgain_MAX = 2171;
    float Vbias1_scale;
    float Vbias2_scale;
    float Vq_scale;
    float NeuronVLeak_scale;
    float Vgain_scale;
    
    /**
     * Creates new form CochleaAMS1cBasicPanel
     * 
     * @param chip
     */
    public CochleaAMS1cBasicPanel(CochleaAMS1c chip) {
        this.chip = chip;
        biasgen = (CochleaAMS1c.Biasgen) chip.getBiasgen();
        initComponents();
        PotTweaker[] tweakers = {VgainTweaker, VqTweaker, NeuronVLeakTweaker, Vbias1Tweaker, Vbias2Tweaker};
        for (PotTweaker tweaker : tweakers) {
           chip.getBiasgen().getSupport().addPropertyChangeListener(tweaker); // to reset sliders on load/save of biases
        }
        Vbias1 = (IPot) biasgen.ipots.getPotByName("Vbias1");       
        Vbias2 = (IPot) biasgen.ipots.getPotByName("Vbias2");
        Vq = (VPot) biasgen.vpots.getPotByName("Vq");
        NeuronVLeak = (VPot) biasgen.vpots.getPotByName("NeuronVleak");
        Vgain = (VPot) biasgen.vpots.getPotByName("Vgain");
        // tweaking 
        updateSliderScalingFactors();
        //chip.getSupport().addPropertyChangeListener(this);
    }
    
    private void setFileModified() {
        if (chip != null && chip.getAeViewer() != null && chip.getAeViewer().getBiasgenFrame() != null) {
            chip.getAeViewer().getBiasgenFrame().setFileModified(true);
        }
    }
    
    void updateSliderScalingFactors() {
        Vbias1_scale = scalingFactor(Vbias1_MIN, Vbias1_MAX, Vbias1.getPreferedBitValue());
        Vbias2_scale = scalingFactor(Vbias2_MIN, Vbias2_MAX, Vbias2.getPreferedBitValue());
        Vq_scale = scalingFactor(Vq_MIN, Vq_MAX, Vq.getPreferedBitValue());
        NeuronVLeak_scale = scalingFactor(NeuronVLeak_MIN, NeuronVLeak_MAX, NeuronVLeak.getPreferedBitValue());
        Vgain_scale = scalingFactor(Vgain_MIN, Vgain_MAX, Vgain.getPreferedBitValue());
    }

    private float scalingFactor(float min, float max, int pref) {   //calculates the scaling factor for the sliders for each bias individually
        float scaleFactor;
        if (pref - min >= max - pref) { //ensures that the MIN and MAX value are accessible with the slider; the slider might go over the limit on one end 
            scaleFactor = pref / (min - 1);
        } else {
            scaleFactor = (max - 1) / pref;
        }
        return scaleFactor;
    }

    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel7 = new javax.swing.JLabel();
        Vbias1Tweaker = new net.sf.jaer.biasgen.PotTweaker();
        Vbias2Tweaker = new net.sf.jaer.biasgen.PotTweaker();
        VqTweaker = new net.sf.jaer.biasgen.PotTweaker();
        NeuronVLeakTweaker = new net.sf.jaer.biasgen.PotTweaker();
        VgainTweaker = new net.sf.jaer.biasgen.PotTweaker();
        ResetBasic = new javax.swing.JButton();

        setName("CochleaAMS1cControlPanel"); // NOI18N
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        setLayout(new java.awt.GridLayout(0, 1));

        jLabel7.setText("<html>This panel allows \"tweaking\" bias values around the nominal ones loaded from the XML file. Change made here are <b>not</b> permanent until the settings are saved to an XML file. On restart, these new settings will then become the nominal settings.");
        add(jLabel7);

        Vbias1Tweaker.setLessDescription("Lower Frequency Cut off");
        Vbias1Tweaker.setMoreDescription("Higher Frequency Cut off");
        Vbias1Tweaker.setName("Vbias1"); // NOI18N
        Vbias1Tweaker.setTweakDescription("Adjusts the High Frequency cut off");
        Vbias1Tweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                Vbias1TweakerStateChanged(evt);
            }
        });
        add(Vbias1Tweaker);

        Vbias2Tweaker.setLessDescription("Lower Frequency Cut off");
        Vbias2Tweaker.setMoreDescription("Higher Frequency Cut off");
        Vbias2Tweaker.setName("Vbias2"); // NOI18N
        Vbias2Tweaker.setTweakDescription("Adjusts the Low Frequency cut off");
        Vbias2Tweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                Vbias2TweakerStateChanged(evt);
            }
        });
        add(Vbias2Tweaker);

        VqTweaker.setLessDescription("wider, less events");
        VqTweaker.setMoreDescription("narrower, more events");
        VqTweaker.setName("Vq"); // NOI18N
        VqTweaker.setTweakDescription("Adjusts Q of filter - dependent on Vtau");
        VqTweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                VqTweakerStateChanged(evt);
            }
        });
        add(VqTweaker);

        NeuronVLeakTweaker.setLessDescription("Less events");
        NeuronVLeakTweaker.setMoreDescription("More events");
        NeuronVLeakTweaker.setName("NeuronVLeak"); // NOI18N
        NeuronVLeakTweaker.setTweakDescription("Adjusts leak current to neuron");
        NeuronVLeakTweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                NeuronVLeakTweakerStateChanged(evt);
            }
        });
        add(NeuronVLeakTweaker);

        VgainTweaker.setLessDescription("Less gain, less events");
        VgainTweaker.setMoreDescription("More gain, more events");
        VgainTweaker.setName("Vgain"); // NOI18N
        VgainTweaker.setTweakDescription("Adjusts input current to neuron");
        VgainTweaker.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                VgainTweakerStateChanged(evt);
            }
        });
        add(VgainTweaker);

        ResetBasic.setText("Recalculate sliders scaling factors; use after load/save new biases");
        ResetBasic.setToolTipText("Recalculates the sliders scaling factors. Use after saving or loading new biases");
        ResetBasic.setMaximumSize(new java.awt.Dimension(73, 15));
        ResetBasic.setMinimumSize(new java.awt.Dimension(73, 15));
        ResetBasic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ResetBasicActionPerformed(evt);
            }
        });
        add(ResetBasic);
    }// </editor-fold>//GEN-END:initComponents

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
//    if (addedUndoListener) {
//        return;
//    }
//    addedUndoListener = true;
//    if (evt.getComponent() instanceof Container) {
//        Container anc = (Container) evt.getComponent();
//        while (anc != null && anc instanceof Container) {
//            if (anc instanceof UndoableEditListener) {
//                editSupport.addUndoableEditListener((UndoableEditListener) anc);
//                break;
//            }
//            anc = anc.getParent();
//        }
//    }
}//GEN-LAST:event_formAncestorAdded

    private float highCutOff = 1, lowCutOff = 1, filterQ = 1, neuronLeak = 1, gain = 1;

    private void Vbias1TweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_Vbias1TweakerStateChanged
        float value = Vbias1Tweaker.getValue();
        if (value > 1) {
            value = 1;
        } else if (value < -1) {
            value = -1;
        }
        float old = highCutOff;
        if (old == value) {
            return;
        }
        highCutOff = value;
        //final float MAX = (float) 2.5;
        float ratio = (float) Math.exp(value * Math.log(Vbias1_scale));
        Vbias1.changeByRatioFromPreferred(ratio);
        log.info(Vbias1.toString());
        setFileModified();
    }//GEN-LAST:event_Vbias1TweakerStateChanged

    private void Vbias2TweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_Vbias2TweakerStateChanged
        float value = Vbias2Tweaker.getValue();
        if (value > 1) {
            value = 1;
        } else if (value < -1) {
            value = -1;
        }
        float old = lowCutOff;
        if (old == value) {
            return;
        }
        lowCutOff = value;
        //final float MAX = 11;
        float ratio = (float) Math.exp(value * Math.log(Vbias2_scale));
        Vbias2.changeByRatioFromPreferred(ratio);
        log.info(Vbias2.toString());
        setFileModified();
    }//GEN-LAST:event_Vbias2TweakerStateChanged

    private void VqTweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_VqTweakerStateChanged
        float value = VqTweaker.getValue();
        if (value > 1) {
            value = 1;
        } else if (value < -1) {
            value = -1;
        }
        float old = filterQ;
        if (old == value) {
            return;
        }
        filterQ = value;
        //final float MAX = (float) 1.003;
        float ratio = (float) Math.exp(value * Math.log(Vq_scale));   //Calculation to achieve the same changes as changeByRationFromPreferred; function "changeByRationFromPreferred" not implemented in Pot class
        ratio = (int) Math.round(Vq.getPreferedBitValue() * ratio);
        ratio = ratio + (ratio >= 1 ? 1 : -1);
        Vq.setBitValue((int) ratio);
        log.info(Vq.toString());
        setFileModified();
    }//GEN-LAST:event_VqTweakerStateChanged

    private void NeuronVLeakTweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_NeuronVLeakTweakerStateChanged
        float value = NeuronVLeakTweaker.getValue();
        if (value > 1) {
            value = 1;
        } else if (value < -1) {
            value = -1;
        }
        float old = neuronLeak;
        if (old == value) {
            return;
        }
        neuronLeak = value;
        //final float MAX = (float) 1.01;
        float ratio = (float) Math.exp(value * Math.log(NeuronVLeak_scale));  //Calculation to achieve the same changes as changeByRationFromPreferred; function "changeByRationFromPreferred" not implemented in Pot class
        ratio = (int) Math.round(NeuronVLeak.getPreferedBitValue() * ratio);
        ratio = ratio + (ratio >= 1 ? 1 : -1);
        NeuronVLeak.setBitValue((int) ratio);
        log.info(NeuronVLeak.toString());
        setFileModified();
    }//GEN-LAST:event_NeuronVLeakTweakerStateChanged

    private void VgainTweakerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_VgainTweakerStateChanged
        float value = 0 - VgainTweaker.getValue();
        if (value > 1) {
            value = 1;
        } else if (value < -1) {
            value = -1;
        }
        float old = gain;
        if (old == value) {
            return;
        }
        gain = value;
        //final float MAX = (float) 1.005;
        float ratio = (float) Math.exp(value * Math.log(Vgain_scale));  //Calculation to achieve the same changes as changeByRationFromPreferred; function "changeByRationFromPreferred" not implemented in Pot class
        ratio = (int) Math.round(Vgain.getPreferedBitValue() * ratio);
        ratio = ratio + (ratio >= 1 ? 1 : -1);
        Vgain.setBitValue((int) ratio);
        log.info(Vgain.toString());
        setFileModified();
    }//GEN-LAST:event_VgainTweakerStateChanged

    private void ResetBasicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResetBasicActionPerformed
       /* VgainTweaker.setValue(0);
        Vgain.setBitValue(Vgain.getPreferedBitValue());
        VqTweaker.setValue(0);
        Vq.setBitValue(Vq.getPreferedBitValue());
        NeuronVLeakTweaker.setValue(0);
        NeuronVLeak.setBitValue(NeuronVLeak.getPreferedBitValue());
        Vbias1Tweaker.setValue(0);
        Vbias1.setBitValue(Vbias1.getPreferedBitValue());
        Vbias2Tweaker.setValue(0);
        Vbias2.setBitValue(Vbias2.getPreferedBitValue());
        setFileModified(); */
        
        updateSliderScalingFactors();
    }//GEN-LAST:event_ResetBasicActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private net.sf.jaer.biasgen.PotTweaker NeuronVLeakTweaker;
    private javax.swing.JButton ResetBasic;
    private net.sf.jaer.biasgen.PotTweaker Vbias1Tweaker;
    private net.sf.jaer.biasgen.PotTweaker Vbias2Tweaker;
    private net.sf.jaer.biasgen.PotTweaker VgainTweaker;
    private net.sf.jaer.biasgen.PotTweaker VqTweaker;
    private javax.swing.JLabel jLabel7;
    // End of variables declaration//GEN-END:variables

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}