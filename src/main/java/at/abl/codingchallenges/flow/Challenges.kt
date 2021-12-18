package at.abl.codingchallenges.flow


class Challenges {
    fun getChallengeData(challenge: String, challengee: String): ChallengeData{

    }

    fun validateChallenge(challenge: String, challengee: String, solution: String): ChallengeResult{

    }
}

data class ChallengeData(val message: String, val attachments: Array<String> = Array(0){""}) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChallengeData

        if (message != other.message) return false
        if (!attachments.contentEquals(other.attachments)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + attachments.contentHashCode()
        return result
    }
}

data class ChallengeResult(val success: Boolean, val message: String)