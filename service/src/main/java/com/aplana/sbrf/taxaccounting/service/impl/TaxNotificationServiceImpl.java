package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.async.AbstractStartupAsyncTaskHandler;
import com.aplana.sbrf.taxaccounting.async.AsyncManager;
import com.aplana.sbrf.taxaccounting.dao.DeclarationDataFileDao;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.service.impl.print.taxnotification.TaxNotificationException;
import com.aplana.sbrf.taxaccounting.service.impl.print.taxnotification.NotificationDocument;
import com.aplana.sbrf.taxaccounting.service.refbook.RefBookAsnuService;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.aplana.sbrf.taxaccounting.model.util.StringUtils.joinNotEmpty;
import static org.apache.commons.collections4.CollectionUtils.*;
import static org.apache.commons.lang3.StringUtils.*;


@Service
public class TaxNotificationServiceImpl implements TaxNotificationService {

    private final DeclarationTemplateDao declarationTemplateDao;
    private final DeclarationDataFileDao declarationDataFileDao;

    // сервисы, по алфавиту классов
    private final BlobDataService blobDataService;
    private final DeclarationDataService declarationDataService;
    private final DepartmentService departmentService;
    private final LockDataService lockDataService;
    private final LogEntryService logEntryService;
    private final NdflPersonService ndflPersonService;
    private final PeriodService periodService;
    private final RefBookAsnuService asnuService;
    private final TAUserService userService;

    private final AsyncManager asyncManager;

    public TaxNotificationServiceImpl(DeclarationTemplateDao declarationTemplateDao,
                                      DeclarationDataFileDao declarationDataFileDao,
                                      BlobDataService blobDataService,
                                      DeclarationDataService declarationDataService,
                                      NdflPersonService ndflPersonService,
                                      DepartmentService departmentService,
                                      PeriodService periodService,
                                      RefBookAsnuService asnuService,
                                      LogEntryService logEntryService,
                                      LockDataService lockDataService,
                                      TAUserService userService,
                                      AsyncManager asyncManager) {
        this.declarationTemplateDao = declarationTemplateDao;
        this.declarationDataFileDao = declarationDataFileDao;
        this.blobDataService = blobDataService;
        this.declarationDataService = declarationDataService;
        this.ndflPersonService = ndflPersonService;
        this.departmentService = departmentService;
        this.periodService = periodService;
        this.asnuService = asnuService;
        this.logEntryService = logEntryService;
        this.lockDataService = lockDataService;
        this.userService = userService;
        this.asyncManager = asyncManager;
    }

    @Override
    public String createAsync(Integer departmentId, Integer periodId, List<Long> asnuIds, TAUserInfo userInfo) {

        Department department = departmentService.getDepartment(departmentId);
        ReportPeriod period = periodService.fetchReportPeriod(periodId);

        // Поиск КНФ по параметрам
        List<DeclarationData> declarations = declarationDataService.findAllDeclarationData(DeclarationType.NDFL_CONSOLIDATE, departmentId, periodId);
        DeclarationData declaration = findAcceptedDeclarationForNotHoldingTax(declarations);

        Logger logger = new Logger();
        if (declaration == null) {
            String errorMessage = "За указанный период %d: %s по территориальному банку %s отсутствует КНФ, " +
                    "необходимая для формирования Уведомлений о неудержанном налоге. " +
                    "Необходимо сформировать КНФ и повторить выполнение операции.";
            logger.error(errorMessage, period.getTaxPeriod().getYear(), period.getName(), department.getShortName());
        } else {
            String keyTask = "CREATE_NOT_HOLDING_TAX_NOTIFICATIONS_FOR_DEPARTMENT_" + departmentId + "_PERIOD_" + periodId;
            Map<String, Object> params = new HashMap<>();
            params.put("declaration", declaration);
            params.put("department", department);
            params.put("period", period);

            // Если есть АСНУ
            if (isNotEmpty(asnuIds)) {
                List<RefBookAsnu> asnuList = asnuService.fetchByIds(asnuIds);
                params.put("asnuList", asnuList);
            }

            asyncManager.executeTask(keyTask, AsyncTaskType.CREATE_NOT_HOLDING_TAX_NOTIFICATIONS, userInfo, params, logger, false, new AbstractStartupAsyncTaskHandler() {
                @Override
                public LockData lockObject(String keyTask, AsyncTaskType reportType, TAUserInfo userInfo) {
                    return lockDataService.lockAsync(keyTask, userInfo.getUser().getId());
                }
            });
        }

        String taskLogsUuid = logEntryService.save(logger.getEntries());
        return taskLogsUuid;
    }

