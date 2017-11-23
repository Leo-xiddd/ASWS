var xmlHttp;
var usr_selected;
function TMS_api(url,med,dats,cfunc){
	var hostpath=getHostUrl("hostpath_api");
	try{
		url=encodeURI(hostpath+url);
		xmlHttp = new XMLHttpRequest();
		xmlHttp.onreadystatechange=cfunc;		
		xmlHttp.open(med,url,true);
		if(med=="GET")xmlHttp.send();
		else xmlHttp.send(dats);	
	}catch(e){
		alert(e);
	}	
}
// 获取指定用户的权限表
function loadpurview(){
	if(usr_selected!=""){
		var url="User/LoadPurview?user="+usr_selected;
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					var purview=resp.purview;
					$("#tbody_purviewlist tr").remove();
					for(var i=0;i<purview.length;i++){
						var line='<tr><td colspan="7"><div class="module_title">'+purview[i].module+'</div></td></tr>';
						var pv_list=purview[i].list;
						var ll="";
						for(var j=0;j<pv_list.length;j++){
							ll=ll+'<td><input type="checkbox" id="'+pv_list[j].key+'">'+pv_list[j].text+'</td>';
							if((j+1)%7==0){
								line=line+"<tr>"+ll+"</tr>";
								ll="";
							}
						}
						if(ll!="")line=line+"<tr>"+ll+"</tr>";
						$("#tbody_purviewlist").append(line);
						for(var j=0;j<pv_list.length;j++){
							if(pv_list[j].value=="x")$("#"+pv_list[j].key).attr("checked",false);
							else $("#"+pv_list[j].key).attr("checked",true);						
						}
					}
				}
				else alert(resp.message);
			}
		});	
	}
	else $("#tab_purviewlist input").attr("checked",false);
}
// 获取用户列表
function loadusrs(){
	var url="User/List?filter=&page_count=&page_num=";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var usrlist=resp.userlist;
				usr_selected="";
				$("#tbody_usrlist tr").remove();
				for(var i=0;i<usrlist.length;i++){
					if(i==0)usr_selected=usrlist[i].usrname;
					var line='<tr id="'+usrlist[i].usrname+'" data-value="'+(i+1)+'">';
					line=line+'<td>'+usrlist[i].usrname+'</td>';
					line=line+'<td>'+usrlist[i].fullname+'</td>';
					line=line+'<td>'+usrlist[i].dept2+'</td></tr>';
					
					$("#tbody_usrlist").append(line);
				}
				if(usr_selected!="")$("#"+usr_selected).css("background-color","#E3F1F7");
				loadpurview();				
			}
			else alert(resp.message);
		}
	});
}
// 选择所有配置项
function SelectAll(){
	$("#tab_purviewlist input").attr("checked",true);
}
// 取消所有配置项
function ClearAll(){
	$("#tab_purviewlist input").attr("checked",false);
}

// 初始化权限表
function InitConf(){
	if(confirm("请确认是否要初始化权限表？执行此操作会导致部分用户操作权限出问题！")){
		var url="User/InitPurview";
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("权限表初始化完成，请尽快进行特殊用户权限的配置！");
					$("#tab_purviewlist input").attr("checked",false);
					$("#tbody_usrlist tr").css("background-color","#FFFFFF");
				}
				else alert(resp.message);
			}
		});	
	}		
}
// 保存配置
function SaveConf(){
	if(usr_selected!=""){
		var purview=[];		
		var confs=$("#tab_purviewlist").find("input");
		for(var i=0;i<confs.length;i++){
			var item={
				key:"",
				value:""
			}
			item.key=confs.eq(i).attr("id");
			item.value="x";
			if(confs.eq(i).attr('checked'))item.value="y";
			purview.push(item);
		}

		var url="User/UpdatePurview?user="+usr_selected;
		var body=JSON.stringify(purview);
		TMS_api(url,"POST",body,function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("修改完成！");
					loadpurview();
				}
				else alert(resp.message);
			}
		});	
	}			
}
// 快速定位用户
function findusr(){
	if($("#filt_value").val()!=""){
		if($("#filt_key option:selected").attr("value")=="usrname"){
			var select_id="#"+$("#filt_value").val();
			if($(select_id).length>0){
				$("#tbody_usrlist tr").css("background-color","#FFFFFF");
				$(select_id).css("background-color","#E3F1F7");
				$(select_id).focus();
				$("#usrlist").scrollTop((parseInt($(select_id).attr("data-value"))-8)*26);
				usr_selected=$("#filt_value").val();
				loadpurview();	
			}
		}
		else{
			var url="User/CheckUser?filter="+$("#filt_key option:selected").attr("value")+" like '*"+$("#filt_value").val()+"*'";
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						if(resp.usrname!=""){
							var select_id="#"+resp.usrname;
							usr_selected=resp.usrname;
							$("#tbody_usrlist tr").css("background-color","#FFFFFF");
							$(select_id).css("background-color","#E3F1F7");
							$(select_id).focus();
							$("#usrlist").scrollTop((parseInt($(select_id).attr("data-value"))-8)*26);
							loadpurview();
						}						
					}
					else alert(resp.message);
				}
			});		
		}
	}
}
//***********************  主函数  *****************************************//
$(document).ready(function(){ 
	usr_selected="";
	if(typeof(sessionStorage.customerId)=='undefined'){
		sessionStorage.currpage="login.html";
		$("#main", parent.document).attr("src",sessionStorage.currpage);
	}
	// 获取用户列表
	loadusrs();	

	// 点击用户，切换权限
	$("#tab_usrlist").click(function b(e){
		var v_class=$(e.target).parent().attr('id');
		if(usr_selected!="")$("#"+usr_selected).css("background-color","#FFFFFF");
		usr_selected=v_class;
		$("#"+usr_selected).css("background-color","#E3F1F7");	
		loadpurview();
	});
});