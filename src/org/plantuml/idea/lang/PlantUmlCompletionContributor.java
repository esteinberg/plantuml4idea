package org.plantuml.idea.lang;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.external.PlantUmlFacade;
import org.plantuml.idea.grammar.psi.PumlElementFactory;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.lang.annotator.LanguageDescriptor;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.PsiUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PlantUmlCompletionContributor extends CompletionContributor implements DumbAware {
    private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(PlantUmlCompletionContributor.class);
    private final SkinParamProvider skinParamProvider = new SkinParamProvider();


    public PlantUmlCompletionContributor() {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new SimpleProvider(LanguageDescriptor.INSTANCE.keywords));
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new SimpleProvider(LanguageDescriptor.INSTANCE.types));
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new SimpleProvider(LanguageDescriptor.INSTANCE.keywords2));
//        extend(
//                CompletionType.BASIC,
//                PlatformPatterns.psiElement(),
//                new SkinParamProvider());

    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        int offset = parameters.getOffset();
        Document document = parameters.getEditor().getDocument();
        final int lineStartOffset = document.getLineStartOffset(document.getLineNumber(offset));
        String text = document.getText(TextRange.create(lineStartOffset, offset));

        if (text.matches("\\s*!\\w*$")) {
            for (String s : LanguageDescriptor.INSTANCE.preproc) {
                result.addElement(LookupElementBuilder.create(s).withPresentableText(s).withCaseSensitivity(true).bold());
            }
        } else if (text.matches(".*%\\w*$")) {
            for (String s : LanguageDescriptor.INSTANCE.functions) {
                result.addElement(LookupElementBuilder.create(s).withPresentableText(s).withCaseSensitivity(true).bold().appendTailText("(...)", true));
            }
        } else if (text.matches(".*skinparam.*")) {
            if (text.matches(".*skinparam\\s+\\w+\\s+.*")) {
                return;
            }

            skinParamProvider.addCompletions(parameters, new ProcessingContext(), result);
            result.stopHere();
        } else if (text.matches("\\s*@\\w*$")) {
            for (String s : LanguageDescriptor.INSTANCE.tags) {
                result.addElement(LookupElementBuilder.create(s).withPresentableText(s).withCaseSensitivity(true).bold());
            }
        } else {
            if (text.endsWith("!")) {
                return;
            }
            if (isInsideSkinParamBlock(parameters)) {
                skinParamProvider.addCompletions(parameters, new ProcessingContext(), result);
                result.stopHere();
                return;
            }


            super.fillCompletionVariants(parameters, result);


            PlantUmlSettings settings = PlantUmlSettings.getInstance();
            if (settings.isAutoComplete() && !settings.isUseGrammar()) { //PumlItemReference.getVariants duplicates it
                WordCompletionContributor.addWordCompletionVariants(result, parameters, Collections.emptySet());
            }
        }


    }

    private boolean isInsideSkinParamBlock(CompletionParameters parameters) {
        Editor editor = parameters.getEditor();
        CaretModel caretModel = editor.getCaretModel();
        PsiElement psiElement = parameters.getOriginalFile().findElementAt(caretModel.getOffset());

        boolean blockStart = false;
        int wordsBeforeBlockStart = 0;
        while (psiElement != null) {
            psiElement = PsiTreeUtil.prevCodeLeaf(psiElement);

            IElementType elementType = PsiUtil.getElementType(psiElement);
            if (elementType == PumlTypes.OTHER) {
                if (psiElement.getText().equals("{")) {
                    blockStart = true;
                } else {
                    return false;
                }
            } else if (elementType == PumlTypes.IDENTIFIER) {
                if (blockStart) {
                    if ("skinparam".equalsIgnoreCase(psiElement.getText())) {
                        return true;
                    } else {
                        wordsBeforeBlockStart++;

                        if (wordsBeforeBlockStart >= 2) {
                            return false;
                        }
                    }
                }
            }

        }
        return false;

    }

    static class SkinParam implements InsertHandler<LookupElement> {
        @Override
        public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
            Editor editor = context.getEditor();
            CaretModel caretModel = editor.getCaretModel();
            PsiElement elementAt = context.getFile().findElementAt(caretModel.getOffset() - 1);
            if (elementAt != null) {
                String text = elementAt.getText();
                if (text.equals(item.getLookupString().toLowerCase())) { //case insensitive autocomplete - inserts all lowercase
                    elementAt.replace(PumlElementFactory.createWord2(context.getProject(), item.getLookupString()));
                }
            }
        }
    }


    class SimpleProvider extends CompletionProvider<CompletionParameters> {
        protected final List<String> myItems;

        SimpleProvider(List<String> items) {
            myItems = items;
        }

        @Override
        public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            if (parameters.getInvocationCount() == 0) {
                return;
            }
            for (String item : myItems) {
                result.addElement(LookupElementBuilder.create(item).withCaseSensitivity(true).withItemTextItalic(true));
            }
        }

    }

    private class SkinParamProvider extends CompletionProvider<CompletionParameters> {

        private final SkinParam insertHandler = new SkinParam();

        @Override
        public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
            if (parameters.getInvocationCount() == 0) {
                return;
            }

            Collection<String> skinParams = PlantUmlFacade.get().getSkinParams();
            for (String key : skinParams) {
                result.addElement(LookupElementBuilder.create(key)
                        .withPresentableText(key).withCaseSensitivity(false).bold().withInsertHandler(insertHandler));
            }
        }

    }
}
