import java.util.Properties
import java.util.Arrays
import org.apache.spark.sql.{Row, SQLContext, SaveMode, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.SparkContext
import org.apache.spark.sql.types._
import org.apache.spark.streaming._
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark
import org.json4s.jackson.Json

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("MyAppAlarm")
      .master("local[*]")
      .getOrCreate()
    import spark.implicits._
    //val conf = SparkConf().setAppName("projectName").setMaster("local[*]")
    //val sc = SparkContext.getOrCreate(conf)
    println("Enternig begin of program")

    // zone geographic, datalake timestamp
    val propsConsumer: Properties = new Properties()
    propsConsumer.put("bootstrap.servers", "localhost:9093")
    propsConsumer.put("key.deserializer", classOf[StringDeserializer].getName)
    propsConsumer.put("value.deserializer", classOf[StringDeserializer].getName)
    propsConsumer.put("group.id", "spark-consumer-group")
    propsConsumer.put("auto.offset.reset", "latest")
    propsConsumer.put("enable.auto.commit", false)
    println("Properties created.")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](propsConsumer)
    println("Consumer created.")

    consumer.subscribe(Arrays.asList("drone_topic")) // Replace "topic_name" with your Kafka topic(s)
    println("Consumer subscribed.")

    while (true) {
      val records = consumer.poll(100);

      // Get information concerning the topic
      records.partitions().forEach { topicPartition =>
        val topic = topicPartition.topic()
        val partition = topicPartition.partition()
        // Process topic and partition as needed
        println(s"Topic: $topic, Partition: $partition")
      }

      // Get value from topic and print the data
      // println(records)
      records.forEach { record =>
        println(record.value())
        val key = record.key()
        val value = record.value()
        println(s"Received message: key = $key, value = $value")

        val data = Seq(Row(value))
        val schema = new StructType()
          .add("id", StringType)
          .add("citizens", ArrayType(StringType))
          .add("words", ArrayType(StringType))
          .add("timestamp", StringType)
          .add("current location", ArrayType(IntegerType))
          .add("harmonyScore", IntegerType)

        val propsProducer = new Properties()
        propsConsumer.put("bootstrap.servers", "localhost:9093")
        propsConsumer.put("key.serializer", classOf[StringSerializer].getName)
        propsConsumer.put("value.serializer", classOf[StringSerializer].getName)
        val rdd = spark.sparkContext.parallelize(data)
        val df = spark.createDataFrame(rdd, schema)
        val producer = new KafkaProducer[String, String](propsProducer)

        val json = Json.(record.value())
        val citizens = (json \ "citizens").as(StringType)
        // Apply your condition on the data
        if (df.schema("harmonyScore")) {
          val outputRecord = new ProducerRecord[String, String]("alarm_topic", record.key(), record.value())
          producer.send(outputRecord)
        }
        producer.close()
      }
    }
  }
}
