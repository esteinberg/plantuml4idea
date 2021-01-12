package org.plantuml.idea.language;

import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.language.psi.PumlWord;

import java.util.ArrayList;
import java.util.List;

public class SimpleChooseByNameContributor implements ChooseByNameContributor {

    @NotNull
    @Override
    public String[] getNames(Project project, boolean includeNonProjectItems) {
        List<PumlWord> properties = SimpleUtil.findAll(project);
        List<String> names = new ArrayList<>(properties.size());
        for (PumlWord property : properties) {
            names.add(property.getText());
        }
        return names.toArray(new String[names.size()]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean includeNonProjectItems) {
        List<PumlWord> properties = SimpleUtil.find(project, name);
        return properties.toArray(new NavigationItem[properties.size()]);
    }

}
