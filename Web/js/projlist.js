var Proj;
var tag_proj_select;
var xmlHttp;
var page_num;
var page_sum;
var tpitem_ppnum;
var proj_id;
var projlist_filt;

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
//打开弹层
function open_form(formID){
	$(formID).css("display","block");
	showOverlay('.overlay');
	$(formID).show();
}
// 关闭弹层
function CloseForm(formID){
	$(formID).hide();
	$("#overlay").hide();
}
function AddNewProj(){
	$(".new_blank").val("");
	//加载产品线和产品列表
	var url="ProjectManage/Listpl";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				$("#new_product_line").empty();
				$("#new_product").empty();
				for(var i=0;i<resp.productlines.length;i++)$("#new_product_line").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
				if(resp.productlines.length>0)$("#new_product_line").val(resp.productlines[0]);

				url="ProjectManage/GetProduts?product_line="+resp.productlines[0];
				TMS_api(url,"GET","",function a(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){	
							for(var i=0;i<resp.products.length;i++){
								$("#new_product").append("<option value='"+resp.products[i]+"'>"+resp.products[i]+"</option>");
							}
							if(resp.products.length>0)$("#new_product").val(resp.products[0]);
						}
						else alert(resp.message);
					}		
				});
			}
			else alert(resp.message);
		}
	});	
	open_form("#newProj");
}
function ChangeProductList(prodline,Element_ID,newprodname){
	var url="ProjectManage/GetProduts?product_line="+prodline;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				$(Element_ID).empty();
				for(var i=0;i<resp.products.length;i++){
					$(Element_ID).append("<option value='"+resp.products[i]+"'>"+resp.products[i]+"</option>");
				}
				if(resp.products.length>0){
					if(newprodname=="")$(Element_ID).val(resp.products[0]);
					else $(Element_ID).val(newprodname);
				}
			}
			else alert(resp.message);
		}		
	});
}
function EditProj(type,pid){
	$("#proj_id").text(pid);
	//加载产品线和产品列表
	var url="ProjectManage/Listpl";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#product_line").empty();
				$("#product").empty();
				for(var i=0;i<resp.productlines.length;i++){
					$("#product_line").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
				}
							
				url="ProjectManage/GetProj?proj_id="+pid;
				TMS_api(url,"GET","",function a(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){	
							//更新表单项目
							$("#proj_name").val(resp.proj_name);
							$("#customer").val(resp.customer);
							$("#priority").val(resp.priority);
							$("#product_line").val(resp.product_line);
							$("#pm").val(resp.pm);
							$("#responsor").val(resp.responsor);
							
							$("#plan_end_time").val(resp.plan_end_time);
							var multitxt=resp.goals;
							multitxt=multitxt.replace(/<br>/g,"\n");
							$("#goals").val(multitxt);
							multitxt=resp.comments;
							multitxt=multitxt.replace(/<br>/g,"\n");
							$("#comments").val(multitxt);										
							$("#team_member").val(resp.team_member);
							$("#git").val(resp.git);
							$("#approver").val(resp.approver);
							$("#start_time").val(resp.start_time);
							$("#proj_status").val(resp.proj_status);
							//变更产品列表
							var product_temp=resp.product;
							ChangeProductList(resp.product_line,"#product",product_temp);

							if(type=="edit"){
								$(".disableView").attr("disabled",false);
								$("#btnSave").show();
							}
							else if(type=="view"){	
								$(".disableView").attr("disabled",true);
								$("#btnSave").hide();
							}										
						}
						else alert(resp.message);
					}		
				});					
			}
			else alert(resp.message);
		}
	});	
	open_form("#ProjView");
}

