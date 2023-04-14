var dropdownCountries;
var dropdownStates;

$(document).ready(function() {
	dropdownCountries = $("#country");
	dropdownStates = $("#listStates");

	dropdownCountries.on("change", function() {
		loadStates4Country();
		$("#state").val("").focus();
	});	
	
	loadStates4Country();
});

//chọn country sẽ load toàn bộ states của country này
function loadStates4Country() {
	selectedCountry = $("#country option:selected");//lấy ra country đang được chọn
	countryId = selectedCountry.val();
	url = contextPath + "states/list_by_country/" + countryId;
	
	$.get(url, function(responseJson) {
		dropdownStates.empty();//xóa tất cả giá trị trong dropdown states hiện tại
		
		$.each(responseJson, function(index, state) {
			$("<option>").val(state.name).text(state.name).appendTo(dropdownStates);
		});
	}).fail(function() {
		showErrorModal("Error loading states/provinces for the selected country.");
	})	
}	