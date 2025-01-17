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
import net.splitcells.dem.object.Discoverable;
import net.splitcells.gel.data.table.attribute.Attribute;

import static net.splitcells.dem.data.set.list.Lists.listWithValuesOf;

public class DatabaseRefFactory implements DatabaseFactory {
    @Override
    public Database database(String name, Attribute<?>... header) {
        return new DatabaseIRef(name, null, header);
    }

    @Override
    public Database database(Attribute<?>... header) {
        return new DatabaseIRef(header);
    }

    @Override
    public Database database(List<Attribute<?>> header) {
        return new DatabaseIRef(header);
    }

    @Override
    public Database database2(String name, Discoverable parent, List<Attribute<Object>> attributes) {
        return new DatabaseIRef(name, parent, attributes);
    }

    @Override
    public Database database(List<Attribute<?>> headers, List<List<Object>> linesValues) {
        return new DatabaseIRef(headers, linesValues);
    }

    @Override
    public Database database(String name, Discoverable parent, Attribute<? extends Object>... header) {
        return new DatabaseIRef(name, parent, listWithValuesOf(header).mapped(a -> (Attribute<Object>) a));
    }

    @Override
    public Database database(String name, Discoverable parent, List<Attribute<? extends Object>> header) {
        return new DatabaseIRef(name, parent, header.mapped(a -> (Attribute<Object>) a));
    }

    @Override
    public void close() {

    }

    @Override
    public void flush() {

    }
}
