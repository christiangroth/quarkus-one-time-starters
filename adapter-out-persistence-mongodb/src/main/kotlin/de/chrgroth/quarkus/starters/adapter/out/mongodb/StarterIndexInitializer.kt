package de.chrgroth.quarkus.starters.adapter.out.mongodb

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.IndexModel
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import io.quarkus.runtime.StartupEvent
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import mu.KLogging

@ApplicationScoped
@Suppress("Unused", "UnusedParameter")
class StarterIndexInitializer(
  private val repository: StarterDocumentRepository,
) {

  private val desiredIndexes: List<IndexModel> = listOf(
    IndexModel(Indexes.ascending("lastStatus"), IndexOptions().name("idx_lastStatus")),
  )

  init {
    require(desiredIndexes.all { it.options.name != null }) {
      "All desired index models must have an explicit name set"
    }
  }

  fun onStart(@Observes event: StartupEvent) {
    syncIndexes(repository.mongoCollection())
  }

  private fun syncIndexes(collection: MongoCollection<StarterDocument>) {
    val existingNames = collection.listIndexes()
      .mapNotNull { it.getString("name") }
      .filter { it != "_id_" }
      .toSet()

    val desiredNames = desiredIndexes.mapNotNull { it.options.name }.toSet()

    val obsoleteNames = existingNames - desiredNames
    for (name in obsoleteNames) {
      logger.info { "Dropping obsolete index '$name' from starters collection" }
      collection.dropIndex(name)
    }

    val currentNames = existingNames - obsoleteNames
    val toCreate = desiredIndexes.filter { it.options.name!! !in currentNames }
    if (toCreate.isNotEmpty()) {
      logger.info { "Creating ${toCreate.size} index(es) on starters collection" }
      collection.createIndexes(toCreate)
    }
  }

  companion object : KLogging()
}
