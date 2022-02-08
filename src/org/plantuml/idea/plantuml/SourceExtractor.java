package org.plantuml.idea.plantuml;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.plantuml.idea.lang.annotator.LanguagePatternHolder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eugene Steinberg
 */
public class SourceExtractor {
    private static final Logger LOG = Logger.getInstance(SourceExtractor.class);

    public static final String TESTDOT = "@startuml\ntestdot\n@enduml";

    public static String extractSource(Document selectedDocument, int i) {
        String text = selectedDocument.getText();
        String s = extractSource(text, i);

        //Feature request: Render *.dot files without plantUML meta tags #250
        if ("".equals(s)) {
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            VirtualFile file = fileDocumentManager.getFile(selectedDocument);
            if (file != null) {
                if (Objects.equals(file.getExtension(), "dot")) {
                    s = "@startuml\n" + text + "\n@enduml";
                }
            }
        }
        return s;
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
        long start = System.currentTimeMillis();
        LinkedHashMap<Integer, String> result = new LinkedHashMap<Integer, String>();

        if (text.contains("@start")) {

            Matcher matcher = LanguagePatternHolder.INSTANCE.sourcePattern.matcher(text);

            while (matcher.find()) {
                result.put(matcher.start(), matcher.group());
            }
        }

        if (text.contains("```plantuml")) {
            Matcher matcher = LanguagePatternHolder.INSTANCE.sourcePatternMarkdown.matcher(text);

            while (matcher.find()) {
                String group = matcher.group(1);
                group = group.substring(0, group.length() - 3);
                if (!group.trim().startsWith("@startuml")) {
                    group = "@startuml\n" + group + "\n@enduml";
                }
                result.put(matcher.start(), group);
            }
        }
        LOG.debug("extractSources done in ", System.currentTimeMillis() - start, "ms");
        return result;
    }

    private static Pattern sourceCommentPattern =
            Pattern.compile("^\\s*\\*\\s", Pattern.MULTILINE);

    private static Pattern sourceCommentPatternEnd =
            Pattern.compile("^\\s*\\*\\s*@end", Pattern.MULTILINE);

    private static Pattern sourceCommentPatternHash =
            Pattern.compile("^\\s*#\\s?", Pattern.MULTILINE);

    private static Pattern sourceCommentPatternHashEnd =
            Pattern.compile("^\\s*#\\s?@end", Pattern.MULTILINE);

    private static String stripComments(String source) {
        if (sourceCommentPatternEnd.matcher(source).find()) {
            Matcher matcher = sourceCommentPattern.matcher(source);
            return matcher.replaceAll("");
        } else if (sourceCommentPatternHashEnd.matcher(source).find()) {
            Matcher matcher = sourceCommentPatternHash.matcher(source);
            return matcher.replaceAll("");
        } else {
            return source;
        }
    }


}
