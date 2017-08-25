Streaming Twitter Service
=========================

A few lines of code to demo how streaming works with Apache Spark, in particular using the extensions provided by [Apache Bahir](https://bahir.apache.org/) to read a live stream of tweets.

To make it work on your installation, be sure to add a `twitter4j.properties` under `src/main/resources` that includes the following information:

    oauth.consumerKey=***
    oauth.consumerSecret=***
    oauth.accessToken=***
    oauth.accessTokenSecret=***

Visit [apps.twitter.com](https://apps.twitter.com) to get your own API keys.

## Usage ##

Compile scala and build fat jar:

```sh
sbt assembly
```

Run (sentiment score):

```sh
$SPARK_HOME/bin/spark-submit \
  --master $SPARK_MASTER \
  --class com.santagar.streaming.Sentiment \
  target/scala-2.11/streaming-twitter-assembly-1.0.0.jar
```
Run (trending topics):

```sh
$SPARK_HOME/bin/spark-submit \
  --master $SPARK_MASTER \
  --class com.santagar.streaming.Topics \
  target/scala-2.11/streaming-twitter-assembly-1.0.0.jar
```

## Spark UI ##

You can access a web UI by simply opening http://node:4040 in a web browser (e.g: [http://192.168.1.34:4040](http://192.168.1.34:4040))

