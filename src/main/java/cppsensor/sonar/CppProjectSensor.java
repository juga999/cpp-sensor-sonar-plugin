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

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.source.Highlightable;
import org.sonar.api.source.Symbolizable;

import cppsensor.core.parser.CppIndexedParser;
import cppsensor.core.parser.CppParserInfoProvider;

public class CppProjectSensor implements Sensor {

  private static final Logger log = LoggerFactory.getLogger("CppProjectSensor");

  private final Settings settings;

  private final FileSystem fs;

  private final ResourcePerspectives perspectives;

  private final String encoding;

  private final CppHighlighter highlighter = new CppHighlighter();

  private final CppSymbolizer symbolizer = new CppSymbolizer();

  public CppProjectSensor(Settings settings, FileSystem fs, ResourcePerspectives perspectives) {
    this.settings = settings;
    this.fs = fs;
    this.perspectives = perspectives;
    this.encoding = fs.encoding().name();
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fs.languages().contains(CppLanguage.KEY);
  }

  @Override
  public void analyse(Project module, SensorContext context) {
    FilePredicate pred = fs.predicates().and(
        fs.predicates().hasLanguage(CppLanguage.KEY),
        fs.predicates().hasType(Type.MAIN));

    CppParserInfoProvider parserInfoProvider =
        CppProjectAnalysisHandler.instance().getParserInfoProvider(module, settings);
    CppIndexedParser parser = CppProjectAnalysisHandler.instance().getParser();
    parser.setEncoding(encoding);
    parser.setScanInfoProvider(parserInfoProvider);

    highlighter.setEncoding(this.encoding);

    for (InputFile f : fs.inputFiles(pred)) {
      log.info("Processing "+f);
      highlight(f);
      IASTTranslationUnit tu = parser.parse(f.absolutePath());
      symbolize(f, tu);
    }
  }

  @Override
  public String toString() {
    return "C/C++ Project";
  }

  private void highlight(InputFile f) {
    highlighter.highlight(f, perspectives.as(Highlightable.class, f));
  }

  private void symbolize(InputFile f, IASTTranslationUnit tu) {
    symbolizer.symbolize(tu, perspectives.as(Symbolizable.class, f));
  }

}
