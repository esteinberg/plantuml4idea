package org.plantuml.idea.lang.settings;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.HighlighterFactory;
import org.plantuml.idea.lang.PlantUmlFileType;

import javax.swing.*;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Max Gorbunov
 */
public class PlantUmlColorsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] ATTRS;

    static {
        HashSet<TextAttributesKey> keys = new HashSet<TextAttributesKey>(HighlighterFactory.MAP.values());
        ATTRS = new AttributesDescriptor[keys.size()];
        int i = 0;
        for (TextAttributesKey key : keys) {
            String name = key.getExternalName().toLowerCase();
            if (name.startsWith("plantuml.")) {
                name = name.substring("plantuml.".length());
            }
            name = name.replace('_', ' ');
            name = StringUtil.capitalize(name);
            ATTRS[i++] = new AttributesDescriptor(name, key);
        }
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return PlantUmlFileType.PLANTUML_ICON;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return HighlighterFactory.SYNTAX_HIGHLIGHTER;
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "@startuml\n"
                + "!pragma labelangle 90\n"
                + "\n"
                + "minwidth 100\n"
                + "\n"
                + "' class List may contain many items\n"
                + "List \"1\" *-- \"many\" ListItem\n"
                + "\n"
                + "@enduml";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "PlantUML";
    }
}
