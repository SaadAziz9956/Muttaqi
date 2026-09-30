import Shared
import UserNotifications

/// Notification permission for the shared code, on UserNotifications
nonisolated final class NotificationService: NSObject, NotificationPermission {
    func request(onResult: @escaping (KotlinBoolean) -> Void) {
        Task {
            let granted = (try? await UNUserNotificationCenter.current()
                .requestAuthorization(options: [.alert, .badge, .sound])) ?? false
            onResult(KotlinBoolean(bool: granted))
        }
    }
}
