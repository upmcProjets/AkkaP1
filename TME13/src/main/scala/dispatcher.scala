
package upmc.akka.culto

import math._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

import akka.actor.{Props, Actor, ActorRef, ActorSystem}

//Question 1
abstract class ObjetMusical
case class Note (pitch:Int, vol:Int, dur:Int) extends ObjetMusical
case class Chord (date:Int, notes:List[Note]) extends ObjetMusical
case class Chordseq (chords:List[Chord]) extends ObjetMusical
case class Voix (id:Int, chords:List[Chord]) extends ObjetMusical
case class MidiNote (pitch:Int, vel:Int, dur:Int, at:Int) 

case class PlayVoice(num : Int, chords:List[Chord])
case class PlayNormal(chordseq:Chordseq)

/* MAIN */
object PlayCantate extends App {  
  val system = ActorSystem("VoicerSystem")
  val PlayerActor = system.actorOf(Props[PlayerActor], name = "Player")
 
  //Question 1
  val cantate  = Chordseq ( List (Chord (0    , List (Note (50, 100, 4000), Note (65, 100, 4000), Note (69, 100, 4000), Note (62, 100, 4000)))
				, Chord (4000 , List (Note (49, 100, 1000), Note (64, 100, 1000), Note (69, 100, 1000), Note (57, 100, 1000)))
				, Chord (5000 , List (Note (50, 100, 1000), Note (62, 100, 1000), Note (65, 100, 1000), Note (57, 100, 1000)))
				, Chord (6000 , List (Note (52, 100, 1000), Note (61, 100, 1000), Note (67, 100, 1000), Note (55, 100, 1000)))
				, Chord (7000 , List (Note (53, 100, 500) , Note (62, 100, 500) , Note (69, 100, 1000), Note (62, 100, 1000)))
				, Chord (7500 , List (Note (55, 100, 500) , Note (64, 100, 500) , Note (69, 100, 1000), Note (62, 100, 1000))) ))
   val VoicerActor = system.actorOf(
		  Props(new VoicerActor(4,4, PlayerActor)),
		  name = "VoicerActor")
  //To test your cantate send this msg
  //PlayerActor ! PlayNormal(cantate)
    PlayerActor ! PlayVoice(0,cantate.chords)

  //End Question 1

///////////////////////////////

class PlayerActor extends Actor {
val remote = context.actorSelection("akka.tcp://Player@127.0.0.1:6000/user/PlayerActor")
/// To send to my laptop just change the ip adresse to ...
def receive = {
    case PlayVoice(num,chords)  => {
     VoicerActor ! chords
    }
    case PlayNormal (chordseq) => {
      remote ! chordseq
    }
  }
}
////////////////////QUESTION 2
///////////////// Worker
class Worker(num :Int) extends Actor {
 def receive = {
	case Work(date,notes) => sender ! Result(num,calculatechord(notes))
 }
 def calculatechord(chord: note) : List[Chord] = {
	va rep = list[Chord]
	 for(i <- start to (start + nbelements)) rep::notes[num]
	rep
 }
}

///////////  Master 
class VoicerActor(nbrOfWorkers: Int, nbrOfElements: Int, playerActor: ActorRef) 
extends Actor {
 var nbrofResults : List[Int] = List(0,0,0,0)
 var Results : List[List[Chord]] = List(Nil,Nil,Nil,Nil)
 var workers: List[ActorRef] = Nil
 var nbrOfMessages : Int =(ceil(Contate.chords.length/nbrOfElements)).toInt

 for (i <- (1 to nbrOfWorkers).reverse)
    workers = context.actorOf(Props[Worker],name = "Worker" + i) :: workers

 def receive = {
  case chords => {
    for (i <- 1 to nbrOfMessages){ 
      workers (i % (nbrOfWorkers - 1)) ! Work(chords.slice(i*nbrOfElements,(i+1)*nbrOfElements) )
    }
  }
  case Result(voice_num,chords) => {
	nbrofResults = nbrofResults.updated(voice_num,nbrofResults(voice_num)+1)
	Results = Results.updated(voice_num,Results(voice_num):::chords)
	if(nbrOfResults(voice_num) == nbrOfMessages)
		PlayerActor ! PlayVoice(voice_num,Results(voice_num) )
  }
 }
}




}
