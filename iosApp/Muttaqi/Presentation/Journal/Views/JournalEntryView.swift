import SwiftUI

struct JournalEntryView: View {
    private enum Field {
        case title, body
    }

    @State private var viewModel: JournalEntryViewModel
    @State private var isConfirmingDelete = false
    @FocusState private var focus: Field?
    @Environment(\.dismiss) private var dismiss
    @Environment(\.scenePhase) private var scenePhase

    init(viewModel: JournalEntryViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                Text(viewModel.createdAt.formatted(Self.dateFormat))
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 12)

                TextField("Title", text: $viewModel.title, prompt: Text("Title").foregroundStyle(Color.appPrimary.opacity(0.35)), axis: .vertical)
                    .font(.custom("ReemKufi-Regular", size: 34, relativeTo: .largeTitle))
                    .foregroundStyle(.appPrimary)
                    .focused($focus, equals: .title)
                    .submitLabel(.next)
                    .padding(.top, 16)
                    // In the title, Return moves on to the body rather than adding a line
                    .onSubmit { focus = .body }
                    .onChange(of: viewModel.title) { _, title in
                        guard title.contains(where: \.isNewline) else { return }
                        viewModel.title = title.filter { !$0.isNewline }
                        // After UIKit finishes handling the key, or the focus change is lost
                        Task { focus = .body }
                    }

                TextField("Body", text: $viewModel.body, prompt: Text("Body").foregroundStyle(Color.textSecondary), axis: .vertical)
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
            guard viewModel.startedEmpty else { return }
            // A focus request made while the screen is still being pushed can be dropped, so it's repeated until
            // the title takes it, for up to a second
            for _ in 0..<10 where focus == nil {
                focus = .title
                try? await Task.sleep(for: .milliseconds(100))
            }
        }
        .onChange(of: focus) { _, focus in
            if focus == nil { viewModel.saveNow() }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase != .active { viewModel.saveNow() }
        }
        .onDisappear {
            viewModel.saveNow()
        }
    }

    private var deleteButton: some View {
        Button {
            // Nothing written yet, so there's nothing to lose
            if viewModel.isEmpty {
                viewModel.delete()
                dismiss()
            } else {
                isConfirmingDelete = true
            }
        } label: {
            Image("trash-linear")
                .resizable()
                .frame(width: 22, height: 22)
                .foregroundStyle(.textPrimary)
        }
        .accessibilityLabel("Delete entry")
        .confirmationDialog("Delete this entry?", isPresented: $isConfirmingDelete, titleVisibility: .visible) {
            Button("Delete Entry", role: .destructive) {
                viewModel.delete()
                dismiss()
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
