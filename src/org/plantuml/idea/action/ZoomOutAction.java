package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author Eugene Steinberg
 */
public class ZoomOutAction extends ZoomAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        setZoom(e.getProject(), Math.max(MIN_ZOOM, getZoom(e.getProject()) - ZOOM_STEP));
    }
}
