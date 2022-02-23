package com.udacity.udasecurity.security.service;

import com.udacity.udasecurity.security.data.*;
import com.udacity.udasecurity.image.service.ImageService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;

    @Mock
    private ImageService imageService;

    @Mock
    private PretendDatabaseSecurityRepositoryImpl securityRepository;

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @ParameterizedTest
    @MethodSource("differentSensorTypeAndSensorActive")
    public void changeSensorActivated_whenAlertArmed_pendingAlarm(Sensor sensor, boolean active) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, active);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @ParameterizedTest
    @MethodSource("differentSensorTypeAndSensorActive")
    public void changeSensorActivated_whenAlertArmedAndPendingAlarmStatus_alarm(Sensor sensor, boolean active) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, active);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void getAlarmStatus_whenPendingAlarmStatusAndAllSensorsInactive_noAlarm() {

    }

    private static Stream<Arguments> differentSensorTypeAndSensorActive() {
        return Stream.of(
                Arguments.of(new Sensor("sensorDoor", SensorType.DOOR), true),
                Arguments.of(new Sensor("sensorMotion", SensorType.MOTION), true),
                Arguments.of(new Sensor("sensorWindow", SensorType.WINDOW), true)
        );
    }
}
