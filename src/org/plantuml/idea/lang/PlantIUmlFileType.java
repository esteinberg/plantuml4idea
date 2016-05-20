package org.plantuml.idea.lang;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PlantIUmlFileType extends LanguageFileType {

    public static final PlantIUmlFileType PLANTUML_FILE_TYPE = new PlantIUmlFileType();

    public static final String PLANTUML_EXT = "iuml";

    public static final Icon PLANTUML_ICON = IconLoader.getIcon("/images/uml.png");

    private PlantIUmlFileType() {
        super(PlantUmlLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "PlantUML include file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "PlantUML diagram include files";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return PLANTUML_EXT;
    }

    @Override
    public Icon getIcon() {
        return PLANTUML_ICON;
    }

    @Override
    public String getCharset(@NotNull VirtualFile virtualFile, byte[] bytes) {
        return "UTF-8";
    }
}