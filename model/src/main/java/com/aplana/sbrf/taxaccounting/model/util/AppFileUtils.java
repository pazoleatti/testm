package com.aplana.sbrf.taxaccounting.model.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

public final class AppFileUtils {
    private static final Log LOG = LogFactory.getLog(AppFileUtils.class);

    public static void deleteTmp(File file) {
        if (file != null) {
            if (!file.delete()) {
                LOG.warn("Не удален временный файл: " + file.getAbsoluteFile());
            }
        }
    }
}
