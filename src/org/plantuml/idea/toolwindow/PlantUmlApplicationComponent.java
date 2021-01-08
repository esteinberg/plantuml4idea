package org.plantuml.idea.toolwindow;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.toolwindow.listener.PlantUmlCaretListener;
import org.plantuml.idea.toolwindow.listener.PlantUmlDocumentListener;

public class PlantUmlApplicationComponent implements ApplicationComponent {
    private DocumentListener plantUmlDocumentListener = new PlantUmlDocumentListener();
    private CaretListener plantUmlCaretListener = new PlantUmlCaretListener();

    public PlantUmlApplicationComponent() {
        PlantUmlApplicationComponent.class.getClassLoader().setDefaultAssertionStatus(false);
    }

    @Override
    public void initComponent() {
        EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        eventMulticaster.addDocumentListener(plantUmlDocumentListener);
        eventMulticaster.addCaretListener(plantUmlCaretListener);
    }

    @Override
    public void disposeComponent() {
        EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        eventMulticaster.removeDocumentListener(plantUmlDocumentListener);
        eventMulticaster.removeCaretListener(plantUmlCaretListener);
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "PlantUmlApplicationComponent";
    }

}
