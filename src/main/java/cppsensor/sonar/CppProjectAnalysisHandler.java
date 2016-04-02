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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.bootstrap.ProjectBuilder;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.events.ProjectAnalysisHandler;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

import cppsensor.core.parser.CppIndexedParser;
import cppsensor.core.parser.CppParserInfoProvider;

public class CppProjectAnalysisHandler extends ProjectBuilder implements ProjectAnalysisHandler {

  private static final Logger log = LoggerFactory.getLogger("CppProjectAnalysisHandler");

  private static CppProjectAnalysisHandler INSTANCE;

  private File baseDir = null;

  private String baseDirPath = null;

  private CppIndexedParser parser = null;

  public CppProjectAnalysisHandler() {}

  public static CppProjectAnalysisHandler instance() {
    return INSTANCE;
  }

  @Override
  public void build(Context context) {
    init(context.projectReactor().getRoot());
  }

  @Override
  public void onProjectAnalysis(ProjectAnalysisEvent event) {
    if (event.isEnd()) {
      onProjectAnalysisDone(event.getProject());
    }
  }

  public void onProjectAnalysisDone(Project project) {
    if (project.isRoot()) {
      if (parser != null) {
        parser.release();
        parser = null;
      }
    }
    log.info(String.format(
        "Analysis of %s (%d) done.",
        project.getKey(), project.getId()));
  }

  public CppIndexedParser getParser() {
    return parser;
  }

  public CppParserInfoProvider getParserInfoProvider(Project project, Settings settings) {
    String[] includeDirs = getPathsFromSetting(
        project, settings, CppGeneralSettings.INCLUDE_DIRECTORIES_KEY);
    String[] forceIncludes = getPathsFromSetting(
        project, settings, CppGeneralSettings.FORCE_INCLUDE_FILES_KEY);

    CppParserInfoProvider infoProvider = new CppParserInfoProvider();
    infoProvider.setIncludePaths(includeDirs);
    infoProvider.setBuiltinIncFiles(forceIncludes);

    return infoProvider;
  }

  /**
   * Returns a list of paths for the given setting.
   * For such a setting, sub-modules can add their own path to the list of global paths.
   * @param project the project to configure
   * @param settings the settings of the project
   * @param key the key to retrieve the paths from
   * @return the list of paths
   */
  public String[] getPathsFromSetting(Project project, Settings settings, String key) {
    List<String> paths = new ArrayList<String>();

    getPathsFromSetting(settings, key, paths);

    if (project.isModule()) {
      String moduleKey = project.getName() + "." + key;
      getPathsFromSetting(settings, moduleKey, paths);
    }

    return paths.toArray(new String[] {});
  }

  private void init(ProjectDefinition projDef) {
    baseDir = projDef.getBaseDir();
    try {
      baseDirPath = baseDir.getCanonicalPath();
    } catch (IOException e) {
      log.error("Failed to get the base directory of the project", e);
      return;
    }

    Map<String, String> props = projDef.properties();

    String[] excludesFromIndex = getStringArrayBySeparator(
        props.get(CppGeneralSettings.EXCLUDE_INDEX_PATTERN_KEY), ",");
    parser = new CppIndexedParser(baseDirPath, excludesFromIndex);
    parser.init();

    INSTANCE = this;
  }

  private void getPathsFromSetting(Settings settings, String key, List<String> results) {
    String[] paths = settings.getStringArray(key);
    for (String path : paths) {
      String value = CppGeneralSettings.resolveValue(settings, path);
      if (value != null) {
        File f = new File(value);
        if (!f.isAbsolute()) {
          f = new File(baseDir, value);
        }
        results.add(f.getPath());
        log.info(String.format("From %s added %s", key, path));
      }
    }
  }

  private String[] getStringArrayBySeparator(String value, String separator) {
    if (value != null) {
      String[] strings = StringUtils.splitByWholeSeparator(value, separator);
      String[] result = new String[strings.length];
      for (int index = 0; index < strings.length; index++) {
        result[index] = StringUtils.trim(strings[index]);
      }
      return result;
    }
    return ArrayUtils.EMPTY_STRING_ARRAY;
  }

}
