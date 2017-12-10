package AbstractData

enum class SizeConst(val value: Int) {
    SizeOfDouble    (64 / 8),
    SizeOfFloat     (32 / 8),
    SizeOfLong      (64 / 8),
    SizeOfInt       (32 / 8),
    SizeOfShort     (16 / 8),
    SizeOfByte      (8  / 8),
    SizeOfChar      (16 / 8),
    SizeOfValidity  (SizeOfInt .value),
    SizeOfDate      (SizeOfLong.value)
}

operator fun SizeConst.plus(other: SizeConst)   = other.value + this.value
operator fun Int      .plus(other: SizeConst)   = other.value + this

operator fun SizeConst.times(other: Int)        = other *  this.value
operator fun Int      .times(sizeOf: SizeConst) = this  * sizeOf.value