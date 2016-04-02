/**
 * Copyright (C) 2016 Julien Gaston
 * cpp-sensor@googlegroups.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */

package cppsensor.sonar;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.sonar.api.source.Symbol;
import org.sonar.api.source.Symbolizable;
import org.sonar.api.source.Symbolizable.SymbolTable;

public class CppSymbolizer extends ASTVisitor {

  private Symbolizable.SymbolTableBuilder builder;

  public CppSymbolizer() {
    shouldVisitNames = true;
    shouldVisitImplicitNames = true;
  }

  public void symbolize(IASTTranslationUnit tu, Symbolizable symbolizable) {
    if (tu == null || symbolizable == null) {
      return;
    }

    builder = symbolizable.newSymbolTableBuilder();

    tu.accept(this);

    SymbolTable symbolTable = builder.build();
    if (symbolTable != null) {
      symbolizable.setSymbolTable(symbolTable);
    }
  }

  @Override
  public int visit(IASTName name) {
    if (name.isPartOfTranslationUnitFile()) {
      IBinding binding = name.resolveBinding();
      if (isParameter(name, binding) ||
          isCompositeTypeField(name, binding) ||
          isFunctionVariable(name, binding)) {
        symbolizeName(name, binding);
      }
    }
    return ASTVisitor.PROCESS_CONTINUE;
  }

  private void symbolizeName(IASTName name, IBinding binding) {
    IASTName[] refs = name.getTranslationUnit().getReferences(binding);
    if (refs == null || refs.length == 0) {
      return;
    }

    IASTFileLocation location = getLocation(name);
    if (location == null) {
      return;
    }

    Symbol symbol = builder.newSymbol(location.getNodeOffset(),
        location.getNodeOffset() + location.getNodeLength());
    for (IASTName ref : refs) {
      location = getLocation(ref);
      if (location != null) {
        builder.newReference(symbol, location.getNodeOffset());
      }
    }
  }

  private boolean isParameter(IASTName name, IBinding binding) {
    return (
        binding instanceof IParameter && isDeclaratorName(name));
  }

  private boolean isCompositeTypeField(IASTName name, IBinding binding) {
    return (
        binding instanceof IField && isDeclaratorName(name));
  }

  private boolean isFunctionVariable(IASTName name, IBinding binding) {
    return (
        binding instanceof IVariable && isDeclaratorName(name));
  }

  private boolean isDeclaratorName(IASTName name) {
    return name.getPropertyInParent() == IASTDeclarator.DECLARATOR_NAME;
  }

  private IASTFileLocation getLocation(IASTNode node) {
    if (node.isPartOfTranslationUnitFile()) {
      IASTNodeLocation[] locations = node.getNodeLocations();
      if (locations != null && locations.length == 1 && locations[0] instanceof IASTFileLocation) {
        return (IASTFileLocation)locations[0];
      }
    }
    return null;
  }

}
