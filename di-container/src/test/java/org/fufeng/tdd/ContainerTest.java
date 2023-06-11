package org.fufeng.tdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

        @Nested
        class Qualifier {

        }

    }

    @Nested
    class LifecycleManagement {

    }

}