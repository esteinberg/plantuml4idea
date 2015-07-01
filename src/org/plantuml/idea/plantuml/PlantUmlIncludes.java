package org.plantuml.idea.plantuml;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.ui.UIUtil;
import net.sourceforge.plantuml.BlockUmlBuilder;
import net.sourceforge.plantuml.preproc.Defines;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.intellij.openapi.vfs.CharsetToolkit.UTF8;

public class PlantUmlIncludes {

    private static final Logger logger = Logger.getInstance(PlantUmlIncludes.class);

    public static Set<File> commitIncludes(String source, @Nullable File baseDir) {
        try {
            if (baseDir != null) {
                BlockUmlBuilder blockUmlBuilder = new BlockUmlBuilder(Collections.<String>emptyList(), UTF8, new Defines(), new StringReader(source), baseDir, null);
                final Set<File> includedFiles = blockUmlBuilder.getIncludedFiles();
                if (!includedFiles.isEmpty()) {
                    saveModifiedFiles(includedFiles);
                }
                return includedFiles;
            }
        } catch (IOException e) {
            logger.error(source + "; baseDir=" + baseDir.getAbsolutePath(), e);
        }
        return Collections.emptySet();
    }

    private static void saveModifiedFiles(final Set<File> files) {
        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            @Override
            public void run() {
                FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
                Set<Document> unsavedDocuments = getUnsavedDocuments(files, fileDocumentManager);
                for (Document unsavedDocument : unsavedDocuments) {
                    fileDocumentManager.saveDocument(unsavedDocument);
                }
            }
        });
    }

    @NotNull
    private static Set<Document> getUnsavedDocuments(Set<File> files, FileDocumentManager fileDocumentManager) {
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        Set<Document> unsavedDocuments = new HashSet<Document>();
        for (File file : files) {
            VirtualFile virtualFile = virtualFileManager.findFileByUrl("file://" + file.getAbsolutePath());
            if (virtualFile != null) {
                // allowed from event dispatch thread or inside read-action only
                Document document = fileDocumentManager.getDocument(virtualFile);
                if (document != null) {
                    if (fileDocumentManager.isDocumentUnsaved(document)) {
                        unsavedDocuments.add(document);
                    }
                }
            }
        }
        return unsavedDocuments;
    }
}
