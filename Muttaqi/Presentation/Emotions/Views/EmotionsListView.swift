import SwiftUI

struct EmotionsListView: View {
    @State private var viewModel: EmotionsViewModel
    @State private var titleBottom: CGFloat = .infinity
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss

    init(viewModel: EmotionsViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Text("Emotions")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.appPrimary)
                    .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                    .padding(.top, 24)

                if let header = viewModel.header {
                    Text(header.translation.quoted)
                        .font(TranslationStyle(for: header.translation, size: 14).font)
                        .foregroundStyle(.textPrimary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 16)

                    Text("Quran (\(header.reference))")
                        .font(.labelSmall)
                        .foregroundStyle(.textSecondary)
                        .padding(.top, 4)
                }

                LazyVGrid(columns: [GridItem(.flexible(), spacing: 20), GridItem(.flexible(), spacing: 20)], spacing: 12) {
                    ForEach(viewModel.emotions) { emotion in
                        Button {
                            router.pushHome(.emotion(id: emotion.id))
                        } label: {
                            Text(emotion.title)
                                .font(.custom("ReemKufi-Regular", size: 12, relativeTo: .footnote))
                                .foregroundStyle(.brandTeal)
                                .frame(maxWidth: .infinity, minHeight: 53)
                                .background(.tintedSurface, in: .rect(cornerRadius: 12))
                                .contentShape(.rect(cornerRadius: 12))
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.top, 48)
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 32)
        }
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("Emotions", titleBottom: titleBottom)
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
                .accessibilityLabel("Back")
            }
        }
        .onAppear { viewModel.load() }
    }
}
