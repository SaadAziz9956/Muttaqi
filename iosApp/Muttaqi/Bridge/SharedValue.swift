import Observation
import Shared

/// A shared value that changes many times a second, like the reading position while scrolling, observed on its own so
/// only the view that shows it redraws
@Observable
@MainActor
final class SharedValue<Value> {
    private(set) var value: Value?

    @ObservationIgnored private var observation: Task<Void, Never>?

    init(_ flow: SkieSwiftOptionalStateFlow<Value>) {
        value = flow.value
        observation = Task { [weak self] in
            for await next in flow {
                self?.value = next
            }
        }
    }

    isolated deinit {
        observation?.cancel()
    }
}
