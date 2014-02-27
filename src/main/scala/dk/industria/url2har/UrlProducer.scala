package dk.industria.url2har

import akka.actor.{Actor, ActorLogging, ActorRef}

import java.nio.file.{FileSystems, Path}

import scala.io.Source

class UrlProducer(config: Configuration) extends Actor with ActorLogging {
  val inputPath = FileSystems.getDefault.getPath(config.input)
  val lineIterator = Source.fromFile(inputPath.toFile()).getLines

  /** Called when an Actor is started.
   */
  override def preStart() = {
    super.preStart()
  }

  /** Called asynchronously after 'actor.stop()' is invoked.
   */
  override def postStop() = {
    super.postStop()
  }


  private def produceLine(sender: ActorRef, offset: Int) {
    if(lineIterator.hasNext) {
      val url = lineIterator.next()
      sender ! URL(url)
    } else {
      sender ! NoMoreUrls
    }
  }




  /** Receive message. */
  def receive = {
    case Produce(offset) => produceLine(sender, offset) 
    case _ => log.error("Unknown message")
  }


}
