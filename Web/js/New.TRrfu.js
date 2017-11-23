var xmlHttp;
var hostpath_page;
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
// 批准测试报告
function approveTR(TR_index){
	var url="TestReport/Approve?user="+sessionStorage.customerId+"&TR_index="+TR_index;
	TMS_api(url,"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				alert("报告已批准，并发布给相关人员！");
				sessionStorage.currpage=hostpath_page+"TP_list.html";
				parent.location.reload();
			}
			else alert(resp.message);
		}
	});
}
// 驳回测试报告
function rejectTR(TR_index){
	var url="TestReport/Reject?user="+sessionStorage.customerId+"&TR_index="+TR_index;
	TMS_api(url,"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				alert("报告已驳回，并通知报告提交人！");
				sessionStorage.currpage=hostpath_page+"TP_list.html";
				parent.location.reload();
			}
			else alert(resp.message);
		}
	});
}
// 删除测试报告
function delTR(TR_index){
	var url="TestReport/Delete?user="+sessionStorage.customerId+"&TR_index="+TR_index;
	TMS_api(url,"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				alert("报告已删除！");
				sessionStorage.currpage=hostpath_page+"TP_list.html";
				parent.location.reload();
			}
			else alert(resp.message);
		}
	});
}

$(document).ready(function(){	
	hostpath_page=getHostUrl('hostpath_page');
	var str=decodeURI(window.location.search);
	var type=getvalue(str,"type");
	var ST_index=getvalue(str,"ST_index");
	var TRname=ST_index.substring(ST_index.indexOf("_")+1,ST_index.lastIndexOf("_"));
	var cycle=TRname.substring(TRname.lastIndexOf("_")+1);	
	TRname=TRname.substring(0,TRname.lastIndexOf("_"));	
	var proj=TRname.substring(0,TRname.lastIndexOf("_"));
	var sver=TRname.substring(TRname.lastIndexOf("_")+1);
	var TR_index="TR_"+TRname;
	var url="";
	var vers="";
	
	if(type=="new"){
		TR_index="";
		url="User/TitleList?title=TM";
		TMS_api(url,"GET","",function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					for(var i=0;i<resp.userlist.length;i++){
						$("#Responsor").append("<option>"+resp.userlist[i]+"</option>"); 
					}
					
					$("#Report_time").text(getDate());
					var curr_time=getDate();
					curr_time=curr_time.substring(0,curr_time.indexOf(" "));
					$("#Time_end").attr("value",curr_time);
					var curr_time2=getvalue(str,"st_time");
					$("#Time_start").attr("value",curr_time2);
					$("#Proj_Name").text(proj);
					$("#Proj_Subversion").text(sver);
					$("#Proj_Subversion_2").text(sver);
					$("#Cycles").text(cycle);
					$("#Reporter").text(sessionStorage.usrfullname);
					$("#Testengineer").val(decodeURI(getvalue(str,"tes")));
					$("#Timecost").text(getTime_diff(curr_time,curr_time2,""));
					url="TestTask/GetSubTask?ST_index="+ST_index;
					TMS_api(url,"GET","",function(){
						if (xmlHttp.readyState==4 && xmlHttp.status==200){
							resp = JSON.parse(xmlHttp.responseText);
							if(resp.code==200){
								vers=resp.Proj_Version;
							}
							else alert(resp.message);
						}
					});
				}
				else alert(resp.message);
			}
		});			
	}
	else{
		$("#Time_start").val("2017-01-17");
		$("#Time_end").val("2017-01-19");
		$("#Time_start").css("width","auto");
		$("#Time_end").css("width","auto");
		//如果报告状态为check和review，则需要将文本框边框和底色去掉，并置为不可改
		if(type!="edit"){
			$("#issues").after('<span id="issues" class="ltext"></span>');
			$("#Testconditions").after('<span id="Testconditions" class="ltext"></span>');
			var newbi='<table id="basicinfo" cellpadding=0 cellspacing=1 border=0><tbody><tr><th width=70>测试版本</th>';
			newbi=newbi+'<th width=70>测试轮数</th>';
			newbi=newbi+'<th width=75>测试负责人</th>';
			newbi=newbi+'<th width=250>测试工程师</th>';
			newbi=newbi+'<th width=400>测试周期</th></tr>';
			newbi=newbi+'<tr><td><span id="Proj_Subversion_2"></span></td>';
			newbi=newbi+'<td><span id="Cycles"></span></td>';
			newbi=newbi+'<td><span id="Responsor"></span></td>';
			newbi=newbi+'<td><span id="Testengineer"></span></td>';
			newbi=newbi+'<td><span id="Time_start"></span>至<span id="Time_end"></span>，共计<span id="Timecost"></span>天</td></tr></tbody></table>';
			$("#insertp").before(newbi);
			$(".del").remove();	
			$("input").css("background-color","#FFFFFF");
			$("input").attr("readonly",true);			
		}
		//如果报告状态为edit，需要增加按钮"删除"，并将按钮组向左移动
		else{
			$("#btnClose").before('<button href="javascript:void(0)" onclick="delTR(\''+TR_index+'\')">删除</button>&nbsp;');
		}
		//如果报告状态为check，需要删除按钮，并增加新按钮"驳回"和"通过"
		if(type=="check"){
			$("#btnClose").remove();
			$("#btnSubmit").remove();
			$("#TR_butt_group").append('<button href="javascript:void(0)" onclick="rejectTR(\''+TR_index+'\')">驳回</button>&nbsp;');
			$("#TR_butt_group").append('<button href="javascript:void(0)" onclick="approveTR(\''+TR_index+'\')">通过</button>');
		}
		//如果报告状态为review，则需要去掉"保存"按钮，并将按钮向右移动
		else if(type=="review"){
			$("#btnSubmit").remove();
		}
		//读取测试报告数据
		url="TestReport/Get?TR_index="+TR_index;
		TMS_api(url,"GET","",function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					$("#Proj_Name").text(resp.Proj_Name);
					vers=resp.Proj_Version;
					$("#Proj_Subversion").text(resp.Proj_Subversion);
					$("#Proj_Subversion_2").text(resp.Proj_Subversion);										
					$("#Report_time").text(resp.Report_time);
					$("#Reporter").text(resp.Reporter);
					$("#Timecost").text(resp.Timecost);
					$("#Cycles").text(resp.Cycles);
					
					var des=resp.issues;										
					var tcond=resp.Testconditions;	
					var TM=resp.Responsor;			
					if(type=="edit"){	
						des=des.replace(/\<br>/g,"\n");
						tcond=tcond.replace(/\<br>/g,"\n");					
						$("#Time_start").val(resp.Time_start);
						$("#Time_end").val(resp.Time_end);
						$("#Responsor").val(resp.Responsor);
						$("#Testengineer").val(resp.Testengineer);
					}
					else{
						des=des.replace(/\<br>/g,"\n<br/>");
						tcond=tcond.replace(/\<br>/g,"\n<br/>");
						$("#Time_start").text(resp.Time_start);
						$("#Time_end").text(resp.Time_end);
						$("#Responsor").text(resp.Responsor);
						$("#Testengineer").text(resp.Testengineer);												
					}
					$("#issues").html(des);
					$("#Testconditions").html(tcond);
					$("#Num_bug_total").val(resp.Num_bug_total);
					$("#Num_bug_unclose_2").val(resp.Num_bug_unclose);
					$("#Num_bug_ul3").val(resp.Num_bug_ul3);
					$("#Num_bug_reopen").val(resp.Num_bug_reopen);
					$("#Num_bug_close").val(resp.Num_bug_close);
					$("#Rate_bug_open").val(resp.Rate_bug_open);
					$("#Rate_bug_reopen").val(resp.Rate_bug_reopen);
					$("#Num_dqs").val(resp.Num_dqs);
					if(type=="edit"){
						url="User/TitleList?title=TM";
						TMS_api(url,"GET","",function(){
							if (xmlHttp.readyState==4 && xmlHttp.status==200){
								var resp = JSON.parse(xmlHttp.responseText);
								if(resp.code==200){
									for(var i=0;i<resp.userlist.length;i++){
										$("#Responsor").append("<option>"+resp.userlist[i]+"</option>"); 
									}
									$("#Responsor").val(TM);
								}
							}
						});
					}
				}
				else alert(resp.message);
			}
		});
	}
	//遗留Bug数和未关闭Bug数自动同步,触发open_rate和dqs变化
	$("#Num_bug_unclose").change(function b(){
		var Num_bug_unclose=$("#Num_bug_unclose").val();
		$("#Num_bug_unclose_2").val(Num_bug_unclose);
		
		var Num_bug_total=$("#Num_bug_total").val();
		if(Num_bug_unclose!="" && Num_bug_total!=""){
			var nbu=parseInt(Num_bug_unclose)*100;
			var sum=parseInt(Num_bug_total);
			if(sum==0)$("#Rate_bug_open").val("0");
			else $("#Rate_bug_open").val(parseInt(nbu/sum*100)/100);
			
			var Num_bug_ul3=$("#Num_bug_ul3").val();
			if(Num_bug_ul3!=""){
				var nb_ul3=parseInt(Num_bug_ul3)*100;
				if(sum==0)$("#Num_dqs").val("0");
				else $("#Num_dqs").val(parseInt((nbu+nb_ul3*2)/sum*100)/100);
			}
		}
	});
	$("#Num_bug_unclose_2").change(function b(){
		var Num_bug_unclose=$("#Num_bug_unclose_2").val();
		$("#Num_bug_unclose").val(Num_bug_unclose);
		
		var Num_bug_total=$("#Num_bug_total").val();
		if(Num_bug_unclose!="" && Num_bug_total!=""){
			var nbu=parseInt(Num_bug_unclose)*100;
			var sum=parseInt(Num_bug_total);
			if(sum==0)$("#Rate_bug_open").val("0");
			else $("#Rate_bug_open").val(parseInt(nbu/sum*100)/100);
			
			var Num_bug_ul3=$("#Num_bug_ul3").val();
			if(Num_bug_ul3!=""){
				var nb_ul3=parseInt(Num_bug_ul3)*100;
				if(sum==0)$("#Num_dqs").val("0");
				else $("#Num_dqs").val(parseInt((nbu+nb_ul3*2)/sum*100)/100);
			}
		}
	});
	
	//总Bug数改变，触发open_rate，reopen_rate和dqs变化
	$("#Num_bug_total").change(function b(){				
		var Num_bug_total=$("#Num_bug_total").val();
		if(Num_bug_total!=""){
			var sum=parseInt(Num_bug_total);
			
			var Num_bug_reopen=$("#Num_bug_reopen").val();
			if(Num_bug_reopen!=""){
				var nbr=parseInt(Num_bug_reopen)*100;
				if(sum==0)$("#Rate_bug_reopen").val("0");
				else $("#Rate_bug_reopen").val(parseInt(nbr/sum*100)/100);
			}
			
			var Num_bug_unclose=$("#Num_bug_unclose_2").val();
			if(Num_bug_unclose!=""){
				var nbu=parseInt(Num_bug_unclose)*100;
				if(sum==0)$("#Rate_bug_open").val("0");
				else $("#Rate_bug_open").val(parseInt(nbu/sum*100)/100);
			}
			
			var Num_bug_ul3=$("#Num_bug_ul3").val();
			if(Num_bug_ul3!="" && Num_bug_unclose!=""){
				var nb_ul3=parseInt(Num_bug_ul3)*100;
				var nbu=parseInt(Num_bug_unclose)*100;
				if(sum==0)$("#Num_dqs").val("0");
				else $("#Num_dqs").val(parseInt((nbu+nb_ul3*2)/sum*100)/100);
			}
		}
	});
	
	//reopen数改变，触发reopen_rate变化
	$("#Num_bug_reopen").change(function b(){
		var Num_bug_reopen=$("#Num_bug_reopen").val();		
		var Num_bug_total=$("#Num_bug_total").val();
		if(Num_bug_reopen!="" && Num_bug_total!=""){
			var nbr=parseInt(Num_bug_reopen)*100;
			var sum=parseInt(Num_bug_total);
			if(sum==0)$("#Rate_bug_reopen").val("0");
			else $("#Rate_bug_reopen").val(parseInt(nbr/sum*100)/100);
		}
	});
	
	//严重Bug数改变，触发dqs变化
	$("#Num_bug_ul3").change(function b(){
		var Num_bug_ul3=$("#Num_bug_ul3").val();		
		var Num_bug_total=$("#Num_bug_total").val();
		var Num_bug_unclose=$("#Num_bug_unclose_2").val();
		if(Num_bug_ul3!="" && Num_bug_total!="" && Num_bug_unclose!=""){
			var nb_ul3=parseInt(Num_bug_ul3)*100;
			var sum=parseInt(Num_bug_total);
			var nbu=parseInt(Num_bug_unclose)*100;
			if(sum==0)$("#Num_dqs").val("0");
			else $("#Num_dqs").val(parseInt((nbu+nb_ul3*2)/sum*100)/100);
		}
	});
	
	//测试日期被改动触发自动计算
	$("input").change(function b(e){
		if($(e.target).attr("type")=="date"){
			var newtime=$("#Time_end").val();
			var oldtime=$("#Time_start").val();
			$("#Timecost").text(getTime_diff(newtime,oldtime,""));
		}
	});
	//按钮 - 关闭报告
	$("#btnClose").click(function b(){	
		sessionStorage.currpage=hostpath_page+"TP_list.html";
		parent.location.reload();
	});
	
	//按钮 - 保存报告
	$("#btnSubmit").click(function(){
		var tr={};										
		var issues=$("#issues").val();
		issues=issues.replace(/\；/g,";");
		tr.issues=issues.replace(/\n/g,"<br>");
		tr.Time_start=$("#Time_start").val();
		tr.Time_end=$("#Time_end").val();
		tr.Cycles=$("#Cycles").text();
		var Testengineer=$("#Testengineer").val();
		tr.Testengineer=Testengineer.replace(/\；/g,";");
		var Testconditions=$("#Testconditions").val();
		Testconditions=Testconditions.replace(/\；/g,";");
		tr.Testconditions=Testconditions.replace(/\n/g,"<br>");			
		
		tr.Num_bug_unclose=$("#Num_bug_unclose_2").val();
		tr.Num_bug_ul3=$("#Num_bug_ul3").val();
		tr.Num_bug_total=$("#Num_bug_total").val();
		tr.Num_bug_reopen=$("#Num_bug_reopen").val();
		tr.Num_bug_close=$("#Num_bug_close").val();
		
		if(tr.issues=="")alert("请补充主要问题！");
		else if(tr.Time_start=="")alert("请选择测试开始时间！");
		else if(tr.Time_end=="")alert("请选择测试结束时间！");
		else if(tr.Cycles=="")alert("请补充测试回归轮数！");
		else if(tr.Testengineer=="")alert("请补充测试工程师字段！");
		else if(tr.Testconditions=="")alert("请补充本版本的测试环境和条件！");
		else if(tr.Num_bug_unclose=="")alert("请补充本版本的遗留Bug数！");
		else if(tr.Num_bug_ul3=="")alert("请补充本版本遗留的严重(LV>3)Bug数！");
		else if(tr.Num_bug_total=="")alert("请补充本版本的总Bug数！");
		else if(tr.Num_bug_reopen=="")alert("请补充本版本的Reopen Bug数！");
		else if(tr.Num_bug_close=="")alert("请补充本版本的关闭Bug数！");
		else{
			tr.Report_time=getDate();
			tr.Reporter=$("#Reporter").text();
			tr.Num_dqs=$("#Num_dqs").val();
			tr.Rate_bug_open=$("#Rate_bug_open").val();
			tr.Rate_bug_reopen=$("#Rate_bug_reopen").val();
			tr.Proj_Productline="";
			tr.TestResult="reject";
			tr.Num_tc_exe="0";
			tr.Num_tc_fail="0";
			tr.Rate_tc_fail="0";
			tr.Range="";
			tr.Timecost=$("#Timecost").text();
			tr.Responsor=$("#Responsor option:selected").val();
 			$("#btnSubmit").attr("disabled",true);	
			url="TestReport/Add?user="+sessionStorage.customerId;
			if(TR_index!=""){
				url="TestReport/Update?user="+sessionStorage.customerId+"&TR_index="+TR_index;				
			}
			else{
				tr.Proj_Name=proj;
				tr.Proj_Version=vers;
				tr.Proj_Subversion=sver;
			}
			var body = JSON.stringify(tr);
			TMS_api(encodeURI(url),"POST",body,function(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){						
						alert("保存成功");
						sessionStorage.currpage=hostpath_page+"TP_list.html";
						parent.location.reload();
					}
					else alert(resp.message);
					$("#btnSubmit").attr("disabled",false);	
				}
			});
		}
	});
});