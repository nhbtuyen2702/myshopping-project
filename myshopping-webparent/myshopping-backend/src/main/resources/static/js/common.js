$(document).ready(function() {
	$("#logoutLink").on("click", function(e) {
		e.preventDefault();
		document.logoutForm.submit();//<form th:action="@{/logout}" method="post" name="logoutForm"/>
	});
	
	customizeDropDownMenu();
	customizeTabs();
});

function customizeDropDownMenu() {
	$(".navbar .dropdown").hover(//lấy ra tất cả thẻ có class .dropdown nằm trong thẻ có class .navbar
		function() {
			$(this).find('.dropdown-menu').first().stop(true, true).delay(250).slideDown();//tìm thẻ con đầu tiên bên trong có class là dropdown-menu và kéo xuống
		},
		function() {
			$(this).find('.dropdown-menu').first().stop(true, true).delay(100).slideUp();//tìm thẻ con đầu tiên bên trong có class là dropdown-menu và kéo lên
		}
	);
	
	$(".dropdown > a").click(function() {
		location.href = this.href;
	});
}

function customizeTabs() {
	// Javascript to enable link to tab
	var url = document.location.toString();
	if (url.match('#')) {
	    $('.nav-tabs a[href="#' + url.split('#')[1] + '"]').tab('show');
	} 

	// Change hash for page-reload
	$('.nav-tabs a').on('shown.bs.tab', function (e) {
	    window.location.hash = e.target.hash;
	})	
}