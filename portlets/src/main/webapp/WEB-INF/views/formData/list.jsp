<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<script type="text/javascript">
	dojo.require('dijit.form.Button');
	dojo.require('dijit.form.Select');
	dojo.require('dojo.parser');
	dojo.addOnLoad(function() {
		dojo.parser.parse();
	});
</script>
<table>
	<thead>
		<th style="width: 5em;">Id</th>
		<th style="width: 60em;">Тип формы</th>
	</thead>
	<tbody>	
<c:forEach items="${data}" var="item">
		<tr>
			<portlet:actionURL name="view" var="viewItemURL" portletMode="edit">
				<portlet:param name="id" value="${item.id}"/>
			</portlet:actionURL>
			<td><a href="${viewItemURL}">${item.id}</a></td>
			<td>${item.form.type.name}</td>			
		</tr>
</c:forEach>
	</tbody>
</table>
<portlet:actionURL var="createNewUrl" name="new" portletMode="edit"/>
<form action="${createNewUrl}" method="post" id="${namespace}_createNewForm">
	<label for="${namespace}_formSelect">Создать новую форму, тип: </label>
	<select dojoType="dijit.form.Select" name="formId" id="${namespace}_formSelect"><c:forEach items="${forms}" var="form">
		<option value="${form.id}">${form.type.name}</option>
	</c:forEach></select>
	<button dojoType="dijit.form.Button" onClick="dojo.byId('${namespace}_createNewForm').submit()">Создать</button>
</form>
