package com.aplana.sbrf.taxaccounting.service.component.lock.locker;

import com.aplana.sbrf.taxaccounting.model.OperationType;
import org.junit.Ignore;
import org.junit.Test;

import static com.aplana.sbrf.taxaccounting.service.component.lock.locker.LocksRelations.*;
import static com.aplana.sbrf.taxaccounting.service.component.lock.locker.LocksRelations.LOCKS_COUNT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверка правильности заполнения класса {@link LocksRelations}
 */
@Ignore
public class LocksRelationsFillingTest {

    @Test
    public void allLocksAreCounted() {
        assertThat(ALL_LOCKS)
                .as("Число блокировок в массиве ALL_LOCKS должно соответствовать заявленному LOCKS_COUNT = %d", LOCKS_COUNT)
                .hasSize(LOCKS_COUNT);
    }

    @Test
    @Ignore
    public void allOperationsAreCounted() {
        assertThat(OperationType.values())
                .as("Число типов блокировок в OperationType отличается от тестируемых %d", LOCKS_COUNT)
                .hasSize(LOCKS_COUNT);
    }

    @Test
    public void allLocksAreMappedToOperations() {
        assertThat(OPERATION_BY_LOCK)
                .as("Для всех %d блокировок должно быть заполнено соответствие операциям OPERATION_BY_LOCK", LOCKS_COUNT)
                .hasSize(LOCKS_COUNT);
    }

    @Test
    public void allConflictsAreMapped() {
        assertThat(CONFLICTING_LOCKS)
                .as("Для всех %d блокировок должны быть указаны конфликтующие блокировки CONFLICTING_LOCKS", LOCKS_COUNT)
                .hasSize(LOCKS_COUNT);
    }
}
