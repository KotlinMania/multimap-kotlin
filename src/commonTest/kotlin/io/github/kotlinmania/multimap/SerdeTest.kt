// port-lint: tests src/serde.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

package io.github.kotlinmania.multimap

import io.github.kotlinmania.serdecore.de.CharDeserialize
import io.github.kotlinmania.serdecore.de.U8Deserialize
import io.github.kotlinmania.serdetest.Token
import io.github.kotlinmania.serdetest.assertTokens
import kotlin.test.Test

class SerdeTest {
    private val deserialize = multiMapDeserialize(CharDeserialize, U8Deserialize)

    @Test
    fun testEmpty() {
        val map = MultiMap<Char, UByte>()

        assertTokens(
            map,
            deserialize,
            listOf(Token.Map(0), Token.MapEnd),
        )
    }

    @Test
    fun testSingle() {
        val map = MultiMap<Char, UByte>()
        map.insert('x', 1u)

        assertTokens(
            map,
            deserialize,
            listOf(
                Token.Map(1),
                Token.CharValue('x'),
                Token.Seq(1),
                Token.U8(1u),
                Token.SeqEnd,
                Token.MapEnd,
            ),
        )
    }

    @Test
    fun testMultiple() {
        val map = MultiMap<Char, UByte>()
        map.insert('x', 1u)
        map.insert('x', 3u)
        map.insert('x', 1u)
        map.insert('x', 5u)

        assertTokens(
            map,
            deserialize,
            listOf(
                Token.Map(1),
                Token.CharValue('x'),
                Token.Seq(4),
                Token.U8(1u),
                Token.U8(3u),
                Token.U8(1u),
                Token.U8(5u),
                Token.SeqEnd,
                Token.MapEnd,
            ),
        )
    }
}
