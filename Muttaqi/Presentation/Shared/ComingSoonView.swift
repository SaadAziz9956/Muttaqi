import SwiftUI

struct ComingSoonView: View {
    let title: String
    let icon: String

    var body: some View {
        ContentUnavailableView {
            Label {
                Text(title)
            } icon: {
                Image(icon)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 44, height: 44)
                    .foregroundStyle(.brandTeal)
            }
        } description: {
            Text("Coming soon, in shaa Allah.")
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
    }
}
