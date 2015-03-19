package org.plantuml.idea.lang.injection;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.javadoc.JavadocTagInfo;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @author Max Gorbunov
 */
public class SimpleTagInfo implements JavadocTagInfo {
    private String tagName;

    public SimpleTagInfo(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String getName() {
        return tagName;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public boolean isValidInContext(PsiElement element) {
        return true;
    }

    @Override
    public Object[] getPossibleValues(PsiElement context, PsiElement place, String prefix) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Nullable
    @Override
    public String checkTagValue(PsiDocTagValue value) {
        return null;
    }

    @Nullable
    @Override
    public PsiReference getReference(PsiDocTagValue value) {
        return null;
    }
}
