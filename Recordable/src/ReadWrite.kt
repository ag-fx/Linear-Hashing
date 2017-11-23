package src

import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile

class ReadWrite(val path: String) {
    private val file = RandomAccessFile(path, "rw")

    fun size() = file.length()

    fun write(byteArray: ByteArray, size: Int, position: Int): Boolean {
        val empty = ByteArray(size)
        try {
            with(file) {
                seek(position)
                write(empty)
                seek(position)
                write(byteArray)
                return true
            }
        } catch (e: FileNotFoundException) {
            println("RandomAccessFile FileNotFoundException $e")
        } catch (e: IOException) {
            println("RandomAccessFile IOException at position $position $e")
        } catch (e: Exception) {
            println("RandomAccessFile Exception $e")
        }
        return false
    }

    fun read(size: Int, index: Int): ByteArray {
        val vysledok = StringBuffer()
        val bytes = ByteArray(size)
        try {
            file.seek(index)
            file.read(bytes)

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
