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

import java.io.IOException;

import org.eclipse.cdt.core.parser.IToken;
import org.sonar.api.batch.AbstractCpdMapping;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.resources.Language;

import cppsensor.core.tokenizer.CppTokenizer;
import cppsensor.core.tokenizer.ICppTokensConsumer;
import net.sourceforge.pmd.cpd.SourceCode;
import net.sourceforge.pmd.cpd.TokenEntry;
import net.sourceforge.pmd.cpd.Tokenizer;
import net.sourceforge.pmd.cpd.Tokens;

public class CppCpdMapping extends AbstractCpdMapping implements ICppTokensConsumer, Tokenizer {

  private final CppTokenizer tokenizer = new CppTokenizer(this);

  private Tokens tokens;

  private String fileName;

  public CppCpdMapping(FileSystem fs) {
    tokenizer.setEncoding(fs.encoding().name());
  }

  @Override
  public void tokenize(SourceCode source, Tokens tokens) throws IOException {
    this.tokens = tokens;
    fileName = source.getFileName();
    tokenizer.tokenize(fileName);
  }

  @Override
  public Tokenizer getTokenizer() {
    return this;
  }

  @Override
  public Language getLanguage() {
    return new CppLanguage();
  }

  @Override
  public void onToken(IToken token, int lineNb) {
    TokenEntry cpdToken = new TokenEntry(token.getImage(), fileName, lineNb);
    tokens.add(cpdToken);
  }

  @Override
  public void onComment(boolean isBlockComment, int offset, int length) {
  }

  @Override
  public void onEndOfInput() {
    tokens.add(TokenEntry.EOF);
    tokens = null;
    fileName = null;
  }

}
