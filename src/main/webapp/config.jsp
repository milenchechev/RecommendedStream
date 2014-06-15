<%--
    Document   : config.jsp
    Created on : Nov 13, 2012, 10:27:16 AM
    Author     : Milen
--%>

<%@page import="com.chechev.phd.utils.GlobalConstants"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Personalize your recommended stream</title>
        <link rel="stylesheet" type="text/css" href="css/config.css" />
        <script src="js/jquery-1.8.2.js"></script>
        <script src="js/friends_sort.js"></script>
        <script src="js/config.js"></script>
        <script src="js/jquery-ui-1.10.4.min.js"></script>
        <script src="js/ui_helper.js"></script>
        <script src="js/recsys.js"></script>
    </head>
    <body>
        <div id="fb-root"></div>
        <script type="text/javascript">
            window.fbAsyncInit = function() {
                // init the FB JS SDK
                FB.init({
                    appId      : '<%= GlobalConstants.APP_ID%>', // App ID from the App Dashboard
                    channelUrl : '//chechev.com/RecommendedStream/channel.html',
                    status     : true, // check the login status upon init?
                    cookie     : true, // set sessions cookies to allow your server to access the session?
                    xfbml      : true  // parse XFBML tags on this page?
                });

                // Additional initialization code such as adding Event Listeners goes here


				FB.login(function(response) {
        			if(response.status=='connected'){
        				$.ajax({
        					url:"data/posts/isnew?access_token="+response.authResponse.accessToken,
        					type:"GET"
        				}).done(function (data) {
        					if(data) {
        						window.location = "intro.jsp";
        					} else {
        						$(window).trigger("fbinit");
        					}
        				});
        			}
    			}, {
        			scope: 'email,read_stream,offline_access,friends_photos,friends_status,friends_likes'
    			});
            };

            // Load the SDK's source Asynchronously
            (function(d, debug){
                var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
                if (d.getElementById(id)) {return;}
                js = d.createElement('script'); js.id = id; js.async = true;
                js.src = "//connect.facebook.net/en_US/all" + (debug ? "/debug" : "") + ".js";
                ref.parentNode.insertBefore(js, ref);
            }(document, /*debug*/ false));
           </script>
    
    <center>
        <table
            style="background-image: url('images/logo.png'); height: 52px; width: 100%">
            <tr>
                <td width="60px"><a href="/RecommendedStream/">Home</a></td>
                <td>
            <center>
                <font size="5">Configuration</font>
            </center>
            </td>
            <td width="60px"><a href="/RecommendedStream/config.jsp">Config</a>
            </td>
            <td width="60px"><a href="/RecommendedStream/about.jsp">About</a>
            </td>
            </tr>
        </table>
        <table
            style="border-top: solid; border-bottom: solid; border-color: #7287B5; align: center">
            <tr>
                <td>
                	<div id="links">
	                	<a href="#friends"><h3>Friends</h3></a> <a href="#interesting"><h3>Items Marked Interesting</h3></a> <a href="#notinteresting"><h3>Items marked not interesting</h3></a>
	                </div>
                    <br />
                    <p id="label">Prefered Friends:</p>
                    <br />
                    	<div id="placeholder">
                        </div>
                    <br />
                </td>
            </tr>
        </table>
    </center>
</body>
</html>
