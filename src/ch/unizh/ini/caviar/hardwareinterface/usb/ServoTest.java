 /*
  * ServoTest.java
  *
  * Created on July 4, 2006, 3:47 PM
  */

package ch.unizh.ini.caviar.hardwareinterface.usb;

import ch.unizh.ini.caviar.hardwareinterface.*;
import ch.unizh.ini.caviar.hardwareinterface.HardwareInterfaceException;
import ch.unizh.ini.caviar.util.HexString;
import ch.unizh.ini.tobi.rccar.CarServoInterfaceFactory;
import ch.unizh.ini.tobi.rccar.SiLabsC8051F320_USBIO_CarServoController;
import de.thesycon.usbio.*;
import de.thesycon.usbio.PnPNotify;
import de.thesycon.usbio.PnPNotifyInterface;
import java.text.ParseException;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Tests the hardware USB  servo interface.
 * @author  tobi
 */
public class ServoTest extends javax.swing.JFrame implements PnPNotifyInterface {
    final int MAX_SLIDER=1000;
    static Logger log=Logger.getLogger("ServoTest");
    
    ServoInterface hwInterface=null;
    float[] servoValues;
    PnPNotify pnp=null;
    FastOscillator oscillator;
    private boolean liveSlidersEnabled;
    java.util.Timer timer;
    private boolean oscLowPhase=true;
    
    class OscillatorTask extends TimerTask {
        
        int delayMs=1000;
        float low=0;
        float high=1;
        ArrayList<Integer> servos=new ArrayList<Integer>();
        OscillatorTask(float low, float high){
            super();
            this.low=low;
            this.high=high;
            servos.clear();
            if(oscSelRadioButton0.isSelected()){ servos.add(0);}
            if(oscSelRadioButton1.isSelected()){ servos.add(1);}
            if(oscSelRadioButton2.isSelected()){ servos.add(2);}
            if(oscSelRadioButton3.isSelected()){ servos.add(3);}
        }
        
        public void run() {
            float val=oscLowPhase?low:high;
            oscLowPhase=!oscLowPhase;
//            log.info("set "+val);
            if(hwInterface==null) return;
            for(Integer i:servos){
                try {
                    hwInterface.setServoValue(i,val);
                } catch (HardwareInterfaceException ex) {
                    log.warning(ex.toString());
                }
            }
        }
    }
    
