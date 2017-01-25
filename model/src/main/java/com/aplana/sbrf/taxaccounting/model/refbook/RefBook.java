package com.aplana.sbrf.taxaccounting.model.refbook;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cправочник
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.05.13 15:38
 */
public class RefBook implements Serializable {

	private static final Long serialVersionUID = 1L;

    public static final String RECORD_ID_ALIAS = "record_id";
	public static final String RECORD_PARENT_ID_ALIAS = "PARENT_ID";
	public static final String REF_BOOK_RECORD_TABLE_NAME = "REF_BOOK_RECORD";

    public static final String RECORD_VERSION_FROM_ALIAS = "record_version_from";
    public static final String REF_BOOK_VERSION_FROM_TITLE = "Дата начала актуальности";
    public static final int REF_BOOK_VERSION_FROM_WIDTH = 6;

    public static final String RECORD_VERSION_TO_ALIAS = "record_version_to";
    public static final String REF_BOOK_VERSION_TO_TITLE = "Дата окончания актуальности";
    public static final int REF_BOOK_VERSION_TO_WIDTH = 6;

	public static final String RECORD_HAS_CHILD_ALIAS = "HAS_CHILD";
	public static final String RECORD_SORT_ALIAS = "row_number_over";

    /**
     * Соотношение основной и табличной части настроек подразделений с типом налога
     */
    public enum WithTable {
        NDFL(TaxType.NDFL, 950L, 951L),
		FOND(TaxType.PFR, 960L, 961L);

        private TaxType taxType;
        private Long refBookId;
        private Long tableRefBookId;

        WithTable(TaxType taxType, Long refBookId, Long tableRefBookId) {
            this.taxType = taxType;
            this.refBookId = refBookId;
            this.tableRefBookId = tableRefBookId;
        }

        public TaxType getTaxType() {
            return taxType;
        }

        public Long getRefBookId() {
            return refBookId;
        }

        public Long getTableRefBookId() {
            return tableRefBookId;
        }

        public static WithTable getByTaxType(TaxType taxType) {
            for (WithTable item : WithTable.values()) {
                if (item.getTaxType() == taxType) {
                    return item;
                }
            }
            throw new RuntimeException("Не найдено сочетание табличной и нетабличной части настроек подразделения для указанного налога");
        }

        public static Long getTablesIdByRefBook(long refBookId) {
            for (WithTable r: values()) {
                if (r.refBookId == refBookId) {
                    return r.getTableRefBookId();
                }
            }
            return null;
        }
    }

	/**	Индентификаторы таблиц, используются датапровайдерами */
	public enum Id {
		ASNU(900), 									// АСНУ
		PERSON(904), 								// Физ. лица
		REGION(923), 								// Субъекты РФ
		NDFL(950), 									// Настройки подразделений по НДФЛ
		NDFL_DETAIL(951), 							// Настройки подразделений по НДФЛ (таблица)
		FOND(960), 									// Настройки подразделений по Сборы, взносы
		FOND_DETAIL(961), 							// Настройки подразделений по Сборы, взносы (таблица)
		SEC_ROLE(95), 								// Роли
		DEPARTMENT(30L),							// Подразделения
		DEPARTMENT_TYPE(103),						// Типы подразделений
		FIAS_OPERSTAT(1010), 						// < ФИАС
		FIAS_SOCRBASE(1020),
		FIAS_ADDR_OBJECT(1030),
		FIAS_HOUSE(1040),
		FIAS_HOUSEINT(1050),
		FIAS_ROOM(1060), 							// ФИАС >
		DECLARATION_DATA_KIND_REF_BOOK(932), 		// Типы форм (declaration)
		DECLARATION_DATA_TYPE_REF_BOOK(931), 		// Випы налоговых форм (declaration)
		CONFIGURATION_PARAM(105), 					// Конфигурационные параметры
		AUDIT_FIELD(104), 							// Список полей для журнала аудита
		EMAIL_CONFIG(400), 							// Настройки почтового клиента
		ASYNC_CONFIG(401), 							// Настройки асинхронных задач
		ID_DOC(902),                                // Документ, удостоверяющий личность
		REORGANIZATION(5),							// Коды форм реорганизации (ликвидации) организации
		USER(74); 									// Пользователи

