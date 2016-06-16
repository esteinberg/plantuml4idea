package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.JBColor;
import org.plantuml.idea.rendering.RenderResult;

import javax.swing.*;
import java.awt.*;

public class ExecutionStatusLabel extends DumbAwareAction implements CustomComponentAction {
    private JLabel label;
    private volatile State state;
    private volatile String message = "---";
    public static String DESCRIPTION;

    {
        {
            StringBuilder sb = new StringBuilder();
            State[] values = State.values();
            for (State value : values) {
                sb.append("\n").append(value.description);
            }
            DESCRIPTION = "Last execution time, page counts\n\nColors:" + sb.toString();
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
        final JPanel panel = new JPanel();
        this.label = new JLabel("---");
        Font font = label.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        label.setFont(boldFont);
        panel.setToolTipText(DESCRIPTION);
        panel.add(this.label);
        return panel;
    }

    public void state(State b) {
        state = b;

        updateOnEDT();
    }

    public void state(State state, long total, RenderResult result) {
        int rendered = result.getRendered();
        int updatedTitles = result.getUpdatedTitles();
        int cached = result.getCached();
        
        this.state = state;
        this.message = String.valueOf(total) + "ms "
                + rendered + " rendered "
                + updatedTitles + " changed "
                + cached + " unchanged";
        updateOnEDT();
    }

    public void state(State state, String message) {
        this.state = state;
        this.message = message;

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
            state.update(label, message);
        }
    }


    public enum State {
        WAITING(JBColor.GRAY, "Delay waiting - gray"),
        EXECUTING(JBColor.GREEN, "Executing - green"),
        CANCELLED(JBColor.PINK, "Cancelled - pink"),
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
