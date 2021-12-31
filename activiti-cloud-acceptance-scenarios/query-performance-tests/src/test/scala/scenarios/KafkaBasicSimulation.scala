package scenarios

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import org.apache.kafka.clients.producer.ProducerConfig
import ru.tinkoff.gatling.kafka.Predef._
import ru.tinkoff.gatling.kafka.protocol.KafkaProtocol

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class KafkaBasicSimulation extends Simulation {

  val kafkaConf: KafkaProtocol = kafka
    .topic("test.topic")
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
  val partitionCount = 2

  val scn: ScenarioBuilder = scenario("Basic")
    .exec(session => session.set("processInstanceId", java.util.UUID.randomUUID.toString()))
    .exec(session => session.set("partitionKey", Math.abs(session("processInstanceId").hashCode()) % partitionCount))
    .exec(session => {println(session.attributes); session})
    .exec(
      kafka("BasicRequest")
        .send[String, String]("${partitionKey}","""{"processInstanceId": "${processInstanceId}"}"""))

  setUp(
    scn.inject(atOnceUsers(10))
//  scn.inject(constantUsersPerSec(10) during (10 seconds))
  ).protocols(kafkaConf)

}
