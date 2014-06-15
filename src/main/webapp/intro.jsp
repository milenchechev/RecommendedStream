<%--
    Document   : intro.jsp
    Created on : Nov 14, 2012, 12:18:37 AM
    Author     : Milen
--%>

<%@page import="com.chechev.phd.utils.GlobalConstants"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Intro page</title>
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css">
<script type="text/javascript" src="js/intro.js"></script>
<script src="js/jquery-1.8.2.js"></script>
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
        	
        	FB.login(function(response) {
            	if (response.status == 'connected') {
            		startPollingFeed(response.authResponse.accessToken);
				}
            },
            {
            	scope : 'email,read_stream,offline_access,friends_photos,friends_status,friends_likes'
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

   <table
      style="background-image: url('images/logo.png'); height: 52px; width: 100%">
      <tr>
         <td>
             <center>
                <font size="5">Recommended Stream Application</font>
            </center>
         </td>
      </tr>
   </table>

   <center>
   <table
      style="border-top: solid; border-bottom: none; border-color: #7287B5">
      <tr>
         <td style="font-size: 12px;"><br>
            <p>Hello and welcome,</p>
            <p>The Recommended Stream application is designed to help user's
               work and life at the social network Facebook.</p>
            <p>It's main idea is to provide services to the users that will
               assist their everyday interaction with the social network.</p>
            The main services that the application is design to provide are:
            <ul style="list-style-type: circle">
            	<li> easy to use and configurable fully functional Facebook news feed reader.</li>
            	<li>search functionality at the news feed.</li>
            	<li>user profile that is automatically adjusted to your last activities, but can be manually changed</li>
            	<li>option to manually or automatically re-rank your news feed to answer your preferences.</li>
            </ul>

            <p>Current version provide just the basic functionality.
               Please, do not hesitate to report bugs or suggestions directly to
               the author. Contacts can be found at the about page.</p>

            <p style="width: 100%; margin: 0 auto; text-align: center;">Video tutorial:<br/>
            	<iframe width="420" height="315" src="//www.youtube.com/embed/w9QFY9XbCy4" frameborder="0" allowfullscreen></iframe>
			</p>
			
			<div class="progress progress-striped active" style="width: 100%; margin-top: 15px;margin-bottom: 0px !important;">
  				<div class="progress-bar progress-bar-info" role="progressbar" aria-valuenow="100" aria-valuemin="0" aria-valuemax="100" style="width: 100%;">
    				Loading your news feed, please wait...
  				</div>
			</div>
			
			<div style="width: 100%;text-align: center;">
				<button type="button" class="btn btn-success goButton" style="display: none;margin: 0 auto;">Go to application</button>
			</div>
		 </td>
      </tr>
   </table>
       </center>
</body>
</html>
