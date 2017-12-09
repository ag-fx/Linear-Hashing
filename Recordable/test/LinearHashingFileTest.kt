package test

import AbstractData.Record
import AbstractData.SizeConst.*
import AbstractData.isInvalid
import AbstractData.isValid
import AbstractData.toBytes
import LinearHashing.LinearHashFileBlock
import LinearHashing.LinearHashingFile
import com.google.common.annotations.VisibleForTesting
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec
import junit.framework.TestCase
import record.Validity
import record.Validity.Invalid
import record.Validity.Valid
import record.emptyMutableList
import record.readValidity
import record.writeValidity
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.File
import java.io.Serializable
import java.util.*

class LinearHashingFileTest : StringSpec({

    val joe = Person("joe", "joejoe", Date(56))
    val john = Person("john", "doe")
    val doe = Person("joe", "doe")
    val mari = Person("mari", "parson")

    val persons = listOf(joe, john, doe, mari)

    val ofType = Person("OFTYPE", "", Date(1))
    val pathToFile = "test_bajty.bin"
    val blockSize = 3

    val block = LinearHashFileBlock(blockSize, Person("", "", Date(5)), listOf(joe, john, doe))

    val ds = LinearHashingFile(pathToFile = pathToFile, instanceOfType = ofType, numberOfRecordsInBlock = blockSize, blockCount = 4,numberOfRecordsInAdditionalBlock = 2)

    "add one record"{
        println(ds.allRecordsInFile())
        val personToAdd = mari
        ds.add(personToAdd)
        println(ds.allRecordsInFile())

        ds.get(personToAdd) shouldEqual personToAdd

    }
})

class MyInt(val value: Int) : Record<MyInt> {

    override fun toByteArray() = toBytes {
        writeValidity(validity)
        writeInt(value)
    }

    override var validity: Validity = Valid
    override val stringSize: Int
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun fromByteArray(byteArray: ByteArray): MyInt {
        val dis = DataInputStream(ByteArrayInputStream(byteArray))
        val valid = dis.readValidity()
        val value = dis.readInt()
        return MyInt(value).apply { validity = valid }
    }

    override val byteSize: Int
        get() = SizeOfInt.value * 2

    override fun toString(): String = if (isValid()) value.toString() else "$value null"
    override val hash: Int
        get() = value

