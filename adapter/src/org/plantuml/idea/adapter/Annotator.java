package org.plantuml.idea.adapter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import net.sourceforge.plantuml.syntax.SyntaxChecker;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.annotator.ErrorSourceAnnotation;
import org.plantuml.idea.lang.annotator.SourceAnnotation;
import org.plantuml.idea.util.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static java.lang.System.currentTimeMillis;
import static org.plantuml.idea.lang.annotator.LanguageDescriptor.IDEA_DISABLE_SYNTAX_CHECK;

public class Annotator {

    private static final Logger logger = Logger.getInstance(Annotator.class);

    @Nullable
    public static Collection<SourceAnnotation> annotateSyntaxErrors(PsiFile file, String source) {
        if (source.contains(IDEA_DISABLE_SYNTAX_CHECK)) {
            return Collections.emptyList();
        }
        Collection<SourceAnnotation> result = new ArrayList<SourceAnnotation>();
        long start = currentTimeMillis();
        SyntaxResult syntaxResult = checkSyntax(file, source);
        logger.debug("syntax checked in ", currentTimeMillis() - start, "ms");

        if (syntaxResult.isError()) {
            String beforeInclude = StringUtils.substringBefore(source, "!include");
            int includeLineNumber = StringUtils.splitPreserveAllTokens(beforeInclude, "\n").length;
            //todo hack because plantuml returns line number from source with inlined includes
            if (syntaxResult.getLineLocation().getPosition() < includeLineNumber) {
                ErrorSourceAnnotation errorSourceAnnotation = new ErrorSourceAnnotation(
                        syntaxResult.getErrors(),
                        null,     // syntaxResult.getSuggest(),   missing since version 1.2018.7
                        syntaxResult.getLineLocation().getPosition()
                );
                result.add(errorSourceAnnotation);
            }
        }
        return result;
    }

    private static SyntaxResult checkSyntax(PsiFile file, String source) {
        File baseDir = UIUtils.getParent(file.getVirtualFile());
        if (baseDir != null) {
            Utils.setPlantUmlDir(baseDir);

            VirtualFile virtualFile = file.getVirtualFile();
            Utils.saveAllDocuments(virtualFile==null?null:virtualFile.getPath());
        } else {
            Utils.resetPlantUmlDir();
        }
        return SyntaxChecker.checkSyntaxFair(source);
    }
}
