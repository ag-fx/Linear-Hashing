import AbstractData.Record
import AbstractData.Serializable
import AbstractData.isValid
import java.util.*

/**
 * Returns a randomized list.
 */
fun <T> Iterable<T>.shuffle(seed: Long? = null): List<T> {
    val list = this.toMutableList()
    val random = if (seed != null) Random(seed) else Random()
    Collections.shuffle(list, random)
    return list
}

inline fun <reified T:Serializable<T>> List<T>.filterInvalid() = this.filter { it.isValid() }