package org.plantuml.idea.toolwindow;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.JBColor;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.rendering.RenderResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class ExecutionStatusPanel extends DumbAwareAction implements CustomComponentAction {
    private JLabel label;
    private volatile int version;
    private volatile State state;
    private volatile String message = "---";
    public static String DESCRIPTION;
    private MyMouseAdapter myMouseAdapter;
    private Runnable mouseOnClickAction;
    private Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD);

    {
        {
            StringBuilder sb = new StringBuilder();
            State[] values = State.values();
            for (State value : values) {
                sb.append("<br>").append(value.description);
            }
            DESCRIPTION = "<html>Last execution time<br>" +
                    "[rendered, changed, not changed page count]" +
                    "<br><br>Colors:" + sb.toString() + "</html>";
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
        myMouseAdapter = new MyMouseAdapter();
        jLabel.addMouseListener(myMouseAdapter);
        return jLabel;
    }

    public synchronized void update(State state) {
        this.state = state;
        updateUiLater();
    }

    public synchronized void update(int version, State state) {
        if (this.version <= version) {
            setState(version, state, message, mouseOnClickAction);
            updateUiLater();
        }
    }

    public synchronized void update(int version, State state, long total, RenderResult result) {
        if (this.version <= version) {
            setState(version, state, total, result, null);
            updateUiLater();
        }
    }

    public void updateNow(Integer version, State state, long total, @NotNull RenderResult result, Runnable mouseOnClickAction) {
        if (this.version <= version) {
            setState(version, state, total, result, mouseOnClickAction);
            state.updateUi(label, myMouseAdapter, message, this.mouseOnClickAction);
        } else {
            //something else is already running, updateUi all but color
            setState(version, this.state, total, result, mouseOnClickAction);
            state.updateUi(label, myMouseAdapter, message, this.mouseOnClickAction);
        }
    }

    public synchronized void updateNow(Integer version, State state, String message) {
        if (this.version <= version) {
            setState(version, state, message, null);
            state.updateUi(label, myMouseAdapter, message, null);
        }
    }

    private void setState(int version, State state, long total, RenderResult result, Runnable mouseOnClickAction) {
        int rendered = result.getRendered();
        int updatedTitles = result.getUpdatedTitles();
        int cached = result.getCached();
        String message = total + "ms ["
                + rendered + ","
                + updatedTitles + ","
                + cached + "]";
        setState(version, state, message, mouseOnClickAction);
    }


    private void setState(Integer version, State state, String message, Runnable mouseOnClickAction) {
        this.version = version;
        this.message = message;
        this.state = state;
        this.mouseOnClickAction = mouseOnClickAction;
    }

    private void updateUiLater() {
        int i = alarm.cancelAllRequests();
        alarm.addRequest(() -> {
            if (state != null) {
                state.updateUi(label, myMouseAdapter, message, mouseOnClickAction);
            }
        }, 0);
    }

    public enum State {
        WAITING(JBColor.GRAY, "Delay waiting - gray"),
        EXECUTING(new JBColor(Color.green.darker(), new Color(98, 150, 85)), "Executing - green"),
        CANCELLED(JBColor.PINK, "Cancelled - pink"),
        ERROR(JBColor.RED, "Error - red"),
        DONE(JBColor.BLACK, "Done - black/white");

        Color color;
        String description;

        State(Color color, String description) {
            this.color = color;
            this.description = description;
        }

        public void updateUi(JLabel comp, MyMouseAdapter myMouseAdapter, String message, Runnable mouseOnClickAction) {
//            ApplicationManager.getApplication().assertIsDispatchThread();
            if (comp != null) { //strange NPE
                comp.setText(message);
                comp.setForeground(this.color);
            }
            if (myMouseAdapter != null) {
                myMouseAdapter.setRunnable(mouseOnClickAction);
            }
        }
    }

    private static class MyMouseAdapter extends MouseAdapter {
        private Runnable runnable;

        @Override
        public void mouseReleased(MouseEvent e) {
            if (runnable != null) {
                runnable.run();
            }
        }

        public void setRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        public Runnable getRunnable() {
            return runnable;
        }
    }
}
