package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentConfigDao;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookOktmo;
import com.aplana.sbrf.taxaccounting.service.impl.refbook.DepartmentConfigServiceImpl;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class DepartmentConfigServiceTest {

    @InjectMocks
    @Spy
    private DepartmentConfigServiceImpl departmentConfigService;
    @Mock
    private DepartmentConfigDao departmentConfigDao;
    @Mock
    private Logger logger;

    private List<DepartmentConfig> departmentConfigs;

    @Before
    public void before() {
        departmentConfigs = new ArrayList<>(Arrays.asList(
                new DepartmentConfig()
                        .id(0L).kpp("111").oktmo(new RefBookOktmo().code("111"))
                        .startDate(newDate(1, 1, 2018))
                        .endDate(newDate(31, 12, 2018))
                        .department(new RefBookDepartment().id(1).name("DEP1")),
                new DepartmentConfig()
                        .id(1L).kpp("111").oktmo(new RefBookOktmo().code("111"))
                        .startDate(newDate(1, 1, 2019))
                        .endDate(newDate(31, 12, 2019))
                        .department(new RefBookDepartment().id(1).name("DEP1")),
                new DepartmentConfig()
                        .id(2L).kpp("111").oktmo(new RefBookOktmo().code("111"))
                        .startDate(newDate(1, 1, 2020))
                        .endDate(null)
                        .department(new RefBookDepartment().id(1).name("DEP1"))
        ));
        MockitoAnnotations.initMocks(this);
        when(departmentConfigDao.findAllByKppAndOktmo(anyString(), anyString())).thenAnswer(new Answer<List<DepartmentConfig>>() {
            @Override
            public List<DepartmentConfig> answer(InvocationOnMock invocation) {
                return findByKppOktmo((String) invocation.getArguments()[0], (String) invocation.getArguments()[1]);
            }
        });
        when(departmentConfigDao.findById(anyLong())).thenAnswer(new Answer<DepartmentConfig>() {
            @Override
            public DepartmentConfig answer(InvocationOnMock invocation) {
                return findById(((Long) invocation.getArguments()[0]).intValue());
            }
        });
        when(departmentConfigDao.findPrevById(anyLong())).thenAnswer(new Answer<DepartmentConfig>() {
            @Override
            public DepartmentConfig answer(InvocationOnMock invocation) {
                DepartmentConfig departmentConfig = findById(((Long) invocation.getArguments()[0]).intValue());
                List<DepartmentConfig> departmentConfigs = findByKppOktmo(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode());
                if (departmentConfigs.indexOf(departmentConfig) > 0) {
                    return departmentConfigs.get(departmentConfigs.indexOf(departmentConfig) - 1);
                }
                return null;
            }
        });
        when(departmentConfigDao.findNextById(anyLong())).thenAnswer(new Answer<DepartmentConfig>() {
            @Override
            public DepartmentConfig answer(InvocationOnMock invocation) {
                DepartmentConfig departmentConfig = findById(((Long) invocation.getArguments()[0]).intValue());
                List<DepartmentConfig> departmentConfigs = findByKppOktmo(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode());
                if (departmentConfigs.indexOf(departmentConfig) < departmentConfigs.size() - 1) {
                    return departmentConfigs.get(departmentConfigs.indexOf(departmentConfig) + 1);
                }
                return null;
            }
        });
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                DepartmentConfig departmentConfig = (DepartmentConfig) invocation.getArguments()[0];
                departmentConfig.setId((long) departmentConfigs.size());
                departmentConfigs.add(departmentConfig);
                return null;
            }
        }).when(departmentConfigDao).create(any(DepartmentConfig.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                DepartmentConfig departmentConfig = (DepartmentConfig) invocation.getArguments()[0];
                DepartmentConfig beforeUpdate = findById(departmentConfig.getId());
                departmentConfigs.set(departmentConfigs.indexOf(beforeUpdate), departmentConfig);
                return null;
            }
        }).when(departmentConfigDao).update(any(DepartmentConfig.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                long id = (long) invocation.getArguments()[0];
                DepartmentConfig departmentConfig = findById(id);
                departmentConfigs.remove(departmentConfig);
                return null;
            }
        }).when(departmentConfigDao).deleteById(anyLong());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                long id = (long) invocation.getArguments()[0];
                DepartmentConfig departmentConfig = findById(id);
                departmentConfig.setEndDate((Date) invocation.getArguments()[1]);
                return null;
            }
        }).when(departmentConfigDao).updateEndDate(anyLong(), any(Date.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                long id = (long) invocation.getArguments()[0];
                DepartmentConfig departmentConfig = findById(id);
                departmentConfig.setStartDate((Date) invocation.getArguments()[1]);
                return null;
            }
        }).when(departmentConfigDao).updateStartDate(anyLong(), any(Date.class));
    }

    // Не создаёт разрыв перед существующими настройками
    @Test
    public void createBeforeOk() {
        departmentConfigService.create(new DepartmentConfig()
                .kpp("111").oktmo(new RefBookOktmo().code("111"))
                .startDate(newDate(1, 1, 2017))
                .endDate(newDate(31, 12, 2017))
                .department(new RefBookDepartment().id(1).name("DEP1")), new Logger());
        check();
    }

    // Не создаёт разрыв после существующих настроек
    @Test
    public void createAfterOk() {
        // сначала нужно закрыть последнюю версию!?
        departmentConfigs.get(departmentConfigs.size() - 1).endDate(newDate(31, 12, 2020));

        departmentConfigService.create(new DepartmentConfig()
                .kpp("111").oktmo(new RefBookOktmo().code("111"))
                .startDate(newDate(1, 1, 2021))
                .endDate(null)
                .department(new RefBookDepartment().id(1).name("DEP1")), new Logger());
        check();
    }

    // Пересекается с существующими настройками
    @Test(expected = ServiceException.class)
    public void createOverlapping() {
        departmentConfigService.create(new DepartmentConfig()
                .id(999L).kpp("111").oktmo(new RefBookOktmo().code("111"))
                .startDate(newDate(1, 1, 2016))
                .endDate(newDate(2, 2, 2018))
                .department(new RefBookDepartment().id(1).name("DEP1")), new Logger());
    }

    // Создаёт разрыв перед существующими настройками
    @Test(expected = ServiceException.class)
    public void createWithGap() {
        departmentConfigService.create(new DepartmentConfig()
                .id(999L).kpp("111").oktmo(new RefBookOktmo().code("111"))
                .startDate(newDate(1, 1, 2016))
                .endDate(newDate(29, 12, 2017))
                .department(new RefBookDepartment().id(1).name("DEP1")), new Logger());
    }

    // Создаёт разрыв после существующих настроек
    @Test(expected = ServiceException.class)
    public void checkDepartmentConfigTestAfterFail() {
        // сначала нужно закрыть последнюю версию!?
        departmentConfigs.get(departmentConfigs.size() - 1).endDate(newDate(31, 12, 2020));

        departmentConfigService.create(new DepartmentConfig()
                .id(999L).kpp("111").oktmo(new RefBookOktmo().code("111"))
                .startDate(newDate(2, 1, 2021))
                .endDate(null)
                .department(new RefBookDepartment().id(1).name("DEP1")), new Logger());
    }

    // Можем изменять КПП/ОКТМО у первой версии
    @Test
    public void updateKppOfFirstVersion() {
        departmentConfigService.update(departmentConfigs.get(0).toBuilder().kpp("222").build(), logger);
        check();
    }

    // Можем изменять КПП/ОКТМО у средней версии
    @Test
    public void updateKppOfMiddleVersion() {
        departmentConfigService.update(departmentConfigs.get(1).toBuilder().kpp("222").build(), logger);
        check();
    }

    // Можем изменять любое поле кроме КПП/ОКТМО у средней версии
    @Test
    public void updateOfMiddleVersion() {
        departmentConfigService.update(departmentConfigs.get(1).toBuilder().approveDocName("approveDocName").build(), logger);
        check();
    }

    // Можем изменять у средней версии дату начала вперед
    @Test
    public void updateStartDateOfMiddleVersionOk() {
        departmentConfigService.update(departmentConfigs.get(1).toBuilder().startDate(newDate(1, 2, 2019)).build(), logger);
        check();
    }

    // Можем изменять у средней версии дату окончания назад
    @Test
    public void updateEndDateOfMiddleVersionOk() {
        departmentConfigService.update(departmentConfigs.get(1).toBuilder().endDate(newDate(31, 10, 2019)).build(), logger);
        check();
    }

    // Не можем изменять у средней версии дату начала назад, будет пересечение!?
    @Test(expected = ServiceException.class)
    public void updateStartDateOfMiddleVersionFail() {
        departmentConfigService.update(departmentConfigs.get(1).toBuilder().startDate(newDate(1, 12, 2018)).build(), logger);
    }

    // Не можем изменять у средней версии дату окончания вперед, будет пересечение!?
    @Test(expected = ServiceException.class)
    public void updateEndDateOfMiddleVersionFail() {
        departmentConfigService.update(departmentConfigs.get(1).toBuilder().startDate(newDate(31, 1, 2020)).build(), logger);
    }

    // Удаление последней версии
    @Test
    public void deleteLast() {
        departmentConfigService.delete(departmentConfigs.get(2), logger);
        check();
    }

    // Удаление последней версии
    @Test
    public void deleteMiddle() {
        departmentConfigService.delete(departmentConfigs.get(1), logger);
        check();
    }

    private DepartmentConfig findById(long id) {
        for (DepartmentConfig departmentConfig : departmentConfigs) {
            if (departmentConfig.getId().equals(id)) {
                return departmentConfig;
            }
        }
        throw new IllegalArgumentException();
    }

    private List<DepartmentConfig> findByKppOktmo(String kpp, String oktmo) {
        List<DepartmentConfig> result = new ArrayList<>();
        for (DepartmentConfig departmentConfig : departmentConfigs) {
            if (departmentConfig.getKpp().equals(kpp) && departmentConfig.getOktmo().getCode().equals(oktmo)) {
                result.add(departmentConfig);
            }
        }
        Collections.sort(result, comparator);
        return result;
    }

    /**
     * Проверяет что не возникло разрывов
     */
    private void check() {
        Set<KppOktmoPair> keys = new HashSet<>();
        for (DepartmentConfig departmentConfig : departmentConfigs) {
            KppOktmoPair key = new KppOktmoPair(departmentConfig.getKpp(), departmentConfig.getOktmo().getCode());
            if (!keys.contains(key)) {
                check(findByKppOktmo(key.getKpp(), key.getOktmo()));
            }
            keys.add(key);
        }
    }

    private void check(List<DepartmentConfig> departmentConfigs) {
        DepartmentConfig prev = null;
        for (DepartmentConfig departmentConfig : departmentConfigs) {
            if (prev != null) {
                assertThat(prev.getEndDate(), equalTo(DateUtils.addDays(departmentConfig.getStartDate(), -1)));
            }
            prev = departmentConfig;
        }
    }

    private Date newDate(int day, int month, int year) {
        return new LocalDate(year, month, day).toDate();
    }

    private static final Comparator<DepartmentConfig> comparator = new Comparator<DepartmentConfig>() {
        @Override
        public int compare(DepartmentConfig o1, DepartmentConfig o2) {
            int r = o1.getKpp().compareTo(o2.getKpp());
            if (r == 0) {
                r = o1.getOktmo().getCode().compareTo(o2.getOktmo().getCode());
            }
            if (r == 0) {
                r = o1.getStartDate().compareTo(o2.getStartDate());
            }
            return r;
        }
    };
}