        private final long id;

		Id(long refBookId) {
			this.id = refBookId;
		}
		Id(int refBookId) {
			this.id = refBookId;
		}
		public long getId() {
			return id;
		}
		public static Id getById(long id){
			for (Id idValue : Id.values()) {
				if (idValue.getId() == id) {
					return idValue;
				}
			}
			return null;
		}
	}

	public enum Table {
		ASNU("REF_BOOK_ASNU"),
        PERSON("REF_BOOK_PERSON"),
        REGION("REF_BOOK_REGION"),
        NDFL("REF_BOOK_NDFL"),
		NDFL_DETAIL("REF_BOOK_NDFL_DETAIL"),
        FOND("REF_BOOK_FOND"),
        FOND_DETAIL("REF_BOOK_FOND_DETAIL"),
		FIAS_OPERSTAT("FIAS_OPERSTAT"),
		FIAS_SOCRBASE("FIAS_SOCRBASE"),
		FIAS_ADDR_OBJECT("FIAS_ADDR_OBJECT"),
		FIAS_HOUSE("FIAS_HOUSE"),
		FIAS_HOUSEINT("FIAS_HOUSEINT"),
		FIAS_ROOM("FIAS_ROOM");

		private final String table;

		Table(String table) {
			this.table = table;
		}

		public String getTable() {
			return table;
		}
	}

	/** Код справочника */
	private Long id;

	/** Наименование справочника */
	private String name;

	/** Атрибуты справочника */
	private List<RefBookAttribute> attributes;

    /** Скрипт справочника */
    private String scriptId;

	/** Признак отображения справочника */
	private boolean visible;

	/** Тип справочника (0 - Линейный, 1 - Иерархический) */
	private int type;

	/** Редактируемый (0 - редактирование недоступно пользователю, 1 - редактирование доступно пользователю) */
	private boolean readOnly;

    /** Версионируемый (0 - не версионируемый, 1 - версионируемый) */
    private boolean versioned;

    /** Название таблицы справочника, заполняется в случае если справочник не универсальный */
    private String tableName;

    private RefBookAttribute regionAttribute;

	public static RefBookAttribute getVersionFromAttribute() {
		RefBookAttribute attr = new RefBookAttribute();
		attr.setWidth(REF_BOOK_VERSION_FROM_WIDTH);
		attr.setName(REF_BOOK_VERSION_FROM_TITLE);
		attr.setAttributeType(RefBookAttributeType.DATE);
		attr.setAlias(RECORD_VERSION_FROM_ALIAS);
		return attr;
	}

	public static RefBookAttribute getVersionToAttribute() {
		RefBookAttribute attr = new RefBookAttribute();
		attr.setWidth(REF_BOOK_VERSION_TO_WIDTH);
		attr.setName(REF_BOOK_VERSION_TO_TITLE);
		attr.setAttributeType(RefBookAttributeType.DATE);
		attr.setAlias(RECORD_VERSION_TO_ALIAS);
		return attr;
	}

	/**
	 * Возвращает код справочника
	 * @return код справочника
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Устанавливает код справочника
	 * @param id код справочника
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Возвращает наименование справочника
	 * @return наименование справочника
	 */
	public String getName() {
		return name;
	}

