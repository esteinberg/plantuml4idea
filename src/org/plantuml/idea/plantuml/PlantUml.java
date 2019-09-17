package org.plantuml.idea.plantuml;

import net.sourceforge.plantuml.FileFormat;
import org.plantuml.idea.lang.annotator.LanguagePatternHolder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eugene Steinberg
 */
public class PlantUml {
    public static final String TESTDOT = "@startuml\ntestdot\n@enduml";
    public static final String UMLSTART = "@start";


    public enum ImageFormat {
        PNG {
            @Override
            public FileFormat getFormat() {
                return FileFormat.PNG;
            }
        },
        SVG {
            @Override
            public FileFormat getFormat() {
                return FileFormat.SVG;
            }
        },
        UTXT {
            @Override
            public FileFormat getFormat() {
                return FileFormat.UTXT;
            }
        },
        ATXT {
            @Override
            public FileFormat getFormat() {
                return FileFormat.ATXT;
            }
        },
        EPS {
            @Override
            public FileFormat getFormat() {
                return FileFormat.EPS;
            }
        };

        public abstract FileFormat getFormat();
    }


    /**
     * Extracts plantUML diagram source code from the given string starting from given offset
     * <ul>
     * <li>Relies on having @startuml and @enduml tags (puml tags) wrapping plantuml sourcePattern code</li>
     * <li>If offset happens to be inside puml tags, returns corresponding sourcePattern code.</li>
     * <li>If offset happens to be outside puml tags, returns empty string </li>
     * </ul>
     *
     * @param text   source code containing multiple plantuml sources
     * @param offset offset in the text from which plantuml sourcePattern is extracted
     * @return extracted plantUml code, including @startuml and @enduml tags or empty string if
     * no valid sourcePattern code was found
     */
    public static String extractSource(String text, int offset) {
        Map<Integer, String> sources = extractSources(text);
        String source = "";
        for (Map.Entry<Integer, String> sourceData : sources.entrySet()) {
            Integer sourceOffset = sourceData.getKey();
            if (sourceOffset <= offset && offset <= sourceOffset + sourceData.getValue().length()) {
                source = stripComments(sourceData.getValue());
                break;
            }
        }
        return source;
    }

    public static Map<Integer, String> extractSources(String text) {
        LinkedHashMap<Integer, String> result = new LinkedHashMap<Integer, String>();

        if (text.contains(UMLSTART)) {

            Matcher matcher = LanguagePatternHolder.INSTANCE.sourcePattern.matcher(text);

            while (matcher.find()) {
                result.put(matcher.start(), matcher.group());
            }
        }
        return result;
    }

    private static Pattern sourceCommentPattern =
            Pattern.compile("^\\s*\\*\\s", Pattern.MULTILINE);

    private static String stripComments(String source) {
        if (isCommented(source)) {
            Matcher matcher = sourceCommentPattern.matcher(source);
            return matcher.replaceAll("");
        } else {
            return source;
        }
    }

    private static Pattern sourceCommentPattern2 =
            Pattern.compile("^\\s*\\*\\s*@end", Pattern.MULTILINE);

    private static boolean isCommented(String source) {
        Matcher matcher = sourceCommentPattern2.matcher(source);
        return matcher.find();
    }

}
