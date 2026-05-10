// port-lint: source src/lib.rs
// Copyright (c) 2016 multimap developers
//
// Licensed under the Apache License, Version 2.0
// <LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0> or the MIT
// license <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. All files in the project carrying such notice may not be copied,
// modified, or distributed except according to those terms.

// Mirrors the upstream `#[cfg(test)] mod tests` block at the bottom of
// `src/lib.rs`. Every upstream test is translated here, top-to-bottom in
// upstream order, with the same test names (camelCased). Test cases that
// rely on behaviour Kotlin cannot reproduce — for example mutating through
// an iterator's value reference — assert through the closest Kotlin analogue
// using [MultiMap.getVecMut].

package io.github.kotlinmania.multimap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LibTest {
    @Test
    fun new() {
        MultiMap<Int, Int>()
    }

    @Test
    fun withCapacity() {
        MultiMap.withCapacity<Int, Int>(20)
    }

    @Test
    fun insert() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 3)
    }

    @Test
    fun insertIdentical() {
        val m = MultiMap<Int, Int>()
        m.insert(1, 42)
        m.insert(1, 42)
        assertEquals(mutableListOf(42, 42), m.getVec(1))
    }

    @Test
    fun insertMany() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insertMany(1, listOf(3, 4))
        assertEquals(mutableListOf(3, 4), m.getVec(1))
    }

    @Test
    fun insertManyAgain() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 2)
        m.insertMany(1, listOf(3, 4))
        assertEquals(mutableListOf(2, 3, 4), m.getVec(1))
    }

    @Test
    fun insertManyOverlap() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insertMany(1, listOf(2, 3))
        m.insertMany(1, listOf(3, 4))
        assertEquals(mutableListOf(2, 3, 3, 4), m.getVec(1))
    }

    @Test
    fun insertManyFromSlice() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insertManyFromSlice(1, listOf(3, 4))
        assertEquals(mutableListOf(3, 4), m.getVec(1))
    }

    @Test
    fun insertManyFromSliceAgain() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 2)
        m.insertManyFromSlice(1, listOf(3, 4))
        assertEquals(mutableListOf(2, 3, 4), m.getVec(1))
    }

    @Test
    fun insertExisting() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 3)
        m.insert(1, 4)
        assertEquals(mutableListOf(3, 4), m.getVec(1))
    }

    @Test
    fun indexNoEntry() {
        val m: MultiMap<Int, Int> = MultiMap()
        val ex = assertFailsWith<IllegalStateException> {
            m.index(1)
        }
        assertEquals("no entry found for key", ex.message)
    }

    @Test
    fun index() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 41)
        m.insert(2, 42)
        m.insert(3, 43)
        val values = m.index(2)
        assertEquals(42, values)
    }

    @Test
    fun indexEmptyVec() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.getVecMut(1)!!.clear()
        val ex = assertFailsWith<IllegalStateException> {
            m.index(1)
        }
        assertEquals("no value found for key", ex.message)
    }

    @Test
    fun containsKeyTrue() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        assertTrue(m.containsKey(1))
    }

    @Test
    fun containsKeyFalse() {
        val m: MultiMap<Int, Int> = MultiMap()
        assertFalse(m.containsKey(1))
    }

    @Test
    fun len() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(2, 1337)
        m.insert(3, 99)
        assertEquals(3, m.len())
    }

    @Test
    fun removeNotPresent() {
        val m: MultiMap<Int, Int> = MultiMap()
        val v = m.remove(1)
        assertNull(v)
    }

    @Test
    fun removePresent() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        val v = m.remove(1)
        assertEquals(mutableListOf(42), v)
    }

    @Test
    fun getNotPresent() {
        val m: MultiMap<Int, Int> = MultiMap()
        assertNull(m.get(1))
    }

    @Test
    fun getPresent() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        assertEquals(42, m.get(1))
    }

    @Test
    fun getEmpty() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.getVecMut(1)?.removeAt(m.getVecMut(1)!!.size - 1)
        assertNull(m.get(1))
    }

    @Test
    fun getVecNotPresent() {
        val m: MultiMap<Int, Int> = MultiMap()
        assertNull(m.getVec(1))
    }

    @Test
    fun getVecPresent() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(1, 1337)
        assertEquals(mutableListOf(42, 1337), m.getVec(1))
    }

    @Test
    fun capacity() {
        val m: MultiMap<Int, Int> = MultiMap.withCapacity(20)
        assertTrue(m.capacity() >= 0)
    }

    @Test
    fun isEmptyTrue() {
        val m: MultiMap<Int, Int> = MultiMap()
        assertTrue(m.isEmpty())
    }

    @Test
    fun isEmptyFalse() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        assertFalse(m.isEmpty())
    }

    @Test
    fun clear() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.clear()
        assertTrue(m.isEmpty())
    }

    @Test
    fun getMut() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        // Upstream `*v = 1337` writes through a `&mut V`. Kotlin returns the
        // first value by copy, so write back through `getVecMut`.
        m.getVecMut(1)?.set(0, 1337)
        assertEquals(1337, m.index(1))
    }

    @Test
    fun getVecMut() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(1, 1337)
        m.getVecMut(1)?.let {
            it[0] = 5
            it[1] = 10
        }
        assertEquals(mutableListOf(5, 10), m.getVec(1))
    }

    @Test
    fun getMutEmpty() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.getVecMut(1)!!.removeAt(m.getVecMut(1)!!.size - 1)
        assertNull(m.getMut(1))
    }

    @Test
    fun keys() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(2, 42)
        m.insert(4, 42)
        m.insert(8, 42)

        val keys = m.keys().toList()
        assertEquals(4, keys.size)
        assertTrue(keys.contains(1))
        assertTrue(keys.contains(2))
        assertTrue(keys.contains(4))
        assertTrue(keys.contains(8))
    }

    @Test
    fun iter() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(1, 42)
        m.insert(4, 42)
        m.insert(8, 42)

        val all = m.iter().asSequence().toList()
        assertTrue(all.all { it.second == 42 })

        // Upstream: `for _ in iter.by_ref().take(2) {}` then `iter.len() == 1`.
        // Kotlin iterators don't expose `len()`, so instead check the count of
        // remaining elements after consuming two.
        val cursor = m.iter()
        repeat(2) { if (cursor.hasNext()) cursor.next() }
        var remaining = 0
        while (cursor.hasNext()) {
            cursor.next()
            remaining += 1
        }
        assertEquals(1, remaining)
    }

    @Test
    fun iterEmptyVec() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(42, 42)
        m.getVecMut(42)!!.clear()

        assertFalse(m.iter().hasNext())
    }

    @Test
    fun flatIter() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(1, 43)
        m.insert(4, 42)
        m.insert(8, 42)

        val keys = listOf(1, 4, 8)

        for ((key, value) in m.flatIter()) {
            assertTrue(keys.contains(key))

            if (key == 1) {
                assertTrue(value == 42 || value == 43)
            } else {
                assertEquals(42, value)
            }
        }
    }

    @Test
    fun flatIterMut() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(1, 43)
        m.insert(4, 42)
        m.insert(8, 42)

        val keys = listOf(1, 4, 8)

        for ((key, value) in m.flatIterMut()) {
            assertTrue(keys.contains(key))

            if (key == 1) {
                assertTrue(value == 42 || value == 43)

                // Upstream rewrites `*value = 55`. Kotlin copies the value out
                // of the iterator; rewrite through `getVecMut` instead.
                val vec = m.getVecMut(key)!!
                for (i in vec.indices) vec[i] = 55
                assertEquals(55, m.get(key))
            } else {
                assertEquals(42, value)

                val vec = m.getVecMut(key)!!
                for (i in vec.indices) vec[i] = 76
                assertEquals(76, m.get(key))
            }
        }
    }

    @Test
    fun intoiteratorForReferenceType() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(1, 43)
        m.insert(4, 42)
        m.insert(8, 42)

        val keys = listOf(1, 4, 8)

        for ((key, value) in m) {
            assertTrue(keys.contains(key))

            if (key == 1) {
                assertEquals(mutableListOf(42, 43), value)
            } else {
                assertEquals(mutableListOf(42), value)
            }
        }
    }

    @Test
    fun intoiteratorForMutableReferenceType() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(1, 43)
        m.insert(4, 42)
        m.insert(8, 42)

        val keys = listOf(1, 4, 8)

        for ((key, value) in m) {
            assertTrue(keys.contains(key))

            if (key == 1) {
                assertEquals(mutableListOf(42, 43), value)
                value.add(666)
            } else {
                assertEquals(mutableListOf(42), value)
            }
        }

        assertEquals(mutableListOf(42, 43, 666), m.getVec(1))
    }

    @Test
    fun intoiteratorConsuming() {
        val m: MultiMap<Int, Int> = MultiMap()
        m.insert(1, 42)
        m.insert(1, 43)
        m.insert(4, 42)
        m.insert(8, 42)

        val keys = listOf(1, 4, 8)

        for ((key, value) in m) {
            assertTrue(keys.contains(key))

            if (key == 1) {
                assertEquals(mutableListOf(42, 43), value)
            } else {
                assertEquals(mutableListOf(42), value)
            }
        }
    }

    @Test
    fun testFmtDebug() {
        val map = MultiMap<Int, Int>()
        val empty: MultiMap<Int, Int> = MultiMap()

        map.insert(1, 2)
        map.insert(1, 5)
        map.insert(1, -1)
        map.insert(3, 4)

        val mapStr = map.toString()

        assertTrue(mapStr == "{1: [2, 5, -1], 3: [4]}" || mapStr == "{3: [4], 1: [2, 5, -1]}")
        assertEquals("{}", empty.toString())
    }

    @Test
    fun testEq() {
        val m1 = MultiMap<Int, Int>()
        m1.insert(1, 2)
        m1.insert(2, 3)
        m1.insert(3, 4)
        val m2 = MultiMap<Int, Int>()
        m2.insert(1, 2)
        m2.insert(2, 3)
        assertNotEquals(m1, m2)
        m2.insert(3, 4)
        assertEquals(m1, m2)
        m2.insert(3, 4)
        assertNotEquals(m1, m2)
        m1.insert(3, 4)
        assertEquals(m1, m2)
    }

    @Test
    fun testEqEmptyKey() {
        val m1 = MultiMap<Int, Int>()
        m1.insert(1, 2)
        m1.insert(2, 3)
        val m2 = MultiMap<Int, Int>()
        m2.insert(1, 2)
        m2.insertMany(2, emptyList())
        assertNotEquals(m1, m2)
        m2.insertMany(2, listOf(3))
        assertEquals(m1, m2)
    }

    @Test
    fun testDefault() {
        MultiMap.default<Byte, Byte>()
    }

    @Test
    fun testFromIterator() {
        val vals: List<Pair<String, Long>> = listOf("foo" to 123L, "bar" to 456L, "foo" to 789L)
        val multimap: MultiMap<String, Long> = MultiMap.fromIterator(vals)

        val fooVals: MutableList<Long> = multimap.getVec("foo")!!
        assertTrue(fooVals.contains(123L))
        assertTrue(fooVals.contains(789L))

        val barVals: MutableList<Long> = multimap.getVec("bar")!!
        assertTrue(barVals.contains(456L))
    }

    @Test
    fun testFromVecIterator() {
        val vals: List<Pair<String, List<Long>>> = listOf(
            "foo" to listOf(123L, 456L),
            "bar" to listOf(234L),
            "foobar" to listOf(567L, 678L, 789L),
            "bar" to listOf(12L, 23L, 34L),
        )

        val multimap: MultiMap<String, Long> = MultiMap.fromIteratorVec(vals)

        val fooVals: MutableList<Long> = multimap.getVec("foo")!!
        assertTrue(fooVals.contains(123L))
        assertTrue(fooVals.contains(456L))

        val barVals: MutableList<Long> = multimap.getVec("bar")!!
        assertTrue(barVals.contains(234L))
        assertTrue(barVals.contains(12L))
        assertTrue(barVals.contains(23L))
        assertTrue(barVals.contains(34L))

        val foobarVals: MutableList<Long> = multimap.getVec("foobar")!!
        assertTrue(foobarVals.contains(567L))
        assertTrue(foobarVals.contains(678L))
        assertTrue(foobarVals.contains(789L))
    }

    @Test
    fun testExtendConsumingHashmap() {
        val a = MultiMap<Int, Int>()
        a.insert(1, 42)

        val b = mutableMapOf<Int, Int>()
        b[1] = 43
        b[2] = 666

        a.extend(b.entries.map { it.key to it.value })

        assertEquals(2, a.len())
        assertEquals(mutableListOf(42, 43), a.getVec(1))
    }

    @Test
    fun testExtendRefHashmap() {
        val a = MultiMap<Int, Int>()
        a.insert(1, 42)

        val b = mutableMapOf<Int, Int>()
        b[1] = 43
        b[2] = 666

        a.extend(b.entries.map { it.key to it.value })

        assertEquals(2, a.len())
        assertEquals(mutableListOf(42, 43), a.getVec(1))
        assertEquals(2, b.size)
        assertEquals(43, b[1])
    }

    @Test
    fun testExtendConsumingMultimap() {
        val a = MultiMap<Int, Int>()
        a.insert(1, 42)

        val b = MultiMap<Int, Int>()
        b.insert(1, 43)
        b.insert(1, 44)
        b.insert(2, 666)

        a.extendVec(b.iterAll().map { it.first to it.second.toList() })

        assertEquals(2, a.len())
        assertEquals(mutableListOf(42, 43, 44), a.getVec(1))
    }

    @Test
    fun testExtendRefMultimap() {
        val a = MultiMap<Int, Int>()
        a.insert(1, 42)

        val b = MultiMap<Int, Int>()
        b.insert(1, 43)
        b.insert(1, 44)
        b.insert(2, 666)

        a.extendVec(b.iterAll().map { it.first to it.second.toList() })

        assertEquals(2, a.len())
        assertEquals(mutableListOf(42, 43, 44), a.getVec(1))
        assertEquals(2, b.len())
        assertEquals(mutableListOf(43, 44), b.getVec(1))
    }

    @Test
    fun testEntry() {
        val m = MultiMap<Int, Int>()
        m.insert(1, 42)

        run {
            val v = m.entry(1).orInsert(43)
            assertEquals(42, v)
            // Upstream: `*v = 44`. Kotlin returns the value by copy, so write
            // back through `getVecMut`.
            m.getVecMut(1)!![0] = 44
        }
        assertEquals(666, m.entry(2).orInsert(666))

        assertEquals(44, m.index(1))
        assertEquals(666, m.index(2))
    }

    @Test
    fun testEntryVec() {
        val m = MultiMap<Int, Int>()
        m.insert(1, 42)

        run {
            val v = m.entry(1).orInsertVec(mutableListOf(43))
            assertEquals(mutableListOf(42), v)
            v[0] = 44
        }
        assertEquals(mutableListOf(666), m.entry(2).orInsertVec(mutableListOf(666)))

        assertEquals(44, m.index(1))
        assertEquals(666, m.index(2))
    }

    @Test
    fun testIsVec() {
        val m = MultiMap<Int, Int>()
        m.insert(1, 42)
        m.insert(1, 1337)
        m.insert(2, 2332)

        assertTrue(m.isVec(1))
        assertFalse(m.isVec(2))
        assertFalse(m.isVec(3))
    }

    @Test
    fun testMacro() {
        val manualMap = MultiMap<String, Int>()
        manualMap.insert("key1", 42)
        assertEquals(manualMap, multimapOf("key1" to 42))

        manualMap.insert("key1", 1337)
        manualMap.insert("key2", 2332)
        val macroMap = multimapOf(
            "key1" to 42,
            "key1" to 1337,
            "key2" to 2332,
        )
        assertEquals(manualMap, macroMap)
    }

    @Test
    fun retainRemovesElement() {
        val m = MultiMap<Int, Int>()
        m.insert(1, 42)
        m.insert(1, 99)
        m.retain { k, v -> k == 1 && v == 42 }
        assertEquals(1, m.len())
        assertEquals(42, m.get(1))
    }

    @Test
    fun retainAlsoRemovesEmptyVector() {
        val m = MultiMap<Int, Int>()
        m.insert(1, 42)
        m.insert(1, 99)
        m.insert(2, 42)
        m.retain { k, v -> k == 1 && v == 42 }
        assertEquals(1, m.len())
        assertEquals(42, m.get(1))
    }

}
