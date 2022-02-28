package com.udacity.udasecurity.security.service;

import com.udacity.udasecurity.image.service.FakeImageService;
import com.udacity.udasecurity.security.data.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
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

//  1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorActivated_whenAlarmArmed_returnPendingAlarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

//  2. If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorActivated_whenAlarmArmedAndAlarmStatusPendingAlarm_returnAlarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

//  3. If pending alarm and all sensors are inactive, return to no alarm state.
    @ParameterizedTest
    @MethodSource("differentSensorType")
    public void changeSensorDeactivated_whenAlarmStatusPendingAlarm_returnNoAlarm(Sensor sensor) {
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        Mockito.when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);
        securityService.changeSensorActivationStatus(sensor, false);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

//  4. If alarm is active, change in sensor state should not affect the alarm state.
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

//  5. If a sensor is activated while already active and the system is in pending state, change it to alarm state.
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

//  6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
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

//  7. If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
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

//  8. If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    @ParameterizedTest
    @MethodSource("differentImageType")
    public void detectNoCat_whenSensorsDeactivated_returnAlarmStatusNoAlarm(BufferedImage image) {
        Mockito.doReturn(false)
                .when(imageService)
                .imageContainsCat(Mockito.any(BufferedImage.class), Mockito.anyFloat());
        Mockito.doReturn(false)
                .when(securityRepository)
                .isAnySensorActive();

        securityService.processImage(image);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

//  9. If the system is disarmed, set the status to no alarm.
    @Test
    public void changeAlarmStatus_whenSystemDisarmed_returnAlarmStatusNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

//  10. If the system is armed, reset all sensors to inactive.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    public void resetAllSensors_whenSystemArmed_returnSensorsDeactivated(ArmingStatus armingStatus) {
        securityService.setArmingStatus(armingStatus);
        Assertions.assertTrue(securityService.getSensors().stream().noneMatch(Sensor::getActive));
    }

//  11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    @ParameterizedTest
    @MethodSource("differentImageType")
    public void alarmArmedHome_whenCatDetected_returnAlarmStatusAlarm(BufferedImage image) {
        Mockito.doReturn(true)
                .when(imageService)
                .imageContainsCat(Mockito.any(BufferedImage.class), Mockito.anyFloat());
        securityService.setImage(image);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        Mockito.verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
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
