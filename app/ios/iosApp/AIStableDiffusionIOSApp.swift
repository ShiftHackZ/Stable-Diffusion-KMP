import SwiftUI
import AiSdPresentation

@main
struct AIStableDiffusionIOSApp: App {
    init() {
        KoinBootstrapKt.doInitKoin()
        OpenAiOAuthModuleKt.registerOpenAiOAuthFeature()
        SiliconDiffusionBonsaiRuntimeRegistration.registerIfAvailable()
        SiliconDiffusionCoreMLRuntimeRegistration.registerIfAvailable()
    }

    var body: some Scene {
        WindowGroup {
            ComposeView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color(uiColor: .systemBackground).ignoresSafeArea())
                .ignoresSafeArea()
        }
    }
}
