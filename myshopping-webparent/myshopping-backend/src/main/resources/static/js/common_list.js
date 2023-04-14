$(document).ready(function () {//khi users.html được load lên thì nó sẽ chạy vào phương thức ready() này, ready() dùng để khai báo các sự kiện(event) cho các thẻ
    $(".link-delete").on("click", function (e) {//$(".link-delete") lấy ra thẻ có class là link-delete -->thẻ <a>, bắt sự kiện click cho thẻ <a> này
       e.preventDefault();//ko cho href xảy ra -->ko gọi xuống controller
       showDeleteConfirmModal($(this), entityName);//$(this) chính là thẻ hiện tại, $(this) là 1 đối tượng JQuery -->có thể sử dụng các phương thức của JQuery
    });
 });

function clearFilter() {
	window.location = moduleURL;
}

function showDeleteConfirmModal(link, entityName) {
	entityId = link.attr("entityId");//link là 1 đối tượng JQuery -->lấy ra giá trị của thuộc tính entityId

	$("#yesButton").attr("href", link.attr("href"));//gán giá trị của thuộc tính href vào thuộc tính href của thẻ có id là yesButton

	$("#confirmText").text("Are you sure you want to delete this "
		+ entityName + " ID " + entityId + "?");//thay đổi nội dung của thẻ có id là confirmText

	$("#confirmModal").modal();//hiển thị modal
}

function handleDetailLinkClick(cssClass, modalId) {
	$(cssClass).on("click", function(e) {
		e.preventDefault();
		linkDetailURL = $(this).attr("href");
		$(modalId).modal("show").find(".modal-content").load(linkDetailURL);
	});		
}

function handleDefaultDetailLinkClick() {
	handleDetailLinkClick(".link-detail", "#detailModal");	
}