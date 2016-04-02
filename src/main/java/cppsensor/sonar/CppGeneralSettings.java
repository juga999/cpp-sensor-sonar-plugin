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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Settings;

public class CppGeneralSettings {

  private static final Logger log = LoggerFactory.getLogger(CppGeneralSettings.class);

  public static final String PLUGIN_PREFIX = "sonar.cppsensor";

  public static final String VAR_PREFIX = PLUGIN_PREFIX + ".var";

  public static final String INCLUDE_DIRECTORIES_KEY = PLUGIN_PREFIX + ".incDirs";

  public static final String FORCE_INCLUDE_FILES_KEY = PLUGIN_PREFIX + ".forceIncludes";

  public static final String EXCLUDE_INDEX_PATTERN_KEY = PLUGIN_PREFIX + ".excludeIndexPatterns";

  private static final Pattern VAR_PATTERN = Pattern.compile("[$%]?\\{(.*?)\\}");

  private CppGeneralSettings() {}

  public static String resolveValue(Settings settings, String rawValue) {
    StringBuffer sb = new StringBuffer(rawValue.length());

    Matcher matcher = VAR_PATTERN.matcher(rawValue);

    while (matcher.find()) {
      String value = null;
      String var = matcher.group(1);
      try {
        value = System.getenv(var);
      } catch (Exception e) {
        log.error("Failed to access environment variable "+var, e);
        value = null;
      }
      if (value == null) {
        value = settings.getString(VAR_PREFIX + "." + var);
      }
      if (value == null) {
        log.warn(String.format(
            "Unknown environment variable %s, ignoring %s",
            var, rawValue));
        return null;
      }
      matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
    }
    matcher.appendTail(sb);

    return sb.toString();
  }
}
