<%@page import="com.chechev.phd.utils.GlobalConstants"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
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