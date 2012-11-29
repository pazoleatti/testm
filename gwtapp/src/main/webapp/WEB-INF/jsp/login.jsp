<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>АС "Учёт налогов"</title>
</head>
<body>
<div style="margin: auto; width: 400px">
<form method="POST" action="j_security_check" style="width: 100%">
    <table>
        <tbody>
        <tr>
            <td><label for="j_username">Имя пользователя</label></td>
            <td><input type="text" name="j_username" id="j_username"/></td>
        </tr>
        <tr>
            <td><label for="j_password">Пароль</label></td>
            <td><input type="password" name="j_password" id="j_password"/></td>
        </tr>
        <tr>
            <td colspan="2" style="text-align: right;"><input type="submit" value="Войти"/></td>
        </tr>
        </tbody>
    </table>
</form>
</div>
</body>
</html>