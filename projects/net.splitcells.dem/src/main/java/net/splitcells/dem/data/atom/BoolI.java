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
package net.splitcells.dem.data.atom;

import net.splitcells.dem.lang.annotations.JavaLegacy;
import net.splitcells.dem.utils.CommonFunctions;

public class BoolI implements Bool {
    private final boolean value;

    @Deprecated
    public BoolI(final boolean arg_val) {
        value = arg_val;
    }

    @Override
    public boolean isTrue() {
        return value;
    }

    @Override
    public boolean isFalse() {
        return !value;
    }

    @Override
    public Bool or(Bool arg) {
        return new BoolI(value || arg.isTrue());
    }

    @Override
    public Bool set(boolean arg) {
        return new BoolI(arg);
    }

    @Override
    public Bool xor(Bool arg) {
        return new BoolI(value != arg.isTrue());
    }

    @Override
    public Bool not() {
        return new BoolI(!value);
    }

    @Override
    public Bool and(Bool arg) {
        return new BoolI(value && arg.isTrue());
    }

    @Override
    public Bool nand(Bool arg) {
        return this.and(arg).not();
    }

    @Override
    public Bool nor(Bool arg) {
        return or(arg).not();
    }

    @Override
    public Bool xnor(Bool arg) {
        return xor(arg).not();
    }

    public Bool equals(Bool arg) {
        if (value == arg.isTrue()) {
            return new BoolI(true);
        } else {
            return new BoolI(false);
        }
    }

    @Override
    public boolean equals(Object arg) {
        return equals((Bool) arg).isTrue();
    }

    @Override
    public int hashCode() {
        return CommonFunctions.hashCode(value);
    }

    @Override
    public BoolI clone() {
        return deepClone(BoolI.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R deepClone(Class<? extends R> arg) {
        return (R) new BoolI(this.value);
    }

    @JavaLegacy
    @Override
    public boolean toJavaPrimitive() {
        return value;
    }

    @Deprecated
    public <A extends Bool> Bool equalContent(A arg) {
        return Bools.bool(value == arg.toJavaPrimitive());
    }

}
