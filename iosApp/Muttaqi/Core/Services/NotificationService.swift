import Shared
import UserNotifications

nonisolated final class NotificationService: NSObject, NotificationPermission {
    func request(onResult: @escaping (KotlinBoolean) -> Void) {
        Task {
            let granted = (try? await UNUserNotificationCenter.current()
                .requestAuthorization(options: [.alert, .badge, .sound])) ?? false
            onResult(KotlinBoolean(bool: granted))
        }
    }
}
