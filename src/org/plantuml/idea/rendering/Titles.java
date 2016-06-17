package org.plantuml.idea.rendering;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;


public class Titles {
    protected static final Logger logger = Logger.getInstance(Titles.class);
    
    private final List<String> titles;

    public Titles(List<String> titles) {
        this.titles = titles;
    }

    public int size() {
        return titles.size();
    }

    public String get(int i) {
        if (titles.size() > i) {
            return titles.get(i);
        }
        logger.debug("no title, for page ", i, ", titles=", this);
        return null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("titles", titles)
                .toString();
    }
}
