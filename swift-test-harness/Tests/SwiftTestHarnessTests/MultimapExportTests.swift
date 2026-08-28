import Testing
import Multimap

// Smoke test for the Kotlin → Swift Export → SPM → swift test pipeline.
@Suite("Multimap Export Tests")
struct MultimapExportTests {
    @Test("Swift module loads and imports cleanly")
    func swiftModuleLoads() {
        #expect(true)
    }
}

