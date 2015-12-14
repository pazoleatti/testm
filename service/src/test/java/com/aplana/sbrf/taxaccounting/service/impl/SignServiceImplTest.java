package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParamModel;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.SignService;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import static org.mockito.Mockito.when;

public class SignServiceImplTest {

    private SignService signService;
	private ConfigurationDao configurationDao;

    private final String COMMON = ClassUtils.classPackageAsResourcePath(getClass()) + "/sign/";
    private final String DATA_FILE =  COMMON + "/dt/5101309D.rnu";
    private final String DATA_XLS_FILE = COMMON + "/dt/260220AD.xls";
    private final String NON_ENC_FILE = COMMON + "/dt/non_enc";
    private final String ZIP_FILE = COMMON + "/dt/RNU00001.64";
    private final String BICR4_DLL = COMMON + "dll/";

    @Before
    public void init() throws IOException {
		final URL bicrUrl = Thread.currentThread().getContextClassLoader().getResource(BICR4_DLL);
        assert bicrUrl != null;

        final URL signFile = Thread.currentThread().getContextClassLoader().getResource(COMMON+"sign.dat");
        assert signFile != null;
        final URL signDir = Thread.currentThread().getContextClassLoader().getResource(COMMON);
        assert signDir != null;

        ConfigurationParamModel model = new ConfigurationParamModel();
        ArrayList<String> keys = new ArrayList<String>(){{
            add(signFile.getProtocol() + "://" + signFile.getPath().substring(1));
            add(signDir.getProtocol() + "://" + signDir.getPath().substring(1));
        }};
        model.put(ConfigurationParam.KEY_FILE, 0, keys);
        ArrayList<String> dlls = new ArrayList<String>(){{add(bicrUrl.getProtocol() + "://" + bicrUrl.getPath().substring(1));}};
        model.put(ConfigurationParam.ENCRYPT_DLL, 0, dlls);

		configurationDao = Mockito.mock(ConfigurationDao.class);
        when(configurationDao.getAll()).thenReturn(model);

		signService = new SignServiceImpl();
		ReflectionTestUtils.setField(signService, "configurationDao", configurationDao);

		Department department = new Department();
		department.setId(0);
		DepartmentServiceImpl departmentService = Mockito.mock(DepartmentServiceImpl.class);
		when(departmentService.getBankDepartment()).thenReturn(department);
		ReflectionTestUtils.setField(signService, "departmentService", departmentService);
    }

    @Test
    public void testRNU() throws IOException, URISyntaxException {
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;
        /*try {
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[])field.get(null);
            for (String path : paths) {
                if (FULL_PATH_DLL.equals(path)) {
                    break;
                }
            }
            String[] tmp = new String[paths.length+1];
            System.arraycopy(paths,0,tmp,0,paths.length);
            tmp[paths.length] = FULL_PATH_DLL;
            field.set(null,tmp);
            System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + FULL_PATH_DLL);
        } catch (IllegalAccessException e) {
            throw new ServiceException("Failed to get permissions to set library path");
        } catch (NoSuchFieldException e) {
            throw new ServiceException("Failed to get field handle to set library path");
        }*/
        Logger logger = new Logger();
        signService.checkSign(copyTmp(DATA_FILE, "dataFile", ".data").getAbsolutePath(), 0, logger);
        Assert.assertTrue(signService.checkSign(copyTmp(DATA_FILE, "dataFile", ".data").getAbsolutePath(), 0, new Logger()));

    }

    @Test
    public void testXls() throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;
        Assert.assertTrue(signService.checkSign(copyTmp(DATA_XLS_FILE, "dataXlsFile", ".data").getAbsolutePath(), 0, new Logger()));
    }

    @Test
    public void testNonEncrypt() throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;
        Assert.assertFalse(signService.checkSign(copyTmp(NON_ENC_FILE, "non_enc", ".data").getAbsolutePath(), 0, new Logger()));
    }

    //Тест с новым подписанным архивом. False, нормальный результат, так как нет такой подписи в нашей БОК
    @Test
    public void testZip() throws IOException {
        if (System.getProperty("os.name").toLowerCase().contains("linux"))
            return;
        Assert.assertFalse(signService.checkSign(copyTmp(ZIP_FILE, "zip", ".data").getAbsolutePath(), 0, new Logger()));
    }

    private static File copyTmp(String dataFilePath, String prefix, String suffix) throws IOException {
        InputStream dataIn = Thread.currentThread().getContextClassLoader().getResourceAsStream(dataFilePath);
        File dataFile = File.createTempFile(prefix,suffix);
        FileOutputStream dataOut = new FileOutputStream(dataFile);
		try {
	        IOUtils.copy(dataIn, dataOut);
		} finally {
			IOUtils.closeQuietly(dataIn);
			IOUtils.closeQuietly(dataOut);
			dataFile.deleteOnExit();
		}
        return dataFile;
    }
}
