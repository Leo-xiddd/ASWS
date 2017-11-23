var xmlHttp;
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

$(document).ready(function(){
	var hostpath_page=getHostUrl('hostpath_page');
	$("#loginbutt").click(function(){	
		var user=$("#loginuser").val();
		var pwd= $("#loginpwd").val();	
		if(user==""){
			alert("用户名不能为空！");
		}
		else if(pwd==""){
			alert("密码不能为空！");
		}
		else{	 
			url="User/Authen?user="+user+"&pwd="+encypt(pwd);
			TMS_api(url,"GET","",function()
			{if (xmlHttp.readyState==4 && xmlHttp.status==200){
				var resp = JSON.parse(xmlHttp.responseText);
				if(resp.code==200){
					nexturl=hostpath_page+"home.html";
					sessionStorage.customerId=user;
					sessionStorage.customerPwd=pwd;
					window.open(nexturl,'_self');
				}
				else alert(resp.message);
			}
			});
		}
	});
});

$(document).keyup(function(event){
	if(event.keyCode ==13) $("#loginbutt").trigger("click");
});