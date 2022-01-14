package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.{http, status}
import org.apache.kafka.clients.producer.ProducerConfig
import ru.tinkoff.gatling.kafka.Predef._
import ru.tinkoff.gatling.kafka.protocol.KafkaProtocol

import scala.concurrent.duration._
import scala.language.postfixOps
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class TwoStepProcessKafka extends Simulation {

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

  val metricsCollector: ScenarioBuilder = scenario("metricsCollector")
    .exec(http("metrics").get("http://localhost:8080/collector/metrics/streams")
      .asJson
      .check(status.is(200))
      .check(
        jsonPath("$._embedded.streamMetricsList[?(@.name == 'query')].applications[?(@.name == 'default-app')].aggregateMetrics[?(@.name == 'integration.channel.auditConsumer.send.mean')].value")
          .ofType[Double]
          .gte(10.0)
          .saveAs("auditConsumer")
      )
      .check(
        jsonPath("$._embedded.streamMetricsList[?(@.name == 'query')].applications[?(@.name == 'default-app')].aggregateMetrics[?(@.name == 'integration.channel.queryConsumer.send.mean')].value")
          .ofType[Double]
          .gte(10.0)
          .saveAs("queryConsumer")
      )
    )
    .exec(session => {
      println(session.attributes)
      session
    })

  val scn: ScenarioBuilder = scenario("twoStepProcess Performance Test")
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
      kafka("twoStepProcess-1.json")
        .send[String,String](session => session("partitionKey").validate[String], ElFileBody("twoStepProcess-1.json"))
    )
    .exitHere

  setUp(
//    metricsCollector.inject(constantUsersPerSec(1.0 ).during(2 minutes)),
    scn.inject(constantUsersPerSec(10.0 ).during(1 minutes))
       .andThen(metricsCollector.inject(atOnceUsers(1)))
//    scn.inject(rampUsersPerSec(1) to 5 during (60\ seconds), constantUsersPerSec(5) during (5 minutes))
//    scn.inject(      // Traffic shape definition....pretty self explanatory
//      nothingFor(2 seconds),
//      atOnceUsers(10),
//      rampUsers(10) during (10 seconds),
//      constantUsersPerSec(20) during (60 seconds),
//      constantUsersPerSec(20) during (60 seconds) randomized,
//    )
//    scn.inject(incrementUsersPerSec(10)
//      .times(10)
//      .eachLevelLasting(10.seconds)
//      .separatedByRampsLasting(3.seconds) // optional
//      .startingFrom(40))
  ).protocols(kafkaConf)
    .assertions(details("metrics").failedRequests.count.is(0))
    .maxDuration(10 minutes)
}
