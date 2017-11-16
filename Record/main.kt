import SizeConst.*
import Validity.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

fun main(args: Array<String>) {
    val a = Person("Andrej", 40, Date())
    val b = Person("Peter" , 29, Date())
    val c = Person("Pavol" , 30, Date())
    val block      = Block(listOf(a,b,c))
    val bytesBlock = block.toBytes()
    val newBlock   = block.fromBytes(bytesBlock)
    println(block.toString())
    println(newBlock.toString())
}

class Person(name: String, val age: Int, val date: Date) : Record<Person> {

    val name: String

    init {
        if (name.length < maxStringLength)
            this.name = name
        else
            throw IllegalArgumentException("Name must be shorter than $maxStringLength")
    }

    override fun toBytes(): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val outStream    = DataOutputStream(outputStream)
        with(outStream) {
            writeString   (name)
            writeInt      (age)
            writeDate     (date)
            writeValidity (validity)
        }
        return outputStream.toByteArray()
    }

    override fun fromBytes(bytes: ByteArray): Person {
        val dis  = DataInputStream(ByteArrayInputStream(bytes))
        val name = dis.readString()
        val age  = dis.readInt()
        val date = dis.readDate()
        return Person(name, age, date).apply { validity = dis.readValidity() }
    }

    override val size: Int
        get() = super.size + SizeOfString + SizeOfInt + SizeOfLong

    override var validity = Valid

    override fun toString() = "$validity $name $age ${date.time}"

}
