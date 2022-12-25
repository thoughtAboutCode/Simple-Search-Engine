package search

import java.io.File

abstract class Menu {
    abstract val name: String

    abstract fun action(lines: List<String>): Boolean

    override fun toString() = name
}

enum class STRATEGIE {
    ALL, ANY, NONE
}

private val menu = mapOf(1 to object : Menu() {

    override val name: String = "Find a person"

    private fun allStrategie(query: String) {
        val result = query.split("\\s+".toRegex()).mapNotNull {
            invertedIndex[it]
        }
        if (result.isNotEmpty()) {
            val reducedResult = result.reduce { acc, ints ->
                acc.intersect(ints.toSet()).toList()
            }
            if (reducedResult.isNotEmpty()) {
                println("${reducedResult.size} person(s) found:")
                reducedResult.forEach { index ->
                    println(lines[index])
                }
            } else {
                println("No matching people found.")
            }
        } else {
            println("No matching people found.")
        }
    }

    private fun anyStrategie(query: String) {
        val result = query.split("\\s+".toRegex()).mapNotNull {
            invertedIndex[it]
        }.flatten().toSet()
        if (result.isNotEmpty()) {
            println("${result.size} person(s) found:")
            result.forEach { index ->
                println(lines[index])
            }
        } else {
            println("No matching people found.")
        }
    }

    private fun noneStrategie(query: String) {
        val result = query.split("\\s+".toRegex()).mapNotNull {
            invertedIndex[it]
        }.flatten().toSet()
        val targetIndices = lines.indices.toSet() - result
        if (targetIndices.isNotEmpty()) {
            println("${targetIndices.size} person(s) found:")
            targetIndices.forEach { index ->
                println(lines[index])
            }
        } else {
            println("No matching people found.")
        }
    }

    override fun action(lines: List<String>): Boolean {
        println("Select a matching strategy: ${STRATEGIE.values().joinToString()}")
        val choice = readln()
        println("Enter a name or email to search all matching people. ")
        val words = readln().lowercase()
        when (STRATEGIE.valueOf(choice)) {
            STRATEGIE.ALL -> allStrategie(words)
            STRATEGIE.ANY -> anyStrategie(words)
            STRATEGIE.NONE -> noneStrategie(words)
        }
        return true
    }
}, 2 to object : Menu() {
    override val name: String = "Print all persons"

    override fun action(lines: List<String>): Boolean {
        println("=== List of people ===")
        lines.forEach(::println)
        return true
    }
}, 0 to object : Menu() {
    override val name: String = "Exit"

    override fun action(lines: List<String>): Boolean {
        println("Bye!")
        return false
    }
})

private lateinit var lines: List<String>
private lateinit var invertedIndex: Map<String, List<Int>>

fun dataBuild(file: File) {
    lines = file.readLines()
    invertedIndex = lines.asSequence().map {
        it.split("\\s+".toRegex()).map(String::lowercase)
    }.foldIndexed(mutableMapOf()) { index, acc, strings ->
        acc.apply {
            strings.forEach {
                put(it, computeIfPresent(it) { _, old ->
                    old + index
                } ?: listOf(index))
            }
        }
    }
    println()
}

fun main(args: Array<String>) {
    dataBuild(File(args[args.indexOf("--data") + 1]))
    do {
        println("=== Menu ===")
        menu.forEach { (order, designation) ->
            println("$order. $designation")
        }
    } while (when (val option = readln().toInt()) {
                in menu.keys -> {
                    menu[option]!!.action(lines)
                }

                else -> {
                    println("Incorrect option! Try again.")
                    true
                }
            })
}
