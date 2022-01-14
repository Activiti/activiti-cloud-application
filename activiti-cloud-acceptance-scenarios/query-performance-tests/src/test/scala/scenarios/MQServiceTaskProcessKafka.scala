package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import org.apache.kafka.clients.producer.ProducerConfig
import ru.tinkoff.gatling.kafka.Predef._
import ru.tinkoff.gatling.kafka.protocol.KafkaProtocol

import scala.concurrent.duration._
import scala.language.postfixOps

class MQServiceTaskProcessKafka extends Simulation {

  val kafkaConf: KafkaProtocol = kafka
    .topic("engineEvents")
    .properties(
      Map(
        ProducerConfig.ACKS_CONFIG -> "1",
        // list of Kafka broker hostname and port pairs
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:29092",
        // in most cases, StringSerializer or ByteArraySerializer
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG ->
          "org.apache.kafka.common.serialization.StringSerializer"
      )
    )

  def generateUUID() = java.util.UUID.randomUUID.toString()

  object Feeders {
    val messageId = Iterator.continually(Map("messageId" -> generateUUID()))
    val executionId = Iterator.continually(Map("executionId" -> generateUUID()))
    val eventId = Iterator.continually(Map("eventId" -> generateUUID()))
    val deploymentId = Iterator.continually(Map("deploymentId" -> generateUUID()))
    val processInstanceId = Iterator.continually(Map("processInstanceId" -> generateUUID()))
    val integrationRequestId = Iterator.continually(Map("integrationRequestId" -> generateUUID()))
    val integrationResultId = Iterator.continually(Map("integrationResultId" -> generateUUID()))
    val taskId = Iterator.continually(Map("taskId" -> generateUUID()))
  }

  val partitionCount = 1

  val scn: ScenarioBuilder = scenario("MQServiceTaskProcess Performance Test")
    .feed(Feeders.deploymentId)
    .feed(Feeders.eventId)
    .feed(Feeders.processInstanceId)
    .feed(Feeders.executionId)
    .feed(Feeders.messageId)
    .feed(Feeders.integrationRequestId)
    .feed(Feeders.integrationResultId)
    .feed(Feeders.taskId)
    .exec(session => session.set("partitionKey", Math.abs(session("processInstanceId").hashCode()) % partitionCount))
    .exec(
      kafka("MQServiceTaskProcess-2.json")
        .send[String,String](session => session("partitionKey").validate[String], ElFileBody("MQServiceTaskProcess-2.json"))
    )
    .pause(1 second)
    .exec(
      kafka("MQServiceTaskProcess-3.json")
        .send[String,String](session => session("partitionKey").validate[String], ElFileBody("MQServiceTaskProcess-3.json"))
    )
    .pause(1 second)
    .exec(
      kafka("MQServiceTaskProcess-4.json")
        .send[String,String](session => session("partitionKey").validate[String], ElFileBody("MQServiceTaskProcess-4.json"))
    )
    .exitHere


  setUp(
    scn.inject(atOnceUsers(40))
//    scn.inject(rampUsersPerSec(1) to 5 during (60 seconds), constantUsersPerSec(5) during (5 minutes))
//    scn.inject(      // Traffic shape definition....pretty self explanatory
//      nothingFor(2 seconds),
//      atOnceUsers(10),
//      rampUsers(10) during (10 seconds),
//      constantUsersPerSec(20) during (60 seconds),
//      constantUsersPerSec(20) during (60 seconds) randomized,
//    )
//    scn.inject(incrementUsersPerSec(10)
//      .times(8)
//      .eachLevelLasting(10.seconds)
//      .separatedByRampsLasting(3.seconds) // optional
//      .startingFrom(30))
  ).protocols(kafkaConf)
    .maxDuration(10 minutes)

}
