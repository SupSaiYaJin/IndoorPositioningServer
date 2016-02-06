<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>BUPT室内定位系统</title>
</head>
<body>
<p>欢迎使用BUPT室内定位系统</p>
<form method="post" action="positioning/uploadmap" enctype="multipart/form-data">
    地图名:<input type="text" name="sceneName"/><br/>
    比例尺:<input type="text" name="scale"/><br/>
    选择文件:<input type="file" name="sceneMap"/><br/>
    <input type="submit" value="上传图片" />
</form>
</body>
</html>
