package org.plantuml.idea.external;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.settings.PlantUmlSettings;
import org.plantuml.idea.util.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @see PlantUmlFacade#get()
 */
public class Classloaders {

    private static final Logger LOG = Logger.getInstance(Classloaders.class);

    private static ParentLastURLClassLoader bundled;
    private static String customPlantumlJarPath;
    private static ParentLastURLClassLoader custom;

    private static volatile boolean checked;
    private static String bundledVersion;

    private static synchronized ClassLoader getClassloader() {

        PlantUmlSettings settings = PlantUmlSettings.getInstance();
        if (!checked) {
            try {
                settings.checkVersion(getBundledVersion());
            } catch (Throwable e) {
                LOG.error(e);
            }
            checked = true;
        }

        String customPlantumlJarPath = settings.getCustomPlantumlJarPath();
        if (settings.isUseBundled() || StringUtils.isBlank(customPlantumlJarPath)) {
            return getBundled();
        } else {
            return Classloaders.getCustomClassloader(customPlantumlJarPath);
        }
    }

    public static String getBundledVersion() {
        if (bundledVersion == null) {
            bundledVersion = getFacade(getBundled()).version();
        }
        return bundledVersion;
    }

    static synchronized ParentLastURLClassLoader getBundled() {
//        bundled=null;
        if (bundled == null) {
            List<File> jarFiles = new ArrayList<>();
            File[] jars = getPluginHome().listFiles();
            if (jars != null) {
                validate(jars);
                jarFiles.addAll(Arrays.asList(jars));
            }
            if (Utils.isUnitTest()) {
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

    private static void validate(File[] jars) {
        if (!Utils.isUnitTest() && jars.length < 2) {
            throw new RuntimeException("Invalid installation. Should find at least 2 jars, but found: " + Arrays.toString(jars));
        }
        List<File> plantumls = Arrays.stream(jars).filter(file -> file.getName().startsWith("plantuml")).collect(Collectors.toList());
        if (plantumls.size() != 1) {
            LOG.error("Invalid installation. Should find only one plantuml jar, but found: " + plantumls);
        }
    }

    private synchronized static ParentLastURLClassLoader getCustomClassloader(String customPlantumlJarPath) {
//        custom = null;
        if (Objects.equals(Classloaders.customPlantumlJarPath, customPlantumlJarPath) && custom != null) {
            return custom;
        }

        List<File> jars = new ArrayList<>();
        jars.addAll(List.of(getPluginHome().listFiles((dir, name) -> name.contains("plantuml4idea"))));
        try {
            if (!new File(customPlantumlJarPath).exists()) {
                throw new IllegalArgumentException("Custom PlantUML jar does not exist! path=" + customPlantumlJarPath);
            }
            Classloaders.customPlantumlJarPath = customPlantumlJarPath;

            File customPath = new File(customPlantumlJarPath);
            if (customPath.isDirectory()) {
                File[] files = customPath.listFiles((file, s) -> s.endsWith(".jar"));
                if (files != null) {
                    Collections.addAll(jars, files);
                }
            } else {
                jars.add(customPath);
            }
            custom = classLoader(jars);
            return custom;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static ParentLastURLClassLoader classLoader(List<File> jarFiles) {
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
            if (LOG.isDebugEnabled()) {
                LOG.debug(new Exception("trace"));
            }

            //must be parent last, otherwise it would conflict with the plugin's classloader  - it always loads the bundled plantuml
            return new ParentLastURLClassLoader(Classloaders.class.getClassLoader(), urls);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static File getPluginHome() {
        if (Utils.isUnitTest()) {
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

    /**
     * @see PlantUmlFacade#get()
     */
    @NotNull
    static PlantUmlFacade getFacade() {
//        if (true) {
//            return new FacadeImpl();
//        }
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

    public static synchronized void clear() {
        if (bundled != null) {
            bundled.close();
            bundled = null;
        }
        if (custom != null) {
            custom.close();
            custom = null;
        }
        System.gc();
    }

    public static synchronized void disposeBundled() {
        bundled.close();
        bundled = null;
    }

}
