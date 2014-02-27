package dk.industria.url2har

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}

import java.nio.file.{FileSystems, Path}

import scala.io.Source

import scala.concurrent.duration._

class MainActor(config: Configuration) extends Actor with ActorLogging {
  private case object Setup
  private case object Start
  private case object DelayedShutdown

  private var urlProducer: ActorRef = null
  private var urlExporter: ActorRef = null
  private var progressWriter: ActorRef = null

  /** Called when an Actor is started.
   */
  override def preStart() = {
    super.preStart()
    self ! Setup
    self ! Start
  }

  /** Called asynchronously after 'actor.stop()' is invoked.
   */
  override def postStop() = {
    super.postStop()
  }

  private def setupApplication() = {
    urlProducer = context.actorOf(Props(classOf[UrlProducer], config), "UrlProducer")
    urlExporter = context.actorOf(Props(classOf[UrlExporter], config), "Exporter")
    progressWriter = context.actorOf(Props(classOf[ProgressWriter], config), "Progress")
  }


  private def scheduleUrl(url: String) = {
    log.info("Handle URL [{}]", url)
    urlExporter ! URL(url)
  }

  private def exported(url: String): Unit = {
    progressWriter ! URL(url)
    urlProducer ! Produce(0)
  }



  private def failedURL(url: String): Unit = {

    log.info("FAILED : [{}]", url)

    // TODO: write failed... maybe retry ?

    urlProducer ! Produce(0)

  }


  private def scheduleShutdown(): Unit = {

    log.info("Scheduling shutdown in 10 seconds")

    import context.dispatcher


    context.system.scheduler.scheduleOnce(10 seconds, self, DelayedShutdown)

  }



  /** Receive message. */
  def receive = {
    case Setup => setupApplication()
    case Start => urlProducer ! Produce(0)
    case URL(url) => scheduleUrl(url) 
    case ExportedURL(url) => exported(url)
    case FailedToExportURL(url) => failedURL(url) 
    case NoMoreUrls => scheduleShutdown()
    case DelayedShutdown => {
      log.info("Shutdown the application.")
      context.system.shutdown()
    }
    case _ => log.error("Unknown message")
  }


}
