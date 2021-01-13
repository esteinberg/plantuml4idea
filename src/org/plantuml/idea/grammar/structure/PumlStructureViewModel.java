package org.plantuml.idea.grammar.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.plantuml.idea.lang.PlantUmlFileImpl;

public class PumlStructureViewModel extends StructureViewModelBase implements
        StructureViewModel.ElementInfoProvider {

  public PumlStructureViewModel(PsiFile psiFile) {
    super(psiFile, new PumlStructureViewElement(psiFile));
  }

  @NotNull
  public Sorter[] getSorters() {
    return new Sorter[]{Sorter.ALPHA_SORTER};
  }


  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return false;
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    return element instanceof PlantUmlFileImpl;
  }

}
