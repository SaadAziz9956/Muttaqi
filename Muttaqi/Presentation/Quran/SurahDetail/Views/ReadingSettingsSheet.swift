import SwiftUI

/// Settings sheet - loosely coupled and reusable
struct ReadingSettingsSheet: View {
    @Bindable var settingsViewModel: ReadingSettingsViewModel
    let onLanguageSelected: (Language) -> Void
    
    @State private var showLanguagePicker = false
    
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
                    isSelected: settingsViewModel.readingMode == .withTranslation
                )
                readingModeCard(
                    mode: .arabicOnly,
                    label: "Arabic Only",
                    isSelected: settingsViewModel.readingMode == .arabicOnly
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
                        settingsViewModel.decreaseFontSize()
                    } label: {
                        Image(systemName: "minus.circle.fill")
                            .font(.title2)
                            .foregroundStyle(.appPrimary)
                    }

                    Text("\(settingsViewModel.fontSize.value)")
                        .font(.bodyLarge)
                        .foregroundStyle(.textPrimary)
                        .frame(width: 30, alignment: .center)

                    Button {
                        settingsViewModel.increaseFontSize()
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .font(.title2)
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
                        Text(settingsViewModel.selectedLanguage.name)
                            .font(.bodyMedium)
                            .foregroundStyle(.textPrimary)

                        Image(systemName: "chevron.down")
                            .font(.caption)
                            .foregroundStyle(.textSecondary)
                    }
                }
            }
            .padding(.top, 28)
            .padding(.horizontal, 20)

            if settingsViewModel.isDownloadingLanguage {
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
            settingsViewModel.setReadingMode(mode)
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
                ForEach(Language.available, id: \.code) { language in
                    Button {
                        onLanguageSelected(language)
                        showLanguagePicker = false
                    } label: {
                        Text(language.name)
                            .font(.bodyLarge)
                            .foregroundStyle(.textPrimary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                            .background(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(
                                        settingsViewModel.selectedLanguage == language ? .appPrimary : Color(.systemGray4),
                                        lineWidth: settingsViewModel.selectedLanguage == language ? 2 : 1
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
