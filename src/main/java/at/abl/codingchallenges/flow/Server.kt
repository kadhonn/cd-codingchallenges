package at.abl.codingchallenges.flow

import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.Charset

private const val PATH_PREFIX = "/api/v1/Challenge/"
private const val PATH_CHALLENGEE = "/challengee/"

class Server(private val challenges: Challenges) {

    private val gson = Gson()
    private val httpServer = HttpServer.create(InetSocketAddress("0.0.0.0", 8080), 20)!!

    fun start() {
        httpServer.start()
    }

    fun stop() {
        httpServer.stop(2)
    }

    private fun handleGetChallenge(exchange: HttpExchange, challenge: String, challengee: String) {
        val challengeData = challenges.getChallengeData(challenge, challengee)
        sendJsonBody(challengeData, exchange)
    }

    private fun handlePostChallenge(exchange: HttpExchange, challenge: String, challengee: String) {
        val solution = exchange.requestBody.readAllBytes().toString(Charset.defaultCharset())
        val challengeResult = challenges.validateChallenge(challenge, challengee, solution)
        sendJsonBody(challengeResult, exchange)
    }

    private fun sendJsonBody(
        challengeData: Any,
        exchange: HttpExchange
    ) {
        val body = gson.toJson(challengeData).toByteArray()

        exchange.sendResponseHeaders(200, body.size.toLong())
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.responseBody.write(body)
    }

    private val challengeHandler: HttpHandler = HttpHandler { exchange ->
        val (challenge, challengee) = tryParseUri(exchange)
        if (challenge == null || challengee == null) {
            exchange.sendResponseHeaders(404, -1)
        } else {
            when (exchange.requestMethod) {
                "GET" -> {
                    handleGetChallenge(exchange, challenge, challengee)
                }
                "POST" -> {
                    handlePostChallenge(exchange, challenge, challengee)
                }
                else -> {
                    exchange.sendResponseHeaders(405, -1)
                }
            }
        }
        exchange.close()
    }

    private fun tryParseUri(exchange: HttpExchange): Pair<String?, String?> {
        var challenge: String? = null
        var challengee: String? = null
        var path = exchange.requestURI.path
        if (path.startsWith(PATH_PREFIX)) {
            path = path.substring(PATH_PREFIX.length)
            val endIndex = path.indexOf("/")
            if (endIndex != -1) {
                challenge = path.substring(0, endIndex)
                path = path.substring(challenge.length)
                if (path.startsWith(PATH_CHALLENGEE)) {
                    path = path.substring(PATH_CHALLENGEE.length)
                    if (!path.contains("/")) {
                        challengee = path
                    }
                }
            }
        }
        return Pair(challenge, challengee)
    }

    private val discoveryHandler: HttpHandler = HttpHandler { exchange ->
        if (exchange.requestMethod != "GET") {
            exchange.sendResponseHeaders(405, -1)
        } else {
            val result = ("[{\n" +
                    "\t\"Identifier\":\"Abl_01\",\n" +
                    "\t\"Name:\"Abl 01\",\n" +
                    "\t\"Description\":\"Abls first test challenge\",\n" +
                    "\t\"PreRequisits\":[],\n" +
                    "}]").toByteArray()
            exchange.sendResponseHeaders(200, result.size.toLong())
            exchange.responseBody.write(result)
        }
        exchange.close()
    }

    init {
        httpServer.createContext("/api/v1/Challenge/Discovery", discoveryHandler)
        httpServer.createContext("/api/v1/Challenge/", challengeHandler)
    }
}