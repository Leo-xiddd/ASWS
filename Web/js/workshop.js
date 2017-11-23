$(document).ready(function(){ 
	var hostpath=getHostUrl('hostpath_page');
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url=hostpath+"login.html";
		window.open(encodeURI(url),'_self');
	}
	
	//鼠标滑过书签的变色效果
	$("div.box_sel").mouseenter(function(e) { 
		$(e.target).css({"border":"3px solid #920707"});
	});
	$("div.box_sel").mouseleave(function (e) { 
		$(e.target).css({"border":"0"});
	});
	
	//选择书签跳转
	$(".bookmark div").click(function b(e){
		var ids=$(e.target).attr("id");
		sessionStorage.currpage=hostpath+ids+".html";
		$("#main",parent.document).attr("src",sessionStorage.currpage);
	});
});