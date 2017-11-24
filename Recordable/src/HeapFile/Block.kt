package HeapFile

import AbstractData.Block
import AbstractData.Record
import AbstractData.Serializable
import AbstractData.toBytes
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import record.Validity
import record.Validity.*
import record.emptyMutableList
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class HeapFileBlock<T : Record<T>> : Block<T> {

    override var data           : MutableList<T>
    override var addressInFile: Int = -1

    override val byteSize   : Int get() = recordCount * ofType.byteSize
    override val blockSize  : Int get() = recordCount * byteSize
    override var recordCount: Int get() = data.size

    override val ofType: T

    constructor(data: MutableList<T>, addressInFile: Int, ofType: T) {
        this.data = data
        this.addressInFile = addressInFile
        this.recordCount = data.size
        this.ofType = ofType
    }

    override fun toByteArray() = toBytes {
        writeInt(recordCount)
        writeInt(addressInFile)
        data.forEach {
            write(it.toByteArray())
        }
    }

    override fun fromByteArray(byteArray: ByteArray): Block<T> {
        val dis = DataInputStream(ByteArrayInputStream(byteArray))
        val recordCount = dis.readInt()
        val addressInFile = dis.readInt()
        val readList = emptyMutableList<T>()

        for (i in 0 until recordCount) {
            val bytes = ByteArray(ofType.byteSize)
            for (j in 0 until ofType.byteSize)
                bytes[j] = dis.readByte()
            readList.add(ofType.fromByteArray(bytes))
        }

        return HeapFileBlock(readList, addressInFile, ofType)
    }

    

    override var validity: Validity = Valid

}