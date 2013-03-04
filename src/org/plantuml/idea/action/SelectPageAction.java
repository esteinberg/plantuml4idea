package org.plantuml.idea.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.ui.components.JBComboBoxLabel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author: Eugene Steinberg
 * Date: 3/3/13
 */
public class SelectPageAction extends AnAction implements CustomComponentAction {

    @Override
    public JComponent createCustomComponent(Presentation presentation) {

        JComboBox jComboBox = new JComboBox( new String[]{ "haha", "hehe" } );
        return jComboBox;

    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        //TODO:implement me
    }
}
