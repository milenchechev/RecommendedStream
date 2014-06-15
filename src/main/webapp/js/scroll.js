$(window).on('scroll', _.debounce(scrollHandler, 200));

function scrollHandler(){
	var top = $(window).scrollTop(),
		bottom = top + $(window).height(),
		viewedIds = [];
	
	$('.postContainer').filter(function () {
		var offset = $(this).offset(),
			height = $(this).outerHeight();
		
		return (top <= offset.top && bottom >= offset.top + height) || (top >= offset.top && bottom <= offset.top + height);
	}).each(function () {
		viewedIds.push($(this).attr("id"));
	});
	
	$.get("data/posts/viewed/?access_token=" + $("#accessToken").html() + "&ids=" + JSON.stringify(viewedIds));
}