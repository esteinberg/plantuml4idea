package org.plantuml.idea.grammar;

import com.intellij.spellchecker.dictionary.Dictionary;
import com.intellij.spellchecker.dictionary.RuntimeDictionaryProvider;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.annotator.LanguageDescriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PumlRuntimeDictionaryProvider implements RuntimeDictionaryProvider {

    private Dictionary[] dictionaries;

    public PumlRuntimeDictionaryProvider() {
        dictionaries = new Dictionary[]{dictionary(LanguageDescriptor.INSTANCE.types, "PlantUml types"),
                dictionary(LanguageDescriptor.INSTANCE.keywords, "PlantUml keywords"),
                dictionary(LanguageDescriptor.INSTANCE.preproc, "PlantUml preprocs"),
                dictionary(LanguageDescriptor.INSTANCE.tags, "PlantUml tags"),};
    }

    @Override
    public Dictionary[] getDictionaries() {
        return dictionaries;
    }

    private Dictionary dictionary(List<String> keys, final String name) {
        HashSet<String> set = new HashSet<>(keys.size());
        for (String type : keys) {
            if (type.startsWith("@") || type.startsWith("!")) {
                set.add(type.substring(1));
            } else {
                set.add(type);
            }
        }
        return new Dictionary() {
            @NotNull
            @Override
            public String getName() {
                return name;
            }

            @Override
            public Boolean contains(@NotNull String s) {
                return set.contains(s);
            }

            @Override
            public @NotNull
            Set<String> getWords() {
                return set;
            }
        };
    }
}
