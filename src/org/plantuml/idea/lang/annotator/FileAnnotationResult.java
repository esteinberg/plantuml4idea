package org.plantuml.idea.lang.annotator;

import java.util.Map;
import java.util.TreeMap;

/**
 * Author: Eugene Steinberg
 * Date: 9/30/14
 */
public class FileAnnotationResult {
    private Map<Integer, SourceAnnotationResult> annotationResultMap = new TreeMap<Integer, SourceAnnotationResult>();

    public FileAnnotationResult() {
    }

    public void put(Integer key, SourceAnnotationResult sourceAnnotationResult) {
        annotationResultMap.put(key, sourceAnnotationResult);
    }

    public Map<Integer, SourceAnnotationResult> getAnnotationResultMap() {
        return annotationResultMap;
    }
}
