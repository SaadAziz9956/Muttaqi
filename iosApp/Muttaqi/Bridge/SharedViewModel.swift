import Observation
import Shared

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
