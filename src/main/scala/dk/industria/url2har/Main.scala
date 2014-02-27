package dk.industria.url2har

import akka.actor.{ActorSystem, Props}

import java.nio.file.{Files, FileSystems, Path}


object Main extends App {

  val parser = new scopt.OptionParser[Configuration]("url2har") {
    head("url2har", "0.1")

    help("help") text("prints this usage text")

    opt[Unit]("verbose") action { (_, c) => c.copy(verbose = true) } text("verbose output.")

    opt[Int]("timeout") action { (v, c) => c.copy(pageLoadTimeout = v) } text("Timeout in seconds to use when performing a page load (default: 300).")

    opt[String]("profile") action { (v, c) => c.copy(profile = Option(v)) } text("Path to the Firefox profile to use.")

    opt[String]("progress") action { (v, c) => c.copy(progress = Option(v)) } text("Path to a progress file.")

    arg[String]("<input>") required() action { (v, c) => c.copy(input = v) } validate { x => if (isInputValid(x)) success else failure(s"[${x}] is not a readable file.") }  text("File containing URL to generate HAR files for.")
    
    arg[String]("<output>") required() action { (v, c) => c.copy(output = v) } validate { x => if (isOutputValid(x)) success else failure(s"[${x}] is not a writable directory.") }  text("Directory where the HAR files are written.")
  }

  parser.parse(args, Configuration()) map { config => {

    val system = ActorSystem("Browsing")
    system.actorOf(Props(classOf[MainActor], config), "Main")
  }
						   
  } getOrElse {
    // arguments are bad, usage message will have been displayed
  }




  private def isInputValid(filename: String): Boolean = {
    val path = FileSystems.getDefault().getPath(filename)
    (Files.isRegularFile(path) && Files.isReadable(path))
    true
  }

  private def isOutputValid(filename: String): Boolean = {
    val path = FileSystems.getDefault().getPath(filename)
    //(Files.isDirectory(path) && Files.isWritable(path))
    true
  }
}
