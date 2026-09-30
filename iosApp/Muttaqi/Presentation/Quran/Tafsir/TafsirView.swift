import Shared
import SwiftUI

struct TafsirView: View {
    let surah: Surah?
    let screen: SharedViewModel<TafsirViewModel, TafsirState>
    /// Opens scrolled to the commentary covering this ayah, e.g. from the ayah's own Explanation button
    var startAyah: Int32? = nil

    private var state: TafsirState { screen.state }
    private var isUrdu: Bool { state.language == .urdu }

    var body: some View {
        VStack(spacing: 0) {
            header

            switch onEnum(of: state.status) {
            case .idle, .loading:
                Spacer()
                ProgressView()
                    .tint(.appPrimary)
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
                        // Commentary often covers a group of ayahs, so this finds the group the ayah is in
                        guard let startAyah, let entry = state.entryCovering(ayah: startAyah) else { return }
                        proxy.scrollTo(entry.ayahNumber, anchor: .top)
                    }
                }

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
        .task {
            // Loads the first time it opens for this surah and language, and keeps what's loaded after
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

            // There's no Hindi tafseer source, so the repository serves English for Hindi readers
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

            // One entry can run past 40k characters, so it's laid out a paragraph at a time
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

    // English commentary quotes hadith and ayahs as their own Arabic paragraphs; those get the Arabic font, right-aligned
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
            // The Uthmanic font draws Arabic punctuation (، ؟ ؛) as a dotted circle, so those marks use the system font
            Text(restyling(["،", "؟", "؛"], in: paragraph.kfgqpcEncoded, base: .arabic(20), mark: .system(size: 17)))
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.trailing)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .fixedSize(horizontal: false, vertical: true)
        } else {
            // ﷺ is taller than a line of body text and would overlap the lines around it, so it's drawn smaller
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
