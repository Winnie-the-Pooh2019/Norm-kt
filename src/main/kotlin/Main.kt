import java.lang.Math.pow
import kotlin.math.*
import kotlin.random.Random

const val mx = 20
const val sx = 2
const val lambda = 0.1

data class Interval(
    val list: MutableList<Double>,
    var bounds: Pair<Double, Double>,
    var frequency: Int,
    var prob: Double = 0.0,
    var thFrequency: Double = 0.0
)

fun List<Interval>.echo() {
    this.forEach {
        println(
            "Границы: ${String.format("%.3f", it.bounds.first)}; " +
                    "${String.format("%.3f", it.bounds.second)} " +
                    "| Теор частота: ${String.format("%.3f", it.thFrequency)} | Частота: ${it.frequency} "
        )
    }
}

fun List<Interval>.minNeighbourIndex(index: Int): Int =
    if (index == 0) index + 1
    else if (index == this.size - 1) index - 1
    else if (this[index + 1].frequency > this[index - 1].frequency) index - 1
    else if (this[index - 1].frequency > this[index + 1].frequency) index + 1
    else -1

fun MutableList<Interval>.unite(left: Int, right: Int) {
    this[left].frequency += this[right].frequency
    this[left].bounds = uniteBounds(this[left].bounds, this[right].bounds)
    this[left].prob += this[right].prob
    this[left].thFrequency += this[right].thFrequency

    this.removeAt(right)
}

fun uniteBounds(left: Pair<Double, Double>, right: Pair<Double, Double>) =
    Pair(
        minOf(left.first, left.second, right.first, right.second),
        maxOf(left.first, left.second, right.first, right.second)
    )

fun main(args: Array<String>) {
    val xs = (1..100).map { ((1..12).fold(0.0) { acc, _ -> acc + Random.nextDouble(0.0, 1.0) } - 6) * sx + mx }.sorted()
        .apply { this.takeIf { "-d" in args }?.onEach { d -> println(d) } }

    val average = xs.sum() / xs.size
    val estimateSX = xs.fold(0.0) { acc, d -> acc + (d - average).pow(2) } / (xs.size)

    println("Оценка матожидания: $average")
    println("Оценка среднеквадратичного отклонения $estimateSX")
    println("Ошибка оценки матожидания ${abs(average - mx)}")
    println("Ошибка оценки среднеквадратичного отклонения ${abs(estimateSX - sx)}")

    val h = (xs.max() - xs.min()) / (1 + 3.3221 * log10(100.0))
    println("Длинна интервала $h")

    val intervals = ArrayList<Interval>()
    var boundLeft = xs[0]
    while (boundLeft <= xs[xs.size - 1]) {
        xs.filter { it >= boundLeft && it <= boundLeft + h }.apply {
            intervals.add(
                Interval(
                    this.toMutableList(),
                    Pair(boundLeft, boundLeft + h),
                    this.size
                )
            )
        }

        boundLeft += h;
    }

    intervals.forEach {
        val median = if (it.frequency == 0)
            return@forEach
        else if (it.frequency % 2 == 1)
            it.list[it.frequency / 2]
        else
            (it.list[it.frequency / 2] + it.list[it.frequency / 2 - 1]) / 2

        val u2 = ((median - average) / estimateSX).pow(2.0)
        val f = (1 / sqrt(2 * PI)) * E.pow(-u2 / 2)
        it.thFrequency = xs.size * h * f / estimateSX
    }

    println("\nИнтервалы до объединения:")
    intervals.echo()

    var i = intervals.size - 1
    while (i in 0 until intervals.size) {
        if (intervals[i].frequency < 5) {
            val targetNeighbour = intervals.minNeighbourIndex(i)
            intervals.unite(i, targetNeighbour)
        }
        i--
    }

    println("\nИнтервалы после объединения:")
    intervals.echo()

    val hi = intervals.sumOf { (it.frequency - it.thFrequency) * (it.frequency - it.thFrequency) / it.thFrequency }
    println("Х2 наблюдаемый = ${sqrt(hi)}")

//    a = 0.05
    val hiList = listOf(3.8, 6.0, 7.8, 9.5, 11.1)
    val hi2 = hiList[intervals.size - 3]
    println("Х2 критческий = $hi2")

    println("Гепотиза ${if (hi2 > hi) "отвергается" else "принимается"}")


}