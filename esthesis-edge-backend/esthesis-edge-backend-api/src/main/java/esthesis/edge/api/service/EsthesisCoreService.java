package esthesis.edge.api.service;

import java.util.List;

public interface EsthesisCoreService {
    void registerDevice(String hardwareId, List<String> tags);
}
