var xmlHttp;
var page_num;
var page_sum;
function TMS_api(url,med,dats,cfunc){
	var hostpath=getHostUrl();
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
	var item_ppnum=18;
	var url="User/List?"+filter+"&page_count="+item_ppnum+"&page_num="+page_num;;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var usrlist=resp.userlist;
				var bgcolor=' style="background-color:#ECF4F4;"';
				$("#tbody_usrlist tr").remove();
				for(var i=0;i<usrlist.length;i++){
					if(bgcolor=="")bgcolor='background-color:#ECF4F4;';
					else bgcolor="";
					var line='<tr>';
					line=line+'<td align="center" style="'+bgcolor+'">'+usrlist[i].id+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].usrname+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].fullname+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].role+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].dept+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].email+'</td>';
					line=line+'<td style="'+bgcolor+'">'+usrlist[i].mobile+'</td>';
					line=line+'<td style="border-right:1px solid #868A8D;'+bgcolor+'">'+usrlist[i].type+'</td>';
					line=line+'</tr>';
					
					$("#tbody_usrlist").append(line);
				}
				var item_sum=parseInt(resp.total_num);
				page_sum=Math.ceil(item_sum/item_ppnum);
				$("#page_num").text(page_sum);
				$("#curr_page").text(page_num);
				if(page_num==1){
					$("#Fir_page").attr("disabled",true);
					$("#Pre_page").attr("disabled",true);
				}
				else{
					$("#Fir_page").attr("disabled",false);
					$("#Pre_page").attr("disabled",false);
				}
				if(page_num==page_sum){
					$("#Next_page").attr("disabled",true);
					$("#Las_page").attr("disabled",true);
				}
				else{
					$("#Next_page").attr("disabled",false);
					$("#Las_page").attr("disabled",false);
				}

				$("#last_sync_time").text(resp.last_sync_time);
			}
			else alert(resp.message);
		}
	});
}
// 打开弹层
function open_form(formID,overlay_class){
	$(formID).css("display","block");
	showOverlay(overlay_class);
	$(formID).show();	
}
// 关闭弹层
function CloseForm(formID,overlayID){
	$(formID).hide();
	$(overlayID).hide();
}
// 保存用户信息
function save_userInfo(url){
	var usrinfo={};
	usrinfo.usrname=$("#usr_account").val();
	usrinfo.fullname=$("#usr_fullname").val();
	usrinfo.passwd=$("#usr_pwd").val();
	
	if(usrinfo.usrname==""){
		alert("用户账号不能为空");
		$("#usr_account").focus();
	}
	else if(usrinfo.fullname==""){
		alert("用户姓名不能为空");
		$("#usr_fullname").focus();
	}
	else if(usrinfo.passwd==""){
		alert("用户密码不能为空");
		$("#usr_passwd").focus();
	}
	else{
		usrinfo.type=$("#usr_type").val();
		usrinfo.dept=$("#usr_dept").val();
		usrinfo.role=$("#usr_role").val();
		usrinfo.email=$("#usr_mail").val();
		usrinfo.mobile=$("#usr_mobile").val();
		var body=JSON.stringify(usrinfo);
		TMS_api(url,"POST",body,function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					if(url.indexOf("Update")>-1)alert("用户更新成功！");
					else alert("用户添加成功！");
					CloseForm('#form_UserInfo','#overlay');
					LoadUserList("filter=");
				}
				else alert(resp.message);
			}
		});
	}		
}
function ldap_save(){
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
		var BaseDN=[];
		var bd_list=$("#list_ldap_BaseDN").children();
		for(var i=0;i<bd_list.length;i++)BaseDN.push(bd_list.eq(i).text());
		ldapconf.BaseDN=BaseDN;
		var body=JSON.stringify(ldapconf);
		var url="User/AddLdap";
		TMS_api(url,"POST",body,function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200)alert("LDAP配置保存成功");
				else alert(resp.message);
			}
		});
	}		
}
// 添加BaseDN
function BaseDN_Save(){
	var BaseDN=$("#BaseDN").val();
	if(BaseDN=="")alert("请输入BaseDN信息！");
	else{
		$("#list_ldap_BaseDN").append("<option>"+BaseDN+"</option>");
		CloseForm('#form_input_BaseDN','#overlay2');
	}	
}
// 与ldap同步用户信息
function ldap_sync(){
	var url="User/syncLdap";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				alert("同步完成，新增用户"+resp.new_member+"名,更新信息"+resp.update_member+"条。");
				LoadUserList("filter=");
			}
			else alert(resp.message);
		}
	});	
}
// 查找用户
function user_filt(){
	var fts="filter=";
	var filter=$("#user_filter").val();
	var phase=$("#user_phase option:selected").attr("value");
	if(filter!="")fts="filter="+phase+" like '*"+filter+"*'";
	LoadUserList(fts);
}

