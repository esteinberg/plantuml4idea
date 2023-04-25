package org.plantuml.idea.action.test;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.ImageFormat;
import org.plantuml.idea.plantuml.SourceExtractor;
import org.plantuml.idea.preview.Zoom;
import org.plantuml.idea.settings.PlantUmlSettings;

import java.io.File;
import java.util.Locale;

public class SaveTestAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        for (ImageFormat value : ImageFormat.values()) {
            try {
                PlantUmlFacade.get().renderAndSave(null, SourceExtractor.TESTDOT, new File("src/test/resources/testData/version.puml"), value, "F:\\workspace\\_projekty\\plantuml4idea\\out\\" + value.name() + "." + value.name().toLowerCase(Locale.ROOT), null, new Zoom(100, PlantUmlSettings.getInstance()), 0);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }
}
