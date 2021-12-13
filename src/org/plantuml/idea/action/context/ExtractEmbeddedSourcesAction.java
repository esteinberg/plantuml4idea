package org.plantuml.idea.action.context;

import com.intellij.ide.scratch.ScratchRootType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.plantuml.SourceExtractor;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.StringReader;
import java.util.Map;

public class ExtractEmbeddedSourcesAction extends DumbAwareAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (file == null) {
            return;
        }
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        String extracted = null;
        if ("svg".equalsIgnoreCase(file.getExtension())) {
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
                String text = document.getText();
                extracted = extractSourceFromSvg(text);
            }
        } else if ("png".equalsIgnoreCase(file.getExtension())) {
            //Extract source from PNG-metadata
            extracted = extractSourceFromPng(VfsUtil.virtualToIoFile(file));
        }

        if (extracted != null) {
            // Remove other code after @enduml
            extracted = SourceExtractor.extractSource(extracted, 0);
            // Open extracted source in new scratch file
            VirtualFile scratchFile = ScratchRootType.getInstance().createScratchFile(project, file.getNameWithoutExtension() + ".puml", null, extracted);
            if (scratchFile != null) {
                FileEditorManager.getInstance(project).openFile(scratchFile, true);
            }
        } else {
            Messages.showErrorDialog(project, "Image does not contain PlantUML metadata.", "Extract PlantUML Source");
        }
    }

    protected String extractSourceFromPng(File file) {
        return PlantUmlFacade.get().extractEmbeddedSourceFromImage(file);
    }

    protected String extractSourceFromSvg(String text) {
        String extracted = null;
        try {
            String comment = null;
            StringReader reader = new StringReader(text);
            XMLStreamReader xr = XMLInputFactory.newInstance().createXMLStreamReader(reader);
            while (xr.hasNext()) {
                if (xr.next() == XMLStreamConstants.COMMENT) {
                    comment = xr.getText();
                }
            }
            if (comment != null) {
                Map<Integer, String> sources = SourceExtractor.extractSources(comment);
                for (Map.Entry<Integer, String> entry : sources.entrySet()) {
                    extracted = entry.getValue();
                    break;
                }
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return extracted;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean visible = false;
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
        if (file != null) {
            String extension = file.getExtension();
            visible = "svg".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension);
        }
        e.getPresentation().setVisible(visible);
    }
}