// 翻页
function Topage(num){
	if(page_num!=num){
		if(num==0)page_num=page_sum;
		else page_num=num;
		user_filt();
	}
}
function Nextpage(tag){
	if(tag=="+" && page_num!=page_sum) page_num++;
	else if(tag=="-" && page_num!=1) page_num--;		
	user_filt();	
}
$(document).ready(function(){ 
	var tr_selected=null;
	var old_bgcolor="";
	var usr_opt="";
	page_sum=0;
	page_num=1
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url="login.html";
		window.open(encodeURI(url),'_self');
	}
	//页面初始化
	LoadUserList("filter=");

	// 打开ldap配置弹层
	$("#ldap_conf").click(function b(){
		var url="User/GetLdap";
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){	
					$("#ldap_host").val(resp.host);
					$("#ldap_port").val(resp.port);
					$("#ldap_domain").val(resp.domain);
					$("#ldap_admin").val(resp.admin);
					$("#ldap_pwd").val("");
					$("#list_ldap_BaseDN option").remove();
					var baseDN=resp.BaseDN;
					for(var i=0;i<baseDN.length;i++){
						$("#list_ldap_BaseDN").append("<option>"+baseDN[i]+"</option>");
					}
					open_form("#form_ldap_conf",".overlay");
				}
				else alert(resp.message);
			}
		});	
	});

	// 弹层按钮-打开添加BaseDN的小弹层
	$("#butt_add_ldap_BaseDN").click(function b(){
		$("#BaseDN").val("");
		open_form("#form_input_BaseDN",".overlay2");
		$("#BaseDN").focus();
	});

	// 弹层按钮-删除BaseDN条目
	$("#butt_del_ldap_BaseDN").click(function b(){
		$("#list_ldap_BaseDN option:selected").remove();
	});

	//添加用户
	$("#Usr_add").click(function b(){
		$("#title_UserInfo").text("添加用户");
		$("#usr_pwd").attr("disabled",false);
		$("#usr_account").attr("disabled",false);
		$("#form_UserInfo input").val("");
		open_form("#form_UserInfo",".overlay");
		$("#usr_account").focus();
		usr_opt="Add";
	});
	
	//双击表格 - 打开用户编辑弹层
	$("body").dblclick(function b(e){
		var tr=$(e.target).parent();
		var v_class=tr.parent().attr('id');
		if(v_class=="tbody_usrlist"){
			var username=tr.children().eq(1).text();
			var url="User/Getinfo?user="+username;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						var ty=tr.children().eq(7).text();
						$("#usr_role").val(resp.role);
						$("#usr_account").val(username);						
						$("#usr_fullname").val(tr.children().eq(2).text());
						$("#usr_dept").val(tr.children().eq(3).text());
						$("#usr_mail").val(tr.children().eq(5).text());
						$("#usr_mobile").val(tr.children().eq(6).text());
						$("#usr_type").val(ty);

						if(ty=="ldap")$("#usr_pwd").attr("disabled","disabled");
						$("#usr_account").attr("disabled","disabled");
						$("#title_UserInfo").text("编辑用户");
						open_form("#form_UserInfo",".overlay");
						usr_opt="Update";
					}
					else alert(resp.message);
				}
			});		
		}		
	});
	//弹层按钮 - 保存用户信息
	$("#UserInfo_save").click(function b(){	
		var url="User/"+usr_opt;
		save_userInfo(url);
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
	
	//选择或取消用户选择
	$("body").click(function b(e){
		var tr=$(e.target).parent();
		var v_class=tr.parent().attr('id');
		if(v_class!="tbody_usrlist"){
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

	//弹层移动
	$('#title_LDAPconf').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_ldap_conf').offset().left; 
		var abs_y = event.pageY - $('#form_ldap_conf').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_ldap_conf'); 
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
	$('#title_input_BaseDN').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_input_BaseDN').offset().left; 
		var abs_y = event.pageY - $('#form_input_BaseDN').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_input_BaseDN'); 
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
		var abs_x = event.pageX - $('#form_UserInfo').offset().left; 
		var abs_y = event.pageY - $('#form_UserInfo').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_UserInfo'); 
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