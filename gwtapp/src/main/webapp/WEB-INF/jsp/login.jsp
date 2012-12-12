<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>АС "Учёт налогов"</title>
</head>
<body>
<table width="100%" style="margin-top: 200px;">
	<col width="*"/>
	<col width="400px"/>
	<col width="*"/>
	<tbody>
	<tr>
		<td>&nbsp;</td>
		<td>
			<form method="POST" action="j_security_check" style="width: 100%; border: solid #006400 1px; background-color: #efd; padding: 20px;" >
				<table width="100%">
					<col width="50%"/>
					<col width="50%" align="right"/>
					<tbody>
					<tr>
						<td><label for="j_username">Имя пользователя</label></td>
						<td><input type="text" name="j_username" id="j_username" style="width: 100%"/></td>
					</tr>
					<tr>
						<td><label for="j_password">Пароль</label></td>
						<td><input type="password" name="j_password" id="j_password" style="width: 100%"/></td>
					</tr>
					<tr>
						<td colspan="2" align="right"><input type="submit" value="Войти"/></td>
					</tr>
					</tbody>
				</table>
			</form>
		</td>
		<td>&nbsp;</td>
	</tr>
	</tbody>
</table>
</body>
</html>