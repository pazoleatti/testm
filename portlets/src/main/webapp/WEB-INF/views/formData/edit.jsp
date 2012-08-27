<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:set var="namespace"><portlet:namespace/></c:set>
<script type="text/javascript">
	dojo.require('dojox.grid.DataGrid');
	dojo.require('dojo.data.ItemFileWriteStore');
	dojo.require('dijit.Dialog');
	dojo.require('dijit.form.Button');
	dojo.require('dijit.form.TextBox');
	dojo.require('dijit.form.NumberTextBox');
	dojo.require('dijit.form.DateTextBox');
	dojo.require('dojo.parser');
	dojo.addOnLoad(function() { dojo.parser.parse();});	
</script>
<style type="text/css"><%-- TODO: переделать импорт CSS --%>
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/Grid.css";
	@import "/portal_dojo/v1.4.3/dojox/grid/resources/tundraGrid.css";
</style>
<portlet:resourceURL id="dataRows" var="storeURL"/>
<div id="${namespace}_store" jsid="${namespace}_store" dojoType="dojo.data.ItemFileWriteStore" url="${storeURL}"></div>
<table id="${namespace}_grid" 
		jsid="${namespace}_grid" 
		dojoType="dojox.grid.DataGrid" 
		store="${namespace}_store"
		rowsPerPage="100"
		autoHeight="true">
	<thead>
		<tr>
			<c:forEach var="column" items="${formData.form.columns}">
				<th field="${column.alias}" width="${column.width}em" title="${column.name}" editable="true">${column.name}</th>
			</c:forEach>
		</tr>
	</thead>
	<script type="dojo/connect" event="onRowDblClick" args="evt">
		var item = evt.grid.getItem(evt.rowIndex);
		<c:forEach var="column" items="${formData.form.columns}">
			<c:set var="fieldId" value="${namespace}_rowDialogField_${column.alias}"/>
			dijit.byId('${fieldId}').attr('value', ${namespace}_store.getValue(item, '${column.alias}'));
		</c:forEach>
		${namespace}_rowDialog.item = item;
		${namespace}_rowDialog.show();
	</script>	
</table>
<div dojoType="dijit.Dialog" jsid="${namespace}_rowDialog">
	<table width="500px">
		<thead>
			<col width="50%"/>
			<col width="50%"/>
		</thead>
		<tbody>
			<c:forEach var="column" items="${formData.form.columns}"><tr>
				<td><c:out value="${column.name}"/></td>
				<td>
					<c:set var="fieldId" value="${namespace}_rowDialogField_${column.alias}"/>
					<c:choose>
						<c:when test="${column.class.name eq 'com.aplana.sbrf.taxaccounting.model.StringColumn'}">
							<input dojoType="dijit.form.TextBox" id="${fieldId}"/>
						</c:when>
						<c:when test="${column.class.name eq 'com.aplana.sbrf.taxaccounting.model.NumericColumn'}">
							<input dojoType="dijit.form.NumberTextBox" id="${fieldId}" constraints="{places: ${column.precision}}"/>
						</c:when>
						<c:when test="${column.class.name eq 'com.aplana.sbrf.taxaccounting.model.DateColumn'}">
							<input dojoType="dijit.form.DateTextBox" id="${fieldId}"/>
						</c:when>
						<c:otherwise>
							<strong>Неизвестный тип столбца: ${column.class.name}</strong>						
						</c:otherwise>
					</c:choose>
				</td>
			</tr></c:forEach>
		</tbody>
	</table>	
	<button dojoType="dijit.form.Button" onClick="${namespace}_rowDialog.hide()">ОК
		<script type="dojo/connect" event="onClick" args="evt">
			var item = ${namespace}_rowDialog.item; 
			if (item == null) {
				var record = { alias: ${namespace}_grid.attr('rowCount') + 1};
				item = ${namespace}_store.newItem(record);
			}
			<c:forEach var="column" items="${formData.form.columns}">
				${namespace}_store.setValue(
					item, '${column.alias}', 
					dijit.byId('${namespace}_rowDialogField_${column.alias}').attr('value')
				);
			</c:forEach>				
			${namespace}_store.save();
			${namespace}_rowDialog.hide();
		</script>
	</button>
	<button dojoType="dijit.form.Button" onClick="${namespace}_rowDialog.hide()">Отмена</button>
</div>
<button dojoType="dijit.form.Button">Добавить строку
	<script type="dojo/connect" event="onClick" args="evt">
		<c:forEach var="column" items="${formData.form.columns}">
			<c:set var="fieldId" value="${namespace}_rowDialogField_${column.alias}"/>
			dijit.byId('${fieldId}').attr('value', '');
		</c:forEach>
		${namespace}_rowDialog.item = null;
		${namespace}_rowDialog.show();
	</script>
</button>
<portlet:actionURL name="save" var="saveURL"/>
<button dojoType="dijit.form.Button">Сохранить
	<script type="dojo/connect" event="onClick" args="evt">
		window.location.href = '${saveURL}';
	</script>
</button>

<portlet:renderURL portletMode="view" windowState="normal" var="backURL" />
<a href="${backURL}">Назад</a>