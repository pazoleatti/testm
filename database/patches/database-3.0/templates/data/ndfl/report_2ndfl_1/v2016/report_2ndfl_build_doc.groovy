package form_template.ndfl.report_2ndfl_1.v2016

import com.aplana.sbrf.taxaccounting.AbstractScriptClass
import com.aplana.sbrf.taxaccounting.model.BlobData
import com.aplana.sbrf.taxaccounting.model.Cell
import com.aplana.sbrf.taxaccounting.model.DataRow
import com.aplana.sbrf.taxaccounting.model.DeclarationData
import com.aplana.sbrf.taxaccounting.model.FormDataEvent
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookIncomeType
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory
import groovy.transform.TypeChecked
import groovy.transform.TypeCheckingMode
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP
import org.w3c.dom.Node

import java.util.regex.Pattern

new BuildDoc(this).run()

class BuildDoc extends AbstractScriptClass {

    RefBookFactory refBookFactory

    DeclarationData declarationData
    DataRow<Cell> selectedPerson
    InputStream templateInputStream
    BlobData blobDataOut

    Map<String, Map<String, RefBookValue>> incomeTypeByCodeCache = [:]

    def Документ
    String fio

    private Check() {
    }

    @TypeChecked(TypeCheckingMode.SKIP)
    BuildDoc(scriptClass) {
        super(scriptClass)
        this.refBookFactory = (RefBookFactory) scriptClass.getProperty("refBookFactory")
        this.declarationData = (DeclarationData) scriptClass.getProperty("declarationData")
        this.selectedPerson = (DataRow<Cell>) scriptClass.getProperty("selectedPerson")
        this.templateInputStream = (InputStream) scriptClass.getProperty("templateInputStream")
        this.blobDataOut = (BlobData) scriptClass.getProperty("blobDataOut")
    }

    @Override
    void run() {
        switch (formDataEvent) {
            case FormDataEvent.BUILD_DOC:
                buildDoc()
        }
    }

