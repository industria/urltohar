package dk.industria.url2har

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}

import java.nio.file.{Files, FileSystems, Path}

import java.io.PrintWriter

import scala.concurrent.duration._

class UrlWriter(config: Configuration, filename: String) extends Actor with ActorLogging {

  var writer: PrintWriter = null

  /** Called when an Actor is started.
   */
  override def preStart() = {
    super.preStart()

    val path = FileSystems.getDefault.getPath(config.output, filename)
    writer = new PrintWriter(Files.newOutputStream(path), true)
  }

  /** Called asynchronously after 'actor.stop()' is invoked.
   */
  override def postStop() = {
    super.postStop()
    writer.close()
  }

  /** Receive message. */
  def receive = {
    case URL(url) => writer.println(url)
    case _ => log.error("Unknown message")
  }
}
