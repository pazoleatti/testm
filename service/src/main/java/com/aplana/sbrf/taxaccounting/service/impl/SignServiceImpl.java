package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.api.ConfigurationDao;
import com.aplana.sbrf.taxaccounting.model.ConfigurationParam;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.SignService;
import com.aplana.sbrf.taxaccounting.utils.FileWrapper;
import com.aplana.sbrf.taxaccounting.utils.ResourceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.infocrypt.bicrypt.Bicr4;

import java.util.List;

@Service
public class SignServiceImpl implements SignService {

    @Autowired
    ConfigurationDao configurationDao;

    private static final Log logger = LogFactory.getLog(SignServiceImpl.class);

    // 128 означает, что инициализации ДСЧ не будет
    private static final int FLAG_TM = 128;

    // не используем главный ключ и узел замены
    private static final String FILE_GK = "";
    private static final String FILE_UZ = "";

    private static final int COM_LEN = 0;
    private static final byte TM_NUMBER[] = new byte[32];
    private static final int TMN_BLEN[] = new int[1];

    static {
        TMN_BLEN[0] = 32;
    }

    @Override
    public boolean checkSign(String pathToSignFile, int delFlag) {
        List<String> ecryptParams = configurationDao.getAll().get(ConfigurationParam.ENCRYPT_DLL, 0);
        if (ecryptParams == null) {
            logger.warn("Не заполнен конфигурационный параметр " + ConfigurationParam.ENCRYPT_DLL.getCaption());
            return false;
        }
        initEncryptLibrary(ecryptParams.get(0));
        List<String> keys = configurationDao.getAll().get(ConfigurationParam.KEY_FILE, 0);
        if (keys == null){
            throw new ServiceException("Ошибка доступа к файлу базы открытых ключей. БОК не заданы.");
        }

        long param[] = new long[10];
        int intParam[] = new int[10];
        long init;
        long pkBase;
        StringBuffer userIdBuf = new StringBuffer("1");
        int result;

        //-------------- инициализация --------------------------------
        result = Bicr4.cr_init(FLAG_TM, FILE_GK, FILE_UZ, "", TM_NUMBER, TMN_BLEN, intParam, param);
        logger.info("cr_init, result = " + result);
        init = param[0];

        try {
            for (String keyFolderPath : keys){
                FileWrapper keyResourceFolder = ResourceUtils.getSharedResource(keyFolderPath, false);
                if (!keyResourceFolder.exists()){
                    logger.warn(String.format("Директории %s с ключами не существует", keyFolderPath));
                    break;
                }
                String[] listFileNames = keyResourceFolder.list();
                if (listFileNames == null || listFileNames.length == 0){
                    logger.warn(String.format("Директории %s с ключами пустая", keyFolderPath));
                    break;
                }
                //Проверяем по всем возможным БОК
                for (String keyName : listFileNames){
                    int total = 0;
                    FileWrapper dbOfpk = ResourceUtils.getSharedResource(keyFolderPath + keyName);
                    if (dbOfpk.isDirectory())
                        continue;
                    //-------------- загрузка базы БОК --------------------------------
                    result =  Bicr4.cr_pkbase_load(init, dbOfpk.getPath(), COM_LEN, 0, param); total += result;
                    if (result != 0){
                        logger.error(String.format("cr_pkbase_load, ошибка загрузки БОК %s, код ошибки %s", dbOfpk.getPath(), result));
                        continue;
                    }
                    else
                        logger.info(String.format("cr_pkbase_load, БОК %s result = ", dbOfpk.getPath()) + result);
                    pkBase = param[0];

                    //-------------- проверка ЭЦП в файле --------------------------------
                    int n = 1; //проверим первую ЭЦП
                    result = Bicr4.cr_check_file(init, pkBase, pathToSignFile, n, delFlag, userIdBuf); total += result;
                    logger.info("cr_check_file, result = " + result);

                    //-------------- деинициализация --------------------------------
                    result = Bicr4.cr_pkbase_close(pkBase); total += result;
                    logger.info("cr_pkbase_close, result = " + result);
                    if (total == 0){
                        return true;
                    }
                }
            }
        } finally {
            result = Bicr4.cr_uninit(init);
            logger.info("cr_uninit, result = " + result);
        }

        return false;
    }

    /**
     * Метод загружает библиотеки для проверки подписи. Должен вызываться перед проверкой подписи.
     * @param dir
     */
    private void initEncryptLibrary(String dir){
        FileWrapper resourceDir = ResourceUtils.getSharedResource(dir);
        if (!resourceDir.isDirectory())
            throw new ServiceException("Необходимо указать директорию с библиотекой ЭЦП");

        String[] listFiles = resourceDir.list();
        assert listFiles != null;
        for (String fileName : listFiles) {
            FileWrapper resourceFile = ResourceUtils.getSharedResource(dir + fileName);
            try {
                System.load(resourceFile.getPath());
            } catch (UnsatisfiedLinkError linkError){
                logger.warn("Ошибка при загрузке библиотек подписи", linkError);
            }
        }
    }
}
