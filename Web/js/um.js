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
function LoadUserList(filter){
	var url="User/List?"+filter;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var usrlist=resp.userlist;
				var bgcolor=' style="background-color:#ECF4F4;"';
				$("#usrlist_tbody tr").remove();
				for(var i=0;i<usrlist.length;i++){
					if(bgcolor=="")bgcolor='background-color:#ECF4F4;';
					else bgcolor="";
					var line='<tr>';
					line=line+'<td align="center" style="'+bgcolor+'">'+usrlist[i].id+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].usrname+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].fullname+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].dept1+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].dept2+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].email+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].mobile+'</td>';
					line=line+'<td style="border-right:1px solid;'+bgcolor+'">'+usrlist[i].type+'</td>';
					line=line+'</tr>';
					
					$("#usrlist_tbody").append(line);
				}
			}
			else alert(resp.message);
		}
	});
}
// 保存用户信息
function save_userInfo(url){
	var usrinfo={};
	usrinfo.usrname=$("#usr_account").val();
	usrinfo.fullname=$("#usr_fullname").val();
	usrinfo.type=$("#usr_type").val();
	if(usrinfo.usrname==""){
		alert("用户账号不能为空");
		$("#usr_account").focus();
	}
	else if(usrinfo.fullname==""){
		alert("用户姓名不能为空");
		$("#usr_fullname").focus();
	}
	else if(usrinfo.type==""){
		alert("用户类型不能为空");
		$("#usr_type").focus();
	}
	else{
		usrinfo.passwd=$("#usr_pwd").val();
		usrinfo.dept1=$("#usr_dept1").val();
		usrinfo.dept2=$("#usr_dept2").val();
		usrinfo.dept3="";
		usrinfo.role=$("#usr_role").val();
		usrinfo.email=$("#usr_mail").val();
		usrinfo.mobile=$("#usr_mobile").val();
		var body=JSON.stringify(usrinfo);
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
					$("#userinfo_table input").val("");
					$("#overlay").hide();
					$("#UserInfo").hide();
					LoadUserList("filter=");
				}
				else alert(resp.message);
			}
		});
	}		
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
	LoadUserList("filter=");
	
	//添加本地用户-打开弹层
	$("#Usr_add").click(function b(){
		$("#overlay").show();
		$("#UserInfo").css("display","block");
		$("#title_UserInfo").text("添加本地用户");
		$("#UserInfo").css("top","20%");
		$("#UserInfo").css("left","40%");
		$("#usr_type").val("local");
		$("#UserInfo").show();
		usr_opt="Add";
	});
	//弹层按钮 - 取消
	$("#UserInfo_cancel").click(function b(){	
		if(usr_opt=="Update"){
			$("#usr_pwd").attr("readonly",false);
			$("#usr_pwd").css("background-color","#FFFFFF");
			$("#usr_account").attr("readonly",false);
			$("#usr_account").css("background-color","#FFFFFF");
		}
		$("#userinfo_table input").val("");
		$("#overlay").hide();
		$("#UserInfo").hide();
	});
	//弹层按钮 - 保存
	$("#UserInfo_save").click(function b(){	
		var url="User/"+usr_opt;
		save_userInfo(url);
	});
	
	//从LDAP导入用户-打开弹层
	$("#User_import").click(function b(){
		$("#overlay").show();
		$("#LDAPimport").css("display","block");
		$("#LDAPimport").css("top","20%");
		$("#LDAPimport").css("left","20%");
		$("#LDAPimport").show();
	});
	//弹层按钮 - 取消
	$("#LDAPimport_cancel").click(function b(){	
		$("#ldap_BD").val("");
		$("#overlay").hide();
		$("#LDAPimport").hide();
	});
	//弹层按钮 - 导入
	$("#LDAPimport_import").click(function b(){	
		var BD=$("#ldap_BD").val();
		if(BD==""){
			alert("请输入BaseDN信息");
			$("#ldap_BD").focus();
		}
		else{
			var url="User/ImportLdap?BaseDN="+BD;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						$("#ldap_BD").val("");
						$("#overlay").hide();
						$("#LDAPimport").hide();
						LoadUserList("filter=");
					}
					else alert(resp.message);
				}
			});
		}		
	});
	//弹层按钮 - 修改配置
	$("#LDAPimport_modify").click(function b(){	
		var url="User/GetLdap";
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){	
					$("#LDAPimport").hide();
					$("#LDAPconf").css("display","block");
					$("#LDAPconf").css("top","20%");
					$("#LDAPconf").css("left","40%");
					$("#ldap_host").val(resp.host);
					$("#ldap_port").val(resp.port);
					$("#ldap_domain").val(resp.domain);
					$("#ldap_admin").val(resp.admin);
					$("#LDAPconf").show();
				}
				else alert(resp.message);
			}
		});
	});	
	//弹层按钮 - 取消修改配置
	$("#LDAPconf_cancel").click(function b(){	
		$("#LDAPconf input").val("");
		$("#LDAPconf").hide();
		$("#LDAPimport").css("top","20%");
		$("#LDAPimport").css("left","20%");
		$("#LDAPimport").show();
	});
	//弹层按钮 - 保存配置
	$("#LDAPconf_save").click(function b(){	
		var ldapconf={};
		ldapconf.host=$("#ldap_host").val();
		ldapconf.port=$("#ldap_port").val();
		ldapconf.domain=$("#ldap_domain").val();
		ldapconf.admin=$("#ldap_admin").val();
		ldapconf.pwd=$("#ldap_pwd").val();
		if(ldapconf.host==""){
			alert("请输入LDAP服务器的IP地址");
			$("#ldap_host").focus();
		}
		else if(ldapconf.port==""){
			alert("请输入LDAP服务器端口");
			$("#ldap_port").focus();
		}
		else if(ldapconf.domain==""){
			alert("请输入基础域信息");
			$("#ldap_domain").focus();
		}
		else if(ldapconf.admin==""){
			alert("请输入管理员账号");
			$("#ldap_admin").focus();
		}
		else{
			var body=JSON.stringify(ldapconf);
			var url="User/AddLdap";
			TMS_api(url,"POST",body,function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						$("#LDAPconf input").val("");
						$("#LDAPconf").hide();
						$("#LDAPimport").css("top","20%");
						$("#LDAPimport").css("left","20%");
						$("#LDAPimport").show();
					}
					else alert(resp.message);
				}
			});
		}		
	});

	//选择或取消用户选择
	$("body").click(function b(e){
		var tr=$(e.target).parent();
		var v_class=tr.parent().attr('id');
		if(v_class!="usrlist_tbody"){
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
		if(v_class=="usrlist_tbody"){
			var username=tr.children().eq(1).text();
			var url="User/Getinfo?user="+username;
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
						$("#UserInfo").css("display","block");
						$("#title_UserInfo").text("用户信息");
						$("#UserInfo").css("top","20%");
						$("#UserInfo").css("left","40%");
						$("#UserInfo").show();
						usr_opt="Update";
					}
					else alert(resp.message);
				}
			});		
		}		
	});
	
	//删除用户
	$("#Usr_del").click(function b(){
		if(tr_selected==null)alert("请先选择要删除的用户");
		else {
			var username=tr_selected.children().eq(1).text();
			var url="User/Delete?user="+username;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						LoadUserList("filter=");
					}
					else alert(resp.message);
				}
			});
		}
	});
	
	//查找用户
	$("#Usr_find").click(function b(){
		var fts="filter=";
		var filter=$("#user_filter").val();
		var phase=$("#user_phase").attr("value");
		if(filter!="")fts="filter="+phase+" like '*"+filter+"*'";
		LoadUserList(fts);
	});
	
	//弹层移动
	$('#title_LDAPconf').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#LDAPconf').offset().left; 
		var abs_y = event.pageY - $('#LDAPconf').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#LDAPconf'); 
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
	$('#title_LDAPimport').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#LDAPimport').offset().left; 
		var abs_y = event.pageY - $('#LDAPimport').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#LDAPimport'); 
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
	$('#title_UserInfo').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#UserInfo').offset().left; 
		var abs_y = event.pageY - $('#UserInfo').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#UserInfo'); 
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