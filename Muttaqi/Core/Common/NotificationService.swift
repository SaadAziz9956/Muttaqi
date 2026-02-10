import UserNotifications

protocol NotificationServiceProtocol: Sendable {
    func requestPermission() async -> Bool
}

final class NotificationService: NotificationServiceProtocol {
    func requestPermission() async -> Bool {
        do {
            return try await UNUserNotificationCenter.current()
                .requestAuthorization(options: [.alert, .badge, .sound])
        } catch {
            return false
        }
    }
}
