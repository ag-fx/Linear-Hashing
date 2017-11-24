package test

import LinearHashing.LinearHashFileBlock
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.matchers.*

class BlockAndRecordSerialization : StringSpec({

    val joe = Person("joe", "joejoe")
    val john = Person("john", "doe")
    val doe = Person("joe", "doe")
    val mari = Person("mari", "parson")
    val persons = listOf(joe, john, doe, mari)

    "All records are the same size"{
        persons.all { it.byteSize == Person("test person", "with test name").byteSize } shouldBe true
    }
    "Serialized byte array size is byte size"{
        joe.toByteArray().size shouldBe joe.byteSize
    }

    "Read serialized object is the same as written object"{
        val bytes = joe.toByteArray()
        joe shouldEqual joe.fromByteArray(bytes)
    }

    val blockSize = 3
    val block = LinearHashFileBlock(blockSize, Person("", ""), listOf(joe, john, doe))

    "Block size is the same as byte array it produecs "{
        block.toByteArray().size shouldEqual block.byteSize
    }

    "Serialized block should be the same one as the written one"{
        val bytes = block.toByteArray()
        block.data shouldEqual block.fromByteArray(bytes).data
    }

    "Blocks with different data sizes should be the same size when serialized"{
        val block1 = LinearHashFileBlock(blockSize, Person("", ""), listOf(joe))
        val block2 = LinearHashFileBlock(blockSize, Person("", ""), listOf(joe, john, doe))
        val block3 = LinearHashFileBlock(blockSize, Person("", ""), listOf(joe, john))

        listOf(block1, block2, block3).all { it.toByteArray().size == block2.toByteArray().size } shouldBe true
    }



})