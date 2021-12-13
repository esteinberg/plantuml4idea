package org.plantuml.idea.adapter.rendering;

import com.intellij.openapi.diagnostic.Logger;
import net.sourceforge.plantuml.NewpagedDiagram;
import net.sourceforge.plantuml.TitledDiagram;
import net.sourceforge.plantuml.UmlDiagram;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.DisplayPositioned;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.Newpage;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

public class Titles {
    protected static final Logger logger = Logger.getInstance(Titles.class);

    private final List<String> titles;

    public Titles(net.sourceforge.plantuml.core.Diagram diagram) {
        this.titles = initTitles(diagram);
    }

    static List<String> initTitles(net.sourceforge.plantuml.core.Diagram diagram) {
        List<String> titles = new ArrayList<>();
        if (diagram instanceof SequenceDiagram) {
            SequenceDiagram sequenceDiagram = (SequenceDiagram) diagram;
            MyBlock.addTitle(titles, sequenceDiagram.getTitle().getDisplay());
            List<Event> events = sequenceDiagram.events();
            for (Event event : events) {
                if (event instanceof Newpage) {
                    Display title = ((Newpage) event).getTitle();
                    MyBlock.addTitle(titles, title);
                }
            }
        } else if (diagram instanceof NewpagedDiagram) {
            NewpagedDiagram newpagedDiagram = (NewpagedDiagram) diagram;
            List<net.sourceforge.plantuml.core.Diagram> diagrams = newpagedDiagram.getDiagrams();
            for (net.sourceforge.plantuml.core.Diagram diagram1 : diagrams) {
                if (diagram1 instanceof UmlDiagram) {
                    DisplayPositioned title = (DisplayPositioned) ((UmlDiagram) diagram1).getTitle();
                    MyBlock.addTitle(titles, title.getDisplay());
                }
            }
        } else if (diagram instanceof UmlDiagram) {
            DisplayPositioned title = (DisplayPositioned) ((UmlDiagram) diagram).getTitle();
            MyBlock.addTitle(titles, title.getDisplay());
        } else if (diagram instanceof PSystemError) {
            DisplayPositioned title = (DisplayPositioned) ((PSystemError) diagram).getTitle();
            if (title == null) {
                titles.add(null);
            } else {
                MyBlock.addTitle(titles, title.getDisplay());
            }
        } else if (diagram instanceof TitledDiagram) {
            MyBlock.addTitle(titles, ((TitledDiagram) diagram).getTitle().getDisplay());
        } else {
            titles.add(null);
        }
        return titles;
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


    public String getTitleOrPageNumber(int imageСounter) {
        if (titles.size() > imageСounter) {
            String s = titles.get(imageСounter);
            if (StringUtils.isBlank(s)) {
                return String.valueOf(imageСounter);
            }
            return substringBetween(s, "[", "]");
        }
        return String.valueOf(imageСounter);
    }

    public static String substringBetween(String str, String open, String close) {
        if (str != null && open != null && close != null) {
            int start = str.indexOf(open);
            if (start != -1) {
                int end = str.lastIndexOf(close);
                if (start < end && end != -1) {
                    return str.substring(start + open.length(), end);
                }
            }

            return str;
        } else {
            return str;
        }
    }

    public String getTitle(int page) {
        if (titles.size() > page) {
            return titles.get(page);
        } else {
            logger.error("page is too big = " + page + " " + this);
            return null;
        }
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("titles", titles)
                .toString();
    }

}
