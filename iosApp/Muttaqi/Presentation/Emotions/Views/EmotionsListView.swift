import Shared
import SwiftUI

struct EmotionsListView: View {
    @State private var screen = SharedViewModel(TopicsViewModels.shared.emotions()) { $0.state }
    @State private var titleBottom: CGFloat = .infinity
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Text("Emotions")
                    .font(.custom("ReemKufi-Regular", size: 28))
                    .foregroundStyle(.appPrimary)
                    .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                    .padding(.top, 24)

                if let header = screen.state.header {
                    Text(header.text.quoted)
                        .font(TranslationStyle(for: header.text, size: 14).font)
                        .foregroundStyle(.textPrimary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 16)

                    Text(header.source)
                        .font(.labelSmall)
                        .foregroundStyle(.textSecondary)
                        .padding(.top, 4)
                }

                LazyVGrid(columns: [GridItem(.flexible(), spacing: 14), GridItem(.flexible(), spacing: 14)], spacing: 14) {
                    ForEach(screen.state.emotions, id: \.id) { emotion in
                        Button {
                            screen.viewModel.dispatch(intent: EmotionsIntentEmotionTapped(emotionId: emotion.id))
                        } label: {
                            HStack(spacing: 8) {
                                Text(emotion.title)
                                    .font(.custom("ReemKufi-Medium", size: 15, relativeTo: .subheadline))
                                    .foregroundStyle(.appPrimary)
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.85)
                                Spacer(minLength: 0)
                                Image("arrow-right-01-linear")
                                    .resizable()
                                    .frame(width: 14, height: 14)
                                    .rotationEffect(.degrees(-45))
                                    .foregroundStyle(.brandTeal)
                                    .accessibilityHidden(true)
                            }
                            .padding(.horizontal, 18)
                            .frame(maxWidth: .infinity, minHeight: 60)
                            .softCard(cornerRadius: 22)
                        }
                        .buttonStyle(SoftPressStyle())
                    }
                }
                .padding(.top, 36)
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 32)
        }
        .background { SoftBackdrop() }
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
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openEmotion(let open): router.pushHome(.emotion(id: open.emotionId))
                }
            }
        }
    }
}
