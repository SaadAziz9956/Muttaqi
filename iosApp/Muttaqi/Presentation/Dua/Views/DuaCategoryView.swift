import Shared
import SwiftUI

struct DuaCategoryView: View {
    @State private var screen: SharedViewModel<DuaCategoryViewModel, DuaCategoryState>
    @State private var titleBottom: CGFloat = .infinity
    @Environment(AppRouter.self) private var router

    init(categoryId: String) {
        _screen = State(initialValue: SharedViewModel(DuaViewModels.shared.category(id: categoryId)) { $0.state })
    }

    var body: some View {
        let category = screen.state.category

        ScrollView {
            LazyVStack(spacing: 12) {
                Text(category?.title ?? "")
                    .font(.custom("ReemKufi-Regular", size: 26))
                    .foregroundStyle(.appPrimary)
                    .multilineTextAlignment(.center)
                    .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                    .onDisappear { titleBottom = -.infinity }
                    .padding(.top, 12)
                    .padding(.bottom, 12)

                ForEach(category?.chapters ?? [], id: \.id) { chapter in
                    Button {
                        screen.viewModel.dispatch(intent: DuaCategoryIntentChapterTapped(chapterId: chapter.id))
                    } label: {
                        HStack(spacing: 12) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(chapter.title)
                                    .font(.custom("ReemKufi-Medium", size: 15))
                                    .foregroundStyle(.appPrimary)
                                    .multilineTextAlignment(.leading)
                                Text(chapter.entries.count == 1 ? "1 dua" : "\(chapter.entries.count) duas")
                                    .font(.custom("ReemKufi-Regular", size: 12))
                                    .foregroundStyle(.brandTeal)
                            }
                            Spacer(minLength: 8)
                            Image("arrow-right-02-linear")
                                .resizable()
                                .frame(width: 18, height: 18)
                                .foregroundStyle(.textSecondary)
                        }
                        .padding(18)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .softCard(cornerRadius: 24)
                    }
                    .buttonStyle(SoftPressStyle())
                }
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle(category?.title ?? "", titleBottom: titleBottom)
        .toolbar(.hidden, for: .tabBar)
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openChapter(let open): router.pushDua(.chapter(id: open.chapterId))
                }
            }
        }
    }
}
