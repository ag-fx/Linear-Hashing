package Tree.BPlusTree

import java.util.*

interface BNode{

}

class Node<T : Comparable<T>>{

    val items    = Items<T>()
    val children = Children<T>()

    //val tree = BTree()
}


class Items<T : Comparable<T>>{

    val list  = emptyMutableList<T>()

    fun insertAt(index:Int,item:T) = list.add(index,item)

    fun pop() = list.pop()

    fun find ( item :T){
        val i = list.binarySearch(item)
        if(i >=0 ){
            Pair(i,true)
        }else
            Pair(i.inv(),false)
    }


}



class Children<T : Comparable<T>>{

    val list  = emptyMutableList<Node<T>>()

    fun insertAt(index:Int,item: Node<T>) = list.add(index,item)

    fun pop() = list.pop()

}
fun <T> emptyMutableList() = LinkedList<T>()
