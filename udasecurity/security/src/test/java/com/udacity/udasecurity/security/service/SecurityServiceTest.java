package com.udacity.udasecurity.security.service;

import com.udacity.udasecurity.image.service.FakeImageService;
import com.udacity.udasecurity.security.data.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;

    @Mock
    private FakeImageService imageService;

    @Mock
    private PretendDatabaseSecurityRepositoryImpl securityRepository;

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorActivated_whenAlarmArmed_returnPendingAlarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorActivated_whenAlarmArmedAndAlarmStatusPendingAlarm_returnAlarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorDeactivated_whenAlarmStatusPendingAlarm_returnNoAlarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        securityService.changeSensorActivationStatus(sensor, false);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorStatus_whenAlarmStatusAlarm_keptAlarmStatus(Sensor sensor) {
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

    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void activateSensor_whenSensorAlreadyActivatedAndAlarmStatusPendingAlarm_returnAlarmStatusAlarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        securityService.changeSensorActivationStatus(sensor, true);

        Assertions.assertAll(
                () -> Mockito.
                        verify(securityRepository)
                        .setAlarmStatus(AlarmStatus.ALARM),
                () -> Mockito
                        .verify(securityRepository, Mockito.never())
                        .setAlarmStatus(AlarmStatus.PENDING_ALARM),
                () -> Mockito
                        .verify(securityRepository, Mockito.never())
                        .setAlarmStatus(AlarmStatus.NO_ALARM)
        );
    }


    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void deactivateSensor_whenSensorAlreadyDeactivatedAndAlarmStatusNoAlarm_noChangeToAlarmStatus(Sensor sensor) {
        securityService.changeSensorActivationStatus(sensor, false);

        Assertions.assertAll(
                () -> Mockito
                        .verify(securityRepository, Mockito.never())
                        .setAlarmStatus(AlarmStatus.PENDING_ALARM),
                () -> Mockito
                        .verify(securityRepository, Mockito.never())
                        .setAlarmStatus(AlarmStatus.ALARM)
        );
    }

    @ParameterizedTest
    @MethodSource("differentImageType")
    public void detectCat_whenAlarmArmedHome_returnAlarmStatusAlarm(BufferedImage image) {
        Mockito.doReturn(true)
                .when(imageService)
                .imageContainsCat(Mockito.any(BufferedImage.class), Mockito.anyFloat());
        Mockito.doReturn(ArmingStatus.ARMED_HOME)
                .when(securityRepository)
                .getArmingStatus();

        securityService.processImage(image);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @MethodSource("differentImageType")
    public void detectNoCat_whenSensorsDeactivated_returnAlarmStatusNoAlarm(BufferedImage image) {
        Mockito.doReturn(false)
                .when(imageService)
                .imageContainsCat(Mockito.any(BufferedImage.class), Mockito.anyFloat());

        securityService.processImage(image);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }


    private static Stream<Arguments> differentSensorType() {
        return Stream.of(
                Arguments.of(new Sensor("sensorDoor", SensorType.DOOR)),
                Arguments.of(new Sensor("sensorMotion", SensorType.MOTION)),
                Arguments.of(new Sensor("sensorWindow", SensorType.WINDOW))
        );
    }

    private static Stream<Arguments> differentImageType() {
        return Stream.of(
                Arguments.of(new BufferedImage(100, 100, 1)),
                Arguments.of(new BufferedImage(150, 150, 8)),
                Arguments.of(new BufferedImage(500, 800, 11))
        );
    }
}
