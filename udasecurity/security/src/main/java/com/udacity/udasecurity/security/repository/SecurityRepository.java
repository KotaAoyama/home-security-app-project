package com.udacity.udasecurity.security.repository;

import com.udacity.udasecurity.security.data.AlarmStatus;
import com.udacity.udasecurity.security.data.ArmingStatus;
import com.udacity.udasecurity.security.data.Sensor;

import java.util.Set;

/**
 * Interface showing the methods our security repository will need to support
 */
public interface SecurityRepository {
    boolean isAnySensorActive();
    void addSensor(Sensor sensor);
    void removeSensor(Sensor sensor);
    void updateSensor(Sensor sensor);
    void setAlarmStatus(AlarmStatus alarmStatus);
    void setArmingStatus(ArmingStatus armingStatus);
    Set<Sensor> getSensors();
    AlarmStatus getAlarmStatus();
    ArmingStatus getArmingStatus();


}
