import java.util.Properties
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import scala.collection.JavaConverters._

import sttp.client3._

import org.json4s._
import org.json4s.native.JsonMethods._

//class Body(name: String, location: List[Float])

object KafkaConsumer {
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-consumer-group")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")

    val consumer = new KafkaConsumer[String, String](props)

    consumer.subscribe(List("alert_topic").asJava)

    loop(consumer)

    consumer.close()
  }

  def loop(consumer: KafkaConsumer[String, String]): Unit = {
    val records = consumer.poll(java.time.Duration.ofSeconds(1))

    records.iterator().asScala.foreach { record =>
      val message = record.value()
      // Call your function to send a POST request to Discord webhook with the message
      sendDiscordWebhook(message)
    }

    loop(consumer)
  }

  def sendDiscordWebhook(message: String): Unit = {
    val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
    implicit val formats: Formats = DefaultFormats

    // Convert message to json
    val messageJson = parse(message)

    // Extract the name and location from the message

    // val name = (messageJson \ "name").extract[String]
    // val location = (messageJson \ "location").extract[List[Float]]

    val webhookUrl = "https://discord.com/api/webhooks/1123598478866661436/XIjKnd0pnqonn_ZPPVA0sbouimRHhE2ibHtDeS3MnbolBINx-2eqthCE5nbzow_OeZE5"

    val citizen = (messageJson \ "name").extract[String]
    val location = (messageJson \ "location").extract[List[Float]]
    val locationString = location.mkString(", ")

    val payload = s"""{
      "content": null,
      "embeds": [
        {
          "title": "Location: $locationString",
          "color": 14297642,
          "author": {
            "name": "Citizen: $citizen"
          }
        }
      ],
      "attachments": []
    }"""

    println(payload)
    

    val response = basicRequest
      .contentType("application/json")
      .post(uri"$webhookUrl")
      .body(payload)
      .send(backend)

    response.body match {
      case Left(error) =>
        println(s"Failed to send POST request to Discord webhook: $error")
      case Right(_) =>
        println("POST request sent to Discord webhook successfully.")
    }
  }
}
