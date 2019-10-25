package com.aplana.sbrf.taxaccounting.service.component.lock.locker;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.mock.TransactionHelperStub;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.DeclarationTemplateService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.component.lock.LockKeyGenerator;
import com.aplana.sbrf.taxaccounting.service.component.lock.descriptor.DeclarationDataKeyLockDescriptor;
import com.aplana.sbrf.taxaccounting.service.impl.component.lock.CheckupLockKeyGeneratorImpl;
import com.aplana.sbrf.taxaccounting.service.impl.component.lock.MainLockKeyGeneratorImpl;
import com.aplana.sbrf.taxaccounting.service.impl.component.lock.locker.DeclarationLockerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.service.component.lock.locker.LocksRelations.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Mockito.*;

/**
 * Тестирование взаимосвязи блокировок в методе {@link DeclarationLocker#establishLock(Long, OperationType, TAUserInfo, Logger)}
 */
@RunWith(Parameterized.class)

public class CrossBlockingTest {

    // Тестируемый класс
    private DeclarationLocker declarationLocker;

    // Зависимости
    @Mock
    private DeclarationDataKeyLockDescriptor lockDescriptionGenerator;
    @Mock
    private LockDataDao lockDataDao;
    @Mock
    private DeclarationDataService declarationDataService;
    @Mock
    private DeclarationTemplateService declarationTemplateService;
    @Mock
    private TAUserService taUserService;


    private static final int USER_ID = 5;
    private static final int TEMPLATE_ID = 10;
    private TAUserInfo userInfo;
    private Logger logger = new Logger();
    private String lockToSet;
    private String lockWasInDB;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        when(lockDescriptionGenerator.createKeyLockDescription(anyLong(), any(OperationType.class)))
                .thenReturn("Test description");
        when(declarationDataService.get(anyListOf(Long.class)))
                .thenReturn(Collections.singletonList(createTestDeclaration()));
        when(declarationTemplateService.get(TEMPLATE_ID))
                .thenReturn(createTestTemplate());
        TransactionHelper tx = new TransactionHelperStub();
        // Генератор ключей берём реальный, заодно проверяем и его.
        LockKeyGenerator mainLockKeyGenerator = new MainLockKeyGeneratorImpl();
        LockKeyGenerator checkupLockKeyGenerator = new CheckupLockKeyGeneratorImpl(mainLockKeyGenerator);
        // Создаем тестовый объект с зависимостями.
        declarationLocker = new DeclarationLockerImpl(
                mainLockKeyGenerator,
                checkupLockKeyGenerator,
                lockDescriptionGenerator,
                lockDataDao,
                declarationDataService,
                declarationTemplateService,
                taUserService,
                tx
        );

        // Пользователь-пустышка
        userInfo = new TAUserInfo();
        TAUser user = new TAUser();
        user.setId(USER_ID);
        userInfo.setUser(user);
    }

    /**
     * Параметры запуска каждого теста - две блокировки.
     */
    @Parameters
    public static Collection<Object[]> parameters() {
        List<Object[]> lockPairs = new ArrayList<>();

        for (String firstLock : ALL_LOCKS) {
            for (String secondLock : ALL_LOCKS) {
                String[] lockPair = {firstLock, secondLock};
                lockPairs.add(lockPair);
            }
        }

        return lockPairs;
    }

    // Сюда на каждой итерации попадает пара из @Parameters
    public CrossBlockingTest(String firstLock, String secondLock) {
        this.lockToSet = firstLock;
        this.lockWasInDB = secondLock;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void locksCrossTest() {

        // Выдаем нужные результаты при запросах блокировок из базы.
        // Аргументом передается коллекция ключей, которые нужно вытащить,
        // если среди них есть наша lockWasInDB, возвращаем LockData с ней.
        // Второй запрос будет только в случае успешной блокировки, там возвращаем новую блокировку
        when(lockDataDao.fetchAllByKeyPrefixSet((Collection<String>) argThat(hasItem(lockWasInDB))))
                .thenReturn(Collections.singletonList(new LockData(lockWasInDB, USER_ID)))
                .thenReturn(Collections.singletonList(new LockData(lockToSet, USER_ID)));

        // Если нашей блокировки среди запрашиваемых нет, возвращаем пустой лист
        when(lockDataDao.fetchAllByKeyPrefixSet((Collection<String>) argThat(not(hasItem(lockWasInDB)))))
                .thenReturn(Collections.<LockData>emptyList())
                .thenReturn(Collections.singletonList(new LockData(lockToSet, USER_ID)));

        when(lockDataDao.fetchAllByKeySet((Collection<String>) argThat(hasItem(lockWasInDB))))
                .thenReturn(Collections.singletonList(new LockData(lockWasInDB, USER_ID)))
                .thenReturn(Collections.singletonList(new LockData(lockToSet, USER_ID)));

        // Если нашей блокировки среди запрашиваемых нет, возвращаем пустой лист
        when(lockDataDao.fetchAllByKeySet((Collection<String>) argThat(not(hasItem(lockWasInDB)))))
                .thenReturn(Collections.<LockData>emptyList())
                .thenReturn(Collections.singletonList(new LockData(lockToSet, USER_ID)));

        // Операция, которую нам нужно запустить, чтобы получить нужную попытку блокировки
        OperationType operation = OPERATION_BY_LOCK.get(lockToSet);

        // Вызываем тестируемый метод
        LockData result = declarationLocker.establishLock(DECLARATION_ID, operation, userInfo, logger);

        // Если блокировка в базе среди тех, которые должны блокировать нашу, ожидаем сообщение о неудаче
        List<String> conflictingLocks = CONFLICTING_LOCKS.get(lockToSet);
        if (lockWasInDB.equals(lockToSet) || conflictingLocks.contains(lockWasInDB)) {
            assertLockConflict(result);
        } else {
            assertNoConflict(result);
        }
    }

    private void assertLockConflict(LockData result) {
        assertThat(result)
                .as("Уже установленная блокировка %s должна не давать поставить блокировку %s", lockWasInDB, lockToSet)
                .isNull();
        assertThat(logger.getEntries()).hasSize(1);
        assertThat(logger.getEntries().get(0).getMessage()).contains("заблокирована");
    }

    private void assertNoConflict(LockData result) {
        assertThat(result)
                .as("Установленная блокировка %s не должна мешать поставить блокировку %s", lockWasInDB, lockToSet)
                .isNotNull();
        assertThat(logger.getEntries()).isEmpty();
    }

    // <editor-fold defaultstate="collapsed" desc="Private initialization methods">

    private DeclarationData createTestDeclaration() {
        DeclarationData declaration = new DeclarationData();
        declaration.setId(DECLARATION_ID);
        declaration.setDeclarationTemplateId(TEMPLATE_ID);
        return declaration;
    }

    private DeclarationTemplate createTestTemplate() {
        DeclarationType declarationType = new DeclarationType();
        declarationType.setName("Тестовая форма");
        DeclarationTemplate template = new DeclarationTemplate();
        template.setId(TEMPLATE_ID);
        template.setType(declarationType);
        template.setDeclarationFormKind(DeclarationFormKind.PRIMARY);
        return template;
    }
    // </editor-fold>
}
