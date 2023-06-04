// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.plantuml.idea.preview.image.svg.batik;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.Strings;
import org.jetbrains.annotations.ApiStatus;

import javax.xml.stream.XMLInputFactory;

public final class MyJDOMUtil {

    private static final Logger LOG = Logger.getInstance(MyJDOMUtil.class);

    private static final String XML_INPUT_FACTORY_KEY = "javax.xml.stream.XMLInputFactory";
    private static final String XML_INPUT_FACTORY_IMPL = "com.sun.xml.internal.stream.XMLInputFactoryImpl";

    private static volatile XMLInputFactory XML_INPUT_FACTORY;

    // do not use AtomicNotNullLazyValue to reduce class loading
    @ApiStatus.Internal
    public static XMLInputFactory getXmlInputFactory() {
        XMLInputFactory factory = XML_INPUT_FACTORY;
        if (factory != null) {
            return factory;
        }

        //noinspection SynchronizeOnThis
        synchronized (MyJDOMUtil.class) {
            factory = XML_INPUT_FACTORY;
            if (factory != null) {
                return factory;
            }

            // requests default JRE factory implementation instead of an incompatible one from the classpath
            String property = System.setProperty(XML_INPUT_FACTORY_KEY, XML_INPUT_FACTORY_IMPL);
            try {
                //its fine
                factory = XMLInputFactory.newFactory();

            } finally {
                if (property != null) {
                    System.setProperty(XML_INPUT_FACTORY_KEY, property);
                } else {
                    System.clearProperty(XML_INPUT_FACTORY_KEY);
                }
            }

            // avoid loading of SystemInfo class
            if (Strings.indexOfIgnoreCase(System.getProperty("java.vm.vendor", ""), "IBM", 0) < 0) {
                try {
                    factory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", true);
                } catch (Exception e) {
                    LOG.error("cannot set \"report-cdata-event\" property for XMLInputFactory", e);
                }
            }

            factory.setProperty(XMLInputFactory.IS_COALESCING, true);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XML_INPUT_FACTORY = factory;
            return factory;
        }
    }

    private MyJDOMUtil() {
    }
}