//用于加载项目表_表格视图
function load_projs(){
	//清除当前所有项目
	$("#tab_projlist").remove();
	tag_proj_select=null;
	proj_id=null;
	Proj=null;
	var filt=projlist_filt+"proj_status<>'待审批' and proj_status<>'审批驳回'";
	var para="filter="+filt+"&page_count="+tpitem_ppnum+"&page_num="+page_num;
	var url="ProjectManage/ListProj?"+para;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#tb_projlist").append('<tbody id="tab_projlist"></tbody>');	
				var FST="";
				for(var i=0;i<resp.proj_list.length;i++){
					//添加项目记录				
					var tr0='<tr class="tdats">';				
					var td1='<td class="cell"><a href="javascript:void(0)" onclick="EditProj(\'view\',\''+resp.proj_list[i].proj_id+'\')">'+resp.proj_list[i].proj_id+'</a></td>';
					var td2='<td class="cell">'+resp.proj_list[i].proj_name+'</td>';
					var td3='<td class="cell">'+resp.proj_list[i].product_line+'</td>';
					var td4='<td class="cell">'+resp.proj_list[i].product+'</td>';
					var td5='<td class="cell">'+resp.proj_list[i].customer+'</td>';
					var td6='<td class="cell">'+resp.proj_list[i].proj_status+'</td>';
					var td7='<td class="cell">'+resp.proj_list[i].approver +'</td>';
					var td8='<td class="cell">'+resp.proj_list[i].priority+'</td>';
					var td9='<td class="cell">'+resp.proj_list[i].start_time+'</td>';
					var td10='<td class="cell">'+resp.proj_list[i].plan_end_time+'</td>';
					var td11='<td class="cell">'+resp.proj_list[i].git+'</td>';
					var td12='<td class="cell">'+resp.proj_list[i].responsor+'</td>';
					var td13='<td class="cell">'+resp.proj_list[i].pm+'</td>';					
					var tr1="</tr>";
					var record=tr0+td1+td2+td3+td4+td5+td6+td7+td8+td9+td10+td11+td12+td13+tr1;
					$("#tab_projlist").append(record);					
				}
				var TP_sum=parseInt(resp.total_num);
				page_sum=Math.ceil(TP_sum/tpitem_ppnum);
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
			}
			else alert(resp.message);
		}		
	});
}
//查询项目
function filte_proj(){
	var filter=$("#filter").val();
	var phase=$("#phase option:selected").attr("value");
	if(filter!="")projlist_filt=phase+"='"+filter+"' and ";
	else projlist_filt="";
	page_num=1;
	load_projs();	
}
//下载文件
function downloadfile(){
	var filter="";
	var filt_value=$("#filter").val();
	var phase=$("#phase option:selected").attr("value");
	if(filt_value!="")filter=phase+" like '"+filt_value+"*'";
	var url="ProjectManage/DownloadProjList?filter="+filter;						
	TMS_api(url,"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				downloadFile('http://tms.tech.bitauto.com:8080/ASWS/datareport/down?filename='+resp.DownloadFile_url);
			}
			else alert(resp.message);
		}
	});	
}
//为树型视图加载项目信息
function trview_proj_load(pid){
	var url="ProjectManage/GetProj?proj_id="+pid;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				//更新表单项目
				$("#trview_proj_id").text(pid);
				$("#trview_proj_name").text(resp.proj_name);
				$("#trview_customer").text(resp.customer);
				$("#trview_priority").text(resp.priority);
				$("#trview_pm").text(resp.pm);
				$("#trview_approver").text(resp.approver);
				$("#trview_responsor").text(resp.responsor);
				$("#trview_start_time").text(resp.start_time);
				$("#trview_plan_end_time").text(resp.plan_end_time);	
				$("#trview_status").text(resp.proj_status);
				$("#trview_team_member").text(resp.team_member);
				$("#trview_goals").html(resp.goals);
				$("#trview_comments").html(resp.comments);
				$("#trview_git").text(resp.git);				
			}
			else alert(resp.message);
		}		
	});
}
//用于加载项目树
function load_projs_tr(){
	//清除当前项目树和项目信息
	$("#treelist").remove();
	$(".tv_blank").text("");
	tag_proj_select=null;
	proj_id=null;
	Proj=null;
	//获取项目树
	var url="ProjectManage/TreeViewProj";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#treebox").append('<ul id="treelist" class="filetree"></ul>');	
				var tao1='';
				var tag=0;
				var mid='';
				for(var i=0;i<resp.pls.length;i++){
					tao1=tao1+'<li><span class="folder">'+resp.pls[i].plname+'</span>';
					var prods=resp.pls[i].ps;
					var tao2='';
					for(var j=0;j<prods.length;j++){
						tao2=tao2+'<li><span class="folder">'+prods[j].pname+'</span>';
						var projs=prods[j].projs;
						var tao3="";
						for(var k=0;k<projs.length;k++){
							tao3=tao3+'<li><span class="file" id="'+projs[k].id+'">'+projs[k].name+'</span></li>';
							if(tag==0){
								tag=1;
								mid=projs[k].id;
							}
						}
						if(tao3!="")tao3='<ul>'+tao3+'</ul>';
						tao2=tao2+tao3+'</li>';
					}
					if(tao2!="")tao2='<ul>'+tao2+'</ul>';
					tao1=tao1+tao2+'</li>';
				}
				$("#treelist").append(tao1);
				$("#treelist").treeview();
				
				if(tag==1)trview_proj_load(mid);
			}
			else alert(resp.message);
		}		
	});
}
//加载待审批项目列表
function load_todolist(){
	$("#tab_projtoAppr").remove();
	tag_proj_select=null;
	proj_id=null;
	Proj=null;
	var para="filter=proj_status='待审批' or proj_status='审批驳回'&page_count=&page_num=";
	var url="ProjectManage/ListProj?"+para;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#tb_projtoAppr").append('<tbody id="tab_projtoAppr"></tbody>');	
				for(var i=0;i<resp.proj_list.length;i++){
					//添加项目记录				
					var tr0='<tr class="tdats">';				
					var td1='<td class="cell">'+resp.proj_list[i].proj_id+'</td>';
					var td2='<td class="cell">'+resp.proj_list[i].product_line+'</td>';
					var td3='<td class="cell">'+resp.proj_list[i].product+'</td>';
					var td4='<td class="cell">'+resp.proj_list[i].customer+'</td>';
					var td5='<td class="cell">'+resp.proj_list[i].proj_name+'</td>';
					var td6='<td class="cell">'+resp.proj_list[i].priority+'</td>';
					var td7='<td class="cell">'+resp.proj_list[i].plan_end_time+'</td>';
					var td8='<td class="cell">'+resp.proj_list[i].responsor+'</td>';
					var td9='<td class="cell">'+resp.proj_list[i].pm+'</td>';
					var td10='<td class="cell">'+resp.proj_list[i].proj_status+'</td>';
					var td11='<td class="cell">'+resp.proj_list[i].approver+'</td>';
					var but1='<button class="td_butt" href="javascript:void(0)" onclick="appr_proj(\''+resp.proj_list[i].proj_id+'\',\'y\')">批准</button>';
					var but2='<button class="td_butt" href="javascript:void(0)" onclick="appr_proj(\''+resp.proj_list[i].proj_id+'\',\'n\')">驳回</button>';
					if(resp.proj_list[i].proj_status=="审批驳回"){
						but1='<button class="td_butt" href="javascript:void(0)" onclick="EditProj(\'edit\',\''+resp.proj_list[i].proj_id+'\')">修改</button>';
						but2='<button class="td_butt2" href="javascript:void(0)" onclick="AppliPro(\''+resp.proj_list[i].proj_id+'\')">从新申请</button>';
					}
					var td12='<td class="cell" align="right">'+but1+'</td>';
					var td13='<td class="cell">'+but2+'</td>';						
					var tr1="</tr>";
					var record=tr0+td1+td2+td3+td4+td5+td6+td7+td8+td9+td10+td11+td12+td13+tr1;
					$("#tab_projtoAppr").append(record);					
				}			
			}
			else alert(resp.message);
		}		
	});
}
//批准和驳回项目
function appr_proj(pid,opt){
	var url="ProjectManage/ApproveProj?proj_id="+pid+"&opt="+opt+"&user="+sessionStorage.customerId;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				if(opt=="y")alert("项目审批通过");
				else alert("项目已被驳回");
				load_todolist();
			}
			else alert(resp.message);
		}		
	});
}
//再次提交审批
function AppliPro(pid){
	var url="ProjectManage/AppliProj?proj_id="+pid+"&user="+sessionStorage.customerId;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				alert("成功提交项目，请等待审批！");
				load_todolist();
			}
			else alert(resp.message);
		}		
	});
	
}
/*打开产品线管理弹层*/
function PLman(){
	var url="ProjectManage/Listpl";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#pls").empty();
				for(var i=0;i<resp.productlines.length;i++)$("#pls").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
				if(resp.productlines.length>0)$("#pls").val(resp.productlines[0]);					
			}
			else alert(resp.message);
		}
	});
	open_form("#PL");
}
/*打开产品管理弹层*/
function Prodman(){
	var url="ProjectManage/Listpl";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#pls_2").empty();
				$("#ps").empty();
				for(var i=0;i<resp.productlines.length;i++)$("#pls_2").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
				if(resp.productlines.length>0)$("#pls_2").val(resp.productlines[0]);

				url="ProjectManage/GetProduts?product_line="+resp.productlines[0];
				TMS_api(url,"GET","",function a(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){	
							for(var i=0;i<resp.products.length;i++){
								$("#ps").append("<option value='"+resp.products[i]+"'>"+resp.products[i]+"</option>");
							}
							if(resp.products.length>0)$("#ps").val(resp.products[0]);
						}
						else alert(resp.message);
					}		
				});
			}
			else alert(resp.message);
		}
	});
	open_form("#produman");
}
function Topage(num){
	if(num==0)page_num=page_sum;
	else page_num=num;
	load_projs();	
}
function Nextpage(tag){
	if(tag=="+"){
		page_num=page_num+1;
		if(page_num>page_sum)page_num=page_sum;
	}
	else if(tag=="-"){
		page_num=page_num-1;
		if(page_num==0)page_num=1;
	}
	load_projs();	
}
$(document).ready(function(){ 
	tag_proj_select=null;
	proj_id=null;
	Proj=null;
	var hostpath_page=getHostUrl('hostpath_page');
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url=hostpath_page+"login.html";
		window.open(encodeURI(url),'_self');
	}
	page_num=1;
	page_sum=0;
	tpitem_ppnum=15;
	projlist_filt="";
	var viewstat=0;
	//初始化页面表单
	//1.获取产品经理列表
	var url="User/TitleList?title=PM";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				for(var i=0;i<resp.userlist.length;i++){
					//为新建项目弹层添加
					$("#new_pm").append("<option>"+resp.userlist[i]+"</option>");
					//为修改项目弹层添加
					$("#pm").append("<option>"+resp.userlist[i]+"</option>");
				}
				//2.加载项目列表
				load_projs();
			}
			else alert(resp.message);				
		}
	});
	
	//项目列表和待审项目的切换
	$("#proj_list").click(function b(){	
		$("#proj_list").css({"color":"#FFFFFF","background-color": "#0DBFB3"});
		$("#proj_todo").css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
		//打开视图切换按钮
		$(".proj_view").show();
		//打开"关闭项目"按钮
		$("#Butt_ProjClose").show();
		$("#proj_view_text").parent().attr("width","20%");
		//打开项目管理列表
		$(".Proj_toAppro").hide();
		if(viewstat==1){
			load_projs_tr();
			$(".ProjTreeview").css("display","block");
			$(".ProjTreeview").show();			
		}
		else {
			page_num=1;
			projlist_filt="";
			load_projs();
			$(".Projlist").show();			
		}	
	});
	$("#proj_todo").click(function b(){	
		$("#proj_todo").css({"color":"#FFFFFF","background-color": "#0DBFB3"});
		$("#proj_list").css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
		//取消视图切换按钮
		$(".proj_view").hide();
		//屏蔽"关闭项目"按钮
		$("#Butt_ProjClose").hide();
		$("#proj_view_text").parent().attr("width","32%");
		//打开项目审批列表
		if($(".Projlist").is(":hidden"))$(".ProjTreeview").hide();
		else $(".Projlist").hide();
		$(".Proj_toAppro").css("display","block");
		$(".Proj_toAppro").show();
		tag_proj_select=null;
		proj_id=null;
		Proj=null;
		page_num=1;
		load_todolist();
	});
	//鼠标滑过按钮的变色效果
	$("button[class*='head_butt left rr']").mouseenter(function(e) { 
		$(e.target).css({"color":"#FFFFFF","background-color": "#0DBFB3"});	
	});
	$("button[class*='head_butt left rr']").mouseleave(function (e) { 
		$(e.target).css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
	});
	//树型视图和列表视图的转换
	$(".proj_view").click(function b(){	
		var type=$("#proj_view").attr("src");
		if(type=="img/trview.png"){
			$(".proj_view").attr("src","img/listview.png");
			$(".proj_view_text").text("切换到列表视图");
			load_projs_tr();
			$(".Projlist").hide();
			$(".ProjTreeview").css("display","block");
			$(".ProjTreeview").show();
			viewstat=1;				
		}
		else{
			$(".proj_view").attr("src","img/trview.png");
			$(".proj_view_text").text("切换到树型视图");
			projlist_filt="";
			load_projs();
			$(".ProjTreeview").hide();
			$(".Projlist").show();
			viewstat=0;
		}
	});
	
	//关闭项目
	$("#Butt_ProjClose").click(function b(){
		if(proj_id==null)alert("请先选择要操作的项目");
		else{	
			var url="ProjectManage/CloseProj?user="+sessionStorage.customerId+"&proj_id="+proj_id;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						alert("项目已关闭");
						if(viewstat==0)	load_projs();
						else trview_proj_load(proj_id);	
						
					}
					else alert(resp.message);
				}
			});
		}
	});
	
	//变更项目
	$("#Butt_ProjUpdate").click(function b(){	
		if(proj_id==null)alert("请先选择要操作的项目");
		else EditProj("edit",proj_id);							
	});
	
	//删除项目
	$("#Butt_ProjDel").click(function b(){
		if(proj_id==null)alert("请先选择要操作的项目");
		else {
			var url="ProjectManage/DelProj?user="+sessionStorage.customerId+"&proj_id="+proj_id;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						alert("项目已删除");
						//表视图直接刷新表格，树视图需要刷新树表,待审批视图刷新审批表
						if($(".Proj_toAppro").is(":hidden")){
							if(viewstat==0)load_projs();
							else trview_proj_load(proj_id);
						}
						else load_todolist();						
					}
					else alert(resp.message);
				}
			});
		}
	});	
	//弹层按钮-添加新产品线
	$("#addpl").click(function b(){
		var a=prompt("请输入新的产品线", "");
		if(a=="")alert("产品线名不能为空！");
		if(null!=a && a!=""){
			url="ProjectManage/Addpl?Produline="+a+"&user="+sessionStorage.customerId;
			TMS_api(url,"GET","",function b(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){							
						$("#pls").append("<option value='"+a+"'>"+a+"</option>"); 
					}
					else alert(resp.message);
				}
			});
		}
	});
	
	//弹层按钮-删除产品线项
	$("#delpl").click(function b(){
		var plname=$("#pls").val();
		if(plname=="")alert("请选择要删除的产品线");
		else{
			url="ProjectManage/Deletepl?Produline="+plname+"&user="+sessionStorage.customerId;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						$("#pls option[value='"+plname+"']").remove();   
					}
					else alert(resp.message);
				}
			});
		}
	});
	
	//弹层按钮-提交新项目
	$("#btnSubmit").click(function(){
		var proj={};
		proj.proj_name= $("#new_proj_name").val();
		proj.responsor=$("#new_responsor").val();
		proj.plan_end_time=$("#new_plan_end_time").val();
		var goals=$("#new_goals").val();
		proj.product_line=$("#new_product_line").val();
		proj.product=$("#new_product").val();

		if(proj.proj_name==""){
			alert("项目名称为必填项！");
			$("#new_proj_name").focus();
		}
		else if(proj.product_line=="" || proj.product_line==null){
			alert("必须为项目选择一个产品线！");
			$("#new_product_line").focus();
		}
		else if(proj.product=="" || proj.product==null){
			alert("必须为项目选择一个产品！");
			$("#new_product").focus();
		}
		else if(proj.responsor==""){
			alert("项目负责人为必填项！");
			$("#new_responsor").focus();
		}
		else if(proj.plan_end_time==""){
			alert("计划完成时间为必填项");
			$("#new_plan_end_time").focus();
		}
		else if(goals==""){
			alert("项目目标为必填项");			
			$("#new_goals").focus();
		}
		else{
			goals=goals.replace(/，/g,",");
			goals=goals.replace(/、/g,",");
			proj.goals=goals.replace(/\n/g,"<br>");
			proj.customer=$("#new_customer").val();
			proj.priority=$("#new_priority").val();
			proj.pm=$("#new_pm").val();
			var body = JSON.stringify(proj);
			url="ProjectManage/AddProj?user="+sessionStorage.customerId;		
			TMS_api(url,"POST",body,function(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						alert("成功提交项目，请等待审批！");
						CloseForm("#newProj");
					/*	if($(".Proj_toAppro").is(":hidden")){
							if(viewstat==0)load_projs();
							else trview_proj_load(proj_id);
						}
						else {
							load_todolist();
						}*/
					}
					else alert(resp.message);
				}
			});
		}
	});
	//弹层按钮-保存项目修改
	$("#btnSave").click(function(){	
		var proj={};
		proj.proj_name= $("#proj_name").val();
		proj.responsor=$("#responsor").val();
		proj.plan_end_time=$("#plan_end_time").val();
		var goals=$("#goals").val();
		var comments=$("#comments").val();
		proj.product_line=$("#product_line").val();
		proj.product=$("#product").val();
		proj.sttime=$("#start_time").val();
		if(proj.sttime=="" || proj.sttime==null){
			var currtime=getDate();
			proj.sttime=currtime.substr(0,currtime.indexOf("-"))+"-01-01";
		}
		if(proj.proj_name==""){
			alert("项目名称为必填项！");
			$("#new_proj_name").focus();
		}
		else if(proj.product_line=="" || proj.product_line==null){
			alert("必须为项目选择一个产品线！");
			$("#new_product_line").focus();
		}
		else if(proj.product=="" || proj.product==null){
			alert("必须为项目选择一个产品！");
			$("#new_product").focus();
		}
		else if(proj.responsor==""){
			alert("项目负责人为必填项！");
			$("#new_responsor").focus();
		}
		else if(proj.plan_end_time==""){
			alert("计划完成时间为必填项");
			$("#new_plan_end_time").focus();
		}
		else if(goals==""){
			alert("项目目标为必填项");			
			$("#new_goals").focus();
		}
		else{
			goals=goals.replace(/，/g,",");
			goals=goals.replace(/、/g,",");
			proj.goals=goals.replace(/\n/g,"<br>");
			comments=comments.replace(/，/g,",");
			comments=comments.replace(/、/g,",");
			proj.comments=comments.replace(/\n/g,"<br>");

			var teamm=$("#team_member").val();
			teamm=teamm.replace(/，/g,",");
			teamm=teamm.replace(/、/g,",");
			teamm=teamm.replace(/  /g,",");
			proj.teamm=teamm.replace(/ /g,",");
			proj.customer=$("#customer").val();
			proj.priority=$("#priority").val();
			proj.pm=$("#pm").val();
			proj.proj_status=$("#proj_status").val();
			proj.approver=$("#approver").val();
			proj.git=$("#git").val();
			var body = JSON.stringify(proj);

			url="ProjectManage/UpdateProj?user="+sessionStorage.customerId+"&proj_id="+$("#proj_id").text();
			body=body+'}';			
			TMS_api(url,"POST",body,function(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						alert("成功保存项目");
						CloseForm("#ProjView");
						if($(".Proj_toAppro").is(":hidden")){
							if(viewstat==0)load_projs();
							else trview_proj_load(proj_id);
						}
						else {
							load_todolist();
						}
					}
					else alert(resp.message);
				}
			});
		}
	});

	//弹层按钮-添加新产品项
	$("#pm_btnSubmit").click(function(){
		var a=prompt("请输入新的产品名称", "");
		if(a=="")alert("产品名不能为空！");
		if(null!=a && a!=""){
			url="ProjectManage/AddProduct?product_line="+$("#pls_2").val()+"&Product="+a+"&user="+sessionStorage.customerId;
			TMS_api(url,"GET","",function b(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						$("#ps").append("<option value='"+a+"'>"+a+"</option>");
					}
					else alert(resp.message);
				}
			});
		}  		
	});
	//弹层按钮-删除产品项
	$("#pm_btnDel").click(function b(){
		var product_name=$("#ps").val();
		if(product_name=="")alert("请选择要删除的产品");
		else{
			url="ProjectManage/DelProduct?product_line="+$("pls_2").val()+"&Product="+product_name+"&user="+sessionStorage.customerId;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						$("#ps option[value='"+product_name+"']").remove();   
					}
					else alert(resp.message);
				}
			});
		}
	});
	//弹层-产品线变更后刷新产品列表(产品管理)
	$("#pls_2").change(function (){
		var pl_name=$("#pls_2").val();
		ChangeProductList(pl_name,"#ps","");
	});
	//弹层-产品线变更后刷新产品列表(新项目)
	$("#new_product_line").change(function (){
		var pl_name=$("#new_product_line").val();
		ChangeProductList(pl_name,"#new_product","");
	});
	//弹层-产品线变更后刷新产品列表(修改项目)
	$("#product_line").change(function (){
		var pl_name=$("#product_line").val();
		ChangeProductList(pl_name,"#product","");
	});
	//弹层拖动
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
	
	$('.newProj_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#newProj').offset().left; 
		var abs_y = event.pageY - $('#newProj').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#newProj'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>670)rel_left=670;
				var rel_top=event.pageY - abs_y;
				if(rel_top<0)rel_top=0;
				if(rel_top>600)rel_top=600;
				obj.css({'left':rel_left, 'top':rel_top});  
			} 
		}).mouseup( function () { 
			isMove = false; 
		}); 
	});
	
	$('.ProjView_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#ProjView').offset().left; 
		var abs_y = event.pageY - $('#ProjView').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#ProjView'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>670)rel_left=670;
				var rel_top=event.pageY - abs_y;
				if(rel_top<0)rel_top=0;
				if(rel_top>600)rel_top=600;
				obj.css({'left':rel_left, 'top':rel_top});  
			} 
		}).mouseup( function () { 
			isMove = false; 
		}); 
	});
	
	$('#prodm_header').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#produman').offset().left; 
		var abs_y = event.pageY - $('#produman').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#produman'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>446)rel_left=446;
				var rel_top=event.pageY - abs_y;
				if(rel_top<0)rel_top=0;
				if(rel_top>470)rel_top=470;
				obj.css({'left':rel_left, 'top':rel_top});  
			} 
		}).mouseup( function () { 
			isMove = false; 
		}); 
	});
});

