package scenarios

import com.rabbitmq.client.BuiltinExchangeType
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import net.sf.saxon.functions.ConstantFunction.True
import ru.tinkoff.gatling.amqp.Predef.{exchange, _}
import ru.tinkoff.gatling.amqp.protocol.AmqpProtocolBuilder
import scenarios.Utils._

import scala.concurrent.duration._
import scala.language.postfixOps

class PublishEngineEvents extends Simulation {

  val engineEvents = exchange("engineEvents", BuiltinExchangeType.TOPIC)
  val consumerQ = queue("test-in-q")

  def generateUUID() = java.util.UUID.randomUUID.toString()

  object Feeders {
    val messageId = Iterator.continually(Map("messageId" -> generateUUID()))
    val executionId = Iterator.continually(Map("executionId" -> generateUUID()))
    val eventId = Iterator.continually(Map("eventId" -> generateUUID()))
    val uuid = Iterator.continually(Map("uuid" -> generateUUID()))
    val deploymentId = Iterator.continually(Map("deploymentId" -> generateUUID()))
    val processInstanceId = Iterator.continually(Map("processInstanceId" -> generateUUID()))
    val integrationRequestId = Iterator.continually(Map("integrationRequestId" -> generateUUID()))
    val integrationResultId = Iterator.continually(Map("integrationResultId" -> generateUUID()))
  }

  val amqpConf: AmqpProtocolBuilder = amqp
    .connectionFactory(
      rabbitmq
        .host("localhost")
        .port(5672)
        .username("guest")
        .password("guest")
        .vhost("/")
    )
    .usePersistentDeliveryMode

  val scn: ScenarioBuilder = scenario("Query Performance Test")
    .feed(Feeders.eventId)
    .feed(Feeders.uuid)
    .feed(Feeders.processInstanceId)
    .feed(Feeders.executionId)
    .feed(Feeders.messageId)
    .exec(
      amqp("publish engine events").publish
        .topicExchange(engineEvents.name, "${messageId}")
        .textMessage(ElFileBody("events.json"))
        .contentType("application/json")
        .messageId("${messageId}")
        .priority(0)
    )
    .exitHere

  setUp(
    scn.inject(atOnceUsers(1))
//    scn.inject(rampUsersPerSec(1) to 5 during (60 seconds), constantUsersPerSec(5) during (5 minutes))
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
//      .startingFrom(30))
  ).protocols(amqpConf)
    .maxDuration(10 minutes)

}
