package org.plantuml.idea.lang;

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.usageView.UsageInfo;
import org.junit.Ignore;
import org.plantuml.idea.grammar.psi.PumlItem;

import java.util.Collection;

@Ignore
public class SimpleCodeInsightTest extends LightJavaCodeInsightFixtureTestCase {

    /**
     * @return path to test data file directory relative to working directory in the run configuration for this test.
     */
    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testData";
    }


    public void testAnnotator() {
        myFixture.configureByFiles("DefaultTestData.puml");
        myFixture.checkHighlighting(false, false, true, true);
    }

//  public void testFormatter() {
//    myFixture.configureByFile("FormatterTestData.simple");
//    CodeStyle.getLanguageSettings(myFixture.getFile()).SPACE_AROUND_ASSIGNMENT_OPERATORS = true;
//    CodeStyle.getLanguageSettings(myFixture.getFile()).KEEP_BLANK_LINES_IN_CODE = 2;
//    WriteCommandAction.writeCommandAction(getProject()).run(() ->
//            CodeStyleManager.getInstance(getProject()).reformatText(
//                    myFixture.getFile(),
//                    ContainerUtil.newArrayList(myFixture.getFile().getTextRange())
//            )
//    );
//    myFixture.checkResultByFile("DefaultTestData.puml");
//  }

    public void testRename() {
        myFixture.configureByFiles("DefaultTestData.puml");
        myFixture.renameElementAtCaret("Foo:");
        myFixture.checkResultByFile("DefaultTestData.puml", "DefaultTestDataAfter.puml", false);
    }

//  public void testFolding() {
//    myFixture.configureByFile("DefaultTestData.puml");
//    myFixture.testFolding(getTestDataPath() + "/FoldingTestData.java");
//  }

    public void testFindUsages() {
        Collection<UsageInfo> usageInfos = myFixture.testFindUsages("DefaultTestData.puml");
        assertEquals(3, usageInfos.size());
    }

    public void testCommenter() {
        myFixture.configureByText(PlantUmlFileType.INSTANCE, "<caret>foo");
        CommentByLineCommentAction commentAction = new CommentByLineCommentAction();
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("'foo");
        commentAction.actionPerformedImpl(getProject(), myFixture.getEditor());
        myFixture.checkResult("foo");
    }

    public void testReference() {
        myFixture.configureByFiles("DefaultTestData.puml");
        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset()).getParent();
        String text = element.getText();

        assertEquals("Bob", ((PumlItem) element.getReferences()[0].resolve()).getText());
    }

}
