package org.plantuml.idea.lang.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlFileType;
import org.plantuml.idea.lang.PlantUmlLanguage;

/**
 * @author Eugene Steinberg
 */
public class PlantUmlFileImpl extends PsiFileBase {

    public PlantUmlFileImpl(FileViewProvider viewProvider) {
        super(viewProvider, PlantUmlLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return PlantUmlFileType.PLANTUML_FILE_TYPE;
    }
}
