package mihon.core.migration.migrations

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import eu.kanade.domain.source.service.SourcePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import logcat.LogPriority
import mihon.core.migration.Migration
import mihon.core.migration.MigrationContext
import mihon.domain.extension.repository.ExtensionStoreRepository
import tachiyomi.core.common.util.system.logcat

/**
 * Registers the Keiyoushi extension store on first launch.
 * Runs on every start until the store has been added once, so a first launch without
 * network access is retried next time. After that, the user can remove it like any other store.
 */
@Inject
@ContributesIntoSet(AppScope::class)
class DefaultExtensionStoreMigration(
    private val sourcePreferences: SourcePreferences,
    private val repository: ExtensionStoreRepository,
) : Migration {
    override val version: Float = Migration.ALWAYS

    override suspend fun invoke(migrationContext: MigrationContext): Boolean = withContext(Dispatchers.IO) {
        val seeded = sourcePreferences.defaultStoreSeeded
        if (seeded.get()) return@withContext true

        repository.insert(KEIYOUSHI_INDEX_URL)
            .onSuccess { seeded.set(true) }
            .onFailure { logcat(LogPriority.WARN, it) { "Could not add default extension store yet" } }
        true
    }

    private companion object {
        const val KEIYOUSHI_INDEX_URL = "https://github.com/keiyoushi/extensions/raw/repo/index.pb"
    }
}