$(document).click(function (e) { 
	var v_class=""+$(e.target).parent().attr('class');
	//当项目被点击
	if(v_class=="tdats"){
		Proj=$(e.target).parent();
		if(tag_proj_select==null){
			//有项目被选中
			tag_proj_select=Proj;
			Proj.css("background-color","#E2F6CB");
			proj_id=Proj.children().eq(0).text();
		}
		else{
			var child_selec_proj=tag_proj_select.children().eq(0).text();
			var	child_new_proj=Proj.children().eq(0).text();
			//项目被取消
			if(child_selec_proj==child_new_proj){
				tag_proj_select=null;
				proj_id=null;
				Proj.css("background-color","#FFFFFF");
			}
			//选择了其他项目
			else{
				tag_proj_select.css("background-color","#FFFFFF");
				tag_proj_select=Proj;
				Proj.css("background-color","#E2F6CB");
				proj_id=Proj.children().eq(0).text();
			}
		}
	}
	else{
		//树型视图下选择项目
		if($(e.target).attr('class')=="file"){
			var pid=$(e.target).attr("id");
			if(proj_id!=pid){
				$("#"+proj_id).css("background-color","#FFFFFF");
				$(e.target).css("background-color","#E2F6CB");
				proj_id=pid;
			}
			trview_proj_load(proj_id);	
		}
	}
});