import Shared
import SwiftUI

struct DhikrListView: View {
    @State private var screen = SharedViewModel(DhikrViewModels.shared.list()) { $0.state }
    @State private var titleBottom: CGFloat = .infinity
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss

    private static let rowInsets = EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20)

    private var state: DhikrListState { screen.state }

    var body: some View {
        List {
            header
                .listRowInsets(Self.rowInsets)
                .listRowSeparator(.hidden)
                .listRowBackground(Color.clear)

            if let section = state.selectedSection {
                categoryTabs(selected: section.id)
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)

                Text(section.subtitle)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .listRowInsets(EdgeInsets(top: 4, leading: 24, bottom: 6, trailing: 20))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)

                ForEach(section.dhikr, id: \.id) { dhikr in
                    Button {
                        dispatch(DhikrListIntentDhikrTapped(dhikrId: dhikr.id))
                    } label: {
                        DhikrRow(dhikr: dhikr)
                    }
                    .buttonStyle(SoftPressStyle())
                    .listRowInsets(EdgeInsets(top: 7, leading: 20, bottom: 7, trailing: 20))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)
                }
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .background { SoftBackdrop() }
        .environment(\.defaultMinListRowHeight, 0)
        .navigationBarTitleDisplayMode(.inline)
        .collapsingBarTitle("Zikr o Azkar", titleBottom: titleBottom)
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
                case .openCounter(let open): router.pushHome(.dhikr(id: open.dhikrId))
                }
            }
        }
    }

    private func dispatch(_ intent: DhikrListIntent) {
        screen.viewModel.dispatch(intent: intent)
    }

    private var header: some View {
        VStack(spacing: 0) {
            Text("Zikr o Azkar")
                .font(.custom("ReemKufi-Regular", size: 28))
                .foregroundStyle(.appPrimary)
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                .padding(.top, 24)

            if let quote = state.header {
                Text(quote.text.quoted)
                    .font(TranslationStyle(for: quote.text, size: 14).font)
                    .foregroundStyle(.textPrimary)
                    .multilineTextAlignment(.center)
                    .padding(.top, 16)

                Text(quote.source)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .padding(.top, 4)
                    .padding(.bottom, 16)
            }
        }
        .frame(maxWidth: .infinity)
    }

    private func categoryTabs(selected: String) -> some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal) {
                GlassEffectContainer(spacing: 4) {
                    HStack(spacing: 8) {
                        ForEach(state.sections, id: \.id) { section in
                            let isSelected = section.id == selected
                            Button {
                                dispatch(DhikrListIntentSectionTapped(sectionId: section.id))
                            } label: {
                                Text(section.title)
                                    .font(.custom("ReemKufi-Medium", size: 13, relativeTo: .subheadline))
                                    .foregroundStyle(isSelected ? Color.white : Color.appPrimary)
                                    .padding(.horizontal, 18)
                                    .frame(minHeight: 36)
                                    .softGlass(in: Capsule(), fill: isSelected ? .shareCard : .softSurface, rim: !isSelected)
                            }
                            .buttonStyle(SoftPressStyle())
                            .id(section.id)
                            .accessibilityAddTraits(isSelected ? .isSelected : [])
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 14)
            }
            .scrollIndicators(.hidden)
            .scrollClipDisabled()
            .onChange(of: selected) { _, id in
                withAnimation(.snappy) { proxy.scrollTo(id, anchor: .center) }
            }
        }
        .listRowInsets(EdgeInsets())
    }
}

private struct DhikrRow: View {
    let dhikr: Dhikr

    var body: some View {
        let caption = dhikr.title
            ?? dhikr.translation.map { $0.sentenceCased.quoted }
            ?? dhikr.transliteration
        let captionStyle = TranslationStyle(for: caption, size: 12)

        VStack(alignment: .leading, spacing: 12) {
            Text(AttributedString.arabic(dhikr.arabic, size: 21))
                .foregroundStyle(.textPrimary)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .trailing)

            HStack(spacing: 10) {
                Text(caption)
                    .font(captionStyle.font)
                    .foregroundStyle(.appPrimary)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: captionStyle.isRightToLeft ? .trailing : .leading)

                if let count = dhikr.target {
                    Text("\(count.intValue)×")
                        .font(.custom("ReemKufi-Medium", size: 12))
                        .foregroundStyle(.brandTeal)
                        .padding(.horizontal, 10)
                        .frame(height: 24)
                        .background(.tintedSurface, in: .capsule)
                }
            }
        }
        .padding(18)
        .softCard(cornerRadius: 24)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(dhikr.title ?? dhikr.transliteration)
        .accessibilityValue(dhikr.translation ?? "")
    }
}
