package org.plantuml.idea.toolwindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.scale.ScaleContext;
import com.intellij.ui.scale.ScaleType;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.settings.PlantUmlSettings;

import javax.swing.*;

public class Zoom {
    private static final Logger LOG = Logger.getInstance(Zoom.class);

    private final int unscaledZoom;
    private final int scaledZoom;
    private boolean displaySvg;
    private double systemScale;

    /**
     * use a component in hierarchy
     */
    public Zoom(@Nullable JComponent context, int unscaledZoom) {
        this.unscaledZoom = unscaledZoom;
        systemScale = getSystemScale(context);
        LOG.debug("systemScale=", systemScale);
        scaledZoom = (int) (unscaledZoom * systemScale);
        displaySvg = PlantUmlSettings.getInstance().isDisplaySvg();
    }

    public Zoom(int unscaledZoom) {
        this(null, unscaledZoom);
    }

    public int getUnscaledZoom() {
        return unscaledZoom;
    }

    public Double getDoubleUnScaledZoom() {
        return (double) unscaledZoom / 100;
    }

    public Double getDoubleScaledZoom() {
        return (double) scaledZoom / 100;
    }

    public int getScaledZoom() {
        return scaledZoom;
    }

    private double getSystemScale(JComponent context) {
        try {
            return ScaleContext.create(context).getScale(ScaleType.SYS_SCALE);
        } catch (Throwable e) {
            LOG.debug(e);
            return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (displaySvg) {
            return true;
        }
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Zoom zoom = (Zoom) o;

        return scaledZoom == zoom.scaledZoom;
    }

    @Override
    public int hashCode() {
        return scaledZoom;
    }

    @Override
    public String toString() {
        return "Zoom{" +
                "unscaledZoom=" + unscaledZoom +
                ", scaledZoom=" + scaledZoom +
                ", displaySvg=" + displaySvg +
                '}';
    }

    public void updateSettings() {
        displaySvg = PlantUmlSettings.getInstance().isDisplaySvg();
    }


}