    override fun equals(other: Any?): Boolean {
        if(other !is MyInt) return false
        if(other.isInvalid() && this.isInvalid()) return true
        if(other.value == this.value && this.isValid() && other.isValid()) return true
        return false
    }

}
enum class Operation(value:Int){Insert(1),Find(2)}
class LinearHashingPrednaska : StringSpec({
    val pathToFile = "test_prednaska"
    val numberOfRecordsInAdditionalBlock = 3
    val ds = LinearHashingFile(pathToFile = pathToFile, blockCount = 2, numberOfRecordsInBlock = 2, instanceOfType = MyInt(5), maxDensity = 0.8, numberOfRecordsInAdditionalBlock = numberOfRecordsInAdditionalBlock)
    val invalid = MyInt(5).apply { validity = Invalid }
    val scope = "scope"

    "delete"{
        ds.deleteFiles() shouldBe true
    }
    /*
    "18,27,29"{
        ds.add(18)
        ds.add(27)
        ds.add(29)
        ds.allBlocksInFile() shouldBe listOf( listOf(MyInt(18),invalid), listOf(MyInt(27), MyInt(29)))
    }

    "29"{
        ds.add(18)
        ds.add(27)
        ds.add(29)
        ds.add(28)
        val b1 = listOf(MyInt(28),invalid)
        val b2 = listOf(MyInt(27),MyInt(29))
        val b3 = listOf(MyInt(18),invalid)
        ds.allBlocksInFile() shouldBe listOf(b1,b2,b3)
    }
    "additional block is ok"{
        ds.add(18)
        ds.add(27)
        ds.add(29)
        ds.add(28)
        val t = MyInt(39)
        ds.add(t)
        val b1 = listOf(MyInt(28), invalid)
        val b2 = listOf(MyInt(27), MyInt(29))
        val b3 = listOf(MyInt(18), invalid)
        println(ds.allBlocksInFile())
        println(ds.additionalFile.allBlocksInFile() )
        ds.additionalFile.allBlocksInFile() shouldBe listOf(listOf(MyInt(39)))
    }

    "linear hash file is ok"{
        val t = MyInt(39)

        with(ds) {
            add(18)
            add(27)
            add(29)
            add(28)
        }
        ds.add(t)

        val b1 = listOf(MyInt(28), invalid)
        val b2 = listOf(MyInt(27), MyInt(29))
        val b3 = listOf(MyInt(18), invalid)
        println(ds.allBlocksInFile())
        println(ds.additionalFile.allBlocksInFile() )

        ds.allBlocksInFile() shouldBe listOf(b1, b2, b3)

    }

*/


    "two additional blocks"{
        val a = MyInt(27)
        val b = MyInt(18)
        val c = MyInt(29)
        val d = MyInt(28)
        val e = MyInt(39)
        val f = MyInt(13)
        val g = MyInt(16)
        val h = MyInt(51)
        val i = MyInt(19)

        with(ds) {
            add(a)
            add(b)
            add(c)
            add(d)
            add(e)
            add(f)
            add(g)
            add(h)
            add(i)
        }

        val found = emptyMutableList<MyInt?>()
        listOf(a,b,c,d,e,f,g,h,i).forEach {
            found.add(ds.get(it))
        }
        found.contains(null) shouldBe false
        println(ds.allBlocksInFile())
        println(ds.additionalFile.allBlocksInFile())
      //  ds.additionalFile.allBlocksInFile() shouldBe listOf(listOf(e,f))
    }.config(enabled = false )//) numberOfRecordsInAdditionalBlock >= 2)


    "second item to be added in additional block"{
        ds.deleteFiles()
        val a = MyInt(27)
        val b = MyInt(18)
        val c = MyInt(29)
        val d = MyInt(28)
        val e = MyInt(39)
        val f = MyInt(13)
        val g = MyInt(16)
        val h = MyInt(51)
        val i = MyInt(19)
        listOf(a,b,c,d,e,f,g,h,i)
        with(ds) {
            add(a)
            add(b)
            add(c)
            add(d)
            add(e)
            add(f)
            add(g)
            add(h)
            add(i)
        }

        val b0 = listOf(MyInt(16), invalid)
        val b1 = listOf(MyInt(29), MyInt(13))
        val b2 = listOf(MyInt(18), invalid)
        val b3 = listOf(MyInt(27), MyInt(39))
        val b4 = listOf(MyInt(28), invalid)

        val blocks = listOf(b0, b1, b2, b3, b4)

        val a0 = listOf(MyInt(51))
        val a1 = listOf(MyInt(19))

        val addit = listOf(a0, a1)

        ds.get(b)
        ds.get(c)
        ds.get(d)
        ds.get(e)
        ds.get(f)
        ds.get(g)
        ds.get(h)
        ds.get(i)
        println(ds.allBlocksInFile())
        println(ds.additionalFile.allBlocksInFile())
        println("""
            ${ds.actualRecordsCount} ...mal by byt  7
            ${ds.actualBlockCount}   ...mal by byt  5
            --------
            ${ds.additionalRecordsCount} ... mal by byt 2
            ${ds.additionalBlockCount}   ... mal by byt 1
           """.trimIndent())
    }.config(enabled = false)

    "insert and find all"{
        val r = Random( )
        val numberOfRecords = 10_000
        val toAdd = (1..numberOfRecords).map { MyInt(Math.abs(r.nextInt())) }.distinctBy { it.value }
        var foundAll = true
        toAdd.forEachIndexed { index, number ->
            ds.add(number)
        }


        val found = emptyMutableList<MyInt?>()
        var foundNull = false

        toAdd.forEach {
            val result = ds.get(it)
            found.add(result)
            if (result == null)
                foundNull = true
        }
        println(toAdd.sortedBy { it.value })
        println((ds.allRecordsInFile() + ds.additionalFile.allRecordsInFile()).filter{it.isValid()}.sortedBy { it.value })

        foundNull shouldBe false
    }.config(enabled = false)


    "insert, delete and find all"{
        val r = Random(5000)
        val numberOfRecords = 5_000 * 2
        val toAdd = (1..numberOfRecords).map { MyInt(Math.abs(r.nextInt())) }//.distinctBy { it.value }
        toAdd.forEach{
            ds.add(it)
        }


        toAdd.forEach {
            if(ds.get(it) != it)
                println("adding was not successful")
        }
         val toDelete = toAdd.subList(numberOfRecords/8,numberOfRecords/4)
        toDelete.forEach {
            ds.delete(it)
        }

        println(ds.additionalFile.allBlocksInFile())

        var foundAll = true
        (toAdd - toDelete).forEach {
            if(ds.get(it) != it)
                foundAll = false
        }
        foundAll shouldBe true
    }.config(enabled = true)


})


inline fun LinearHashingFile<MyInt>.add(int: Int) = add(MyInt(int))

data class Test(@VisibleForTesting private val safas:Int)
