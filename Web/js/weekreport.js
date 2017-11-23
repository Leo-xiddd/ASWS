var xmlHttp;
var dept2;
var wr_complete;
var role;
var wr_name;
var rows;
var wrlist_page_num;
var wrlist_page_sum;

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
function getWeekNumber(d) { 
	d = new Date(d); 
	d.setHours(0,0,0); 
	d.setDate(d.getDate() + 4 - (d.getDay()||7)); 
	var yearStart = new Date(d.getFullYear(),0,1); 
	var weekNo = Math.ceil(( ( (d - yearStart) / 86400000) + 1)/7) 
	var getWeekNo = weekNo; 
	return getWeekNo;
}
function wr_review(fname,sta,opt){	
	//屏蔽按钮
	$("#copywr").hide();
	$("#additem").hide();
	$("#submit_wr").hide();	
	$("#delitem").hide();
	$("#author_2").attr("readonly",true);
	var url="WeekReport/Get?user=&reportname="+fname;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var wrs=resp.dept_work;
				var line='';
				if(wrs[0].dept2=="")alert("本周报还未填充内容，请先编辑内容!");
				else{
					for(var i=0;i<wrs.length;i++){
						var dept_id="dept2_"+(i+1);
						var wr_items=wrs[i].wokitems;
						var csp=wr_items.length;
						if(csp==0){
							line=line+'<tr><td id="'+dept_id+'" align="center" style="border-left:1px solid #000000;">'+wrs[i].dept2+'</td>';
							line=line+'<td class="itemno1" align="center" valign="top" style="border-right:0px;"><span></span></td>';						
							line=line+'<td><div class="items1"></div></td>';	
							line=line+'<td><div class="items"></div></td>';
							line=line+'<td><div class="issue"></div></td></tr>';
						}
						else{
							line=line+'<tr><td rowspan='+csp+' align="center" style="border-left:1px solid #000000;">'+wrs[i].dept2+'</td>';	
							line=line+'<td class="itemno1" align="center" valign="top" style="border-right:0px;">'+wr_items[0].workitem_no+'</td>';
							line=line+'<td valign="top"><div class="items1">'+wr_items[0].workitem+'</div></td>';	
							line=line+'<td valign="top"><div class="items">'+wr_items[0].content+'</div></td>';
							var issu=wrs[i].wokitems[0].issue;
							var temp_line="";
							for(var j=1;j<csp;j++){
								temp_line=temp_line+'<tr><td class="itemno1" align="center" valign="top" style="border-right:0px;">'+wr_items[j].workitem_no+'</td>';
								temp_line=temp_line+'<td valign="top"><div class="items1">'+wr_items[j].workitem+'</div></td>';
								temp_line=temp_line+'<td valign="top"><div class="items">'+wr_items[j].content+'</div></td></tr>';
								if(wr_items[j].issue!="")issu=issu+"<br>"+wr_items[j].issue;
							}
							line=line+'<td rowspan='+csp+' valign="top"><div class="issue">'+issu+'</div></td></tr>';
							line=line+temp_line;							
						}																	
					}
					line='<tbody>'+line+'</tbody>';
					$("#wr_items tbody").remove();
					$("#wr_items table").append(line);
					$("#author_2").val(resp.author);
					$("#author_2").attr("size",$("#author_2").val().length*2);
					wr_complete=resp.complete;
					wr_name=fname;
					$("#page_title").text(fname);
					if(role=="dept_admin" && sta!="已发布"){
						$("#release_wr").show();
						$("#edit_wr").show();
					}
					else {
						$("#release_wr").hide();
						$("#edit_wr").hide();
					}
					if(opt==1){
						$("#newWR").toggle();
						$("#wr_list").toggle();	
						$("#addteamWR").hide();
					}
				}								
			}
			else alert(resp.message);
		}
	});
}
//用于管理员调整页面
function wr_modify(fname){	
	//屏蔽按钮
	$("#copywr").hide();
	$("#additem").hide();		
	$("#delitem").hide();
	$("#submit_wr").hide();
	$("#edit_wr").hide();
	$("#release_wr").hide();
	$("#save_wr").show();
	$("#canceledit_wr").show();
	$("#author_2").attr("readonly",true);
	var url="WeekReport/Get?user=&reportname="+fname;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var wrs=resp.dept_work;
				var line='';
				if(wrs[0].dept2=="")alert("本周报还未填充内容，请先编辑内容!");
				else{
					for(var i=0;i<wrs.length;i++){
						var dept_id="dept2_"+(i+1);
						var wr_items=wrs[i].wokitems;
						var csp=wr_items.length;
						var wki=wr_items[0].workitem;
						wki=wki.replace(/\<br>/g,"\n");
						var wkc=wr_items[0].content;
						wkc=wkc.replace(/\<br>/g,"\n");
						if(csp==0){
							line=line+'<tr><td id="'+dept_id+'" align="center" style="border-left:1px solid #000000;">'+wrs[i].dept2+'</td>';
							line=line+'<td class="itemno1" align="center" valign="top" style="border-right:0px;"><span></span></td>';						
							line=line+'<td><div class="items1"></div></td>';	
							line=line+'<td><div class="items"></div></td>';
							line=line+'<td><div class="issue"></div></td></tr>';
						}
						else{
							line=line+'<tr><td rowspan='+csp+' align="center" style="border-left:1px solid #000000;">'+wrs[i].dept2+'</td>';	
							line=line+'<td class="itemno1" align="center" valign="top" style="border-right:0px;">'+wr_items[0].workitem_no+'</td>';
							line=line+'<td valign="top"><textarea class="items1">'+wki+'</textarea></td>';	
							line=line+'<td valign="top"><textarea class="items">'+wkc+'</textarea></td>';
							var issu=wrs[i].wokitems[0].issue;
							var temp_line="";
							for(var j=1;j<csp;j++){
								wki=wr_items[j].workitem;
								wki=wki.replace(/\<br>/g,"\n");
								wkc=wr_items[j].content;
								wkc=wkc.replace(/\<br>/g,"\n");
								temp_line=temp_line+'<tr><td class="itemno1" align="center" valign="top" style="border-right:0px;">'+wr_items[j].workitem_no+'</td>';
								temp_line=temp_line+'<td valign="top"><textarea class="items1">'+wki+'</textarea></td>';
								temp_line=temp_line+'<td valign="top"><textarea class="items">'+wkc+'</textarea></td></tr>';
								if(wr_items[j].issue!="")issu=issu+"<br>"+wr_items[j].issue;
							}
							issu=issu.replace(/\<br>/g,"\n");
							line=line+'<td rowspan='+csp+' valign="top"><textarea class="issue" style="height:100%;">'+issu+'</textarea></td></tr>';
							line=line+temp_line;							
						}																	
					}
					line='<tbody>'+line+'</tbody>';
					$("#wr_items tbody").remove();
					$("#wr_items table").append(line);
					$("#author_2").val(resp.author);
					$("#author_2").attr("size",$("#author_2").val().length*2);
					wr_complete=resp.complete;
					wr_name=fname;
					$("#addteamWR").hide();
				}								
			}
			else alert(resp.message);
		}
	});
}
function wr_edit(fname,sta,usrname){
	if(sta=="已发布")alert("本周报已发布，不能再修改");
	else{	
		$("#copywr").show();
		$("#additem").show();		
		$("#delitem").show();			
		$("#submit_wr").show();
		$("#release_wr").hide();						
		$("#edit_wr").hide();
		var url="WeekReport/Get?user="+sessionStorage.customerId+"&reportname="+fname;
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){	
					var wrs=resp.dept_work;
					var line='';
					if(wrs[0].dept2==""){
						line=line+'<tr id="tr1"><td id="col_dept2" align="center" style="border-left:1px solid #000000;">'+dept2+'</td>';	
						line=line+'<td id="itemno1" class="itemno" align="right" valign="top" style="border-right:0px;"><span>1.</span></td>';
						line=line+'<td id="item1" valign="top"><textarea class="items1"></textarea></td>';
						line=line+'<td id="cont1" valign="top"><textarea class="items"></textarea></td>';
						line=line+'<td id="col_issue" valign="top"><textarea class="issue" style="height:100%;"></textarea></td></tr>';
						rows=1;
					}
					else{
						var wr_items=wrs[0].wokitems;
						rows=wr_items.length;
						var wki=wr_items[0].workitem;
						wki=wki.replace(/\<br>/g,"\n");
						var wkc=wr_items[0].content;
						wkc=wkc.replace(/\<br>/g,"\n");
						var issu=wr_items[0].issue;					
						line=line+'<tr id="tr1"><td id="col_dept2" align="center" rowspan='+rows+' style="border-left:1px solid #000000;">'+wrs[0].dept2+'</td>';
						line=line+'<td id="itemno1" class="itemno" align="right" valign="top" style="border-right:0px;"><span>1.</span></td>';
						line=line+'<td id="item1" valign="top"><textarea class="items1">'+wki+'</textarea></td>';
						line=line+'<td id="cont1" valign="top"><textarea class="items">'+wkc+'</textarea></td>';						
						var temp_line="";
						for(var j=1;j<wr_items.length;j++){
							var item_no='<span>'+(j+1)+'.</span>';
							wki=wr_items[j].workitem;
							wki=wki.replace(/\<br>/g,"\n");
							wkc=wr_items[j].content;
							wkc=wkc.replace(/\<br>/g,"\n");
							
							var item='<textarea class="items1">'+wki+'</textarea>';
							var cont='<textarea class="items">'+wkc+'</textarea>';
							temp_line=temp_line+'<tr id="tr'+(j+1)+'"><td id="itemno'+(j+1)+'" class="itemno" align="right" valign="top" style="border-right:0px;">'+item_no+'</td><td id="item'+(j+1)+'" valign="top">'+item+'</td><td id="cont'+(j+1)+'" valign="top">'+cont+'</td></tr>';
							if(wr_items[j].issue!="")issu=issu+"<br>"+wr_items[j].issue;
						}	
						issu=issu.replace(/\<br>/g,"\n");
						line=line+'<td id="col_issue" rowspan='+rows+' valign="top"><textarea class="issue" style="height:100%;">'+issu+'</textarea></td></tr>';
						line=line+temp_line;
					}
					line='<tbody>'+line+'</tbody>';
					$("#wr_items tbody").remove();
					$("#wr_items table").append(line);
					$("#author_2").val(resp.author);
					$("#author_2").attr("size",$("#author_2").val().length*2);
					wr_complete=resp.complete;
					wr_name=fname;
					$("#page_title").text(fname);
					if(role=="dept_admin")$("#author_2").attr("readonly",false);
					else $("#author_2").attr("readonly",true);
					$("#addteamWR").hide();
					$("#newWR").toggle();
					$("#wr_list").toggle();						
				}
				else alert(resp.message);
			}
		});
	}
}
function wr_del(fname){
	if(confirm("确定要删除本周报吗？")){
		var url="WeekReport/Delete?user="+sessionStorage.customerId+"&reportname="+fname;
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){	
					alert("周报已删除。");	
					location.reload();
				}
				else alert(resp.message);
			}
		});
	}
}
//获取周报列表
function reload_wrlist(page_num){	
	var url="WeekReport/List?user="+sessionStorage.customerId+"&filter=&type=dept&page_count=20&page_num="+page_num;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var wrlist=resp.report_list;
				var bcolor=' class="list_bgcolor1"';
				wrlist_page_sum=Math.ceil(resp.wr_num/20);
				wrlist_page_num=page_num;
				$("#page_num").text(wrlist_page_sum);
				$("#curr_page").text(page_num);
				$("#wr_list tbody tr").remove();
				for(var i=0;i<wrlist.length;i++){
					if(bcolor=='')bcolor=' class="wrlist_bgcolor1"';
					else bcolor='';
					var line='<tr><td'+bcolor+'>'+wrlist[i].fname+'</td><td'+bcolor+'>'+wrlist[i].week_date+'</td>';
					line=line+'<td'+bcolor+'>'+wrlist[i].creattime+'</td><td'+bcolor+'>'+wrlist[i].status+'</td>';
					line=line+'<td'+bcolor+'><nav><a class="edittp" href="javascript:void(0)" onclick="wr_review(\''+wrlist[i].fname+'\',\''+wrlist[i].status+'\',1)">查看</a> |';
					line=line+'<a class="edittp" href="javascript:void(0)" onclick="wr_edit(\''+wrlist[i].fname+'\',\''+wrlist[i].status+'\')">修改</a> |';
					line=line+'<a class="edittp" href="javascript:void(0)" onclick="wr_del(\''+wrlist[i].fname+'\')">删除</a></nav></td></tr>';
					$("#wr_list tbody").append(line);
				}
			}
			else alert(resp.message);
		}
	});
}
// 创建个人周报
function creat_per_wr(){

}
function Topage(num){
	if(wrlist_page_num!=num){
		if(num==0)wrlist_page_num=wrlist_page_sum;
		else wrlist_page_num=num;
		reload_wrlist(wrlist_page_num);	
	}
}
function Nextpage(tag){
	if(tag=="+" && wrlist_page_num!=wrlist_page_sum){
		wrlist_page_num=wrlist_page_num+1;
		reload_wrlist(wrlist_page_num);
	}
	else if(tag=="-" && wrlist_page_num!=1){
		wrlist_page_num=wrlist_page_num-1;
		reload_wrlist(wrlist_page_num);
	}		
}
$(document).ready(function(){ 
	var hostpath_page=getHostUrl('hostpath_page');
	var item_id=1;
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url=hostpath_page+"login.html";
		window.open(encodeURI(url),'_self');
	}
	//页面初始化
	var dept="";
	role="";
	dept2="";
	wr_complete="";
	wr_name="";
	rows=0;
	var curtime=getDate();
	var curyear=curtime.substring(0,4);
	var curweek=getWeekNumber(curtime);
	var curtime_CHN=curtime.substr(0,curtime.indexOf(" "));
	curtime_CHN=curtime_CHN.replace("-","年");
	curtime_CHN=curtime_CHN.replace("-","月")+"日";

	$("#loginuser").text(sessionStorage.usrfullname);
	$("#currtime").text(curtime_CHN);
	$("#weeks").text(curweek);
	var url="User/Getinfo?user="+sessionStorage.customerId;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				dept=resp.dept1;
				role=resp.role;
				dept2=resp.dept2;
				if(role.indexOf("admin")==-1)$("#addteamWR").hide();
				reload_wrlist(1);
			}
			else alert(resp.message);
		}
	});
	//从周报页返回列表
	$("#exit_wr").click(function b(){
		wr_complete="";	
		$("#page_title").text("历史周报");		
		$("#newWR").toggle();
		$("#wr_list").toggle();	
		$("#addteamWR").show();
		$("#edit_wr").hide();
		location.reload();
	});
	
	//周报页 - 复制上期周报
	$("#copywr").click(function b(){
		var fname=wr_name;
		var weeks=fname.substring(fname.indexOf("第")+1,fname.indexOf("周"));
		var weeknum=parseInt(weeks)-1;
		var years=fname.substring(fname.indexOf("-")+1,fname.indexOf("-")+5);
		var yearnum=parseInt(years);		
		if(weeknum==0){
			yearnum--;
			var d=yearnum+"-12-31";
			weeknum=getWeekNumber(d);			
		}
		fname=fname.substring(0,fname.indexOf("-"))+"-"+yearnum+"第"+weeknum+"周周报";
		url="WeekReport/Get?user="+sessionStorage.customerId+"&reportname="+fname;
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){	
					var wrs=resp.dept_work;
					var line='';
					if(wrs[0].dept2=="")alert("没有在上期周报找到您提交的信息");
					else{
						var wr_items=wrs[0].wokitems;
						rows=wr_items.length;
						var wki=wr_items[0].workitem;
						wki=wki.replace(/\<br>/g,"\n");
						var wkc=wr_items[0].content;
						wkc=wkc.replace(/\<br>/g,"\n");
						var issu=wr_items[0].issue;
						issu=issu.replace(/\<br>/g,"\n");
						line=line+'<tr id="tr1"><td id="col_dept2" align="center" rowspan='+rows+' style="border-left:1px solid #000000;">'+wrs[0].dept2+'</td>';
						line=line+'<td id="itemno1" class="itemno" align="right" valign="top" style="border-right:0px;"><span>1.</span></td>';
						line=line+'<td id="item1" valign="top"><textarea class="items1">'+wki+'</textarea></td>';
						line=line+'<td id="cont1" valign="top"><textarea class="items">'+wkc+'</textarea></td>';
						line=line+'<td id="col_issue" rowspan='+rows+'><textarea class="issue" style="height:100%;">'+issu+'</textarea></td></tr>';
						
						for(var j=1;j<wr_items.length;j++){
							var item_no='<span>'+(j+1)+'.</span>'
							wki=wr_items[j].workitem;
							wki=wki.replace(/\<br>/g,"\n");
							wkc=wr_items[j].content;
							wkc=wkc.replace(/\<br>/g,"\n");
							var item='<textarea class="items1">'+wki+'</textarea>';
							var cont='<textarea class="items">'+wkc+'</textarea>';
							line=line+'<tr id="tr'+(j+1)+'"><td id="itemno'+(j+1)+'" class="itemno" align="right" valign="top" style="border-right:0px;">'+item_no+'</td><td id="item'+(j+1)+'" valign="top">'+item+'</td><td id="cont'+(j+1)+'" valign="top">'+cont+'</td></tr>';
						}	
						line='<tbody>'+line+'</tbody>';
						$("#wr_items tbody").remove();
						$("#wr_items table").append(line);
					}					
				}
				else alert(resp.message);
			}
		});				
	});
	
	//周报页 - 添加项目,每个人只能编辑自己的内容
	$("#additem").click(function b(){
		rows++;
		$("#col_dept2").attr("rowspan",rows);
		$("#col_issue").attr("rowspan",rows);
		var item_no='<span>'+rows+'.</span>'
		var item='<textarea class="items1"></textarea>';
		var cont='<textarea class="items"></textarea>';
		var line='<tr id="tr'+rows+'"><td id="itemno'+rows+'" class="itemno" align="right" valign="top" style="border-right:0px;">'+item_no+'</td><td id="item'+rows+'" valign="top">'+item+'</td><td id="cont'+rows+'" valign="top">'+cont+'</td></tr>';
		$("#wr_items tbody").append(line);
	});
	
	//周报页 - 删减项目
	$("#delitem").click(function b(){
		if(rows>1){
			$("#tr"+rows).remove();
			rows--;
			$("#col_dept2").attr("rowspan",rows);
			$("#col_issue").attr("rowspan",rows);
		}
		else alert("已经没有能删的项目了。");	
	});
	
	//周报页 - 编辑周报（只有管理员有权限）
	$("#edit_wr").click(function b(){
		wr_modify(wr_name);
	});
	
	//周报页 - 发布报告
	$("#release_wr").click(function b(){
		var authors=$("#author_2").val();
		var aut=wr_complete.split(",");
		authors=authors+",";
		for(var i=0;i<aut.length;i++){
			aut[i]=aut[i]+",";
			authors=authors.replace(aut[i],"");
		}		
		if(authors!="," && authors!=""){
			authors=authors.substring(0,authors.length-1);
			alert("周报还未完成，还需'"+authors+"'补充工作内容");
		}
		else{
			url="WeekReport/Release?user="+sessionStorage.customerId+"&reportname="+wr_name;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						alert("周报已通过邮件发布，请查收邮件确认！");
						wr_complete="";	
						$("#page_title").text("历史周报");							
						$("#newWR").toggle();
						$("#wr_list").toggle();	
						$("#addteamWR").show();
						$("#edit_wr").hide();
						location.reload();
					}
					else alert(resp.message);
				}
			});
		}
	});
	
	//周报页 - 提交周报
	$("#submit_wr").click(function b(){
		var tag=0;
		var wrdata={};
		var authorlist=$("#author_2").val();
		wrdata.authorlist=authorlist.replace(/\，/g,",");
		wrdata.dept2=$("#col_dept2").text();
		wrdata.author=sessionStorage.usrfullname;
		wrdata.workitems=[];	
		for(var i=1;i<=rows;i++){
			var wi={};
			wi.workitem_no=$("#itemno"+i+" span").text();
			var workitem=$("#item"+i+" textarea").val();
			var content=$("#cont"+i+" textarea").val();
			var issue="";
			if(i==1)issue=$("#col_issue textarea").val();
		/*	if(workitem==""){
				alert("请补充第"+i+"个项目的内容");
				$("#item"+i+" textarea").focus();
				tag=1;
				break;
			}
			else if(content==""){
				alert("请补充第"+i+"个项目的进展");
				$("#cont"+i+" textarea").focus();
				tag=1;
				break;
			}*/
			wi.workitem=workitem.replace(/\n/g,"<br>");
			wi.content=content.replace(/\n/g,"<br>");
			wi.issue=issue.replace(/\n/g,"<br>");
			wrdata.workitems.push(wi);
		}
		var body=JSON.stringify(wrdata);
		
		if(tag==0){
			url="WeekReport/Update?user="+sessionStorage.customerId+"&reportname="+wr_name;		
			TMS_api(url,"POST",body,function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						alert("周报已成功提交！");
						wr_complete="";		
						$("#page_title").text("历史周报");	
						$("#newWR").toggle();
						$("#wr_list").toggle();	
						$("#addteamWR").show();
						$("#edit_wr").hide();
						location.reload();
					}
					else alert(resp.message);
				}
			});
		}	
	});
	
	//周报页 - 保存管理员修改的周报
	$("#save_wr").click(function b(){
		var wrdat={wrs:[]};		
		var TR_num=$("#wr_items tbody").children().length;
		var td_num=0;
		var tag=0;
		var dept2="";
		for(var i=0;i<TR_num;i++){
			var wi={};
			var tabconts=$("#wr_items tbody").children().eq(i);
			td_num=tabconts.children().length;			
			var workitem_no="";
			var workitem="";
			var content="";
			var issue="";
			if(td_num==5){
				if(tag==0)tag=1;
				dept2=tabconts.children().eq(0).text();
				workitem_no=tabconts.children().eq(1).text();
				workitem=tabconts.children().eq(2).children().eq(0).val();
				content=tabconts.children().eq(3).children().eq(0).val();
				issue=tabconts.children().eq(4).children().eq(0).val();
				issue=issue.replace(/\n/g,"<br>");
			}
			else{
				workitem_no=tabconts.children().eq(0).text();
				workitem=tabconts.children().eq(1).children().eq(0).val();
				content=tabconts.children().eq(2).children().eq(0).val();
			}
			wi.workitem=workitem.replace(/\n/g,"<br>");
			wi.content=content.replace(/\n/g,"<br>");	
			wi.dept2=dept2;
			wi.workitem_no=workitem_no;
			wi.issue=issue;
			wrdat.wrs.push(wi);
		}
		var body=JSON.stringify(wrdat);
		url="WeekReport/Save?user="+sessionStorage.customerId+"&reportname="+wr_name;		
		TMS_api(url,"POST",body,function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){								
					$("#save_wr").hide();
					$("#canceledit_wr").hide();
					wr_review(wr_name,"",0);
				}
				else alert(resp.message);
			}
		});	
	});
	//取消管理员修改周报
	$("#canceledit_wr").click(function b(){	
		$("#save_wr").hide();
		$("#canceledit_wr").hide();
		wr_review(wr_name,"",0);
	});
	
	//创建个人周报
	$("#addWR").click(function b(e){
		creat_per_wr();		
	});
	
	//创建团队周报
	$("#addteamWR").click(function b(e){
		$("#newWR_panel").css("display","block");	
		$("#owner").val(dept);	
		$("#currweek").val(curyear+"-W"+curweek);
		$("#author").val(sessionStorage.usrfullname);
		showOverlay('.overlay');		
		$("#newWR_panel").show();	
	});
	
	//弹层按钮 - 创建新周报
	$("#wr_creat").click(function b(){	
		var wr={};
		var author=$("#author").val();
		wr.owner=$("#owner").val();
		if(author==""){
			alert("请补充填写周报的人员信息！");
			$("#author").focus();
		}
		else if(wr.owner==""){
			alert("请补充周报所属部门");
			$("#owner").focus();
		}
		else{			
			author=author.replace(/\n/g,"");
			wr.author=author.replace(/\，/g,",");
			var curweek_new=$("#currweek").val();
			wr.week=curweek_new.substring(6,curweek_new.length);
			wr.type="dept";
			wr.year=curyear;
			wr.creat_time=curtime;
			body=JSON.stringify(wr);
			url="WeekReport/Add?user="+sessionStorage.customerId;
			TMS_api(url,"POST",body,function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						alert("新周报已创建，并已通知相关人员尽快填写。");	
						$("#owner").val("");
						$("#author").val("");
						$("#overlay").hide();
						$("#newWR_panel").hide();
						location.reload();
					}
					else alert(resp.message);
				}
			});
		}		
	});
	
	//弹层按钮 - 取消
	$("#wr_cancle_creat").click(function b(){	
		$("#owner").val("");
		$("#overlay").hide();
		$("#newWR_panel").hide();
	});
	
	//弹层移动
	$('.popw_head').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#newWR_panel').offset().left; 
		var abs_y = event.pageY - $('#newWR_panel').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#newWR_panel'); 
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