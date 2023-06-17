package org.fufeng.tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

public class ContainerTest {

    ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    class DependenciesSelection {

        @Nested
        class ProviderType {

        }

    }

    @Nested
    class LifecycleManagement {

    }

}