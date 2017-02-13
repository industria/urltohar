package dk.industria.url2har

import akka.actor.{Actor, ActorLogging, ActorRef}

import java.io.File
import java.nio.file.{FileSystems, Path}


import scala.compat.Platform

import scala.io.Source

import io.github.bonigarcia.wdm.FirefoxDriverManager

import org.openqa.selenium.remote.{DesiredCapabilities,  CapabilityType}
import org.openqa.selenium.{By, JavascriptExecutor, NoSuchElementException, Proxy, TimeoutException, WebDriver, WebElement}
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxProfile}
import org.openqa.selenium.support.events.{AbstractWebDriverEventListener, EventFiringWebDriver, WebDriverEventListener}

import net.lightbody.bmp.{BrowserMobProxy, BrowserMobProxyServer}
import net.lightbody.bmp.client.{ClientUtil}
import net.lightbody.bmp.proxy.{CaptureType}

import scala.concurrent.duration._

class UrlExporter(config: Configuration) extends Actor with ActorLogging with WebDriverEventListener {

  val outputPath = FileSystems.getDefault.getPath(config.output)

  var driver: WebDriver = null

  var proxy: BrowserMobProxy = null

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

    proxy = new BrowserMobProxyServer();
    proxy.start(0);

    val seleniumProxy = ClientUtil.createSeleniumProxy(proxy);


    val capabilities = new DesiredCapabilities();
    capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);

    profile.setPreference("network.proxy.http", "localhost")
    profile.setPreference("network.proxy.http_port", proxy.getPort())
    profile.setPreference("network.proxy.ssl", "localhost")
    profile.setPreference("network.proxy.ssl_port", proxy.getPort())
    profile.setPreference("network.proxy.type", 1)
    profile.setPreference("network.proxy.share_proxy_settings", true)
    profile.setPreference("network.proxy.socks", "localhost")
    profile.setPreference("network.proxy.socks_port", proxy.getPort())

    val firefox = new FirefoxDriver(profile)
    //val firefox = new FirefoxDriver(capabilities)

    proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);



    val eventWebDriver = new EventFiringWebDriver(firefox)

    eventWebDriver.manage().timeouts().pageLoadTimeout(config.pageLoadTimeout, java.util.concurrent.TimeUnit.SECONDS)
    eventWebDriver.manage().timeouts().implicitlyWait(config.pageLoadTimeout, java.util.concurrent.TimeUnit.SECONDS)
    eventWebDriver.register(this)

    eventWebDriver


  }


  private def navigateTo(sender: ActorRef, url: String) = {

    try {
      proxy.newHar(url)

      driver.navigate.to(url)

      val element = driver.findElement(By.tagName("body"))

      this.restartBatchSize = this.restartBatchSize + 1
      if((0 < config.restart) && (config.restart <= this.restartBatchSize)) {
	restartDriver()
      }

      val outputPathFull = outputPath.toAbsolutePath().toString()

      val writeFile = FileSystems.getDefault.getPath(outputPathFull, "archive" + System.currentTimeMillis() + ".har").toFile()

      val har = proxy.getHar();
      har.writeTo(writeFile)

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
