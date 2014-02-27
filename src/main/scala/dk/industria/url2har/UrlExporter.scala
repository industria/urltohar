package dk.industria.url2har

import akka.actor.{Actor, ActorLogging, ActorRef}

import java.io.File
import java.nio.file.{FileSystems, Path}


import scala.compat.Platform

import scala.io.Source

import org.openqa.selenium.{By, TimeoutException, WebDriver, WebElement}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxProfile}
import org.openqa.selenium.support.events.{AbstractWebDriverEventListener, EventFiringWebDriver, WebDriverEventListener}


class UrlExporter(config: Configuration) extends Actor with ActorLogging with WebDriverEventListener {

  val outputPath = FileSystems.getDefault.getPath(config.output)


  var driver: WebDriver = null

  var beforeTime: Long = 0L


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


  def afterChangeValueOf(element: WebElement, driver: WebDriver): Unit = {
  }

  def afterClickOn(element: WebElement, driver: WebDriver): Unit = {
  }

  def afterFindBy(by: By, element: WebElement, driver: WebDriver): Unit = {
  }

  def afterNavigateBack(driver: WebDriver): Unit = {
  }

  def afterNavigateForward(driver: WebDriver): Unit = {
  }

  def afterScript(script: String, driver: WebDriver): Unit = {
  }

  def beforeChangeValueOf(element: WebElement, driver: WebDriver): Unit = {
  }

  def beforeClickOn(element: WebElement, driver: WebDriver): Unit = {
  }

  def beforeFindBy(by: By, element: WebElement, driver: WebDriver): Unit = {
  }

  def beforeNavigateBack(driver: WebDriver): Unit = {
  }

  def beforeNavigateForward(driver: WebDriver): Unit = {
  }

  def beforeScript(script: String, driver: WebDriver): Unit = {
  }
   
  def onException(throwable: Throwable, driver: WebDriver): Unit = {
  }

  def afterNavigateTo(url: String, driver: WebDriver): Unit = {
    val delta = Platform.currentTime - this.beforeTime
    log.info("[{}] - [{}ms]", url, delta);

    sender ! ExportedURL(url)
  }

  def beforeNavigateTo(url: String, driver: WebDriver): Unit = {
    this.beforeTime = Platform.currentTime
  }

  private def setupDriver(): WebDriver = {
    val profile = if(config.profile.isDefined) {
      val filename = config.profile.get.replace("~", System.getProperty("user.home"))
      val file = FileSystems.getDefault.getPath(filename).toFile()
      new FirefoxProfile(file)
    } else {
      new FirefoxProfile()
    }

    val firebugExtension = new File("firebug-1.12.0-fx.xpi").getAbsoluteFile()
    profile.addExtension(firebugExtension)

    val netexportExtension = new File("netExport-0.8.xpi").getAbsoluteFile()
    profile.addExtension(netexportExtension)
    profile.setPreference("app.update.enabled", false)
    
    // Set default Firebug preferences
    profile.setPreference("extensions.firebug.currentVersion", "1.12.0")
    profile.setPreference("extensions.firebug.allPagesActivation", "on")
    profile.setPreference("extensions.firebug.defaultPanelName", "net")
    profile.setPreference("extensions.firebug.net.enableSites", true)

    // Set default NetExport preferences
    profile.setPreference("extensions.firebug.netexport.alwaysEnableAutoExport", true)
    profile.setPreference("extensions.firebug.netexport.showPreview", false)

    val outputPathFull = outputPath.toAbsolutePath().toString()

    profile.setPreference("extensions.firebug.netexport.defaultLogDir", outputPathFull)

    val eventWebDriver = new EventFiringWebDriver(new FirefoxDriver(profile))

    eventWebDriver.manage().timeouts().pageLoadTimeout(config.pageLoadTimeout, java.util.concurrent.TimeUnit.SECONDS)

    eventWebDriver.register(this)

    eventWebDriver
  }


  private def navigateTo(sender: ActorRef, url: String) = {

    try {
      driver.navigate.to(url)
    } catch {
      case e: TimeoutException => {
	this.driver.quit()
	this.driver = setupDriver()
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
