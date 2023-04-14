// Sales Report by Product
var data;
var chartOptions;

$(document).ready(function() {
	setupButtonEventHandlers("_product", loadSalesReportByDateForProduct);	
});

function loadSalesReportByDateForProduct(period) {
	if (period == "custom") {
		startDate = $("#startDate_product").val();
		endDate = $("#endDate_product").val();
		
		requestURL = contextPath + "reports/product/" + startDate + "/" + endDate;
	} else {
		requestURL = contextPath + "reports/product/" + period;		
	}
	
	$.get(requestURL, function(responseJSON) {
		prepareChartDataForSalesReportByProduct(responseJSON);
		customizeChartForSalesReportByProduct();
		formatChartData(data, 2, 3);
		drawChartForSalesReportByProduct(period);
		setSalesAmount(period, '_product', "Total Products");
	});
}

function prepareChartDataForSalesReportByProduct(responseJSON) {
	data = new google.visualization.DataTable();
	data.addColumn('string', 'Product');
	data.addColumn('number', 'Quantity');
	data.addColumn('number', 'Gross Sales');
	data.addColumn('number', 'Net Sales');
	
	totalGrossSales = 0.0;
	totalNetSales = 0.0;
	totalItems = 0;
	
	//[reportItem.identifier, reportItem.productsCount, reportItem.grossSales, reportItem.netSales] là 4 cột được hiển thị trong 1 dòng
	$.each(responseJSON, function(index, reportItem) {
		data.addRows([[reportItem.identifier, reportItem.productsCount, reportItem.grossSales, reportItem.netSales]]);
		totalGrossSales += parseFloat(reportItem.grossSales);
		totalNetSales += parseFloat(reportItem.netSales);
		totalItems += parseInt(reportItem.productsCount);
	});
}

function customizeChartForSalesReportByProduct() {
	chartOptions = {
		height: 360, width: '98%',	//Chiều cao (height) của biểu đồ là 360px và Chiều rộng (width) của biểu đồ là 98% của kích thước của phần tử chứa biểu đồ trên trang web.
		showRowNumber: true, //hiển thị thứ tự của các dòng
		page: 'enable',//Cho phép phân trang (page) 
		sortColumn: 2,//sắp xếp các dòng theo cột số 2(reportItem.grossSales)
		sortAscending: false //sắp xếp tăng dần theo cột số 2
	};
}

function drawChartForSalesReportByProduct() {
	var salesChart = new google.visualization.Table(document.getElementById('chart_sales_by_product'));//vẽ biểu đồ cột
	salesChart.draw(data, chartOptions);
}