import Observation
import Shared

/// A shared (Kotlin) view model for one SwiftUI screen: `state` follows the view model's state flow, so SwiftUI
/// redraws when it changes, and the view model is cleared, cancelling its work, when the screen goes away.
///
///     @State private var screen = SharedViewModel(DuaViewModels.shared.list()) { $0.state }
///     ...
///     Text(screen.state.query)
///     screen.viewModel.dispatch(intent: DuaListIntentQueryChanged(query: text))
///     .task { for await effect in screen.viewModel.effects { ... } }
@Observable
@MainActor
final class SharedViewModel<VM: Lifecycle_viewmodelViewModel, State: AnyObject> {
    let viewModel: VM
    private(set) var state: State

    @ObservationIgnored private let owner: IosViewModelOwner<VM>
    @ObservationIgnored private var observation: Task<Void, Never>?

    init(_ viewModel: VM, state: (VM) -> SkieSwiftStateFlow<State>) {
        self.viewModel = viewModel
        self.owner = IosViewModelOwner(viewModel: viewModel)
        let flow = state(viewModel)
        self.state = flow.value
        observation = Task { [weak self] in
            for await next in flow {
                self?.state = next
            }
        }
    }

    isolated deinit {
        observation?.cancel()
        owner.clear()
    }
}
