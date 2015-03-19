package org.plantuml.idea.lang.injection;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.JavaDocElementType;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlLanguage;
import org.plantuml.idea.lang.settings.PlantUmlSettings;

import java.util.Collections;
import java.util.List;

/**
 * PlantUML injecting into Java code comments.
 *
 * @author Max Gorbunov
 */
public class PlantUmlMultiHostInjector implements MultiHostInjector {

    @Override
    public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull final PsiElement context) {
        if (!PlantUmlSettings.getInstance().isErrorAnnotationEnabled()) {
            return;
        }
        final PsiComment comment = (PsiComment) context;
        if (comment.getTokenType() == JavaTokenType.C_STYLE_COMMENT
                || comment.getTokenType() == JavaDocElementType.DOC_COMMENT) {
            String text = comment.getText();
            int offset = 0;
            while (offset < text.length()) {
                int start = text.indexOf("@startuml", offset);
                if (start < 0) {
                    break;
                }
                int end = text.indexOf("@enduml", start);
                if (end < 0) {
                    end = text.length();
                } else {
                    // One extra character to avoid assertion failure when auto-completion triggers
                    // at the end of injected file. Anyway it should be a whitespace.
                    end += "@enduml".length() + 1;
                }
                int nextStart = text.indexOf("@startuml", start + 1);
                if (nextStart >= 0 && nextStart < end) {
                    end = nextStart;
                }
                registrar.startInjecting(PlantUmlLanguage.INSTANCE);
                if (context instanceof PsiLanguageInjectionHost) {
                    registrar.addPlace(null, null, (PsiLanguageInjectionHost) context,
                            new TextRange(start, end));
                } else {
                    registrar.addPlace(null, null, new SurrogateLanguageInjectionHost(context),
                            new TextRange(start, end));
                }
                registrar.doneInjecting();
                offset = start + 1;
            }
        }
    }

    @NotNull
    @Override
    public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
        return Collections.singletonList(PsiComment.class);
    }
}
