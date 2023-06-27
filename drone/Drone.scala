import scala.util.Random
import java.util.UUID

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

  val NB_CITIZENS: Int = 5

  class HarmonyWatcher {
    val id: UUID = UUID.randomUUID()
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

  def simulate(): Unit = {
    val watcher = new HarmonyWatcher()

    watcher.updatePosition()
    watcher.updateCitizens()
    watcher.updateWords()

    println(watcher.createReport())
  }

  def main(args: Array[String]): Unit = {
    simulate()
  }
}

