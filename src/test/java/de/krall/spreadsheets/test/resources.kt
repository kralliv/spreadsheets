@file:OptIn(ExperimentalPathApi::class)

package de.krall.spreadsheets.test

import java.net.URI
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.name
import kotlin.io.path.toPath
import kotlin.io.path.walk

class TestCaseResource(
    val directory: Path,
    val name: String,
    val extension: String,
) {

    val inputFile: Path
        get() = file("input")

    val outputFile: Path
        get() = file("output")

    fun file(type: String): Path = directory.resolve("$name.$type.$extension")

    companion object {

        fun resolve(path: String, extension: String): List<TestCaseResource> {
            val resourceDirectory = resolveTestResourceDirectory().resolve(path)

            val suffix = ".input.$extension"

            return resourceDirectory.walk()
                .filter { it.name.endsWith(suffix) }
                .map { TestCaseResource(it.parent, it.name.removeSuffix(suffix), extension) }
                .toList()
        }
    }
}

fun resolveTestResourceDirectory(): Path {
    val url = Ref::class.java.protectionDomain.codeSource.location ?: error("cannot resolve test directory root")
    val projectDirectory = url.toString().substringBefore("/target") + "/src/test/resources/"
    return URI(projectDirectory).toPath()
}

private object Ref
