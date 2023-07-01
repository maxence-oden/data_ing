import java.util.Properties
import java.util.Arrays
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.spark.sql.{Row, SQLContext, SaveMode, SparkSession}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.SparkContext
import org.apache.spark.sql.types._
import org.apache.spark.streaming._
import org.apache.spark.sql.functions._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.spark
import org.apache.spark.sql.functions.explode
object Main {
  val ACCESS_KEY = sys.env.get("ACCESS_KEY")
  val SECRET_KEY = sys.env.get("SECRET_KEY")
  val BUCKET_NAME = "myharmonisedbucket"
  val RAW_PATH = "s3a://" + BUCKET_NAME + "/raw_data" // Folder to write input data

  def main(args: Array[String]): Unit = {
    println("Enternig begin of program")
    println("ACCESS_KEY:", ACCESS_KEY)
    println("SECRET_KEY:", SECRET_KEY)

    val spark = SparkSession
      .builder()
      .appName("MyAppS3Storing")
      .master("local[*]")
      .getOrCreate()

    // zone geographic, datalake timestamp
    val propsConsumer: Properties = new Properties()
    propsConsumer.put("bootstrap.servers", "localhost:9093")
    propsConsumer.put("key.deserializer", classOf[StringDeserializer].getName)
    propsConsumer.put("value.deserializer", classOf[StringDeserializer].getName)
    propsConsumer.put("group.id", "spark-consummer-group")
    propsConsumer.put("auto.offset.reset", "latest")
    propsConsumer.put("enable.auto.commit", false)
    println("Properties created.")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](propsConsumer)
    println("Consumer created.")

    consumer.subscribe(Arrays.asList("drone_topic"))
    println("Consumer subscribed.")

    def loop() {
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

        import spark.implicits._
        val df = spark.read.json(Seq(value).toDS())

        // Flatten the citizens array column
        val flattenedDF = df.withColumn("citizen", explode($"citizens"))
          .select($"id", $"timestamp", $"current location", $"words",
            $"citizen.name".as("citizen_name"), $"citizen.harmonyscore")

        // Extract id and timestamp from the input value
        val id = flattenedDF.select("id").first().getString(0)
        val timestamp = flattenedDF.select("timestamp").first().getString(0)

        // Generate the file path based on id and timestamp
        val filePath = s"$RAW_PATH/$id/$timestamp.json"

        // Write the final DataFrame to the specified CSV file
        flattenedDF.write.json(filePath)
      }
      loop();
    }
    loop();
  }
  }