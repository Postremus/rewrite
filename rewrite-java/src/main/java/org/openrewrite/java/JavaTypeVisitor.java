/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java;

import org.openrewrite.Cursor;
import org.openrewrite.Incubating;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.JavaType;

import java.util.List;

@Incubating(since = "7.13.0")
public class JavaTypeVisitor<P> {
    private Cursor cursor = new Cursor(null, "root");

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public @Nullable <JT extends JavaType> List<JT> visit(@Nullable List<JT> javaTypes, P p) {
        //noinspection unchecked
        return ListUtils.map(javaTypes, jt -> (JT) visit(jt, p));
    }

    @Nullable
    public JavaType preVisit(JavaType javaType, P p) {
        return javaType;
    }

    /**
     * By calling this method, you are asserting that you know that the outcome will be non-null
     * when the compiler couldn't otherwise prove this to be the case. This method is a shortcut
     * for having to assert the non-nullability of the returned tree.
     *
     * @param javaType A non-null type.
     * @param p    A state object that passes through the visitor.
     * @return A non-null type.
     */
    public JavaType visitNonNull(JavaType javaType, P p) {
        JavaType t = visit(javaType, p);
        //noinspection ConstantConditions
        assert t != null;
        return t;
    }

    public JavaType visit(@Nullable JavaType javaType, P p) {
        if (javaType != null) {
            cursor = new Cursor(cursor, javaType);
            javaType = preVisit(javaType, p);

            if (javaType instanceof JavaType.Array) {
                return visitArray((JavaType.Array) javaType, p);
            } else if (javaType instanceof JavaType.Class) {
                return visitClass((JavaType.Class) javaType, p);
            } else if (javaType instanceof JavaType.Cyclic) {
                return visitCyclic((JavaType.Cyclic) javaType, p);
            } else if (javaType instanceof JavaType.GenericTypeVariable) {
                return visitGenericTypeVariable((JavaType.GenericTypeVariable) javaType, p);
            } else if (javaType instanceof JavaType.Method) {
                return visitMethod((JavaType.Method) javaType, p);
            } else if (javaType instanceof JavaType.MultiCatch) {
                return visitMultiCatch((JavaType.MultiCatch) javaType, p);
            } else if (javaType instanceof JavaType.Parameterized) {
                return visitParameterized((JavaType.Parameterized) javaType, p);
            } else if (javaType instanceof JavaType.Primitive) {
                return visitPrimitive((JavaType.Primitive) javaType, p);
            } else if (javaType instanceof JavaType.ShallowClass) {
                return visitShallowClass((JavaType.ShallowClass) javaType, p);
            } else if (javaType instanceof JavaType.Variable) {
                return visitVariable((JavaType.Variable) javaType, p);
            }

            cursor = cursor.getParentOrThrow();
        }

        //noinspection ConstantConditions
        return null;
    }

    public JavaType visitMultiCatch(JavaType.MultiCatch multiCatch, P p) {
        return multiCatch.withThrowableTypes(ListUtils.map(multiCatch.getThrowableTypes(), tt -> visit(tt, p)));
    }

    public JavaType visitArray(JavaType.Array array, P p) {
        return visit(array.getElemType(), p);
    }

    public JavaType visitClass(JavaType.Class aClass, P p) {
        JavaType.Class c = aClass;
        c = c.withAnnotations(ListUtils.map(c.getAnnotations(), a -> (JavaType.FullyQualified) visit(a, p)));
        c = c.withSupertype((JavaType.FullyQualified) visit(c.getSupertype(), p));
        c = c.withInterfaces(ListUtils.map(c.getInterfaces(), i -> (JavaType.FullyQualified) visit(i, p)));
        c = c.withMembers(ListUtils.map(c.getMembers(), m -> (JavaType.Variable) visit(m, p)));
        c = c.withMethods(ListUtils.map(c.getMethods(), m -> (JavaType.Method) visit(m, p)));
        c = c.withOwningClass((JavaType.FullyQualified) visit(c.getOwningClass(), p));
        return c;
    }

    public JavaType.Cyclic visitCyclic(JavaType.Cyclic cyclic, P p) {
        return cyclic;
    }

    public JavaType visitGenericTypeVariable(JavaType.GenericTypeVariable generic, P p) {
        return visit(generic.getBound(), p);
    }

    public JavaType visitMethod(JavaType.Method method, P p) {
        JavaType.Method m = method;

        m = m.withAnnotations(ListUtils.map(m.getAnnotations(), a -> (JavaType.FullyQualified) visit(a, p)));
        m = m.withDeclaringType((JavaType.FullyQualified) visit(m.getDeclaringType(), p));

        JavaType.Method.Signature genericSignature = m.getGenericSignature();
        if (genericSignature != null) {
            m = m.withGenericSignature(genericSignature
                    .withReturnType(visit(genericSignature.getReturnType(), p))
                    .withParamTypes(ListUtils.map(genericSignature.getParamTypes(), pt -> visit(pt, p))));
        }

        JavaType.Method.Signature resolvedSignature = m.getResolvedSignature();
        if (resolvedSignature != null) {
            m = m.withResolvedSignature(resolvedSignature
                    .withReturnType(visit(resolvedSignature.getReturnType(), p))
                    .withParamTypes(ListUtils.map(resolvedSignature.getParamTypes(), pt -> visit(pt, p))));
        }

        m = m.withThrownExceptions(ListUtils.map(m.getThrownExceptions(), t -> (JavaType.FullyQualified) visit(t, p)));
        return m;
    }

    public JavaType visitParameterized(JavaType.Parameterized parameterized, P p) {
        JavaType.Parameterized pa = parameterized;
        pa = pa.withType((JavaType.FullyQualified) visit(pa.getType(), p));
        pa = pa.withTypeParameters(ListUtils.map(pa.getTypeParameters(), t -> visit(t, p)));
        return pa;
    }

    public JavaType visitPrimitive(JavaType.Primitive primitive, P p) {
        return primitive;
    }

    public JavaType visitShallowClass(JavaType.ShallowClass shallow, P p) {
        return shallow;
    }

    public JavaType visitVariable(JavaType.Variable variable, P p) {
        JavaType.Variable v = variable;
        v = v.withAnnotations(ListUtils.map(v.getAnnotations(), a -> (JavaType.FullyQualified) visit(a, p)));
        v = v.withOwner((JavaType.FullyQualified) visit(variable.getOwner(), p));
        v = v.withType(visit(variable.getType(), p));
        return v;
    }
}
