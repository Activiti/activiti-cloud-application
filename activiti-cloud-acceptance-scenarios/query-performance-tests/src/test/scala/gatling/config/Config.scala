package gatling.config

object Config {
  val domain = System.getProperty("domain", "localhost").toString()

  val realm = System.getProperty("realm", "activiti").toString()
}