package src

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

class ReadWrite(val path: String) {
    val file = RandomAccessFile(path, "rw")

    fun write(byteArray: ByteArray, size: Int, position: Int) {
        val empty = ByteArray(size)
        try {
            with(file) {
                seek(position)
                write(empty)
                seek(position)
                write(byteArray)
            }
        } catch (e: FileNotFoundException) {
            println("RandomAccessFile FileNotFoundException $e")
        } catch (e: IOException) {
            println("RandomAccessFile IOException at position $position $e")
        } catch (e: Exception) {
            println("RandomAccessFile Exception $e")
        }

    }

    fun read(size: Int, index: Int): ByteArray {
        val vysledok = StringBuffer()
        val bytes = ByteArray(size)
        try {
            file.seek(index)
            file.read(bytes, 0, size)

        } catch (e: FileNotFoundException) {
            println("No File citanie")

        } catch (e: IOException) {
            println("IO citanie")

        } catch (e: Exception) {
            println("Ex citanie " + e)
        }

        return bytes
    }

}
fun RandomAccessFile.seek(position: Int) = seek(position.toLong())
