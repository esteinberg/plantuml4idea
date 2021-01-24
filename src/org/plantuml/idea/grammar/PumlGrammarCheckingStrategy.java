package org.plantuml.idea.grammar;

import com.intellij.grazie.grammar.Typo;
import com.intellij.grazie.grammar.strategy.GrammarCheckingStrategy;
import com.intellij.grazie.grammar.strategy.impl.ReplaceCharRule;
import com.intellij.grazie.grammar.strategy.impl.RuleGroup;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import kotlin.ranges.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.plantuml.idea.grammar.psi.PumlTypes;
import org.plantuml.idea.lang.PlantUmlFileImpl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PumlGrammarCheckingStrategy implements GrammarCheckingStrategy {

    @NotNull
    @Override
    public ElementBehavior getElementBehavior(@NotNull PsiElement root, @NotNull PsiElement child) {
        if (child.getNode().getElementType() == PumlTypes.OTHER) {
            return ElementBehavior.ABSORB;
        } else if (child instanceof PsiWhiteSpace && child.getText().contains("\n")) {
            return ElementBehavior.ABSORB;
        } else {
            return ElementBehavior.TEXT;
        }
    }

    @Override
    public boolean isMyContextRoot(@NotNull PsiElement psiElement) {
        if (psiElement instanceof PsiComment) {
            return true;
        } else if (psiElement instanceof PlantUmlFileImpl) {
            return true;
        } else {
            return false;
        }
    }

    @NotNull
    @Override
    public TextDomain getContextRootTextDomain(@NotNull PsiElement root) {
        if (root instanceof PsiComment) {
            return TextDomain.COMMENTS;
        }
        return TextDomain.PLAIN_TEXT;
    }

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }


    @Nullable
    @Override
    public RuleGroup getIgnoredRuleGroup(@NotNull PsiElement root, @NotNull PsiElement child) {
        return null;
    }

    @Nullable
    @SuppressWarnings({"UnstableApiUsage", "MissingOverride", "deprecation"})
    // to be removed in 2020.2, remove @Override to keep compatibility
    // @Override
    public Set<Typo.Category> getIgnoredTypoCategories(@NotNull PsiElement psiElement, @NotNull PsiElement psiElement1) {
        return Collections.emptySet();
    }

    @SuppressWarnings({"UnstableApiUsage", "MissingOverride", "deprecation"})
    @NotNull
    // to be removed in 2020.2, remove @Override to keep compatibility
    // @Override
    public List<ReplaceCharRule> getReplaceCharRules(@NotNull PsiElement psiElement) {
        return Collections.emptyList();
    }

    @SuppressWarnings({"UnstableApiUsage", "MissingOverride", "deprecation"})
    // to be removed in 2021.1, remove @Override to keep compatibility
    public boolean isTypoAccepted(@NotNull PsiElement psiElement, @NotNull IntRange intRange, @NotNull IntRange intRange1) {
        return true;
    }

    @NotNull
    @Override
    public LinkedHashSet<IntRange> getStealthyRanges(@NotNull PsiElement psiElement, @NotNull CharSequence charSequence) {
        return new LinkedHashSet<>(0);
    }


}