import Shared
import SwiftUI

struct DuaChapterView: View {
    @State private var screen: SharedViewModel<DuaChapterViewModel, DuaChapterState>
    @State private var titleBottom: CGFloat = .infinity
    @Environment(AppRouter.self) private var router

    init(chapterId: String) {
        _screen = State(initialValue: SharedViewModel(DuaViewModels.shared.chapter(id: chapterId)) { $0.state })
    }

    var body: some View {
        if let chapter = screen.state.chapter {
            content(chapter)
        } else {
            Color.clear.background { SoftBackdrop() }
        }
    }

    private func content(_ chapter: DuaChapter) -> some View {
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

                ForEach(chapter.entries, id: \.id) { entry in
                    DuaEntryCard(
                        entry: entry,
                        onShare: { screen.viewModel.dispatch(intent: DuaChapterIntentShareTapped(entryId: entry.id)) },
                        onCopy: { screen.viewModel.dispatch(intent: DuaChapterIntentCopyTapped(entryId: entry.id)) }
                    )
                }

                // Names whose translations are shown, as their publishers ask
                Text("Translation: " + screen.state.translationCredits.joined(separator: ", "))
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
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openShare(let share): router.push(share.passage)
                case .copy(let copy): UIPasteboard.general.string = copy.text
                }
            }
        }
    }
}
