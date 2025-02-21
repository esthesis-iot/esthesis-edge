package esthesis.edge.jobs;

import esthesis.edge.dto.DeviceDTO;
import esthesis.edge.services.DeviceService;
import esthesis.edge.services.EsthesisCoreService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@Transactional
class CoreRegistrationJobTest {

    @Inject
    CoreRegistrationJob coreRegistrationJob;

    @InjectSpy
    DeviceService deviceService;

    @InjectSpy
    EsthesisCoreService esthesisCoreService;


    @Test
    void execute() {
        when(deviceService.listDevicesPendingCoreRegistration(anyString()))
                .thenReturn(List.of(DeviceDTO.builder().hardwareId("hardwareId").tags("tag1,tag2").build()));

        doNothing().when(esthesisCoreService).registerDevice(anyString(), anyList());

        assertDoesNotThrow(() ->
                coreRegistrationJob.execute()
        );

        verify(esthesisCoreService).registerDevice("hardwareId", List.of("tag1", "tag2"));
    }
}