package com.aplana.sbrf.taxaccounting.service.impl.component.lock;

import com.aplana.sbrf.taxaccounting.model.OperationType;
import com.aplana.sbrf.taxaccounting.service.component.lock.LockKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Генерирует ключи блокировок для проверок на наличие мешающих блокировок
 */
@Component("checkupLockKeyGeneratorImpl")
public class CheckupLockKeyGeneratorImpl implements LockKeyGenerator {

    private LockKeyGenerator mainLockKeyGeneratorImpl;

    @Autowired
    public CheckupLockKeyGeneratorImpl(@Qualifier("mainLockKeyGeneratorImpl") LockKeyGenerator mainLockKeyGeneratorImpl) {
        this.mainLockKeyGeneratorImpl = mainLockKeyGeneratorImpl;
    }

    @Override
    public String generateLockKey(Map<String, Long> idTokens, OperationType operationType) {
        Long declarationDataId = idTokens.get("declarationDataId");
        if (!operationType.equals(OperationType.TRANSFER)) {
            return mainLockKeyGeneratorImpl.generateLockKey(idTokens, operationType);
        } else {
            return String.format("DECLARATION_DATA_%s_TRANSFER", declarationDataId);
        }
    }
}
