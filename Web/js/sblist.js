var xmlHttp;
var item_select;
var proj;
var version;
var newsb;
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
// 打开提测表单
function open_submit_form(TSB_index,type,testtype){
	if(type=="new"){
		$("#Submittime").text(getDate());
		$("#Submitter").text(sessionStorage.usrfullname);
		$("#sbstate").text("");
		if(texttype=='功能'){
			$("#basicinfo").show();
			$("#TPinfo").hide();
		}
		else{
			$("#basicinfo").hide();
			$("#TPinfo").show();
		}
		var url="User/TitleList?title=PM";
		TMS_api(url,"GET","",function(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var body=xmlHttp.responseText;
				var resp = JSON.parse(body);
				if(resp.code==200){
					for(var i=0;i<resp.userlist.length;i++){
						$("#Proj_Manager").append("<option>"+resp.userlist[i]+"</option>"); 
					}
				}
				else alert(resp.message);			
			}
		});
	}	
	else if(type!=""){
		//审核提测单，改变按钮布局
		if(type=="check"){			
			$("#btnClose").remove();
			$("#btnSubmit").remove();
			$("#btnReject").remove();
			$("#btnApprove").remove();
			$("#sb_buttgroup").append('<button id="btnReject" href="javascript:void(0)" onclick="sb_reject(\''+TSB_index+'\')">驳回</button>&nbsp;');
			$("#sb_buttgroup").append('<button id="btnApprove" href="javascript:void(0)" onclick="sb_approve(\''+TSB_index+'\')">接受</button>');
		}		
		//查看提测单，隐藏所有按钮
		else if(type=="review")$("#sb_buttgroup").hide();
		else newsb=TSB_index;
		
		//查看和审核提测单，改变表单布局
		if(type!="edit"){
			$("#Proj_Subversion").attr("readonly",true);
			$("#Proj_Subversion").css("border","0");
			$("#Developer").attr("readonly",true);
			$("#Developer").css("border","0");
			$("#Proj_Manager").attr("disabled",true);
			$("#Proj_Manager").css("border","0");
			$("#Proj_Manager").css("background-color","#FFFFFF");
			$("#Proj_Manager").css("color","#000000");

			// 常规测试项
			$("#FunctionDes").after('<span id="FunctionDes" class="ltext"></span>');
			$("#FunctionDes").remove();
			
			$("#Range").after('<span id="Range" class="ltext"></span>');
			$("#Range").remove();
			
			$("#UED_codeurl").after('<span id="UED_codeurl"></span>&nbsp;');
			$("#UED_codeurl").remove();

			$("#front_codeurl").after('<span id="front_codeurl"></span>&nbsp;');
			$("#front_codeurl").remove();

			$("#Other_codeurl").after('<span id="Other_codeurl"></span>&nbsp;');
			$("#Other_codeurl").remove();

			$("#UED_codeurl_branch").after('&nbsp;<span id="UED_codeurl_branch"></span>');
			$("#UED_codeurl_branch").remove();
			$("#front_codeurl_branch").after('&nbsp;<span id="front_codeurl_branch"></span>');
			$("#front_codeurl_branch").remove();
			$("#Other_codeurl_branch").after('&nbsp;<span id="Other_codeurl_branch"></span>');
			$("#Other_codeurl_branch").remove();

			$("#Wikiurl").attr("readonly",true);
			$("#Wikiurl").css("border","0");
			$("textarea").css("height","auto");
			
			// 性能测试项


			//通过API请求提测单数据
			var url="TestTask/GetSubmit?TSB_index="+TSB_index;
			TMS_api(url,"GET","",function(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){
						if(testtype=='功能'){
							$("#basicinfo").show();
							$("#TPinfo").hide();
						}
						else{
							$("#basicinfo").hide();
							$("#TPinfo").show();
						}
						$("#Submittime").text(resp.Submitt.Submittime);
						$("#Submitter").text(resp.Submitt.Submitter);
						$("#Submitstatus").text(resp.Submitt.Status);
						$("#Proj_Subversion").val(resp.Submitt.Proj_Subversion);
						$("#Proj_Manager").append("<option>"+resp.Submitt.Proj_Manager+"</option>"); 
						$("#Proj_Manager").val(resp.Submitt.Proj_Manager);

						$("#Developer").val(resp.Submitt.Developer);

						var fdes=resp.Submitt.FunctionDes;
						fdes=fdes.replace(/\<br>/g,"\n<br/>");
						$("#FunctionDes").html(fdes);
						fdes=resp.Submitt.Range;
						fdes=fdes.replace(/\<br>/g,"\n<br/>");
						$("#Range").html(fdes);

						var codeurl=resp.Submitt.UED_codeurl;
						if(codeurl.indexOf("::")>0){
							var cul=codeurl.split("::");
							$("#UED_codeurl").text(cul[0]);
							$("#UED_codeurl_branch").text(cul[1]);
						}
						else{
							$("#UED_codeurl").text(codeurl);
							$("#UED_codeurl_branch").text("");
						}
						
						codeurl=resp.Submitt.front_codeurl;
						if(codeurl.indexOf("::")>0){
							var cul=codeurl.split("::");
							$("#front_codeurl").text(cul[0]);
							$("#front_codeurl_branch").text(cul[1]);
						}
						else{
							$("#front_codeurl").text(codeurl);
							$("#front_codeurl_branch").text("");
						}

						codeurl=resp.Submitt.Other_codeurl;
						if(codeurl.indexOf("::")>0){
							var cul=codeurl.split("::");
							$("#Other_codeurl").text(cul[0]);
							$("#Other_codeurl_branch").text(cul[1]);
						}
						else{
							$("#Other_codeurl").text(codeurl);
							$("#Other_codeurl_branch").text("");
						}
						$("#Wikiurl").val(resp.Submitt.Wikiurl);
					}
					else alert(resp.message);
				}
			});
		}
		else{
			var url="User/TitleList?title=PM&"+Math.random();
			TMS_api(url,"GET","",function a(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var resp = JSON.parse(xmlHttp.responseText);
					if(resp.code==200){	
						var getapi=xmlHttp.responseText;
						var PMlist = JSON.parse(getapi);
						for(var i=0;i<PMlist.userlist.length;i++){
							$("#Proj_Manager").append("<option>"+PMlist.userlist[i]+"</option>"); 
						}
						
						//通过API请求提测单数据
						url="TestTask/GetSubmit?TSB_index="+TSB_index;
						TMS_api(url,"GET","",function(){
							if (xmlHttp.readyState==4 && xmlHttp.status==200){
								var resp = JSON.parse(xmlHttp.responseText);
								if(resp.code==200){
									if(testtype=='功能'){
										$("#basicinfo").show();
										$("#TPinfo").hide();

										$("#Submittime").text(resp.Submitt.Submittime);
										$("#Submitter").text(resp.Submitt.Submitter);
										$("#sbstate").text("");
										$("#Proj_Subversion").val(resp.Submitt.Proj_Subversion);
										$("#Proj_Manager").val(resp.Submitt.Proj_Manager);
										$("#Developer").val(resp.Submitt.Developer);

										var fdes=resp.Submitt.FunctionDes;
										fdes=fdes.replace(/\<br>/g,"\n");
										$("#FunctionDes").html(fdes);
										fdes=resp.Submitt.Range;
										fdes=fdes.replace(/\<br>/g,"\n");
										$("#Range").html(fdes);

										var codeurl=resp.Submitt.UED_codeurl;
										if(codeurl.indexOf("::")>0){
											var cul=codeurl.split("::");
											$("#UED_codeurl").val(cul[0]);
											$("#UED_codeurl_branch").val(cul[1]);
										}
										else{
											$("#UED_codeurl").val(codeurl);
											$("#UED_codeurl_branch").val("");
										}
						
										codeurl=resp.Submitt.front_codeurl;
										if(codeurl.indexOf("::")>0){
											var cul=codeurl.split("::");
											$("#front_codeurl").val(cul[0]);
											$("#front_codeurl_branch").val(cul[1]);
										}
										else{
											$("#front_codeurl").val(codeurl);
											$("#front_codeurl_branch").val("");
										}

										codeurl=resp.Submitt.Other_codeurl;
										if(codeurl.indexOf("::")>0){
											var cul=codeurl.split("::");
											$("#Other_codeurl").val(cul[0]);
											$("#Other_codeurl_branch").val(cul[1]);
										}
										else{
											$("#Other_codeurl").val(codeurl);
											$("#Other_codeurl_branch").val("");
										}								
										$("#Wikiurl").val(resp.Submitt.Wikiurl);
									}
									else{
										$("#basicinfo").hide();
										$("#TPinfo").show();

									}
																		
									//增加删除按钮
									if($("#btnReject").length==0){
										$("#btnClose").before('<button id="btnReject" href="javascript:void(0)" onclick="sb_del(\''+TSB_index+'\')">删除</button>&nbsp;');
									}									
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
function opensb(TSB_index,Status,Submitter,testtype){
	var type="review";
	if(Status=="待处理"){
		var url="User/Getinfo?user="+sessionStorage.customerId;
		TMS_api(url,"GET","",function a(){
			if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var body=xmlHttp.responseText;
				var resp = JSON.parse(body);
				if(resp.title=="TM" || resp.title=="ATM")type="check";
				if(resp.fullname==Submitter)type="edit";
				open_submit_form(TSB_index,type,testtype);
			}
		});
	}
	else{
		if(Status=="")type="new";
		open_submit_form(TSB_index,type,testtype);
	}
}
function reload_sblist(){
	//如果已有列表存在，则清除
	$("#sblist").children().remove();
	var url="TestTask/ListSubmit?proj="+proj+"&version="+version;
	TMS_api(encodeURI(url),"GET","",function a(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var body=xmlHttp.responseText;
			var resp = JSON.parse(body);
			if(resp.code==200){
				var sbs=resp.Submitts;
				if(sbs.length==0){
					opensb("","","","功能");
					$("#sblist").append("<div id='blank_note'>本项目还没有提测记录，在右侧表单中创建一个吧!</div>");
					$("#sblist").append("<div><button id='butt_switchToPT'>改为性能测试提测</button></div>");
				}
				else{
					var th='<tr><th class="thead" style="display:none;"></th>';
					var th0='<th class="thead" width="70px">提测版本</th>';
					var th1='<th class="thead" width="130px">提测时间</th>';
					var th2='<th class="thead" width="60px">提测人</th>';
					var th3='<th class="thead" width="60px">测试类型</th>';
					var th4='<th class="thead" width="70px">提测状态</th></tr>';					
					
					var tbody="";
					for(var i=0;i<sbs.length;i++){
						var td='<tr id="tr'+i+'"><td class="cell" style="display:none;">'+sbs[i].TSB_index+'</td>';	
						var td0='<td class="cell">'+sbs[i].Proj_Subversion+'</td>';	
						var td1='<td class="cell">'+sbs[i].Submittime+'</td>';
						var td2='<td class="cell">'+sbs[i].Submitter+'</td>';
						var td3='<td class="cell">'+sbs[i].testtype+'</td>';
						var td4='<td class="cell">'+sbs[i].Status+'</td></tr>';
						tbody=tbody+td+td0+td1+td2+td3+td4;
					}
					$("#sblist").append(th+th0+th1+th2+th3+th4+tbody);
					$("#tr0").css("background-color","#E2F6CB");
					item_select=$("#tr0");
					opensb(sbs[0].TSB_index,sbs[0].Status,sbs[0].Submitter,sbs[0].testtype);
				}
			}
			else alert(resp.message);
		}
	});
}
//删除当前提测单
function sb_del(sbname){
	var url="TestTask/DelSubmit?TSB_index="+sbname;
	TMS_api(url,"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){	
				alert("删除成功！");
				parent.location.reload();
			}
			else alert(resp.message);
		}
	});
}
//接受当前提测单
function sb_approve(sbname){
	var url="TestTask/StartTask?user="+sessionStorage.customerId+"&TSB_index="+sbname;
	TMS_api(url,"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				alert("提测已接受，已通知相关人员！");
				parent.location.reload();
			}
			else alert(resp.message);
		}
	});
}
//驳回当前提测单
function sb_reject(sbname){
	var url="TestTask/RejectSubmit?user="+sessionStorage.customerId+"&TSB_index="+sbname;
	TMS_api(url,"GET","",function(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var resp = JSON.parse(xmlHttp.responseText);
			if(resp.code==200){
				alert("提测已驳回，已通知相关人员！");
				parent.location.reload();
			}
			else alert(resp.message);
		}
	});
}
$(document).ready(function(){ 
	var hostpath_page=getHostUrl('hostpath_page');
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url="login.html";
		window.open(encodeURI(url),'_self');
	}
	var str=window.location.search;
	proj=decodeURI(getvalue(str,"proj"));
	version=decodeURI(getvalue(str,"vers"));
	item_select=null;
	newsb="";
	$("#Test_Proj").text(proj);
	$("#Test_Version").text(version);
	reload_sblist();

	//按钮 - 提交提测单
	$("#btnSubmit").click(function(){
		var submit={};
		submit.Proj_Subversion= $("#Proj_Subversion").val();
		submit.Submitter=$("#Submitter").text();
		var developer=$("#Developer").val();
		submit.Developer=developer.replace(/，/g,",");
		submit.Proj_Name=$("#Test_Proj").text();	
		submit.Proj_Version=$("#Test_Version").text();
		submit.Proj_Manager=$("#Proj_Manager").val();
		submit.Submittime=getDate();
		// 判断当前是功能还是性能测试提测
		if($("#basicinfo").is(":hidden")) {
				var body = JSON.stringify(submit);
				url="TestTask/AddPTSubmit?user="+sessionStorage.customerId;
				if(newsb!="")url="TestTask/UpdateSubmit?TSB_index="+newsb;
				TMS_api(url,"POST",body,function(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){
							alert("保存成功！");
							parent.location.reload();
						}
						else alert(resp.message);
					}
				});	
		}
		else{
			var FunctionDes= $("#FunctionDes").val();
			FunctionDes=FunctionDes.replace(/；/g,";");
			submit.FunctionDes=FunctionDes.replace(/\n/g,"<br>");
			var Range=$("#Range").val();
			Range=Range.replace(/；/g,";");
			submit.Range=Range.replace(/\n/g,"<br>");

			var UED_codeurl=$("#UED_codeurl").val();
			if(UED_codeurl=="http://git.ctags.cn/***.git")submit.UED_codeurl="null";
			submit.UED_codeurl=UED_codeurl+"::"+$("#UED_codeurl_branch").val();

			var front_codeurl=$("#front_codeurl").val();
			if(front_codeurl=="http://git.ctags.cn/***.git")submit.front_codeurl="null";
			submit.front_codeurl=front_codeurl+"::"+$("#front_codeurl_branch").val();

			var Other_codeurl=$("#Other_codeurl").val();
			if(Other_codeurl=="http://git.ctags.cn/***.git")submit.Other_codeurl="null";
			submit.Other_codeurl=Other_codeurl+"::"+$("#Other_codeurl_branch").val();

			var Wikiurl= $("#Wikiurl").val();
			if(Wikiurl=="http://jw.tech.bitauto.com:8090/pages/viewpage.action?pageId=")submit.Wikiurl="null";
			else submit.Wikiurl=Wikiurl;
			if(submit.Proj_Subversion==""){
			alert("提测版本号为必填项！");
			$("#Proj_Subversion").focus();
			}
			else if(submit.FunctionDes==""){
				alert("版本的功能描述为必填项！");
				$("#FunctionDes").focus();
			}
			else if(submit.Range==""){
				alert("测试范围为必填项");
				$("#Range").focus();
			}
			else if(submit.UED_codeurl==""){
				alert("UED代码拉取地址为必填项！");
				$("#UED_codeurl").focus();
			}
			else if(submit.front_codeurl==""){
				alert("前端代码拉取地址为必填项");
				$("#front_codeurl").focus();
			}
			else if(submit.Wikiurl==""){
				alert("部署文档链接为必填项");
				$("#Wikiurl").focus();
			}
			else{
				submit.Note="";
				var body = JSON.stringify(submit);
				url="TestTask/AddSubmit?user="+sessionStorage.customerId;
				if(newsb!="")url="TestTask/UpdateSubmit?TSB_index="+newsb;
				TMS_api(url,"POST",body,function(){
					if (xmlHttp.readyState==4 && xmlHttp.status==200){
						var resp = JSON.parse(xmlHttp.responseText);
						if(resp.code==200){
							alert("保存成功！");
							parent.location.reload();
						}
						else alert(resp.message);
					}
				});	
			}		
		}		
	});
	//按钮 - 取消编辑或关闭本页面
	$("#btnClose").click(function(){
		if(newsb==""){
			sessionStorage.currpage=encodeURI(hostpath_page+"TP_list.html");
			$("#main", parent.document).attr("src",sessionStorage.currpage);
		}
		else{
			var testtype=item_select.children().eq(4).text();
			open_submit_form(newsb,"review",testtype);
			newsb="";
		}		
	});
	// 将新提测单切换为性能测试格式或功能测试格式
	$("#butt_switchToPT").click(function(){
		if($("#basicinfo").is(":hidden")) {
			$("#butt_switchToPT").html("改为功能测试提测");
			$("#basicinfo").show();
			$("#TPinfo").hide();
		}
		else{
			$("#butt_switchToPT").html("改为性能测试提测");
			$("#basicinfo").hide();
			$("#TPinfo").show();
		}
	});
});

$(document).click(function (e) { 
	var v_class=""+$(e.target).attr('class');
	//当项目被点击
	if(v_class=="cell"){
		var item=$(e.target).parent();
		if(item_select==null){
			//有项目被选中
			item_select=item;
			var TSB_index=item.children().eq(0).text();;
			var	Status=item.children().eq(5).text();
			var submiter=item.children().eq(3).text();
			var testtype=item.children().eq(4).text();
			item.css("background-color","#E2F6CB");
			opensb(TSB_index,Status,submiter,testtype);
		}
		else{
			var TSB_index=item_select.children().eq(0).text();
			var TSB_index_new=item.children().eq(0).text();
			var	Status=item.children().eq(5).text();
			var submiter=item.children().eq(3).text();
			var testtype=item.children().eq(4).text();
			//项目被取消
			if(TSB_index==TSB_index_new){
				item_select=null;
				item.css("background-color","#FFFFFF");
			}
			//选择了其他项目
			else{
				item_select.css("background-color","#FFFFFF");
				item_select=item;
				item.css("background-color","#E2F6CB");
				opensb(TSB_index_new,Status,submiter,testtype);
			}
		}
	}
});