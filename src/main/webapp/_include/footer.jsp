<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<footer class="footer">
    <div class="container">
        <p>
            ${application.name} - version ${project.version}
            &copy; <a href="${organization.url}">${organization.name}</a>
        </p>
    </div>
</footer>


