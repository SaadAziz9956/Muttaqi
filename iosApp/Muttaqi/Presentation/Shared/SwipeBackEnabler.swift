import SwiftUI

struct SwipeBackEnabler: UIViewRepresentable {
    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        DispatchQueue.main.async {
            uiView.next(ofType: UINavigationController.self)?
                .interactivePopGestureRecognizer?.isEnabled = true
        }
    }
}

private extension UIResponder {
    func next<T>(ofType type: T.Type) -> T? {
        guard let next else { return nil }
        return (next as? T) ?? next.next(ofType: type)
    }
}
