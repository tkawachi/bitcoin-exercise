package com.github.tkawachi.bitcoin

case class Host(string: String)

object Host {
  val seed: Set[Host] = Set(
    "seed.bitcoin.sipa.be",
    "dnsseed.bluematt.me",
    "dnsseed.bitcoin.dashjr.org",
    "seed.bitcoinstats.com",
    "bitseed.xf2.org",
    "seed.bitcoin.jonasschnelli.ch"
  ).map(apply)
}
