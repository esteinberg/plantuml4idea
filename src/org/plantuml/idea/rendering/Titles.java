package org.plantuml.idea.rendering;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.plantuml.idea.rendering.LazyApplicationPoolExecutor.logger;

public class Titles {
    private static final Logger log = LoggerFactory.getLogger(Titles.class);
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
        logger.warn("no title, for page " + i + ", titles=" + this);
        return null;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("titles", titles)
                .toString();
    }
}
