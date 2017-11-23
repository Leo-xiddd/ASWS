var xmlHttp;
var domain;
var proj;
var Buglist_page_sum;
var Buglist_page_num;
var order;
var sphase;
var Bug_filters;

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
function LoadBugList(){
	var item_ppnum=18;
	$("#overlay").show();
	$("#wait").show();
	var url="ALM/BugList?domain="+domain+"&proj="+proj+"&filter="+Bug_filters+"&page_count="+item_ppnum+"&page_num="+Buglist_page_num;
	TMS_api(encodeURI(url),"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var buglist=resp.Buglist;
				var bgcolor=' style="background-color:#ECF4F4;"';
				$("#buglist_tbody tr").remove();
				for(var i=0;i<buglist.length;i++){
					if(bgcolor=="")bgcolor='background-color:#ECF4F4;';
					else bgcolor="";
					var line='<tr>';
					line=line+'<td align="center" style="'+bgcolor+'">'+buglist[i].id+'</td>';
					line=line+'<td style="'+bgcolor+'"><input type="text" value="'+buglist[i].summary+'"></td>';
					line=line+'<td style="'+bgcolor+'">'+buglist[i].assignto+'</td>';
					line=line+'<td style="'+bgcolor+'">'+buglist[i].state+'</td>';
					line=line+'<td style="'+bgcolor+'">'+buglist[i].severity+'</td>';
					line=line+'<td style="'+bgcolor+'">'+buglist[i].version+'</td>';
					line=line+'<td style="'+bgcolor+'">'+buglist[i].tester+'</td>';
					line=line+'<td style="'+bgcolor+'">'+buglist[i].defect_time+'</td>';
					line=line+'<td style="border-right:1px solid;'+bgcolor+'">'+buglist[i].module+'</td>';
					line=line+'</tr>';
					
					$("#buglist_tbody").append(line);
				}
				var Bug_sum=parseInt(resp.Bug_num);
				Buglist_page_sum=Math.ceil(Bug_sum/item_ppnum);
				$("#page_num").text(Buglist_page_sum);
				$("#curr_page").text(Buglist_page_num);				
			}
			else alert(resp.message);
			$("#overlay").hide();
			$("#wait").hide();
		}
	});
}
function save_DefectInfo(url){
	var defect={};
	defect.usrname=$("#usr_account").val();
	defect.fullname=$("#usr_fullname").val();
	defect.type=$("#usr_type").val();
	if(defect.usrname==""){
		alert("用户账号不能为空");
		$("#usr_account").focus();
	}
	else if(defect.fullname==""){
		alert("用户姓名不能为空");
		$("#usr_fullname").focus();
	}
	else if(defect.type==""){
		alert("用户类型不能为空");
		$("#usr_type").focus();
	}
	else{
		defect.passwd=$("#usr_pwd").val();
		defect.dept1=$("#usr_dept1").val();
		defect.dept2=$("#usr_dept2").val();
		defect.dept3="";
		defect.role=$("#usr_role").val();
		defect.email=$("#usr_mail").val();
		defect.mobile=$("#usr_mobile").val();

		var body=JSON.stringify(defect);
		TMS_api(url,"POST",body,function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					if(url.indexOf("Update")>-1){
						$("#usr_pwd").attr("readonly",false);
						$("#usr_pwd").css("background-color","#FFFFFF");
						$("#usr_account").attr("readonly",false);
						$("#usr_account").css("background-color","#FFFFFF");
					}
					$("#DefectInfo_table input").val("");
					$("#overlay").hide();
					$("#DefectInfo").hide();
					LoadBugList();
				}
				else alert(resp.message);
			}
		});
	}		
}
function Topage(num){
	if(Buglist_page_num!=num){
		if(num==0)Buglist_page_num=Buglist_page_sum;
		else Buglist_page_num=num;
		LoadBugList();	
	}
}
function Nextpage(tag){
	if(tag=="+" && Buglist_page_num!=Buglist_page_sum){
		Buglist_page_num=Buglist_page_num+1;
		LoadBugList();
	}
	else if(tag=="-" && Buglist_page_num!=1){
		Buglist_page_num=Buglist_page_num-1;
		LoadBugList();
	}		
}

function relist(phase){
	if(sphase==phase){
		if(order=="desc")order="asc";
		else order="desc";
	}
	else sphase=phase;
	
	var ft="id>0";
	var filter=$("#Bug_filter").val();
	var checkphase=$("#phase").attr("value");
	if(filter!="")ft=checkphase+" like '*"+filter+"*'"
	Bug_filters=ft+" order by "+sphase+" "+order;
	LoadBugList();
}

