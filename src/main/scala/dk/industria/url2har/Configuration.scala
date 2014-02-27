package dk.industria.url2har


case class Configuration(verbose: Boolean = false, input: String = "", output: String = "", profile: Option[String] = None, progress: Option[String] = None, pageLoadTimeout: Int = 300)
