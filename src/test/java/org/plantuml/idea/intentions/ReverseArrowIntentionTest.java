package org.plantuml.idea.intentions;

import com.intellij.codeInsight.daemon.LightIntentionActionTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;

import java.io.File;

/**
 * The first test fails with some SDKs and JDK 1.6 - IDEA-141019
 * If you get "Invalid home path '...plugins-sandbox/test'" then create the 'test' folder
 */
@Ignore
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
        return "src/test/resources/testData/reverseArrowIntention";
    }

    @Override
    protected boolean shouldBeAvailableAfterExecution() {
        return true;
    }
}