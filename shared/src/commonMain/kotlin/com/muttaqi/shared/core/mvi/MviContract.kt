package com.muttaqi.shared.core.mvi

/**
 * The building blocks of a screen, in one direction:
 * the user's [UiIntent] → the view model runs use cases → each result is a [UiMutation] → a pure [Reducer] folds it
 * into the next [UiState], which the screen draws. One-off events that aren't state, like opening a page, are
 * [UiEffect]s.
 */

/** Everything a screen shows at one moment. Immutable: a change is a new value */
interface UiState

/** Something the user did, or the screen asked for, e.g. typing a search or opening a topic */
interface UiIntent

/** One change to the state, produced from an intent's work and applied by the reducer */
interface UiMutation

/** Something that happens once rather than being shown, e.g. navigating or showing a message */
interface UiEffect
