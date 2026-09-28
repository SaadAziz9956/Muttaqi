import SwiftUI

struct NamesSearchView: View {
    @Bindable var viewModel: NamesViewModel
    let onSelect: (AllahName) -> Void
    @FocusState private var isFieldFocused: Bool
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Text("Search")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.appPrimary)
                    .padding(.top, 8)

                TextField("Type here", text: $viewModel.query)
                    .font(.custom("ReemKufi-Regular", size: 16, relativeTo: .body))
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .keyboardType(viewModel.searchMode == .number ? .numberPad : .default)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .submitLabel(.search)
                    .focused($isFieldFocused)
                    .frame(height: 50)
                    .background(.textField, in: .rect(cornerRadius: 12))
                    .padding(.top, 36)

                Picker("Search by", selection: $viewModel.searchMode) {
                    Text("by Number").tag(NamesViewModel.SearchMode.number)
                    Text("by Name (eng)").tag(NamesViewModel.SearchMode.name)
                }
                .pickerStyle(.segmented)
                .padding(.top, 20)
                // A new mode needs its own keyboard, e.g. the number pad, which only appears on refocusing
                .onChange(of: viewModel.searchMode) {
                    viewModel.query = ""
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
        .onDisappear { viewModel.query = "" }
    }

    @ViewBuilder
    private var results: some View {
        let results = viewModel.results
        if viewModel.isSearching, results.isEmpty {
            ContentUnavailableView.search(text: viewModel.query)
        } else {
            LazyVStack(spacing: 16) {
                ForEach(results) { name in
                    Button {
                        onSelect(name)
                    } label: {
                        NameCard(name: name)
                    }
                    .buttonStyle(.plain)
                    .accessibilityHint("Shows this name on the 99 Names page")
                }
            }
        }
    }
}