    void buildDoc() {
        def xmlStr = declarationService.getXmlData(declarationData.id, userInfo)
        def Файл = new XmlSlurper().parseText(xmlStr)
        Документ = Файл.Документ.find { Документ -> Документ.@НомСпр.text() == selectedPerson.pNumSpravka }

        if (Документ) {
            def byteArrayOutputStream = generateDocumentFromTemplate(templateInputStream)
            blobDataOut.setInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))
            blobDataOut.setName("Уведомление_${fio?.replaceAll("[^а-яА-ЯёЁ\\w]", "_")}_${new Date().format("yyyyMMdd-HHmmss")}.docx")
        } else {
            logger.error("В xml-файле формы № ${declarationData.id} отсутствует элемент \"Файл.Документ\", для которого атрибут НомСпр = ${selectedPerson.pNumSpravka}")
        }
    }

    // Регулярное выражение для отделения имени закладки от номера
    Pattern BOOKMARK_REG_EX = ~/([^\d]+)(\d*)/

    // Закладки
    String DEPT_NOTICE_FIO_BOOKMARK = "dept_notice_fio"
    String DEPT_NOTICE_INCOME_TYPE_BOOKMARK = "dept_notice_income_type"
    String DEPT_NOTICE_PAYMENT_YEAR_BOOKMARK = "dept_notice_payment_year"
    String DEPT_NOTICE_SUM_BOOKMARK = "dept_notice_sum"
    String DEPT_NOTICE_YEAR_BOOKMARK = "dept_notice_year"
    String BOOKMARK_TO_IGNORE = "ignore this bookmark"

    /**
     * Возвращает текст для вставки в шаблон по имени закладки
     *
     * @param bookmarkName имя закладки
     * @return текст для вставки в шаблон
     */
    String getBookmarkValueByName(String bookmarkName) {
        // отделяем имя закладки от номера (для повторяющихся закладок)
        def m = BOOKMARK_REG_EX.matcher(bookmarkName)
        if (m.find()) {
            bookmarkName = m.group(1)
        }

        switch (bookmarkName) {
            case DEPT_NOTICE_FIO_BOOKMARK:
                fio = "${Документ.ПолучДох.ФИО.@Фамилия.text()} ${Документ.ПолучДох.ФИО.@Имя.text()} ${Документ.ПолучДох.ФИО.@Отчество.text()}"
                return fio
            case DEPT_NOTICE_YEAR_BOOKMARK:
                return Integer.valueOf((String) Документ.@ОтчетГод.text())
            case DEPT_NOTICE_PAYMENT_YEAR_BOOKMARK:
                return Integer.valueOf((String) Документ.@ОтчетГод.text()) + 1
            case DEPT_NOTICE_INCOME_TYPE_BOOKMARK:
                def incomeTypeNames = [] as Set
                Документ.СведДох.ДохВыч.СвСумДох.each { СвСумДох ->
                    String incomeTypeCode = СвСумДох.@КодДоход.text()
                    incomeTypeNames.add("«" + getIncomeTypeByCode(incomeTypeCode).NAME.stringValue + "»")
                }
                return incomeTypeNames.join(", ")
            case DEPT_NOTICE_SUM_BOOKMARK:
                return Документ.СведДох.СумИтНалПер.@НалИсчисл.text()
            default: return BOOKMARK_TO_IGNORE
        }
    }

    /**
     * Выполняет формированием файла по шаблону
     * @return поток байтов
     */
    ByteArrayOutputStream generateDocumentFromTemplate(InputStream templateInputStream) {
        XWPFDocument document
        try {
            document = new XWPFDocument(templateInputStream)
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e)
        }

        // вставляем значения в параграфы самого документа
        editParagraphs(document.getParagraphs(), document)

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        try {
            document.write(byteArrayOutputStream)
        } catch (IOException e) {
            throw new ServiceException(e.getMessage(), e)
        }
        return byteArrayOutputStream
    }

    /**
     * Проходит по всем параграфам и заменяет закладки на значения, возвращенное ф. getBookmarkValueByName()
     */
    void editParagraphs(List<XWPFParagraph> paragraphs, XWPFDocument doc) {
        for (int paragraphIndex = 0; paragraphIndex < paragraphs.size(); paragraphIndex++) {
            XWPFParagraph paragraph = paragraphs.get(paragraphIndex)
            CTP ctp = paragraph.getCTP()

            // получаем все закладки из параграфа
            List<CTBookmark> bookmarks = ctp.getBookmarkStartList()
            for (CTBookmark bookmark : bookmarks) {
                // закладка не содержит условие, вместо закладки необходимо подставить значение
                def newBookmarkValue = getBookmarkValueByName(bookmark.getName())
                if (newBookmarkValue != BOOKMARK_TO_IGNORE) {
                    setParagraphBookmarkValue(paragraph, bookmark, newBookmarkValue)
                }
            }
        }
    }

    void setParagraphBookmarkValue(XWPFParagraph paragraph, CTBookmark bookmark, def newValue) {
        def run = paragraph.createRun()
        def nextNode = bookmark.getDomNode().getNextSibling()

        run.setText(getStringValue(newValue))

        // находим узел, содержащий форматирование текста в закладке
        Node styleNode = getStyleNode(nextNode)
        // удаляем существующий текст в закладке и получаем узел - окончание закладки
        nextNode = deleteBookmarkValue(bookmark, paragraph)
        // вставляем узел с форматированием перед новым текстом
        if (styleNode != null) {
            run.getCTR().getDomNode().insertBefore(
                    styleNode.cloneNode(true),
                    run.getCTR().getDomNode().getFirstChild())
        }
        // вставляем новое значение в закладку
        paragraph.getCTP().getDomNode().insertBefore(
                run.getCTR().getDomNode(),
                nextNode)
    }

    /**
     * Удаляет сушествующий текст внутри закладки
     *
     * @param bookmark закладка
     * @param paragraph параграф
     * @return узел - окончание закладке
     */
    def deleteBookmarkValue(CTBookmark bookmark, XWPFParagraph paragraph) {
        Node nextNode
        // Текст закладки должен быть вставлен между узлами bookmarkStart и bookmarkEnd.
        // Поэтому ищем bookmarkEnd
        nextNode = bookmark.getDomNode().getNextSibling()
        // если узел не bookmarkEnd, переходим на следующий
        // текущий узел удаляем, так как он может содержать старое значение текста в закладке
        while (!(nextNode.getNodeName().contains("bookmarkEnd"))) {
            paragraph.getCTP().getDomNode().removeChild(nextNode)
            nextNode = bookmark.getDomNode().getNextSibling()
        }
        return nextNode
    }

    /**
     * Возвращает узел, содержащий информацию о форматировании текста
     * @param parentNode родительский узел
     * @return узел , содержащий информацию о форматировании текста
     */
    Node getStyleNode(Node parentNode) {
        Node childNode
        Node styleNode = null
        if (parentNode != null) {

            if (parentNode.getNodeName().equalsIgnoreCase("w:r")
                    && parentNode.hasChildNodes()) {
                childNode = parentNode.getFirstChild()
                if (childNode.getNodeName().equals("w:rPr")) {
                    styleNode = childNode
                } else {
                    while ((childNode = childNode.getNextSibling()) != null) {
                        if (childNode.getNodeName().equals("w:rPr")) {
                            styleNode = childNode
                            childNode = null
                        }
                    }
                }
            }
        }
        return styleNode
    }

    String getStringValue(def value) {
        if (value instanceof Date) {
            value = value.format("dd.MM.yyyy")
        }
        if (value) {
            return value.toString()
        } else {
            return ""
        }
    }


    Map<String, RefBookValue> getIncomeTypeByCode(String code) {
        Map<String, RefBookValue> result = null
        if (code) {
            result = incomeTypeByCodeCache.get(code)
            if (!result) {
                def records = refBookFactory.getDataProvider(RefBook.Id.INCOME_CODE.id).getRecordDataVersionWhere(" where code = '${code}'", new Date())
                if (1 == records.entrySet().size()) {
                    result = records.entrySet().iterator().next().getValue()
                    incomeTypeByCodeCache.put(code, result)
                } else {
                    throw new ServiceException("Не найдена запись в справочнике \"Коды видов дохода\" по коду \"${code}\"")
                }
            }
        }
        return result
    }
}