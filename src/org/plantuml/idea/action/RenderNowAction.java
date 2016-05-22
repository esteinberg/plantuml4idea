package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.plantuml.idea.rendering.LazyApplicationPoolExecutor;
import org.plantuml.idea.toolwindow.PlantUmlToolWindow;
import org.plantuml.idea.util.UIUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @author Eugene Steinberg
 */
public class RenderNowAction extends DumbAwareAction {


    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project != null) {
            PlantUmlToolWindow plantUmlToolWindow = UIUtils.getPlantUmlToolWindow(project);
            if (plantUmlToolWindow != null) {
                plantUmlToolWindow.renderLater(LazyApplicationPoolExecutor.Delay.NOW);
            }
        }
    }

    public static void main(String[] args) {
        new SimpleDateFormat("MM-dd-u", Locale.ROOT);
    }
}
