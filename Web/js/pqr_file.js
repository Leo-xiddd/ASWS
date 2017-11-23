var xmlHttp;
var last_aver_bugnum;
var last_rate_bug_l45;

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
		alert("通讯错误："+e);
	}	
}
function Tochart(target,picname,seris,dats){
	var myChart = echarts.init(document.getElementById(target)); 
	var option = {
		title:{
			show:true,
			text:'',
			textStyle:{fontWeight:'bolder',fontFamily:'微软雅黑',fontSize:14},
			left:'center'
		},
		tooltip: {},
		legend:{show:true,top:30,data:[]},
		grid:{ x:40, y:70,x2:10,y2:25},
		xAxis : {data : []},
		yAxis : {},
		series : [],
	};
	option.title.text=picname;
	var ss=seris.split(",");	
	for(var i=0;i<ss.length;i++)option.xAxis.data[i]=ss[i];
	for(var i=0;i<dats.length;i++){
		option.legend.data[i]=dats[i].seriers;
		var item={
			name:dats[i].seriers,
			type:dats[i].type,
			barWidth:'50%',
			showSymbol: true,
			itemStyle:{
				normal:{color:''},
				emphasis:{color:''}
			},
			data:[]			
		};
		option.series.push(item);
		var dd=dats[i].dat.split(",");		
		for(var j=0;j<ss.length;j++)option.series[i].data[j]=dd[j];	
		if(dats[i].seriers=="测试轮数"){
			option.series[i].itemStyle.normal.color='#4D659D';
			option.series[i].itemStyle.emphasis.color='#4D57F5';
		}
	}
	myChart.setOption(option); 
}
function Tochart_stack(target,picname,seris,dats){
	var myChart = echarts.init(document.getElementById(target)); 
	var option = {
		title:{
			show:true,
			text:'',
			textStyle:{fontWeight:'bolder',fontFamily:'微软雅黑',fontSize:14},
			left:'center'
		},
		tooltip: {},
		legend:{show:true,top:30,data:[]},
		grid:{ x:40, y:70,x2:10,y2:25},
		xAxis : {data : []},
		yAxis : {},
		series : [],
	};
	option.title.text=picname;
	var ss=seris.split(",");	
	for(var i=0;i<ss.length;i++)option.xAxis.data[i]=ss[i];
	for(var i=0;i<dats.length;i++){
		option.legend.data[i]=dats[i].seriers;
		var item={
			name:dats[i].seriers,
			type:dats[i].type,
			barWidth:'50%',
			showSymbol: true,
			itemStyle:{
				normal:{color:'#2F4554'},
				emphasis:{color:'#4F768C'}
			},
			stack: '百分百',
            label: {
                normal: {
                    show: true,
					formatter:'{c}%',
                    position: 'inside'
                }
            },
			data:[]			
		};
		option.series.push(item);
		var dd=dats[i].dat.split(",");		
		for(var j=0;j<ss.length;j++)option.series[i].data[j]=dd[j];	
		if(dats[i].seriers=="Bug重开次数占比"){
			option.series[i].itemStyle.normal.color='#C23531';
			option.series[i].itemStyle.emphasis.color='#D2544B';
		}
	}
	myChart.setOption(option); 
}
function Tochart_tag(target,picname,seris,dats,max_line,sta_line,line_name){
	var myChart = echarts.init(document.getElementById(target)); 
	var option = {
		title:{
			show:true,
			text:'',
			textStyle:{fontWeight:'bolder',fontFamily:'微软雅黑',fontSize:14},
			left:'center'
		},
		tooltip: {},
		legend:{show:true,top:30,data:[]},
		grid:{ x:40, y:70,x2:20,y2:25},
		xAxis : {
			type : 'category',
			axisTick: {
                alignWithLabel: false
            },
			data : []
		},
		yAxis : {max:'auto'},
		series : [
			{
				name:'',
				type:'bar',
				barWidth:'50%',
				itemStyle: {
					normal: {
						color: new echarts.graphic.LinearGradient(
							0, 0, 0, 1,
							[
								{offset: 0, color: '#9CABCE'},
								{offset: 0.5, color: '#4D659D'},
								{offset: 1, color: '#4D659D'}
							]
						)
					},
					emphasis: {
						color: new echarts.graphic.LinearGradient(
							0, 0, 0, 1,
							[
								{offset: 0, color: '#2378f7'},
								{offset: 0.7, color: '#2378f7'},
								{offset: 1, color: '#83bff6'}
							]
						)
					}
				},
				data:[],
				markPoint : {
					data : [
						{type : 'max', name: '最大值'},
						{type : 'min', name: '最小值'}
					]
				},
				markLine : {
					data : [
						{yAxis: 100, name: '平均值'}
					]
				}
			}
		]
	};
	option.title.text=picname;
	var ss=seris.split(",");	
	for(var i=0;i<ss.length;i++)option.xAxis.data[i]=ss[i];
	for(var i=0;i<dats.length;i++){
		option.legend.data[i]=dats[i].seriers;
		option.series[0].name=dats[i].seriers;
		var dd=dats[i].dat.split(",");		
		for(var j=0;j<ss.length;j++){
			option.series[0].data[j]=dd[j];	
			var ds=parseInt(dd[j]);
			if(ds>max_line)max_line=ds;
		}
	}
	option.yAxis.max=max_line;
	option.series[0].markLine.data[0].name=line_name;
	option.series[0].markLine.data[0].yAxis=sta_line;
	myChart.setOption(option); 
}
function ToPiechart(target,picname,seriers,dats){
	var myChart = echarts.init(document.getElementById(target)); 
	var option = {
		title:{
			show:true,
			text:'',
			textStyle:{fontWeight:'bolder',fontFamily:'微软雅黑',fontSize:14},
			x:'center'
		},
		legend: {
			orient: 'vertical',
			left: 'left',
			top:35,
			left:30,
			data: []
		},      
		series : [
			{
				name:"Bug严重级别",
				type:"pie",
				center:['60%','56%'],
				label: {
					normal: {show: false,position:'inside',formatter: '{b}: {d}%'},
					emphasis: {show: true}
				},
				lableLine: {
					normal: {show: false},
					emphasis: {show: true}
				},
				data:[]
			}
		]
	};
	option.title.text=picname;
	option.series[0].name=seriers;	
	for(var j=0;j<dats.length;j++){
		var item={
			name:dats[j].name,
			value:dats[j].value
		};
		option.series[0].data.push(item);
		option.legend.data[j]=dats[j].name;
	}
	myChart.setOption(option); 
}
function initPQR(body,opt){
	//分类变量
	var QualRisk=body.QualRisk;
	var summ_table=body.Summ_table;
	var state_analyze=body.state_analyze;
	var intest_proj_table=body.intest_proj_table;
	var chart=body.chart;
	// 质量风险
	if(opt=="edit")QualRisk=QualRisk.replace(/\[br]/g,"\n");
	else QualRisk=QualRisk.replace(/\[br]/g,"<br>");	
	$("#QualRisk").html(QualRisk);
	// 概要表
	for(var i=0;i<summ_table.length;i++){
		$("#Summ_table_body").append('<tr>'
			+'<td>'+summ_table[i].prodline+'</td>'
			+'<td class="cell_bgcolor_2">'+summ_table[i].vers_test+'</td>'
			+'<td class="cell_bgcolor_2">'+summ_table[i].vers_pass+'</td>'
			+'<td class="cell_bgcolor_2">'+summ_table[i].bug_total+'</td>'
			+'<td class="cell_bgcolor_2">'+summ_table[i].bug_close+'</td>'			
			+'<td class="cell_bgcolor_2">'+summ_table[i].bug_unclose+'</td>'				
			+'<td>'+summ_table[i].ver_pass_rate+'</td>'
			+'<td>'+summ_table[i].bug_close_rate+'</td>'
			+'<td>'+summ_table[i].PQI+'</td></tr>');
	}
	// 产品质量数据表
	for(var i=0;i<intest_proj_table.length;i++){
		$("#intest_proj_table_body").append('<tr>'
			+'<td>'+intest_proj_table[i].Test_proj+'</td>'
			+'<td>'+intest_proj_table[i].Test_cycle+'</td>'
			+'<td>'+intest_proj_table[i].Rate_passTC+'</td>'
			+'<td>'+intest_proj_table[i].Bug_Sum+'</td>'
			+'<td>'+intest_proj_table[i].Reopen_Rate+'</td>'
			+'<td>'+intest_proj_table[i].OpenBug+'</td>'
			+'<td>'+intest_proj_table[i].Version+'</td>'
			+'<td>'+intest_proj_table[i].PM+'</td></tr>');
	}

	// PQI曲线
	var chart_pqi=chart.chart_pqi;
	Tochart("chart_pqi",chart_pqi.title,chart_pqi.times,chart_pqi.dats);
	// 产品平均测试周期及回归次数变化图
	var chart_tpd=chart.chart_tpd;
	Tochart("chart_tpd",chart_tpd.title,chart_tpd.times,chart_tpd.dats);
	//新增Bug严重程度分布图
	var chart_bsd=chart.chart_bsd;
	ToPiechart("chart_bsd","Bug严重程度分布",chart_bsd.seriers,chart_bsd.dats);
	//产品回归次数图
	var chart_intest_tpd=chart.chart_intest_tpd;
	var maxlist=chart_intest_tpd.dats[0].dat;
	var maxvs=maxlist.split(",");
	var maxv=0;
	for(var i=0;i<maxvs.length;i++){
		if(maxv<parseInt(maxvs[i]))maxv=parseInt(maxvs[i]);
	}
	maxv=Math.ceil(maxv*1.3);
	Tochart_tag("chart_intest_tpd",chart_intest_tpd.title,chart_intest_tpd.times,chart_intest_tpd.dats,maxv,5,"经验值");
	//产品DQS指标状态图
	var chart_intest_bsd=chart.chart_intest_bsd;
	maxlist=chart_intest_bsd.dats[0].dat;
	maxvs=maxlist.split(",");
	maxv=0;
	for(var i=0;i<maxvs.length;i++){
		if(maxv<parseInt(maxvs[i]))maxv=parseInt(maxvs[i]);
	}
	maxv=Math.ceil(maxv*1.3);
	Tochart_tag("chart_intest_bsd",chart_intest_bsd.title,chart_intest_bsd.times,chart_intest_bsd.dats,maxv,3,"上线标准");
	//产品Bug重开率对比图
	var chart_intest_brd=chart.chart_intest_brd;
	Tochart_stack("chart_intest_brd",chart_intest_brd.title,chart_intest_brd.times,chart_intest_brd.dats);
	//产品待处理Bug分布图
	var chart_intest_bld=chart.chart_intest_bld;
	Tochart("chart_intest_bld",chart_intest_bld.title,chart_intest_bld.times,chart_intest_bld.dats);

	// 产品测试周期控制描述及表格
	var curr_aver_ct=chart_tpd.dats[1].dat;
	if(curr_aver_ct.indexOf(",")>-1)curr_aver_ct=curr_aver_ct.substring(curr_aver_ct.lastIndexOf(",")+1);
	$(".curr_aver_cycles").text(curr_aver_ct);
	curr_aver_ct=chart_tpd.dats[0].dat;
	if(curr_aver_ct.indexOf(",")>-1)curr_aver_ct=curr_aver_ct.substring(curr_aver_ct.lastIndexOf(",")+1);
	$(".curr_aver_timecost").text(curr_aver_ct);
	var toler_aver_timecost=state_analyze.toler_aver_timecost;
	if(toler_aver_timecost=='0')$("#toler_aver_timecost_hide").hide();
	else{
		if(toler_aver_timecost.indexOf("-")>-1)toler_aver_timecost=toler_aver_timecost.replace(/\-/g,"降低");
		else toler_aver_timecost="增加"+toler_aver_timecost;
		$("#toler_aver_timecost").text(toler_aver_timecost);
	}
	
	$(".reject_version").text(state_analyze.reject_version);
	
	//产品Bug分布描述及表格	
	$("#sum_bugnum").text(state_analyze.sum_bugnum);	
	$(".aver_bugnum").text(state_analyze.aver_bugnum);

	last_aver_bugnum=parseFloat(state_analyze.last_aver_bugnum);
	if(last_aver_bugnum==0)$("#toler_aver_bugnum_hide").hide();
	else{
		var toler_aver_bugnum=state_analyze.toler_aver_bugnum;
		if(toler_aver_bugnum.indexOf("-")>-1)toler_aver_bugnum=toler_aver_bugnum.replace(/\-/g,"减少");
		else toler_aver_bugnum="增加"+toler_aver_bugnum;
		$("#toler_aver_bugnum").text(toler_aver_bugnum);
	}
	

	$("#rate_bug_l45").text(state_analyze.rate_bug_l45);
	last_rate_bug_l45=parseFloat(state_analyze.last_rate_bug_l45);
	if(last_rate_bug_l45==0)$("#toler_bug_l45_hide").hide();
	else{
		var toler_bug_l45=state_analyze.toler_bug_l45;
		if(toler_bug_l45.indexOf("-")>-1)toler_bug_l45=toler_bug_l45.replace(/\-/g,"减少");
		else toler_bug_l45="增加"+toler_bug_l45;
		$("#toler_bug_l45").text(toler_bug_l45);
	}
	
	if(opt=="new" || opt=="edit"){
		$("#onlinever_untest").val(state_analyze.onlinever_untest);
		$("#dev_num").val(state_analyze.dev_num);
		$("#L1_bugnum").val(state_analyze.L1_bugnum);
		$("#L2_bugnum").val(state_analyze.L2_bugnum);
		$("#L3_bugnum").val(state_analyze.L3_bugnum);
		$("#L4_bugnum").val(state_analyze.L4_bugnum);
		$("#L5_bugnum").val(state_analyze.L5_bugnum);
	}
	else{
		$("#onlinever_untest").text(state_analyze.onlinever_untest);
		$("#dev_num").text(state_analyze.dev_num);
		$("#L1_bugnum").text(state_analyze.L1_bugnum);
		$("#L2_bugnum").text(state_analyze.L2_bugnum);
		$("#L3_bugnum").text(state_analyze.L3_bugnum);
		$("#L4_bugnum").text(state_analyze.L4_bugnum);
		$("#L5_bugnum").text(state_analyze.L5_bugnum);
	}
	
	$("#wait").hide();
	$("#overlay").hide();
}

