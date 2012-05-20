package org.plantuml.idea.messaging;

import com.intellij.util.messages.Topic;

/**
 * User: eugene
 * Date: 5/19/12
 * Time: 9:36 PM
 */
public interface RenderingNotifier {
    Topic<RenderingNotifier> RENDERING_COMPLETED_TOPIC =
            Topic.create("org.plantuml.idea.renderingCompleted", RenderingNotifier.class);
    void afterRe

}
