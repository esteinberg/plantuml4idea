package org.plantuml.idea.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.util.ui.TextTransferable;
import net.sourceforge.plantuml.code.Transcoder;
import net.sourceforge.plantuml.code.TranscoderUtil;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.rendering.ImageItem;
import org.plantuml.idea.rendering.RenderRequest;
import org.plantuml.idea.rendering.RenderingType;
import org.plantuml.idea.toolwindow.PlantUmlLabel;

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
        PlantUmlLabel data = (PlantUmlLabel) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if (data != null) {
            ImageItem imageWithData = data.getImageWithData();
            String source;
            if (imageWithData.getRenderingType() == RenderingType.PARTIAL) {
                source = imageWithData.getPageSource();
            } else {
                RenderRequest renderRequest = data.getRenderRequest();
                source = renderRequest.getSource();
            }

            try {
                Transcoder defaultTranscoder = TranscoderUtil.getDefaultTranscoder();
                String encoded = defaultTranscoder.encode(source);
                CopyPasteManager.getInstance().setContents(new TextTransferable("http://plantuml.com/plantuml/uml/" + encoded));
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }


}
