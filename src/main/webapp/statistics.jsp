<%--
    Document   : newjsp
    Created on : Dec 29, 2012, 12:40:38 PM
    Author     : Milen
--%>

<%@page import="java.util.List"%>
<%@page import="com.chechev.phd.recommendedstream.services.XPostWS"%>
<%@page import="com.chechev.phd.utils.GlobalConstants"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <script src="js/jquery-1.8.2.js"></script>
        <script src="js/recsys.js"></script>
        <script src="js/ui_helper.js"></script>
        <title>Recommended Stream</title>

    </head>
    <body>
        <div id="fb-root"></div>
        <script>
            window.fbAsyncInit = function() {
                // init the FB JS SDK
                FB.init({
                    appId      : '<%= GlobalConstants.APP_ID%>', // App ID from the App Dashboard
                    channelUrl : '//94.26.46.180:8080/RecommendedStream/channel.html',
                    status     : true, // check the login status upon init?
                    cookie     : true, // set sessions cookies to allow your server to access the session?
                    xfbml      : true  // parse XFBML tags on this page?
                });

                // Additional initialization code such as adding Event Listeners goes here

                FB.login(function(response) {
                    if(response.status=='connected'){
                        console.log(response.authResponse.accessToken);
                        updateFunction(response.authResponse.accessToken);
                        setInterval(function(){updateFunction(response.authResponse.accessToken)},30*1000);

                        /*$.ajax({url:"data/posts/pesho", data:response.authResponse.accessToken,contentType : "application/json",dataType : "application/json", type:"POST" ,
                        statusCode: {
                            201: function() {
                              console.log("+++");

                            }
                          }});*/

                       // $.post("data/posts/pesho","{ 'func': 'getNameAndTime' }",
                       // function(data){
                       //   console.log(data); // John
                       // }, "json");
                    }else{
                        console.log(response);
                    }
                    // handle the response
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
        <table style="background-image:url('images/logo.png'); height: 52px; width: 100%">
            <tr>
                <td width="60px">
                    <a href="/RecommendedStream/">Home</a>
                </td>
                <td>
            <center>
                <font size="5">Statistics</font>
            </center>
        </td>
        <td width="60px">
            <a href="/RecommendedStream/config.jsp">Config</a>
        </td>
        <td width="60px">
            <a href="/RecommendedStream/about.jsp">About</a>
        </td>
    </tr>
</table>
    <center>
        <%
            List<String> list =XPostWS.statistics();
            for(String string:list){
                out.println(string + " <br/>");
            }

        %>
    </center>
</body>
</html>
