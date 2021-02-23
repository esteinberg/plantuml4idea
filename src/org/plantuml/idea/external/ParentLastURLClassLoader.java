package org.plantuml.idea.external;

import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.ui.MessageType;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import static org.plantuml.idea.util.UIUtils.notification;

/**
 * A parent-last classloader that will try the child classloader first and then the parent. This takes a fair bit of
 * doing because java really prefers parent-first.
 * <p/>
 * For those not familiar with class loading trickery, be wary
 */
public class ParentLastURLClassLoader extends ClassLoader {

    final static Set<String> loadFromParent = new HashSet<String>();

    {
        {
        }
    }

    final static Set<String> neverLoadFromParentWithPrefix = new HashSet<String>();

    {
        {
            neverLoadFromParentWithPrefix.add("net.sourceforge.plantuml.");
            neverLoadFromParentWithPrefix.add("org.plantuml.idea.adapter.");
        }
    }

    private ChildURLClassLoader childClassLoader;


    /**
     * This class allows me to call findClass on a classloader
     */
    private static class FindClassClassLoader extends ClassLoader {
        public FindClassClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            return super.findClass(name);
        }
    }

    /**
     * This class delegates (child then parent) for the findClass method for a URLClassLoader. We need this because
     * findClass is protected in URLClassLoader
     */
    private static class ChildURLClassLoader extends URLClassLoader {
        private FindClassClassLoader realParent;
        boolean shownIncompatibleNotification;
        private transient boolean closed;

        public ChildURLClassLoader(URL[] urls, FindClassClassLoader realParent) {
            super(urls, null);

            this.realParent = realParent;
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                if (loadFromParent.contains(name)) {
                    return realParent.loadClass(name);
                }

                //calling twic #findClass with the same classname, you will get a LinkageError, this fixes it
                Class<?> loaded = super.findLoadedClass(name);
                if (loaded != null)
                    return loaded;

                // first try to use the URLClassLoader findClass
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                for (String forbiddenParentPrefixes : neverLoadFromParentWithPrefix) {
                    if (name.startsWith(forbiddenParentPrefixes)) {
                        if (!closed && !shownIncompatibleNotification) {
                            shownIncompatibleNotification = true;
                            SwingUtilities.invokeLater(() -> {
                                Notifications.Bus.notify(notification().createNotification("Incompatible PlantUML Version!", MessageType.ERROR));
                            });
                        }
                        if (closed) {
                            throw new ProcessCanceledException();
                        } else {
                            throw new IncompatiblePlantUmlVersionException(
                                    name + " not found in child classloader, and cannot be loaded from parent", e);
                        }
                    }
                }
                // if that fails, we ask our real parent classloader to load the class (we give up)
                return realParent.loadClass(name);
            }
        }
    }


    public ParentLastURLClassLoader(ClassLoader parent, URL... urls) {
        super(parent);
        setDefaultAssertionStatus(false);
        childClassLoader = new ChildURLClassLoader(urls, new FindClassClassLoader(this.getParent()));
        childClassLoader.setDefaultAssertionStatus(false);    //for performance
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            // first we try to find a class inside the child classloader
            return childClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            // didn't find it, try the parent
            return super.loadClass(name, resolve);
        }
    }

    public void close() {
        try {
            childClassLoader.close();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
