package at.abl.codingchallenges.flow

fun main() {
    val challenges = Challenges()

    val server = Server(challenges)
    server.start()
    Runtime.getRuntime().addShutdownHook(Thread { server.stop() })

    System.`in`.read()
    server.stop()
}