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
    @MethodSource("differentSensorType")
    public void changeSensorActivated_whenAlertArmed_pendingAlarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorActivated_whenAlertArmedAndPendingAlarmStatus_alarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void getAlarmStatus_whenPendingAlarmStatusAndAllSensorsInactive_noAlarm() {
    }

    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorStatus_whenAlarmStatusActive_keptAlarmStatus(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        securityService.changeSensorActivationStatus(sensor, false);

        Assertions.assertAll(
                () -> Mockito
                        .verify(securityRepository, Mockito.never())
                        .setAlarmStatus(AlarmStatus.NO_ALARM),
                () -> Mockito
                        .verify(securityRepository, Mockito.never())
                        .setAlarmStatus(AlarmStatus.PENDING_ALARM)
        );
    }

    private static Stream<Arguments> differentSensorType() {
        return Stream.of(
                Arguments.of(new Sensor("sensorDoor", SensorType.DOOR)),
                Arguments.of(new Sensor("sensorMotion", SensorType.MOTION)),
                Arguments.of(new Sensor("sensorWindow", SensorType.WINDOW))
        );
    }
}
