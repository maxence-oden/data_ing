import java.util.Properties
import java.util.Arrays
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.sql.SparkSession
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.KafkaConsumer

object Main {
  val ACCESS_KEY = sys.env.get("ACCESS_KEY")
  val SECRET_KEY = sys.env.get("SECRET_KEY")

  def main(args: Array[String]): Unit = {
    println("Enternig begin of program")
    println("ACCESS_KEY:", ACCESS_KEY)
    println("SECRET_KEY:", SECRET_KEY)

    // zone geographic, datalake timestamp
    val props : Properties = new Properties()
    props.put("bootstrap.servers", "localhost:9093")
    props.put("key.deserializer", classOf[StringDeserializer].getName)
    props.put("value.deserializer", classOf[StringDeserializer].getName)
    props.put("group.id", "spark-consummer-group")
    props.put("auto.offset.reset", "latest")
    props.put("enable.auto.commit", false)
    println("Properties created.")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](props)
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
      records.forEach { record =>
        val key = record.key()
        val value = record.value()
        println(s"Received message: key = $key, value = $value")
      }
    }

    //  val producer : KafkaProducer[String, String] = new KafkaProducer[String, String](props)

    // Replace Key with your AWS account key (You can find this on IAM
//    spark.sparkContext
//      .hadoopConfiguration.set("fs.s3a.access.key", ACCESS_KEY.getOrElse(""))
//    println("ACCESS_KEY configured.")
//    // Replace Key with your AWS secret key (You can find this on IAM
//    spark.sparkContext
//      .hadoopConfiguration.set("fs.s3a.secret.key", SECRET_KEY.getOrElse(""))
//    println("SECRET_KEY configured.")
//
//    spark.sparkContext
//      .hadoopConfiguration.set("fs.s3a.endpoint", "s3.amazonaws.com")
//    println("Endpoint created.")
  }

}