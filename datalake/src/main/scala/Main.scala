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
    //val conf = SparkConf().setAppName("projectName").setMaster("local[*]")
    //val sc = SparkContext.getOrCreate(conf)

    // zone geographic, datalake timestamp
    val propsConsumer : Properties = new Properties()
    propsConsumer.put("bootstrap.servers", "localhost:9093")
    propsConsumer.put("key.deserializer", classOf[StringDeserializer].getName)
    propsConsumer.put("value.deserializer", classOf[StringDeserializer].getName)
    propsConsumer.put("group.id", "spark-consummer-group")
    propsConsumer.put("auto.offset.reset", "latest")
    propsConsumer.put("enable.auto.commit", false)
    println("Properties created.")

    val consumer: KafkaConsumer[String, String] = new KafkaConsumer[String, String](propsConsumer)
    println("Consumer created.")

    consumer.subscribe(Arrays.asList("drone_topic")) // Replace "topic_name" with your Kafka topic(s)
    println("Consumer subscribed.")
    //
    //    // zone geographic, datalake timestamp
    //    val propsProducer : Properties = new Properties()
    //    propsProducer.put("bootstrap.servers", "localhost:9093")
    //    propsProducer.put("key.serializer", classOf[StringSerializer].getName)
    //    propsProducer.put("value.serializer", classOf[StringSerializer].getName)
    //    println("Properties created.")
    //
    //    val producer: KafkaProducer[String, String] = new KafkaProducer[String, String](propsProducer)
    //    println("Producer created.")

    //    val spark = SparkSession
    //      .builder()
    //      .appName("KafkaToS3Example")
    //      .master("local[*]") // Replace with the appropriate master URL for your cluster
    //      .getOrCreate()
    //    println("SparkSession created")

    while (true) {
      val records = consumer.poll(100);

      // Get information concerning the topic
      records.partitions().forEach { topicPartition =>
        val topic = topicPartition.topic()
        val partition = topicPartition.partition()
        // Process topic and partition as needed
        println(s"Topic: $topic, Partition: $partition")
      }
      //      val data = records.parallelStream().map(record => (record.key(), record.value()))
      //      val dataFrame = spark.sparkContext.parallelize(data).toDF()
      //
      //      spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", ACCESS_KEY.getOrElse(""))
      //      spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", SECRET_KEY.getOrElse(""))
      //
      //      dataFrame.write.mode(SaveMode.Append)
      //        .option("header", "true")
      //        .csv(RAW_PATH)

      // Get value from topic and print the data
//      println(records)
      records.forEach { record =>
        val key = record.key()
        val value = record.value()
        println(s"Received message: key = $key, value = $value")

//        val data = Seq(Row(value))
//        println(data)
//        val schema = new StructType()
//          .add("id", StringType)
//          .add("citizens", AnyType)
//          .add("words", StringType)
//          .add("timestamp", LongType)
//          .add("current location", IntegerType)

//        val rdd = spark.sparkContext.parallelize(data)
//        val df = spark.createDataFrame(rdd, schema)
        import spark.implicits._
        val df = spark.read.json(Seq(value).toDS())

        // Flatten the citizens array column
        val flattenedDF = df.withColumn("citizen", explode($"citizens"))
          .select($"id", $"timestamp", $"current location", $"words",
            $"citizen.name".as("citizen_name"), $"citizen.harmonyscore")


        // Convert the current location array to string
//        val stringifyCitizens = udf((arr: Seq[Row]) => arr.map(row => s"${row.getString(0)}:${row.getLong(1)}").mkString(","))
//
//        val finalDF = flattenedDF.withColumn("citizens_str", stringifyCitizens($"citizen"))
//          .drop("citizens", "citizen")

        // Extract id and timestamp from the input value
        val id = flattenedDF.select("id").first().getString(0)
        val timestamp = flattenedDF.select("timestamp").first().getLong(0)

        // Generate the file path based on id and timestamp
        val filePath = s"$RAW_PATH/$id/$timestamp.json"

        // Write the final DataFrame to the specified CSV file
        flattenedDF.write.json(filePath)
        //val df = spark.createDataFrame(data, schema)
        //val df = spark.createDataFrame(data)

        //        val df = spark.createDataFrame(spark.sparkContext.parallelize(data), schema)
        //        df.printSchema()
        //        df.show()

        //        val data = List(value)
        //        val dataFrame = sqlContext.sparkContext.parallelize(data).toDF()
        //        val dataFrame = spark.createDataFrame(data).toDF("key", "value")
        //        val dataframe = data.toDF
        //        spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", ACCESS_KEY.getOrElse(""))
        //        spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", SECRET_KEY.getOrElse(""))

        //        dataFrame.write.mode(SaveMode.Append)
        //          .option("header", "true")
        //          .csv(RAW_PATH)
      }
    }
  }
}