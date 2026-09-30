import Shared
import SwiftUI

struct SurahDetailView: View {
    /// The surah opened, shown in the header until the reader has loaded
    private let surah: Surah
    @State private var screen: SharedViewModel<SurahReaderViewModel, SurahReaderState>
    @State private var readingPosition: SharedValue<String>
    // Made with the reader, so a translation still downloading when the sheet closes carries on, and the explanation
    // loaded once is there straight away when it's opened again
    @State private var settings = SharedViewModel(QuranViewModels.shared.settings()) { $0.state }
    @State private var tafsir = SharedViewModel(QuranViewModels.shared.tafsir()) { $0.state }
    @State private var showSettings = false
    /// The explanation being shown, if any. An item rather than a flag, so the sheet always opens at the ayah just
    /// asked for
    @State private var tafsirRequest: TafsirRequest?
    @State private var contentWidth: CGFloat = 0
    @State private var titleBottom: CGFloat = .infinity
    @Environment(\.dismiss) private var dismiss
    @Environment(AppRouter.self) private var router

    /// `startAyah` is the ayah (number within the surah) to open at, e.g. when continuing where the reader left off
    init(surah: Surah, startAyah: Int? = nil) {
        self.surah = surah
        let viewModel = QuranViewModels.shared.reader(surahNumber: surah.number, startAyah: Int32(startAyah ?? 0))
        _screen = State(initialValue: SharedViewModel(viewModel) { $0.state })
        _readingPosition = State(initialValue: SharedValue(viewModel.readingPosition))
    }

