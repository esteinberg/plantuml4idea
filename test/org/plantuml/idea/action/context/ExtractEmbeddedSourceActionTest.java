package org.plantuml.idea.action.context;

import com.intellij.openapi.vfs.CharsetToolkit;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ExtractEmbeddedSourceActionTest {

    @Test
    public void extractSourceFromSvg() throws IOException {
        String text = FileUtils.readFileToString(new File("testData/test.svg"), CharsetToolkit.UTF8);
        Assert.assertNotNull(new ExtractEmbeddedSourcesAction().extractSourceFromSvg(text));
    }

    @Test
    public void extractSourceFromPng() throws IOException {
        File file = new File("testData/test.png");
        Assert.assertNotNull(new ExtractEmbeddedSourcesAction().extractSourceFromPng(file));
    }
}