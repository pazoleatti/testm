/**
 * 
 */
package com.aplana.sbrf.taxaccounting.model;

import java.util.Collection;

/**
 * Утилиты для работы с моделью
 * @author sgoryachkin
 *
 */
public class ModelUtils {

	private ModelUtils() {}
	
	public static interface GetPropertiesFunc<T>{
		Object getProperties(T object);
	}
	
	/**
	 * Поиск объекта в коллекции по значению свойства
	 * @param collection
	 * @param value
	 * @param func
	 * @return
	 */
	public static <T> T findByProperties(Collection<T> collection, Object value, GetPropertiesFunc<T> func){
		for (T object : collection) {
			if (value.equals(func.getProperties(object))){
				return object;
			}
		}
		return null;
	}
	
	//mapByProperties(){
	//	
	//}

}
