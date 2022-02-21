package com.udacity.udasecurity.service;

import com.sun.nio.sctp.SendFailedNotification;
import com.udacity.udasecurity.data.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;
    private SecurityRepository securityRepository;
    private Sensor sensor;

    @Mock
    private ImageService imageService;

    @BeforeEach
    void init() {
        securityRepository = new PretendDatabaseSecurityRepositoryImpl();
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    public void getAlarmStatus_whenAlarmArmedAndSensorActivated_returnsPendingAlarmStatus() {
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        sensor = new Sensor("", SensorType.DOOR);
        sensor.setActive(true);
        securityService.addSensor(sensor);

        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
    }

    @AfterEach
    public void cleanup() {
        securityRepository = null;
        securityService = null;
    }

}
