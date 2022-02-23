package com.udacity.udasecurity.security.service;

import com.udacity.udasecurity.security.data.*;
import com.udacity.udasecurity.image.service.ImageService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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
    public void givenAlarmArmed_whenSensorActivated_getPendingAlarmStatus(Sensor sensor) {
        securityService.addSensor(sensor);
        Mockito.when(securityRepository.isAnySensorActive()).thenReturn(true);
        Mockito.when(securityRepository.getArmingStatus()).thenReturn(
                ArmingStatus.ARMED_HOME, ArmingStatus.ARMED_AWAY);

        Assertions.assertAll(
                () -> Assertions.assertTrue(
                        securityService.isAnySensorActive()
                        && securityService.getArmingStatus() == ArmingStatus.ARMED_HOME
                        && securityService.getAlarmStatus() == AlarmStatus.PENDING_ALARM),
                () ->  Assertions.assertTrue(
                        securityService.isAnySensorActive()
                                && securityService.getArmingStatus() == ArmingStatus.ARMED_AWAY
                                && securityService.getAlarmStatus() == AlarmStatus.PENDING_ALARM)
        );

        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.getAlarmStatus());
    }

    private static Stream<Arguments> differentSensorType() {
        return Stream.of(
                Arguments.of(
                        new Sensor("sensorDoor", SensorType.DOOR),
                        new Sensor("sensorMotion", SensorType.MOTION),
                        new Sensor("sensorWindow", SensorType.WINDOW)
                )
        );
    }
}
