package dk.industria.url2har

import akka.actor.{Actor, ActorLogging, ActorRef}

import java.io.File
import java.nio.file.{FileSystems, Path}


import scala.compat.Platform

import scala.io.Source

import io.github.bonigarcia.wdm.FirefoxDriverManager

import org.openqa.selenium.{By, NoSuchElementException, TimeoutException, WebDriver, WebElement}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxProfile}
import org.openqa.selenium.support.events.{AbstractWebDriverEventListener, EventFiringWebDriver, WebDriverEventListener}

import scala.concurrent.duration._

class UrlExporter(config: Configuration) extends Actor with ActorLogging with WebDriverEventListener {

  val outputPath = FileSystems.getDefault.getPath(config.output)


  var driver: WebDriver = null

  var beforeTime: Long = 0L

  var restartBatchSize: Int = 0

  /** Called when an Actor is started.
   */
  override def preStart() = {
    super.preStart()
    driver = setupDriver()
  }

  /** Called asynchronously after 'actor.stop()' is invoked.
   */
  override def postStop() = {
    super.postStop()
    driver.close()
  }


  def afterChangeValueOf(element: WebElement, driver: WebDriver, keysToSend: Array[CharSequence]): Unit = {
  }

  def afterClickOn(element: WebElement, driver: WebDriver): Unit = {
  }

  def afterFindBy(by: By, element: WebElement, driver: WebDriver): Unit = {
  }

  def afterNavigateBack(driver: WebDriver): Unit = {
  }

  def afterNavigateForward(driver: WebDriver): Unit = {
  }

  def afterNavigateRefresh(driver: WebDriver): Unit = {
  }

  def afterScript(script: String, driver: WebDriver): Unit = {
  }

  def beforeChangeValueOf(element: WebElement, driver: WebDriver, keysToSend: Array[CharSequence]): Unit = {
  }

  def beforeClickOn(element: WebElement, driver: WebDriver): Unit = {
  }

  def beforeFindBy(by: By, element: WebElement, driver: WebDriver): Unit = {
  }

  def beforeNavigateBack(driver: WebDriver): Unit = {
  }

  def beforeNavigateForward(driver: WebDriver): Unit = {
  }

  def beforeNavigateRefresh(driver: WebDriver): Unit = {
  }

  def beforeScript(script: String, driver: WebDriver): Unit = {
  }
   
  def onException(throwable: Throwable, driver: WebDriver): Unit = {
  }

  def afterNavigateTo(url: String, driver: WebDriver): Unit = {
    val delta = Platform.currentTime - this.beforeTime
    log.info("[{}] - [{}ms]", url, delta);

  }

  def beforeNavigateTo(url: String, driver: WebDriver): Unit = {
    this.beforeTime = Platform.currentTime
  }

  private def restartDriver(): Unit = {
    this.driver.quit()
    this.driver = setupDriver()
    this.restartBatchSize = 0
  }


  private def setupDriver(): WebDriver = {
    FirefoxDriverManager.getInstance().setup();

    val profile = if(config.profile.isDefined) {
      val filename = config.profile.get.replace("~", System.getProperty("user.home"))
      val file = FileSystems.getDefault.getPath(filename).toFile()
      new FirefoxProfile(file)
    } else {
      new FirefoxProfile()
    }

    val harExportTrigger = new File("har_export_trigger-0.5.0-beta.7-fx.xpi").getAbsoluteFile()
    profile.addExtension(harExportTrigger)
    profile.setPreference("extensions.netmonitor.har.contentAPIToken", "some")
    profile.setPreference("extensions.netmonitor.har.autoConnect", true)
    profile.setPreference("extensions.netmonitor.har.enableAutomation", true)

    profile.setPreference("app.update.enabled", false)
    
    val outputPathFull = outputPath.toAbsolutePath().toString()

    profile.setPreference("devtools.netmonitor.enabled", true)
    profile.setPreference("devtools.netmonitor.har.includeResponseBodies", true)
    profile.setPreference("devtools.netmonitor.har.forceExport", true)
    profile.setPreference("devtools.netmonitor.har.enableAutoExportToFile", true)
    profile.setPreference("devtools.netmonitor.har.defaultLogDir", outputPathFull)
    profile.setPreference("devtools.netmonitor.statistics", true)

    val eventWebDriver = new EventFiringWebDriver(new FirefoxDriver(profile))

    eventWebDriver.manage().timeouts().pageLoadTimeout(config.pageLoadTimeout, java.util.concurrent.TimeUnit.SECONDS)
    eventWebDriver.manage().timeouts().implicitlyWait(config.pageLoadTimeout, java.util.concurrent.TimeUnit.SECONDS)
    eventWebDriver.register(this)

    eventWebDriver
  }


  private def navigateTo(sender: ActorRef, url: String) = {

    try {
      driver.navigate.to(url)

      val element = driver.findElement(By.tagName("body"))

      this.restartBatchSize = this.restartBatchSize + 1
      if((0 < config.restart) && (config.restart <= this.restartBatchSize)) {
	restartDriver()
      }

      sender ! ExportedURL(url)
    } catch {
      case e: TimeoutException => {
	restartDriver()
	sender ! FailedToExportURL(url)
      }
      case e :NoSuchElementException => {
	sender ! FailedToExportURL(url)
      }
    }
  } 


  /** Receive message. */
  def receive = {
    case URL(url) => navigateTo(sender, url)
    case _ => log.error("Unknown message")
  }

}
