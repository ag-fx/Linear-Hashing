package test
import LinearHashing.LinearHashingFile
import io.kotlintest.specs.StringSpec
import model.*
import java.util.*

class DOHIS : StringSpec({

    val ds = LinearHashingFile(
        pathToFile = "test____patients",
        instanceOfType = instanceOfPatientRecord,
        numberOfRecordsInBlock = 10,
        numberOfRecordsInAdditionalBlock = 4,
        blockCount = 2,
        minDensity = 0.4,
        maxDensity = 0.75,
        deleteFiles = true
    )

    val patients = (1..10000).map { Patient(PatientId(it)).toRecord() }

   "erwrwe"{

       val a = instanceOfHospitRecord
       val ab = a.toByteArray()
       val ba = a.byteSize
       val id = PatientId(10)
       val p = Patient(id)

       val r = p.toRecord()
       val rb = r.toByteArray()//6478 bjatov
       val rbbbb= r.byteSize
       val rp = r.fromByteArray(rb)
       println()
   }
    "insert test"{
        println("start")
        patients.forEach {
                val success = ds.add(it)
                if(!success){
                    println("boha. $it")
                }


        }
        println("end")

    }.config(enabled = false)

})