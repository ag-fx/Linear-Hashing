@file:Suppress("NOTHING_TO_INLINE")

import LinearHashing.LinearHashingFile
import model.Patient
import model.PatientId
import model.instanceOfPatientRecord
import model.toRecord
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
var seed = 1L
val rnd  get()= Random(seed++)
fun <T> emptyMutableList() = LinkedList<T>()

//Date to local date
inline fun Date.toLocalDate() =  java.sql.Date(time).toLocalDate()!!
//LocalDate -> Date:
inline fun LocalDate.toDate() = java.sql.Date.valueOf(this)!!// Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())

inline  fun <T>  T.rndNull() = if(Random().nextBoolean()) null else this

//random date
val ms get() = -946771200000L + (Math.abs(rnd.nextLong()) % (70L * 365 * 24 * 60 * 60 * 1000));

inline  fun Random.nextDate() = Date(ms)
inline  fun Random.nextDateNullable() = if(nextBoolean()) nextDate() else null

inline  fun Random.nextLocalDate() = nextDate().toLocalDate()
inline  fun Random.nextLocalDateNullable() = if(nextBoolean()) nextDate().toLocalDate() else null

inline  fun Random.nextFirstname(): String {
    val names = listOf("Ferko","Janko","Jozef","Peter","Pavol","Andrej","Roman","Matus","Lukas","Majka","Lenka","Maria","Tomas")
    return names[nextInt(names.size)]
}

inline fun Random.nextSurname(): String {
    val names = listOf("Smith", "Jones", "Taylor", "William", "Brown", "Davies", "Evans", "Wilson", "Thomas", "Roberts", "Johnson",
        "Lewis", "Walker", "Robinson", "Wood", "Thompson", "White", "Watson")
    return names[nextInt(names.size)]
}

inline fun Random.nextDiagnosis(): String {
    val names = listOf("Anemia","Botulizmus","Celiakia","Ebola","Hepatitida","Cholera","Kvapavka","Leukemia","Nador","Osypky"
    ,"Rakovina","Tuberkuloza","Zimnica")
    return names[nextInt(names.size)]
}

fun main(args: Array<String>) {
    println("Start")
    val ds = LinearHashingFile(
        pathToFile = "test_patients",
        instanceOfType = instanceOfPatientRecord,
        numberOfRecordsInAdditionalBlock = 2,
        maxDensity = 0.75,
        minDensity = 0.55,
        numberOfRecordsInBlock = 3,
        blockCount = 2,
        deleteFiles = true
    )
    println("generujem pacientaov")
    val patients = (1..1000).map { Patient(PatientId(it)).toRecord() }
    println("vytvaram")
    var i = 0
    patients.forEach {
        val success = ds.add(it)
        if (!success) {
            println("boha. $it")
        }
        println(i++)

    }
    println("done")

}