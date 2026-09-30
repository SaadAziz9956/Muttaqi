import SwiftUI

struct DhikrListView: View {
    @State private var viewModel: DhikrListViewModel
    @State private var titleBottom: CGFloat = .infinity
    @Environment(AppRouter.self) private var router
    @Environment(\.dismiss) private var dismiss

    private static let rowInsets = EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 20)

    init(viewModel: DhikrListViewModel) {
        self._viewModel = State(initialValue: viewModel)
    }

    var body: some View {
        List {
            header
                .listRowInsets(Self.rowInsets)
                .listRowSeparator(.hidden)
                .listRowBackground(Color.clear)

            if let section = viewModel.selectedSection {
                categoryTabs(selected: section.id)
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)

                Text(section.subtitle)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .listRowInsets(EdgeInsets(top: 4, leading: 24, bottom: 6, trailing: 20))
                    .listRowSeparator(.hidden)
                    .listRowBackground(Color.clear)

                ForEach(section.dhikr) { dhikr in
                    Button {
                        router.pushHome(.dhikr(dhikr))
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
        // Lets the one-line caption under the tabs be only as tall as its text
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
        .onAppear { viewModel.load() }
    }

    private var header: some View {
        let quote = PublishedQuote.rememberingAllah.text(language: viewModel.language)
        let quoteStyle = TranslationStyle(for: quote, size: 14)

        return VStack(spacing: 0) {
            Text("Zikr o Azkar")
                .font(.custom("ReemKufi-Regular", size: 28))
                .foregroundStyle(.appPrimary)
                .onGeometryChange(for: CGFloat.self) { $0.frame(in: .global).maxY } action: { titleBottom = $0 }
                .padding(.top, 24)

            Text(quote.quoted)
                .font(quoteStyle.font)
                .foregroundStyle(.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.top, 16)

            Text(PublishedQuote.rememberingAllah.source)
                .font(.labelSmall)
                .foregroundStyle(.textSecondary)
                .padding(.top, 4)
                .padding(.bottom, 16)
        }
        .frame(maxWidth: .infinity)
    }

    private func categoryTabs(selected: DhikrSection.ID) -> some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal) {
                // Native glass rendered as one group, which is cheaper than each on its own
                GlassEffectContainer(spacing: 4) {
                    HStack(spacing: 8) {
                        ForEach(viewModel.sections) { section in
                            let isSelected = section.id == selected
                            Button {
                                viewModel.selectedSectionID = section.id
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
                // Room for the chips' float shadow, which a scroll view would otherwise clip
                .padding(.vertical, 14)
            }
            .scrollIndicators(.hidden)
            .scrollClipDisabled()
            // Brings the picked tab into view, e.g. one partly off the edge
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
        // A set of phrases is named by when it's said; a single phrase shows its meaning, or how it's said where no
        // published translation exists
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
                    Text("\(count)×")
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
