$(document).ready(function() {
	$("#productList").on("click", ".linkRemove", function(e) {
		e.preventDefault();
		
		if (doesOrderHaveOnlyOneProduct()) {//nếu chỉ còn duy nhất 1 orderDetail thì ko cho xóa, vì ít nhất phải có 1 orderDetail 
			showWarningModal("Could not remove product. The order must have eat least one product.");
		} else {
			removeProduct($(this));//xóa orderDetail dựa vào id		
			updateOrderAmounts();	
		}
	});
});

function removeProduct(link) {
	rowNumber = link.attr("rowNumber");
	$("#row" + rowNumber).remove();
	$("#blankLine" + rowNumber).remove();
	
	$(".divCount").each(function(index, element) {//cập nhật lại toàn bộ count
		element.innerHTML = "" + (index + 1);
	});
}

function doesOrderHaveOnlyOneProduct() {
	productCount = $(".hiddenProductId").length;
	return productCount == 1;
}