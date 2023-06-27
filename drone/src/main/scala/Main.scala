import scala.util.Random
import io.jvm.uuid._

import java.nio.charset.StandardCharsets
import java.util.{Map => JMap}
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import org.apache.kafka.common.serialization.{Serdes => JSerdes}
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write


import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

object HarmonyWatcher {
  val pronouns: Array[String] = Array(
    "We",
    "I",
    "They",
    "He",
    "She"
  )

  val names: Array[String] = Array(
    "Jack",
    "Jim",
    "Jill",
    "John",
    "Jane",
    "Bob",
    "Bill",
    "Sally",
    "Sue",
    "Sam",
    "Samantha",
    "Alex",
    "Alexis",
    "Adam",
    "Eve",
    "Mary",
    "Joseph",
    "Jesus",
    "Mohammed",
    "Buddha",
    "Gandhi",
    "Martin Luther King Jr."
  ) ++ pronouns

  val verbs: Array[String] = Array(
    "was",
    "is",
    "are",
    "were"
  )

  val nouns: Array[String] = Array(
    "playing a game",
    "watching television",
    "talking",
    "dancing",
    "speaking",
    "eating",
    "drinking",
    "sleeping",
    "running",
    "walking",
    "sitting",
    "standing",
    "reading",
    "writing",
    "listening to music",
    "playing music",
    "killing",
    "hurting",
    "helping",
    "loving",
    "hating",
    "liking",
    "disliking",
    "burning",
    "freezing",
    "melting",
    "exploding",
    "imploding",
    "singing",
    "shouting",
    "whispering",
    "crying",
    "laughing",
    "smiling",
    "frowning",
    "screaming",
    "yelling"
  )

  val NB_CITIZENS: Int = 50

  class HarmonyWatcher {
    val id: String = UUID.random.string
    var position: Array[Double] = Array(0.0, 0.0)
    var citizens: List[Map[String, Any]] = List()
    var words: List[String] = List()

    println("Drone created")

    def createReport(): Map[String, Any] = {
      Map(
        "id" -> id,
        "current location" -> position,
        "citizens" -> citizens,
        "words" -> words,
        "timestamp" -> System.currentTimeMillis() / 1000L
      )
    }

    def updatePosition(): Unit = {
      position = Array.fill(2)(Random.nextDouble() * 20.0 - 10.0)
    }

    def createCitizen(): Map[String, Any] = {
      Map(
        "name" -> names(Random.nextInt(names.length)),
        "harmonyscore" -> Random.nextInt(101)
      )
    }

    def updateCitizens(): Unit = {
      citizens = List.fill(Random.nextInt(NB_CITIZENS) + 1)(createCitizen())
    }

    def createWord(): String = {
      s"${pronouns(Random.nextInt(pronouns.length))} ${verbs(Random.nextInt(verbs.length))} ${nouns(Random.nextInt(nouns.length))}"
    }

    def updateWords(): Unit = {
      words = List.fill(Random.nextInt(NB_CITIZENS) + 1)(createWord())
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val formats: Formats = DefaultFormats

    val props = new java.util.Properties()
    props.put("bootstrap.servers", "localhost:9093")
    // Create the Kafka producer
    val producer = new KafkaProducer[String, String](props, new StringSerializer, new StringSerializer)

    val watcher = new HarmonyWatcher()

    while (true) {
      watcher.updatePosition()
      watcher.updateCitizens()
      watcher.updateWords()

      // Convert the watcher to JSON
      val report = watcher.createReport()
      val reportJson = write(report)

      println(reportJson)

      // Send the watcher JSON to the 'drone_topic' Kafka topic
      val record = new ProducerRecord[String, String]("drone_topic", reportJson)
      producer.send(record)

      val delay = scala.util.Random.nextInt(5 * 60 * 1000 - 1 * 60 * 1000) + 1 * 60 * 1000
      Thread.sleep(delay)
    }
    // Close the Kafka producer
    producer.close()
  }
}

