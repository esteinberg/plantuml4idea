package org.plantuml.idea.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.event.SelectionListener;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.toolwindow.listener.PlantUmlCaretListener;
import org.plantuml.idea.toolwindow.listener.PlantUmlDocumentListener;
import org.plantuml.idea.toolwindow.listener.PlantUmlSelectionListener;

public class PlantUmlApplicationComponent implements ApplicationComponent, Disposable {
    private DocumentListener plantUmlDocumentListener = new PlantUmlDocumentListener();
    private CaretListener plantUmlCaretListener = new PlantUmlCaretListener();
    private SelectionListener selectionListener = new PlantUmlSelectionListener();

    public PlantUmlApplicationComponent() {
        PlantUmlApplicationComponent.class.getClassLoader().setDefaultAssertionStatus(false);
    }

    @Override
    public void initComponent() {
        EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        eventMulticaster.addDocumentListener(plantUmlDocumentListener, this);
        eventMulticaster.addCaretListener(plantUmlCaretListener, this);
        eventMulticaster.addSelectionListener(selectionListener, this);
    }

    @Override
    public void disposeComponent() {
        EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        eventMulticaster.removeDocumentListener(plantUmlDocumentListener);
        eventMulticaster.removeCaretListener(plantUmlCaretListener);
        eventMulticaster.removeSelectionListener(selectionListener);
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "PlantUmlApplicationComponent";
    }

    @Override
    public void dispose() {
    }
}
