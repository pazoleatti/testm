package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

/**
 * User: avanteev
 */
public class BlobDataServiceImplTest {

    private static BlobDataService blobDataService;

    @BeforeClass
    public static void setUp() throws FileNotFoundException {
        //blobDataService = new BlobDataServiceImpl();
        URL url = Thread.currentThread().getContextClassLoader().getResource(ClassUtils.classPackageAsResourcePath(BlobDataServiceImplTest.class));
        File file = new File(url.getPath() + "/BlobDataServiceImplTest.class");
        System.out.println(url.getPath());

        BlobData blobData = new BlobData();
        blobData.setName("home");
        blobData.setInputStream(new FileInputStream(file));
        blobData.setCreationDate(new Date());
        blobData.setUuid(UUID.randomUUID().toString().toLowerCase());
        blobData.setType(1);
    }

    @Test
    public void test(){
        BlobDataDao blobDataDao = Mockito.mock(BlobDataDao.class);
    }
}
