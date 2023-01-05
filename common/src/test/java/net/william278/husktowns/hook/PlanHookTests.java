package net.william278.husktowns.hook;

import com.djrapitops.plan.extension.extractor.ExtensionExtractor;
import org.junit.jupiter.api.Test;

public class PlanHookTests {

    @Test
    public void testPlanHookImplementation() {
        new ExtensionExtractor(new PlanHook.PlanDataExtension()).validateAnnotations();
    }

}
