package scenarios

import com.rabbitmq.client.BuiltinExchangeType
import io.gatling.commons.validation.Validation
import io.gatling.core.Predef._
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioBuilder
import ru.tinkoff.gatling.amqp.Predef.{exchange, _}
import ru.tinkoff.gatling.amqp.protocol.AmqpProtocolBuilder

import java.util
import scala.concurrent.duration._
import scala.language.postfixOps

class MQServiceTaskProcessRabbit extends Simulation {

  val engineEvents = exchange("engineEvents", BuiltinExchangeType.TOPIC)

  def generateUUID() = java.util.UUID.randomUUID.toString()

  var headers = List[(String, Expression[String])](
    "appName" -> "activiti-app",
           "deploymentId" -> "${deploymentId}",
           "deploymentName" -> "SpringAutoDeployment",
           "deploymentVersion" -> "1",
           "messagePayloadType" -> "[Lorg.activiti.cloud.api.model.shared.events.CloudRuntimeEvent;",
           "rootBusinessKey" -> "businessKey",
           "rootProcessDefinitionId" -> "MQServiceTaskProcess:1:1156578c-57b9-11ec-9110-747827bd7e73",
           "rootProcessDefinitionKey" -> "MQServiceTaskProcess",
           "rootProcessDefinitionName" -> "MQServiceTaskProcess",
           "rootProcessDefinitionVersion" -> "1",
           "rootProcessInstanceId" -> "${processInstanceId}",
           "routingKey" -> "engineEvents.test-app.activiti-app",
           "serviceFullName" -> "test-app",
           "serviceName" -> "test-app",
           "serviceType" -> "runtime-bundle",
           "serviceVersion" -> ""
  )

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

  val partitionCount = 2

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
      amqp("MQServiceTaskProcess-2.json").publish
        .topicExchange(engineEvents.name, "engineEvents-${partitionKey}")
        .textMessage(ElFileBody("MQServiceTaskProcess-2.json"))
        .contentType("application/json")
        .messageId("${messageId}")
        .headers(headers: _*)
        .priority(0)
    )
    .pause(1 second)
    //.pace(2 seconds)
    .exec(
      amqp("MQServiceTaskProcess-3.json").publish
        .topicExchange(engineEvents.name, "engineEvents-${partitionKey}")
        .textMessage(ElFileBody("MQServiceTaskProcess-3.json"))
        .contentType("application/json")
        .messageId("${messageId}")
        .headers(headers: _*)
    )
    //.pace(2 seconds)
    .pause(2 second)
    .exec(
      amqp("MQServiceTaskProcess-4.json").publish
        .topicExchange(engineEvents.name, "engineEvents-${partitionKey}")
        .textMessage(ElFileBody("MQServiceTaskProcess-4.json"))
        .contentType("application/json")
        .messageId("${messageId}")
        .headers(headers: _*)
    )
    .exitHere

  setUp(
//    scn.inject(atOnceUsers(10))
//    scn.inject(rampUsersPerSec(1) to 5 during (60 seconds), constantUsersPerSec(5) during (5 minutes))
//    scn.inject(      // Traffic shape definition....pretty self explanatory
//      nothingFor(2 seconds),
//      atOnceUsers(10),
//      rampUsers(10) during (10 seconds),
//      constantUsersPerSec(20) during (60 seconds),
//      constantUsersPerSec(20) during (60 seconds) randomized,
//    )
    scn.inject(incrementUsersPerSec(10)
      .times(8)
      .eachLevelLasting(10.seconds)
      .separatedByRampsLasting(3.seconds) // optional
      .startingFrom(30))
  ).protocols(amqpConf)
    .maxDuration(10 minutes)

}
