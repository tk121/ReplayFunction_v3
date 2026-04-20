package com.example.app.feature.trend.service;

import java.util.List;

import com.example.app.feature.trend.repository.DeviceRepository;

public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<String> findAllDeviceIds() {
        return deviceRepository.findAllDeviceIds();
    }
}