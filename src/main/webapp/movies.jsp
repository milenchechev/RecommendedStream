<%@page import="com.chechev.phd.utils.GlobalConstants"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <jsp:include page="partials/head.jsp"/>
    <body>
        <jsp:include page="partials/fb.jsp"/>
    
    <center>
        <jsp:include page="partials/menu.jsp"/>
        <table
            style="border-top: solid; border-bottom: solid; border-color: #7287B5; align: center">
            <tr>
                <td>
                    <br />
                    <p id="label">Recommended movies:</p>
                    <br />
                    	<div id="placeholder">
                        </div>
                    <br />
                </td>
            </tr>
        </table>
    </center>
</body>
<script type="text/javascript">
$(window).on('fbinit', function () {
	FB.login(function(response) {
        if(response.status=='connected'){
            FB.api('/me', function(response) {
            	window.movies.fetchAndShowMovies(response.id);
            });
        }
    }, {
        scope: 'email,read_stream,offline_access,friends_photos,friends_status,friends_likes'
    });
});
</script>
</html>
