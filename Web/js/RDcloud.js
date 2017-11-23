var xmlHttp;
var vm_selected;
var host_selected;
// 与后台传递API
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
// 加载IP列表
function load_ips(net){
	var url="VMM/ListNetwork?net="+net;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				$("#net option").remove();
				for(var i=0;i<resp.nets.length;i++)$("#net").append('<option>'+resp.nets[i]+'</option>');
				if(net!="")$("#net").val(net);

				$("#tbody_iplist tr").remove();
				var bgcolor="";
				for(var i=0;i<resp.ips.length;i++){
					if(resp.ips[i].state=="已用")bgcolor=' style="color:#B3B3B3;background-color:#E6E6E6;"';
					else bgcolor="";
					var td1="<tr"+bgcolor+"><td>"+resp.ips[i].net+"</td>"
					var td2="<td>"+resp.ips[i].addr+"</td>"
					var td3="<td>"+resp.ips[i].state+"</td>"
					var td4="<td>"+resp.ips[i].des+"</td></tr>"
					$("#tbody_iplist").append(td1+td2+td3+td4);
				}
			}			
			else alert(resp.message);
		}
	});
}
// 加载虚拟机列表
function load_VMs(){
	//清除当前列表
	$("#tb_vmlist tr").remove();
	vm_selected=null;
	var url="VMM/GetServer?hostname="+host_selected;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#hostname").text(resp.hostname);
				$("#ipaddr").text(resp.ipaddr);
				$("#locate").text(resp.locate);
				$("#sn").text(resp.sn);
				$("#model").text(resp.model);
				$("#asset_sn").text(resp.asset_sn);
				$("#vm_num").text(resp.vm_num);
				$("#cpu").text(resp.cpu);
				$("#cpu_used").text(resp.cpu_used);
				$("#cpu_usable").text(resp.cpu_usable);
				$("#cpu_rate_usage").text(resp.cpu_rate_usage);
				$("#mem").text(resp.mem);
				$("#mem_used").text(resp.mem_used);
				$("#mem_usable").text(resp.mem_usable);
				$("#mem_rate_usage").text(resp.mem_rate_usage);
				$("#disk").text(resp.disk);
				$("#disk_used").text(resp.disk_used);
				$("#disk_usable").text(resp.disk_usable);
				$("#disk_rate_usage").text(resp.disk_rate_usage);

				for(var i=0;i<resp.vmlist.length;i++){			
					var tr0='<tr id="'+resp.vmlist[i].name+'">';				
					var td1='<td>'+resp.vmlist[i].id+'</td>';
					var td2='<td style="text-align: left;padding-left: 5px;">'+resp.vmlist[i].name+'</td>';
					var td3='<td>'+resp.vmlist[i].os+'</td>';
					var td4='<td>'+resp.vmlist[i].cpu+'</td>';
					var td5='<td>'+resp.vmlist[i].mem+'</td>';
					var td6='<td>'+resp.vmlist[i].disk+'</td>';
					var td7='<td>'+resp.vmlist[i].ip +'</td>';
					var td8='<td style="text-align: left;padding-left: 5px;">'+resp.vmlist[i].des+'</td>';
					var td9='<td>'+resp.vmlist[i].user+'</td>';
					var td10='<td>'+resp.vmlist[i].type+'</td>';				
					var tr1="</tr>";
					var record=tr0+td1+td2+td3+td4+td5+td6+td7+td8+td9+td10+tr1;
					$("#tb_vmlist").append(record);					
				}
				var tree_height=220+resp.vmlist.length*25;
				$("#treebox").css("height",""+tree_height+"px");		
			}
			else alert(resp.message);
		}		
	});
}
// 加载主机列表，刷新主机资源状态图
function load_Servers() {
	var url="VMM/ListServ";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#treelist li").remove();
				$("#vm_host option").remove();
				var tao1='';
				var tag=0;
				var mid='';
				// 初始化主机资源图配置
				var seriers=[
					{
						name:'磁盘',
						radius:['10%','35%'],
						data:[
							{
								value:20,
								name:'已使用',
								itemStyle: {normal: {color: '#3F84AB'}}
							},
							{
								value:80,
								name:'可用',
								itemStyle: {normal: {color: '#D6E6F0'}}
							}
						]
					},
					{
						name:'内存',
						radius:['55%','80%'],
						data:[
							{
								value:20,
								name:'已使用',
								itemStyle: {normal: {color: '#F2B721'}}
							},
							{
								value:80,
								name:'可用',
								itemStyle: {normal: {color: '#FCEDCA'}}
							}
						]
					}
				];
				var legend={};

				for(var i=0;i<resp.domain.length;i++){
					tao1=tao1+'<li><span class="folder">'+resp.domain[i].name+'</span>';
					var hosts=resp.domain[i].hosts;
					var tao2='';
					for(var j=0;j<hosts.length;j++){
						if(j==0 && host_selected=="")host_selected=hosts[j].hostname;
						tao2=tao2+'<li><span class="file" id="'+hosts[j].hostname+'">'+hosts[j].hostname+'</span></li>';
						seriers[0].data[0].value=hosts[j].disk_used;
						seriers[0].data[1].value=hosts[j].disk_usable;
						seriers[1].data[0].value=hosts[j].mem_used;
						seriers[1].data[1].value=hosts[j].mem_usable;
						$("#title_chart_vm"+(j+1)).text(hosts[j].hostname);
						ToPiechart('Chart_usage_vm'+(j+1),'',seriers,legend);

						$("#vm_host").append('<option>'+hosts[j].hostname+'</option>');
					}
					if(tao2!="")tao2='<ul>'+tao2+'</ul>';
					tao1=tao1+tao2+'</li>';
				}
				$("#treelist").append(tao1);
				$("#treelist").treeview();	
				if(host_selected!=""){
					$("#"+host_selected).css("background-color","#E2F6CB");
					load_VMs();	
				}						
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
// 打开IP池管理弹层
function ipman(){
	open_form('#form_ip','.overlay');
	load_ips("");
}
// 弹层按钮-添加新网络
function AddNetwork(){
	var newnet=prompt("请输入新网络的网关地址：","xxx.xxx.xxx.xxx");
	if(newnet!=null){
		if(newnet=="")alert("必须输入网关地址！");
		else {
			var net=newnet.substring(0,newnet.lastIndexOf("."))+".0";
			var url="VMM/AddNetwork?gateway="+newnet;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200)load_ips(net);					
					else alert(resp.message);
				}
			});
		}
	}
}
// 添加新服务器
function addServ(){
	$("#save_butt_serv").attr('data-value','AddServ');
	open_form('#Servinfo','.overlay');
}
// 打开编辑服务器弹层
function editServ(){
	if(host_selected=="")alert("请先选择要操作的服务器。");
	else {
		$("#popform_hostname").val($("#hostname").text());
		$("#popform_model").val($("#model").text());
		$("#popform_ipaddr").val($("#ipaddr").text());
		$("#popform_locate").val($("#locate").text());
		$("#popform_sn").val($("#sn").text());
		$("#popform_asset_sn").val($("#asset_sn").text());
		$("#popform_cpu").val($("#cpu").text());
		$("#popform_mem").val($("#mem").text());
		$("#popform_disk").val($("#disk").text());

		$("#save_butt_serv").attr('data-value','UpdateServ');
		open_form('#Servinfo','.overlay');
	}	
}
// 删除服务器
function delServ(){
	if(host_selected=="")alert("请先选择要操作的服务器。");
	else {
		if(confirm("请确认是否要删除服务器"+host_selected)){
			var url="VMM/DelServ?hostname="+host_selected;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						alert("服务器已删除！");
						host_selected="";
						load_Servers();					
					}
					else alert(resp.message);
				}
			});
		}
	}
}
// 保存服务器
function Serv_save(){
	var host={};	
	host.hostname=$("#popform_hostname").val();	
	host.ipaddr=$("#popform_ipaddr").val();	
	host.cpu=$("#popform_cpu").val();
	host.mem=$("#popform_mem").val();
	host.disk=$("#popform_disk").val();	
	if(host.hostname==""){
		alert("服务器名不能为空！");
		$("#popform_hostname").focus();
	}
	else if(host.ipaddr==""){
		alert("IP地址不能为空！");
		$("#popform_ipaddr").focus();
	}
	else if(host.cpu==""){
		alert("cpu数不能为空！");
		$("#popform_cpu").focus();
	}
	else if(host.mem==""){
		alert("内存不能为空！");
		$("#popform_mem").focus();
	}
	else if(host.disk==""){
		alert("磁盘不能为空！");
		$("#popform_disk").focus();
	}
	else{
		host.model=$("#popform_model").val();
		host.locate=$("#popform_locate").val();
		host.sn=$("#popform_sn").val();
		host.asset_sn=$("#popform_asset_sn").val();
		
		var body = JSON.stringify(host);
		var url="VMM/"+$("#save_butt_serv").attr('data-value')+"?hostname="+host_selected;						
		TMS_api(url,"POST",body,function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("操作成功");	
					CloseForm("#Servinfo","#overlay");				
					load_Servers();
				}
				else alert(resp.message)
			}
		});
	}
}
// 添加新虚拟机
function addVM(){
	$("#save_butt").attr('data-value','AddVM');
	if(host_selected!="")$("#vm_host").val(host_selected);
	open_form('#VMinfo','.overlay');
}
// 打开编辑虚拟机弹层
function editVM(){
	if(vm_selected==null)alert("请先选择要操作的虚拟机。");
	else {
		var vm_name=vm_selected.children().eq(1).text();
		var vm_os=vm_selected.children().eq(2).text();
		var vm_cpu=vm_selected.children().eq(3).text();
		var vm_mem=vm_selected.children().eq(4).text();
		var vm_disk=vm_selected.children().eq(5).text();
		var vm_ip=vm_selected.children().eq(6).text();
		var vm_des=vm_selected.children().eq(7).text();
		var vm_user=vm_selected.children().eq(8).text();
		var vm_type=vm_selected.children().eq(9).text();

		$("#vm_type").val(vm_type);
		$("#vm_name").val(vm_name);
		$("#vm_os").val(vm_os);
		$("#vm_ip").val(vm_ip);
		$("#vm_cpu").val(vm_cpu);
		$("#vm_mem").val(vm_mem);
		$("#vm_disk").val(vm_disk);
		$("#vm_user").val(vm_user);
		$("#vm_des").val(vm_des);
		$("#vm_host").val(host_selected);

		$("#save_butt").attr('data-value','UpdateVM');
		open_form('#VMinfo','.overlay');
	}	
}
// 删除虚拟机
function delVM(){
	if(vm_selected==null)alert("请先选择要操作的虚拟机。");
	else {
		var vm_hostname=vm_selected.children().eq(1).text();
		if(confirm("请确认是否要删除虚拟机"+vm_hostname)){
			var url="VMM/DelVM?vm_name="+vm_hostname;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						alert("虚拟机已删除！");
						vm_selected=null;
						load_VMs();					
					}
					else alert(resp.message);
				}
			});
		}
	}
}
// 保存虚拟机
function VM_save(){
	var vm={};	
	vm.name=$("#vm_name").val();	
	vm.ip=$("#vm_ip").val();
	vm.cpu=$("#vm_cpu").val();
	vm.mem=$("#vm_mem").val();
	vm.disk=$("#vm_disk").val();
	vm.user=$("#vm_user").val();	
	if(vm.name==""){
		alert("虚拟机名不能为空！");
		$("#vm_name").focus();
	}
	else if(vm.ip==""){
		alert("IP地址不能为空！");
		$("#vm_ip").focus();
	}
	else if(vm.cpu==""){
		alert("cpu数不能为空！");
		$("#vm_cpu").focus();
	}
	else if(vm.mem==""){
		alert("内存不能为空！");
		$("#vm_mem").focus();
	}
	else if(vm.disk==""){
		alert("磁盘不能为空！");
		$("#vm_disk").focus();
	}
	else if(vm.user==""){
		alert("使用人不能为空！");
		$("#vm_user").focus();
	}
	else{
		vm.hostname=$("#vm_host").val();
		vm.type=$("#vm_type").val();
		vm.os=$("#vm_os").val();
		vm.des=$("#vm_des").val();
		var body = JSON.stringify(vm);
		var vm_name="";
		if($("#save_butt").attr('data-value')=="UpdateVM")vm_name=vm_selected.children().eq(1).text()
		var url="VMM/"+$("#save_butt").attr('data-value')+"?vm_name="+vm_name;						
		TMS_api(url,"POST",body,function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("操作成功");	
					CloseForm("#VMinfo","#overlay");				
					load_Servers();
				}
				else alert(resp.message)
			}
		});
	}
}
// 快速查找虚机
function findVM(){
	var value=$("#filt_value").val();
	if(value!=""){
		var key=$("#filt_key option:selected").attr("value");
		var url="VMM/FindVM?key="+key+"&value="+value;
		TMS_api(url,"GET","",function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					$("#tbody_findvmlist tr").remove();
					for(var i=0;i<resp.vms.length;i++){
						var td0="<tr><td>"+resp.vms[i].hostname+"</td>"
						var td1="<td>"+resp.vms[i].name+"</td>"
						var td2="<td>"+resp.vms[i].ip+"</td>"
						var td3="<td>"+resp.vms[i].os+"</td>"
						var td4="<td>"+resp.vms[i].cpu+"</td>"
						var td5="<td>"+resp.vms[i].mem+"</td>"
						var td6="<td>"+resp.vms[i].disk+"</td>"
						var td7="<td>"+resp.vms[i].des+"</td>"
						var td8="<td>"+resp.vms[i].user+"</td>"
						var td9="<td>"+resp.vms[i].type+"</td></tr>"
						$("#tbody_findvmlist").append(td0+td1+td2+td3+td4+td5+td6+td7+td8+td9);
					}
					$("#findvm_nuum").text("共检索到"+resp.vms.length+"个结果。");
					open_form("#form_findvm",".overlay");
				}
				else alert(resp.message)
			}
		});		
	}
}
/*******************主函数****************/
$(document).ready(function(){ 
	host_selected="";
	vm_selected=null;
	if(typeof(sessionStorage.customerId)=='undefined'){;
		sessionStorage.currpage="login.html";
		$("#main", parent.document).attr("src",sessionStorage.currpage);
	}
	//初始化页面
	load_Servers();

	//鼠标滑过按钮的变色效果
	$("button[class*='head_butt']").mouseenter(function(e) { 
		$(e.target).css("background-color","#0DBFB3");	
	});
	$("button[class*='head_butt']").mouseleave(function (e) { 
		$(e.target).css("background-color", "#2C5C77");
	});


	//弹层拖动
	$('#VMinfo_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#VMinfo').offset().left; 
		var abs_y = event.pageY - $('#VMinfo').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#VMinfo'); 
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
	$('#Servinfo_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#Servinfo').offset().left; 
		var abs_y = event.pageY - $('#Servinfo').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#Servinfo'); 
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
	$('#form_ip_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_ip').offset().left; 
		var abs_y = event.pageY - $('#form_ip').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_ip'); 
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
	$('#form_findvm_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_findvm').offset().left; 
		var abs_y = event.pageY - $('#form_findvm').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_findvm'); 
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
	// IP管理模块，切换网络
	$("#net").change(function (){
		load_ips($("#net").val());
	});
	//树型视图下选择服务器
	$("#treelist").click(function (e){
		if($(e.target).attr('class')=="file"){
			var new_hostname=$(e.target).text();
			if(host_selected!=new_hostname){
				$("#"+host_selected).css("background-color","#FFFFFF");
				$(e.target).css("background-color","#E2F6CB");
				host_selected=new_hostname;
			}
			load_VMs();	
		}
	});
		
	// 选择虚拟机
	$("#tb_vmlist").click(function (e){
		var vm_name=$(e.target).parent().attr('id');
		if(vm_selected==null){
			//有项目被选中
			vm_selected=$(e.target).parent();
			vm_selected.css("background-color","#E3F1F7");
		}
		else{
			var vm_name_old=vm_selected.attr("id");		
			//项目被取消
			if(vm_name_old==vm_name){
				vm_selected.css("background-color","#FFFFFF");
				vm_selected=null;
			}
			//选择了其他项目
			else{
				vm_selected.css("background-color","#FFFFFF");
				vm_selected=$(e.target).parent();
				vm_selected.css("background-color","#E3F1F7");
			}
		}
	});
});