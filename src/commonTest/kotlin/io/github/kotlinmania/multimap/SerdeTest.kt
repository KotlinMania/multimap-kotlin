// port-lint: source src/serde.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

// Mirrors the upstream `#[cfg(test)] mod tests` block at the bottom of
// `src/serde.rs`. The upstream tests drive `serde_test::assert_tokens`, which
// asserts both the serialize and deserialize sides against a fixed token
// stream. The Kotlin counterpart round-trips the [MultiMap] through JSON and
// asserts both the encoded text and the decoded value, since kotlinx.
// serialization does not vend a token-stream test format.

package io.github.kotlinmania.multimap

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerdeTest {

    private val serializer = MultiMapSerializer(Char.serializer(), UByte.serializer())

    private fun assertTokens(map: MultiMap<Char, UByte>, expected: String) {
        val encoded = Json.encodeToString(serializer, map)
        assertEquals(expected, encoded)
        val decoded = Json.decodeFromString(serializer, expected)
        assertEquals(map, decoded)
    }

    @Test
    fun testEmpty() {
        val map = MultiMap<Char, UByte>()

        assertTokens(map, "{}")
    }

    @Test
    fun testSingle() {
        val map = MultiMap<Char, UByte>()
        map.insert('x', 1u)

        assertTokens(map, """{"x":[1]}""")
    }

    @Test
    fun testMultiple() {
        val map = MultiMap<Char, UByte>()
        map.insert('x', 1u)
        map.insert('x', 3u)
        map.insert('x', 1u)
        map.insert('x', 5u)

        assertTokens(map, """{"x":[1,3,1,5]}""")
    }
}
