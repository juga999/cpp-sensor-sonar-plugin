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

import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.resources.Language;

public class CppLanguage extends AbstractLanguage {

  public static final String DEFAULT_SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";

  public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

  public static final String DEFAULT_C_FILES = "*.c,*.C";

  public static final String KEY = "c++";

  public CppLanguage() {
    super(KEY, "c/c++");
  }

  public static Language get() {
    return new CppLanguage();
  }

  @Override
  public String[] getFileSuffixes() {
    String suffixes = String.format("%s,%s",
        DEFAULT_SOURCE_SUFFIXES,
        DEFAULT_HEADER_SUFFIXES);
    return suffixes.split(",");
  }

}
