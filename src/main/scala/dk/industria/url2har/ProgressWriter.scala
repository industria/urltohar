package dk.industria.url2har

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}

import java.nio.file.{Files, FileSystems, Path}

import java.io.PrintWriter

import scala.concurrent.duration._

class ProgressWriter(config: Configuration) extends Actor with ActorLogging {

  val writeProgress = config.progress.isDefined

  var progressWriter: PrintWriter = null

  /** Called when an Actor is started.
   */
  override def preStart() = {
    super.preStart()
    
    if(writeProgress) {
      val path = FileSystems.getDefault.getPath(config.progress.get)
      progressWriter = new PrintWriter(Files.newOutputStream(path), true)
    }

  }

  /** Called asynchronously after 'actor.stop()' is invoked.
   */
  override def postStop() = {
    super.postStop()
    if(writeProgress) {
      progressWriter.close()
    }
  }


  private def append(url: String): Unit = {
    progressWriter.println(url)
  }


  /** Receive message. */
  def receive = {
    case URL(url) => {
      if(this.writeProgress) {
	append(url)
      }
    }
    case _ => log.error("Unknown message")
  }


}
