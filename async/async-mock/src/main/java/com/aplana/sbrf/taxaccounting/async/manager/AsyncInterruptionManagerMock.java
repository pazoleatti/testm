package com.aplana.sbrf.taxaccounting.async.manager;

import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class AsyncInterruptionManagerMock implements AsyncInterruptionManager {
    @Override
    public void addTask(String key, Thread task) {

    }

    @Override
    public void interruptAll(Collection<String> keys) {

    }

    @Override
    public void interruptAll() {

    }
}
