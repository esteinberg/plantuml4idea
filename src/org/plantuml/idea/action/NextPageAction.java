package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.ui.UIUtil;
import org.plantuml.idea.util.UIUtils;

/**
 * Author: Eugene Steinberg
 * Date: 2/28/13
 */
public class NextPageAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        UIUtils.getToolWindow(anActionEvent.getProject()).nextPage();
    }
}
