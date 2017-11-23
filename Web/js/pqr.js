var pqrfile;
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
		alert("通讯错误："+e);
	}	
}
$(document).ready(function(){
	hostpath_page=getHostUrl('hostpath_page');
	if(typeof(sessionStorage.customerId)=='undefined'){
		var url=hostpath_page+"login.html";
		window.open(encodeURI(url),'_self');
	}
	pqrfile=null;
	//初始化报告页，发送API获取报告列表，如果返回为空，则显示blank页，否则显示最新的报告页
	var url="PQR/GetList";
	TMS_api(url,"GET","",function b(){
		if (xmlHttp.readyState==4 && xmlHttp.status==200){
			var body=xmlHttp.responseText;
			var resp = JSON.parse(body);
			if(resp.code==200){
				$("#treelist").remove();
				var num=resp.pqrnum;
				if(num!="0"){
					var pqrs=resp.pqrs;
					//添加列表
					var tag1=-1;
					var tao1="";
					$("#treebox").append('<ul id="treelist" class="filetree"></ul>');
					for(var i=0;i<pqrs.length;i++){
						tao1=tao1+'<li><span class="folder">'+pqrs[i].Fyear+'年报告列表</span>';
						var tao2='';
						if(tag1==-1 && pqrs[i].Ylist.length>0)tag1=i;
						for(var j=0;j<pqrs[i].Ylist.length;j++){
							tao2=tao2+'<li><span class="file" id="'+pqrs[i].Ylist[j]+'">'+pqrs[i].Ylist[j]+'</span></li>';
						}
						if(tao2!="")tao2='<ul>'+tao2+'</ul>';
						tao1=tao1+tao2+'</li>';
					}
					$("#treelist").append(tao1);
					$("#treelist").treeview();

					//打开最新的一期质量报告									
					pqrfile=pqrs[tag1].Ylist[0];
					$("#"+pqrfile).css("fontWeight","600");
					$("button").show();
					$("#ifrb").attr("src",encodeURI(hostpath_page+"PQR_file.html?pqr_index="+pqrfile));
				}
				else{
					$("#ifrb").attr("src",encodeURI(hostpath_page+"PQR_blank.html"));
				}
			}
			else alert(resp.message);
		}
	});
	
	//添加一个新报告，需要弹出弹层询问时间
	$("#add").click(function(){	
			$("#newfile_panel").css("display","block");
			var date = new Date();
			$("#pqr_year").val(date.getFullYear());
			var month = date.getMonth() + 1;
			$("#pqr_month").val(month+"月");
			$("#newfile_panel").show();
	});
	$("#newpqr_sel_cancle").click(function(){			
		$("#newfile_panel").hide();
	});
	//弹层按钮 创建新PQR报告
	$("#newpqr_sel_next").click(function(){		
		var Y=$("#pqr_year").val();
		var M=$("#pqr_month").val();
		var E=M.replace(/\季度/g,"Q");
		var E=E.replace(/\月/g,"M");
		$("#newfile_panel").hide();
		$("#ifrb").attr("src",encodeURI(hostpath_page+"PQR_newfile.html?type=new&pqr_index=PQR_"+Y+E+"&filename=产品质量审计报告 - "+Y+"年"+M));
	});
	
	//删除当前报告
	$("#del").click(function(e){
		if(pqrfile==null)alert("请选择要删除的报告");
		else {
			url="PQR/Delete?user="+sessionStorage.customerId+"&PQR_index="+pqrfile;
			TMS_api(url,"GET","",function b(){
				if (xmlHttp.readyState==4 && xmlHttp.status==200){
					var body=xmlHttp.responseText;
					var resp = JSON.parse(body);
					if(resp.code==200){
						alert("质量报告"+pqrfile+"已成功删除！");
						location.reload();
					}
					else alert(resp.message);
				}
			});			
		}
	});
});
//树型视图下选择项目
$(document).click(function (e) { 		
	if($(e.target).attr('class')=="file"){
		var pid=$(e.target).attr("id");
		if(pqrfile!=pid){
			$("#"+pqrfile).css("fontWeight","");
			$(e.target).css("fontWeight","600");
			pqrfile=pid;
			$("#ifrb").attr("src",encodeURI(hostpath_page+"PQR_file.html?pqr_index="+pqrfile));	
		}
	}
});