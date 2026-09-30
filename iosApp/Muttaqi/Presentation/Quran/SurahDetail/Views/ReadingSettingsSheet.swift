import Shared
import SwiftUI

/// The reader's settings: the reading mode, the font size and the translation
struct ReadingSettingsSheet: View {
    let screen: SharedViewModel<ReadingSettingsViewModel, ReadingSettingsState>

    @State private var showLanguagePicker = false

    private var state: ReadingSettingsState { screen.state }

    private func dispatch(_ intent: ReadingSettingsIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Reading mode")
                .font(.titleSmall)
                .foregroundStyle(.textPrimary)
                .padding(.top, 24)
                .padding(.horizontal, 20)

            HStack(spacing: 16) {
                readingModeCard(
                    mode: .withTranslation,
                    label: "With Translation",
                    isSelected: state.mode == .withTranslation
                )
                readingModeCard(
                    mode: .arabicOnly,
                    label: "Arabic Only",
                    isSelected: state.mode == .arabicOnly
                )
            }
            .padding(.top, 16)
            .padding(.horizontal, 20)

            HStack {
                Text("Font Size")
                    .font(.bodyMedium)
                    .foregroundStyle(.textPrimary)

                Spacer()

                HStack(spacing: 16) {
                    Button {
                        dispatch(ReadingSettingsIntentFontSizeDecreased.shared)
                    } label: {
                        Image("minus-circle-bold")
                            .resizable()
                            .frame(width: 28, height: 28)
                            .foregroundStyle(.appPrimary)
                    }

                    Text("\(state.fontSize.percentage)%")
                        .font(.bodyLarge)
                        .foregroundStyle(.textPrimary)
                        .frame(width: 50, alignment: .center)

                    Button {
                        dispatch(ReadingSettingsIntentFontSizeIncreased.shared)
                    } label: {
                        Image("add-circle-bold")
                            .resizable()
                            .frame(width: 28, height: 28)
                            .foregroundStyle(.appPrimary)
                    }
                }
            }
            .padding(.top, 28)
            .padding(.horizontal, 20)

            HStack {
                Text("Translation")
                    .font(.bodyMedium)
                    .foregroundStyle(.textPrimary)

                Spacer()

                Button {
                    showLanguagePicker = true
                } label: {
                    HStack(spacing: 4) {
                        Text(state.language.displayName)
                            .font(.bodyMedium)
                            .foregroundStyle(.textPrimary)

                        Image("arrow-down-02-linear")
                            .resizable()
                            .frame(width: 14, height: 14)
                            .foregroundStyle(.textSecondary)
                    }
                }
            }
            .padding(.top, 28)
            .padding(.horizontal, 20)

            if state.isDownloadingLanguage {
                HStack(spacing: 8) {
                    ProgressView()
                        .tint(.appPrimary)
                    Text("Downloading translation...")
                        .font(.bodySmall)
                        .foregroundStyle(.textSecondary)
                }
                .padding(.top, 12)
                .padding(.horizontal, 20)
            }

            Spacer()
        }
        .sheet(isPresented: $showLanguagePicker) {
            languagePickerSheet
                .presentationDetents([.medium])
                .presentationDragIndicator(.visible)
        }
    }

    private func readingModeCard(mode: ReadingMode, label: String, isSelected: Bool) -> some View {
        Button {
            dispatch(ReadingSettingsIntentModeSelected(mode: mode))
        } label: {
            VStack(spacing: 8) {
                RoundedRectangle(cornerRadius: 8)
                    .fill(isSelected ? Color.appPrimary.opacity(0.05) : Color(.systemGray6))
                    .frame(height: 120)
                    .overlay(
                        previewContent(for: mode)
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(isSelected ? .appPrimary : Color(.systemGray4), lineWidth: isSelected ? 2 : 1)
                    )

                Text(label)
                    .font(.labelMedium)
                    .foregroundStyle(isSelected ? .appPrimary : .textSecondary)
            }
        }
        .frame(maxWidth: .infinity)
    }

    @ViewBuilder
    private func previewContent(for mode: ReadingMode) -> some View {
        VStack(spacing: 4) {
            switch mode {
            case .withTranslation:
                Text("بِسْمِ ٱللَّهِ")
                    .font(.arabic(12))
                    .foregroundStyle(.textPrimary)
                Text("Bismillaahir")
                    .font(.system(size: 8))
                    .foregroundStyle(.appPrimary)
                Text("In the Name of Allah")
                    .font(.system(size: 7))
                    .foregroundStyle(.textSecondary)

                Rectangle()
                    .fill(Color(.systemGray4))
                    .frame(height: 0.5)
                    .padding(.horizontal, 12)

                Text("ٱلْحَمْدُ لِلَّهِ")
                    .font(.arabic(12))
                    .foregroundStyle(.textPrimary)
                Text("Alhamdu lillaahi")
                    .font(.system(size: 8))
                    .foregroundStyle(.appPrimary)

            case .arabicOnly:
                Text("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ ۝١ ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ ۝٢")
                    .font(.arabic(11))
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.trailing)
                    .environment(\.layoutDirection, .rightToLeft)
                    .padding(.horizontal, 8)
            }
        }
        .padding(8)
    }

    private var languagePickerSheet: some View {
        VStack(spacing: 0) {
            Text("Choose Language")
                .font(.titleMedium)
                .foregroundStyle(.textPrimary)
                .padding(.top, 24)

            VStack(spacing: 12) {
                ForEach(state.languages, id: \.self) { language in
                    Button {
                        dispatch(ReadingSettingsIntentLanguageSelected(language: language))
                        showLanguagePicker = false
                    } label: {
                        Text(language.displayName)
                            .font(.bodyLarge)
                            .foregroundStyle(.textPrimary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(
                                        state.language == language ? .appPrimary : Color(.systemGray4),
                                        lineWidth: state.language == language ? 2 : 1
                                    )
                            )
                    }
                }
            }
            .padding(.top, 20)
            .padding(.horizontal, 20)

            Spacer()
        }
    }
}
