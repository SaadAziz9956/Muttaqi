import SwiftUI

struct DuaChapterView: View {
    let chapter: DuaChapter
    @State private var titleBottom: CGFloat = .infinity

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                VStack(spacing: 6) {
                    Text(chapter.title)
                        .font(.custom("ReemKufi-Regular", size: 24))
                        .foregroundStyle(.appPrimary)
                        .multilineTextAlignment(.center)
                        .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                        .onDisappear { titleBottom = -.infinity }

                    if let arabic = chapter.titleArabic {
                        Text(arabic)
                            .font(.arabic(18))
                            .foregroundStyle(.textSecondary)
                    }
                }
                .padding(.top, 12)
                .padding(.bottom, 12)

                ForEach(chapter.entries) { entry in
                    DuaEntryCard(entry: entry)
                }

                // Names whose translations are shown, as their publishers ask
                Text("Translation: " + Self.credits(for: chapter).joined(separator: ", "))
                    .font(.system(size: 11))
                    .foregroundStyle(.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 8)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle(chapter.title, titleBottom: titleBottom)
        .toolbar(.hidden, for: .tabBar)
    }

    private static func credits(for chapter: DuaChapter) -> [String] {
        chapter.entries.reduce(into: []) { credits, entry in
            if !credits.contains(entry.translationCredit) { credits.append(entry.translationCredit) }
        }
    }
}
