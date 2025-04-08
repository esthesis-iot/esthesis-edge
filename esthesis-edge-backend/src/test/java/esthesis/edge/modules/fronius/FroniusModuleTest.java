package esthesis.edge.modules.fronius;

import esthesis.edge.modules.fronius.service.FroniusService;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@QuarkusTest
class FroniusModuleTest {

    @Inject
    FroniusModule froniusModule;


    @InjectMock
    FroniusService froniusService;

    @BeforeEach
    void setUp() {
        doNothing().when(froniusService).updateDevices();
    }


    @Test
    void onStart() {
        assertDoesNotThrow(() -> froniusModule.onStart(mock(StartupEvent.class)));
        verify(froniusService).updateDevices();

    }

}