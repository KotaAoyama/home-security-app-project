package com.udacity.udasecurity.security.application;

import com.udacity.udasecurity.security.data.AlarmStatus;

public class FakePanel implements StatusListener {
    @Override
    public void notify(AlarmStatus status) {

    }

    @Override
    public void catDetected(boolean catDetected) {

    }

    @Override
    public void sensorStatusChanged() {

    }
}