    /**
     * Возвращает первую найденную КНФ по неудержанному НДФЛ в состоянии "Принята".
     */
    private DeclarationData findAcceptedDeclarationForNotHoldingTax(Collection<DeclarationData> declarations) {
        if (isEmpty(declarations)) return null;

        for (DeclarationData declaration : declarations) {
            if (declaration.getKnfType().equals(RefBookKnfType.BY_NONHOLDING_TAX) && declaration.getState().equals(State.ACCEPTED)) {
                return declaration;
            }
        }
        return null;
    }


    @Override
    public String create(DeclarationData knf, List<RefBookAsnu> selectedAsnuList, Logger logger) {
        try {
            List<NotificationDocument> notifications = generateNotificationsForKnf(knf, selectedAsnuList);

            String zipFileName = generateZipFileName(knf) + ".zip";
            String fileUuid = archiveAndSaveNotifications(notifications, zipFileName);
            attachFileToKnf(knf, fileUuid);

            return fileUuid;
        } catch (TaxNotificationException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * Генерирует документы Уведомлений для КНФ по неудержанному НДФЛ.
     */
    private List<NotificationDocument> generateNotificationsForKnf(DeclarationData knf, List<RefBookAsnu> selectedAsnuList) {
        int reportYear = getKnfReportYear(knf);

        // Группируем доходы по физлицам
        Collection<NdflPersonIncome> incomes = ndflPersonService.findNdflPersonIncome(knf.getId());

        // Если требуется, фильтруем доходы по АСНУ
        if (isNotEmpty(selectedAsnuList)) {
            Collection<Long> selectedAsnuIds = Collections2.transform(selectedAsnuList, getId());
            incomes = Collections2.filter(incomes, isIncomeByAsnuIds(selectedAsnuIds));

            // Если incomes получилась пустой, то выкидываем исключение
            if (isEmpty(incomes)) {
                Collection<String> selectedAsnuCodes = Collections2.transform(selectedAsnuList, getCode());
                String asnuCodesList = join(selectedAsnuCodes, ", ");
                throw new TaxNotificationException("При формировании Уведомлений по неудержанному налогу не найдено ни одной строки, " +
                        "удовлетворяющей условию: \"Код АСНУ в [" + asnuCodesList + "]\"");
            }
        }

        ImmutableListMultimap<Long, NdflPersonIncome> incomesGroupedByPerson = Multimaps.index(incomes, getPersonId());

        List<NotificationDocument> notifications = new ArrayList<>();

        // Генерируем Уведомления для каждого физлица
        ImmutableSet<Long> personsInKnf = incomesGroupedByPerson.keySet();
        for (Long personId : personsInKnf) {
            NdflPerson person = ndflPersonService.findOne(personId);
            ImmutableList<NdflPersonIncome> personIncomes = incomesGroupedByPerson.get(personId);

            List<NotificationDocument> personNotifications = generatePersonNotifications(person, personIncomes, reportYear, selectedAsnuList);

            notifications.addAll(personNotifications);
        }
        return notifications;
    }

    private int getKnfReportYear(DeclarationData knf) {
        int periodId = knf.getReportPeriodId();
        ReportPeriod period = periodService.fetchReportPeriod(periodId);
        return period.getTaxPeriod().getYear();
    }

    /**
     * Проверка, содержится ли АСНУ дохода в списке АСНУ.
     */
    private Predicate<NdflPersonIncome> isIncomeByAsnuIds(final Collection<Long> asnuIds) {
        return new Predicate<NdflPersonIncome>() {
            @Override
            public boolean apply(NdflPersonIncome income) {
                return asnuIds.contains(income.getAsnuId());
            }
        };
    }

    /**
     * Функция asnu -> asnu.code
     */
    private Function<RefBookAsnu, String> getCode() {
        return new Function<RefBookAsnu, String>() {
            @Override
            public String apply(RefBookAsnu asnu) {
                return asnu.getCode();
            }
        };
    }

    /**
     * Функция income -> income.personId
     */
    private Function<NdflPersonIncome, Long> getPersonId() {
        return new Function<NdflPersonIncome, Long>() {
            @Override
            public Long apply(NdflPersonIncome income) {
                return income.getNdflPersonId();
            }
        };
    }

    /**
     * Генерирует Уведомления по физлицу и списку его доходов.
     */
    private List<NotificationDocument> generatePersonNotifications(NdflPerson person, List<NdflPersonIncome> personIncomes, int reportYear, List<RefBookAsnu> selectedAsnuList) {

        Collection<Long> selectedAsnuIds = null;
        if (isNotEmpty(selectedAsnuList)) {
            selectedAsnuIds = Collections2.transform(selectedAsnuList, getId());
        }

        // Группируем доходы по АСНУ
        ImmutableListMultimap<Long, NdflPersonIncome> incomesGroupedByAsnu = Multimaps.index(personIncomes, getAsnuId());

        List<NotificationDocument> notifications = new ArrayList<>();

        // Для каждого АСНУ формируем отдельное Уведомление
        for (Long asnuId : incomesGroupedByAsnu.keySet()) {

            // Если АСНУ содержится в избранных, либо избранные пусты
            if (isEmpty(selectedAsnuIds) || (selectedAsnuIds.contains(asnuId))) {

                RefBookAsnu asnu = asnuService.fetchById(asnuId);
                ImmutableList<NdflPersonIncome> personAsnuIncomes = incomesGroupedByAsnu.get(asnuId);

                NotificationDocument personNotificationByAsnu = generateNotificationByPersonAndAsnu(person, asnu, personAsnuIncomes, reportYear);
                notifications.add(personNotificationByAsnu);
            }
        }
        return notifications;
    }

    /**
     * Функция income -> income.asnuId
     */
    private Function<NdflPersonIncome, Long> getAsnuId() {
        return new Function<NdflPersonIncome, Long>() {
            @Override
            public Long apply(NdflPersonIncome income) {
                return income.getAsnuId();
            }
        };
    }

    /**
     * Функция asnu -> asnu.id
     */
    private Function<RefBookAsnu, Long> getId() {
        return new Function<RefBookAsnu, Long>() {
            @Override
            public Long apply(RefBookAsnu asnu) {
                return asnu.getId();
            }
        };
    }

    /**
     * Генерирует одно Уведомление по физлицу и списку его доходов, относящихся к одной АСНУ.
     */
    private NotificationDocument generateNotificationByPersonAndAsnu(NdflPerson person, RefBookAsnu asnu, List<NdflPersonIncome> incomes, int reportYear) {

        BigDecimal notHoldingTaxSum = BigDecimal.ZERO;
        BigDecimal overHoldingTaxSum = BigDecimal.ZERO;
        BigDecimal incomeAccruedSum = BigDecimal.ZERO;

        for (NdflPersonIncome income : incomes) {
            notHoldingTaxSum = sum(notHoldingTaxSum, income.getNotHoldingTax());
            overHoldingTaxSum = sum(overHoldingTaxSum, income.getOverholdingTax());
            incomeAccruedSum = sum(incomeAccruedSum, income.getIncomeAccruedSumm());
        }

        // Если неудержанный налог больше сверхудержанного
        if (notHoldingTaxSum.compareTo(overHoldingTaxSum) > 0) {

            // задолженность = неудержанный - сверхудержанный
            BigDecimal debt = notHoldingTaxSum.subtract(overHoldingTaxSum);

            // Выгрузка шаблона
            String templateFileName = getTemplateFileByAsnuCode(asnu.getCode());
            try (InputStream template = declarationTemplateDao.getTemplateFileContent(DeclarationType.NDFL_CONSOLIDATE, templateFileName)) {

                // Инициализируем документ шаблоном
                XWPFDocument document = new XWPFDocument(template);

                Map<String, String> model = new HashMap<>();
                model.put("ADDRESS", getPersonAddressString(person).toUpperCase());
                model.put("FULLNAME", getPersonFullNameString(person).toUpperCase());
                model.put("USERNAME", getPersonNamePatronymString(person));
                model.put("REPORTYEAR", String.valueOf(reportYear));
                model.put("PAYMENTYEAR", String.valueOf(reportYear + 1));
                model.put("DEBT", debt.toString());
                model.put("INCOME", incomeAccruedSum.toString());
                model.put("ASNU", asnu.getName() + " (" + asnu.getCode() + ")");

                fillTemplate(document, model);

                String fileName = generateFileName(person, asnu) + ".docx";
                return new NotificationDocument(fileName, document);
            } catch (IOException | XmlException e) {
                String errorMessage = "При формировании Уведомлений по неудержанному налогу (Приложение %d) " +
                        "по ФЛ %s (ИНП = %s) возникла ошибка при формировании файла - файл не сформирован";
                throw new TaxNotificationException(String.format(errorMessage, getTemplateCodeByAsnuCode(asnu.getCode()), getPersonFullNameString(person), person.getInp()));
            }
        } else {
            String errorMessage = "Для формирования Уведомлений по неудержанному налогу (Приложение %d) " +
                    "по ФЛ %s (ИНП = %s) не найдено ни одной строки, удовлетворяющей условию: " +
                    "\"Разница сумм неудержанного и излишне удержанного налога > 0\"";
            throw new TaxNotificationException(String.format(errorMessage, getTemplateCodeByAsnuCode(asnu.getCode()), getPersonFullNameString(person), person.getInp()));
        }
    }

    /**
     * Суммирование BigDecimal чисел, null-безопасно.
     */
    private static BigDecimal sum(BigDecimal x1, BigDecimal x2) {
        if (x1 == null) x1 = BigDecimal.ZERO;
        if (x2 == null) x2 = BigDecimal.ZERO;
        return x1.add(x2);
    }

    /**
     * Получить название файла шаблона по коду АСНУ.
     */
    private String getTemplateFileByAsnuCode(String asnuCode) {
        switch (asnuCode) {
            case "6004":
                return DeclarationTemplateFile.APPLICATION11_NDFL_DEBT_TEMPLATE;
            case "6001":
            case "7000":
                return DeclarationTemplateFile.APPLICATION12_NDFL_DEBT_TEMPLATE;
            case "1001":
            case "6005":
            case "9000":
            case "9001":
                return DeclarationTemplateFile.APPLICATION13_1_NDFL_DEBT_TEMPLATE;
            case "6000":
                return DeclarationTemplateFile.APPLICATION13_2_NDFL_DEBT_TEMPLATE;
            default:
                return DeclarationTemplateFile.APPLICATION14_NDFL_DEBT_TEMPLATE;
        }
    }

    /**
     * Получить название файла шаблона по коду АСНУ.
     */
    private int getTemplateCodeByAsnuCode(String asnuCode) {
        switch (asnuCode) {
            case "6004":
                return 11;
            case "6001":
            case "7000":
                return 12;
            case "1001":
            case "6005":
            case "9000":
            case "9001":
            case "6000":
                return 13;
            default:
                return 14;
        }
    }

    /**
     * Заполняет шаблон значениями из модели
     */
    private void fillTemplate(XWPFDocument template, Map<String, String> model) throws XmlException {

        Object[] modelKeys = model.keySet().toArray();
        String[] variables = new String[modelKeys.length];
        String[] values = new String[modelKeys.length];

        for (int i = 0; i < modelKeys.length; i++) {
            String key = modelKeys[i].toString();
            values[i] = model.get(key);
            variables[i] = key;
        }

        // Заполняем по параграфам
        for (XWPFParagraph paragraph : template.getParagraphs()) {
            fillParagraph(paragraph, variables, values);
        }
    }

    /**
     * Заменяет в параграфе переменные на значения.
     */
    private void fillParagraph(XWPFParagraph paragraph, String[] variables, String[] values) throws XmlException {
        replaceVariablesInParagraphText(paragraph, variables, values);
        replaceVariablesInTextBoxes(paragraph, variables, values);
    }

    /**
     * Заменяет переменные в обычном тексте параграфа.
     */
    private void replaceVariablesInParagraphText(XWPFParagraph paragraph, String[] variables, String[] values) {
        // Заходим в параграф, только если в его тексте есть переменная
        if (containsAny(paragraph.getText(), variables)) {
            List<XWPFRun> runs = paragraph.getRuns();
            if (runs != null) {
                for (XWPFRun run : runs) {
                    replaceVariablesInRun(run, variables, values);
                }
            }
        }
    }

    /**
     * Заменяет переменные в текстовых полях ("Вставка" -> "Текстовое поле", в старом Word "Вставка" -> "Надпись")
     */
    private void replaceVariablesInTextBoxes(XWPFParagraph paragraph, String[] variables, String[] values) throws XmlException {
        // Приходится работать с XML-основой Word-документа. Находим текстовые поля и разбираем их до объектов XWPFRun
        XmlCursor cursor = paragraph.getCTP().newCursor();
        cursor.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//*/w:txbxContent/w:p/w:r");

        List<XmlObject> textBoxElements = new ArrayList<>();
        while (cursor.hasNextSelection()) {
            cursor.toNextSelection();
            XmlObject obj = cursor.getObject();
            textBoxElements.add(obj);
        }
        for (XmlObject element : textBoxElements) {
            CTR ctr = CTR.Factory.parse(element.toString());
            XWPFRun run = new XWPFRun(ctr, paragraph);
            replaceVariablesInRun(run, variables, values);
            element.set(run.getCTR());
        }
    }

    /**
     * Если в элементе run имеется текстовая переменная, заменяет её значением
     *
     * @param run       элемент Apache POI XWPFRun - кусочек текста Word-документа
     * @param variables массив переменных, подлежащих замене
     * @param values    массив значений, на которые следует заменять
     */
    private void replaceVariablesInRun(XWPFRun run, String[] variables, String[] values) {
        String text = run.getText(0);
        if (containsAny(text, variables)) {
            String replacedText = replaceEach(text, variables, values);
            run.setText(replacedText, 0);
        }
    }

    // Имя Отчество
    private String getPersonNamePatronymString(NdflPerson person) {
        String[] nameArray = new String[2];
        nameArray[0] = person.getFirstName();
        nameArray[1] = person.getMiddleName();
        return joinNotEmpty(nameArray, " ");
    }

    // Фамилия Имя Отчество
    private String getPersonFullNameString(NdflPerson person) {
        String[] nameArray = new String[3];
        nameArray[0] = person.getLastName();
        nameArray[1] = person.getFirstName();
        nameArray[2] = person.getMiddleName();
        return joinNotEmpty(nameArray, " ");
    }

    // Строка адреса
    private String getPersonAddressString(NdflPerson person) {
        String[] addressElements = new String[8];
        addressElements[0] = person.getPostIndex();
        addressElements[1] = person.getArea();
        addressElements[2] = person.getCity();
        addressElements[3] = person.getLocality();
        addressElements[4] = person.getStreet();
        addressElements[5] = person.getHouse();
        addressElements[6] = person.getBuilding();
        addressElements[7] = person.getFlat();
        return joinNotEmpty(addressElements, " ");
    }

    /**
     * Генерирует название файла "<ФИО>_<Дата рождения>_<ИД Физлица>_<КодАСНУ>"
     */
    private String generateFileName(NdflPerson person, RefBookAsnu asnu) {
        String[] partsArray = new String[6];
        partsArray[0] = person.getLastName();
        partsArray[1] = person.getFirstName();
        partsArray[2] = person.getMiddleName();
        partsArray[3] = FastDateFormat.getInstance("ddMMyyyy").format(person.getBirthDay());
        partsArray[4] = person.getInp();
        partsArray[5] = asnu.getCode();
        return joinNotEmpty(partsArray, "_");
    }

    /**
     * Генерирует название архива "<Тербанк>_<Номер формы>_<Дата создания>"
     */
    private String generateZipFileName(DeclarationData knf) {
        String[] partsArray = new String[3];

        Department department = departmentService.getDepartment(knf.getDepartmentId());
        partsArray[0] = department.getShortName().replace(' ', '_');

        partsArray[1] = knf.getId().toString();

        LocalDateTime now = new LocalDateTime();
        partsArray[2] = DateTimeFormat.forPattern("ddMMYYYY_HHmmss").print(now);

        return joinNotEmpty(partsArray, "_");
    }

    /**
     * Архивирует Уведомления и сохраняет архив в базу.
     *
     * @return uuid файла в базе
     */
    private String archiveAndSaveNotifications(List<NotificationDocument> notifications, String fileName) {
        File tempZipFile = null;
        try {
            tempZipFile = File.createTempFile("archive", ".zip");

            writeDocumentsIntoZipFile(notifications, tempZipFile);

            String fileUuid = blobDataService.create(tempZipFile, fileName, new Date());
            return fileUuid;

        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e);
        } finally {
            if (tempZipFile != null) {
                tempZipFile.delete();
            }
        }
    }

    /**
     * Архивирует документы в zip-файл.
     */
    private void writeDocumentsIntoZipFile(List<NotificationDocument> documents, File zipFile) throws IOException {
        FileOutputStream fileOut = new FileOutputStream(zipFile);
        ZipOutputStream zipOut = new ZipOutputStream(fileOut);

        for (NotificationDocument document : documents) {
            ZipEntry zipEntry = new ZipEntry(document.getName());
            zipOut.putNextEntry(zipEntry);
            zipOut.write(document.toByteArray());
        }

        zipOut.close();
        fileOut.close();
    }

    /**
     * Добавляет файл Уведомлений к файлам КНФ.
     */
    private void attachFileToKnf(DeclarationData knf, String fileUuid) {
        TAUser currentUser = userService.getCurrentUser();
        String userDepartmentName = departmentService.getParentsHierarchyShortNames(currentUser.getDepartmentId());

        DeclarationDataFile declarationDataFile = new DeclarationDataFile();
        declarationDataFile.setDeclarationDataId(knf.getId());
        declarationDataFile.setUuid(fileUuid);
        declarationDataFile.setUserName(currentUser.getName());
        declarationDataFile.setUserDepartmentName(userDepartmentName);
        declarationDataFile.setFileTypeId(AttachFileType.NOTICE.getId());
        declarationDataFileDao.create(declarationDataFile);
    }
}
