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
class DiaryPersonCensor @Inject constructor(
    @DebuggerScope debugScope: CoroutineScope,
    diary: ContactDiaryRepository
) : BugCensor {

    private val persons by lazy {
        diary.people.stateIn(
            scope = debugScope,
            started = SharingStarted.Lazily,
            initialValue = null
        ).filterNotNull()
    }

    override suspend fun checkLog(entry: LogLine): LogLine? {
        val personsNow = persons.first()

        if (personsNow.isEmpty()) return null

        val newMessage = personsNow.fold(entry.message) { orig, person ->
            var wip = orig.replace(person.fullName, "Person#${person.personId}/Name")

            person.emailAddress?.let {
                wip = wip.replace(it, "Person#${person.personId}/EMail")
            }

            person.phoneNumber?.let {
                wip = wip.replace(it, "Person#${person.personId}/PhoneNumber")
            }

            wip
        }

        return entry.copy(message = newMessage)
    }
}
