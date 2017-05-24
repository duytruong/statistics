package duy.statistics

import com.twitter.bijection.avro.GenericAvroCodecs
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils
import kafka.serializer.StringDecoder
import kafka.serializer.DefaultDecoder
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import redis.clients.jedis.Jedis

/**
  * Created by duyrau on 5/23/17.
  */
object PageView {

  def main(args: Array[String]): Unit = {
    val PAGEVIEW_SCHEMA = "{\"fields\":[{\"name\":\"metric\",\"type\":\"string\"},{\"name\":\"uuid\",\"type\":\"string\"},{\"name\":\"location\",\"type\":\"string\"},{\"name\":\"referrer\",\"type\":\"string\"},{\"name\":\"url\",\"type\":\"string\"},{\"name\":\"product\",\"type\":\"string\"},{\"name\":\"video\",\"type\":\"string\"},{\"name\":\"viewer\",\"type\":\"int\"},{\"name\":\"ts\",\"type\":\"long\"}],\"name\":\"pageviewevent\",\"type\":\"record\"}";
    val duration = args(0).toInt
    val conf = new SparkConf().setAppName("pageview-consumer")
    val ssc = new StreamingContext(conf, Seconds(duration))
    val kafkaParams = Map[String, String]("metadata.broker.list" -> "localhost:9092")
    val topicSet = Set("pageview")
    lazy val parser = new Schema.Parser()
    lazy val schema = parser.parse(PAGEVIEW_SCHEMA)
    lazy val recordInjection = GenericAvroCodecs.toBinary(schema)

    val directKafkaStream = KafkaUtils.createDirectStream[String, Array[Byte], StringDecoder, DefaultDecoder](ssc, kafkaParams, topicSet)

    directKafkaStream
      .foreachRDD(rdd => rdd.foreachPartition(iter => {
        val jedis = new Jedis("localhost")
        while (iter.hasNext) {
          val item = iter.next()
          val record = recordInjection.invert(item._2)
          val actualData = record.get.asInstanceOf[GenericRecord]
          val uuid = actualData.get("uuid").toString
          jedis.pfadd("pview", uuid)
          println(record.get)
          println(uuid)
          println(record.get.getClass)
        }
        jedis.close()
      }))

//    directKafkaStream.foreachRDD(rdd => rdd.foreach(msg => {
//      println(msg.toString())
//    }))

//    directKafkaStream.foreachRDD(rdd => rdd.foreachPartition(iter => {
//      while (iter.hasNext) {
//        val item = iter.next()
//        print("key = " + item._1 + ", value = " + item._2)
//      }
//    }))

    ssc.start()
    ssc.awaitTermination()
  }
}
