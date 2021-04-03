package org.plantuml.idea.action.test;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.plantuml.SourceExtractor;
import org.plantuml.idea.preview.Zoom;

import java.io.File;
import java.util.Locale;

public class SaveTestAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        for (ImageFormat value : ImageFormat.values()) {
            try {
                PlantUmlFacade.get().renderAndSave(SourceExtractor.TESTDOT, new File("testData/version.puml"), value, "F:\\workspace\\_projekty\\plantuml4idea\\out\\" + value.name() + "." + value.name().toLowerCase(Locale.ROOT), null, new Zoom(100), 0);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }
}
