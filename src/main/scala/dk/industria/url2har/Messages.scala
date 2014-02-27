package dk.industria.url2har


case class Produce(offset: Int)

case class URL(url: String)

case class ExportedURL(url: String)

case class FailedToExportURL(url: String)

case object NoMoreUrls
