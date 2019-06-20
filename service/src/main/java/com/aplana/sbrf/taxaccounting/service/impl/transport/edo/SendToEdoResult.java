package com.aplana.sbrf.taxaccounting.service.impl.transport.edo;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Результат отправки списка форм в ЭДО.
 */
public class SendToEdoResult {

    private Map<LogLevel, List<DeclarationData>> declarationsByLogLevel = new HashMap<>();

    public void put(LogLevel logLevel, DeclarationData declarationData) {
        List<DeclarationData> declarations = declarationsByLogLevel.get(logLevel);
        if (declarations == null) {
            declarations = new ArrayList<>();
            declarationsByLogLevel.put(logLevel, declarations);
        }
        declarations.add(declarationData);
    }

    public List<DeclarationData> getDeclarationsByLogLevel(LogLevel logLevel) {
        return declarationsByLogLevel.get(logLevel);
    }
}
