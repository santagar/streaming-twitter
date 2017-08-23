package com.santagar.streaming

// Needed for all Spark jobs.
import org.apache.spark.SparkConf

// Only needed for Spark Streaming.
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext

// Only needed for utilities for streaming from Twitter.
import org.apache.spark.streaming.twitter._

object Topics {
  def main(args: Array[String]): Unit = {

    // Set up the Spark configuration with our app name and any other config
    // parameters you want (e.g., Kryo serialization or executor memory).
    val sparkConf = new SparkConf().setAppName("streaming-twitter-topics")

    // Use the config to create a streaming context that creates a new RDD
    // with a batch interval of every 5 seconds.
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    // Use the streaming context and the TwitterUtils to create the
    // Twitter stream.
    val stream = TwitterUtils.createStream(ssc, None)

    // Each tweet comes as a twitter4j.Status object, which we can use to
    // extract hash tags. We use flatMap() since each status could have
    // ZERO OR MORE hashtags.
    val hashTags = stream.flatMap(status => status.getHashtagEntities)

    // Convert hashtag to (hashtag, 1) pair for future reduction.
    val hashTagPairs = hashTags.map(hashtag => ("#" + hashtag.getText, 1))

    // Use reduceByKeyAndWindow to reduce our hashtag pairs by summing their
    // counts over the last 10 seconds of batch intervals (in this case, 2 RDDs).
    val topCounts10 = hashTagPairs.reduceByKeyAndWindow((l, r) => {l + r}, Seconds(10))

    // topCounts10 will provide a new RDD for every window. Calling transform()
    // on each of these RDDs gives us a per-window transformation. We use
    // this transformation to sort each RDD by the hashtag counts. The FALSE
    // flag tells the sortBy() function to sort in descending order.
    val sortedTopCounts10 = topCounts10.transform(rdd =>
      rdd.sortBy(hashtagPair => hashtagPair._2, false))

    // Print popular hashtags.
    sortedTopCounts10.foreachRDD(rdd => {
      val topList = rdd.take(10)
      println("\nPopular topics in last 10 seconds (%s total):".format(rdd.count()))
      topList.foreach{case (tag, count) => println("%s (%d tweets)".format(tag, count))}
    })

    // Finally, start the streaming operation and continue until killed.
    ssc.start()
    ssc.awaitTermination()
  }
}
