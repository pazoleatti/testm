package com.aplana.sbrf.taxaccounting.service.impl.component.lock;

import com.aplana.sbrf.taxaccounting.service.component.lock.BaseLockKeyGenerator;
import org.springframework.stereotype.Component;

@Component
public class BaseLockKeyGeneratorImpl implements BaseLockKeyGenerator{
    @Override
    public String generatePersonsRegistryLockKey() {
        return "PERSONS_REGISTRY";
    }
}