    private var state: SurahReaderState { screen.state }
    private var headerSurah: Surah { state.headerSurah ?? surah }

    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 0) {
                        SurahHeaderView(
                            surah: headerSurah,
                            previousSurah: state.previousSurah,
                            nextSurah: state.nextSurah,
                            onPrevious: { dispatch(SurahReaderIntentPreviousTapped.shared) },
                            onNext: { dispatch(SurahReaderIntentNextTapped.shared) },
                            onExplanation: { dispatch(SurahReaderIntentExplanationTapped.shared) },
                            onTitleBottomChange: { titleBottom = $0 }
                        )

                        contentView
                    }
                    .scrollTargetLayout()
                    .padding(.horizontal, 16)
                }
                // Ayahs (or Mushaf pages) at least a fifth on screen count as read
                .onScrollTargetVisibilityChange(idType: Int32.self, threshold: 0.2) { visibleIDs in
                    dispatch(SurahReaderIntentVisibleAyahsChanged(ids: visibleIDs.map { KotlinInt(value: $0) }))
                }
                .task(id: startScrollTarget) {
                    guard let target = startScrollTarget else { return }
                    // Ayah cards are laid out lazily, so the first jump uses estimated heights and can land an ayah
                    // off; once the cards around the target are laid out, a second jump lands exactly
                    proxy.scrollTo(target, anchor: .top)
                    try? await Task.sleep(for: .milliseconds(300))
                    proxy.scrollTo(target, anchor: .top)
                    try? await Task.sleep(for: .milliseconds(300))
                    dispatch(SurahReaderIntentReachedStart.shared)
                }
            }
            .id(state.surahNumber)
            .transition(SurahSlideTransition(screen: screen, width: contentWidth))
        }
        // The next or previous surah slides in from the side of the arrow that was tapped
        .animation(.easeInOut(duration: 0.35), value: state.surahNumber)
        .background { SoftBackdrop() }
        .overlay(alignment: .bottom) {
            ReadingPositionPill(position: readingPosition)
                .padding(.bottom, 8)
        }
        .onDisappear {
            dispatch(SurahReaderIntentLeft.shared)
        }
        .onGeometryChange(for: CGFloat.self) { $0.size.width } action: { contentWidth = $0 }
        .collapsingBarTitle(headerSurah.englishName, titleBottom: titleBottom)
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
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { showSettings.toggle() } label: {
                    Image("setting-4-linear")
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.textPrimary)
                }
            }
        }
        .sheet(isPresented: $showSettings) {
            ReadingSettingsSheet(screen: settings)
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
        }
        .sheet(item: $tafsirRequest) { request in
            TafsirView(surah: headerSurah, screen: tafsir, startAyah: request.startAyah)
                .presentationDetents([.large])
                .presentationDragIndicator(.visible)
        }
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openTafsir(let open):
                    tafsirRequest = TafsirRequest(startAyah: open.startAyah?.int32Value)
                case .openShare(let share):
                    router.push(share.passage)
                case .copy(let copy):
                    UIPasteboard.general.string = copy.text
                }
            }
        }
    }

    private func dispatch(_ intent: SurahReaderIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    /// Scroll ID for the ayah the reader is continuing from: the ayah itself, or its Mushaf page in Arabic Only mode
    private var startScrollTarget: Int32? {
        state.startScrollTarget?.int32Value
    }

    @ViewBuilder
    private var contentView: some View {
        switch onEnum(of: state.content) {
        case .loading:
            ProgressView()
                .tint(.appPrimary)
                .padding(.top, 100)
        case .loaded(let loaded):
            loadedContent(loaded.reading)
        case .failed(let failed):
            errorView(message: failed.message, suggestion: failed.suggestion)
        }
    }

    @ViewBuilder
    private func loadedContent(_ reading: SurahReading) -> some View {
        if reading.showsBismillah {
            BismillahView(
                text: reading.bismillahText,
                translation: reading.bismillahTranslation
            )
        }

        switch state.settings.mode {
        case .withTranslation:
            ForEach(reading.displayAyahs, id: \.number) { ayah in
                AyahCardView(
                    ayah: ayah,
                    fontSize: state.settings.fontSize,
                    language: state.settings.language,
                    onExplanation: { dispatch(SurahReaderIntentAyahExplanationTapped(numberInSurah: ayah.numberInSurah)) },
                    onCopy: { dispatch(SurahReaderIntentCopyTapped(ayahNumber: ayah.number)) },
                    onShare: { dispatch(SurahReaderIntentShareTapped(ayahNumber: ayah.number)) }
                )
            }
        case .arabicOnly:
            ArabicOnlyView(
                pages: reading.pages,
                fontSize: state.settings.fontSize
            )
            .padding(.top, 16)
        }

        SurahEndNavigationView(
            previousSurah: reading.previousSurah,
            nextSurah: reading.nextSurah,
            onPrevious: { dispatch(SurahReaderIntentPreviousTapped.shared) },
            onNext: { dispatch(SurahReaderIntentNextTapped.shared) }
        )
        .padding(.top, 24)
        // Room for the reading position pill, so it never covers the last cards
        .padding(.bottom, 72)
    }

    private func errorView(message: String, suggestion: String) -> some View {
        VStack(spacing: 16) {
            Text("Failed to load")
                .font(.titleMedium)
                .foregroundStyle(.textPrimary)
            Text(message)
                .font(.bodySmall)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Text(suggestion)
                .font(.caption)
                .foregroundStyle(.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
            Button {
                dispatch(SurahReaderIntentRetry.shared)
            } label: {
                Text("Retry")
                    .font(.titleSmall)
                    .foregroundStyle(.onPrimary)
                    .frame(width: 120, height: 40)
                    .background(.appPrimary)
                    .clipShape(Capsule())
            }
        }
        .padding(.top, 100)
    }
}

/// The explanation to open: at the passage covering `startAyah`, or at the start when it's nil
private struct TafsirRequest: Identifiable {
    let id = UUID()
    let startAyah: Int32?
}

// Slides the surah in from, and out toward, the side of the arrow that was tapped.
// The direction is read from the reader's state as the transition runs rather than captured: SwiftUI animates an
// outgoing view with the transition from its last render, which still holds the old direction when the user switches
// arrows.
private struct SurahSlideTransition: Transition {
    let screen: SharedViewModel<SurahReaderViewModel, SurahReaderState>
    let width: CGFloat

    func body(content: Content, phase: TransitionPhase) -> some View {
        // Forward: the new surah enters from the trailing edge and the old one leaves by the leading edge
        let sign: CGFloat = screen.state.direction == .forward ? 1 : -1
        content.offset(x: -phase.value * sign * width)
    }
}

/// Where the reader is, floating at the foot of the screen. It reads the position in its own body, so scrolling
/// redraws only the pill, not the ayahs.
private struct ReadingPositionPill: View {
    let position: SharedValue<String>

    var body: some View {
        if let position = position.value {
            Text(position)
                .font(.custom("ReemKufi-Medium", size: 13))
                .foregroundStyle(.appPrimary)
                .contentTransition(.numericText())
                .animation(.snappy, value: position)
                .padding(.horizontal, 16)
                .frame(height: 36)
                // Glass, since it floats over the ayahs as they scroll beneath it
                .glassEffect(.regular, in: .capsule)
                .transition(.opacity)
        }
    }
}
