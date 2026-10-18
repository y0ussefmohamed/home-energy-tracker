package com.projects.Device.Service.repository;

import com.projects.Device.Service.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, Long> {  }
