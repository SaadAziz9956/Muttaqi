import SwiftUI

struct JournalListView: View {
    @State private var viewModel: JournalListViewModel
    @State private var titleBottom: CGFloat = .infinity
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss

    private static let rowInsets = EdgeInsets(top: 0, leading: 24, bottom: 0, trailing: 24)

    init(viewModel: JournalListViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        // Filtered once per update rather than once per row
        let entries = viewModel.entries

        List {
            if !viewModel.isSearching {
                header
                    .listRowInsets(Self.rowInsets)
                    .listRowSeparator(.hidden)
            }

            ForEach(entries) { entry in
                Button {
                    router.pushHome(.journalEntry(entry))
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
        .searchable(text: $viewModel.query, prompt: "Search")
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
                    router.pushHome(.journalEntry(viewModel.newEntry()))
                } label: {
                    Image("add-linear")
                        .resizable()
                        .frame(width: 24, height: 24)
                        .foregroundStyle(.brandTeal)
                }
                .accessibilityLabel("New entry")
            }
        }
        .task { await viewModel.load() }
    }

    private var header: some View {
        VStack(spacing: 0) {
            Text("Journal")
                .font(.custom("ReemKufi-Regular", size: 28))
                .foregroundStyle(.appPrimary)
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                .padding(.top, 24)

            if let quote = viewModel.quote, let translation = quote.ayah.translation {
                // 68:1's sentence runs on into 68:2, so its translation ends in a comma
                let excerpt = translation.trimmingCharacters(in: CharacterSet(charactersIn: ",;").union(.whitespaces))
                Text(excerpt.quoted)
                    .font(TranslationStyle(for: translation, size: 14).font)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 16)

                Text("Quran (\(quote.reference))")
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 4)
            }

            if viewModel.hasEntries {
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
        if viewModel.isSearching, viewModel.entries.isEmpty {
            ContentUnavailableView.search(text: viewModel.query)
        } else if viewModel.hasLoaded, !viewModel.hasEntries {
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
            viewModel.delete(entry)
        } label: {
            Label("Delete", image: "trash-linear")
        }
    }
}