	/**
	 * Устанавливает наименование справочника
	 * @param name наименование справочника
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Возвращает список атрибутов справочника
	 * @return список атрибутов справочника
	 */
	public List<RefBookAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Возвращает список атрибутов справочника
	 * @param attributes список атрибутов справочника
	 */
	public void setAttributes(List<RefBookAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Возвращает атрибут по его псевдониму. Регистронезависимый поиск
	 * @param alias
	 * @throws IllegalArgumentException в случае, если искомого атрибута нет в справочнике
	 * @return
	 */
	public RefBookAttribute getAttribute(final String alias) {
		if (alias == null) {
			throw new IllegalArgumentException("Attribute alias must be defined");
		}

		for(RefBookAttribute attribute : attributes) {
			if (alias.equalsIgnoreCase(attribute.getAlias())) {
				return attribute;
			}
		}
		throw new IllegalArgumentException("Attribute \"" + alias + "\" not found in refbook (id=" + id + ", \"" + name + "\"))");
	}

	/**
	 * Возвращает атрибут по его коду
	 * @param attributeId
	 * @return
	 */
	public RefBookAttribute getAttribute(Long attributeId) {
		for(RefBookAttribute attribute : attributes) {
			if (attributeId.equals(attribute.getId())) {
				return attribute;
			}
		}
		throw new IllegalArgumentException("Attribute id=" + attributeId + " not found in refbook (id=" + id + ", \"" + name + "\"))");
	}

	/**
	 * Проверяет, что справочник иерархичный. Проверка осуществляется по псевдонимам. Если есть атрибут
	 * с предопределнным для иерархии псевдонимом, то справочник считается иерархичным.
	 * @return
	 */
	public boolean isHierarchic() {
		for(RefBookAttribute attribute : attributes) {
			if (RECORD_PARENT_ID_ALIAS.equals(attribute.getAlias())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Создает "рыбу" для строки справочника
	 * @return
	 */
	public Map<String, RefBookValue> createRecord() {
		Map<String, RefBookValue> result = new HashMap<String, RefBookValue>();
		result.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, null));
		if (isHierarchic()) {
			result.put(RefBook.RECORD_PARENT_ID_ALIAS, new RefBookValue(RefBookAttributeType.NUMBER, null));
		}
		for (RefBookAttribute attribute : getAttributes()) {
			result.put(attribute.getAlias(), new RefBookValue(attribute.getAttributeType(), null));
		}
		return result;
	}

    /**
     * Id скрипта справочника в BLOB_DATA
     * @return
     */
    public String getScriptId() {
        return scriptId;
    }

    /**
     * Id скрипта справочника в BLOB_DATA
     * @param scriptId
     */
    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

	/**
	 * Возвращает признак видимости справочника
	 * @return true - виден; false - скрыт
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Устанавливает признак видимости справочника
	 * @param visible true - виден; false - скрыт
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	/** Возвращает упорядоченный список атрибутов для сортировки по умолчанию. В запросах пока используется только
	 * первый в списке, то есть мультисортировка пока не поддерживается.
	 * @return всегда определен, не может быть null
	 */
	private List<RefBookAttribute> getSortAttributes() {
		List<RefBookAttribute> defaultSort = new ArrayList<RefBookAttribute>();
		for (RefBookAttribute attr : attributes) {
			if (attr.getSortOrder() != null) {
				defaultSort.add(attr);
			}
		}
		if (!defaultSort.isEmpty()) {
			Collections.sort(defaultSort, new Comparator<RefBookAttribute>() {
				@Override
				public int compare(RefBookAttribute o1, RefBookAttribute o2) {
					return o2.getSortOrder() - o1.getSortOrder();
				}
			});
		}
		return defaultSort;
	}

	/** Возвращает атрибут для сортировки по умолчанию
	 * @return может быть null
	 */
	public RefBookAttribute getSortAttribute() {
		List<RefBookAttribute> list = getSortAttributes();
		return !list.isEmpty() ? list.get(0) : null;
	}

    public RefBookAttribute getRegionAttribute() {
        return regionAttribute;
    }

    public void setRegionAttribute(RefBookAttribute regionAttribute) {
        this.regionAttribute = regionAttribute;
    }

    @Override
    public String toString() {
        return "RefBook{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", scriptId='" + scriptId + '\'' +
                ", visible=" + visible +
                ", type=" + type +
                ", readOnly=" + readOnly +
                ", versioned=" + versioned +
                ", tableName='" + tableName + '\'' +
                '}';
    }

    public boolean isVersioned() {
        return versioned;
    }

    public void setVersioned(boolean versioned) {
        this.versioned = versioned;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isSimple(){
        return tableName != null;
    }

	public boolean addAttribute(RefBookAttribute attribute) {
		return this.attributes.add(attribute);
	}
}