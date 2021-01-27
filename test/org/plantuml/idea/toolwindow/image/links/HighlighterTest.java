package org.plantuml.idea.toolwindow.image.links;

import org.junit.Assert;
import org.junit.Test;

public class HighlighterTest {

    @Test
    public void sanitize() {
        Assert.assertEquals("Alice", Highlighter.sanitize("Alice"));
        Assert.assertEquals("Alice", Highlighter.sanitize("[Alice]"));
        Assert.assertEquals("Alice", Highlighter.sanitize(" Alice "));
        Assert.assertEquals("  ", Highlighter.sanitize("  "));
        Assert.assertEquals("***", Highlighter.sanitize("***"));
    }
}