    class FastOscillator extends Thread{
        int delayNanos=1000000;
        float low=0;
        float high=1;
        private volatile boolean stop=false;
        ArrayList<Integer> servos=new ArrayList<Integer>();
        FastOscillator(float low, float high, int nanos){
            super();
            this.low=low;
            this.high=high;
            this.delayNanos=nanos;
            servos.clear();
            if(oscSelRadioButton0.isSelected()){ servos.add(0);}
            if(oscSelRadioButton1.isSelected()){ servos.add(1);}
            if(oscSelRadioButton2.isSelected()){ servos.add(2);}
            if(oscSelRadioButton3.isSelected()){ servos.add(3);}
        }
        public void run() {
            while(!stop){
                float val=oscLowPhase?low:high;
                oscLowPhase=!oscLowPhase;
//            log.info("set "+val);
                if(hwInterface==null) return;
                for(Integer i:servos){
                    try {
                        hwInterface.setServoValue(i,val);
                    } catch (HardwareInterfaceException ex) {
                        log.warning(ex.toString());
                    }
                }
                try {
                    Thread.currentThread().sleep(0,delayNanos);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        public void shutdownThread(){
            stop=true;
            try {
                join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private void startOscillator(){
        try {
            float low=Float.parseFloat(oscLowTextField.getText());
            float high=Float.parseFloat(oscHighTextField.getText());
            float periodMs=Float.parseFloat(oscDelayTextField.getText());
            int periodNs=(int)(periodMs*1e6f);
            oscDelayTextField.setEnabled(false);
            oscHighTextField.setEnabled(false);
            oscLowTextField.setEnabled(false);
            if(periodMs>=2){
                log.info("using java Timer because period is longer than 2ms");
                timer=new java.util.Timer();
                timer.scheduleAtFixedRate(new OscillatorTask(low,high),0,(int)(periodMs/2)); // start right away, wait delayMs/2 between phases
            }else{
                log.info("using nanotime run loop for short period");
                oscillator=new FastOscillator(low,high,(int)periodNs/2);
                oscillator.start();
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
    
    private void stopOscillator(){
        if(timer!=null) {
            timer.cancel();
            timer=null;
        }
        if(oscillator!=null){
            oscillator.shutdownThread();
        }
        oscDelayTextField.setEnabled(true);
        oscHighTextField.setEnabled(true);
        oscLowTextField.setEnabled(true);
    }
    
    /** Creates new form ServoTest */
    public ServoTest() {
        initComponents();
        liveSlidersEnabled=liveSlidersCheckBox.isSelected();
        if(UsbIoUtilities.usbIoIsAvailable){
            pnp=new PnPNotify(this);
            pnp.enablePnPNotification(SiLabsC8051F320_USBIO_ServoController.GUID);
            pnp.enablePnPNotification(SiLabsC8051F320_USBIO_CarServoController.GUID);
        }
        JOptionPane.showMessageDialog(this,"\"Choose interaface\" to open the controller to test");
    }
    
    /** Constructs a new controller panel using existing hardware interface
     * @param hw the interface
     */
    public ServoTest(ServoInterface hw){
        this();
        hwInterface=hw;
        String s=hw.getClass().getSimpleName();
        setTitle(s);
        servoTypeComboBox.addItem(s);
        int n=servoTypeComboBox.getItemCount();
        for(int i=0;i<servoTypeComboBox.getItemCount();i++){
            if(s==servoTypeComboBox.getItemAt(i)){
                servoTypeComboBox.setSelectedItem(i);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        servo0Panel = new javax.swing.JPanel();
        servo0Slider = new javax.swing.JSlider();
        disableServo0Button = new javax.swing.JButton();
        servo1Panel = new javax.swing.JPanel();
        servo1Slider = new javax.swing.JSlider();
        disableServo1Button = new javax.swing.JButton();
        syncPanel = new javax.swing.JPanel();
        synchronizeCheckBox = new javax.swing.JCheckBox();
        sendValuesButton = new javax.swing.JButton();
        disableButton = new javax.swing.JButton();
        liveSlidersCheckBox = new javax.swing.JCheckBox();
        servo2Panel = new javax.swing.JPanel();
        servo2Slider = new javax.swing.JSlider();
        disableServo2Button = new javax.swing.JButton();
        servo3Panel = new javax.swing.JPanel();
        servo3Slider = new javax.swing.JSlider();
        disableServo3Button = new javax.swing.JButton();
        chooserPanel = new javax.swing.JPanel();
        servoTypeComboBox = new javax.swing.JComboBox();
        oscillatePanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        oscDelayTextField = new javax.swing.JTextField();
        oscLowTextField = new javax.swing.JTextField();
        oscHighTextField = new javax.swing.JTextField();
        oscStartStopToggleButton = new javax.swing.JToggleButton();
        oscSelRadioButton0 = new javax.swing.JRadioButton();
        oscSelRadioButton1 = new javax.swing.JRadioButton();
        oscSelRadioButton2 = new javax.swing.JRadioButton();
        oscSelRadioButton3 = new javax.swing.JRadioButton();
        carServoPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        deadzoneSteeringTextFirld = new javax.swing.JTextField();
        deadzoneSpeedTextFirld = new javax.swing.JTextField();
        radioLockoutTimeTextField = new javax.swing.JTextField();
        servoFreqPanel = new javax.swing.JPanel();
        servoFreqLabel = new javax.swing.JLabel();
        servoFreqTextBox = new javax.swing.JTextField();
        port2Panel = new javax.swing.JPanel();
        port2ValueTextField = new javax.swing.JTextField();
        port2Label = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();

        jButton1.setText("jButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("ServoTest");

        servo0Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Servo 0"));

        servo0Slider.setMaximum(1000);
        servo0Slider.setMinorTickSpacing(10);
        servo0Slider.setValue(500);
        servo0Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                servo0SliderStateChanged(evt);
            }
        });

        disableServo0Button.setText("Disable");
        disableServo0Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableServo0ButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout servo0PanelLayout = new org.jdesktop.layout.GroupLayout(servo0Panel);
        servo0Panel.setLayout(servo0PanelLayout);
        servo0PanelLayout.setHorizontalGroup(
            servo0PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servo0PanelLayout.createSequentialGroup()
                .add(servo0PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(servo0Slider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(servo0PanelLayout.createSequentialGroup()
                        .add(69, 69, 69)
                        .add(disableServo0Button)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        servo0PanelLayout.setVerticalGroup(
            servo0PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servo0PanelLayout.createSequentialGroup()
                .add(servo0Slider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(disableServo0Button))
        );

        servo1Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Servo 1"));

        servo1Slider.setMaximum(1000);
        servo1Slider.setMinorTickSpacing(10);
        servo1Slider.setValue(500);
        servo1Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                servo1SliderStateChanged(evt);
            }
        });

        disableServo1Button.setText("Disable");
        disableServo1Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableServo1ButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout servo1PanelLayout = new org.jdesktop.layout.GroupLayout(servo1Panel);
        servo1Panel.setLayout(servo1PanelLayout);
        servo1PanelLayout.setHorizontalGroup(
            servo1PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servo1PanelLayout.createSequentialGroup()
                .add(servo1PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(servo1Slider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(servo1PanelLayout.createSequentialGroup()
                        .add(69, 69, 69)
                        .add(disableServo1Button)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        servo1PanelLayout.setVerticalGroup(
            servo1PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servo1PanelLayout.createSequentialGroup()
                .add(servo1Slider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(disableServo1Button))
        );

        synchronizeCheckBox.setText("Synchronize");
        synchronizeCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        synchronizeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        sendValuesButton.setText("Send all values");
        sendValuesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendValuesButtonActionPerformed(evt);
            }
        });

        disableButton.setText("Disable all");
        disableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableButtonActionPerformed(evt);
            }
        });

        liveSlidersCheckBox.setText("Sliders are live");
        liveSlidersCheckBox.setToolTipText("Enable to makes sliders immediately affect servo, disable to wait until button release");
        liveSlidersCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        liveSlidersCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        liveSlidersCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                liveSlidersCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout syncPanelLayout = new org.jdesktop.layout.GroupLayout(syncPanel);
        syncPanel.setLayout(syncPanelLayout);
        syncPanelLayout.setHorizontalGroup(
            syncPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(syncPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(syncPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(syncPanelLayout.createSequentialGroup()
                        .add(synchronizeCheckBox)
                        .add(21, 21, 21)
                        .add(sendValuesButton)
                        .add(14, 14, 14)
                        .add(disableButton))
                    .add(liveSlidersCheckBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        syncPanelLayout.setVerticalGroup(
            syncPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(syncPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(syncPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(synchronizeCheckBox)
                    .add(sendValuesButton)
                    .add(disableButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(liveSlidersCheckBox)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        servo2Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Servo 2"));

        servo2Slider.setMaximum(1000);
        servo2Slider.setMinorTickSpacing(10);
        servo2Slider.setValue(500);
        servo2Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                servo2SliderStateChanged(evt);
            }
        });

        disableServo2Button.setText("Disable");
        disableServo2Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableServo2ButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout servo2PanelLayout = new org.jdesktop.layout.GroupLayout(servo2Panel);
        servo2Panel.setLayout(servo2PanelLayout);
        servo2PanelLayout.setHorizontalGroup(
            servo2PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servo2PanelLayout.createSequentialGroup()
                .add(servo2PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(servo2Slider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(servo2PanelLayout.createSequentialGroup()
                        .add(69, 69, 69)
                        .add(disableServo2Button)))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        servo2PanelLayout.setVerticalGroup(
            servo2PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servo2PanelLayout.createSequentialGroup()
                .add(servo2Slider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(disableServo2Button))
        );

        servo3Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Servo 3"));

        servo3Slider.setMaximum(1000);
        servo3Slider.setMinorTickSpacing(10);
        servo3Slider.setValue(500);
        servo3Slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                servo3SliderStateChanged(evt);
            }
        });

        disableServo3Button.setText("Disable");
        disableServo3Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableServo3ButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout servo3PanelLayout = new org.jdesktop.layout.GroupLayout(servo3Panel);
        servo3Panel.setLayout(servo3PanelLayout);
        servo3PanelLayout.setHorizontalGroup(
            servo3PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servo3PanelLayout.createSequentialGroup()
                .add(servo3PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(servo3Slider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(servo3PanelLayout.createSequentialGroup()
                        .add(69, 69, 69)
                        .add(disableServo3Button)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        servo3PanelLayout.setVerticalGroup(
            servo3PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servo3PanelLayout.createSequentialGroup()
                .add(servo3Slider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(disableServo3Button))
        );

        chooserPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Choose interface"));

        servoTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "ServoController", "CarServoController" }));
        servoTypeComboBox.setToolTipText("Selects device type to be controlled");
        servoTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                servoTypeComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout chooserPanelLayout = new org.jdesktop.layout.GroupLayout(chooserPanel);
        chooserPanel.setLayout(chooserPanelLayout);
        chooserPanelLayout.setHorizontalGroup(
            chooserPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, chooserPanelLayout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(servoTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 211, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        chooserPanelLayout.setVerticalGroup(
            chooserPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(chooserPanelLayout.createSequentialGroup()
                .add(servoTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        oscillatePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Oscillate"));

        jLabel1.setText("Period  (ms)");
        jLabel1.setToolTipText("Period of oscillation");

        jLabel2.setText("Low (0-1)");
        jLabel2.setToolTipText("Low servo value");

        jLabel3.setText("High (0-1)");

        oscDelayTextField.setText("1000");
        oscDelayTextField.setToolTipText("Period of oscillation (ms)");

        oscLowTextField.setText("0");
        oscLowTextField.setToolTipText("Low servo value");

        oscHighTextField.setText("1");
        oscHighTextField.setToolTipText("High servo value");

        oscStartStopToggleButton.setText("Start");
        oscStartStopToggleButton.setToolTipText("Starts and stops oscillation task");
        oscStartStopToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                oscStartStopToggleButtonActionPerformed(evt);
            }
        });

        oscSelRadioButton0.setSelected(true);
        oscSelRadioButton0.setText("S0");
        oscSelRadioButton0.setToolTipText("Oscillate servo 0");
        oscSelRadioButton0.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        oscSelRadioButton0.setMargin(new java.awt.Insets(0, 0, 0, 0));

        oscSelRadioButton1.setText("S1");
        oscSelRadioButton1.setToolTipText("Oscillate servo 1");
        oscSelRadioButton1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        oscSelRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        oscSelRadioButton2.setText("S2");
        oscSelRadioButton2.setToolTipText("Oscillate servo 2");
        oscSelRadioButton2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        oscSelRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        oscSelRadioButton3.setText("S3");
        oscSelRadioButton3.setToolTipText("Oscillate servo 3");
        oscSelRadioButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        oscSelRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout oscillatePanelLayout = new org.jdesktop.layout.GroupLayout(oscillatePanel);
        oscillatePanel.setLayout(oscillatePanelLayout);
        oscillatePanelLayout.setHorizontalGroup(
            oscillatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(oscillatePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(oscillatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(jLabel2)
                    .add(jLabel3)
                    .add(oscillatePanelLayout.createSequentialGroup()
                        .add(oscSelRadioButton0)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(oscSelRadioButton1)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(oscillatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(oscLowTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(oscDelayTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(oscillatePanelLayout.createSequentialGroup()
                        .add(oscHighTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(24, 24, 24)
                        .add(oscStartStopToggleButton))
                    .add(oscillatePanelLayout.createSequentialGroup()
                        .add(oscSelRadioButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(oscSelRadioButton3)))
                .add(17, 17, 17))
        );
        oscillatePanelLayout.setVerticalGroup(
            oscillatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(oscillatePanelLayout.createSequentialGroup()
                .add(oscillatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(oscDelayTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(oscillatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(oscLowTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(oscillatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(oscHighTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(oscStartStopToggleButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(oscillatePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(oscSelRadioButton0)
                    .add(oscSelRadioButton1)
                    .add(oscSelRadioButton2)
                    .add(oscSelRadioButton3)))
        );

        carServoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Car Servo Controls"));

        jLabel4.setText("Deadzone Steering (0-1)");

        jLabel5.setText("Deadzone Speed (0-1)");

        jLabel6.setText("Radio Lockout Time (ms)");

        deadzoneSteeringTextFirld.setColumns(5);
        deadzoneSteeringTextFirld.setToolTipText("Sets dead zone for remote radio control of steering");
        deadzoneSteeringTextFirld.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deadzoneSteeringTextFirldActionPerformed(evt);
            }
        });

        deadzoneSpeedTextFirld.setColumns(5);
        deadzoneSpeedTextFirld.setToolTipText("Sets dead zone for remote radio control of speed");
        deadzoneSpeedTextFirld.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deadzoneSpeedTextFirldActionPerformed(evt);
            }
        });

        radioLockoutTimeTextField.setColumns(5);
        radioLockoutTimeTextField.setToolTipText("Sets lockout time after radio command that computer control is ignored");
        radioLockoutTimeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioLockoutTimeTextFieldActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout carServoPanelLayout = new org.jdesktop.layout.GroupLayout(carServoPanel);
        carServoPanel.setLayout(carServoPanelLayout);
        carServoPanelLayout.setHorizontalGroup(
            carServoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(carServoPanelLayout.createSequentialGroup()
                .add(carServoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(jLabel5)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(carServoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(radioLockoutTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deadzoneSpeedTextFirld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deadzoneSteeringTextFirld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        carServoPanelLayout.setVerticalGroup(
            carServoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(carServoPanelLayout.createSequentialGroup()
                .add(carServoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(deadzoneSteeringTextFirld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(carServoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(deadzoneSpeedTextFirld, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(carServoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(radioLockoutTimeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        servoFreqLabel.setText("Servo frequency (Hz)");

        servoFreqTextBox.setColumns(10);
        servoFreqTextBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                servoFreqTextBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout servoFreqPanelLayout = new org.jdesktop.layout.GroupLayout(servoFreqPanel);
        servoFreqPanel.setLayout(servoFreqPanelLayout);
        servoFreqPanelLayout.setHorizontalGroup(
            servoFreqPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servoFreqPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(servoFreqLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(servoFreqTextBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 89, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        servoFreqPanelLayout.setVerticalGroup(
            servoFreqPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(servoFreqPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(servoFreqPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(servoFreqLabel)
                    .add(servoFreqTextBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        port2Panel.setBorder(javax.swing.BorderFactory.createTitledBorder("Port2"));

        port2ValueTextField.setColumns(4);
        port2ValueTextField.setText("0xff");
        port2ValueTextField.setToolTipText("enter a hex value to set port 2");
        port2ValueTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                port2ValueTextFieldActionPerformed(evt);
            }
        });

        port2Label.setText("value (hex)");

        org.jdesktop.layout.GroupLayout port2PanelLayout = new org.jdesktop.layout.GroupLayout(port2Panel);
        port2Panel.setLayout(port2PanelLayout);
        port2PanelLayout.setHorizontalGroup(
            port2PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(port2PanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(port2Label)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(port2ValueTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE))
        );
        port2PanelLayout.setVerticalGroup(
            port2PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(port2PanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(port2Label)
                .add(port2ValueTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, 0));
        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(servo0Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(servo1Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(servo2Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(servo3Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(servoFreqPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(syncPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(oscillatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(carServoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(chooserPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(47, 47, 47)
                                .add(port2Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(servo0Panel, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(servo1Panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(servo2Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(servo3Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(syncPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(servoFreqPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(carServoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(oscillatePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(chooserPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(port2Panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(57, 57, 57))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void liveSlidersCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_liveSlidersCheckBoxActionPerformed
        liveSlidersEnabled=liveSlidersCheckBox.isSelected();
    }//GEN-LAST:event_liveSlidersCheckBoxActionPerformed
    
    private void servoFreqTextBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_servoFreqTextBoxActionPerformed
        try{
            SiLabsC8051F320_USBIO_ServoController servo=(SiLabsC8051F320_USBIO_ServoController)hwInterface;
            float desiredFreq=Float.parseFloat(servoFreqTextBox.getText());
            float actualFreq=servo.setServoPWMFrequencyHz(desiredFreq);
            servoFreqTextBox.setText(Float.toString(actualFreq));
        }catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_servoFreqTextBoxActionPerformed
    
    private void radioLockoutTimeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioLockoutTimeTextFieldActionPerformed
        try{
            SiLabsC8051F320_USBIO_CarServoController carServo=(SiLabsC8051F320_USBIO_CarServoController)hwInterface;
            int t=Integer.parseInt(radioLockoutTimeTextField.getText());
            carServo.setRadioTimeoutMs(t);
        }catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_radioLockoutTimeTextFieldActionPerformed
    
    private void deadzoneSpeedTextFirldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deadzoneSpeedTextFirldActionPerformed
        try{
            if(hwInterface==null) return;
            SiLabsC8051F320_USBIO_CarServoController carServo=(SiLabsC8051F320_USBIO_CarServoController)hwInterface;
            float f=Float.parseFloat(deadzoneSpeedTextFirld.getText());
            if(f<0 || f>1){
                deadzoneSpeedTextFirld.selectAll();
                return;
            }
            carServo.setDeadzoneForSpeed(f);
        }catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_deadzoneSpeedTextFirldActionPerformed
    
    private void deadzoneSteeringTextFirldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deadzoneSteeringTextFirldActionPerformed
        try{
            if(hwInterface==null) return;
            SiLabsC8051F320_USBIO_CarServoController carServo=(SiLabsC8051F320_USBIO_CarServoController)hwInterface;
            float f=Float.parseFloat(deadzoneSteeringTextFirld.getText());
            if(f<0 || f>1){
                deadzoneSteeringTextFirld.selectAll();
                return;
            }
            carServo.setDeadzoneForSteering(f);
        }catch(Exception e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_deadzoneSteeringTextFirldActionPerformed
    
    private void oscStartStopToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_oscStartStopToggleButtonActionPerformed
        if(oscStartStopToggleButton.isSelected()){
            oscStartStopToggleButton.setText("Stop");
            startOscillator();
        }else{
            stopOscillator();
            oscStartStopToggleButton.setText("Start");
        }
    }//GEN-LAST:event_oscStartStopToggleButtonActionPerformed
    
    private void servoTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_servoTypeComboBoxActionPerformed
        synchronized(this){
            switch(servoTypeComboBox.getSelectedIndex()){
                case 0:
                    if(hwInterface!=null){
                        hwInterface.close();
                    }
                    hwInterface=null;
                    setTitle("ServoTest (no interface opened)");
                    break;
                case 1: // servo interface
                    try{
//                        hwInterface=new SiLabsC8051F320_USBIO_ServoController();
                        hwInterface=(ServoInterface)ServoInterfaceFactory.instance().getFirstAvailableInterface(); //new SiLabsC8051F320_USBIO_ServoController();
                        if(hwInterface==null) {
                            JOptionPane.showMessageDialog(this,"No ServoInterface found");
                            servoTypeComboBox.setSelectedIndex(0);
                            break;
                        }
                        hwInterface.open();
                        servoValues=new float[hwInterface.getNumServos()];
                        setTitle("ServoController");
                        carServoPanel.setEnabled(false);
                    }catch(HardwareInterfaceException e){
                        e.printStackTrace();
                    }
                    break;
                case 2: // car servo controller
                    try{
                        hwInterface=(ServoInterface)CarServoInterfaceFactory.instance().getFirstAvailableInterface(); //new SiLabsC8051F320_USBIO_ServoController();
                        if(hwInterface==null) {
                            JOptionPane.showMessageDialog(this,"No CarServoInterface found");
                            servoTypeComboBox.setSelectedIndex(0);
                            break;
                        }
                        hwInterface.open();
                        servoValues=new float[hwInterface.getNumServos()];
                        setTitle("CarServoController");
                        carServoPanel.setEnabled(true);
                    }catch(HardwareInterfaceException e){
                        e.printStackTrace();
                    }
                    break;
                default:
                    log.warning("unknown selection");
            }
        }
    }//GEN-LAST:event_servoTypeComboBoxActionPerformed
    
    
    void disableServo(int i){
        try{
            hwInterface.disableServo(i);
        }catch(HardwareInterfaceException e){
            e.printStackTrace();
        }
    }
    
    
    void setAllServos(float f) throws HardwareInterfaceException{
        Arrays.fill(servoValues,f);
        hwInterface.setAllServoValues(servoValues);
    }
    
    void setServo(int servo, ChangeEvent evt){
        
        if(!(evt.getSource() instanceof JSlider)){
            log.warning("evt not from a slider: "+evt);
        }
        JSlider slider=(JSlider)evt.getSource();
        if(!liveSlidersEnabled && slider.getValueIsAdjusting()) return;
        float f= (float)slider.getValue()/MAX_SLIDER;
        if(hwInterface==null){
            log.warning("null hardware interface");
            return;
        }
        try{
            if(synchronizeCheckBox.isSelected()) {
                setAllServos(f);
            }else{
                hwInterface.setServoValue(servo,f);
            }
        }catch(HardwareInterfaceException e){
            e.printStackTrace();
        }
    }
    
    
    void delayMs(int ms){
        try{
            Thread.currentThread().sleep(ms);
        }catch(InterruptedException e){}
    }
    
    
    
    private void disableServo3ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableServo3ButtonActionPerformed
        disableServo(3);
    }//GEN-LAST:event_disableServo3ButtonActionPerformed
    
    private void servo3SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_servo3SliderStateChanged
        setServo(3,evt);
    }//GEN-LAST:event_servo3SliderStateChanged
    
    private void disableServo2ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableServo2ButtonActionPerformed
        disableServo(2);
    }//GEN-LAST:event_disableServo2ButtonActionPerformed
    
    private void servo2SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_servo2SliderStateChanged
        setServo(2,evt);
    }//GEN-LAST:event_servo2SliderStateChanged
    
    private void disableServo1ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableServo1ButtonActionPerformed
        disableServo(1);
    }//GEN-LAST:event_disableServo1ButtonActionPerformed
    
    private void disableServo0ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableServo0ButtonActionPerformed
        disableServo(0);
    }//GEN-LAST:event_disableServo0ButtonActionPerformed
    
    
    private void disableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableButtonActionPerformed
        try{
            hwInterface.disableAllServos();
        }catch(HardwareInterfaceException e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_disableButtonActionPerformed
    
    private void sendValuesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendValuesButtonActionPerformed
        try{
            hwInterface.setServoValue(3,(float)servo3Slider.getValue()/MAX_SLIDER);
            hwInterface.setServoValue(2,(float)servo2Slider.getValue()/MAX_SLIDER);
            hwInterface.setServoValue(1,(float)servo1Slider.getValue()/MAX_SLIDER);
            hwInterface.setServoValue(0,(float)servo0Slider.getValue()/MAX_SLIDER);
        }catch(HardwareInterfaceException e){
            e.printStackTrace();
        }
    }//GEN-LAST:event_sendValuesButtonActionPerformed
    
    
    
    private void servo1SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_servo1SliderStateChanged
        setServo(1,evt);
    }//GEN-LAST:event_servo1SliderStateChanged
    
    private void servo0SliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_servo0SliderStateChanged
        setServo(0,evt);
    }//GEN-LAST:event_servo0SliderStateChanged
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        if(hwInterface!=null) {
            if(hwInterface!=null && hwInterface instanceof SiLabsC8051F320_USBIO_ServoController){
                try{
                    hwInterface.disableAllServos();
                }catch(HardwareInterfaceException e){
                    e.printStackTrace();
                }
            }
            hwInterface.close();
        }
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void port2ValueTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_port2ValueTextFieldActionPerformed
        if(hwInterface!=null && hwInterface instanceof SiLabsC8051F320_USBIO_ServoController){
            SiLabsC8051F320_USBIO_ServoController controller=(SiLabsC8051F320_USBIO_ServoController)hwInterface;
            try{
                int val=HexString.parseShort(port2ValueTextField.getText());
                controller.setPort2(val);
            }catch(ParseException e){
                log.warning(e.toString());
            }
        }
        port2ValueTextField.selectAll();
    }//GEN-LAST:event_port2ValueTextFieldActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ServoTest().setVisible(true);
            }
        });
    }
    
    /** called when device added */
    public void onAdd() {
        log.info("device added");
    }
    
    public void onRemove() {
        log.info("device removed, closing it");
        if(hwInterface!=null && hwInterface.isOpen()){
            hwInterface.close();
            servoTypeComboBox.setSelectedIndex(0);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel carServoPanel;
    private javax.swing.JPanel chooserPanel;
    private javax.swing.JTextField deadzoneSpeedTextFirld;
    private javax.swing.JTextField deadzoneSteeringTextFirld;
    private javax.swing.JButton disableButton;
    private javax.swing.JButton disableServo0Button;
    private javax.swing.JButton disableServo1Button;
    private javax.swing.JButton disableServo2Button;
    private javax.swing.JButton disableServo3Button;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JCheckBox liveSlidersCheckBox;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JTextField oscDelayTextField;
    private javax.swing.JTextField oscHighTextField;
    private javax.swing.JTextField oscLowTextField;
    private javax.swing.JRadioButton oscSelRadioButton0;
    private javax.swing.JRadioButton oscSelRadioButton1;
    private javax.swing.JRadioButton oscSelRadioButton2;
    private javax.swing.JRadioButton oscSelRadioButton3;
    private javax.swing.JToggleButton oscStartStopToggleButton;
    private javax.swing.JPanel oscillatePanel;
    private javax.swing.JLabel port2Label;
    private javax.swing.JPanel port2Panel;
    private javax.swing.JTextField port2ValueTextField;
    private javax.swing.JTextField radioLockoutTimeTextField;
    private javax.swing.JButton sendValuesButton;
    private javax.swing.JPanel servo0Panel;
    private javax.swing.JSlider servo0Slider;
    private javax.swing.JPanel servo1Panel;
    private javax.swing.JSlider servo1Slider;
    private javax.swing.JPanel servo2Panel;
    private javax.swing.JSlider servo2Slider;
    private javax.swing.JPanel servo3Panel;
    private javax.swing.JSlider servo3Slider;
    private javax.swing.JLabel servoFreqLabel;
    private javax.swing.JPanel servoFreqPanel;
    private javax.swing.JTextField servoFreqTextBox;
    private javax.swing.JComboBox servoTypeComboBox;
    private javax.swing.JPanel syncPanel;
    private javax.swing.JCheckBox synchronizeCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
