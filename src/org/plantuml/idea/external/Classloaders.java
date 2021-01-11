package org.plantuml.idea.external;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.settings.PlantUmlSettings;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Classloaders {

    private static final Logger LOG = Logger.getInstance(Classloaders.class);

    private static ClassLoader bundled;
    private static String customPlantumlJarPath;
    private static ClassLoader custom;


    private static ClassLoader getClassloader() {
        PlantUmlSettings settings = PlantUmlSettings.getInstance();
        String customPlantumlJarPath = settings.getCustomPlantumlJarPath();
        if (settings.isUseBundled() || StringUtils.isBlank(customPlantumlJarPath)) {
            return getBundled();
        } else {
            return Classloaders.getCustomClassloader(customPlantumlJarPath);
        }
    }

    public static ClassLoader getBundled() {
        if (bundled == null) {

            List<File> jarFiles = new ArrayList<>();
            File[] jars = getPluginHome().listFiles((dir, name) -> !name.equals("plantuml4idea.jar"));
            if (jars != null) {
                validate(jars);
                jarFiles.addAll(Arrays.asList(jars));
            }
            if (isUnitTest()) {
                File file = new File("lib/plantuml");
                if (!file.exists()) {
                    throw new RuntimeException(file.getAbsolutePath());
                }
                File[] files = file.listFiles();
                if (files != null) {
                    jarFiles.addAll(Arrays.asList(files));
                }
            }


            bundled = classLoader(jarFiles);
        }
        return bundled;
    }

    public static void validate(File[] jars) {
        if (!isUnitTest() && jars.length < 2) {
            throw new RuntimeException("Invalid installation. Should find at least 2 jars, but found: " + Arrays.toString(jars));
        }
        List<File> plantumls = Arrays.stream(jars).filter(file -> file.getName().startsWith("plantuml")).collect(Collectors.toList());
        if (plantumls.size() != 1) {
            LOG.error("Invalid installation. Should find only one plantuml jar, but found: " + plantumls);
        }
    }

    public static ClassLoader getCustomClassloader(String customPlantumlJarPath) {
        if (Objects.equals(Classloaders.customPlantumlJarPath, customPlantumlJarPath) && custom != null) {
            return custom;
        }

        List<File> jars = new ArrayList<>();
        try {
            if (!new File(customPlantumlJarPath).exists()) {
                throw new IllegalArgumentException("Custom PlantUML jar does not exist! path=" + customPlantumlJarPath);
            }
            Classloaders.customPlantumlJarPath = customPlantumlJarPath;
            jars.add(new File(customPlantumlJarPath));
            jars.add(new File(getPluginHome(), "adapter.jar"));
            custom = classLoader(jars);
            return custom;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static ClassLoader classLoader(List<File> jarFiles) {
        URL[] urls = new URL[jarFiles.size()];
        for (int i = 0; i < jarFiles.size(); i++) {
            File jarFile = jarFiles.get(i);
            if (!jarFile.exists()) {
                throw new IllegalStateException("Plugin jar file not found: " + jarFile.getAbsolutePath());
            }
            try {
                urls[i] = jarFile.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            LOG.info("Creating classloader for " + Arrays.toString(urls));
            //must be parent last, otherwise it would conflict with the plugin's classloader  - it always loads the bundled plantuml
            return new ParentLastURLClassLoader(Classloaders.class.getClassLoader(), urls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static File getPluginHome() {
        if (isUnitTest()) {
            return new File("lib/");
        } else {
            File pluginHome = new File(PathManager.getPluginsPath(), "plantuml4idea/lib/");
            if (pluginHome.exists()) {
                return pluginHome;
            }

            LOG.warn(pluginHome.getAbsolutePath() + " does not exist!");
            pluginHome = new File(PathManager.getPreInstalledPluginsPath(), "plantuml4idea/lib/");
            if (pluginHome.exists()) {
                return pluginHome;
            }
            LOG.warn(pluginHome.getAbsolutePath() + " does not exist!");
        }
        throw new RuntimeException("Plugin home not found! Did you install the whole zip file?! (PathManager.getPluginsPath()=" + PathManager.getPluginsPath() + ")");
    }

    public static boolean isUnitTest() {
        return ApplicationManager.getApplication() == null || ApplicationManager.getApplication().isUnitTestMode();
    }

    @NotNull
    static PlantUmlFacade getFacade() {
        return getFacade(getClassloader());
    }

    @NotNull
    static PlantUmlFacade getFacade(ClassLoader classloader) {
        try {
            return (PlantUmlFacade) Class.forName("org.plantuml.idea.adapter.FacadeImpl", true, classloader).getConstructor().newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
