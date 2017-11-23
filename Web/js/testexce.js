var xmlHttp;
var Dlog_selected;
var member_selected;
var task_selected;
var subtask_selected;
var Dlog_id;
var row_selected;
var page_num;
var page_sum;
var tpitem_ppnum;
var cur_year;
var cur_date;
var opt;
var tag_attend_edit;
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
// 加载个人出勤热力图
function load_attendance_person(curyear,tester) {
	var range=[curyear+'-01-01',curyear+'-12-31'];
	var pieces=[
		{value: 1, label: '请假', color: '#F9A9E8'},
		{value: 2, label: '上午到岗', color: '#7EAF64'},
		{value: 3, label: '下午到岗', color: '#3A7947'},
		{value: 4, label: '全天到岗', color: '#AADFF8'}
	];
	var categories=['请假','上午到岗','下午到岗','全天到岗'];

	var url="TestExecManage/GetAttendanceData?st_date="+range[0]+"&end_date="+range[1]+"&member_name="+tester;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var dats=[];
				for(var i=0;i<resp.attendance.length;i++){
					var item=[];
					item.push(resp.attendance[i].datetime);
					item.push(resp.attendance[i].value);
					dats.push(item);
				}
				HeatMap_chart_pieces("Chart_attendance_detail","",dats,pieces,categories,range);
			}
			else alert(resp.message);
		}
	});	
}
// 加载人员需求变化图
function load_ManPoReq() {
	// 初始化图表数据结构
	var dats=[
		{
			seriers:"人员需求",
			color_line:'#B85F00',
			color_area:'#FDE363',
			dat:''
		},
		{
			seriers:"实际到岗",
			color_line:'#861883',
			color_area:'#E35FDD',
			dat:''
		}
	];
	var url="TestExecManage/GetManPoReq";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var dat1='';
				var dat2='';
				var dats2=[];
				for(var i=0;i<resp.ManPoReq.length;i++){
					var item=[];
					item.push(resp.ManPoReq[i].datetime);
					item.push(resp.ManPoReq[i].req);
					dats2.push(item);
					dat1=dat1+resp.ManPoReq[i].req+',';
					dat2=dat2+resp.ManPoReq[i].attend+',';
				}
				dats[0].dat=dat1.substr(0,dat1.length-1);
				dats[1].dat=dat2.substr(0,dat2.length-1);
				Stack_chart("chart_ManPowerReq","",resp.times,dats);

				// 同步人员需求弹层图表
				var range=[cur_year+'-01-01',cur_year+'-12-31'];
				var color=['#F9E9B4','#CB3D10'];
				HeatMap_chart("Chart_ManPoReq_detail","",dats2,0,resp.maxreq,range,color);
				tms_receive_tag="ok";
			}
			else alert(resp.message);
		}
	});	
}
// 加载工作日志
function load_Dlogs(filter){
	//清除当前所有日志
	$("#tab_Dloglist").remove();
	Dlog_selected=null;
	var para="filter="+filter+"&page_count="+tpitem_ppnum+"&page_num="+page_num;
	var url=encodeURI("TestExecManage/ListDlogs?"+para);
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#tb_Dloglist").append('<tbody id="tab_Dloglist"></tbody>');	
				for(var i=0;i<resp.Dlog_list.length;i++){
					//添加项目记录				
					var tr0='<tr class="tdats" id="'+resp.Dlog_list[i].id+'">';				
					var td1='<td class="cell">'+resp.Dlog_list[i].date+'</td>';
					var td2='<td class="cell">'+resp.Dlog_list[i].name+'</td>';
					var td3='<td class="cell">'+resp.Dlog_list[i].worktype+'</td>';
					var td4='<td class="cell">'+resp.Dlog_list[i].attendtime+'</td>';
					var td5='<td class="cell">'+resp.Dlog_list[i].testproj+'</td>';
					var td6='<td class="cell">'+resp.Dlog_list[i].projversion+'</td>';
					var td7='<td class="cell">'+resp.Dlog_list[i].tcexe +'</td>';
					var td8='<td class="cell">'+resp.Dlog_list[i].newbug+'</td>';
					var td9='<td class="cell">'+resp.Dlog_list[i].regbug+'</td>';
					var td10='<td class="cell">'+resp.Dlog_list[i].content+'</td>';				
					var tr1="</tr>";
					var record=tr0+td1+td2+td3+td4+td5+td6+td7+td8+td9+td10+tr1;
					$("#tab_Dloglist").append(record);					
				}
				var Dlogs_sum=parseInt(resp.total_num);
				page_sum=Math.ceil(Dlogs_sum/tpitem_ppnum);
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
				tms_receive_tag="ok";				
			}
			else alert(resp.message);
		}		
	});
}
// 页面初始化
function init_page() {
	var range=[cur_year+'-01-01',cur_year+'-12-31'];
	var url="TestExecManage/GetAttendanceData?st_date="+range[0]+"&end_date="+range[1]+"&member_name=";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				var dats=[];
				for(var i=0;i<resp.attendance.length;i++){
					var item=[];
					item.push(resp.attendance[i].datetime);
					item.push(resp.attendance[i].value);
					dats.push(item);
				}
				HeatMap_chart("Chart_attendance","",dats,0,resp.maxvalue,range,"");

				// 初始化考勤图表数据结构
				var dats=[
					{
						seriers:"执行效率",
						type:'bar',
						color1:'#1962E1',
						color2:'#11449C',
						markPoint:'',
						markLine:'',
						dat:''
					}
				];
				var url="TestExecManage/GetTCexeRate";
				TMS_api(url,"GET","",function a(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){	
							var dat1='';
							for(var i=0;i<resp.TCexeRate.length;i++) dat1=dat1+resp.TCexeRate[i]+',';
							dats[0].dat=dat1.substr(0,dat1.length-1);

							BarLine_chart("chart_TCexeRate","",resp.times,dats);
							
							// 初始化人员需求图表数据结构
							var dats_manpo=[
								{
									seriers:"人员需求",
									color_line:'#B85F00',
									color_area:'#FDE363',
									dat:''
								},
								{
									seriers:"实际到岗",
									color_line:'#861883',
									color_area:'#E35FDD',
									dat:''
								}
							];
							var url="TestExecManage/GetManPoReq";
							TMS_api(url,"GET","",function a(){
								if (xmlHttp.readyState==4 && xmlHttp.status==200){
									var resp = JSON.parse(xmlHttp.responseText);
									if(resp.code==200){	
										var dat1='';
										var dat2='';
										var dats2=[];
										for(var i=0;i<resp.ManPoReq.length;i++){
											var item=[];
											item.push(resp.ManPoReq[i].datetime);
											item.push(resp.ManPoReq[i].req);
											dats2.push(item);
											dat1=dat1+resp.ManPoReq[i].req+',';
											dat2=dat2+resp.ManPoReq[i].attend+',';
										}
										dats_manpo[0].dat=dat1.substr(0,dat1.length-1);
										dats_manpo[1].dat=dat2.substr(0,dat2.length-1);

										Stack_chart("chart_ManPowerReq","",resp.times,dats_manpo);

										// 同步人员需求弹层图表
										var range=[cur_year+'-01-01',cur_year+'-12-31'];
										var color=['#F9E9B4','#CB3D10'];
										HeatMap_chart("Chart_ManPoReq_detail","",dats2,0,resp.maxreq,range,color);
										load_Dlogs("");						
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
}
// 初始化考勤详情弹层
function init_form_AttendDetail(){
	// 在职实习生列表
	var url="TestExecManage/ListContractors?filter=";
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				// 清除年份列表和人员列表内容，从新初始化
				$("#attend_year_list").empty();
				$("#tester_list").empty();
				for(var i=0;i<resp.years.length;i++)$("#attend_year_list").append('<option>'+resp.years[i]+'</option>');
				for(var i=0;i<resp.contractors.length;i++)$("#tester_list").append('<option>'+resp.contractors[i].name+'</option>');
				$("#form_AttendDetail_year").text(resp.years[0]);
				// 根据默认实习生查询考勤记录并生成图表
				if(null!=resp.contractors[0] && resp.contractors[0]!="")load_attendance_person(cur_year,resp.contractors[0].name);
			}
			else alert(resp.message);
		}
	});	
}
// 初始化考勤数据编辑弹层（二级）
function init_form_AttendDetail_edit() {
	$("#AttendDetail_edit_date").val(cur_date);
	$("#AttendDetail_edit_tester").val($("#tester_list").val());
}
// 初始化实习生管理弹层
function init_form_Attendee_manage() {
	$("#tab_Attendee_manage").remove();
	var filter="status='在职'";
	member_selected=null;
	var url="TestExecManage/ListContractors?filter="+filter;
	TMS_api(url,"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				$("#tb_Attendee_manage").append('<tbody id="tab_Attendee_manage"></tbody>');	
				for(var i=0;i<resp.contractors.length;i++){
					//添加项目记录				
					var tr0='<tr class="tdats" id="Contra_'+resp.contractors[i].id+'">';				
					var td1='<td class="leftcol">'+resp.contractors[i].name+'</td>';
					var td2='<td>'+resp.contractors[i].status+'</td>';
					var td3='<td>'+resp.contractors[i].sign_num+'</td>';
					var td4='<td>'+resp.contractors[i].entry_date+'</td>';
					var td5='<td>'+resp.contractors[i].quit_date+'</td>';
					var td6='<td>'+resp.contractors[i].period+'</td>';			
					var tr1="</tr>";
					var record=tr0+td1+td2+td3+td4+td5+td6+tr1;
					$("#tab_Attendee_manage").append(record);					
				}
			}
			else alert(resp.message);
		}
	});	
}
// 初始化实习生管理弹层（二级）
function init_form_Attendee_edit(){
	if(null==member_selected){
		alert("请选择要操作的人员！");
		return 1;
	}
	else{
		$("#member_name").val(member_selected.children().eq(0).text());
		$("#member_state").val(member_selected.children().eq(1).text());
		$("#member_signs").val(member_selected.children().eq(2).text());
		$("#in_time").val(member_selected.children().eq(3).text());
		$("#out_time").val(member_selected.children().eq(4).text());
	}
	return 0;
}
// 初始化人员需求详情弹层
function init_form_ManPoReq(){
	$("#ManPoReq_sttime").val(cur_date);
	$("#ManPoReq_edtime").val(cur_date);
	$("#ManPoReq_num").val("");
}
// 打开弹层
function open_form(formID,overlay_class){
	var tag=0;
	if(formID=="#form_AttendDetail")init_form_AttendDetail();
	else if(formID=="#form_AttendDetail_edit")init_form_AttendDetail_edit();
	else if(formID=="#form_ManPoReq")init_form_ManPoReq();
	else if(formID=="#form_Attendee_manage")init_form_Attendee_manage();
	else if(formID=="#form_Attendee_edit")tag=init_form_Attendee_edit();
	if(tag==0){
		$(formID).css("display","block");
		showOverlay(overlay_class);
		$(formID).show();
	}	
}
// 关闭弹层
function CloseForm(formID,overlayID){
	if(formID=='#form_AttendDetail' && tag_attend_edit==1){
		tag_attend_edit=0;
		init_page();	
	}
	row_selected=null;
	$(formID).hide();
	$(overlayID).hide();
}
// 添加或者修改工作日志
function add_Daylog(opt_type){
	if(opt_type=="new"){
		opt="new";
		$("#Daylog_date").attr("disabled",false);
		$("#Daylog_attendee").attr("disabled",false);
		$(".new_blank").val("");
		$("#Daylog_person").text(sessionStorage.usrfullname);
		$("#Daylog_date").val(cur_date);
		open_form("#form_Daylog",".overlay");
	}
	else if(opt_type=="edit"){
		opt="update";
		$("#Daylog_date").attr("disabled",true);
		$("#Daylog_attendee").attr("disabled",true);
		if(null==Dlog_selected)alert("请选择要编辑的日志记录！");
		else{
			//加载日志信息
			var contractor=Dlog_selected.children().eq(1).text();
			var date=Dlog_selected.children().eq(0).text();
			$("#Daylog_person").text(contractor);
			$("#Daylog_date").val(date);
			$("#Daylog_attendee").val(Dlog_selected.children().eq(3).text());

			$("#form_Daylog").css("display","block");
			showOverlay('.overlay');
			$("#form_Daylog").show();

			var url="TestExecManage/GetDlog?member_name="+contractor+"&date="+date;
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						var dl=resp.Dlog_list;
						$("#form_Daylog_content").remove();
						$("#tb_form_Dlog").append('<tbody id="form_Daylog_content"></tbody>');
						var linecc='';
						// 使用Update_Daylog API的返回结果
						for(var i=0;i<dl.length;i++){
							var tr0='<tr class="Dlog_row">';
							var tr1='<td class="leftcol"><select class="Dlog_type">';
							var tr11='<option>测试任务</option>';
							var tr12='<option>学习或会议</option>';
							var tr13='<option>系统运维</option>';
							var tr14='<option>测试开发</option></select></td>';
							if(dl[i].worktype=='测试任务')tr11='<option selected=true>测试任务</option>';
							else if(dl[i].worktype=='学习或会议')tr12='<option selected=true>学习或会议</option>';
							else if(dl[i].worktype=='系统运维')tr13='<option selected=true>系统运维</option>';
							else if(dl[i].worktype=='测试开发')tr14='<option selected=true>测试开发</option></select></td>';
							tr1=tr1+tr11+tr12+tr13+tr14;
							var tr2='<td><textarea type="text" class="Dlog_content new_blank">'+dl[i].content+'</textarea></td>';
							var tr3='<td><input type="text" class="Dlog_proj new_blank" value="'+dl[i].testproj+'"/></td>';
							var tr4='<td><input type="text" class="Dlog_ver new_blank" value="'+dl[i].projversion+'"/></td>';
							var tr5='<td><input type="text" class="Dlog_tcexe new_blank" value="'+dl[i].tcexe+'"/></td>';
							var tr6='<td><input type="text" class="Dlog_newbug new_blank" value="'+dl[i].newbug+'"/></td>';
							var tr7='<td><input type="text" class="Dlog_regbug new_blank" value="'+dl[i].regbug+'"/></td>';
							var tr8='</tr>';
							linecc=linecc+tr0+tr1+tr2+tr3+tr4+tr5+tr6+tr7+tr8;
						}						
						$("#form_Daylog_content").append(linecc);
						open_form("#form_Daylog",".overlay");
					}
					else alert(resp.message);
				}
			});	
		}
	}		
}
// 删除日志列表的一条记录
function del_Daylog(){
	if(Dlog_selected==null)alert("请先选择要操作的日志条目");
	else {
		$("#del_Daylog").attr("disabled",true);
		var logID=Dlog_selected.attr("id");
		var url="TestExecManage/DelDlog?user="+sessionStorage.customerId+"&logID="+logID;
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){	
					alert("日志已删除");
					load_Dlogs("");					
				}
				else alert(resp.message);
				$("#del_Daylog").attr("disabled",false);
			}
		});
	}
}
// 查询日志
function check_dlog(){
	var filter="";
	var filt_value=$("#filter").val();
	var phase=$("#phase option:selected").attr("value");
	if(filt_value!="")filter=phase+" like '"+filt_value+"*'";
	page_num=1;
	load_Dlogs(filter);	
}
// 下载日志文件
function import_Daylog(){
	var filter="";
	var filt_value=$("#filter").val();
	var phase=$("#phase option:selected").attr("value");
	if(filt_value!="")filter=phase+" like '"+filt_value+"*'";
	var url="TestExecManage/DownloadDLG?filter="+filter;	
	$("#import_Daylog").attr("disabled",true);					
	TMS_api(url,"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				downloadFile('http://tms.tech.bitauto.com:8080/ASWS/datareport/down?filename='+resp.DownloadFile_url);
			}
			else alert(resp.message);
			$("#import_Daylog").attr("disabled",false);
		}
	});	
}
// 考勤数据编辑弹层按钮-保存修改后的个人考勤数据并刷新考勤图
function Submit_AttendDetail() {
	var attend_dat={};
	attend_dat.contractor=$("#AttendDetail_edit_tester").val();
	attend_dat.attend_date=$("#AttendDetail_edit_date").val();
	if(attend_dat.contractor==""){
		alert("人员不能为空！");
		$("#AttendDetail_edit_tester").focus();
	}
	else if(attend_dat.attend_date==""){
		alert("考勤日期不能为空！");
		$("#AttendDetail_edit_date").focus();
	}
	else{
		$("#Submit_AttendDetail").attr("disabled",true);
		attend_dat.attend_time=$("#AttendDetail_edit_time").val();
		attend_dat.man_day=$("#AttendDetail_edit_time option:selected").attr("data-value");
		attend_dat.attendtype=$("#AttendDetail_edit_time option:selected").attr("data-type");
		var body=JSON.stringify(attend_dat);
		var url="TestExecManage/UpdateAttendance?user="+sessionStorage.customerId;						
		TMS_api(url,"POST",body,function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("成功保存");					
					CloseForm("#form_AttendDetail_edit",".overlay2");
					tag_attend_edit=1;
					load_attendance_person(cur_year,attend_dat.contractor);				
				}
				else alert(resp.message);
				$("#Submit_AttendDetail").attr("disabled",false);
			}
		});
	}
}
// 实习生管理弹层按钮-新增人员
function Add_contractor() {
	var new_member=prompt("请按照[姓名]=[YYYY-MM-DD]的格式输入新的成员信息：");
	if(null!=new_member){
		if(new_member.indexOf("=")>0){
			var temp_dat=new_member.split('=');
			var member_name=temp_dat[0];
			var in_time=temp_dat[1];
			if(isDate(in_time)){
				$("#Add_contractor").attr("disabled",true);
				var url="TestExecManage/AddContractor?user="+sessionStorage.customerId+"&member_name="+member_name+"&in_time="+in_time;;
				TMS_api(url,"GET","",function a(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){	
							init_form_Attendee_manage();
						}
						else alert(resp.message);
					}
					$("#Add_contractor").attr("disabled",false);
				});
			}
		}
		else alert("数据输入格式不对，缺少日期或者分隔符‘=’，请从新输入");
	}
}
// 实习生管理弹层按钮-删除人员
function Del_contractor() {
	if(null==member_selected)alert("请选择要操作的人员！");
	else{
		$("#Del_contractor").attr("disabled",true);
		var member_name=member_selected.children().eq(0).text();
		var url="TestExecManage/DelContractor?user="+sessionStorage.customerId+"&member_name="+member_name;
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200)init_form_Attendee_manage();
				else alert(resp.message);
				$("#Del_contractor").attr("disabled",false);
			}
		});
	}
}
// 实习生管理二级弹层按钮-编辑后保存
function Update_contractor(){
	var contractor={};
	contractor.name=$("#member_name").val();
	contractor.status=$("#member_state").val();
	contractor.sign_num=$("#member_signs").val();
	contractor.entry_date=$("#in_time").val();
	contractor.quit_date=$("#out_time").val();
	
	if(contractor.name==""){
		alert("人员不能为空！");
		$("#member_name").focus();
	}
	else if(contractor.entry_date==""){
		alert("入职日期不能为空！");
		$("#in_time").focus();
	}
	else{
		if(contractor.quit_date!=""){
			contractor.period=getTime_diff(contractor.quit_date,contractor.entry_date,'month');
			contractor.state="离职";
		}
		else {
			contractor.period=getTime_diff(cur_date,contractor.entry_date,'month');
		}
		if(contractor.sign_num==""){
			contractor.sign_num=parseInt(contractor.period/3+1);
		}
		$("#Update_contractor").attr("disabled",true);
		var body = JSON.stringify(contractor);
		var url="TestExecManage/UpdateContractor?user="+sessionStorage.customerId;						
		TMS_api(url,"POST",body,function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("成功保存");					
					init_form_Attendee_manage();
					CloseForm("#form_Attendee_edit",".overlay2");
				}
				else alert(resp.message);
				$("#Update_contractor").attr("disabled",false);
			}
		});
	}
}
// 日志编辑弹层按钮-增加一个日志记录
function addADlog(){
	if($("#form_Daylog_content").children().length>6)alert("日志最大支持6条！");
	else{
		var tr0='<tr class="Dlog_row">';
		var tr1='<td class="leftcol"><select class="Dlog_type"><option>测试任务</option><option>学习或会议</option><option>系统运维</option><option>测试开发</option></select></td>';
		var tr2='<td><textarea type="text" class="Dlog_content new_blank"></textarea></td>';
		var tr3='<td><input type="text" class="Dlog_proj new_blank" value=""/></td>';
		var tr4='<td><input type="text" class="Dlog_ver new_blank" value=""/></td>';
		var tr5='<td><input type="text" class="Dlog_tcexe new_blank" value=""/></td>';
		var tr6='<td><input type="text" class="Dlog_newbug new_blank" value=""/></td>';
		var tr7='<td><input type="text" class="Dlog_regbug new_blank" value=""/></td>';
		var tr8='</tr>';
		var linecc=tr0+tr1+tr2+tr3+tr4+tr5+tr6+tr7+tr8;
		$("#form_Daylog_content").append(linecc);
	}	
}
// 日志编辑弹层按钮-删除一个日志记录
function delADlog(){
	if(null!=row_selected)row_selected.remove();
}
// 日志编辑弹层按钮-提交新日志或修改日志
function Update_Daylog() {
	// 初始化数据结构
	var daylog={logs:[]};
	var tag=0;
	daylog.contractor=$("#Daylog_person").text();
	daylog.logdate=$("#Daylog_date").val();
	daylog.attendance=$("#Daylog_attendee").val();
	daylog.man_day=$("#Daylog_attendee option:selected").attr("data-value");
	daylog.attendtype=$("#Daylog_attendee option:selected").attr("data-type");

	var item_count=$("#form_Daylog_content").children().length;
	for(var i=0;i<item_count;i++){
		var log=$("#form_Daylog_content").children().eq(i);
		// 判断本行是否为空
		var log_item={};
		log_item.worktype=log.children().eq(0).children().eq(0).val();
		log_item.content=log.children().eq(1).children().eq(0).val();
		log_item.testproj=log.children().eq(2).children().eq(0).val();
		log_item.projversion=log.children().eq(3).children().eq(0).val();
		log_item.tcexe=log.children().eq(4).children().eq(0).val();
		if(log_item.tcexe=='')log_item.tcexe='0';
		log_item.newbug=log.children().eq(5).children().eq(0).val();
		if(log_item.newbug=='')log_item.newbug='0';
		log_item.regbug=log.children().eq(6).children().eq(0).val();
		if(log_item.regbug=='')log_item.regbug='0';
		if(log_item.content==""){
			alert("本条记录的工作内容说明不能为空，请补充或删除！");
			log.children().eq(1).children().eq(0).focus();
			tag=1;
			break;
		}
		else{
			if(log_item.worktype=='测试任务'){
				if(log_item.testproj==""){
					alert("本条记录的测试项目不能为空，请补充！");
					log.children().eq(2).children().eq(0).focus();
					tag=1;
					break;
				}
				else if(log_item.projversion==""){
					alert("本条记录的测试项目版本不能为空，请补充！");
					log.children().eq(3).children().eq(0).focus();
					tag=1;
					break;
				}
				else if(log_item.tcexe==""){
					alert("本条记录的执行用例数不能为空，请补充！");
					log.children().eq(4).children().eq(0).focus();
					tag=1;
					break;
				}
				else if(log_item.newbug==""){
					alert("本条记录的新提Bug数不能为空，请补充！");
					log.children().eq(5).children().eq(0).focus();
					tag=1;
					break;
				}
				else if(log_item.regbug==""){
					alert("本条记录的回归Bug数不能为空，请补充！");
					log.children().eq(6).children().eq(0).focus();
					tag=1;
					break;
				}
			}
		}
		daylog.logs.push(log_item);
	}
	if(tag==0){
		$("#Update_Daylog").attr("disabled",true);
		var body=JSON.stringify(daylog);
		var url="TestExecManage/Update_Daylog?user="+sessionStorage.customerId+"&opt="+opt;						
		TMS_api(url,"POST",body,function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("成功保存");					
					load_Dlogs("");
					init_page();
					CloseForm("#form_Daylog",".overlay");
				}
				else alert(resp.message);
				$("#Update_Daylog").attr("disabled",false);
			}
		});
	}
}
// 人员需求弹层按钮-提交或变更新的需求
function Update_ManPoReq() {
	var start_time=$("#ManPoReq_sttime").val();
	var end_time=$("#ManPoReq_edtime").val();
	var req=$("#ManPoReq_num").val();
	if(start_time>end_time)alert("起始时间不能大于结束时间！");
	else if(req==""){
		alert("人员需求数不能为空！");
		$("#ManPoReq_num").focus();
	}
	else if(isNaN(req)){
		alert("人员需求数只能为数字！");
		$("#ManPoReq_num").focus();
	}
	else{
		$("#Update_ManPoReq").attr("disabled",true);
		var url="TestExecManage/Update_ManPoReq?user="+sessionStorage.customerId+"&sttime="+start_time+"&edtime="+end_time+"&req="+req;
		TMS_api(url,"GET","",function b(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					load_ManPoReq();
					alert("更新成功！");
				}
				else alert(resp.message);
				$("#Update_ManPoReq").attr("disabled",false);
			}
		});
	}
}
// 加载任务表
function load_tasks(){
	var url="TestExecManage/ListTask";
	TMS_api(url,"GET","",function b(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				$("#tab_tasklist").remove();
				$("#tb_tasklist").append('<tbody id="tab_tasklist"></tbody>');
				var st="";
				var task=resp.task;
				var expand='<img src="img/collspan.png" width="12" height="12" href="javascript:void(0)" onclick="expandST(this)">';				
				var subtab_head='<tr class="collspan"><td></td><td colspan=4>';
				subtab_head=subtab_head+'<table width=100% cellspacing="0" cellpadding="0" class="subtable">';
				subtab_head=subtab_head+'<thead class="thead"><th class="cell" width=90 height=25>编号</th>';
				subtab_head=subtab_head+'<th class="cell" width=120>任务</th>';
				subtab_head=subtab_head+'<th class="cell" width=70>执行人</th>';
				subtab_head=subtab_head+'<th class="cell" width=120>任务创建时间</th>';
				subtab_head=subtab_head+'<th class="cell" width=120>期望结束时间</th>';
				subtab_head=subtab_head+'<th class="cell" width=70>任务状态</th>';
				subtab_head=subtab_head+'<th class="cell" width=170>必要条件说明</th>';
				subtab_head=subtab_head+'<th class="cell" width=170>任务内容说明</th></thead><tbody class="tab_subtasklist">';
				var subtab_foot="</tbody></table></td></tr>";
				for(var i=0;i<task.length;i++){
					var subtab="";
					var expand_tag="";
					var subtab_head_tag="";
					var subtab_foot_tag="";
					// 判断是否存在子任务
					var subtask=task[i].subtasks;
					if(subtask.length>0){
						expand_tag=expand;
						subtab_head_tag=subtab_head;
						subtab_foot_tag=subtab_foot;
						for(var j=0;j<subtask.length;j++){
							subtab=subtab+'<tr class="tdats" id="'+subtask[j].et_index+'">';
							subtab=subtab+'<td class="cell"><a href="javascript:void(0)" onclick="open_subtask(\''+
									subtask[j].et_index+'\',\''+task[i].responsor+'\',\''+task[i].starttime+'\')">'+
									subtask[j].et_index+'</a></td>';
							subtab=subtab+'<td class="cell"><div class="ltxt">'+subtask[j].name+'</div></td>';
							subtab=subtab+'<td class="cell">'+subtask[j].executor+'</td>';
							subtab=subtab+'<td class="cell">'+subtask[j].create_time+'</td>';
							subtab=subtab+'<td class="cell">'+subtask[j].endtime_exp+'</td>';
							subtab=subtab+'<td class="cell">'+subtask[j].status+'</td>';
							var precondition=subtask[j].precondition.replace(/\<br>/g,"");
							subtab=subtab+'<td class="cell"><div class="ltxt">'+precondition+'</div></td>';
							var content=subtask[j].content.replace(/\<br>/g,"");
							subtab=subtab+'<td class="cell"><div class="ltxt">'+content+'</div></td></tr>';
						}
					}
					// 创建任务表和子任务表
					st=st+'<tr class="tdats"><td>'+expand_tag+'</td>';
					st=st+'<td class="cell">'+task[i].proj+'</td>';
					st=st+'<td class="cell">'+task[i].subversion+'</td>';
					st=st+'<td class="cell">'+task[i].responsor+'</td>';
					st=st+'<td class="cell">'+task[i].starttime+'</td></tr>';
					st=st+subtab_head_tag;
					st=st+subtab;
					st=st+subtab_foot_tag;
				}
				if(st!="")$("#tab_tasklist").append(st);
				task_selected=null;
				subtask_selected=null;
			}
			else alert(resp.message);
		}
	});
}
// 添加和修改测试任务
function add_task(opt){
	var url="TestExecManage/ListContractors?filter=status='在职'";
	if(opt=='new'){
		if(null==task_selected)alert("请先选择要创建任务的测试项目");
		else{
			// 新表单清空所有input、texterea,初始化执行人列表
			$("#task_index").text("");
			$("#task_status").text("进行中");
			$("#form_task_edit input").val("");
			$("#task_proj").text(task_selected.children().eq(1).text());
			$("#task_subver").text(task_selected.children().eq(2).text());
			$("#task_starttime").text(task_selected.children().eq(4).text());
			$("#task_responsor").text(task_selected.children().eq(3).text());
			// 初始化执行人列表
			TMS_api(url,"GET","",function b(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						$("#task_exector option").remove();
						for(var i=0;i<resp.contractors.length;i++){
							$("#task_exector").append("<option>"+resp.contractors[i].name+"</option>");
						}
						open_form("#form_task_edit",".overlay");
					}
					else alert(resp.message);
				}
			});
		}		
	}
	else if(opt=='edit'){
		if(null==subtask_selected)alert("请先选择要修改的任务");
		else{
			// 修改任务，设置所有信息
			var et_index=subtask_selected.attr("id");
			$("#task_index").text("(编号："+et_index+")");	
			var task_row=subtask_selected.parent().parent().parent().parent().prev();	
			$("#task_responsor").text(task_row.children().eq(3).text());
			$("#task_starttime").text(task_row.children().eq(4).text());
			
			// 初始化执行人列表
			TMS_api(url,"GET","",function b(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						$("#task_exector option").remove();
						for(var i=0;i<resp.contractors.length;i++){
							$("#task_exector").append("<option>"+resp.contractors[i].name+"</option>");
						}

						// 获取任务信息
						url="TestExecManage/GetTask?et_index="+et_index;
						TMS_api(url,"GET","",function b(){
							if (xmlHttp.readyState==4 && xmlHttp.status==200){
								resp = JSON.parse(xmlHttp.responseText);
								if(resp.code==200){				
									$("#task_name").val(resp.name);	
									var dt=resp.endtime_exp;
									var exp_date="";
									var exp_time="";
									if(dt!=""){
										exp_date=dt.substr(0,dt.indexOf(" "));
										exp_time=dt.substr(dt.indexOf(" ")+1);
									}		
									$("#task_endtime_exp").val(exp_time);
									$("#task_enddate_exp").val(exp_date);
									$("#task_status").text(resp.status);
									$("#task_precondition").val(resp.precondition.replace(/\<br>/g,"\n"));
									$("#task_content").val(resp.content.replace(/\<br>/g,"\n"));
									$("#task_proj").text(resp.proj);
									$("#task_subver").text(resp.subversion);			
									$("#task_exector").val(resp.executor);
									open_form("#form_task_edit",".overlay");			
								}
								else alert(resp.message);
							}
						});	
					}
					else alert(resp.message);
				}
			});									
		}
	}
}
// 查看测试任务
function open_subtask(et_index,tm,sttime){	
	// 修改任务，设置所有信息
	$("#task_index_review").text("(编号："+et_index+")");	
	$("#task_responsor_review").text(tm);
	$("#task_starttime_review").text(sttime);

	var url="TestExecManage/GetTask?et_index="+et_index;
	TMS_api(url,"GET","",function b(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){				
				$("#task_name_review").val(resp.name);
				$("#task_exector_review").text(resp.executor);	
				$("#task_endtime_exp_review").text(resp.endtime_exp);
				var task_sta=resp.status;
				$("#task_status_review").text(task_sta);
				$("#task_precondition_review").html(resp.precondition.replace(/\<br>/g,"\n"));
				$("#task_content_review").html(resp.content.replace(/\<br>/g,"\n"));
				$("#task_proj_review").text(resp.proj);
				$("#task_subver_review").text(resp.subversion);			
				
				// 根据任务状态判断按钮功能
				$("#butt_task_opt1").hide();
				if(task_sta=="进行中"){
					$("#butt_task_opt").text("完成");
					$("#butt_task_opt").attr("data-value","已完成");
				}
				else if(task_sta=="已完成"){
					$("#butt_task_opt").text("结束");
					$("#butt_task_opt").attr("data-value","已关闭");
					$("#butt_task_opt1").show();
				}
				else if(task_sta=="已驳回"){
					$("#butt_task_opt").text("完成");
					$("#butt_task_opt").attr("data-value","已完成");
				}
				else if(task_sta=="已关闭"){
					$("#butt_task_opt").hide();
				}
				open_form("#form_task",".overlay");
			}
			else alert(resp.message);
		}
	});		
}
// 删除测试任务
function del_task(){
	if(null==subtask_selected)alert("请先选择要删除的任务");
	else{
		var url="TestExecManage/DelTask?et_index="+subtask_selected.attr("id")+"&user="+sessionStorage.customerId;
		TMS_api(url,"GET","",function b(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("删除成功！");
					load_tasks();
				}
				else alert(resp.message);
			}
		});			
	}
}
// 保存测试任务内容
function Save_task(){
	var task={};
	var url="TestExecManage/";
	var et_index=$("#task_index").text();
	var proj=$("#task_proj").text();
	var subver=$("#task_subver").text();
	var message="任务已更新！";
	// 任务编号为空，表示新任务
	if(et_index==""){
		url=url+"AddTask?proj="+proj+"&subversion="+subver+"&user="+sessionStorage.customerId;
		message="任务已发布！";
	}
	// 否则为对已有任务编辑
	else {
		et_index=et_index.substring(et_index.indexOf("：")+1,et_index.length-1);
		url=url+"UpdateTask?et_index="+et_index+"&user="+sessionStorage.customerId;
	}
	task.name=$("#task_name").val();
	task.executor=$("#task_exector").val();
	task.endtime_exp=$("#task_enddate_exp").val()+" "+$("#task_endtime_exp").val();
	var precondition=$("#task_precondition").val();
	task.precondition=precondition.replace(/\n/g,"<br>");
	var content=$("#task_content").val();
	task.content=content.replace(/\n/g,"<br>");
	var body=JSON.stringify(task);
	
	TMS_api(encodeURI(url),"POST",body,function b(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				alert(message);
				CloseForm('#form_task_edit','#overlay');
				load_tasks();
			}
			else alert(resp.message);
		}
	});		
}
// 翻到指定页数
function Topage(num){
	var filter="";
	var filt_value=$("#filter").val();
	var phase=$("#phase option:selected").attr("value");
	if(filt_value!="")filter=phase+" like '"+filt_value+"*'";

	if(num==0)page_num=page_sum;
	else page_num=num;
	load_Dlogs(filter);	
}
// 下一页
function Nextpage(tag){
	var filter="";
	var filt_value=$("#filter").val();
	var phase=$("#phase option:selected").attr("value");
	if(filt_value!="")filter=phase+" like '"+filt_value+"*'";

	if(tag=="+"){
		page_num=page_num+1;
		if(page_num>page_sum)page_num=page_sum;
	}
	else if(tag=="-"){
		page_num=page_num-1;
		if(page_num==0)page_num=1;
	}
	load_Dlogs(filter);	
} 
// 测试任务点击按钮后的展开与收缩
function expandST(e){
	var img_src=$(e).attr("src");
	if(task_selected!=null)task_selected.css("background-color","#FFFFFF");
	task_selected=$(e).parent().parent();
	var subtab=$(e).parent().parent().next();
	if(img_src=='img/collspan.png'){		
		$(e).attr("src",'img/collspan2.png');
		subtab.show();
	}
	else{
		$(e).attr("src",'img/collspan.png');
		subtab.hide();
	}
}
$(document).ready(function(){ 
	Dlog_selected=null;
	Dlog_id=null;
	member_selected=null;
	row_selected=null;
	task_selected=null;
	subtask_selected=null;
	if(typeof(sessionStorage.customerId)=='undefined'){;
		sessionStorage.currpage="login.html";
		$("#main", parent.document).attr("src",sessionStorage.currpage);
	}
	page_num=1;
	page_sum=0;
	tpitem_ppnum=11;
	opt="";
	tag_attend_edit=0;
	// 初始化当前日期参数
	var currtime=getDate();
	cur_date=currtime.substr(0,currtime.indexOf(" "));
	cur_year=cur_date.substr(0,cur_date.indexOf("-"));
	//初始化页面
	var url="TestExecManage/ListTask";
	TMS_api(url,"GET","",function b(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				$("#tab_tasklist").remove();
				$("#tb_tasklist").append('<tbody id="tab_tasklist"></tbody>');
				var st="";
				var task=resp.task;
				var expand='<img src="img/collspan.png" width="12" height="12" href="javascript:void(0)" onclick="expandST(this)">';				
				var subtab_head='<tr class="collspan"><td></td><td colspan=4>';
				subtab_head=subtab_head+'<table width=100% cellspacing="0" cellpadding="0" class="subtable">';
				subtab_head=subtab_head+'<thead class="thead"><th class="cell" width=90 height=25>编号</th>';
				subtab_head=subtab_head+'<th class="cell" width=120>任务</th>';
				subtab_head=subtab_head+'<th class="cell" width=70>执行人</th>';
				subtab_head=subtab_head+'<th class="cell" width=120>任务创建时间</th>';
				subtab_head=subtab_head+'<th class="cell" width=120>期望结束时间</th>';
				subtab_head=subtab_head+'<th class="cell" width=70>任务状态</th>';
				subtab_head=subtab_head+'<th class="cell" width=170>必要条件说明</th>';
				subtab_head=subtab_head+'<th class="cell" width=170>任务内容说明</th></thead><tbody class="tab_subtasklist">';
				var subtab_foot="</tbody></table></td></tr>";
				for(var i=0;i<task.length;i++){
					var subtab="";
					var expand_tag="";
					var subtab_head_tag="";
					var subtab_foot_tag="";
					// 判断是否存在子任务
					var subtask=task[i].subtasks;
					if(subtask.length>0){
						expand_tag=expand;
						subtab_head_tag=subtab_head;
						subtab_foot_tag=subtab_foot;
						for(var j=0;j<subtask.length;j++){
							subtab=subtab+'<tr class="tdats" id="'+subtask[j].et_index+'">';
							subtab=subtab+'<td class="cell"><a href="javascript:void(0)" onclick="open_subtask(\''+
									subtask[j].et_index+'\',\''+task[i].responsor+'\',\''+task[i].starttime+'\')">'+
									subtask[j].et_index+'</a></td>';
							subtab=subtab+'<td class="cell"><div class="ltxt">'+subtask[j].name+'</div></td>';
							subtab=subtab+'<td class="cell">'+subtask[j].executor+'</td>';
							subtab=subtab+'<td class="cell">'+subtask[j].create_time+'</td>';
							subtab=subtab+'<td class="cell">'+subtask[j].endtime_exp+'</td>';
							subtab=subtab+'<td class="cell">'+subtask[j].status+'</td>';
							var precondition=subtask[j].precondition.replace(/\<br>/g,".");
							subtab=subtab+'<td class="cell"><div class="ltxt">'+precondition+'</div></td>';
							var content=subtask[j].content.replace(/\<br>/g,".");
							subtab=subtab+'<td class="cell"><div class="ltxt">'+content+'</div></td></tr>';
						}
					}
					// 创建任务表和子任务表
					st=st+'<tr class="tdats" id="tl'+i+'"><td>'+expand_tag+'</td>';
					st=st+'<td class="cell">'+task[i].proj+'</td>';
					st=st+'<td class="cell">'+task[i].subversion+'</td>';
					st=st+'<td class="cell">'+task[i].responsor+'</td>';
					st=st+'<td class="cell">'+task[i].starttime+'</td></tr>';
					st=st+subtab_head_tag;
					st=st+subtab;
					st=st+subtab_foot_tag;
				}
				if(st!="")$("#tab_tasklist").append(st);
				init_page();
			}
			else alert(resp.message);
		}
	});
		

	//鼠标滑过按钮的变色效果
	$("button[class*='head_butt']").mouseenter(function(e) { 
		$(e.target).css({"color":"#FFFFFF","background-color": "#0DBFB3"});	
	});
	$("button[class*='head_butt']").mouseleave(function (e) { 
		$(e.target).css({"color":"#091E43","background-color": "rgba(204,204,204,0.3)"});
	});
	
	// 查找日志
	$("#find").click(function (e){
		var filter="";
		var filt_value=$("#filter").val();
		var phase=$("#phase option:selected").attr("value");
		if(filt_value!="")filter=phase+" like '"+filt_value+"*'";
		load_Dlogs(filter);	
	});

	//弹层-工作日志类型变更后清除后续内容(个人日志编辑和新建)
	$(".Dlog_type").change(function (e){
		if($(e.target).val()!=="测试任务"){
			var tr_parent=$(e.target).parent().parent();
			tr_parent.children().find("input").val("");
		}		
	});

	//弹层-人员变更后重新加载个人考勤信息
	$("#tester_list").change(function (e){
		var tester=$("#tester_list").val();
		var curyear=$("#attend_year_list").val();
		load_attendance_person(curyear,tester);
	});
	//弹层-年份变更后重新加载个人考勤信息
	$("#attend_year_list").change(function (e){
		var tester=$("#tester_list").val();
		var curyear=$("#attend_year_list").val();
		$("#form_AttendDetail_year").text(curyear);
		load_attendance_person(curyear,tester);
	});

	// 弹层-任务提交、审核按钮
	$("#butt_task_opt").click(function (e){
		var state=$("#butt_task_opt").attr("data-value");
		var et_index=$("#task_index_review").text();
		et_index=et_index.substring(et_index.indexOf("：")+1,et_index.length-1);
		var url="TestExecManage/ChangeTaskStatus?status="+state+"&et_index="+et_index+"&user="+sessionStorage.customerId;
		TMS_api(encodeURI(url),"GET","",function b(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("任务"+state+"！");
					CloseForm('#form_task','#overlay');
				}
				else alert(resp.message);
			}
		});
	});
	// 弹层-任务驳回按钮
	$("#butt_task_opt1").click(function (e){
		var et_index=$("#task_index_review").text();
		et_index=et_index.substring(et_index.indexOf("：")+1,et_index.length-1);
		var url="TestExecManage/ChangeTaskStatus?status=已驳回&et_index="+et_index+"&user="+sessionStorage.customerId;
		TMS_api(encodeURI(url),"GET","",function b(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					alert("任务已驳回！");
					CloseForm('#form_task','#overlay');
				}
				else alert(resp.message);
			}
		});
	});
	
	//弹层拖动-日志编辑
	$('#form_Daylog .form_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_Daylog').offset().left; 
		var abs_y = event.pageY - $('#form_Daylog').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_Daylog'); 
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
	//弹层拖动-考勤详情
	$('#form_AttendDetail .form_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_AttendDetail').offset().left; 
		var abs_y = event.pageY - $('#form_AttendDetail').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_AttendDetail'); 
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
	//弹层拖动-人力需求
	$('#form_ManPoReq .form_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_ManPoReq').offset().left; 
		var abs_y = event.pageY - $('#form_ManPoReq').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_ManPoReq'); 
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
	//弹层拖动-考勤详情编辑框
	$('#form_AttendDetail_edit .form_title_2').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_AttendDetail_edit').offset().left; 
		var abs_y = event.pageY - $('#form_AttendDetail_edit').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_AttendDetail_edit'); 
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
	//弹层拖动-人员管理
	$('#form_Attendee_manage .form_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_Attendee_manage').offset().left; 
		var abs_y = event.pageY - $('#form_Attendee_manage').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_Attendee_manage'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>846)rel_left=846;
				var rel_top=event.pageY - abs_y;
				if(rel_top<0)rel_top=0;
				if(rel_top>470)rel_top=470;
				obj.css({'left':rel_left, 'top':rel_top});  
			} 
		}).mouseup( function () { 
			isMove = false; 
		}); 
	});
	//弹层拖动-人员管理编辑框
	$('#form_Attendee_edit .form_title_2').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_Attendee_edit').offset().left; 
		var abs_y = event.pageY - $('#form_Attendee_edit').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_Attendee_edit'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>846)rel_left=846;
				var rel_top=event.pageY - abs_y;
				if(rel_top<0)rel_top=0;
				if(rel_top>470)rel_top=470;
				obj.css({'left':rel_left, 'top':rel_top});  
			} 
		}).mouseup( function () { 
			isMove = false; 
		}); 
	});
	//弹层拖动-任务查看
	$('#form_task .form_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_task').offset().left; 
		var abs_y = event.pageY - $('#form_task').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_task'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>846)rel_left=846;
				var rel_top=event.pageY - abs_y;
				if(rel_top<0)rel_top=0;
				if(rel_top>470)rel_top=470;
				obj.css({'left':rel_left, 'top':rel_top});  
			} 
		}).mouseup( function () { 
			isMove = false; 
		}); 
	});
	//弹层拖动-任务查看
	$('#form_task_edit .form_title').mousedown(function (event) { 
		var isMove = true; 
		var abs_x = event.pageX - $('#form_task_edit').offset().left; 
		var abs_y = event.pageY - $('#form_task_edit').offset().top; 
		$(document).mousemove(function (event) { 
			if (isMove) { 
				var obj = $('#form_task_edit'); 
				var rel_left=event.pageX - abs_x;
				if(rel_left<0)rel_left=0;
				if(rel_left>846)rel_left=846;
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
//实现项目选中和取消
$(document).click(function (e) { 
	var v_class=""+$(e.target).parent().attr('class');
	var obj_selected=null;
	if(v_class=="tdats"){		
		var obj_id="";
		var clickobj_id=$(e.target).parent().parent().attr("id");
		if(clickobj_id=="tab_Dloglist")obj_selected=Dlog_selected;
		else if(clickobj_id=="tab_Attendee_manage")obj_selected=member_selected;
		else if(clickobj_id=="tab_tasklist")obj_selected=task_selected;
		else if($(e.target).parent().parent().attr("class")=="tab_subtasklist")obj_selected=subtask_selected;
		else obj_selected=null;

		if(obj_selected==null){
			//有项目被选中
			obj_selected=$(e.target).parent();
			obj_selected.css("background-color","#F0F0F0");
		}
		else{
			var obj_id_old=obj_selected.attr("id");		
			var	obj_id_new=$(e.target).parent().attr("id");

			//项目被取消
			if(obj_id_old==obj_id_new){
				$(e.target).parent().css("background-color","#FFFFFF");
				obj_selected=null;
			}
			//选择了其他项目
			else{
				obj_selected.css("background-color","#FFFFFF");
				obj_selected=$(e.target).parent();
				obj_selected.css("background-color","#F0F0F0");
			}
		}

		if(clickobj_id=="tab_Dloglist")Dlog_selected=obj_selected;
		else if(clickobj_id=="tab_Attendee_manage")member_selected=obj_selected;
		else if(clickobj_id=="tab_tasklist")task_selected=obj_selected;
		else if($(e.target).parent().parent().attr("class")=="tab_subtasklist")subtask_selected=obj_selected;
		else if($(e.target).parent().parent().parent().attr("class")=="tab_subtasklist")subtask_selected=obj_selected;
		else obj_selected=null;
	}
	// 表格单元格下有子元素的情况
	else if($(e.target).parent().parent().attr('class')=="tdats"){
		var clickobj_id=$(e.target).parent().parent().parent().attr("class");
		if(clickobj_id=="tab_subtasklist")obj_selected=subtask_selected;
		if(obj_selected==null){
			//有项目被选中
			obj_selected=$(e.target).parent().parent();
			obj_selected.css("background-color","#F0F0F0");
		}
		else{
			var obj_id_old=obj_selected.attr("id");		
			var	obj_id_new=$(e.target).parent().parent().attr("id");

			//项目被取消
			if(obj_id_old==obj_id_new){
				$(e.target).parent().css("background-color","#FFFFFF");
				obj_selected=null;
			}
			//选择了其他项目
			else{
				obj_selected.css("background-color","#FFFFFF");
				obj_selected=$(e.target).parent().parent();
				obj_selected.css("background-color","#F0F0F0");
			}
		}

		if(clickobj_id=="tab_subtasklist")subtask_selected=obj_selected;
		else obj_selected=null;
	}
	else if($(e.target).parent().parent().attr('class')=="Dlog_row"){
		row_selected=$(e.target).parent().parent();
	}
});