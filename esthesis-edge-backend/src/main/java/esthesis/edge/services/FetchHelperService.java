package esthesis.edge.services;

import esthesis.edge.model.DeviceEntity;
import esthesis.edge.model.DeviceModuleConfigEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Transactional
@ApplicationScoped
@RequiredArgsConstructor
public class FetchHelperService {

    /**
     * A helper method to increase by one the number of errors for a specific configuration key. If
     * the key does not exist, it will be created with value "1".
     *
     * @param hardwareId The hardware ID of the device.
     * @param configKey  The configuration key.
     */
    public void increaseErrors(String hardwareId, String configKey) {
        Optional<DeviceModuleConfigEntity> config = DeviceModuleConfigEntity.getConfig(hardwareId,
                configKey);
        if (config.isPresent()) {
            int currentErrorVal = Integer.parseInt(config.get().getConfigValue());
            config.get().setConfigValue(String.valueOf(currentErrorVal + 1));
        } else {
            DeviceModuleConfigEntity newConfig = DeviceModuleConfigEntity.create(configKey, "1");
            newConfig.setDevice(DeviceEntity.findByHardwareId(hardwareId).orElseThrow());
            newConfig.persist();
        }
    }

    /**
     * A helper method to reset (to zero) the number of errors for a specific configuration key.
     *
     * @param hardwareId The hardware ID of the device.
     * @param configKey  The configuration key.
     */
    public void resetErrors(String hardwareId, String configKey) {
        Optional<DeviceModuleConfigEntity> config = DeviceModuleConfigEntity.getConfig(hardwareId,
                configKey);
        if (config.isPresent()) {
            config.get().setConfigValue("0");
        } else {
            log.warn("Failed to reset errors for device '{}' as config key '{}' does not exist.",
                    hardwareId, configKey);
        }
    }

}
