package robot.core

fun Int.has(flag: Int) = flag and this == flag
fun Int.with(flag: Int) = this or flag
fun Int.without(flag: Int) = this and flag.inv()