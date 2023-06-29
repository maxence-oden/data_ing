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



object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("MyAppAlarm")
      .master("local[*]")
      .getOrCreate()
    
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

    loop(consumer, spark)
  }
  def loop(consumer: KafkaConsumer[String, String], spark: SparkSession): Unit =
  {
    import spark.implicits._
    val records = consumer.poll(100);

    // Get information concerning the topic
    records.partitions().forEach { topicPartition =>
      val topic = topicPartition.topic()
      val partition = topicPartition.partition()
      // Process topic and partition as needed
      println(s"Topic: $topic, Partition: $partition")
    }

    val schema = new StructType()
      .add("id", StringType)
      .add("citizens", ArrayType(StringType))
      .add("words", ArrayType(StringType))
      .add("timestamp", StringType)
      .add("current location", ArrayType(IntegerType))

    val propsProducer = new Properties()
    propsProducer.put("bootstrap.servers", "localhost:9093")
    propsProducer.put("key.serializer", classOf[StringSerializer].getName)
    propsProducer.put("value.serializer", classOf[StringSerializer].getName)
    val producer = new KafkaProducer[String, String](propsProducer)

    // Get value from topic and print the data
    // println(records)
    records.forEach { record =>
      println(record.value())
      val key = record.key()
      val value = record.value()
      println(s"Received message: key = $key, value = $value")

      val data = Seq(Row(value))
      val rdd = spark.sparkContext.parallelize(data)
      val df = spark.createDataFrame(rdd, schema)

      //val json = parse(record.value())
      val jsondf = spark.read.json(Seq(record.value()).toDS)
      val location = jsondf.select("current location").collect()(0)(0).asInstanceOf[Seq[Long]]
      // format location to be a string "[x, y]"
      val locationString = s"[${location(0)}, ${location(1)}]"
      // Foreach citizen, send a message to the topic "alarm_topic"
      jsondf.select("citizens").collect().foreach { citizens =>
        // ex: citizen = {"name":"Jesus","harmonyscore":39}
        citizens(0).asInstanceOf[Seq[Row]].foreach { citizen =>
          val citizenName = citizen(1).asInstanceOf[String]
          val citizenHarmonyScore = citizen(0).asInstanceOf[Long]
          if (citizenHarmonyScore <= 10) {
            val citizenMessage = s"""{"name": "$citizenName", "location": $locationString}"""
            println(citizenMessage)
            val record = new ProducerRecord[String, String]("alert_topic", citizenName, citizenMessage)
            producer.send(record)
          }
        }
      }
    }
    producer.close()

    loop(consumer, spark)
  }
}
