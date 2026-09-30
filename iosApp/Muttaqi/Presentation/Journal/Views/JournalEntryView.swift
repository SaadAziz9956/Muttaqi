import Shared
import SwiftUI

/// One entry, or a new one when `entryId` is nil. There's no Save button: the view model saves a moment after typing
/// stops, and straight away when the reader leaves, puts the keyboard away or leaves the app
struct JournalEntryView: View {
    private enum Field {
        case title, body
    }

    @State private var screen: SharedViewModel<JournalEntryViewModel, JournalEntryState>
    @FocusState private var focus: Field?
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase

    init(entryId: String?) {
        _screen = State(initialValue: SharedViewModel(JournalViewModels.shared.entry(id: entryId)) { $0.state })
    }

    private var state: JournalEntryState { screen.state }

    /// The view model's state as it is this instant. `screen.state` follows it a moment later, so a text field bound
    /// to that could be redrawn with an older value between two fast keystrokes and lose the second
    private var current: JournalEntryState { screen.viewModel.state.value }

    var body: some View {
        ScrollView {
            // An existing entry is read from the database first, which takes a moment; a new one is ready at once
            if let entry = state.entry {
                VStack(alignment: .leading, spacing: 0) {
                    Text(entry.createdDate.formatted(Self.dateFormat))
                        .font(.labelSmall)
                        .foregroundStyle(.textSecondary)
                        .padding(.top, 12)

                    TextField("Title", text: title, prompt: Text("Title").foregroundStyle(Color.appPrimary.opacity(0.35)), axis: .vertical)
                        .font(.custom("ReemKufi-Regular", size: 34, relativeTo: .largeTitle))
                        .foregroundStyle(.appPrimary)
                        .focused($focus, equals: .title)
                        .submitLabel(.next)
                        .padding(.top, 16)
                        // In the title, Return moves on to the body rather than adding a line
                        .onSubmit { focus = .body }

                    TextField("Body", text: Binding(get: { current.body }, set: { dispatch(JournalEntryIntentBodyChanged(body: $0)) }), prompt: Text("Body").foregroundStyle(Color.textSecondary), axis: .vertical)
                        .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .body))
                        .foregroundStyle(.textPrimary)
                        .lineSpacing(4)
                        .focused($focus, equals: .body)
                        .padding(.top, 20)

                    // Tapping below the text carries on writing, as on a page
                    Color.clear
                        .frame(maxWidth: .infinity, minHeight: 240)
                        .contentShape(.rect)
                        .onTapGesture { focus = .body }
                        .accessibilityHidden(true)
                }
                .padding(.horizontal, 20)
            }
        }
        .scrollDismissesKeyboard(.interactively)
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .background(SwipeBackEnabler())
        .toolbar(.hidden, for: .tabBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button { dismiss() } label: {
                    Image("arrow-left-02-linear")
                        .resizable()
                        .frame(width: 24, height: 24)
                        .foregroundStyle(.textPrimary)
                }
                .accessibilityLabel("Back")
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                if focus != nil {
                    Button("Done") { focus = nil }
                        .font(.custom("ReemKufi-Medium", size: 16))
                } else {
                    deleteButton
                }
            }
        }
        .task {
            guard state.startedEmpty else { return }
            // A focus request made while the screen is still being pushed can be dropped, so it's repeated until
            // the title takes it, for up to a second
            for _ in 0..<10 where focus == nil {
                focus = .title
                try? await Task.sleep(for: .milliseconds(100))
            }
        }
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .close: dismiss()
                }
            }
        }
        .onChange(of: focus) { _, focus in
            if focus == nil { dispatch(JournalEntryIntentSaveNow.shared) }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active { dispatch(JournalEntryIntentSaveNow.shared) }
        }
        .onDisappear {
            dispatch(JournalEntryIntentSaveNow.shared)
        }
    }

    private func dispatch(_ intent: JournalEntryIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    /// The view model keeps a title to one line; Return, typed as a line break, moves on to the body
    private var title: Binding<String> {
        Binding(get: { current.title }, set: { title in
            dispatch(JournalEntryIntentTitleChanged(title: title))
            // After UIKit finishes handling the key, or the focus change is lost
            if title.contains(where: \.isNewline) { Task { focus = .body } }
        })
    }

    private var deleteButton: some View {
        // Nothing written yet: the view model deletes and closes without asking
        Button {
            dispatch(JournalEntryIntentDeleteTapped.shared)
        } label: {
            Image("trash-linear")
                .resizable()
                .frame(width: 22, height: 22)
                .foregroundStyle(.textPrimary)
        }
        .accessibilityLabel("Delete entry")
        .confirmationDialog(
            "Delete this entry?",
            isPresented: Binding(get: { state.isConfirmingDelete }, set: { if !$0 { dispatch(JournalEntryIntentDeleteCancelled.shared) } }),
            titleVisibility: .visible
        ) {
            Button("Delete Entry", role: .destructive) {
                dispatch(JournalEntryIntentDeleteConfirmed.shared)
            }
        } message: {
            Text("This can't be undone.")
        }
    }

    /// "02 - May - 2023", as in the design
    private static let dateFormat = Date.VerbatimFormatStyle(
        format: "\(day: .twoDigits) - \(month: .abbreviated) - \(year: .defaultDigits)",
        locale: .autoupdatingCurrent,
        timeZone: .autoupdatingCurrent,
        calendar: .autoupdatingCurrent
    )
}
