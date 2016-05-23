package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;

public class ExucutionTimeLabel extends DumbAwareAction implements CustomComponentAction {
    private JLabel comp;
    private volatile State state;
    private volatile String total;
    public static String DESCRIPTION;

    {
        {
            StringBuilder sb = new StringBuilder();
            State[] values = State.values();
            for (State value : values) {
                sb.append("\n").append(value.description);
            }
            DESCRIPTION = "Last execution time.\n\nColors:" + sb.toString();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {

    }

    @Override
    public JComponent createCustomComponent(Presentation presentation) {
        final JPanel label = new JPanel();
        this.comp = new JLabel("---");
        label.setToolTipText(DESCRIPTION);
        label.add(this.comp);
        return label;
    }

    public void setState(State b) {
        state = b;

        updateOnEDT();
    }

    public void setState(State state, long total) {
        this.state = state;
        this.total = String.valueOf(total) + "ms";

        updateOnEDT();
    }

    private void updateOnEDT() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                updateLabel();
            }
        });
    }

    private void updateLabel() {
        if (state != null) {
            state.update(comp, total);
        }
    }


    public enum State {
        WAITING(JBColor.GRAY, "Delay waiting - gray"),
        EXECUTING(JBColor.GREEN, "Executing - green"),
        CANCELLED(JBColor.BLUE, "Cancelled - blue"),
        ERROR(JBColor.RED, "Error - red"),
        DONE(JBColor.BLACK, "Done - black/white");

        Color color;
        String description;

        State(Color color, String description) {
            this.color = color;
            this.description = description;
        }

        public void update(JLabel comp, String total) {
            comp.setText(total);
            comp.setForeground(this.color);
        }
    }
}
