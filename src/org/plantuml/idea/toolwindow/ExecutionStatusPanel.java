package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.rendering.RenderResult;

import javax.swing.*;
import java.awt.*;

public class ExecutionStatusPanel extends DumbAwareAction implements CustomComponentAction {
    private JLabel label;
    private volatile int version;
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
            DESCRIPTION = "Last execution time\n" +
                    "[rendered, changed, not changed page count]" +
                    "\n\nColors:" + sb.toString();
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
        this.label = createLabel();
        panel.setToolTipText(DESCRIPTION);
        panel.add(this.label);
        return panel;
    }

    @NotNull
    public JLabel createLabel() {
        JLabel jLabel = new JLabel("---");
        Font font = jLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize());
        jLabel.setFont(boldFont);
        return jLabel;
    }

    public synchronized void update(State state) {
        this.state = state;
        updateLabelLater();
    }

    public synchronized void update(int version, State state) {
        if (this.version <= version) {
            updateState(version, state, message);
            updateLabelLater();
        } 
    }

    public synchronized void update(int version, State state, long total, RenderResult result) {
        if (this.version <= version) {
            updateState(version, state, total, result);
            updateLabelLater();
        }
    }


    public synchronized void updateNow(Integer version, State state, long total, RenderResult result) {
        if (this.version <= version) {
            updateState(version, state, total, result);
            state.update(label, message);
        }
    }


    public synchronized void updateNow(Integer version, State state, String message) {
        if (this.version <= version) {
            updateState(version, state, message);
            state.update(label, message);
        }
    }

    protected void updateState(int version, State state, long total, RenderResult result) {
        int rendered = result.getRendered();
        int updatedTitles = result.getUpdatedTitles();
        int cached = result.getCached();
        String message = String.valueOf(total) + "ms ["
                + rendered + ","
                + updatedTitles + ","
                + cached + "]";
        updateState(version, state, message);
    }


    private void updateState(Integer version, State state, String message) {
        this.version = version;
        this.message = message;
        this.state = state;
    }

    private void updateLabelLater() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                if (state != null) {
                    state.update(label, message);
                }
            }
        });
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

        public void update(JLabel comp, String message) {
            ApplicationManager.getApplication().assertIsDispatchThread();
            if (comp != null) { //strange NPE
                comp.setText(message);
                comp.setForeground(this.color);
            }
        }
    }

}
