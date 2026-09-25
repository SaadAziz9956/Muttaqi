import SwiftUI

struct SurahDetailView: View {
    @State private var coordinator: SurahDetailCoordinator
    @State private var contentWidth: CGFloat = 0
    @State private var scrollTop: CGFloat = 0
    @State private var showsBarTitle = false
    @Environment(\.dismiss) private var dismiss

    init(coordinator: SurahDetailCoordinator) {
        self._coordinator = State(initialValue: coordinator)
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                LazyVStack(spacing: 0) {
                    SurahHeaderView(
                        surah: coordinator.headerSurah,
                        previousSurah: coordinator.previousSurah,
                        nextSurah: coordinator.nextSurah,
                        onPrevious: goToPreviousSurah,
                        onNext: goToNextSurah,
                        onExplanation: {
                            coordinator.toggleTafsir()
                        },
                        // The bar title takes over once the header's title has scrolled up past the top of the page
                        onTitleBottomChange: { titleBottom in
                            showsBarTitle = titleBottom < scrollTop
                        }
                    )

                    contentView
                }
                .padding(.horizontal, 16)
            }
            .id(coordinator.navigator.currentSurah.value)
            .transition(SurahSlideTransition(navigator: coordinator.navigator, width: contentWidth))
        }
        .onGeometryChange(for: CGRect.self) { $0.frame(in: .global) } action: { frame in
            contentWidth = frame.width
            scrollTop = frame.minY
        }
        .animation(.easeInOut(duration: 0.2), value: showsBarTitle)
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
            ToolbarItem(placement: .principal) {
                Text(coordinator.headerSurah?.englishName ?? "")
                    .font(.titleMedium)
                    .foregroundStyle(.appPrimary)
                    .lineLimit(1)
                    .opacity(showsBarTitle ? 1 : 0)
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
        .sheet(isPresented: $coordinator.showTafsir) {
            TafsirView(
                surah: coordinator.headerSurah,
                viewModel: coordinator.tafsirViewModel,
                language: coordinator.settingsViewModel.selectedLanguage
            )
            .presentationDetents([.large])
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
                    fontSize: coordinator.settingsViewModel.fontSize,
                    language: coordinator.settingsViewModel.selectedLanguage
                )
            }
        case .arabicOnly:
            ArabicOnlyView(
                ayahs: content.displayAyahs,
                fontSize: coordinator.settingsViewModel.fontSize
            )
            .padding(.top, 16)
        }

        SurahEndNavigationView(
            previousSurah: content.previousSurah,
            nextSurah: content.nextSurah,
            onPrevious: goToPreviousSurah,
            onNext: goToNextSurah
        )
        .padding(.top, 24)
        .padding(.bottom, 32)
    }

    private func goToPreviousSurah() {
        let moved = withAnimation(.easeInOut(duration: 0.35)) { coordinator.goPrevious() }
        if moved { Task { await coordinator.loadCurrentSurah() } }
    }

    private func goToNextSurah() {
        let moved = withAnimation(.easeInOut(duration: 0.35)) { coordinator.goNext() }
        if moved { Task { await coordinator.loadCurrentSurah() } }
    }

    private var content: SurahContentViewModel.SurahContent? {
        if case .loaded(let content) = coordinator.contentViewModel.state {
            return content
        }
        return nil
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

// Slides the surah in from, and out toward, the side of the arrow that was tapped.
// The direction is read from the navigator as the transition runs rather than captured: SwiftUI animates an outgoing
// view with the transition from its last render, which still holds the old direction when the user switches arrows.
private struct SurahSlideTransition: Transition {
    let navigator: SurahNavigator
    let width: CGFloat

    func body(content: Content, phase: TransitionPhase) -> some View {
        // Forward: the new surah enters from the trailing edge and the old one leaves by the leading edge
        let sign: CGFloat = navigator.navigationDirection == .forward ? 1 : -1
        content.offset(x: -phase.value * sign * width)
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
