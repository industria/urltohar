package dk.industria.url2har

case class Configuration(input: String = "", output: String = "", profile: Option[String] = None, pageLoadTimeout: Int = 60)
