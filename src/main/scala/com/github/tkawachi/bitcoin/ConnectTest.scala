package com.github.tkawachi.bitcoin

import java.net._

import com.typesafe.scalalogging.LazyLogging
import io.github.yzernik.bitcoinscodec.messages._
import io.github.yzernik.bitcoinscodec.structures.{ Message, NetworkAddress }
import scodec.DecodeResult
import scodec.bits.BitVector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scala.util.Random
import scala.util.control.NonFatal

object ConnectTest extends App with LazyLogging {

  val codec = Message.codec(0xD9B4BEF9L, 60002) // on the main network, using version 60002.

  def lookupHost(host: Host): Future[Set[InetAddress]] =
    Future(InetAddress.getAllByName(host.string).toSet)
      .recover {
        case _: UnknownHostException => Set()
      }

  def lookupHosts(hosts: Set[Host]): Future[Set[InetAddress]] =
    Future.sequence(hosts.map(lookupHost)).map(_.flatten)

  def versionMessage(): Future[Version] = Future {
    val local = InetAddress.getLocalHost

    Version(
      60002,
      1,
      System.currentTimeMillis() / 1000,
      NetworkAddress(1, new InetSocketAddress(local, 8333)),
      NetworkAddress(1, new InetSocketAddress(local, 8333)),
      Random.nextLong(),
      "/Connect test:0.0.0/",
      //        212672,
      0,
      false
    )
  }

  def readVersion(address: InetSocketAddress, myVersion: Version): Future[Option[Version]] = Future {
    val sock = new Socket()
    logger.debug(s"Connecting $address")

    try {
      sock.connect(address, 3000)
      sock.setSoTimeout(3000)
      val is = sock.getInputStream
      val os = sock.getOutputStream

      codec.encode(myVersion).toOption.flatMap { bv =>
        os.write(bv.toByteArray)

        val buf = new Array[Byte](1024)
        val len = is.read(buf)
        // 最初で読めなかったら諦め
        if (len > 0) {
          codec.decode(BitVector(buf.take(len))).toOption.flatMap {
            case DecodeResult(v @ Version(_, _, _, _, _, _, _, _, _), _) => Some(v)
            case _ => None
          }
        } else None
      }
    } finally {
      try sock.close() catch {
        case NonFatal(_) => // ignore
      }
      logger.debug(s"Closed $address")
    }
  }.recover {
    case e: SocketException =>
      logger.debug(s"Exception about $address, ${e.getMessage}")
      None
    case _: SocketTimeoutException =>
      logger.debug(s"Socket timeout $address")
      None
  }

  val versions = for {
    addrs <- lookupHosts(Host.seed)
    myVersion <- versionMessage()
    vs <- Future.sequence(
      addrs.map(a => readVersion(new InetSocketAddress(a, 8333), myVersion))
    ).map(_.flatten)
  } yield vs

  val theResult: Set[Version] = Await.result(versions, Duration.Inf)
  theResult.foreach(v => logger.info(s"$v"))
  val countByUA = theResult.groupBy(_.user_agent).mapValues(_.size)
  countByUA.toSeq.sortBy(-_._2).foreach { case (ua, c) => println(s"$ua\t$c") }
}
