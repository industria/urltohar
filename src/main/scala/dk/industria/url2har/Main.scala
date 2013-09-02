package dk.industria.url2har


object Main extends App {


  val parser = new scopt.OptionParser[Configuration]("url2har") {
    head("url2har", "0.1")

    help("help") text("prints this usage text")

    opt[Unit]("verbose") action { (_, c) => c.copy(verbose = true) } text("verbose output")

    arg[String]("<input>") required() action { (v, c) => c.copy(input = v) } validate { x => if (true) success else failure("Value <input> is not a file") }  text("File containing URL to generate HAR files for")
    
    arg[String]("<output>") required() action { (v, c) => c.copy(output = v) } validate { x => if (true) success else failure("Value <output> is not a directory") }  text("Directory where the HAR files are written")
  }

  parser.parse(args, Configuration()) map { config => {

    Console.println(s"Starter with ${config.verbose}, input ${config.input} => ${config.output}")

  }
						   
  } getOrElse {
    // arguments are bad, usage message will have been displayed
  }


}
