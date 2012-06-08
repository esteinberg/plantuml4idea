package org.plantuml.idea.action;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PlantumlFileType extends LanguageFileType {

    public static final PlantumlFileType PLANTUML_FILE_TYPE = new PlantumlFileType();

    public static final String PLANTUML_EXT = ".puml";

    private PlantumlFileType() {
        super(new PlantumlLanguage());
    }

    @NotNull
    @Override
    public String getName() {
        return "PLAIN_TEXT";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "PlantUML diagram files";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return PLANTUML_EXT;
    }

    @Override
    public Icon getIcon() {
        return IconLoader.getIcon("/images/uml.gif");
    }

    @Override
    public String getCharset(@NotNull VirtualFile virtualFile, byte[] bytes) {
        return "UTF-8";
    }
}