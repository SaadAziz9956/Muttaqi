import SwiftUI

/// The chapters in one category, e.g. every chapter under "Prayer & Purification"
struct DuaCategoryView: View {
    let category: DuaCategory

    var body: some View {
        List(category.chapters) { chapter in
            NavigationLink(value: AppRouter.DuaDestination.chapter(chapter)) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(chapter.title)
                        .font(.bodyMedium)
                        .foregroundStyle(.textPrimary)
                    Text(chapter.entries.count == 1 ? "1 dua" : "\(chapter.entries.count) duas")
                        .font(.labelSmall)
                        .foregroundStyle(.textSecondary)
                }
                .padding(.vertical, 4)
            }
        }
        .listStyle(.insetGrouped)
        .navigationTitle(category.title)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar(.hidden, for: .tabBar)
    }
}