$(document).ready(function(){
	var hostpath_page=getHostUrl('hostpath_page');
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url="login.html";
		window.open(encodeURI(url),'_self');
	}
	var str=window.location.search;
	var pqr_fn=decodeURI(getvalue(str,"filename"));
	var pqr_index=decodeURI(getvalue(str,"pqr_index"));
	var type=decodeURI(getvalue(str,"type"));
	var alone=decodeURI(getvalue(str,"alone"));
	var pqr_data="";
	var url="";
	var M="";
	var st=0;
	var et=0;
	last_rate_bug_l45=0;
	last_aver_bugnum=0;
	//初始化报告
	$("#title").text(pqr_fn);
	if(type=="new"){
		if(pqr_fn.indexOf("季度")>-1){
			$(".pqr_period").text("季度");
			M=pqr_fn.substring(pqr_fn.indexOf("年")+1,pqr_fn.indexOf("季度"));
			if(M=="1")st=1;
			else if(M=="2")st=4;
			else if(M=="3")st=7;
			else st=10;
			et=st+2;
		}
		else{
			$(".pqr_period").text("月度");
			M=pqr_fn.substring(pqr_fn.indexOf("年")+1,pqr_fn.indexOf("月"));
			st=parseInt(M);
			et=st;
		}
		var Y=pqr_fn.substring(pqr_fn.indexOf("-")+2,pqr_fn.indexOf("年"));
		var day = new Date(parseInt(Y),et,0);    
		var lastdate = day.getDate(); 
	
		$("#pqr_st").text(Y+"年"+st+"月1日");
		$("#pqr_et").text(Y+"年"+et+"月"+lastdate+"日");
		
		url="PQR/Add?PQR_index="+pqr_index+"&pqr_st="+$("#pqr_st").text()+"&pqr_et="+$("#pqr_et").text()+"&user="+sessionStorage.customerId;
		TMS_api(url,"GET","",function b(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){				
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					initPQR(resp,type);
				}
				else {
					$("#wait").hide();
					$("#overlay").hide();
					alert(resp.message);
					parent.location.reload();
				}
			}
		});
	}
	else{
		if(alone=="yes"){
			$(".pqrfile_butt").hide();
		}
		url="PQR/Get?PQR_index="+pqr_index;
		TMS_api(url,"GET","",function b(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					$("#title").text(resp.PQR_name);
					var st_t=resp.pqr_STtime;
					var et_t=resp.pqr_ENDtime;
					$("#pqr_st").text(st_t);					
					$("#pqr_et").text(et_t);
					$(".pqr_period").text(resp.pqr_period);
					initPQR(resp,type);
				}
				else  {
					$("#wait").hide();
					$("#overlay").hide();
					$(".pqrfile_butt").hide();
					alert(resp.message);
				}
			}
		});
	}
	
	//实现模块收缩和展开效果
	$(".butt_slid").click(function(e){
		var obj=$(e.target).parent().parent().parent().parent().parent();
		obj.next().slideToggle("slow");
	});
	
	//退出报告创建或编辑
	$("#butt_cancel").click(function(){
		//如果修改报告时取消，则直接返回上个页面
		if(type=="new"){
			//取消创建报告，应该先删除服务器端已创建的报告，所以应该先发通知
			url="PQR/Delete?user="+sessionStorage.customerId+"&PQR_index="+pqr_index;
			TMS_api(url,"GET","",function b(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						sessionStorage.currpage=hostpath_page+"PQR.html";
						$("#main",parent.parent.document).attr("src",sessionStorage.currpage);
					}
					else alert("由于："+resp.message+",质量报告未能成功取消，请手动删除服务器后台的数据");
				}
			});				
		}
		else {
			sessionStorage.currpage=hostpath_page+"PQR.html";
			$("#main",parent.parent.document).attr("src",sessionStorage.currpage);
		}
		
	});
	
	//开发人数被改动触发自动计算
	var devnum=0;
	$("#dev_num").change(function b(e){
		if(isNaN($("#dev_num").val())){
			alert("开发人员数量必须为数字！");
			$("#dev_num").val("");
		}
		else{			
			devnum=parseInt($("#dev_num").val());
			if(devnum<1 || $("#dev_num").val().indexOf(".")>-1){
				alert("开发人员数量必须为大于0的整数！");
				$("#dev_num").val("");
			}
			else{
				var dev_num=parseInt($("#dev_num").val());
				var Bug_sum=parseInt($("#sum_bugnum").text());
				var aver_bugnum=parseInt(100*Bug_sum/dev_num)/100;
				$(".aver_bugnum").text(""+aver_bugnum);
				
				var toler_aver_bugnum=0;
				if(last_aver_bugnum>0){
					toler_aver_bugnum=parseInt((aver_bugnum-last_aver_bugnum)*10000/last_aver_bugnum)/100;		
					if(toler_aver_bugnum<0)$("#toler_aver_bugnum").text("降低"+(0-toler_aver_bugnum));
					else $("#toler_aver_bugnum").text("增加"+toler_aver_bugnum);	
				}
			}			
		}			
	});		
	//未测试上线版本数被改动
	$("#onlinever_untest").change(function b(e){
		if(isNaN($("#onlinever_untest").val())){
			alert("未测试上线版本数必须为数字！");
			$("#onlinever_untest").val("");
		}
		else{
			var untest=parseInt($("#onlinever_untest").val());
			if(untest<0 || $("#onlinever_untest").val().indexOf(".")>-1){
				alert("未测试上线版本数必须为大于或等于0的整数！");
				$("#onlinever_untest").val("");
			}
		}
	});	
	//L1\2\3\4\5 Bug数被改动触发自动计算
	$("input[id*='_bugnum']").change(function b(e){
		if(isNaN($(e.target).val())){
			alert("Bug数量必须为数字！");
			$(e.target).val("");
		}
		else{
			L1B=parseInt($(e.target).val());
			if(L1B<0 ||  $(e.target).val().indexOf(".")>-1){
				alert("Bug数量必须为大于或等于0的整数！");
				$(e.target).val("");
			}
			else{
				var LB=[];
				var num=0;
				var chartdat="";
				var nams=["","L1-轻微问题","L2-轻度问题","L3-中等问题","L4-重要问题","L5-严重问题"];
				for(var i=1;i<6;i++){
					var tager="#L"+i+"_bugnum";
					var bugnum=$(tager).val();
					if(bugnum!=""){
						chartdat=chartdat+'{"name": "'+nams[i]+'","value": "'+bugnum+'"},';
						LB.push(parseInt(bugnum));
						num++;
					}
				}
				if(chartdat!=""){
					chartdat=chartdat.substring(0,chartdat.length-1);
					chartdat='['+chartdat+']';
					var jsondat=JSON.parse(chartdat);
					ToPiechart("chart_bsd","Bug严重程度分布","Buglv",jsondat);
				}
				if(num==5){
					var sum_bug=LB[0]+LB[1]+LB[2]+LB[3]+LB[4];
					$("#sum_bugnum").text(""+sum_bug);
					var dev_num=0;
					if($("#dev_num").val()!="")dev_num=parseInt($("#dev_num").val());
					if(dev_num>0){
						var aver_bugnum=parseInt(sum_bug*100/dev_num)/100;
						$(".aver_bugnum").text(""+aver_bugnum);
					}
					var rate_bug_l45=0;					
					if(sum_bug>0)rate_bug_l45=parseInt((LB[4]+LB[3])*100/sum_bug);
					$("#rate_bug_l45").text(""+rate_bug_l45);
					if(last_rate_bug_l45>0){
						var toler_bug_l45=rate_bug_l45-last_rate_bug_l45;			
						if(rate_bug_l45>=last_rate_bug_l45)$("#toler_bug_l45").text("增加"+toler_bug_l45);
						else $("#toler_bug_l45").text("降低"+(0-toler_bug_l45));
					}
					if(last_aver_bugnum>0){						
						var aver_bugnum=parseFloat($(".aver_bugnum:eq(0)").text());
						toler_aver_bugnum=parseInt((aver_bugnum-last_aver_bugnum)*10000/last_aver_bugnum)/100;			
						if(toler_aver_bugnum<0)$("#toler_aver_bugnum").text("降低"+(0-toler_aver_bugnum));
						else $("#toler_aver_bugnum").text("增加"+toler_aver_bugnum);	
					}					
				}
			}
		}			
	});	
	
	//提交保存报告
	$("#butt_save").click(function(){
		var pqr={
			pqr_period: $("#pqr_period_value").text(),
			pqr_STtime: $("#pqr_st").text(),
			pqr_ENDtime: $("#pqr_et").text(),
		};
		//检查所有表单是否都填写
		var QualRisk=$("#QualRisk").val();
		var dev_num=$("#dev_num").val();
		var onlinever_untest=$("#onlinever_untest").val();
		var L1_bugnum=$("#L1_bugnum").val();
		var L2_bugnum=$("#L2_bugnum").val();
		var L3_bugnum=$("#L3_bugnum").val();
		var L4_bugnum=$("#L4_bugnum").val();
		var L5_bugnum=$("#L5_bugnum").val();
		var aver_bugnum=$(".aver_bugnum:eq(1)").text();
		var rate_bug_l45=$("#rate_bug_l45").text();
		var toler_aver_bugnum=$("#toler_aver_bugnum").text();
		if(toler_aver_bugnum=="")toler_aver_bugnum="0";
		else{
			toler_aver_bugnum=toler_aver_bugnum.replace(/\增加/g,"");
			toler_aver_bugnum=toler_aver_bugnum.replace(/\降低/g,"-");
		}
		var toler_bug_l45=$("#toler_bug_l45").text();
		if(toler_bug_l45=="")toler_bug_l45="0";
		else{
			toler_bug_l45=toler_bug_l45.replace(/\增加/g,"");
			toler_bug_l45=toler_bug_l45.replace(/\降低/g,"-");
		}
		if(QualRisk==""){
			alert("请补充质量风险分析！");
			$("#QualRisk").focus();
		}
		else if(dev_num==""){
			alert("请补充开发人员数量！");
			$("#dev_num").focus();
		}
		else if(onlinever_untest==""){
			alert("请补充未经测试上线的版本数量！");
			$("#onlinever_untest").focus();
		}
		else if(L1_bugnum==""){
			alert("请补充L1级Bug数量！");
			$("#L1_bugnum").focus();
		}
		else if(L2_bugnum==""){
			alert("请补充L2级Bug数量！");
			$("#L2_bugnum").focus();
		}
		else if(L3_bugnum==""){
			alert("请补充L3级Bug数量！");
			$("#L3_bugnum").focus();
		}
		else if(L4_bugnum==""){
			alert("请补充L4级Bug数量！");
			$("#L4_bugnum").focus();
		}
		else if(L5_bugnum==""){
			alert("请补充L5级Bug数量！");
			$("#L5_bugnum").focus();
		}
		else{
			QualRisk=QualRisk.replace(/\；/g,";");
			QualRisk=QualRisk.replace(/\n/g,"[br]");	
			pqr.QualRisk=QualRisk;
			var state_analyze={};

			state_analyze.dev_num=dev_num;
			state_analyze.onlinever_untest=onlinever_untest;
			state_analyze.L1_bugnum=L1_bugnum;
			state_analyze.L2_bugnum=L2_bugnum;
			state_analyze.L3_bugnum=L3_bugnum;
			state_analyze.L4_bugnum=L4_bugnum;
			state_analyze.L5_bugnum=L5_bugnum;
			state_analyze.aver_bugnum=aver_bugnum;
			state_analyze.toler_aver_bugnum=toler_aver_bugnum;
			state_analyze.rate_bug_l45=rate_bug_l45
			state_analyze.toler_bug_l45=toler_bug_l45
			pqr.state_analyze=state_analyze;
				
			var body=JSON.stringify(pqr);
			url="PQR/Update?user="+sessionStorage.customerId+"&PQR_index="+pqr_index;
			//提交API，如果成功则关闭页面，并触发父页面刷新，失败则回到页面提示失败原因
			TMS_api(url,"POST",body,function b(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						alert("报告提交成功！");
						parent.location.reload(); 
					}
					else  alert(resp.message);
				}
			});
		}
	});
	
	//打开一个独立页面
	$("#pqrfile_butt_review").click(function(e){
		url="PQR_file.html?type=review&pqr_index="+pqr_index+"&alone=yes";
		window.open(encodeURI(url),"");
	});
	
	//进入编辑状态
	$("#pqrfile_butt_edit").click(function(e){
		url="PQR_newfile.html?type=edit&pqr_index="+pqr_index+"&filename="+pqr_fn;
		window.open(encodeURI(url),"_self");
	});	
});
