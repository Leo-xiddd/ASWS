var xmlHttp;
function TMS_api(url,med,dats,cfunc){	
	var hostpath=getHostUrl('hostpath_api');		
	try{
		url=hostpath+url;
		xmlHttp = new XMLHttpRequest();
		xmlHttp.onreadystatechange=cfunc;		
		xmlHttp.open(med,url,true);
		if(med=="GET")xmlHttp.send();
		else xmlHttp.send(dats);	
	}catch(e){
		alert(e);
	}	
}
function Topage(num){
	if(num==0)Tplist_page_num=Tplist_page_sum;
	else Tplist_page_num=num;
	reload_tplist(tplist_his);	
}
function Nextpage(tag){
	if(tag=="+"){
		Tplist_page_num=Tplist_page_num+1;
		if(Tplist_page_num>Tplist_page_sum)Tplist_page_num=Tplist_page_sum;
	}
	else if(tag=="-"){
		Tplist_page_num=Tplist_page_num-1;
		if(Tplist_page_num==0)Tplist_page_num=1;
	}
	reload_tplist(tplist_his);	
}
$(document).ready(function(){ 
	var hostpath_page=getHostUrl('hostpath_page');
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url=hostpath_page+"login.html";
		window.open(encodeURI(url),'_self');
	}
	Tplist_page_num=1;
	Tplist_page_sum=0;
	tpitem_ppnum=5;
	//初始化页面表单,
	
	//鼠标滑过按钮的变色效果
	$("button[class*='head_butt left rr']").mouseenter(function(e) { 
		$(e.target).css({"color":"#FFFFFF","background-color": "#0DBFB3"});	
	});
	$("button[class*='head_butt left rr']").mouseleave(function (e) { 
		$(e.target).css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
	});
	
	// 弹层移动效果
	$('.pl_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#PL').offset().left; 
		var abs_y = event.pageY - $('#PL').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#PL'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>1080)rel_left=1080;
				var rel_top=event.pageY - abs_y;
				if(rel_top<0)rel_top=0;
				if(rel_top>740)rel_top=740;
				obj.css({'left':rel_left, 'top':rel_top}); 
			} 
		}).mouseup( function () { 
			isMove = false; 
		}); 
	});
});
// 页面点击事件
$(document).click(function (e) { 
	var v_class=""+$(e.target).parent().attr('class');
	//当项目被点击
	if(v_class=="tdats"){
		Testproj=$(e.target).parent();
		if(tag_proj_select==null){
			//有项目被选中
			tag_proj_select=Testproj;
			Testproj.css("background-color","#E2F6CB");
			//刷新DQS和DCL曲线,按钮默认切换回DCL
			projn=Testproj.children().eq(1).children().eq(0).text();
			projv=Testproj.children().eq(2).text();
			$("#proj_lable").text(projn+"_"+projv);
			load_subtask();
		}
		else{
			var child_selec_proj=tag_proj_select.children().eq(0).text();
			var	child_new_proj=Testproj.children().eq(0).text();
			//项目被取消
			if(child_selec_proj==child_new_proj){
				tag_proj_select=null;
				Testproj.css("background-color","#FFFFFF");
			}
			//选择了其他项目
			else{
				tag_proj_select.css("background-color","#FFFFFF");
				tag_proj_select=Testproj;
				Testproj.css("background-color","#E2F6CB");
				//刷新DQS和DCL曲线
				projn=Testproj.children().eq(1).children().eq(0).text();
				projv=Testproj.children().eq(2).text();
				$("#proj_lable").text(projn+"_"+projv);
				load_subtask();
			}
		}
	}
});