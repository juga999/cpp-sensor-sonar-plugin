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

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.SonarPlugin;

public class CppSensorPlugin extends SonarPlugin {

  @SuppressWarnings("rawtypes")
  @Override
  public List getExtensions() {
    List<Object> l = new ArrayList<>();

    l.add(CppLanguage.class);
    l.add(CppSensorQualityProfile.class);
    l.add(CppProjectAnalysisHandler.class);
    l.add(CppProjectSensor.class);
    l.add(CppCpdMapping.class);

    return l;
  }

}