$(document).ready(function(){ 
	var tr_selected=null;
	var old_bgcolor="";
	var usr_opt="";
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url="login.html";
		window.open(encodeURI(url),'_self');
	}
	//页面初始化
	Buglist_page_sum=0;
	Buglist_page_num=1
	order="desc";
	sphase="";
	Bug_filters="id>0";
	var str=window.location.search;
	domain=decodeURI(getvalue(str,"domain"));
	proj=decodeURI(getvalue(str,"proj"));
	$("#doamin").text(domain);
	$("#proj").text(proj);	
	LoadBugList();
	
	
	//弹层按钮 - 取消
	$("#DefectInfo_cancel").click(function b(){	
		if(usr_opt=="Update"){
			$("#usr_pwd").attr("readonly",false);
			$("#usr_pwd").css("background-color","#FFFFFF");
			$("#usr_account").attr("readonly",false);
			$("#usr_account").css("background-color","#FFFFFF");
		}
		$("#DefectInfo_table input").val("");
		$("#overlay").hide();
		$("#DefectInfo").hide();
	});
	//弹层按钮 - 保存
	$("#DefectInfo_save").click(function b(){	
		var url="User/"+usr_opt;
		save_DefectInfo(url);
	});
	
	//选择或取消用户选择
	$("body").click(function b(e){
		var tr=$(e.target).parent();
		var v_class=tr.parent().attr('id');
		if(v_class!="buglist_tbody"){
			if(tr_selected!=null)tr_selected.children().css("background-color",old_bgcolor);
			tr_selected==null;		
		}
		else {
			if(tr_selected!=null)tr_selected.children().css("background-color",old_bgcolor);
			tr_selected=tr;
			old_bgcolor=tr.children().eq(0).css("background-color");
			tr_selected.children().css("background-color","#E2F6CB");	
		}
	});
	//双击表格 - 打开用户信息页弹层
	$("body").dblclick(function b(e){
		var tr=$(e.target).parent();
		var v_class=tr.parent().attr('id');
		if(v_class=="buglist_tbody"){
			var username=tr.children().eq(1).text();
			var url="ALM/GetBug?domain="+username+"&proj="+projname+"&bugid="+bugid;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						var ty=tr.children().eq(7).text();
						$("#usr_role").val(resp.role);
						$("#usr_account").val(username);
						$("#usr_account").attr("readonly",true);
						$("#usr_account").css("background-color","#EDEDED");
						if(ty=="ldap"){
							$("#usr_pwd").attr("readonly",true);
							$("#usr_pwd").css("background-color","#EDEDED");
						}
						$("#usr_fullname").val(tr.children().eq(2).text());
						$("#usr_dept1").val(tr.children().eq(3).text());
						$("#usr_dept2").val(tr.children().eq(4).text());
						$("#usr_mail").val(tr.children().eq(5).text());
						$("#usr_mobile").val(tr.children().eq(6).text());
						$("#usr_type").val(tr.children().eq(7).text());
						
						$("#overlay").show();
						$("#DefectInfo").css("display","block");
						$("#title_DefectInfo").text("用户信息");
						$("#DefectInfo").css("top","20%");
						$("#DefectInfo").css("left","40%");
						$("#DefectInfo").show();
						usr_opt="Update";
					}
					else alert(resp.message);
				}
			});		
		}		
	});
	//查找Bug
	$("#Bug_find").click(function b(){
		var filter=$("#Bug_filter").val();
		var phase=$("#phase").attr("value");
		if(filter!="")Bug_filters=phase+" like '*"+filter+"*'";
		Buglist_page_num=1;
		LoadBugList();
	});

	//刷新Bug列表
	$("#Bug_fresh").click(function b(){
		$("#overlay").show();
		$("#wait").show();
		var url="ALM/Fresh?domain="+domain+"&proj="+proj;
		TMS_api(encodeURI(url),"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){	
					Buglist_page_num=1;
					$("#Bug_filter").val("");
					order="desc";
					sphase="";
					Bug_filters="id>0";
					$("#overlay").hide();
					$("#wait").hide();
					LoadBugList();
				}
				else {
					alert(resp.message);
					$("#overlay").hide();
					$("#wait").hide();
				}
			}
		});		
	});
	//弹层移动
	$('#title_DefectInfo').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#DefectInfo').offset().left; 
		var abs_y = event.pageY - $('#DefectInfo').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#DefectInfo'); 
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