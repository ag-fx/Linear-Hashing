package test

import LinearHashing.LinearHashFileBlock
import LinearHashing.LinearHashingFile
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec
import record.emptyMutableList
import java.io.File

class LinearHashingFileTest : StringSpec({

    val joe = Person("joe", "joejoe")

    val john = Person("john", "doe")
    val doe = Person("joe", "doe")
    val mari = Person("mari", "parson")
    val persons = listOf(joe, john, doe, mari)
    val ofType = mari
    val pathToFile = "test_bajty.bin"
    val blockSize = 3
    val block = LinearHashFileBlock(blockSize, Person("", ""), listOf(joe, john, doe))
    val ds = LinearHashingFile(pathToFile = pathToFile, ofType = ofType, numberOfRecordsInBlock = blockSize)
    "delete file"{
        File(pathToFile).delete() shouldBe true
    }

    "add records to file"{
        val result = emptyMutableList<Boolean>()
        with(result) {
            add(ds.add(joe))
            add(ds.add(john))
            add(ds.add(doe))
            add(ds.add(mari))
        }
        result.all { it } shouldBe true
    }

    "Everything I've written is in file"{
        ds.allBlocksInFile().flatMap { it.data } shouldEqual persons
    }
/*
1 0

  00000001 = 1
  00000010 = 2
  00000011 = 3
  00000100 = 45
 */

})