import Shared
import SwiftUI

struct TafsirView: View {
    let surah: Surah?
    let screen: SharedViewModel<TafsirViewModel, TafsirState>
    var startAyah: Int32? = nil

    private var state: TafsirState { screen.state }
    private var isUrdu: Bool { state.language == .urdu }

    private var isSettled: Bool {
        switch onEnum(of: state.status) {
        case .idle, .loading: false
        case .loaded, .failed: true
        }
    }

    var body: some View {
        VStack(spacing: 0) {
            header

            switch onEnum(of: state.status) {
            case .idle, .loading:
                Spacer()
                SoftLoadingIndicator()
                Spacer()

            case .loaded(let loaded) where loaded.entries.isEmpty:
                Spacer()
                Text("No tafseer available for this surah")
                    .font(.bodySmall)
                    .foregroundStyle(.textSecondary)
                Spacer()

            case .loaded(let loaded):
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 0) {
                            ForEach(loaded.entries, id: \.ayahNumber) { entry in
                                ayahRow(entry)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                    .onAppear {
                        guard let startAyah, let entry = state.entryCovering(ayah: startAyah) else { return }
                        proxy.scrollTo(entry.ayahNumber, anchor: .top)
                    }
                }
                .transition(.opacity)

            case .failed(let failed):
                Spacer()
                VStack(spacing: 12) {
                    Text("Failed to load tafseer")
                        .font(.titleSmall)
                        .foregroundStyle(.textPrimary)
                    Text(failed.message)
                        .font(.bodySmall)
                        .foregroundStyle(.textSecondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    Button {
                        screen.viewModel.dispatch(intent: TafsirIntentRetry.shared)
                    } label: {
                        Text("Retry")
                            .font(.titleSmall)
                            .foregroundStyle(.onPrimary)
                            .frame(width: 120, height: 40)
                            .background(.appPrimary)
                            .clipShape(Capsule())
                    }
                }
                Spacer()
            }
        }
        .animation(.easeInOut(duration: 0.22), value: isSettled)
        .task {
            if let surah {
                screen.viewModel.dispatch(intent: TafsirIntentOpened(surahNumber: surah.number))
            }
        }
    }

    private var header: some View {
        VStack(spacing: 4) {
            Text(surah?.englishName ?? "Tafseer")
                .font(.titleMedium)
                .foregroundStyle(.textPrimary)

            Text("Tafseer Ibn Kathir")
                .font(.bodySmall)
                .foregroundStyle(.textSecondary)

            if state.showsEnglishInstead {
                Text("Hindi tafseer isn't available yet — showing English")
                    .font(.bodySmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 4)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 24)
        .padding(.bottom, 16)
    }

    private func ayahRow(_ entry: TafsirEntry) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(ayahLabel(entry))
                .font(.labelLarge)
                .foregroundStyle(.appPrimary)

            VStack(alignment: .leading, spacing: 12) {
                ForEach(Array(entry.paragraphs.enumerated()), id: \.offset) { _, paragraph in
                    paragraphText(paragraph)
                }
            }

            Rectangle()
                .fill(Color(.systemGray5))
                .frame(height: 1)
                .padding(.top, 4)
        }
        .padding(.vertical, 12)
    }

    @ViewBuilder
    private func paragraphText(_ paragraph: String) -> some View {
        if isUrdu {
            Text(paragraph)
                .font(.urduNastaliq(17))
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .fixedSize(horizontal: false, vertical: true)
        } else if Self.startsWithArabic(paragraph) {
            Text(restyling(["،", "؟", "؛"], in: paragraph.kfgqpcEncoded, base: .arabic(20), mark: .system(size: 17)))
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .fixedSize(horizontal: false, vertical: true)
        } else {
            Text(restyling(["ﷺ"], in: paragraph, base: .bodySmall, mark: .system(size: 9)))
                .foregroundStyle(.textPrimary)
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    private func restyling(_ marks: [String], in paragraph: String, base: Font, mark markFont: Font) -> AttributedString {
        var styled = AttributedString(paragraph)
        styled.font = base
        for mark in marks {
            var searchStart = styled.startIndex
            while let range = styled[searchStart...].range(of: mark) {
                styled[range].font = markFont
                searchStart = range.upperBound
            }
        }
        return styled
    }

    private nonisolated static func startsWithArabic(_ paragraph: String) -> Bool {
        guard let firstLetter = paragraph.unicodeScalars.first(where: { $0.properties.isAlphabetic }) else { return false }
        return isArabicScript(firstLetter)
    }

    private nonisolated static func isArabicScript(_ scalar: Unicode.Scalar) -> Bool {
        switch scalar.value {
        case 0x0600...0x06FF, 0x0750...0x077F, 0x08A0...0x08FF, 0xFB50...0xFDFF, 0xFE70...0xFEFF:
            return true
        default:
            return false
        }
    }

    private func ayahLabel(_ entry: TafsirEntry) -> String {
        entry.lastAyahNumber > entry.ayahNumber
            ? "Ayah \(entry.ayahNumber)–\(entry.lastAyahNumber)"
            : "Ayah \(entry.ayahNumber)"
    }
}
