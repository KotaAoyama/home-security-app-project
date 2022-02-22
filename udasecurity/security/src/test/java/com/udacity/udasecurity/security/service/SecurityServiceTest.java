package com.udacity.udasecurity.security.service;

import com.udacity.udasecurity.security.data.*;
import com.udacity.udasecurity.image.service.ImageService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;
    private Sensor sensor;

    @Mock
    private ImageService imageService;

    @Mock
    private PretendDatabaseSecurityRepositoryImpl securityRepository;

    @BeforeEach
    void init() {

        securityService = new SecurityService(securityRepository, imageService);
    }

//    @Test
//    public void givenAlarmArmed_whenSensorActivated_getPendingAlarmStatus() {
//
//        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
//        sensor = new Sensor("", SensorType.DOOR);
//        securityService.addSensor(sensor);
//
//        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
//    }

    @AfterEach
    public void cleanup() {
        securityRepository = null;
        securityService = null;
    }

}
