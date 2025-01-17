/*
 * Copyright (c) 2021 Mārtiņš Avots (Martins Avots) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the MIT License,
 * which is available at https://spdx.org/licenses/MIT.html.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * or any later versions with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT OR GPL-2.0-or-later WITH Classpath-exception-2.0
 */
package net.splitcells.gel.data.database;

import net.splitcells.dem.data.set.list.List;
import net.splitcells.dem.environment.resource.ResourceI;
import net.splitcells.dem.object.Discoverable;
import net.splitcells.gel.data.table.attribute.Attribute;
import org.w3c.dom.Element;

import static net.splitcells.dem.Dem.environment;
import static net.splitcells.dem.data.set.list.Lists.*;

public class Databases extends ResourceI<DatabaseFactory> {
    public Databases() {
        super(() -> new DatabaseIFactory());
    }

    public static Database database(String name, Attribute<? extends Object>... attributes) {
        return environment().config().configValue(Databases.class).database(name, attributes);
    }

    public static Database database(Attribute<? extends Object>... attributes) {
        return environment().config().configValue(Databases.class).database(attributes);
    }

    public static Database database(List<Attribute<? extends Object>> attributes, List<List<Object>> linesValues) {
        return environment().config().configValue(Databases.class).database(attributes, linesValues);
    }

    @Deprecated
    public static Database database(String name, Discoverable parent, Attribute<? extends Object>... attributes) {
        return environment().config().configValue(Databases.class).database(name, parent, attributes);
    }

    public static Database database(String name, Discoverable parent, List<Attribute<? extends Object>> attributes) {
        return environment().config().configValue(Databases.class).database(name, parent, attributes);
    }

    public static Database database(List<Attribute<?>> attributes) {
        return environment().config().configValue(Databases.class).database(attributes);
    }

    public static Database database2(List<Attribute<Object>> attributes) {
        return environment().config().configValue(Databases.class).database(attributes.mapped(a -> (Attribute<Object>) a));
    }

    public static Database database2(String name, Discoverable parent, List<Attribute<Object>> attributes) {
        return environment().config().configValue(Databases.class).database2(name, parent, attributes);
    }

    public static Database databaseOfFods(List<Attribute<?>> attributes, Element fods) {
        return environment().config().configValue(Databases.class).databaseOfFods(attributes, fods);
    }

    public static List<Attribute<? extends Object>> objectAttributes(List<Attribute<Object>> attributes) {
        return attributes.stream()
                .map(a -> (Attribute<? extends Object>) a)
                .collect(toList());
    }
}
