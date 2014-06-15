$(window).on('hashchange', function () {
	showEntityByHash(window.location.hash)
});

$(window).on('fbinit', function () {
	showEntityByHash(window.location.hash);
});

function showEntityByHash(hash) {
	var $placeholder = $('#placeholder'),
	$label = $('#label');

	$placeholder.empty();

	if(hash === "#interesting") {
		showInterestingFeed($placeholder, $label);
	} else if(hash === "#notinteresting") {
		showNotInterestingFeed($placeholder, $label);
	} else {
		showFriends($placeholder, $label);
	}
}

function showFriends($placeholder, $label){
	$label.text('Prefered Friends:');
	$placeholder.removeClass("friends").removeClass("interesting").removeClass("notInteresting").addClass("friends");
	FB.login(function(response) {
        if(response.status=='connected'){
            FB.api('/me', function(response) {
            	window.friends.fetchAndShowFriends(response.id);
            });
        }
    }, {
        scope: 'email,read_stream,offline_access,friends_photos,friends_status,friends_likes'
    });	
}

function showInterestingFeed($placeholder, $label) {
	showFeed($placeholder, $label, "data/posts/interesting/", "Items, that are marked as interesting:", "interesting");
}


function showNotInterestingFeed($placeholder, $label){
	showFeed($placeholder, $label, "data/posts/uninteresting/", "Items, that are marked as not interesting:", "notInteresting");
}

function showFeed($placeholder, $label, servicePath, labelText, placeholderClass) {
	$label.text(labelText);
	$placeholder.removeClass("friends").removeClass("interesting").removeClass("notInteresting").addClass(placeholderClass);
	if($placeholder.hasClass('ui-sortable')){
		$placeholder.sortable('destroy');
	}
	FB.login(function(response) {
        if(response.status=='connected'){
            FB.api('/me', function(response) {
            	$.get(servicePath + response.id).done(function (posts) {
            		for(var i = 0; i < posts.length; ++i) {
            			$placeholder.append(postToHtml(JSON.parse(posts[i]), response.id));
            		}
            		if(!$placeholder.children().length) {
            			$label.text("No items to be displayed.");
            		}
            		$placeholder.find('.mark').remove();
            		initUnmarkButtons($placeholder, $label, response.id);
            	});
            });
        }
    }, {
        scope: 'email,read_stream,offline_access,friends_photos,friends_status,friends_likes'
    });
}

function initUnmarkButtons($placeholder, $label, userId) {
	$placeholder.find('.unmarkPost').css('display', 'inline').click(function (event) {
		var $this = $(this);
		$.get("data/posts/unmark?user_id=" + userId + "&post_id=" + $(this).attr('id')).done(function () {
			$this.closest('.postContainer').remove();
			if(!$placeholder.find('.postContainer').length) {
    			$label.text("No items to be displayed.");
    		}
		});
	});	
}