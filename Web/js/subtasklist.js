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
function opentr(stname,result,st_time,tes,type){	
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
function show_endtest(ST_index,sttime,tes){
	$("#endtest_ST_index", parent.document).text(ST_index);
	$("#endtest_tes", parent.document).text(tes);
	$("#endtest_sttime", parent.document).text(sttime);

	$("#endtest", parent.document).css("display","block");
	$(".ra_butt_reject", parent.document).attr("checked",false);
	$(".ra_butt_pass", parent.document).attr("checked",false);
	$(".ra_butt_fail", parent.document).attr("checked",false);
	$("#endtest", parent.document).show();
}
$(document).ready(function(){ 
	hostpath_page=getHostUrl('hostpath_page');
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url="login.html";
		window.open(encodeURI(url),'_self');
	}
	var str=window.location.search;
	var proj=getvalue(str,"proj");
	var version=getvalue(str,"version");
	var url="TestTask/ListSubTask?proj="+proj+"&version="+version+"&HistSwitch=0";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var body = JSON.parse(xmlHttp.responseText);
			if(body.code==200){	
				var tasknum=parseInt(body.testproj_num);
				if(tasknum>0){
					var subtt=body.tp_list[0].SubTask;
					var tes=body.tp_list[0].tester;
					var st="";
					for(var k=0;k<subtt.length;k++){						
						var t1='<tr><td class="stdat" width=11%><a href="javascript:void(0)" onclick="opentr(\''+subtt[k].ST_index+'\',\''+subtt[k].TestStatus+'\',\''+subtt[k].Start_time+'\',\''+tes+'\',\'review\')">'+subtt[k].Proj_Subversion+'</a></td>';
						var t2='<td class="stdat" width=18%>'+subtt[k].Start_time+'</td>';
						var t3='<td class="stdat" width=18%>'+subtt[k].End_time+'</td>';
						var t4='<td class="stdat" width=14%>'+subtt[k].TestStatus+'</td>';
						var t5='<td class="stdat" width=14%>'+subtt[k].Test_Time+'</td>';
						var t6='<td class="stdat" width=11%>'+subtt[k].Test_Cycle+'</td>';
						var t7='<td class="stdat" width=17%></td></tr>';
						if(subtt[k].TestStatus=="running"){
							t7='<td class="stdat" width=17%><a href="javascript:void(0)" onclick="show_endtest(\''+subtt[k].ST_index+'\',\''+subtt[k].Start_time+'\',\''+tes+'\')">结束任务</a></td>';
							t1='<tr><td class="stdat" width=11%>'+subtt[k].Proj_Subversion+'</td>';
						}
						else if(subtt[k].TestStatus=="finish"){
							t7='<td class="stdat" width=17%><a href="javascript:void(0)" onclick="opentr(\''+subtt[k].ST_index+'\',\''+subtt[k].TestStatus+'\',\'\',\''+tes+'\',\'edit\')">修改</a>|<a href="javascript:void(0)" onclick="opentr(\''+subtt[k].ST_index+'\',\''+subtt[k].TestStatus+'\',\''+subtt[k].Start_time+'\',\''+tes+'\',\'check\')">审核</a></td>';
							t1='<tr><td class="stdat" width=11%>'+subtt[k].Proj_Subversion+'</td>';
						}
						st=st+t1+t2+t3+t4+t5+t6+t7;
					}
					$("tbody").append(st);
				}
				
			}
			else alert(body.message);
		}
	});
});