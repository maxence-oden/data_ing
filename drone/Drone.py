import random
import time

pronouns = [
    "We",
    "I",
    "They",
    "He",
    "She",
]

names = [
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
    "Martin Luther King Jr.",
] + pronouns

verbs = ["was", "is", "are", "were"]
nouns = [
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
    "yelling",
]

NB_CITIZENS = 5


class HarmonyWatcher:
    def __init__(self, id):
        self.id = id
        self.position = [0.0, 0.0, 0.0]
        self.citizens = []
        self.words = []
        print("Drone created")

    def create_report(self):
        report = {
            "id": self.id,
            "current location": self.position,
            "citizens": self.citizens,
            "words": self.words,
            "timestamp": time.time(),
        }
        return report

    def update_position(self):
        for i in range(len(self.position)):
            self.position[i] = random.uniform(-10.0, 10.0)

    def create_citizen(self):
        citizen = {
            "name": random.choice(names),
            "harmonyscore": random.randint(0, 100),
        }
        return citizen

    def update_citizens(self):
        self.citizens = []

        for i in range(random.randint(0, NB_CITIZENS)):
            self.citizens.append(self.create_citizen())

    def create_word(self):
        word = random.choice(pronouns) + random.choice(verbs) + random.choice(nouns)
        return word

    def update_words(self):
        self.words = []

        for i in range(random.randint(0, NB_CITIZENS)):
            self.words.append(self.create_word())
