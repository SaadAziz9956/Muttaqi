package com.muttaqi.shared.feature.quran.data.repository

import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.data.local.QuranTextDao
import com.muttaqi.shared.feature.quran.data.local.TafsirDao
import com.muttaqi.shared.feature.quran.data.local.TafsirEntity
import com.muttaqi.shared.feature.quran.data.remote.TafsirApi
import com.muttaqi.shared.feature.quran.data.remote.TafsirSource
import com.muttaqi.shared.feature.quran.domain.model.TafsirEntry
import com.muttaqi.shared.feature.quran.domain.repository.TafsirRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

internal class RoomTafsirRepository(
    private val tafsirDao: TafsirDao,
    private val textDao: QuranTextDao,
    private val api: TafsirApi,
    private val dispatchers: DispatcherProvider,
) : TafsirRepository {

    override suspend fun tafsir(surahNumber: Int, language: Language): Outcome<List<TafsirEntry>> = try {
        val cached = stored(surahNumber, language)
        if (cached.isNotEmpty()) {
            Outcome.Success(cached)
        } else {
            download(surahNumber, language)
            Outcome.Success(stored(surahNumber, language))
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: IOException) {
        Outcome.Failure(DomainError.NoConnection)
    } catch (error: Exception) {
        Outcome.Failure(DomainError.Unexpected(error.message))
    }

    private suspend fun download(surahNumber: Int, language: Language) {
        val source = TafsirSource.forLanguage(language)
        val entries = withContext(dispatchers.io) {
            api.tafsirByChapter(source.id, surahNumber).tafsirs.mapNotNull { dto ->
                val text = dto.text.strippingHtml()
                if (text.isEmpty()) return@mapNotNull null
                TafsirEntity(
                    surahNumber = surahNumber,
                    ayahNumber = dto.verseKey.substringAfterLast(':').toIntOrNull() ?: 0,
                    language = language.code,
                    tafsirSource = source.title,
                    text = text,
                )
            }
        }
        tafsirDao.insert(entries)
    }

    private suspend fun stored(surahNumber: Int, language: Language): List<TafsirEntry> {
        val rows = tafsirDao.tafsir(surahNumber, language.code)
        val last = rows.lastOrNull() ?: return emptyList()
        val surahEnd = textDao.surah(surahNumber)?.numberOfAyahs ?: last.ayahNumber
        return rows.mapIndexed { index, row ->
            val nextStart = rows.getOrNull(index + 1)?.ayahNumber ?: (surahEnd + 1)
            TafsirEntry(
                surahNumber = surahNumber,
                ayahNumber = row.ayahNumber,
                lastAyahNumber = maxOf(row.ayahNumber, nextStart - 1),
                text = row.text,
            )
        }
    }
}

private val lineBreak = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)
private val blockBoundary = Regex("</?(p|div|h[1-6])(\\s[^>]*)?>", RegexOption.IGNORE_CASE)
private val anyTag = Regex("<[^>]+>")
private val blankLines = Regex("[ \\t]*\\n(\\s*\\n)+[ \\t]*")

internal fun String.strippingHtml(): String = this
    .replace(lineBreak, "\n")
    .replace(blockBoundary, "\n\n")
    .replace(anyTag, "")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&nbsp;", " ")
    .replace(blankLines, "\n\n")
    .trim()
