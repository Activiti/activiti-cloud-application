package gatling.config

object Config {
  val domain = System.getProperty("domain", "localhost:8080").toString()
  val sso = System.getProperty("sso", "localhost:8180").toString()

  val realm = System.getProperty("realm", "activiti").toString()
}