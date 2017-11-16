package com.aplana.taxaccounting.querydsl;

import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.NotificationType;
import com.aplana.sbrf.taxaccounting.model.State;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.codegen.MetaDataExporter;
import com.querydsl.sql.types.EnumByOrdinalType;
import com.querydsl.sql.types.InputStreamType;
import com.querydsl.sql.types.LocalDateTimeType;
import com.querydsl.sql.types.NumericBooleanType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Генерация QueryDSL классов из схем БД НДФЛ и УН.
 * Из УН берётся только часть таблиц и необходимо их ручное редактирование после генерации.
 */
public class QueryDSLGenRunner {
    private static final Log LOG = LogFactory.getLog(QueryDSLGenRunner.class);

    public static void main(String[] args) {
        try {
            LOG.info("QueryDSLGenRunner start");
            LOG.info("Load jdbc driver");
            Class.forName("oracle.jdbc.OracleDriver");
            LOG.info("Jdbc driver loaded");

            String dbUrl = "jdbc:oracle:thin:@//172.19.214.45:1521/ORCL.APLANA.LOCAL";
            String packageStr = "com.aplana.sbrf.taxaccounting.model.querydsl";
            String targetFolder = "model/src/main/java";

            // НДФЛ (удаляются все раннее сгенерированные Q классы)
            Configuration configuration = createConfiguration();
            configuration.register("NOTIFICATION", "IS_READ", new NumericBooleanType());
            configuration.register("NOTIFICATION", "TYPE", new EnumByOrdinalType<>(NotificationType.class));
            configuration.register("DECLARATION_DATA", "STATE", new EnumByOrdinalType<>(State.class));
            configuration.register("DECLARATION_DATA", "MANUALLY_CREATED", new NumericBooleanType());
            configuration.register("TAX_PERIOD", "YEAR", Integer.class);

            exportScheme(dbUrl,
                    "ndfl_unstable",
                    "ndfl_unstable",
                    "NDFL_UNSTABLE",
                    packageStr,
                    targetFolder,
                    new String[]{"DECLARATION_DATA"},
                    configuration);

            // УН (удаляются только ранее сгенерированные Q классы по передаваемому перечню таблиц).
            // Необходимо ручное редактирование Q классов после генерации для удаления ссылок на другие таблицы УН (ошибки компиляции)
            /*configuration = createConfiguration();

            configuration.register("DEPARTMENT", "TYPE", new EnumByOrdinalType<>(DepartmentType.class));

            exportScheme(dbUrl,
                    "TAX_1_5",
                    "TAX",
                    "TAX_1_5",
                    packageStr,
                    targetFolder,
                    new String[]{"DEPARTMENT", "SEC_ROLE", "SEC_USER", "SEC_USER_ROLE"},
                    configuration);*/

            LOG.info("QueryDSLGenRunner finish");
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * Создание общих настроек конфигурации для экспорта
     *
     * @return Configuration конфигурация для экспорта
     */
    private static Configuration createConfiguration() {
        SQLTemplates templates = new OracleTemplates();
        Configuration configuration = new Configuration(templates);

        configuration.register(new LocalDateTimeType());
        configuration.register(new InputStreamType());

        configuration.registerNumeric(2, 0, Integer.class);
        configuration.registerNumeric(20, 0, BigDecimal.class);

        return configuration;
    }

    /**
     * @param dbUrl           Url базы данных
     * @param dbUserName      Пользователь базы данных
     * @param dbPassword      Пароль
     * @param schemePattern   Схема таблиц для генерации
     * @param packageName     Пакет в который будут сгенерированы классы, относительно папки
     * @param targetFolderStr Папка в которую будет производиться генерация классов
     * @param tables          Перечень таблиц для генерации классов, для генерации всех таблиц и вьюшек передать null
     * @param configuration   Конфигурация QueryDSL для экспорта
     * @throws SQLException При ошибке работы с БД
     */
    private static void exportScheme(
            String dbUrl,
            String dbUserName,
            String dbPassword,
            String schemePattern,
            String packageName,
            String targetFolderStr,
            String[] tables,
            Configuration configuration
    ) throws SQLException {
        LOG.info("Get connection to SCHEME " + dbUrl + " " + dbUserName);
        Connection conn = DriverManager.getConnection(dbUrl, dbUserName, dbPassword);
        LOG.info("Connect to SCHEME - ok.");

        MetaDataExporter exporter = new MetaDataExporter();

        exporter.setPackageName(packageName);
        File targetFolder = new File(targetFolderStr);
        exporter.setTargetFolder(targetFolder);
        exporter.setSchemaPattern(schemePattern);
        if (tables != null) {
            exporter.setTableNamePattern(Joiner.on(", ").join(tables));
        }

        exporter.setConfiguration(configuration);
        LOG.info("Start exporting SCHEME " + dbUserName);

        File srcFolder = new File(targetFolder, packageName.replaceAll("\\.", "/"));
        List<String> qClasses = Lists.newArrayList();
        if (tables != null) {
            for (String table : tables) {
                qClasses.add("q" + table.replaceAll("_", "").toLowerCase());
            }
        }
        LOG.info("Delete files in " + srcFolder.getAbsolutePath());
        File[] files = srcFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                String qClassName = file.getName().replaceAll("\\.java", "").toLowerCase();
                if (qClasses.isEmpty() || qClasses.contains(qClassName)) {
                    LOG.info("Delete file " + file.getAbsolutePath());
                    file.delete();
                }
            }
        }
        LOG.info("Delete files in " + srcFolder.getAbsolutePath() + " - ok");

        exporter.export(conn.getMetaData());
        LOG.info("End exporting SCHEME " + dbUserName);

        LOG.info("Close connect to SCHEME " + dbUserName);
        conn.close();
        LOG.info("Close connect to SCHEME - ok");
    }
}