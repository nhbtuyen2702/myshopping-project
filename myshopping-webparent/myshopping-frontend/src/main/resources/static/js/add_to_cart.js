$(document).ready(function() {
	//khi customer nhấn button Add To Cart để thêm product vào cart
	$("#buttonAdd2Cart").on("click", function(evt) {
		addToCart();
	});
});

function addToCart() {
	quantity = $("#quantity" + productId).val();
	url = contextPath + "cart/add/" + productId + "/" + quantity;//update lại quantity xuống db
	
	/*
	 _csrf là một token bảo mật được sử dụng để xác thực các request từ user 
	 Khi user truy cập vào một trang web, một mã _csrf sẽ được tạo và lưu trữ trong cookie của user 
	 Khi user thực hiện một request, mã _csrf sẽ được gửi kèm theo request đó để xác thực người dùng. 
	 Nếu mã _csrf không hợp lệ hoặc không khớp, request sẽ bị từ chối 
	*/
	$.ajax({
		type: "POST",
		url: url,
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeaderName, csrfValue);
		}
	}).done(function(response) {
		showModalDialog("Shopping Cart", response);
	}).fail(function() {
		showErrorModal("Error while adding product to shopping cart.");
	});
}