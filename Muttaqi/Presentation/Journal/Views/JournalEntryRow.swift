import SwiftUI

struct JournalEntryRow: View {
    let entry: JournalEntry
    @ScaledMetric(relativeTo: .title2) private var dateColumnWidth: CGFloat = 100

    var body: some View {
        HStack(spacing: 0) {
            VStack(spacing: 2) {
                Text(entry.createdAt, format: .dateTime.day().month(.abbreviated))
                    .font(.custom("ReemKufi-Regular", size: 22, relativeTo: .title2))
                    .foregroundStyle(.brandTeal)
                Text(weekday)
                    .font(.labelSmall)
                    .foregroundStyle(.textSecondary)
            }
            .lineLimit(1)
            .frame(width: dateColumnWidth)

            Text(entry.preview)
                .font(.custom("ReemKufi-Regular", size: 14, relativeTo: .body))
                .foregroundStyle(.textPrimary)
                .lineLimit(1)
                .padding(.leading, 15)

            Spacer(minLength: 12)

            Image("arrow-right-02-linear")
                .resizable()
                .frame(width: 24, height: 24)
                .foregroundStyle(.textPrimary)
                .padding(.trailing, 8)
                .accessibilityHidden(true)
        }
        .padding(.vertical, 20)
        .contentShape(.rect)
        // The divider spans the row's full width, under the date too, with the same margin on both sides
        .alignmentGuide(.listRowSeparatorLeading) { _ in 0 }
        .alignmentGuide(.listRowSeparatorTrailing) { $0[.trailing] }
        .accessibilityElement(children: .combine)
    }

    /// The weekday, plus the year for entries from another year, since the date above shows only day and month
    private var weekday: String {
        let weekday = entry.createdAt.formatted(.dateTime.weekday(.wide))
        guard !Calendar.current.isDate(entry.createdAt, equalTo: .now, toGranularity: .year) else { return weekday }
        return "\(weekday) · \(entry.createdAt.formatted(.dateTime.year()))"
    }
}
