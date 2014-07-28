package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.service.SignService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import ru.infocrypt.bicrypt.Bicr4;

@Service
public class SignServiceImpl implements SignService {

    private final Log logger = LogFactory.getLog(getClass());

    // 128 означает, что инициализации ДСЧ не будет
    private static final int FLAG_TM = 128;

    // не используем главный ключ и узел замены
    private static final String FILE_GK = "";
    private static final String FILE_UZ = "";

    private static final int COM_LEN = 0;
    private static final byte TM_NUMBER[] = new byte[32];
    private static final int TMN_BLEN[] = new int[1];
    {
        TMN_BLEN[0] = 32;
    }

    @Override
    public boolean checkSign(String pathToSignFile, String pathToSignDat, int delFlag) {
        int total = 0;

        long param[] = new long[10];
        int intParam[] = new int[10];
        long init;
        long pkBase;
        StringBuffer userIdBuf = new StringBuffer("1");
        //-------------- грузим DLL --------------------------------
        //try-catch на случай если библиотека подгружена другим ClassLoader-ом
        try {
            if (System.getProperty("os.arch", "?").equals("amd64"))
            {   //64 бит
                System.loadLibrary("grn64");
                System.loadLibrary("bicr4_64");
                System.loadLibrary("bicr_adm64");
            }
            else //32 бит
            {
                System.loadLibrary("grn");
                System.loadLibrary("bicr4");
                System.loadLibrary("bicr_adm");
            }
        } catch (UnsatisfiedLinkError linkError){
            logger.info("Библиотека уже загружена." + linkError);
        }

        //-------------- инициализация --------------------------------
        Bicr4.cr_init(FLAG_TM, FILE_GK, FILE_UZ, "", TM_NUMBER, TMN_BLEN, intParam, param);
        init = param[0];

        //-------------- загрузка базы БОК --------------------------------
        total +=  Bicr4.cr_pkbase_load(init, pathToSignDat, COM_LEN, 0, param);
        logger.info("cr_pkbase_load, result = " + total);
        pkBase = param[0];

        //-------------- проверка ЭЦП в файле --------------------------------
        int n = 1; //проверим первую ЭЦП
        total += Bicr4.cr_check_file(init, pkBase, pathToSignFile, n, delFlag, userIdBuf);
        logger.info("cr_check_file, result = " + total);

        //-------------- деинициализация --------------------------------
        total += Bicr4.cr_pkbase_close(pkBase);
        logger.info("cr_pkbase_close, result = " + total);
        total += Bicr4.cr_uninit(init);
		return total == 0;
    }
}
