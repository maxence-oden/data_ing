name := "datalake"
version := "1.0"
scalaVersion := "2.12.12"

//resolvers += "org.apache.spark:spark-streaming-kafka_2.12:3.0.1" at "https://repo1.maven.org/maven2/org/apache/spark/spark-streaming-kafka_2.12/3.0.1/spark-streaming-kafka_2.12-3.0.1.pom"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.12" % "3.0.1",
  "org.apache.spark" % "spark-sql_2.12" % "3.0.1",
  "org.apache.spark" % "spark-streaming_2.12" % "3.3.1",
  "org.apache.spark" % "spark-mllib_2.12" % "3.0.1",
  "org.jmockit" % "jmockit" % "1.34" % "test",
  // "org.apache.spark" % "spark-streaming-kafka" % "3.0.1"
  "org.apache.kafka" % "kafka-clients" % "2.8.1",
  "org.apache.hadoop" % "hadoop-aws" % "3.3.1", // Add this line
  "org.apache.hadoop" % "hadoop-hdfs" % "3.2.2", // Add this line for Hadoop dependency
  "org.apache.hadoop" % "hadoop-common" % "3.2.2" // Add this line for Hadoop dependency

)