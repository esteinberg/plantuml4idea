package org.plantuml.idea.rendering;

import com.intellij.ide.plugins.cl.PluginAwareClassLoader;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.plantuml.idea.settings.PlantUmlSettings;

import javax.swing.*;
import javax.xml.transform.TransformerFactory;

import static org.plantuml.idea.util.UIUtils.notification;

public class CompatibilityCheck {
    public static boolean checkTransformer(PlantUmlSettings plantUmlSettings) {
        if (!plantUmlSettings.isDisplaySvg()) {
            return false;
        }
        if (plantUmlSettings.isRemoteRendering()) {
            return false;
        }

        TransformerFactory factory = TransformerFactory.newInstance();
        ClassLoader classLoader = factory.getClass().getClassLoader();

        if (classLoader instanceof PluginAwareClassLoader) {
            String clName = ((PluginAwareClassLoader) classLoader).getPluginDescriptor().getName();


            plantUmlSettings.setDisplaySvg(false);

            SwingUtilities.invokeLater(() -> {
                Notifications.Bus.notify(notification().createNotification("Conflict detected with '" + clName + "'. Switching to PNG rendering.", NotificationType.WARNING));
            });
            return false;
        }
        return true;
    }
}
