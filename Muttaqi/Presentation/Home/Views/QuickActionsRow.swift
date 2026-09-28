import SwiftUI

enum QuickAction: CaseIterable, Identifiable {
    case qibla
    case emotions
    case journal
    case dikr
    case names

    var id: Self { self }

    var title: String {
        switch self {
        case .qibla: "Qibla"
        case .emotions: "Emotions"
        case .journal: "Journal"
        case .dikr: "Dikr"
        case .names: "99 Names"
        }
    }

    var icon: String {
        switch self {
        case .qibla: "home-qibla"
        case .emotions: "home-emotions"
        case .journal: "home-journal"
        case .dikr: "home-dikr"
        case .names: "home-99-names"
        }
    }
}

struct QuickActionsRow: View {
    let onSelect: (QuickAction) -> Void

    var body: some View {
        HStack(spacing: 0) {
            ForEach(QuickAction.allCases) { action in
                Button {
                    onSelect(action)
                } label: {
                    VStack(spacing: 5) {
                        Image(action.icon)
                            .foregroundStyle(.brandTeal)
                            .frame(width: 40, height: 40)
                            .background(.tintedSurface, in: .rect(cornerRadius: 8))
                            .accessibilityHidden(true)

                        Text(action.title)
                            .font(.custom("ReemKufi-Regular", size: 10))
                            .foregroundStyle(.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                    .contentShape(.rect)
                }
                .buttonStyle(.plain)
            }
        }
    }
}
