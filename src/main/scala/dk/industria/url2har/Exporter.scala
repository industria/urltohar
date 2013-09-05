package dk.industria.url2har

import java.io.File
import java.nio.file.{FileSystems, Path}

import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxProfile}

import scala.io.Source

class Exporter(config: Configuration) {

  val inputPath = FileSystems.getDefault.getPath(config.input)
  val outputPath = FileSystems.getDefault.getPath(config.output)


  private def setupBrowser(): WebDriver = {

    val profile = if(config.profile.isDefined) {
      val filename = config.profile.get.replace("~", System.getProperty("user.home"))
      val file = FileSystems.getDefault.getPath(filename).toFile()
      if(config.verbose) {
	Console.println(s"Using profile ${filename}")
      }
      new FirefoxProfile(file)
    } else {
      new FirefoxProfile()
    }

    val firebugExtension = new File("firebug-1.12.0-fx.xpi").getAbsoluteFile()
    if(config.verbose) {
      Console.println(s"Installing extension ${firebugExtension}")
    }
    profile.addExtension(firebugExtension)

    val netexportExtension = new File("netExport-0.8.xpi").getAbsoluteFile()
    if(config.verbose) {
      Console.println(s"Installing extension ${netexportExtension}")
    }
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
    if(config.verbose) {
      Console.println(s"Output path set in profile [${outputPathFull}]")
    }
    profile.setPreference("extensions.firebug.netexport.defaultLogDir", outputPathFull)

    new FirefoxDriver(profile)
  }

  def export() = {
    val driver = setupBrowser()
    if(config.verbose) {
      Console.println("Waiting 5 seconds for things to settle down.")
    }
    Thread.sleep(5000)

    for(line <- Source.fromFile(inputPath.toFile()).getLines) {

      if(config.verbose) {
	Console.print(s"Navigate to [${line}]...")
      }
      driver.navigate.to(line)
      Thread.sleep(5000)

      if(config.verbose) {
	Console.println("done")
      }
    }


    if(config.verbose) {
      Console.println("Waiting 5 seconds for things to settle down.")
    }
    Thread.sleep(5000)


    driver.close()
    

  }



}

