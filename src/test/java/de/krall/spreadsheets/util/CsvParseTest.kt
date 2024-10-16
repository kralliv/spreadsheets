package de.krall.spreadsheets.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CsvParseTest {

    @Test
    fun `empty input`() {
        val expected = listOf<List<String>>()
        assertEquals(expected, "".parseCsv())
    }

    @Test
    fun `empty line`() {
        val expected = listOf(
            listOf(""),
        )
        assertEquals(expected, "\n".parseCsv())
    }

    @Test
    fun `empty lines`() {
        val expected = listOf(
            listOf(""),
            listOf(""),
            listOf(""),
        )
        assertEquals(expected, "\n\n\n".parseCsv())
    }

    @Test
    fun `single line`() {
        val expected = listOf(
            listOf("something", "all", "none"),
        )
        assertEquals(expected, "something,all,none".parseCsv())
    }

    @Test
    fun `single line trailing line`() {
        val expected = listOf(
            listOf("something", "all", "none"),
        )
        assertEquals(expected, "something,all,none\n".parseCsv())
    }

    @Test
    fun `multiple line`() {
        val expected = listOf(
            listOf("something", "all", "none"),
            listOf("any", "many", "contains"),
            listOf("some"),
        )
        assertEquals(expected, "something, all , none\nany,many,contains\r\nsome".parseCsv())
    }

    @Test
    fun `multiple line trialing line`() {
        val expected = listOf(
            listOf("something", "all", "none"),
            listOf("any", "many", "contains"),
        )
        assertEquals(expected, "something,all,none\nany,many,contains\n".parseCsv())
    }

    @Test
    fun unescaped() {
        val expected = listOf(
            listOf("something", "all", "none disappear", "split word"),
        )
        assertEquals(expected, " something , all, none disappear, split word".parseCsv())
    }

    @Test
    fun escaped() {
        val expected = listOf(
            listOf(" something ", "all ,none", "split word"),
            listOf("\"\""),
        )
        assertEquals(expected, " \" something \" , \"all ,none\" disappear, \"split word\" \n \"\"\"\"".parseCsv())
    }

    @Test
    fun `escaped eof`() {
        val expected = listOf(
            listOf("something", "split word"),
        )
        assertEquals(expected, "something, \"split word".parseCsv())
    }

    @Test
    fun `leading comma`() {
        val expected = listOf(
            listOf("", "all", "none"),
            listOf("", ""),
        )
        assertEquals(expected, ",all,none\n,".parseCsv())
    }

    @Test
    fun `trailing comma unescaped`() {
        val expected = listOf(
            listOf("something", "all", "none", ""),
        )
        assertEquals(expected, "something,all,none,".parseCsv())
    }

    @Test
    fun `trailing comma unescaped whitespace`() {
        val expected = listOf(
            listOf("something", "all", "none", ""),
        )
        assertEquals(expected, "something,all,none, ".parseCsv())
    }

    @Test
    fun `trailing comma escaped`() {
        val expected = listOf(
            listOf("something", "all", "none", ""),
        )
        assertEquals(expected, "something,all,\"none\",".parseCsv())
    }

    @Test
    fun `trailing comma escaped whitespace`() {
        val expected = listOf(
            listOf("something", "all", "none", ""),
        )
        assertEquals(expected, "something,all,\"none\", ".parseCsv())
    }
}