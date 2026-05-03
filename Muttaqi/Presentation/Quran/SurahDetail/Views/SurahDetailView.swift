import SwiftUI

struct SurahDetailView: View {
    @State private var coordinator: SurahDetailCoordinator
    @Environment(\.dismiss) private var dismiss

    init(coordinator: SurahDetailCoordinator) {
        self._coordinator = State(initialValue: coordinator)
    }

    var body: some View {
        VStack(spacing: 0) {
            SurahHeaderView(
                surah: coordinator.headerSurah,
                canGoNext: coordinator.navigator.canGoNext,
                canGoPrevious: coordinator.navigator.canGoPrevious,
                onPrevious: {
                    let moved = withAnimation(.easeInOut(duration: 0.35)) { coordinator.goPrevious() }
                    if moved { Task { await coordinator.loadCurrentSurah() } }
                },
                onNext: {
                    let moved = withAnimation(.easeInOut(duration: 0.35)) { coordinator.goNext() }
                    if moved { Task { await coordinator.loadCurrentSurah() } }
                }
            )

            ScrollView {
                LazyVStack(spacing: 0) {
                    contentView
                }
                .padding(.horizontal, 16)
            }
            .id(coordinator.navigator.currentSurah.value)
            .transition(slideTransition)
            .clipped()
        }
        .navigationBarBackButtonHidden(true)
        .background(SwipeBackEnabler())
        .toolbar(.hidden, for: .tabBar)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button { dismiss() } label: {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundStyle(.textPrimary)
                }
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { coordinator.toggleSettings() } label: {
                    Image("ic_settings")
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.textPrimary)
                }
            }
        }
        .sheet(isPresented: $coordinator.showSettings) {
            ReadingSettingsSheet(
                settingsViewModel: coordinator.settingsViewModel,
                onLanguageSelected: { language in
                    Task { await coordinator.selectLanguage(language) }
                }
            )
            .presentationDetents([.medium])
            .presentationDragIndicator(.visible)
        }
        .task {
            await coordinator.onAppear()
        }
    }

    @ViewBuilder
    private var contentView: some View {
        switch coordinator.contentViewModel.state {
        case .idle:
            EmptyView()
        case .loading:
            ProgressView()
                .tint(.appPrimary)
                .padding(.top, 100)
        case .loaded(let content):
            loadedContent(content)
        case .error(let error):
            errorView(error)
        }
    }

    @ViewBuilder
    private func loadedContent(_ content: SurahContentViewModel.SurahContent) -> some View {
        if content.showBismillah {
            BismillahView(
                text: content.bismillahText,
                translation: content.bismillahTranslation
            )
        }

        switch coordinator.settingsViewModel.readingMode {
        case .withTranslation:
            ForEach(content.displayAyahs) { ayah in
                AyahCardView(
                    ayah: ayah,
                    fontSize: coordinator.settingsViewModel.fontSize
                )
            }
        case .arabicOnly:
            ArabicOnlyView(
                ayahs: content.displayAyahs,
                fontSize: coordinator.settingsViewModel.fontSize
            )
            .padding(.top, 16)
            .padding(.bottom, 32)
        }
    }

    private var content: SurahContentViewModel.SurahContent? {
        if case .loaded(let content) = coordinator.contentViewModel.state {
            return content
        }
        return nil
    }

    private var slideTransition: AnyTransition {
        switch coordinator.navigator.navigationDirection {
        case .forward:
            return .asymmetric(
                insertion: .move(edge: .trailing),
                removal: .move(edge: .leading)
            )
        case .backward:
            return .asymmetric(
                insertion: .move(edge: .leading),
                removal: .move(edge: .trailing)
            )
        }
    }

    private func errorView(_ error: SurahDetailError) -> some View {
        VStack(spacing: 16) {
            Text("Failed to load")
                .font(.titleMedium)
                .foregroundStyle(.textPrimary)
            Text(error.localizedDescription)
                .font(.bodySmall)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            if let suggestion = error.recoverySuggestion {
                Text(suggestion)
                    .font(.caption)
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
            Button {
                Task { await coordinator.retry() }
            } label: {
                Text("Retry")
                    .font(.titleSmall)
                    .foregroundStyle(.white)
                    .frame(width: 120, height: 40)
                    .background(.appPrimary)
                    .clipShape(Capsule())
            }
        }
        .padding(.top, 100)
    }
}

// Re-enables the swipe-back gesture that navigationBarBackButtonHidden(true) disables.
private struct SwipeBackEnabler: UIViewRepresentable {
    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        DispatchQueue.main.async {
            uiView.next(ofType: UINavigationController.self)?
                .interactivePopGestureRecognizer?.isEnabled = true
        }
    }
}

private extension UIResponder {
    func next<T>(ofType type: T.Type) -> T? {
        guard let next else { return nil }
        return (next as? T) ?? next.next(ofType: type)
    }
}
