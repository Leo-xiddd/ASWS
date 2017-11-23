var Testproj;
var projn;
var projv;
var pm;
var tag_proj_select;
var tag_task_select;
var xmlHttp;
var st_indx;
var tes;
var st_time;
var tp_name;
var hostpath_page;
var Tplist_page_num;
var Tplist_page_sum;
var tpitem_ppnum;
var tplist_his; 
function TMS_api(url,med,dats,cfunc){
	var hostpath=getHostUrl('hostpath_api_tms');		
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
//关闭弹层
function CloseForm(formID){
	$(formID).hide();
	$("#overlay").hide();
	location.reload();
}
function openTP(type){
	if(type=="new"){
		tp_name="";
		$(".newtp_title").text("添加新的测试项目");
		$(".newtprw").val("");
		$("#Proj_Priority").val("");	
		$("#Proj_Name").attr("readonly",false);
		$("#Proj_Version").attr("readonly",false);		
		$("#Expect_StartTime").attr("readonly",false);
		$("#Proj_Priority").attr("readonly",false);
		$("#Proj_Manager").attr("disabled",false);
		$("#Test_Manager").attr("disabled",false);		
		$("#productline").attr("disabled",false);
		$("#Test_Engineer").attr("readonly",false);
		$("#Others").attr("readonly",false);

		$("#Proj_Name").css("background-color","#FFFFFF");
		$("#Proj_Version").css("background-color","#FFFFFF");
		$("#Expect_StartTime").css("background-color","#FFFFFF");
		$("#Proj_Priority").css("background-color","#FFFFFF");
		$("#Proj_Manager").css("background-color","#FFFFFF");
		$("#Test_Manager").css("background-color","#FFFFFF");
		$("#productline").css("background-color","#FFFFFF");
		$("#Test_Engineer").css("background-color","#FFFFFF");
		$("#Others").css("background-color","#FFFFFF");
		
		$("#Fact_StartTime").attr("readonly",true);
		$("#Fact_StartTime").css("background-color","#DDD");
		$("#TP_butts").css("left","330px");
		$("#btnDel").hide();
		$("#BugReview").hide();	
		$("#btnSubmit").text("提交");
		$("#btnSubmit").show();
	}
	//编辑'测试项目'页，可以保存、删除和取消
	else if(type=="edit"){	
		tp_name="1";
		$(".newtp_title").text("编辑测试项目");
		$("#btnDel").show();
	}
	open_form("#newTP");
}
function edittp(pname,pversion){
	$("#Proj_Name").val(pname);
	$("#Proj_Version").val(pversion);
	$("#Proj_Name").css("background-color","#E3E3E3");
	$("#Proj_Version").css("background-color","#E3E3E3");
	$("#Proj_Name").attr("readonly",true);
	$("#Proj_Version").attr("readonly",true);
	$("#Fact_StartTime").attr("readonly",false);
	$("#Fact_StartTime").css("background-color","#FFFFFF");
	var url="TestTask/Get?user="+sessionStorage.customerId+"&proj="+$("#Proj_Name").val()+"&version="+$("#Proj_Version").val();
	TMS_api(encodeURI(url),"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){					
				$("#Test_Engineer").val(resp.Test_Engineer);
				$("#Proj_Manager").val(resp.Proj_Manager);
				$("#Proj_Priority").val(resp.Proj_Priority);
				$("#Test_Manager").val(resp.Test_Manager);
				$("#Expect_StartTime").val(resp.Expect_StartTime);
				$("#productline").val(resp.Proj_Productline);
				$("#productline").attr("disabled",true);
				$("#productline").css("background-color","#E3E3E3");
				$("#Others").val(resp.Others);
				if(resp.Fact_StartTime!="" && typeof(resp.Fact_StartTime)!="undefined"){
					var tt=resp.Fact_StartTime;
					tt=tt.substring(0,tt.indexOf(" "));
					$("#Fact_StartTime").val(tt);
				}
				else $("#Fact_StartTime").val("");
				
				//如果当前项目已关闭，优先级为0，则所有属性都不可改
				if(resp.Proj_Priority=="0"){
					$("#Fact_StartTime").attr("readonly",true);
					$("#Fact_StartTime").css("background-color","#E3E3E3");					
					$("#Test_Engineer").attr("readonly",true);
					$("#Test_Engineer").css("background-color","#E3E3E3");
					$("#Proj_Manager").attr("disabled",true);
					$("#Proj_Manager").css("background-color","#E3E3E3");
					$("#Proj_Priority").attr("readonly",true);
					$("#Proj_Priority").css("background-color","#E3E3E3");
					$("#Test_Manager").attr("disabled",true);
					$("#Test_Manager").css("background-color","#E3E3E3");
					$("#Expect_StartTime").attr("readonly",true);
					$("#Expect_StartTime").css("background-color","#E3E3E3");
					$("#Others").attr("readonly",true);
					$("#Others").css("background-color","#E3E3E3");
					$("#btnSubmit").hide();
					$("#TP_butts").css("left","300px");
				}
				else{				
					$("#Test_Engineer").attr("readonly",false);
					$("#Test_Engineer").css("background-color","#FFFFFF");
					$("#Proj_Manager").attr("disabled",false);
					$("#Proj_Manager").css("background-color","#FFFFFF");
					$("#Proj_Priority").attr("readonly",false);
					$("#Proj_Priority").css("background-color","#FFFFFF");
					$("#Test_Manager").attr("disabled",false);
					$("#Test_Manager").css("background-color","#FFFFFF");
					$("#Expect_StartTime").attr("readonly",false);
					$("#Expect_StartTime").css("background-color","#FFFFFF");
					$("#Others").attr("readonly",false);
					$("#Others").css("background-color","#FFFFFF");
					$("#btnSubmit").text("保存");
					$("#btnSubmit").show();
					$("#TP_butts").css("left","243px");
				}
				openTP("edit");
				open_form("#newTP");
				$("#BugReview").show();				
			}
			else alert(resp.message);
		}
	});
}
function opentr(stname,result,type){	
	if(result=="finish"){
		var trr=stname.substring(stname.indexOf("_")+1,stname.lastIndexOf("_"));
		trr="TR_"+trr.substring(0,trr.lastIndexOf("_"));
		var url="TestReport/Get?TR_index="+trr;
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){	
					result=resp.TestResult;
					sessionStorage.currpage=encodeURI(hostpath_page+"Testreport.html?ST_index="+stname+"&type="+type+"&Result="+result+"&st_time="+st_time+"&tes="+tes);
					if(result=="reject")sessionStorage.currpage=encodeURI(hostpath_page+"Testreport_RFU.html?ST_index="+stname+"&type="+type+"&st_time="+st_time+"&tes="+tes);
					$("#main", parent.parent.document).attr("src",sessionStorage.currpage);
				}
				else alert(resp.message);
			}
		});
	}
	else{
		sessionStorage.currpage=encodeURI(hostpath_page+"Testreport.html?ST_index="+stname+"&type="+type+"&Result="+result+"&st_time="+st_time+"&tes="+tes);
		if(result=="reject")sessionStorage.currpage=encodeURI(hostpath_page+"Testreport_RFU.html?ST_index="+stname+"&type="+type+"&st_time="+st_time+"&tes="+tes);
		$("#main", parent.parent.document).attr("src",sessionStorage.currpage);
	}
}
//刷新曲线图表
function refresh_pic(picname){
	var xAxis_dats=[];
	var dats=[
		{
			seriers:"未关闭Bug数",
			type:'line',
			color1:'#B70B19',
			color2:'#B70B19',
			markPoint:'',
			markLine:'',
			dat:''
		}
	];
	if(projn!="" && projv!=""){
		var url="PQR/GetPQD?Proj_Name="+projn+"&Proj_Version="+projv;
		TMS_api(url,"GET","",function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					var sts=resp.PQD;
					var temp_dat="";
					if(picname=="DCL"){
						for(var i=0;i<sts.length;i++){
							xAxis_dats.push("第"+sts[i].Cycles+"轮");
							temp_dat=temp_dat+sts[i].Num_bug_unclose+",";
						}
						dats[0].dat=temp_dat.substring(0,temp_dat.length-1);
						BarLine_chart("TPchart","",xAxis_dats,dats)
					}
					else if(picname=="DQS"){
						for(var i=0;i<sts.length;i++){
							xAxis_dats.push("第"+sts[i].Cycles+"轮");
							temp_dat=temp_dat+sts[i].Num_dqs+",";
						}
						dats[0].dat=temp_dat.substring(0,temp_dat.length-1);
						dats[0].seriers="DQS";
						BarLine_chart("TPchart","",xAxis_dats,dats)
					}
				}
				else alert(resp.message);
			}
		});
	}
}
// 刷新rps曲线
function load_rps_chart(task_index){
	var xAxis_dats=[];
	var dats=[
		{
			seriers:"响应时间（ms）",
			type:'line',
			color1:'#B70B19',
			color2:'#B70B19',
			markPoint:'',
			markLine:{
				data:[
					{yAxis : 0, name : '90% line'}
				]
			},
			dat:''
		}
	];
	var url="TestReport/Get_PT_chart?TR_index="+task_index+"&chart=rps";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var dat_temp="";
				for(var i=0;i<resp.chartdata.length;i++){
					xAxis_dats.push(resp.chartdata[i].xAxis);
					dat_temp=dat_temp+resp.chartdata[i].yAxis+",";
				}
				dats[0].dat=dat_temp.substring(0,dat_temp.length-1);
				dats[0].markLine.data[0].yAxis=parseInt(resp.perc90);
				BarLine_chart("PTPchart","",xAxis_dats,dats);
			}
			else alert(resp.message);
		}
	});	
}
// 打开性能测试报告
function opentr_pt(ST_index){
	sessionStorage.currpage=encodeURI(hostpath_page+"PT_Testreport.html?ST_index="+ST_index);
	$("#main", parent.document).attr("src",sessionStorage.currpage);
}
//用于刷新测试任务状态模块
function load_subtask(){
	//更新子任务表
	var url=hostpath_page+"subtest.html?proj="+projn+"&version="+projv;
	$("#subtest").attr("src",url);
	//刷新DQS和DCL曲线。默认切回DCL
	$("#butt_DCL").css({"color":"#FFFFFF","background-color": "#0DBFB3"});
	$("#butt_DQS").css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
	//刷新DQS和DCL曲线
	$("#titl_TPchart").text("项目Bug收敛曲线");

	// 更新性能任务
	url="TestTask/ListSubTask?proj="+projn+"&version="+projv+"&HistSwitch=0";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var body = JSON.parse(xmlHttp.responseText);
			if(body.code==200){	
				$("#tab_TPlist").remove();
				$("#table_subPTtest").append('<tbody id="tab_TPlist"></tbody>');
				var subtt=body.tp_list[0].SubTask_pt;
				var st="";
				for(var k=0;k<subtt.length;k++){
					st=st+'<tr class="tdats_pt" data-index="'+subtt[k].ST_index+'">';
					st=st+'<td class="cell"><a href="javascript:void(0)" onclick="opentr_pt(\''+
						subtt[k].ST_index+'\')">'+subtt[k].index+'</a></td>';
					st=st+'<td class="cell">'+subtt[k].Start_time+'</td>';
					st=st+'<td class="cell">'+subtt[k].End_time+'</td>';
					st=st+'<td class="cell">'+subtt[k].TestStatus+'</td>';
					st=st+'<td class="cell">'+subtt[k].Test_Time+'</td></tr>';
				}
				$("#tab_TPlist").append(st);
				var xAxis_dats=[];
				var dats=[
					{
						seriers:"响应时间（ms）",
						type:'line',
						color1:'#B70B19',
						color2:'#B70B19',
						markPoint:'',
						markLine:{
							data:[
								{yAxis : 0, name : '90% line'}
							]
						},
						dat:''
					}
				];
				if(st!=""){					
					var url="TestReport/Get_PT_chart?TR_index="+subtt[0].ST_index+"&chart=rps";
					TMS_api(url,"GET","",function a(){
						if (xmlHttp.readyState==4 && xmlHttp.status==200){
							var resp = JSON.parse(xmlHttp.responseText);
							if(resp.code==200){	
								var dat_temp="";
								for(var i=0;i<resp.chartdata.length;i++){
									xAxis_dats.push(resp.chartdata[i].xAxis);
									dat_temp=dat_temp+resp.chartdata[i].yAxis+",";
								}
								dats[0].dat=dat_temp.substring(0,dat_temp.length-1);
								dats[0].markLine.data[0].yAxis=parseInt(resp.perc90);
								BarLine_chart("PTPchart","",xAxis_dats,dats);
								refresh_pic("DCL");
							}
							else alert(resp.message);
						}
					});	
				}
				else {
					BarLine_chart("PTPchart","",xAxis_dats,dats);
					refresh_pic("DCL");
				}
			}
			else alert(body.message);
		}
	});	
}
//用于加载测试项目表
function reload_tplist(proirity){
	//清除当前所有项目
	$("#tab_tplist").remove();
	//更新列表
	var para="filter=&HistSwitch="+proirity+"&page_count="+tpitem_ppnum+"&page_num="+Tplist_page_num;
	var url="TestTask/List?"+para;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var body = JSON.parse(xmlHttp.responseText);
			if(body.code==200){	
				if(proirity==1 && body.tp_list.length==0){
					$("#butt_hist").click();
				}
				$("#tb_tplist").append('<tbody id="tab_tplist"></tbody>');	
				var FST="";
				for(var i=0;i<body.tp_list.length;i++){
					//添加测试项目记录				
					var tr0='<tr class="tdats">';				
					var td1='<td class="cell">'+body.tp_list[i].id+'</td>';
					var td2='<td class="cell"><a class="edittp" href="javascript:void(0)" onclick="edittp(\''+body.tp_list[i].Proj_Name+'\',\''+body.tp_list[i].Proj_Version+'\')">'+body.tp_list[i].Proj_Name+'</a></td>';
					var td3='<td class="cell">'+body.tp_list[i].Proj_Version+'</td>';
					var td4='<td class="cell">'+body.tp_list[i].Proj_Manager+'</td>';
					var td5='<td class="cell">'+body.tp_list[i].Test_Manager+'</td>';
					var td6='<td class="cell">'+body.tp_list[i].Proj_Priority+'</td>';
					var td7='<td class="cell">'+body.tp_list[i].Expect_StartTime+'</td>';
					if(typeof(body.tp_list[i].Fact_StartTime)!='undefined')FST=body.tp_list[i].Fact_StartTime;
					else FST="";
					var td8='<td class="cell">'+FST+'</td>';
					var td9='<td class="cell">'+body.tp_list[i].Proj_Status+'</td>';
					var td10='<td class="cell">'+body.tp_list[i].Test_Status+'</td>';
					var td11='<td class="cell">'+body.tp_list[i].Test_Time+'</td>';
					var td12='<td class="cell">'+body.tp_list[i].Test_Cycle+'</td>';		
					var tr1="</tr>";
					var record=tr0+td1+td2+td3+td4+td5+td6+td7+td8+td9+td10+td11+td12+tr1;
					$("#tab_tplist").append(record);					
				}
				var TP_sum=parseInt(body.total_num);
				Tplist_page_sum=Math.ceil(TP_sum/tpitem_ppnum);
				$("#page_num").text(Tplist_page_sum);
				$("#curr_page").text(Tplist_page_num);
				if(Tplist_page_num==1){
					$("#Fir_page").attr("disabled",true);
					$("#Pre_page").attr("disabled",true);
				}
				else{
					$("#Fir_page").attr("disabled",false);
					$("#Pre_page").attr("disabled",false);
				}
				if(Tplist_page_num==Tplist_page_sum){
					$("#Next_page").attr("disabled",true);
					$("#Las_page").attr("disabled",true);
				}
				else{
					$("#Next_page").attr("disabled",false);
					$("#Las_page").attr("disabled",false);
				}
				//默认选择第一行的项目刷新项目任务模块
				if(TP_sum>0){
					tes=body.tp_list[0].Test_Engineer;
					projn=body.tp_list[0].Proj_Name;
					projv=body.tp_list[0].Proj_Version;
					$("#proj_lable").text(projn+"_"+projv);
					load_subtask();
				}				
			}
			else alert(body.message);
		}		
	});
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
// 弹层按钮-添加性能测试中的URL参数
function addPT_para(e){
	var para=prompt("请按照[参数]=[参数内容或说明]的格式输入参数信息");
	if(null!=para){
		var tag=0;
		var para_sel=$(e).parent().parent().parent().find(".params")
		var paralist=para_sel.children();
		for(var i=0;i<paralist.length;i++){
			var par=paralist.eq(i).html();
			if(par==para){
				alert("参数已存在！");
				tag=1;
				break;
			}
		}
		if(tag==0)para_sel.append("<option>"+para+"</option>");
	}
}
$(document).ready(function(){ 
	tag_proj_select=null;
	Testproj=null;
	hostpath_page=getHostUrl('hostpath_page');
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url=hostpath_page+"login.html";
		window.open(encodeURI(url),'_self');
	}
	st_time="";
	tes="";
	projn="";
	projv="";
	Tplist_page_num=1;
	Tplist_page_sum=0;
	tpitem_ppnum=5;
	tplist_his=1;
	//初始化页面表单,
	//1.获取产品经理列表
	var url="User/TitleList?title=PM";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				for(var i=0;i<resp.userlist.length;i++){
					//添加项目弹层
					$("#Proj_Manager").append("<option>"+resp.userlist[i]+"</option>");
					//添加提测弹层
					$("#sb_Proj_Manager").append("<option>"+resp.userlist[i]+"</option>"); 
				}
					
				//2.获取测试经理列表
				url="User/TitleList?title=TM";
				TMS_api(url,"GET","",function a(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){	
							for(var i=0;i<resp.userlist.length;i++){
								$("#Test_Manager").append("<option>"+resp.userlist[i]+"</option>"); 
							}
							$("#Test_Manager").val(sessionStorage.usrfullname);	
							
							//3.获取产品线列表
							url="ProjectManage/Listpl";
							TMS_api(url,"GET","",function a(){
								if (xmlHttp.readyState==4 && xmlHttp.status==200){
									resp = JSON.parse(xmlHttp.responseText);
									if(resp.code==200){	
										for(var i=0;i<resp.productlines.length;i++){
											$("#productline").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
											$("#pls").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
										}
										if(resp.productlines.length>0)$("#productline").val(resp.productlines[0]);
										//4.加载项目列表
										reload_tplist(tplist_his);
									}
									else alert(resp.message);
								}
							});
						}
						else alert(resp.message);
					}
				});
			}
			else alert(resp.message);				
		}
	});
	//功能测试任务和性能测试任务记录的切换
	$(".butt_switch").click(function b(){	
		$("#PTList").slideToggle();
		$("#TPsumm").slideToggle();
	});

	//历史记录和当前及记录的切换
	$("#butt_currt").click(function b(){	
		$("#butt_currt").css({"color":"#FFFFFF","background-color": "#0DBFB3"});
		$("#butt_hist").css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
		tplist_his=1;
		Tplist_page_num=1;
		reload_tplist(tplist_his);
	});
	$("#butt_hist").click(function b(){	
		$("#butt_hist").css({"color":"#FFFFFF","background-color": "#0DBFB3"});
		$("#butt_currt").css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
		tplist_his=0;
		Tplist_page_num=1;
		reload_tplist(tplist_his);
	});
	//DCL和DQS曲线图表切换
	$("#butt_DCL").click(function b(){	
		$("#butt_DCL").css({"color":"#FFFFFF","background-color": "#0DBFB3"});
		$("#butt_DQS").css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
		$("#titl_TPchart").text("项目Bug收敛曲线");
		refresh_pic("DCL");
	});
	$("#butt_DQS").click(function b(){	
		$("#butt_DQS").css({"color":"#FFFFFF","background-color": "#0DBFB3"});
		$("#butt_DCL").css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
		$("#titl_TPchart").text("项目上线质量指数曲线");
		refresh_pic("DQS");
	});
	//鼠标滑过按钮的变色效果
	$("button[class*='head_butt left rr']").mouseenter(function(e) { 
		$(e.target).css({"color":"#FFFFFF","background-color": "#0DBFB3"});	
	});
	$("button[class*='head_butt left rr']").mouseleave(function (e) { 
		$(e.target).css({"color":"#091E43","background-color": "rgba(199,240,224,0.3)"});
	});
	//收缩或展开项目状态模块
	$("#tpsumm_fold").click(function b(){	
		$("#TPsumm").slideToggle("fast");
		//从新加载项目
		if(tpitem_ppnum==15)tpitem_ppnum=5;
		else tpitem_ppnum=15;
		Tplist_page_num=1;
		reload_tplist(tplist_his);		
	});	
	// 提测单弹层-功能提测和性能提测的切换
	$("#sb_test_type").change(function(e){
		$("#sb_FT").slideToggle();
		$("#sb_PT").slideToggle();
		if($("#addInterf").is(":hidden"))$("#addInterf").show();
		else $("#addInterf").hide();
	});
	$("#closeTP").click(function b(){
		if(tag_proj_select==null)alert("请先选择要操作的项目");
		else{
			var Proj_Name=Testproj.children().eq(1).children().eq(0).text();	
			var Proj_Version=Testproj.children().eq(2).text();	
			var url="TestTask/Close?user="+sessionStorage.customerId+"&proj="+Proj_Name+"&version="+Proj_Version;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						alert("项目已关闭");
					}
					else alert(resp.message);
				}
			});
		}
	});
	
	//点击按钮添加提测
	var Proj_Name_sb="";
	var Proj_Version_sb="";
	$("#butt_addsb").click(function b(){	
		if(tag_proj_select==null)alert("请先选择要操作的项目");
		else{
			Proj_Name_sb=Testproj.children().eq(1).children().eq(0).text();	
			Proj_Version_sb=Testproj.children().eq(2).text();	
			$("#Submittime").text(getDate());
			$("#Submitter").text(sessionStorage.usrfullname);	
			$("#Proj_Subversion").val("");
			$("#Range").val("");
			$("#FunctionDes").val("");
			$("#Developer").val("");
			$("#UED_codeurl").val("http://git.ctags.cn/***.git");
			$("#front_codeurl").val("http://git.ctags.cn/***.git");
			$("#Other_codeurl").val("http://git.ctags.cn/***.git");
			$("#UED_codeurl_branch").val("");
			$("#front_codeurl_branch").val("");
			$("#Other_codeurl_branch").val("");
			$("#Wikiurl").val("http://jw.tech.bitauto.com:8090/pages/viewpage.action?pageId=");
			open_form("#newSB");				
		}		
	});
	//点击按钮查看提测
	$("#butt_checksb").click(function b(){	
		if(tag_proj_select==null)alert("请先选择要操作的项目");
		else{
			var pname=tag_proj_select.children().eq(1).children().eq(0).text();
			var pver=tag_proj_select.children().eq(2).text();
			sessionStorage.currpage=encodeURI(hostpath_page+"Submit_main.html?proj="+pname+"&vers="+pver);
			$("#main", parent.document).attr("src",sessionStorage.currpage);
		}
	});
	//弹层按钮-添加新产品线
	$("#addpl").click(function b(){
		var a=prompt("请输入新的产品线", "");
		if(a=="")alert("产品线名不能为空！");
		if(null!=a && a!=""){
			url="ProjectManage/Addpl?Produline="+a+"&user="+sessionStorage.customerId;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						$("#productline option").remove();
						$("#pls option").remove();
						for(var i=0;i<resp.productlines.length;i++){
							$("#productline").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
							$("#pls").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
						}
					}
					else alert(resp.message);
				}
			});
		}
	});
	var plname="";
	$("#pls").change(function(){
		plname=$("#pls").val();
	});
	//弹层按钮-删除产品线项
	$("#delpl").click(function b(){
		if(plname=="")alert("请选择要删除的产品线");
		else{
			url="ProjectManage/Deletepl?Produline="+plname+"&user="+sessionStorage.customerId;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						$("#productline option").remove();
						$("#pls option").remove();
						for(var i=0;i<resp.productlines.length;i++){
							$("#productline").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
							$("#pls").append("<option value='"+resp.productlines[i]+"'>"+resp.productlines[i]+"</option>"); 
						}
					}
					else alert(resp.message);
				}
			});
		}
	});
	//弹层按钮-查看项目Bug
	$("#BugReview").click(function(){
		var proj=$("#Proj_Name").val();
		var domain=$("#productline").val();
		$("#overlay").hide();
		var url=hostpath_page+"DefectList.html?domain="+domain+"&proj="+proj;
		sessionStorage.currpage=encodeURI(url);
		window.open(encodeURI(url),'_self');
	});
	//弹层按钮-提交项目
	$("#btnSubmit").click(function(){	
		var projdat={};
		projdat.Proj_Priority=$("#Proj_Priority").val();
		projdat.Proj_Name= $("#Proj_Name").val();
		projdat.Proj_Version=$("#Proj_Version").val();
		projdat.Expect_StartTime=$("#Expect_StartTime").val();
		projdat.pl=$("#productline").val();

		if(projdat.Proj_Name==""){
			alert("项目名称为必填项！");
			$("#Proj_Name").focus();
		}
		else if(projdat.Proj_Version==""){
			alert("项目版本为必填项！");
			$("#Proj_Version").focus();
		}
		else if(projdat.Expect_StartTime==""){
			alert("期望测试时间为必填项");
			$("#Expect_StartTime").focus();
		}
		else if(projdat.pl=="" || projdat.pl==null){
			alert("产品线信息为必填项");
			$("#productline").focus();
		}
		else if(projdat.Proj_Priority==""){
			alert("项目优先级为必填项，只能填写0-500之间的数字");
			$("#Proj_Priority").focus();
		}
		else if(isNaN(projdat.Proj_Priority)){
			alert("项目优先级为必填项，只能填写0-500之间的数字");
		}
		else if(parseInt(projdat.Proj_Priority)>500){
			alert("项目优先级为必填项，只能填写0-500之间的数字");
		}
		else{
			var Others=$("#Others").val();
			Others=Others.replace(/，/g,",");
			projdat.Others=Others.replace(/、/g,",");
			var testers=$("#Test_Engineer").val();
			testers=testers.replace(/，/g,",");
			projdat.Test_Engineer=testers.replace(/、/g,",");
			projdat.Proj_Productline=$("#productline").val();
			projdat.Proj_Manager=$("#Proj_Manager").val();
			projdat.Test_Manager=$("#Test_Manager").val();

			url="TestTask/Add?user="+sessionStorage.customerId;
			if(tp_name=="1"){
				url="TestTask/Update?user="+sessionStorage.customerId;
				projdat.Fact_StartTime=$("#Fact_StartTime").val();		
			}
			var body=JSON.stringify(projdat);			
			TMS_api(url,"POST",body,function(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						alert("成功保存项目");
						CloseForm("#newTP");
						tp_name="";
					}
					else alert(resp.message);
				}
			});
		}
	});
	//弹层按钮-删除当前项目
	$("#btnDel").click(function b(){
		var delconfirm=confirm("确认要删除测试项目"+$("#Proj_Name").val()+"吗？");
		if(delconfirm==true){
			url="TestTask/Delete?user="+sessionStorage.customerId+
				"&proj="+$("#Proj_Name").val()+"&version="+$("#Proj_Version").val();
			TMS_api(encodeURI(url),"GET","",function(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200)CloseForm("#newTP");
					else alert(resp.message);
				}
			});
		}		
	});	
	//弹层按钮-提交提测
	$("#sb_btnSubmit").click(function(){
		var submit={};
		// 公共基础信息
		submit.Proj_Manager=$("#sb_Proj_Manager").val();
		submit.Submittime=$("#Submittime").text();
		submit.Proj_Name=Proj_Name_sb;
		submit.Proj_Version=Proj_Version_sb;
		submit.Submitter=sessionStorage.usrfullname;
		submit.Proj_Subversion=$("#Proj_Subversion").val();	
		var testtype=$("#sb_test_type option:selected").val();	

		if(testtype=="功能"){
			var FunctionDes=$("#FunctionDes").val();
			FunctionDes=FunctionDes.replace(/；/g,";");
			submit.FunctionDes=FunctionDes.replace(/\n/g,"<br>");
			
			var Range=$("#Range").val();
			Range=Range.replace(/；/g,";");
			submit.Range=Range.replace(/\n/g,"<br>");
			
			var Developer=$("#Developer").val();
			Developer=Developer.replace(/，/g,",");
			submit.Developer=Developer.replace(/、/g,",");

			var UED_codeurl=$("#UED_codeurl").val();
			if(UED_codeurl=="http://git.ctags.cn/***.git")UED_codeurl="null";
			submit.UED_codeurl=UED_codeurl+"::"+$("#UED_codeurl_branch").val();

			var front_codeurl=$("#front_codeurl").val();
			if(front_codeurl=="http://git.ctags.cn/***.git")front_codeurl="null";
			submit.front_codeurl=front_codeurl+"::"+$("#front_codeurl_branch").val();

			var Other_codeurl=$("#Other_codeurl").val();
			if(Other_codeurl=="http://git.ctags.cn/***.git")Other_codeurl="null";
			submit.Other_codeurl=Other_codeurl+"::"+$("#Other_codeurl_branch").val();

			var Wikiurl=$("#Wikiurl").val();
			if(Wikiurl=='http://jw.tech.bitauto.com:8090/pages/viewpage.action?pageId=')Wikiurl="null";
			submit.Wikiurl=Wikiurl;

			if(submit.Proj_Subversion=="")alert("请补充提测版本信息");
			else if(submit.FunctionDes=="")alert("请补充功能说明");
			else if(submit.Range=="")alert("请补充测试范围说明");
			else {
				submit.Note="";
				var body=JSON.stringify(submit);
				url="TestTask/AddSubmit?user="+sessionStorage.customerId;
				TMS_api(encodeURI(url),"POST",body,function(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){	
							alert("提交成功");
							CloseForm("#newSB");
						}
						else alert(resp.message);
					}
				});    
			}    		
		}
		// 性能测试提测单
		else{
			var PT_sbforms=$("#sb_PT").find(".sb_PT_form");
			var interfs=[];
			var err_tag=0;
			for(var i=0;i<PT_sbforms.length;i++){
				var sbform={};
				// 接口信息(required)
				var elem_host=PT_sbforms.eq(i).find(".url");
				sbform.url=elem_host.val();				
				if(sbform.url==""){
					alert("接口的URL不能为空");
					err_tag=1;
					elem_host.focus();
					break;
				}
				// 接口说明
				var note=PT_sbforms.eq(i).find(".Note").val();
				if(note!=""){
					note=note.replace(/\n/g,"<br>");
				}
				sbform.note=note;
				// 接口参数
				var paras=[];
				var para_list=PT_sbforms.eq(i).find(".params").find("option");
				for(var j=0;j<para_list.length;j++){
					paras.push(para_list.eq(j).html());
				}
				sbform.paras=paras;
				// 预期结果(勾选的结果必须填写)
				sbform.rps_exp="";
				var result_sel=PT_sbforms.eq(i).find(".rps_sel");
				if(result_sel.is(':checked')){
					var exp=PT_sbforms.eq(i).find(".rps");
					if(exp.val()==""){
						alert("预期rps值被勾选，不能为空！");
						err_tag=1;
						exp.focus();
						break;
					}
					else sbform.rps_exp=exp.val();
				}
				sbform.averresptime_exp="";
				result_sel=PT_sbforms.eq(i).find(".averresptime_sel");
				if(result_sel.is(':checked')){
					var exp=PT_sbforms.eq(i).find(".averresptime");
					if(exp.val()==""){
						alert("预期平均响应时间被勾选，不能为空！");
						err_tag=1;
						exp.focus();
						break;
					}
					else sbform.averresptime_exp=exp.val();
				}
				sbform.maxresptime_exp="";
				result_sel=PT_sbforms.eq(i).find(".maxresptime_sel");
				if(result_sel.is(':checked')){
					var exp=PT_sbforms.eq(i).find(".maxresptime");
					if(exp.val()==""){
						alert("最大响应时间被勾选，不能为空！");
						err_tag=1;
						exp.focus();
						break;
					}
					else sbform.maxresptime_exp=exp.val();
				}
				interfs.push(sbform);
			}

			if(err_tag==0 && submit.Proj_Subversion==""){
				alert("提测版本不能为空！");
				err_tag=1;
				$("#Proj_Subversion").focus();
			}
			if(err_tag==0){
				submit.interfs=interfs;
				var body=JSON.stringify(submit);
				url="TestTask/AddPTSubmit?user="+sessionStorage.customerId;
				TMS_api(encodeURI(url),"POST",body,function(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){	
							alert("提交成功");
							CloseForm("#newSB");
						}
						else alert(resp.message);
					}
				});   
			}  		
		}		
	});
	//弹层操作 - 选择测试报告类型
	$("input[class*='ra_butt']").click(function(e){	
		var trs=""+$(e.target).attr('class');
		CloseForm("#endtest");
		var result=trs.substring(trs.lastIndexOf("_")+1);
		var st_name=$("#endtest_ST_index").text();
		tes=$("#endtest_tes").text();
		st_time=$("#endtest_sttime").text();
		st_indx=""
		opentr(st_name,result,"new");
	});	
	// 弹层按钮-添加新接口
	$("#addInterf").click(function(e){	
		var int='<div class="sb_PT_form"><label>接口URL</label><input type="text" class="url" style="margin-left: 5px;width: 660px;">';
		var cont='<table cellspacing="0" cellpadding="0" width=100% style="margin-bottom: 20px;"><tbody>';
		cont=cont+'<tr valign="bottom" style="height:20px"><td width="250px">接口说明</td><td width="280px">参数说明'+
				'<img src="img/additem.png" width="16" height="16" title="新增" class="add_pt_para" href="javascript:void(0)"'+
				' onclick="addPT_para(this)"></td><td>预期结果</td></tr>';
		cont=cont+'<tr><td rowspan="3" valign="top"><textarea class="Note"></textarea></td><td rowspan="3" valign="top">';
		cont=cont+'<select class="params" multiple="multiple" size="5" style="width:260px"></select></td>';
		cont=cont+'<td><input type="checkbox" class="rps_sel">接口响应速度<input type="text" class="sta rps"><span>rps</span></td></tr>';
		cont=cont+'<tr><td><input type="checkbox" class="averresptime_sel">平均响应时间<input type="text" class="sta averresptime"><span>毫秒</span></td></tr>';
		cont=cont+'<tr><td><input type="checkbox" class="maxresptime_sel">最大响应时间<input type="text" class="sta maxresptime">';
		cont=cont+'<span>毫秒</span></td></tr></tbody></table></div>';
		$("#sb_PT").append('<hr>'+int+cont);
	});
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
	$('#endtest_head').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#endtest').offset().left; 
		var abs_y = event.pageY - $('#endtest').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#endtest'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>850)rel_left=850;
				var rel_top=event.pageY - abs_y;
				if(rel_top<0)rel_top=0;
				if(rel_top>740)rel_top=740;
				obj.css({'left':rel_left, 'top':rel_top}); 
			} 
		}).mouseup( function () { 
			isMove = false; 
		}); 
	});
	
	$('.newtp_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#newTP').offset().left; 
		var abs_y = event.pageY - $('#newTP').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#newTP'); 
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
	
	$('#SB_header').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#newSB').offset().left; 
		var abs_y = event.pageY - $('#newSB').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#newSB'); 
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
	else if(v_class=="tdats_pt"){
		var Testtask=$(e.target).parent();
		if(tag_task_select==null){
			//有项目被选中
			tag_task_select=Testtask;
			Testtask.css("background-color","#CAEFFA");
			//刷新rps曲线
			load_rps_chart(Testtask.attr("data-index"));
		}
		else{
			var child_selec_task=tag_task_select.children().eq(0).text();
			var	child_new_task=Testtask.children().eq(0).text();
			//项目被取消
			if(child_selec_task==child_new_task){
				tag_task_select=null;
				Testtask.css("background-color","#FFFFFF");
			}
			//选择了其他项目
			else{
				tag_task_select.css("background-color","#FFFFFF");
				tag_task_select=Testtask;
				Testtask.css("background-color","#CAEFFA");
				//刷新rrps曲线
				load_rps_chart(Testtask.attr("data-index"));
			}
		}
	}
});