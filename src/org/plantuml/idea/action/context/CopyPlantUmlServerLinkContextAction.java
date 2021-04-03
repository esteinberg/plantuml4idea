package org.plantuml.idea.action.context;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.util.ui.TextTransferable;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.preview.image.ImageContainer;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderingType;
import org.plantuml.idea.settings.PlantUmlSettings;

import javax.swing.*;

public class CopyPlantUmlServerLinkContextAction extends DumbAwareAction {

    public CopyPlantUmlServerLinkContextAction() {
        super("Copy as PlantUML Server link", "Generate PlantUML Server link to clipboard", AllIcons.Ide.Link);
    }

    public CopyPlantUmlServerLinkContextAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    @Override
    public void actionPerformed(final AnActionEvent e) {
        ImageContainer data = (ImageContainer) e.getData(ImageContainer.CONTEXT_COMPONENT);
        if (data != null) {
            ImageItem imageWithData = data.getImageItem();
            String source;
            if (imageWithData.getRenderingType() == RenderingType.PARTIAL) {
                source = imageWithData.getPageSource();
            } else {
                RenderRequest renderRequest = data.getRenderRequest();
                source = renderRequest.getSource();
            }

            try {
                String encoded = PlantUmlFacade.get().encode(source);
                PlantUmlSettings instance = PlantUmlSettings.getInstance();
                String serverUrl = instance.getServerPrefix();
                String serverClipboardLinkType = instance.getServerClipboardLinkType();
                CopyPasteManager.getInstance().setContents(new TextTransferable(serverUrl + "/" + serverClipboardLinkType + "/" + encoded));
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }


}
