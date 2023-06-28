import java.util.Properties
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer

object Main {

// zone geographic, datalake timestamp
val props : Properties = new Properties()
props.put("bootstrap.servers", "broker1:9092")
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
val producer : KafkaProducer[String, String] = new KafkaProducer[String, String](props)

  def main(args: Array[String]): Unit = {
    println("Enternig begin of program")
  }

}
