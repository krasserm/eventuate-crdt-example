package example.crdt

import akka.actor.ActorSystem

import com.rbmhtechnology.eventuate.{ReplicationConnection, ReplicationEndpoint}
import com.rbmhtechnology.eventuate.crdt.MVRegisterService
import com.rbmhtechnology.eventuate.log.leveldb.LeveldbEventLog
import com.typesafe.config.ConfigFactory

object MVRegisterExample extends App {
  def config(port: Int) =
    s"""
       |akka {
       |  actor.provider = "akka.remote.RemoteActorRefProvider"
       |  remote {
       |    enabled-transports = ["akka.remote.netty.tcp"]
       |    netty.tcp {
       |      hostname = "127.0.0.1"
       |      port = $port
       |    }
       |  }
       |  loglevel = "ERROR"
       |}
       |eventuate {
       |  log.replication.retry-delay = 2s
       |  log.replication.remote-read-timeout = 2s
       |  log.leveldb.dir = target/example/logs
       |  snapshot.filesystem.dir = target/example/snapshots
       |}
    """.stripMargin

  def service(locationId: String, port: Int, connectToPorts: Set[Int]): MVRegisterService[String] = {
    implicit val system: ActorSystem =
      ActorSystem(ReplicationConnection.DefaultRemoteSystemName, ConfigFactory.parseString(config(port)))

    val logName = "L"

    val endpoint = new ReplicationEndpoint(id = locationId, logNames = Set(logName),
      logFactory = logId => LeveldbEventLog.props(logId),
      connections = connectToPorts.map(ReplicationConnection("127.0.0.1", _)))

    endpoint.activate()

    new MVRegisterService[String](s"service-$locationId", endpoint.logs(logName))
  }

  val serviceA = service("A", 2552, Set(2553, 2554)) // at location A
  val serviceB = service("B", 2553, Set(2552, 2554)) // at location B
  val serviceC = service("C", 2554, Set(2552, 2553)) // at location C

  val crdtId = "1"

  import serviceA.system.dispatcher

  serviceA.assign(crdtId, "abc").onSuccess {
    case r => println(s"assign result to replica at location A: $r")
  }

  serviceB.assign(crdtId, "xyz").onSuccess {
    case r => println(s"assign result to replica at location B: $r")
  }

  // wait a bit ...
  Thread.sleep(1000)

  serviceC.value(crdtId).onSuccess {
    case r => println(s"read result from replica at location C: $r")
  }
}
