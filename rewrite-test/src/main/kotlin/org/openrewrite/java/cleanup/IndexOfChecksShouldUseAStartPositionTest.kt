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
import org.openrewrite.java.JavaParser
import org.openrewrite.java.JavaRecipeTest

interface IndexOfChecksShouldUseAStartPositionTest : JavaRecipeTest {
    override val recipe: Recipe
        get() = IndexOfChecksShouldUseAStartPosition()

    @Test
    fun doNotChangeCompliantRhs(jp: JavaParser) = assertUnchanged(
        before = """
            class Test {
                boolean hasIndex(String str) {
                    return str.indexOf("x", 2) > -1;
                }
            }
        """
    )

    @Test
    fun changeLhsWithLiteral(jp: JavaParser) = assertChanged(
        jp,
        before = """
            class Test {
                boolean hasIndex(String str) {
                    return str.indexOf("x") > 2;
                }
            }
        """,
        after = """
            class Test {
                boolean hasIndex(String str) {
                    return str.indexOf("x", 2) > -1;
                }
            }
        """
    )

    @Test
    fun changeLhsWithMethodInvocation(jp: JavaParser) = assertChanged(
        jp,
        before = """
            class Test {
                boolean hasIndex(String str) {
                    return str.indexOf(testVal()) > 2;
                }
                String testVal() {
                    return "";
                }
            }
        """,
        after = """
            class Test {
                boolean hasIndex(String str) {
                    return str.indexOf(testVal(), 2) > -1;
                }
                String testVal() {
                    return "";
                }
            }
        """
    )

    @Test
    fun changeRhs(jp: JavaParser) = assertChanged(
        jp,
        before = """
            class Test {
                boolean hasIndex(String str) {
                    return 2 < str.indexOf("str");
                }
            }
        """,
        after = """
            class Test {
                boolean hasIndex(String str) {
                    return -1 < str.indexOf("str", 2);
                }
            }
        """
    )
}
