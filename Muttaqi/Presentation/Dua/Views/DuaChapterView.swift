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
            }
            .padding(.horizontal, 22)
            .padding(.bottom, 32)
        }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle(chapter.title, titleBottom: titleBottom)
        .toolbar(.hidden, for: .tabBar)
    }
}
