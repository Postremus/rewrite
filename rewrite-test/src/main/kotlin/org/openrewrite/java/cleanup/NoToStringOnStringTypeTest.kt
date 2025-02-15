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
package org.openrewrite.java.cleanup

import org.junit.jupiter.api.Test
import org.openrewrite.Recipe
import org.openrewrite.java.JavaRecipeTest

interface NoToStringOnStringTypeTest : JavaRecipeTest {
    override val recipe: Recipe?
        get() = NoToStringOnStringType()

    @Test
    fun doNotChangeOnObject() = assertUnchanged(
        before = """
            class Test {
                String method() {
                    Object obj;
                    return obj.toString();
                }
            }
        """
    )

    @Test
    fun toStringOnString() = assertChanged(
        before = """
            class Test {
                String method() {
                    return "hello".toString();
                }
            } 
        """,
        after = """
            class Test {
                String method() {
                    return "hello";
                }
            } 
        """
    )

    @Test
    fun toStringOnStringVariable() = assertChanged(
        before = """
            class Test {
                String method(String val) {
                    return val.toString();
                }
            } 
        """,
        after = """
            class Test {
                String method(String val) {
                    return val;
                }
            } 
        """
    )

    @Test
    fun toStringOnMethodInvocation() = assertChanged(
        before = """
            class Test {
                void method1() {
                    String a = method2().toString();
                }
                String method2() {
                    return "";
                }
            }
        """,
        after = """
            class Test {
                void method1() {
                    String a = method2();
                }
                String method2() {
                    return "";
                }
            }
        """
    )
}
