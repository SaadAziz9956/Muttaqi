import Shared
import SwiftUI

struct NamesSearchView: View {
    let screen: SharedViewModel<NamesViewModel, NamesState>
    let onSelect: (Int32) -> Void
    @FocusState private var isFieldFocused: Bool
    @Environment(\.dismiss) private var dismiss

    private var state: NamesState { screen.state }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Text("Search")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 8)

                TextField("Type here", text: Binding(get: { state.query }, set: { dispatch(NamesIntentQueryChanged(query: $0)) }))
                    .font(.custom("ReemKufi-Regular", size: 16, relativeTo: .body))
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .keyboardType(state.searchMode == .byNumber ? .numberPad : .default)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .submitLabel(.search)
                    .focused($isFieldFocused)
                    .frame(height: 50)
                    .softGlass(in: Capsule())
                    .padding(.top, 36)

                Picker("Search by", selection: Binding(get: { state.searchMode }, set: { dispatch(NamesIntentSearchModeChanged(mode: $0)) })) {
                    Text("by Number").tag(NameSearchMode.byNumber)
                    Text("by Name (eng)").tag(NameSearchMode.byName)
                }
                .pickerStyle(.segmented)
                .padding(.top, 20)
                .onChange(of: state.searchMode) {
                    isFieldFocused = false
                    Task { isFieldFocused = true }
                }

                results
                    .padding(.top, 32)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 24)
        }
        .scrollDismissesKeyboard(.interactively)
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
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
        }
        .onAppear { isFieldFocused = true }
        .onDisappear { dispatch(NamesIntentClearQuery.shared) }
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .showName(let show): onSelect(show.number)
                case .openShare: break
                }
            }
        }
    }

    private func dispatch(_ intent: NamesIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    @ViewBuilder
    private var results: some View {
        let results = state.results
        if state.isSearching, results.isEmpty {
            ContentUnavailableView.search(text: state.query)
        } else {
            LazyVStack(spacing: 16) {
                ForEach(results, id: \.number) { name in
                    Button {
                        dispatch(NamesIntentResultTapped(number: name.number))
                    } label: {
                        NameCard(name: name)
                    }
                    .buttonStyle(SoftPressStyle())
                    .accessibilityHint("Shows this name on the 99 Names page")
                }
            }
        }
    }
}
