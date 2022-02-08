package org.plantuml.idea.lang;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author imamontov
 */
public class PlantUmlTypeFactory extends FileTypeFactory {

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(
                PlantUmlFileType.INSTANCE,
                PlantUmlFileType.PLANTUML_EXT);
        consumer.consume(
                PlantUmlFileType.INSTANCE,
                PlantUmlFileType.PLANTUML_EXT_2);
        consumer.consume(
                PlantIUmlFileType.INSTANCE,
                PlantIUmlFileType.PLANTUML_EXT);
    }
}