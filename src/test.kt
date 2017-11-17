package src

import record.Block
import record.Person
import record.Record
import java.util.*
import kotlin.reflect.KClass

fun main(args: Array<String>) {
    val a = Person("Marha", 40, Date())
    val b = Person("Spina", 29, Date())
    val c = Person("Svina", 30, Date())
    println(a.size*3)
    val block = Block(Person(),3).apply {
        data.addAll(listOf(a,b,c))
    }

    val tree = PseudoTree(type = Person())
    println(tree.get())
   // val som = tree.get()
 //    println(som)
}



class PseudoTree<T:Record>(
    val pathToFile: String = "bajty.bin",
    val type : T
) {
    val file = ReadWrite(pathToFile)

    fun insert(block: Record) {
        file.write(block.toBytes(),block.size,0)
    }

    fun get(): Record {
        val block = Block(type,3)
        return block.fromBytes(file.read(block.size,0))
    }

}