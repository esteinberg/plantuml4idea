package org.plantuml.idea.intentions;

import com.intellij.codeInsight.daemon.LightIntentionActionTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ReverseArrowIntentionTest extends LightIntentionActionTestCase {

    public void test() throws Exception {
        doAllTests();
    }

    @NotNull
    @Override
    protected String getTestDataPath() {
        //watch out for working directory with which you run this test
        return new File("").getAbsolutePath();
    }

    @Override
    protected String getBasePath() {
        return "/testData/reverseArrowIntention";
    }

    @Override
    protected boolean shouldBeAvailableAfterExecution() {
        return true;
    }
}