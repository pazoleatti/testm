package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.impl.refbook.FiasRefBookDaoImpl;
import com.aplana.sbrf.taxaccounting.script.service.ImportFiasDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Сервис импорта данных в справочники ФИАС
 *
 * @author Andrey Drunk
 */
@Service("importFiasDataService")
public class ImportFiasDataServiceImpl implements ImportFiasDataService {

    @Autowired
    FiasRefBookDaoImpl fiasRefBookDao;

    @Override
    public void insertRecords(String table, List<Map<String, Object>> records) {
        fiasRefBookDao.insertRecordsBatch(table, records);
    }

    @Override
    public void beforeImport() {
        fiasRefBookDao.beforeImport();
    }

    @Override
    public void afterImport() {
        fiasRefBookDao.afterImport();
    }

}
