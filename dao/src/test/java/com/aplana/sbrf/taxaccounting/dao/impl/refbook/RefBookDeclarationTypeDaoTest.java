package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"RefBookDeclarationTypeDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class RefBookDeclarationTypeDaoTest {
    private final static int DECLARATION_TYPES_COUNT = 2;
    private final static String DECLARATION_TYPE_NAME_0 = "Вид налоговой формы 2";
    private final static String DECLARATION_TYPE_NAME_1 = "Вид налоговой формы 3";

    @Autowired
    private RefBookDeclarationTypeDao declarationTypeDao;

    @Test
    public void getRecordData() {
        List<RefBookDeclarationType> declarationTypes = declarationTypeDao.fetchAll();
        assertTrue(declarationTypes.size() == DECLARATION_TYPES_COUNT);
        assertTrue(declarationTypes.get(0).getName().equals(DECLARATION_TYPE_NAME_0));
        assertTrue(declarationTypes.get(1).getName().equals(DECLARATION_TYPE_NAME_1));
    }
}
