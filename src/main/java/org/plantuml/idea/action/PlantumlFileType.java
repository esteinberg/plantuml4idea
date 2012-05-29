package org.plantuml.idea.action;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PlantumlFileType extends LanguageFileType {

    protected PlantumlFileType() {
        super(new PlantumlLanguage());
    }

    @NotNull
    @Override
    public String getName() {
        return "SequenceDiagram";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "PlantUML diagram files";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "plantuml";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getCharset(@NotNull VirtualFile virtualFile, byte[] bytes) {
        return null;
    }
}