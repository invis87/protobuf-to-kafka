package com.pronvis

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import org.apache.kafka.common.serialization.ByteArraySerializer
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Source, SourceQueueWithComplete}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class KafkaWriter
(bootstrapServers: String, queueSize: Int)
(implicit system: ActorSystem,
  materializer: Materializer) {


  private val producerSettings: ProducerSettings[Array[Byte], Array[Byte]] = ProducerSettings(system, new ByteArraySerializer(), new ByteArraySerializer())
    .withBootstrapServers(bootstrapServers)
    .withProperty("acks", "all")
    .withProperty("retries", "5")
    .withProperty("batch.size", "1000")
    .withProperty("request.timeout.ms", Int.MaxValue.toString)
    .withProperty("max.block.ms", Int.MaxValue.toString)

  private val kafkaProducer: KafkaProducer[Array[Byte], Array[Byte]] = producerSettings.createKafkaProducer()

  val kafkaQueue: SourceQueueWithComplete[ProducerRecord[Array[Byte], Array[Byte]]] =
    Source
      .queue(queueSize, OverflowStrategy.dropHead)
      .to(Producer.plainSink[Array[Byte], Array[Byte]](producerSettings, kafkaProducer))
      .run()(materializer)
}
