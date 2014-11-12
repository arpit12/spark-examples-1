package sparkExamples.invertedIndices.execution

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import sparkExamples.invertedIndices.models.models.{InvertedIndex, OccrCount, Line, Word}
import sparkExamples.wrappers.RDDImplicits.RichRDD

//http://lintool.github.io/Cloud9/docs/exercises/indexing.html
object Indices {
  def main(arg: Array[String]): Unit = {
    //val input  = sc.textFile("hdfs:///tmp/bible-shakes.nopunc.gz")
    val sc = new SparkContext(
      new SparkConf()
        .setMaster("local")
        .setAppName("bigram analysis")
    )

    val input  = sc.textFile("/Users/Rahul/Downloads/bible+shakes.nopunc.gz")

    val lineWithLineNumbers = input.zipWithIndex().map(a => Line(a._2, a._1))
    //val wordLine = lineWithLineNumbers.map(a => (a.split(" "), a)).flatMap(a1 => a1._1.map(b1 => (b1, a1._2)))

    val wordToLine = lineWithLineNumbers.explode(line => line.words)


//    println(wordToLine.collect())

    val wordLines = wordToLine.map(a => (Word(a._1), List(a._2))).reduceByKey(_ ++ _)

    val wordCountLine = wordLines.map(a => (a._1, OccrCount[Word](a._2.size), a._2))

    val wordCountLinePerLineCount = wordCountLine.map(a => (a._1, a._2, a._3.map(line => (line.lineNumber, line.occuranceCount(a._1)))))

    val invertedIndices = wordCountLinePerLineCount.map(a => InvertedIndex(a._1, a._2, a._3))

    invertedIndices.cache()

    invertedIndices.filter(invertedIndex => invertedIndex.word.equals(Word("starcross'd"))).first()
//    res4: Array[sparkExamples.invertedIndices.models.models.InvertedIndex] = Array(InvertedIndex(Word(starcross'd),OccrCount(1),List((57052,1))))

    val goldOccr = invertedIndices.filter(invertedIndex => invertedIndex.word.equals(Word("gold"))).first()
    goldOccr.occurances.groupBy(a => a._2).map(a => (a._1, a._2.size))
//    Map(2 -> 116, 1 -> 523, 3 -> 9)

    val silverOccr = invertedIndices.filter(invertedIndex => invertedIndex.word.equals(Word("silver"))).first()
    silverOccr.occurances.groupBy(a => a._2).map(a => (a._1, a._2.size))
//    Map(2 -> 78, 1 -> 314, 3 -> 3)

  }
}
