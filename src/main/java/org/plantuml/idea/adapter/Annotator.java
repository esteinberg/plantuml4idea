package org.plantuml.idea.adapter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.plantuml.BlockUml;
import net.sourceforge.plantuml.ErrorUml;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.UmlDiagram;
import net.sourceforge.plantuml.core.Diagram;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.syntax.SyntaxResult;
import net.sourceforge.plantuml.utils.LineLocation;
import net.sourceforge.plantuml.utils.LineLocationImpl;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.lang.annotator.ErrorSourceAnnotation;
import org.plantuml.idea.lang.annotator.SourceAnnotation;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static org.plantuml.idea.lang.annotator.LanguageDescriptor.IDEA_DISABLE_SYNTAX_CHECK;

public class Annotator {

    private static final Logger logger = Logger.getInstance(Annotator.class);

    @Nullable
    public static Collection<SourceAnnotation> annotateSyntaxErrors(Project project, String source, VirtualFile virtualFile) {
        if (source.contains(IDEA_DISABLE_SYNTAX_CHECK)) {
            return Collections.emptyList();
        }
        Collection<SourceAnnotation> result = new ArrayList<SourceAnnotation>();
        long start = currentTimeMillis();
        SyntaxResult syntaxResult = checkSyntax(project, source, virtualFile);
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

    private static SyntaxResult checkSyntax(Project project, String source, VirtualFile virtualFile) {
        File baseDir = UIUtils.getParent(virtualFile);
        if (baseDir != null) {
            Utils.setPlantUmlDir(baseDir);

            Utils.saveAllDocuments(project, virtualFile == null ? null : virtualFile.getPath());
        } else {
            Utils.resetPlantUmlDir();
        }
        return checkSyntaxFair(source);
    }

    /**
     * from net.sourceforge.plantuml.syntax.SyntaxChecker
     */
    public static SyntaxResult checkSyntaxFair(String source) {
        final SyntaxResult result = new SyntaxResult();
        final SourceStringReader sourceStringReader = new SourceStringReader(Defines.createEmpty(), source,
                PlantUmlSettings.getInstance().getConfigAsList());

        final List<BlockUml> blocks = sourceStringReader.getBlocks();
        if (blocks.size() == 0) {
            result.setError(true);
            result.setLineLocation(lastLineNumber(source));
            result.addErrorText("No @enduml found");
            return result;
        }

        final Diagram system = blocks.get(0).getDiagram();
        result.setCmapData(system.hasUrl());
        if (system instanceof UmlDiagram) {
            result.setUmlDiagramType(((UmlDiagram) system).getUmlDiagramType());
            result.setDescription(system.getDescription().getDescription());
        } else if (system instanceof PSystemError) {
            result.setError(true);
            final PSystemError sys = (PSystemError) system;
            result.setLineLocation(sys.getLineLocation());
            for (ErrorUml er : sys.getErrorsUml()) {
                result.addErrorText(er.getError());
            }
            result.setSystemError(sys);
        } else {
            result.setDescription(system.getDescription().getDescription());
        }
        return result;
    }

    private static LineLocation lastLineNumber(String source) {
        LineLocationImpl result = new LineLocationImpl("", null).oneLineRead();
        for (int i = 0; i < source.length(); i++)
            if (source.charAt(i) == '\n')
                result = result.oneLineRead();

        return result;
    }
}
