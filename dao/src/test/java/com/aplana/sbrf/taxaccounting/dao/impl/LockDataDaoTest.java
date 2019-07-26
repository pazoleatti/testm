package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LockDataDao;
import com.aplana.sbrf.taxaccounting.model.LockData;
import com.aplana.sbrf.taxaccounting.model.LockDataDTO;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.LockException;
import com.google.common.collect.HashMultiset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static java.util.Arrays.asList;


@RunWith(SpringRunner.class)
@ContextConfiguration({"LockDataDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LockDataDaoTest {

    @Autowired
    private LockDataDao dao;
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Test
    public void test_get_onExistentLock() {
        LockData data = dao.findByKey("a");
        assertThat(data)
                .isNotNull()
                .extracting("key", "userId")
                .containsExactly("a", 0);
    }

    @Test
    public void test_get_onNonexistentLock() {
        LockData data = dao.findByKey("c");
        assertThat(data).isNull();
    }

    @Test
    public void test_existsByKey_forExistentLock() {
        boolean result = dao.existsByKey("a");
        assertThat(result).isTrue();
    }

    @Test
    public void test_existsByKey_forNonexistentLock() {
        boolean result = dao.existsByKey("z");
        assertThat(result).isFalse();
    }

    @Test
    public void test_existsByKeyAndUserId_matching() {
        boolean result = dao.existsByKeyAndUserId("a", 0);
        assertThat(result).isTrue();
    }

    @Test
    public void test_existsByKeyAndUserId_matchingByKeyOnly() {
        boolean result = dao.existsByKeyAndUserId("a", 1);
        assertThat(result).isFalse();
    }

    @Test
    public void test_existsByKeyAndUserId_forNonexistentKey() {
        boolean result = dao.existsByKeyAndUserId("z", 0);
        assertThat(result).isFalse();
    }

    @Test
    public void test_lock_nonexistent() {
        dao.lock("c", 0, "");
        LockData data = dao.findByKey("c");
        assertThat(data)
                .isNotNull()
                .extracting("key", "userId")
                .containsExactly("c", 0);
    }

    @Test(expected = LockException.class)
    public void test_lock_existent() {
        dao.lock("a", 0, ""); // дубликат
    }

    @Test
    public void test_getLocks() {
        PagingParams paging = new PagingParams();

        PagingResult<LockDataDTO> data = dao.getLocks("a", paging);
        assertThat(data).hasSize(4);

        data = dao.getLocks("", paging);
        assertThat(data).hasSize(5);

        data = dao.getLocks(null, paging);
        assertThat(data).hasSize(5);

        data = dao.getLocks("not exists", paging);
        assertThat(data).hasSize(0);
    }

    @Test
    public void test_getLockIfOlderThan() throws InterruptedException {
        Assert.assertEquals(1, unlockIfOlderThan(1));
        Thread.sleep(2000);
        Assert.assertEquals(4, unlockIfOlderThan(1));
        // создаем новую блокировку
        dao.lock("test_key", 1, "test_description");
        Thread.sleep(1000);
        Assert.assertEquals(0, unlockIfOlderThan(2));
        Thread.sleep(2000);
        Assert.assertEquals(1, unlockIfOlderThan(2));
    }

    // Считаем, сколько блокировок старше указанного времени и снимаем их
    private int unlockIfOlderThan(long seconds) {
        List<String> keyList = dao.getLockIfOlderThan(seconds);
        for (String key : keyList) {
            dao.unlock(key);
        }
        return keyList.size();
    }

    @Test
    public void checkExceptions() {
        dropTable();
        try {
            unlockIfOlderThan(0);
        } catch (LockException e) {
            Assert.assertTrue(e.getMessage().startsWith("Ошибка при удалении"));
        }
        try {
            dao.unlockAllByUserId(0, false);
        } catch (LockException e) {
            Assert.assertTrue(e.getMessage().startsWith("Ошибка при удалении блокировок для пользователя"));
        }
    }

    /**
     * Метод предназначен для проверки обработки исключительных ситуаций
     */
    private void dropTable() {
        namedParameterJdbcTemplate.update("DROP TABLE lock_data", new HashMap<String, Object>());
    }

    @Test
    public void test_unlockAllByUserId() {
        dao.unlockAllByUserId(0, false);
        PagingResult<LockDataDTO> locks = dao.getLocks("", new PagingParams());
        System.out.println(locks);
        Assert.assertEquals(2, locks.size());

        dropTable();
        dao.unlockAllByUserId(0, true);
    }

    @Test
    public void test_fetchAllByKeySet() {
        List<LockData> locks = dao.fetchAllByKeySet(new HashSet<>(asList("b", "aaa")));
        Assert.assertEquals(2, locks.size());
        Assert.assertEquals(HashMultiset.create(asList(2L, 4L)), HashMultiset.create(asList(locks.get(0).getId(), locks.get(1).getId())));
    }

    @Test
    public void test_lockKeysBatch() {
        Map<String, String> locks = new HashMap<>();
        locks.put("lock_key1", "description1");
        locks.put("lock_key2", "description2");

        dao.lockKeysBatch(locks, 0);

        dao.lock("c", 0, "");
        LockData data = dao.findByKey("lock_key1");
        assertThat(data).isNotNull();
        assertThat(data.getKey()).isEqualTo("lock_key1");
        assertThat(data.getUserId()).isEqualTo(0);
        assertThat(data.getDescription()).isEqualTo("description1");

        data = dao.findByKey("lock_key2");
        assertThat(data).isNotNull();
        assertThat(data.getKey()).isEqualTo("lock_key2");
        assertThat(data.getUserId()).isEqualTo(0);
        assertThat(data.getDescription()).isEqualTo("description2");
    }

    @Test
    public void test_bindTaskToMultiKeys() {
        List<String> keys = Arrays.asList("a", "b", "q");

        dao.bindTaskToMultiKeys(keys, 1L);

        LockData data = dao.findByKey("a");
        assertThat(data.getTaskId()).isEqualTo(1L);
        data = dao.findByKey("b");
        assertThat(data.getTaskId()).isEqualTo(1L);
        data = dao.findByKey("q");
        assertThat(data.getTaskId()).isEqualTo(1L);
    }

    @Test
    public void test_unlockMultipleTasks() {
        List<String> keys = Arrays.asList("a", "b", "q");

        dao.unlockMultipleTasks(keys);

        LockData data = dao.findByKey("a");
        assertThat(data).isNull();
        data = dao.findByKey("b");
        assertThat(data).isNull();
        data = dao.findByKey("q");
        assertThat(data).isNull();
    }
}
