var pollTimer;

function startPollingFeed(accessToken){
	pollTimer = setInterval(function () {
		pollFeed(accessToken);
	}, 10000);
}

function pollFeed(accessToken){
	$.get("data/posts/get?access_token=" + accessToken + "&since=" + (new Date().getTime() - 24 * 60 * 60 * 1000) + "&query=''&initial_load=true").done(function (data) {
		if(data && data.length) {
			clearInterval(pollTimer);
			$('.progress').hide();
			$('.goButton').show();
			$('.goButton').on('click', function () {
				window.location = "index.jsp?firstvisit=no";	
			});
		}
	}).error(function (error) {
		console.log('feed polling error is');
		console.dir(error);
	});
}
