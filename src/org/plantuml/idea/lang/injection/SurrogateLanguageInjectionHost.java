package org.plantuml.idea.lang.injection;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.PsiElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This wrapper is needed to inject languages into code fragments that don't support code injection (such as javadoc).
 * This is a hack and may be broken in future releases of Idea.
 *
 * @author Max Gorbunov
 */
class SurrogateLanguageInjectionHost extends PsiElementBase implements PsiLanguageInjectionHost {
    private final PsiElement context;

    public SurrogateLanguageInjectionHost(PsiElement context) {
        this.context = context;
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String text) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new JavadocTextEscaper(this);
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return context.getLanguage();
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return context.getChildren();
    }

    @Override
    public PsiElement getParent() {
        return context.getParent();
    }

    @Override
    public TextRange getTextRange() {
        return context.getTextRange();
    }

    @Override
    public int getStartOffsetInParent() {
        return context.getStartOffsetInParent();
    }

    @Override
    public int getTextLength() {
        return context.getTextLength();
    }

    @Nullable
    @Override
    public PsiElement findElementAt(int offset) {
        return context.findElementAt(offset);
    }

    @Override
    public int getTextOffset() {
        return context.getTextOffset();
    }

    @Override
    public String getText() {
        return context.getText();
    }

    @NotNull
    @Override
    public char[] textToCharArray() {
        return context.textToCharArray();
    }

    @Override
    public ASTNode getNode() {
        return context.getNode();
    }

    // methods below are overridden to prevent issues with default implementation (infinite recursion or assertions)
    @Override
    public PsiManager getManager() {
        return context.getManager();
    }

    @Override
    public PsiElement getPrevSibling() {
        return context.getPrevSibling();
    }
}
