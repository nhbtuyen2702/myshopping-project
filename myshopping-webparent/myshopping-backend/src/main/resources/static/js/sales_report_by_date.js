// Sales Report by Date
var data;
var chartOptions;
var totalGrossSales;
var totalNetSales;
var totalItems;

$(document).ready(function() {
	setupButtonEventHandlers("_date", loadSalesReportByDate);	
});

function loadSalesReportByDate(period) {
	if (period == "custom") {
		startDate = $("#startDate_date").val();
		endDate = $("#endDate_date").val();
		
		requestURL = contextPath + "reports/sales_by_date/" + startDate + "/" + endDate;
	} else {
		requestURL = contextPath + "reports/sales_by_date/" + period;//khi chọn Custom Date Range thì period là sẽ undefined thì ko khai báo 		
	}
	
	$.get(requestURL, function(responseJSON) {
		prepareChartDataForSalesReportByDate(responseJSON);
		customizeChartForSalesReportByDate(period);
		formatChartData(data, 1, 2);
		drawChartForSalesReportByDate(period);
		setSalesAmount(period, '_date', "Total Orders");
	});
}

function prepareChartDataForSalesReportByDate(responseJSON) {
	data = new google.visualization.DataTable();
	data.addColumn('string', 'Date');
	data.addColumn('number', 'Gross Sales');
	data.addColumn('number', 'Net Sales');
	data.addColumn('number', 'Orders');
	
	totalGrossSales = 0.0;
	totalNetSales = 0.0;
	totalItems = 0;
	
	//[reportItem.identifier, reportItem.grossSales, reportItem.netSales, reportItem.ordersCount
	//reportItem.identifier là trục dọc(giá trị thứ 0 của mảng)
	//reportItem.grossSales là trục ngang(giá trị thứ 1 của mảng)
	//reportItem.netSales là trục ngang(giá trị thứ 2 của mảng)
	//reportItem.ordersCount là trục ngang(giá trị thứ 3 của mảng)
	$.each(responseJSON, function(index, reportItem) {
		data.addRows([[reportItem.identifier, reportItem.grossSales, reportItem.netSales, reportItem.ordersCount]]);
		totalGrossSales += parseFloat(reportItem.grossSales);
		totalNetSales += parseFloat(reportItem.netSales);
		totalItems += parseInt(reportItem.ordersCount);
	});
}

function customizeChartForSalesReportByDate(period) {
	chartOptions = {
		title: getChartTitle(period),//tiêu đề
		'height': 360, //độ cao của cột
		legend: {position: 'top'},//chú thích sẽ nằm ở trên
		
		series: { //series dùng để cấu hình các trục ngang(0:reportItem.grossSales, 1:reportItem.netSales, 2:reportItem.ordersCount)  
			0: {targetAxisIndex: 0}, //cột Gross Sales(0) sẽ được so sánh với bên trái(0)
			1: {targetAxisIndex: 0}, //cột Net Sales(1) sẽ được so sánh với bên trái(0)
			2: {targetAxisIndex: 1, type: 'line'}, //cột Orders(2) sẽ được so sánh với bên phải(1)
		},
		
		vAxes: { //vAxes đề cấu hình trục dọc(0: bên trái, 1: bên phải)
			0: {title: 'Sales Amount', format: 'currency'}, //tiêu đề bên trái
			1: {title: 'Number of Orders'} //tiêu đề bên phải
		}
	};
}

function drawChartForSalesReportByDate() {
	var salesChart = new google.visualization.ColumnChart(document.getElementById('chart_sales_by_date'));//vẽ biểu đồ cột
	salesChart.draw(data, chartOptions);
}