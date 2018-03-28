package com.pronvis

import java.nio.ByteBuffer

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.pronvis.TestMessage.RequestTime
import org.apache.kafka.clients.producer.ProducerRecord


object Main {

  val exampleValues: List[Long] = List(
    -4791902657223630865l, // OK
    0l, // OK
    500l, // deserialized to 7299055   O_O
    22454353l, // deserialized to -4791902657223630865   O_O
    7299055l, // OK
    -4791902657223630865l, // OK
    (Int.MaxValue - 1).toLong, // fail
    Int.MinValue.toLong, // fail
    Int.MaxValue.toLong, // fail
    Long.MinValue, // fail
    Long.MaxValue // fail
  )


  final val bootstrapServer = "localhost:9092"
  final val topic = "test-topic"

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("protobuf-message-to-kafka")
    val processingDecider: Supervision.Decider = {
      case e: Exception =>
        println("Error!\n" + e.getMessage)
        akka.stream.Supervision.Resume
    }
    implicit val kafkaMaterializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(actorSystem)
      .withSupervisionStrategy(processingDecider))

    val kafkaWriter = new KafkaWriter(bootstrapServer, 1)



    exampleValues.foreach { l =>
      val message = RequestTime(l)
      sendMsgToKafka(message, kafkaWriter)
    }
  }

  private def sendMsgToKafka(msg: RequestTime, kafkaWriter: KafkaWriter): Unit = {
    val messageTime = System.currentTimeMillis()
    val messageTimeBytes = ByteBuffer.allocate(8).putLong(messageTime).array()
    val recordBytes = msg.toByteArray
    val record: ProducerRecord[Array[Byte], Array[Byte]] = new ProducerRecord(topic, messageTimeBytes, recordBytes)

    println(s"Sending protobuf message (which byte representation is [${recordBytes.mkString(",")}]) to kafka")
    kafkaWriter.kafkaQueue.offer(record)
  }



}
