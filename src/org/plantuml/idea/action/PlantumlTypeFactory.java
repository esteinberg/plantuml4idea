package org.plantuml.idea.action;

import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author imamontov
 */
public class PlantumlTypeFactory extends FileTypeFactory {

    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(
                PlantumlFileType.PLANTUML_FILE_TYPE,
                new ExtensionFileNameMatcher(PlantumlFileType.PLANTUML_EXT));
    }
}