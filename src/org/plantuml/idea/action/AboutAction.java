package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.plantuml.idea.AboutDialog;

import java.awt.*;

/**
 * @author Eugene Steinberg
 */
public class AboutAction extends DumbAwareAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        AboutDialog aboutDialog = new AboutDialog(e, e.getProject());
        aboutDialog.pack();
        Point centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        Point topLeftPoint = new Point((centerPoint.x - aboutDialog.getWidth() / 2),
                centerPoint.y - aboutDialog.getHeight() / 2);
        aboutDialog.setLocation(topLeftPoint);
        aboutDialog.setVisible(true);
    }
}
