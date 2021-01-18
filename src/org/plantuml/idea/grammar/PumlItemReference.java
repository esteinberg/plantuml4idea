package org.plantuml.idea.grammar;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.psi.PumlItem;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.lang.PlantUmlFileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PumlItemReference extends PsiReferenceBase<PumlItem> {

    private static final Logger LOG = Logger.getInstance(PumlItemReference.class);

    private final String key;

    public PumlItemReference(PumlItem element, String text) {
        super(element);
        key = text;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return PumlPsiUtil.findDeclarationInFile(getElement().getContainingFile(), getElement(), key);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PumlItem[] childrenOfType = PsiTreeUtil.getChildrenOfType(myElement.getContainingFile(), PumlItem.class);
        HashSet<String> added = new HashSet<>();
        List<LookupElement> variants = new ArrayList<>();

        if (childrenOfType != null) {
            for (final PumlItem item : childrenOfType) {
                if (item.getNode().getFirstChildNode().getElementType() != PumlTypes.IDENTIFIER) {
                    continue;
                }
                String text = item.getText();
                if (!added.contains(text)) {
                    added.add(text);
                    if (text != null && text.length() > 0) {
                        variants.add(LookupElementBuilder
                                        .create(item).withIcon(PlantUmlFileType.PLANTUML_ICON)
//                                        .withTypeText(item.getContainingFile().getName())
                                        .withCaseSensitivity(true)
                                        .withBoldness(true)
                                        .withLookupStrings(Arrays.asList(text, sanitize(text)))
                                        .withInsertHandler(new LookupElementInsertHandler())
                        );
                    }
                }

            }
        }
        return variants.toArray();
    }

    @Nullable
    private String sanitize(String text) {
        text = StringUtils.removeStart(text, "\"");
        text = StringUtils.removeStart(text, "[");
        text = StringUtils.removeStart(text, "(");
        return text;
    }

    static class LookupElementInsertHandler implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
            Editor editor = context.getEditor();
            Document document = editor.getDocument();
            if (context.getCompletionChar() == Lookup.REPLACE_SELECT_CHAR) {
                int startOffset = context.getStartOffset();
                int tailOffset = context.getTailOffset();
                String text = document.getText(TextRange.create(startOffset, tailOffset));
                if (startOffset == 0) {
                    return;
                }
                String before = document.getText(TextRange.create(startOffset - 1, startOffset));

                String substring = text.substring(0, 1);
                if (!StringUtils.isAlphanumeric(substring) && before.equals(substring)) {
                    document.deleteString(startOffset - 1, startOffset);
                }
            }
        }
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        PumlItem element = getElement();
        element.setName(newElementName);
        return element;
    }
}
