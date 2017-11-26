package test

import LinearHashing.LinearHashFileBlock
import LinearHashing.LinearHashingFile
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.specs.StringSpec
import record.emptyMutableList
import java.io.File
import java.lang.Math.pow

class HashModuloTest : StringSpec({

    val blockCount = 2
    var currentLevel = 0
    var actualSplitAddress = 0
    fun firstHashModulo() = (blockCount * pow(2.toDouble(), currentLevel.toDouble())).toInt()
    fun secondHashModulo() = (blockCount * pow(2.toDouble(), currentLevel + 1.toDouble())).toInt()
    fun Int.firstHash() = this % firstHashModulo()   //* numberOfRecordsInBlock + firstBlockAddress
    fun Int.secondHash() = this % secondHashModulo() //* numberOfRecordsInBlock + firstBlockAddress
    fun Int.address(): Int {
        val first = firstHash()
        if (first < actualSplitAddress)
            return secondHash()
        else return firstHash()
    }


    "27"{
        27.address() shouldBe 1
    }
    "big number"{
        1236923466.address() shouldBe 0
    }
    "18"{
        18.address() shouldBe 0
    }
    "29"{
        29.address() shouldBe 1
    }
    "28"{
        28.address() shouldBe 0
    }

    "18"{
        actualSplitAddress = 1
        18.address() shouldBe 2
    }

    "39"{
        39.address() shouldBe 1
    }

    "13"{
        13.address() shouldBe 1
    }

    "16"{
        16.address() shouldBe 0
    }

    "51"{
        currentLevel=1
        actualSplitAddress = 0
        51.address() shouldBe 3
    }
    "19"{
        currentLevel=1
        actualSplitAddress = 0
        19.address() shouldBe 3
    }

})