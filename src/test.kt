//@file:Suppress("MemberVisibilityCanPrivate")
//
//package src
//
//import record.emptyMutableList
//import java.util.*
//
//val fajlNajm = "bajty.bin"
//val blockSajz = 4
//
//fun main(args: Array<String>) {
//
////    val block = Block(Person(),4).apply {
////        data.addAll(listOf(a,b,c,d))
//////    }
////    val tree = PseudoTree(fajlNajm, tajp, blockSajz)
////    println(block.size)
////    tree.insert(block)
////    tree.insert(block)
////    println(tree.get())
////    val som = tree.get()
////    println(som)
//}
//
////
//class PseudoTree<T : Record>(
//    val pathToFile: String,
//    val type: T,
//    val blockSize: Int
//) {
//
//    val file = ReadWrite(pathToFile)
//    val insertedBlocks = emptyMutableList<Record>()
//    var index = 0
//
//    fun insert(block: Record) {
//        insertedBlocks.add(block)
//        val b = block.size
//
//        println(b)
//        file.write(block.toBytes(), block.size, index)
//        index += blockSize
//    }
//
////    fun get(): Record {
////     //   val block = Block(record,blockSize)
////        val b = block.size
////        println(b)
////        return block.fromBytes(file.read(record.size,0))
////    }
//
//}