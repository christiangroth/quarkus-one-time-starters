package de.chrgroth.quarkus.starters.adapter.out.mongodb

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonId
import java.time.Instant

@MongoEntity(collection = "starters")
class StarterDocument {

  @BsonId
  lateinit var starterId: String
  lateinit var lastStatus: String
  var executions: List<StarterExecutionDocument> = emptyList()
}

class StarterExecutionDocument {
  lateinit var startedAt: Instant
  var finishedAt: Instant? = null
  lateinit var status: String
  var errorMessage: String? = null
}
