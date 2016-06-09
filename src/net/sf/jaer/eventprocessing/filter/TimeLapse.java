/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jaer.eventprocessing.filter;

import com.jogamp.opengl.GLAutoDrawable;
import eu.seebetter.ini.chips.DavisChip;
import java.util.Timer;
import java.util.TimerTask;
import net.sf.jaer.Description;
import net.sf.jaer.chip.AEChip;
import net.sf.jaer.event.EventPacket;
import net.sf.jaer.eventprocessing.EventFilter2D;
import net.sf.jaer.graphics.FrameAnnotater;

/**
 * Exposes DAVIS frames at desired (and low) frame rate for a time lapse movie
 * mode.
 *
 * @author tobid
 */
@Description("Exposes DAVIS frames at desired (and low) frame rate for a time lapse movie mode")
public class TimeLapse extends EventFilter2D implements FrameAnnotater {

    private float frameRateHz = getFloat("frameRateHz", 1);
    private Timer frameTimer = null;

    public TimeLapse(AEChip chip) {
        super(chip);
    }

    @Override
    public EventPacket<?> filterPacket(EventPacket<?> in) {

        return in;
    }

    @Override
    public void resetFilter() {
    }

    @Override
    public void initFilter() {
    }

    @Override
    public void annotate(GLAutoDrawable drawable) {
    }

    /**
     * @return the frameRateHz
     */
    public float getFrameRateHz() {
        return frameRateHz;
    }

    /**
     * @param frameRateHz the frameRateHz to set
     */
    public void setFrameRateHz(float frameRateHz) {
        this.frameRateHz = frameRateHz;
        putFloat("frameRateHz", frameRateHz);
    }

    @Override
    public synchronized void setFilterEnabled(boolean yes) {

        if (frameTimer != null) {
            frameTimer.cancel();
        }
        if (yes) {
            if (!(chip instanceof DavisChip)) {
                log.warning("Only works with a DavisChip");
                setFilterEnabled(false);
                return;
            }
            if (frameTimer == null) {
                frameTimer = new Timer("TimeLapseFrameTimer", true);
            }
            FrameCaptureTask tt = new FrameCaptureTask((DavisChip) chip);
            frameTimer.scheduleAtFixedRate(tt, 0, (int) (1e3 / frameRateHz));

        }

    }

    private class FrameCaptureTask extends TimerTask {

        DavisChip davisChip;

        public FrameCaptureTask(DavisChip davisChip) {
            this.davisChip = davisChip;
        }

        @Override
        public void run() {
            davisChip.takeSnapshot();
        }
    }
}