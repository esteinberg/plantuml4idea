package org.plantuml.idea.preview.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlLanguage;

public class PlantUmlSplitEditorProvider implements AsyncFileEditorProvider, DumbAware {
    private static final String FIRST_EDITOR = "first_editor";
    private static final String SECOND_EDITOR = "second_editor";
    private static final String SPLIT_LAYOUT = "split_layout";
    private static final String VERTICAL_SPLIT = "vertical_split";

    @NotNull
    private final FileEditorProvider myFirstProvider;
    @NotNull
    private final FileEditorProvider mySecondProvider;
    @NotNull
    private final String myEditorTypeId;

    public PlantUmlSplitEditorProvider() {
        this.myFirstProvider = new PsiAwareTextEditorProvider();
        this.mySecondProvider = new PlantUmlPreviewEditorProvider(false);

        this.myEditorTypeId = "split-provider[" + PlantUmlSplitEditorProvider.this.myFirstProvider.getEditorTypeId() + ";" + PlantUmlSplitEditorProvider.this.mySecondProvider.getEditorTypeId() + "]";
        // when this plugin is installed at runtime, check if the existing editors as split editors.
        // If not, close the editor and re-open the files
        // on startup, the list of files is always empty (it is still being restored)
        ApplicationManager.getApplication().invokeLater(() -> {
            // run later, to avoid a cyclic dependency plugin is dynamically loaded
            for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                FileEditorManager fem = FileEditorManager.getInstance(project);
                PsiManager pm = PsiManager.getInstance(project);
                for (FileEditor editor : fem.getAllEditors()) {
                    if (!(editor instanceof PlantUmlSplitEditor)) {
                        VirtualFile vFile = editor.getFile();
                        if (vFile != null) {
                            PsiFile pFile = pm.findFile(vFile);
                            if (pFile != null) {
                                if (pFile.getLanguage() == PlantUmlLanguage.INSTANCE) {
                                    // an PlantUml file in a non-split editor, close and re-open the file to enforce split editor
                                    ApplicationManager.getApplication().runWriteAction(() -> {
                                        // closing the file might trigger a save, therefore wrap in write action
                                        fem.closeFile(vFile);
                                        fem.openFile(vFile, false);
                                    });
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @NotNull
    private static Builder getBuilderFromEditorProvider(@NotNull final FileEditorProvider provider,
                                                        @NotNull final Project project,
                                                        @NotNull final VirtualFile file) {
        if (provider instanceof AsyncFileEditorProvider) {
            return ((AsyncFileEditorProvider) provider).createEditorAsync(project, file);
        } else {
            return new Builder() {
                @Override
                public FileEditor build() {
                    return provider.createEditor(project, file);
                }
            };
        }
    }

    protected FileEditor createSplitEditor(@NotNull final FileEditor firstEditor, @NotNull FileEditor secondEditor) {
        if (!(firstEditor instanceof TextEditor) || !(secondEditor instanceof PlantUmlPreviewEditor)) {
            throw new IllegalArgumentException("Main editor should be TextEditor");
        }
        PlantUmlPreviewEditor asciiDocPreviewEditor = (PlantUmlPreviewEditor) secondEditor;
        asciiDocPreviewEditor.setEditor(((TextEditor) firstEditor).getEditor());
        return new PlantUmlSplitEditor(((TextEditor) firstEditor), ((PlantUmlPreviewEditor) secondEditor));
    }

    @Override
    public void disposeEditor(@NotNull FileEditor fileEditor) {
        // default -- needed for IntelliJ IDEA 15 compatibility
        Disposer.dispose(fileEditor);
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return myFirstProvider.accept(project, file) && mySecondProvider.accept(project, file);
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return createEditorAsync(project, file).build();
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return myEditorTypeId;
    }

    @NotNull
    @Override
    public Builder createEditorAsync(@NotNull final Project project, @NotNull final VirtualFile file) {
        final Builder firstBuilder = getBuilderFromEditorProvider(myFirstProvider, project, file);
        final Builder secondBuilder = getBuilderFromEditorProvider(mySecondProvider, project, file);

        return new Builder() {
            @Override
            public FileEditor build() {
                return createSplitEditor(firstBuilder.build(), secondBuilder.build());
            }
        };
    }

    @NotNull
    @Override
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
        Element child = sourceElement.getChild(FIRST_EDITOR);
        FileEditorState firstState = null;
        if (child != null) {
            firstState = myFirstProvider.readState(child, project, file);
        }
        child = sourceElement.getChild(SECOND_EDITOR);
        FileEditorState secondState = null;
        if (child != null) {
            secondState = mySecondProvider.readState(child, project, file);
        }

        final Attribute attribute = sourceElement.getAttribute(SPLIT_LAYOUT);

        final String layoutName;
        if (attribute != null) {
            layoutName = attribute.getValue();
        } else {
            layoutName = null;
        }

        final Attribute verticalAttribute = sourceElement.getAttribute(SPLIT_LAYOUT);
        boolean vertical = true;
        if (verticalAttribute != null) {
            try {
                vertical = verticalAttribute.getBooleanValue();
            } catch (DataConversionException e) {
            }
        }

        return new SplitFileEditor.MyFileEditorState(layoutName, vertical, firstState, secondState);
    }

    @Override
    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        if (!(state instanceof SplitFileEditor.MyFileEditorState)) {
            return;
        }
        final SplitFileEditor.MyFileEditorState compositeState = (SplitFileEditor.MyFileEditorState) state;

        Element child = new Element(FIRST_EDITOR);
        if (compositeState.getFirstState() != null) {
            myFirstProvider.writeState(compositeState.getFirstState(), project, child);
            targetElement.addContent(child);
        }

        child = new Element(SECOND_EDITOR);
        if (compositeState.getSecondState() != null) {
            mySecondProvider.writeState(compositeState.getSecondState(), project, child);
            targetElement.addContent(child);
        }

        if (compositeState.getSplitLayout() != null) {
            targetElement.setAttribute(SPLIT_LAYOUT, compositeState.getSplitLayout());
        }
        targetElement.setAttribute(VERTICAL_SPLIT, String.valueOf(compositeState.isVerticalSplit()));
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
