import SwiftUI
import Shared
import FirebaseCrashlytics

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            setCrashCollectionEnabled: { enabled in
                Crashlytics.crashlytics()
                    .setCrashlyticsCollectionEnabled(enabled.boolValue)
            }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}
