package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.io.Serializable;

/**
 * Вид декларации.
 * @author dsultanbekov
 */
public class DeclarationType implements Serializable {
	private static final long serialVersionUID = 1L;

	//РНУ_НДФЛ (первичная)
	public final static int NDFL_PRIMARY = 100;
	//РНУ_НДФЛ (консолидированная)
	public final static int NDFL_CONSOLIDATE = 101;
	//6-НДФЛ
	public final static int NDFL_6 = 103;
	public final static String NDFL_6_NAME = "6-НДФЛ";
	//2-НДФЛ (1)
	public final static int NDFL_2_1 = 102;
	public final static String NDFL_2_1_NAME = "2-НДФЛ (1)";
	//2-НДФЛ (2)
	public final static int NDFL_2_2 = 104;
	public final static String NDFL_2_2_NAME = "2-НДФЛ (2)";
	
	private int id;
	private String name;
    private VersionedObjectStatus status;
    private Integer versionsCount;

    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    /**
	 * Получить идентификатор
	 * @return идентфикатор
	 */
	public int getId() {
		return id;
	}

	/**
	 * Задать идентификатор
	 * @param id значение идентификатора
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Получить название вида декларации
	 * @return название вида декларации
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Задать название вида декларации
	 * @param name название вида декларации
	 */
	public void setName(String name) {
        this.name = StringUtils.cleanString(name);
	}

	public Integer getVersionsCount() {
		return versionsCount;
	}

	public void setVersionsCount(Integer versionsCount) {
		this.versionsCount = versionsCount;
	}

	@Override
    public String toString() {
        return "DeclarationType{" +
                "name='" + name + '\'' +
                '}';
    }
}
