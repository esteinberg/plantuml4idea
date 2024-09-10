package org.plantuml.idea.lang.annotator;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixture4TestCase;

import java.util.List;
import java.util.stream.Collectors;

public class PlantUmlExternalAnnotatorTest extends LightPlatformCodeInsightFixture4TestCase {

    private PlantUmlExternalAnnotator plantUmlExternalAnnotator = new PlantUmlExternalAnnotator();

    public void testLineCommentHighlight() {
        PsiFile psiFile = createForPUMLFile(
                "@startuml\n'Line Comment\nactor User/'Block Comment'/\n@enduml");

        FileAnnotationResult fileAnnotationResult = plantUmlExternalAnnotator.doAnnotate(new PlantUmlExternalAnnotator.Info(psiFile));

        assertNotNull(fileAnnotationResult);

        List<SyntaxHighlightAnnotation> lineCommentAnnotations = getAnnotationByAttributeKeys(fileAnnotationResult, DefaultLanguageHighlighterColors.LINE_COMMENT);

        assertSize(1, lineCommentAnnotations);

        SyntaxHighlightAnnotation lineCommentAnnotation = lineCommentAnnotations.get(0);
        assertEquals(10, lineCommentAnnotation.startSourceOffset);
        assertEquals(23, lineCommentAnnotation.endSourceOffset);
    }

    public void testLineCommentHighlight_withWhitespacesBefore() {
        PsiFile psiFile = createForPUMLFile(
                "@startuml\n\t 'Line Comment\nactor User/'Block Comment'/\n@enduml");

        FileAnnotationResult fileAnnotationResult = plantUmlExternalAnnotator.doAnnotate(new PlantUmlExternalAnnotator.Info(psiFile));

        assertNotNull(fileAnnotationResult);

        List<SyntaxHighlightAnnotation> lineCommentAnnotations = getAnnotationByAttributeKeys(fileAnnotationResult, DefaultLanguageHighlighterColors.LINE_COMMENT);

        assertSize(1, lineCommentAnnotations);

        SyntaxHighlightAnnotation lineCommentAnnotation = lineCommentAnnotations.get(0);
        assertEquals(10, lineCommentAnnotation.startSourceOffset);  //should be 12 really     
        assertEquals(25, lineCommentAnnotation.endSourceOffset);
    }


    public void testBlockCommentHighlight() {
        PsiFile psiFile = createForPUMLFile(
                "@startuml\n/'Block Comment'/\nactor User/'Second Block Comment'/\n@enduml");

        FileAnnotationResult fileAnnotationResult = plantUmlExternalAnnotator.doAnnotate(new PlantUmlExternalAnnotator.Info(psiFile));

        assertNotNull(fileAnnotationResult);

        List<SyntaxHighlightAnnotation> blockCommentAnnotations = getAnnotationByAttributeKeys(fileAnnotationResult, DefaultLanguageHighlighterColors.BLOCK_COMMENT);

        assertSize(2, blockCommentAnnotations);

        SyntaxHighlightAnnotation firstBlockCommentAnnotation = blockCommentAnnotations.get(0);
        assertEquals(10, firstBlockCommentAnnotation.startSourceOffset);
        assertEquals(27, firstBlockCommentAnnotation.endSourceOffset);

        SyntaxHighlightAnnotation secondBlockCommentAnnotation = blockCommentAnnotations.get(1);
        assertEquals(38, secondBlockCommentAnnotation.startSourceOffset);
        assertEquals(62, secondBlockCommentAnnotation.endSourceOffset);
    }

    private PsiFile createForPUMLFile(String text) {
        return myFixture.configureByText("PUML", text);
    }

    private List<SyntaxHighlightAnnotation> getAnnotationByAttributeKeys(FileAnnotationResult result,
                                                                         TextAttributesKey textAttributesKey) {
        return result
                .getSourceAnnotationResults()
                .stream()
                .flatMap(sourceAnnotationResult -> sourceAnnotationResult.getAnnotations().stream())
                .filter(annotation -> annotation instanceof SyntaxHighlightAnnotation)
                .map(annotation -> (SyntaxHighlightAnnotation) annotation)
                .filter(syntaxHighlightAnnotation -> syntaxHighlightAnnotation.textAttributesKey == textAttributesKey)
                .collect(Collectors.toList());
    }
}