import Shared
import SwiftUI

struct NamesView: View {
    @State private var screen = SharedViewModel(NamesViewModels.shared.names()) { $0.state }
    @State private var currentNumber: Int32? = 1
    @State private var isSearchOpen = false
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss

    private var state: NamesState { screen.state }

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 0) {
                    Text("99 Names")
                        .font(.custom("ReemKufi-Regular", size: 28))
                        .foregroundStyle(.appPrimary)
                        .padding(.top, 8)

                    carousel(cardHeight: max(260, geometry.size.height * 0.415))
                        .padding(.top, 28)

                    Spacer(minLength: 32)

                    Text("\(currentNumber ?? 1) of \(max(state.names.count, 99))")
                        .font(.custom("ReemKufi-Medium", size: 13))
                        .foregroundStyle(.appPrimary)
                        .contentTransition(.numericText())
                        .animation(.snappy, value: currentNumber)
                        .softPill()
                        .padding(.bottom, 22)

                    if let hadith = state.hadith {
                        VStack(spacing: 6) {
                            Text(hadith.text)
                                .font(TranslationStyle(for: hadith.text, size: 14).font)
                                .foregroundStyle(.textSecondary)
                            Text(hadith.source)
                                .font(.labelSmall)
                                .foregroundStyle(.brandTeal)
                            Text(state.listSource)
                                .font(.labelSmall)
                                .foregroundStyle(.textSecondary)
                        }
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24)
                        .padding(.bottom, 12)
                    }
                }
                .frame(minHeight: geometry.size.height)
            }
            .scrollBounceBehavior(.basedOnSize)
        }
        .background { SoftBackdrop() }
        .navigationBarTitleDisplayMode(.inline)
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
            if state.names.contains(where: { $0.number == currentNumber }) {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button { dispatch(NamesIntentShareTapped.shared) } label: {
                        Image("export-arrow-01-linear")
                            .resizable()
                            .frame(width: 22, height: 22)
                            .foregroundStyle(.textPrimary)
                            .contentShape(.rect)
                    }
                    .buttonStyle(SoftPressStyle())
                    .accessibilityLabel("Share")
                }
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { isSearchOpen = true } label: {
                    Image("search-normal-linear")
                        .resizable()
                        .frame(width: 22, height: 22)
                        .foregroundStyle(.textPrimary)
                }
                .accessibilityLabel("Search names")
            }
        }
        .navigationDestination(isPresented: $isSearchOpen) {
            NamesSearchView(screen: screen) { number in
                currentNumber = number
                isSearchOpen = false
            }
        }
        .onChange(of: currentNumber) { _, number in
            if let number { dispatch(NamesIntentNameShown(number: number)) }
        }
        .task {
            for await effect in screen.viewModel.effects {
                switch onEnum(of: effect) {
                case .openShare(let share): router.push(share.passage)
                case .showName(let show): currentNumber = show.number
                }
            }
        }
    }

    private func dispatch(_ intent: NamesIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private func carousel(cardHeight: CGFloat) -> some View {
        ScrollView(.horizontal) {
            LazyHStack(alignment: .top, spacing: 12) {
                ForEach(state.names, id: \.number) { name in
                    NameCard(name: name, minHeight: cardHeight)
                        .padding(.vertical, 18)
                        .containerRelativeFrame(.horizontal)
                        .scrollTransition(axis: .horizontal) { card, phase in
                            card
                                .scaleEffect(phase.isIdentity ? 1 : 0.88, anchor: phase.value < 0 ? .trailing : .leading)
                                .opacity(phase.isIdentity ? 1 : 0.6)
                        }
                }
            }
            .scrollTargetLayout()
        }
        .contentMargins(.horizontal, 38, for: .scrollContent)
        .scrollClipDisabled()
        .scrollTargetBehavior(.viewAligned)
        .scrollPosition(id: $currentNumber)
        .scrollIndicators(.hidden)
        .sensoryFeedback(.selection, trigger: currentNumber)
    }
}
