$(document).ready(function(){
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url="login.html";
		window.open(encodeURI(url),'_self');
	}
	$(".blank_butt").click(function(e){
		var date = new Date();
		$("#pqr_year", parent.document).val(date.getFullYear());
		var month = date.getMonth() + 1;
		$("#pqr_month",parent.document).val(month+"月");
		$("#newfile_panel", parent.document).css("display","block");
		$("#newfile_panel", parent.document).show();
	});
});
