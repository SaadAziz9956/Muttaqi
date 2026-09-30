package com.muttaqi.shared.testing

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.SelectedLanguage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Shared test doubles for every feature's tests */

class FakeSelectedLanguage(initial: Language = Language.English) : SelectedLanguage {
    private val flow = MutableStateFlow(initial)
    override val current: Language get() = flow.value
    override val changes: Flow<Language> = flow
    fun switchTo(language: Language) { flow.value = language }
}

class FakeContentSource(private val files: Map<String, String>) : BundledContentSource {
    override fun read(fileName: String): String = files[fileName] ?: error("No $fileName in the fake")
}

class TestDispatchers(dispatcher: CoroutineDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val io = dispatcher
    override val default = dispatcher
}
