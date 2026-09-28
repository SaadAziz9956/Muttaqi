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

            if let section = viewModel.selectedSection {
                categoryTabs(selected: section.id)
                    .listRowSeparator(.hidden)

                Text(section.subtitle)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
                    .listRowInsets(EdgeInsets(top: 4, leading: 20, bottom: 0, trailing: 20))
                    .listRowSeparator(.hidden)

                ForEach(section.dhikr) { dhikr in
                    Button {
                        router.pushHome(.dhikr(dhikr))
                    } label: {
                        DhikrRow(dhikr: dhikr)
                    }
                    .listRowInsets(Self.rowInsets)
                }
            }
        }
        .listStyle(.plain)
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
        let quote = HadithQuote.rememberingAllah.text(language: viewModel.language)
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

            Text(HadithQuote.rememberingAllah.source)
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
                HStack(spacing: 8) {
                    ForEach(viewModel.sections) { section in
                        let isSelected = section.id == selected
                        Button {
                            viewModel.selectedSectionID = section.id
                        } label: {
                            Text(section.title)
                                .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .subheadline))
                                .foregroundStyle(isSelected ? Color.onPrimary : Color.brandTeal)
                                .padding(.horizontal, 16)
                                .frame(minHeight: 36)
                                .background(isSelected ? Color.appPrimary : Color.tintedSurface, in: .capsule)
                                .contentShape(.capsule)
                        }
                        .buttonStyle(.plain)
                        .id(section.id)
                        .accessibilityAddTraits(isSelected ? .isSelected : [])
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
            }
            .scrollIndicators(.hidden)
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

        VStack(alignment: .leading, spacing: 10) {
            Text(AttributedString.arabic(dhikr.arabic, size: 20))
                .foregroundStyle(.brandTeal)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .trailing)

            Text(caption)
                .font(captionStyle.font)
                .foregroundStyle(.textPrimary)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: captionStyle.isRightToLeft ? .trailing : .leading)
        }
        .padding(.vertical, 18)
        .contentShape(.rect)
        // The divider spans the row's full width, with the same margin on both sides
        .alignmentGuide(.listRowSeparatorLeading) { _ in 0 }
        .alignmentGuide(.listRowSeparatorTrailing) { $0[.trailing] }
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(dhikr.title ?? dhikr.transliteration)
        .accessibilityValue(dhikr.translation ?? "")
    }
}
