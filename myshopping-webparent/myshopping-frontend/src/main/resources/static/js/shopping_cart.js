decimalSeparator = decimalPointType == 'COMMA' ? ',' : '.';
thousandsSeparator = thousandsPointType == 'COMMA' ? ',' : '.'; 

$(document).ready(function() {
	$(".linkMinus").on("click", function(evt) {//khi bấm giảm số lượng product trong cartItem thì sẽ vào phương thức này
		evt.preventDefault();
		decreaseQuantity($(this));
	});
	
	$(".linkPlus").on("click", function(evt) {//khi bấm tăng số lượng product trong cartItem thì sẽ vào phương thức này
		evt.preventDefault();
		increaseQuantity($(this));
	});
	
	$(".linkRemove").on("click", function(evt) {//khi bấm xóa cartItem thì sẽ vào phương thức này
		evt.preventDefault();
		removeProduct($(this));
	});		
});

function decreaseQuantity(link) {//link là đối tượng JQuery
	productId = link.attr("pid");
	quantityInput = $("#quantity" + productId);
	newQuantity = parseInt(quantityInput.val()) - 1;//lấy quantity hiện tại - 1
	
	if (newQuantity > 0) {
		quantityInput.val(newQuantity);
		updateQuantity(productId, newQuantity);//cập nhật lại quantity dưới db
	} else {
		showWarningModal('Minimum quantity is 1');
	}	
}

function increaseQuantity(link) {//link là đối tượng JQuery
	productId = link.attr("pid");
	quantityInput = $("#quantity" + productId);
	newQuantity = parseInt(quantityInput.val()) + 1;//lấy quantity hiện tại + 1
	
	if (newQuantity <= 5) {
		quantityInput.val(newQuantity);
		updateQuantity(productId, newQuantity);//cập nhật lại quantity dưới db
	} else {
		showWarningModal('Maximum quantity is 5');
	}	
}

function updateQuantity(productId, quantity) {//cập nhật lại quantity dưới db
	url = contextPath + "cart/update/" + productId + "/" + quantity;
	
	$.ajax({
		type: "POST",
		url: url,
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeaderName, csrfValue);
		}
	}).done(function(updatedSubtotal) {
		updateSubtotal(updatedSubtotal, productId);//khi tăng/giảm số lượng product thì cập nhật lại subtotal 
		updateTotal();//cập nhật lại total
	}).fail(function() {
		showErrorModal("Error while updating product quantity.");
	});	
}

function updateSubtotal(updatedSubtotal, productId) {//khi tăng/giảm số lượng product thì cập nhật lại subtotal 
	$("#subtotal" + productId).text(formatCurrency(updatedSubtotal));
}

function updateTotal() {//khi tăng/giảm số lượng product hoặc xóa cartItem thì cập nhật lại total 
	total = 0.0;
	productCount = 0;
	
	$(".subtotal").each(function(index, element) {//lấy ra tất cả thẻ có class là subtotal -->cộng tất cả giá trị của các thẻ này sẽ ra được total
		productCount++;
		total += parseFloat(clearCurrencyFormat(element.innerHTML));//xóa dấu , để có thể chuyển từ String thành Float
	});
	
	if (productCount < 1) {//nếu productCount < 1 -->tất cả products đã bị xóa khỏi cart -->ẩn div sectionTotal và hiển thị div sectionEmptyCartMessage
		showEmptyShoppingCart();
	} else {
		$("#total").text(formatCurrency(total));//cập nhật lại total, thêm dấu , vào hàng nghìn của phần nguyên
	}
}

function showEmptyShoppingCart() {
	$("#sectionTotal").hide();//ẩn nội dung div bên phải
	$("#sectionEmptyCartMessage").removeClass("d-none");//hiển thị lại "You have not chosen any products yet."
}

function removeProduct(link) {//xóa cartItem trong db
	url = link.attr("href");

	$.ajax({
		type: "DELETE",
		url: url,
		beforeSend: function(xhr) {
			xhr.setRequestHeader(csrfHeaderName, csrfValue);
		}
	}).done(function(response) {
		rowNumber = link.attr("rowNumber");
		removeProductHTML(rowNumber);//xóa cartItem
		updateTotal();//cập nhật lại total
		updateCountNumbers();//khi xóa 1 cartItem ra khỏi cart thì phải cập nhật lại index của tất cả các cartItems còn lại
		
		showModalDialog("Shopping Cart", response);//hiện modal thông báo đã xóa cartItem khỏi cart
		
	}).fail(function() {
		showErrorModal("Error while removing product.");
	});				
}

function removeProductHTML(rowNumber) {
	$("#row" + rowNumber).remove();//xóa cartItem
	$("#blankLine" + rowNumber).remove();//xóa khoảng trắng giửa các cartItems
}

function updateCountNumbers() {//khi xóa 1 cartItem ra khỏi cart thì phải cập nhật lại index của tất cả các cartItems còn lại
	$(".divCount").each(function(index, element) {
		element.innerHTML = "" + (index + 1);//index bắt đầu từ 0 --> + 1
	}); 
}

function formatCurrency(amount) {
	return $.number(amount, decimalDigits, decimalSeparator, thousandsSeparator);//phương thức này dùng , và . thay vì COMMA và POINT
}

function clearCurrencyFormat(numberString) {
	result = numberString.replaceAll(thousandsSeparator, "");//thay tất cả dấu , thành "" để có thể chuyển từ String thành Float
	return result.replaceAll(decimalSeparator, ".");
}
