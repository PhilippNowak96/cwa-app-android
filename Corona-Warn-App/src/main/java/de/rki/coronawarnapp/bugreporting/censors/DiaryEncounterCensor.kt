package de.rki.coronawarnapp.bugreporting.censors

import dagger.Reusable
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import de.rki.coronawarnapp.bugreporting.debuglog.internal.DebuggerScope
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Reusable
class DiaryEncounterCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    private val encounters by lazy {
        diary.personEncounters.stateIn(
            scope = debugScope,
            started = SharingStarted.Lazily,
            initialValue = null
        ).filterNotNull()
    }

    override suspend fun checkLog(entry: LogLine): LogLine? {
        val encountersNow = encounters.first().filter { !it.circumstances.isNullOrBlank() }

        if (encountersNow.isEmpty()) return null

        val newMessage = encountersNow.fold(entry.message) { orig, encounter ->
            if (encounter.circumstances.isNullOrBlank()) return@fold orig

            orig.replace(encounter.circumstances!!, "Encounter#${encounter.id}/Circumstances")
        }

        return entry.copy(message = newMessage)
    }
}
