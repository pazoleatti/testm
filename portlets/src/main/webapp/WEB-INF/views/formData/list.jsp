<%@ page contentType="text/html; UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<script type="text/javascript">
	dojo.require('dijit.form.Button');
	dojo.require('dojo.parser');
</script>
<table>
	<thead>
		<th style="width: 5em;">Id</th>
		<th style="width: 30em;">Тип формы</th>
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
<portlet:actionURL var="createNewURL" name="new" portletMode="edit">
	<portlet:param name="formId" value="1"/>
</portlet:actionURL>
<button dojoType="dijit.form.Button" onClick="window.location.href = '${createNewURL}'">Добавить</button>