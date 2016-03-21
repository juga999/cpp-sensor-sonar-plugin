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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.source.Highlightable;

import cppsensor.core.tokenizer.CppTokenTypes;
import cppsensor.core.tokenizer.CppTokenizer;
import cppsensor.core.tokenizer.ICppTokensConsumer;

public class CppHighlighter implements ICppTokensConsumer {

  private static final Logger log = LoggerFactory.getLogger("Highlighter");

  private final CppTokenizer tokenizer = new CppTokenizer(this);

  private boolean nextTokenForPreprocessor = false;

  private Highlightable.HighlightingBuilder builder;

  public void setEncoding(String encoding) {
    tokenizer.setEncoding(encoding);
  }

  public void highlight(InputFile f, Highlightable highlightable) {
    if (f == null || highlightable == null ) {
      return;
    }

    builder = highlightable.newHighlighting();

    try {
      tokenizer.tokenize(f.file().getCanonicalPath());
    } catch (IOException e) {
      log.error("Failed to highlight " + f.file(), e);
    }
  }

  @Override
  public void onToken(IToken token, int lineNb) {
    String type = null;
    if (isPreprocessorToken(token)) {
      type = "p";
    } else if (isRegularToken(token)) {
      type = "k";
    }
    highlightToken(token, type);
  }

  @Override
  public void onComment(boolean isBlockComment, int offset, int length) {
    if (isBlockComment) {
      builder.highlight(offset, offset + length, "j");
    } else {
      builder.highlight(offset, offset + length, "cppd");
    }
  }

  @Override
  public void onEndOfInput() {
    builder.done();
    builder = null;
  }

  private void highlightToken(IToken token, String type) {
    String highlightType = type;

    if (highlightType == null) {
      if (CppTokenTypes.STRING_TOKEN_TYPES.contains(token.getType())) {
        highlightType = "s";
      } else if (CppTokenTypes.NUMBER_TOKEN_TYPES.contains(token.getType())) {
        highlightType = "c";
      }
    }

    if (highlightType != null) {
      builder.highlight(token.getOffset(), token.getEndOffset(), highlightType);
    }
  }

  private boolean isPreprocessorToken(IToken token) {
    int tokType = token.getType();
    if (IToken.tPOUND == tokType) {
      nextTokenForPreprocessor = true;
      return true;
    } else if (IToken.tPOUNDPOUND == tokType) {
      nextTokenForPreprocessor = false;
      return true;
    } else if (IToken.tIDENTIFIER == tokType && nextTokenForPreprocessor) {
      nextTokenForPreprocessor = false;
      return true;
    } else {
      nextTokenForPreprocessor = false;
      return false;
    }
  }

  private boolean isRegularToken(IToken token) {
    int tokType = token.getType();
    return (IToken.tIDENTIFIER == tokType
        && CppTokenTypes.KEYWORDS.contains(token.getImage()));
  }

}
