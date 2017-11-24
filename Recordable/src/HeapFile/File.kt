package HeapFile

import AbstractData.Block
import AbstractData.Record
import record.emptyMutableList
import src.ReadWrite

class HeapFile<T : Record<T>, B : Block<T>> {

    private val blockRecordCount: Int
    private val blockSize: Int get () = ofBlockType.byteSize
    private val file: ReadWrite
    private val prop: ReadWrite
    private var currentBlock: B? = null
    private val ofType: T
    private val ofBlockType: B

    val emptyBlockAddresses       = emptyMutableList<Int>()
    val almostEmptyBlockAddresses = emptyMutableList<Int>()
    val lastAdress = -1

    constructor(path: String, propertiesPath: String, ofTypeBlock: B, ofType: T, recordsInBlockSize: Int) {
        this.file = ReadWrite(path)
        this.prop = ReadWrite(propertiesPath)
        this.ofType = ofType
        this.ofBlockType = ofTypeBlock
        this.blockRecordCount = recordsInBlockSize
    }

}