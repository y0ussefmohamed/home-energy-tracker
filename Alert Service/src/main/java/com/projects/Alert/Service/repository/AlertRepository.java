package com.projects.Alert.Service.repository;

import com.projects.Alert.Service.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
