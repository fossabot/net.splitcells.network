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
package net.splitcells.dem.data.set.list;

import net.splitcells.dem.data.set.SetWA;
import net.splitcells.dem.lang.annotations.Returns_this;

import java.util.Collection;

/**
 * TODOC What is the difference between append and add? -> add set specific and
 * the method does not guarrenties that is added to the of the list. Append adds
 * to end.
 */
public interface ListWA<T> extends SetWA<T> {
	@Returns_this
	<R extends ListWA<T>> R append(T arg);

	@Override
	default <R extends SetWA<T>> R add(T arg) {
		return append(arg);
	}

	@SuppressWarnings("unchecked")
	@Returns_this
	default <R extends ListWA<T>> R appendAll(T... arg) {
		for (int i = 0; i < arg.length; ++i) {
			append(arg[i]);
		}
		return (R) this;
	}

	@SuppressWarnings("unchecked")
	@Returns_this
	default <R extends ListWA<T>> R appendAll(Collection<T> arg) {
		arg.forEach(e -> this.append(e));
		return (R) this;
	}

	@Override
	default <R extends SetWA<T>> R addAll(Collection<T> value) {
		return appendAll(value);
	}
}
