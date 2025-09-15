package esthesis.edge.modules.deddie;

import esthesis.edge.modules.deddie.config.DeddieProperties;
import io.quarkus.runtime.StartupEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeddieModuleTest {

    @InjectMocks
    DeddieModule deddieModule;

    @Mock
    DeddieProperties deddieProperties;

    @BeforeEach
    void setUp() {
        when(deddieProperties.enabled()).thenReturn(true);
    }

    @Test
    void onStart() {
        assertDoesNotThrow(() -> deddieModule.onStart(mock(StartupEvent.class)));
    }
}