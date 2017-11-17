package Tree

interface Tree<T>{

    fun insert(item :T) : Boolean

    fun delete(item :T) : Boolean

    operator fun get(item :T) : T?

}