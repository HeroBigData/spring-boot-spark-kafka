package com.wisrc.etl.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConsumerService extends CommandLineRunner{


  @Value("${etl.data.stream.group}")
  val GROUP_ID:String = "etl-demo"

  @Value("${etl.data.stream.topic}")
  val TOPICS:String = "demo"

  @Value("${etl.executor.log.level}")
  val SPARK_EXECUTOR_LOG_LEVEL:String = "WARN"

  @Value("${etl.executor.name}")
  val EXECUTOR_NAME:String = "Default-App"

  @Value("${spring.kafka.bootstrap-servers}")
  val KAFKA_BOOTSTRAP_BROKERS:String = null

  @Value("${spring.kafka.consumer.enable-auto-commit}")
  val KAFKA_AUTO_COMMIT:String = "true"

  override def run(args: String*): Unit = {
    println(args)
    consumer()
  }

  def consumer(args: String*): Unit = {

    val sparkConf = new SparkConf()
      .setAppName(EXECUTOR_NAME)
      .set("spark.serializer","org.apache.spark.serializer.KryoSerializer")
      // TODO setMaster 用于本地开发测试，发布到集群中时，去掉setMaster
      .setMaster("local[2]")

    val topicsSet = TOPICS.split(",").toSet

    println("Topic is:" + topicsSet)

    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.sparkContext.setLogLevel(SPARK_EXECUTOR_LOG_LEVEL)

    // Create direct kafka stream with brokers and topics
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> KAFKA_BOOTSTRAP_BROKERS,
      ConsumerConfig.GROUP_ID_CONFIG -> GROUP_ID,
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> KAFKA_AUTO_COMMIT,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer])

    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams))

    messages.foreachRDD(rdd => {
      try {
        rdd.collect().foreach(record => {
          println("接收到数据是：", record)
        })
      } catch {
        case e:Exception => {
          println(e.getMessage)
        }
      }
    })

    println("start the computation.")
    ssc.start()
    ssc.awaitTermination()

  }

}
