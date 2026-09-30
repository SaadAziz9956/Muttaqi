import Shared
import SwiftUI

struct JournalListView: View {
    @State private var screen = SharedViewModel(JournalViewModels.shared.list()) { $0.state }
    @State private var titleBottom: CGFloat = .infinity
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss

    private static let rowInsets = EdgeInsets(top: 0, leading: 24, bottom: 0, trailing: 24)

    private var state: JournalListState { screen.state }

    var body: some View {
        let entries = state.shownEntries

        List {
            if !state.isSearching {
                header
                    .listRowInsets(Self.rowInsets)
                    .listRowSeparator(.hidden)
            }

            ForEach(entries, id: \.id) { entry in
                Button {
                    dispatch(JournalListIntentEntryTapped(entryId: entry.id))
                } label: {
                    JournalEntryRow(entry: entry)
                }
                .listRowInsets(Self.rowInsets)
                // No divider above the first row, which shows while searching, when the header is hidden
                .listRowSeparator(entry.id == entries.first?.id ? .hidden : .automatic, edges: .top)
                // Needs the Delete button tapped, so a long swipe can't lose an entry by accident
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    deleteButton(for: entry)
                        // The app's green tint would otherwise replace the destructive red
                        .tint(.red)
                }
                .contextMenu {
                    deleteButton(for: entry)
                }
            }

            emptyState
                .listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .scrollDismissesKeyboard(.immediately)
        // Read from the view model as it is this instant, so fast typing can't be redrawn with an older query
        .searchable(text: Binding(get: { screen.viewModel.state.value.query }, set: { dispatch(JournalListIntentQueryChanged(query: $0)) }), prompt: "Search")
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("Journal", titleBottom: titleBottom)
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

            // Search and a new-entry button along the bottom, as in Notes
            DefaultToolbarItem(kind: .search, placement: .bottomBar)
            ToolbarSpacer(.fixed, placement: .bottomBar)
            ToolbarItem(placement: .bottomBar) {
                Button {
                    dispatch(JournalListIntentNewEntryTapped.shared)
                } label: {
                    Image("add-linear")
                        .resizable()
                        .frame(width: 24, height: 24)
                        .foregroundStyle(.brandTeal)
                }
                .accessibilityLabel("New entry")
            }
        }
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openEntry(let open): router.pushHome(.journalEntry(id: open.entryId))
                }
            }
        }
    }

    private func dispatch(_ intent: JournalListIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private var header: some View {
        VStack(spacing: 0) {
            Text("Journal")
                .font(.custom("ReemKufi-Regular", size: 28))
                .foregroundStyle(.appPrimary)
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                .padding(.top, 24)

            if let quote = state.header {
                Text(quote.text.quoted)
                    .font(TranslationStyle(for: quote.text, size: 14).font)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 16)

                Text(quote.source)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 4)
            }

            if state.hasEntries {
                Text("Notes")
                    .font(.custom("ReemKufi-Regular", size: 12))
                    .foregroundStyle(.textPrimary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.top, 48)
                    .padding(.bottom, 4)
            }
        }
        .frame(maxWidth: .infinity)
    }

    @ViewBuilder
    private var emptyState: some View {
        if state.isSearching, state.shownEntries.isEmpty {
            ContentUnavailableView.search(text: state.query)
        } else if !state.isLoading, !state.hasEntries {
            ContentUnavailableView {
                Label {
                    Text("No entries yet")
                } icon: {
                    Image("home-journal")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 44, height: 44)
                        .foregroundStyle(.brandTeal)
                }
            } description: {
                Text("Write down what you're grateful for today. Tap + to start.")
            }
            .padding(.top, 24)
        }
    }

    private func deleteButton(for entry: JournalEntry) -> some View {
        Button(role: .destructive) {
            dispatch(JournalListIntentDeleteTapped(entryId: entry.id))
        } label: {
            Label("Delete", image: "trash-linear")
        }
    }
}
