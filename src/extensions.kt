import java.time.LocalDate
import java.time.ZoneId
import java.util.*

fun <T> emptyMutableList() = LinkedList<T>()

//Date to local date
inline fun Date.toLocalDate() = toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
//LocalDate -> Date:
inline fun LocalDate.toDate() = Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())