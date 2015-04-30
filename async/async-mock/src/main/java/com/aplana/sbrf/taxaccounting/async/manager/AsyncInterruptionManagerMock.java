package com.aplana.sbrf.taxaccounting.async.manager;

import org.springframework.stereotype.Service;

@Service
public class AsyncInterruptionManagerMock implements AsyncInterruptionManager {
    @Override
    public void addTask(String key, Thread task) {

    }

    @Override
    public void interruptAsyncTask(String key) {

    }

    @Override
    public void interruptAll() {

    }
}
