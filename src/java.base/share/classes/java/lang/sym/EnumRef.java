/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.lang.sym;

import java.lang.annotation.Foldable;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A symbolic reference for an {@code enum} constant.
 *
 * @param <E> the type of the enum constant
 */
public final class EnumRef<E extends Enum<E>> extends DynamicConstantRef<E> {

    /**
     * Construct a symbolic reference for the specified enum class and name
     *
     * @param enumClass the enum class
     * @param constantName the name of the enum constant
     * @throws NullPointerException if any argument is null
     */
    private EnumRef(ClassRef enumClass, String constantName) {
        super(SymbolicRefs.BSM_ENUM_CONSTANT, requireNonNull(constantName), requireNonNull(enumClass));
    }

    /**
     * Return a symbolic reference for the specified enum class and name
     *
     * @param <E> the type of the enum constant
     * @param enumClass the enum class
     * @param constantName the name of the enum constant
     * @return the symbolic reference
     * @throws NullPointerException if any argument is null
     */
    @Foldable
    public static<E extends Enum<E>> EnumRef<E> of(ClassRef enumClass, String constantName) {
        return new EnumRef<>(enumClass, constantName);
    }

    /**
     * Return the enum class for this symbolic reference
     * @return the enum class
     */
    @Foldable
    public ClassRef enumClass() {
        return type();
    }

    /**
     * Return the enum constant name for this symbolic reference
     * @return the enum constant name
     */
    @Foldable
    public String constantName() {
        return name();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E resolveRef(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        return Enum.valueOf((Class<E>) enumClass().resolveRef(lookup), constantName());
    }

    @Override
    public Optional<? extends SymbolicRef<E>> toSymbolicRef(MethodHandles.Lookup lookup) {
        Optional<? extends SymbolicRef<Class<?>>> classRefRef = enumClass().toSymbolicRef(lookup);
        if (!classRefRef.isPresent())
            return Optional.empty();
        return Optional.of(DynamicConstantRef.<E>of(SymbolicRefs.BSM_INVOKE, name(), SymbolicRefs.CR_EnumRef)
                                   .withArgs(SymbolicRefs.MHR_ENUMREF_FACTORY, classRefRef.get(), constantName()));
    }

    @Override
    public String toString() {
        return String.format("EnumRef[%s.%s]", enumClass().canonicalName(), constantName());
    }
}
