import akka.actor._
/*import akka.util.Duration
import akka.util.duration._*/

sealed trait PiMessage
case object Calculate extends PiMessage
case class Work(start: Int, nrOfElements: Int) extends PiMessage
case class Result(value: Double) extends PiMessage
case class PiApproximation(pi: Double, duration: Double) extends PiMessage

class Worker extends Actor {
 def receive = {
  case Work(start, nrOfElements) => sender ! Result(calcuatePiFor(start, nrOfElements)) // perform the work
 }

 def calcuatePiFor(start: Int, nrOfElements: Int) : Double = {
  var acc = 0.0
  for(i <- start until (start + nrOfElements) ) acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1)
  acc
 }
}


class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef) extends Actor {

  var pi: Double = _
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis
  var workers: List[ActorRef] = Nil

  for (i <- (1 to nrOfWorkers).reverse)
    workers = context.actorOf(Props[Worker], name = "Worker" + i) :: workers

  def receive = {
  case Calculate =>
    for (i <- 0 until nrOfMessages) 
      workers (i % (nrOfWorkers - 1)) ! Work(i * nrOfElements, nrOfElements)
  case Result(value) => 
    pi += value
    nrOfResults += 1
    if (nrOfResults == nrOfMessages) {
      // Send the result to the listener
      listener ! PiApproximation(pi, duration = (System.currentTimeMillis - start))
      // Stops this actor and all its supervised children
      context.stop(self)
    }
  }
}

class Listener extends Actor {
  def receive = {
    case PiApproximation(pi, duration) =>
      println("\n\tPi approximation: \t\t%s\n\tCalculation time: \t%s".format(pi, duration))
      context.system.shutdown()
  }
}

object Pi extends App {

  calculate(nrOfWorkers = 100, nrOfElements = 10000, nrOfMessages = 10000)

  // actors and messages ...

  def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
    // Create an Akka system
    val system = ActorSystem("PiSystem")

    // create the result listener, which will print the result and 
    // shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")

    // create the master
    val master = system.actorOf(
		  Props(new Master(nrOfWorkers, nrOfMessages, 
			  nrOfElements, listener)),
		  name = "master")

    // start the calculation
    master ! Calculate

  }
}
