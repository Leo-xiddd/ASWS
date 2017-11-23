/**
 * 本模块提供的API包括：
 * 1. Authen	--	User.Authority(String name, String pwd)
 * 2. Add		--	User.Add(String name, String pwd, String fname, String role, String email, String mobile)
 * 3. Delete	--	User.Del(String name) 
 * 4. List			--	User.List(String filter)
 * 5. Getinfo	--	User.Get(String name)
 */
package main;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.log4j.*;

import base.*;

public class UserAPI {
	User user=new User();	
	XMLDriver xml= new XMLDriver();
	Ldap ldap =new Ldap();
	DBDriver dbd = new DBDriver();
//	配置日志属性文件位置
	static String confpath=System.getProperty("user.dir").replace("\\bin", "");
	static String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	static String sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	Logger logger = Logger.getLogger(UserAPI.class.getName());
	
	/**[Function] 				API解释模块，根据API检查必要的参数和请求数据的完整性，调用对应API实现模块
	 * @param API			API字符串
	 * @param Param		API请求中URL携带的参数表
	 * @param body		API请求携带的body数据，Json格式字符串
	 * @param token		校验字，API请求携带的header参数，用来保证报文的可靠性和安全性
	 * @return [String]		Json格式字符串，返回API执行的结果
	 */
	public String DoAPI(String API,Map<String, String[]> Param,String body){						
		PropertyConfigurator.configure(logconf);		
		String message="";
		logger.info("API: "+API+" "+" [Body]"+body);
		String backvalue="412,http 请求的参数缺失或无效";
		try {
			switch(API){
			case "Authen":
				logger.info("进行用户鉴权...");	
				String usr=checkpara(Param,"user");
				String p=checkpara(Param,"pwd");
				if((!usr.equals(""))&&(!p.equals(""))) {
					int co=user.Authority(usr,p);
					String fn="";
					if(co<300)fn=user.getfname(usr);
					return "{\"code\":"+co+",\"fullname\":\""+fn+"\"}";
				}
				break;
			case "AddLdap":   	
				logger.info("添加LDAP...");
				if(!body.equals("")) {
					AddLDAP(body);
					backvalue="200,ok"; 
				}
				break;
			case "GetLdap":   	
				logger.info("获取LDAP配置信息...");
				return GetLDAP();
			case "ImportLdap":   	
				logger.info("从LDAP导入用户账户...");
				String bdn=checkpara(Param,"BaseDN");
				if(!bdn.equals("")) {
					ImpLDAP(bdn);
					backvalue="200,ok";  
				}
				break;
			case "Add":   	
				logger.info("创建新用户账户...");
				if(!body.equals("")) {
					JSONObject jsb=new JSONObject(body);			
					String name=jsb.getString("usrname");
					String pwd=jsb.getString("passwd");
					String fname=jsb.getString("fullname");
					String dept1=jsb.getString("dept1");
					String dept2=jsb.getString("dept2");
					String dept3=jsb.getString("dept3");
					String role=jsb.getString("role");
					String email=jsb.getString("email");
					String mobile=jsb.getString("mobile");
					String type=jsb.getString("type");
					user.Add(name, pwd, fname,dept1,dept2,dept3, role, email, mobile,type);
					backvalue="200,ok";
				}				
				break;
			case "Update":   	
				logger.info("更新用户信息...");
				if(!body.equals("")) {
					JSONObject jsb=new JSONObject(body);			
					String name=jsb.getString("usrname");
					String pwd=jsb.getString("passwd");
					String fname=jsb.getString("fullname");
					String dept1=jsb.getString("dept1");
					String dept2=jsb.getString("dept2");
					String role=jsb.getString("role");
					String email=jsb.getString("email");
					String mobile=jsb.getString("mobile");
					String type=jsb.getString("type");
					user.Change(name, pwd, fname, dept1,dept2,role, email, mobile,type);
					backvalue="200,ok";
				}				
				break;
			case "Delete":  
				String pd=checkpara(Param,"user");
				if(!pd.equals("")) {
					logger.info("删除用户账户"+pd+"...");
					user.Del(pd);
					backvalue="200,ok";
				}			
				break;
			case "List":   
				logger.info("列出所有用户账户...");
				pd=checkpara(Param,"filter");
				if(pd.indexOf("=")==-1)pd=pd.replace("*", "%");
				return listuser(pd);
			case "TitleList":
				String titl=checkpara(Param,"title");
				if(!titl.equals("")) {
					logger.info("列出所有职位为"+titl+"的用户账户...");
					return ListuserbyTitle(titl);
				}
			case "Getinfo":   
				logger.info("获取指定用户信息...");
				pd=checkpara(Param,"user");
				if(!pd.equals("")) {
					return GetInfo(pd);				
				}
			case "CheckUser":   	
				pd=checkpara(Param,"filter");
				if(pd.indexOf("=")==-1)pd=pd.replace("*", "%");
				if(!pd.equals("")) {
					logger.info("根据条件"+pd+"查询用户...");
					return GetUserinfo(pd);				
				}
			case "LoadPurview":				
				pd=checkpara(Param,"user");
				if(!pd.equals("")) {
					logger.info("获取指定用户的权限表...");
					return LoadPurview(pd);				
				}
			case "UpdatePurview":				
				pd=checkpara(Param,"user");
				if(!pd.equals("") && !body.equals("")) {
					logger.info("更新指定用户的权限表...");
					UpdatePurview(pd,body);	
					backvalue="200,ok";
				}	
				break;
			case "InitPurview":				
				logger.info("初始化权限表...");
				InitPurview();	
				backvalue="200,ok";		
				break;
			default:
				logger.error("无效API: "+API);
				backvalue="400,无效API!";
			}
		}catch (Throwable e) {
			backvalue=e.getMessage();
			int firtag=backvalue.lastIndexOf("[info]");
			if(firtag>-1) backvalue=backvalue.substring(firtag+6);
			else backvalue="500,"+backvalue;
			logger.error(backvalue,e);
		}	
		String code=backvalue.substring(0,backvalue.indexOf(","));
		message=backvalue.substring(backvalue.indexOf(",")+1);
		backvalue="{\"code\":"+code+",\"message\":\""+message+"\"}";
		return backvalue;
	}
	
	String listuser(String pd) throws Exception {
		try {
			String[][] ul=user.List(pd);
			JSONObject Userlist=new JSONObject();
			JSONArray jas= new JSONArray();
			int num=ul.length;
			if(!ul[0][0].equals("")) {
				for(int i=0;i<num;i++) {
					JSONObject jsb=new JSONObject();
					jsb.put("id", ul[i][0]);
					jsb.put("usrname", ul[i][1]);
					jsb.put("fullname", ul[i][2]);
					jsb.put("dept1", ul[i][3]);
					jsb.put("dept2", ul[i][4]);
					jsb.put("dept3", ul[i][5]);
					jsb.put("role", ul[i][6]);
					jsb.put("email", ul[i][7]);
					jsb.put("mobile", ul[i][8]);
					jsb.put("type", ul[i][9]);
					jas.put(jsb);
				}
			}			
			Userlist.put("userlist", jas);
			Userlist.put("code", 200);
			return Userlist.toString();
		}catch(Throwable e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}		
	}
	
	String ListuserbyTitle(String title)throws Exception {
		try {
			JSONObject Usrlst=new JSONObject();
			JSONArray Userlist= new JSONArray();
			String[][] ur=dbd.readDB("sys_usr_role", "fullname", "role='"+title+"'");
			if(!ur[0][0].equals("")) {
				for(int i=0;i<ur.length;i++)Userlist.put(i, ur[i][0]);
			}
			Usrlst.put("userlist", Userlist);
			Usrlst.put("code", 200);
			return Usrlst.toString();
		}catch(Throwable e) {
			logger.error(e.getMessage(), e);
			throw new Exception(e.getMessage());
		}
	}
	void AddLDAP(String body) throws Exception {
		try {
			JSONObject ldap=new JSONObject(body);
			xml.Update(sysconf, "LDAP_conf/host", ldap.getString("host"));
			xml.Update(sysconf, "LDAP_conf/port", ldap.getString("port"));
			xml.Update(sysconf, "LDAP_conf/domain", ldap.getString("domain"));
			xml.Update(sysconf, "LDAP_conf/admin", ldap.getString("admin"));
			String pwd=ldap.getString("pwd");
			if(!pwd.equals(""))	xml.Update(sysconf, "LDAP_conf/pwd",pwd );
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e);
		}
	}
	String GetLDAP() throws Exception {
		try {
			JSONObject ldap=new JSONObject();
			ldap.put("host", xml.GetNode(sysconf, "LDAP_conf/host"));
			ldap.put("port", xml.GetNode(sysconf, "LDAP_conf/port"));
			ldap.put("domain", xml.GetNode(sysconf, "LDAP_conf/domain"));
			ldap.put("admin", xml.GetNode(sysconf, "LDAP_conf/admin"));
			ldap.put("code",200);
			return ldap.toString();
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e);
		}
	}
	
	void ImpLDAP(String bdn) throws Exception {
		try {
//			1.判断配置文件中是否有完整的LDAP配置信息
			String host=xml.GetNode(sysconf, "LDAP_conf/host");
			if(host.equals(""))throw new Exception("[info]404,请先配置LDAP。");
			
//			2.从LDAP中导入用户信息
			String[] phase= {"account","fullname","dept","dept2","mail","mobile"};
			String[] colname= {"usrname","fullname","dept1","dept2","email","mobile","role","type"};
			String[] record=new String[colname.length];
			String usrlist=ldap.Getuserlist(bdn);
			JSONArray ulist=new JSONArray(usrlist);
			String[] check_col= {"usrname","type"};
			String[] check_dat=new String[2];
			check_dat[1]="ldap";
			if(ulist.length()>0) {
				for(int i=0;i<ulist.length();i++) {
					JSONObject a=ulist.getJSONObject(i);
					for(int j=0;j<phase.length;j++) {
						record[j]=a.getString(phase[j]);
					}
					record[6]="user";
					record[7]="ldap";
					check_dat[0]=record[0];
					int row=dbd.check("sys_usrdb", check_col, check_dat);
					if(row>0) {
						dbd.UpdateSQl("sys_usrdb", row, "fullname", record[1]);
						dbd.UpdateSQl("sys_usrdb", row, "dept1", record[2]);
						dbd.UpdateSQl("sys_usrdb", row, "dept2", record[3]);
						dbd.UpdateSQl("sys_usrdb", row, "email", record[4]);
						dbd.UpdateSQl("sys_usrdb", row, "mobile", record[5]);
					}
					else dbd.AppendSQl("sys_usrdb", colname, record, 1, 1);
				}
			}
		}catch(Throwable e) {
			throw new Exception("[info]500,导入用户失败，请检查路径设置和LDAP设置是否正确");
		}
	}
	/**
	 * 函数说明：用于获取用户的单一信息，包括全名、邮箱、手机号等
	 * @param account		要获取信息的用户名，可以是多个用户，用逗号分隔
	 * @param item				要获取的信息项目
	 * @return		单一字符串，用逗号分隔多个结果
	 */
	String GetInfo(String username) throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
			String[] user_info=user.Get(username);
			JSONObject Userinfo=new JSONObject();
			
			Userinfo.put("name", user_info[0]);
			Userinfo.put("fullname", user_info[1]);
			Userinfo.put("dept1", user_info[2]);
			Userinfo.put("dept2", user_info[3]);
			Userinfo.put("dept3", user_info[4]);
			Userinfo.put("role", user_info[5]);
			Userinfo.put("email", user_info[6]);
			Userinfo.put("mobile", user_info[7]);
			Userinfo.put("type", user_info[8]);

			String[][] at=dbd.readDB("sys_usr_role", "role", "usrname='"+username+"'");
			Userinfo.put("title", at[0][0]);
			Userinfo.put("code", 200);
			return Userinfo.toString();	
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e);
		}
	}
	/**
	 * 函数说明：查询满足条件的用户账号
	 * @param filter	查询条件
	 * @return		JSON格式字符串，如：{"usrname":"xxxxx","code":200}
	 * @throws Exception
	 */
	String GetUserinfo(String filter) throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
			JSONObject bku=new JSONObject();
			String[][] usrinfo=dbd.readDB("sys_usrdb", "usrname", filter);
			bku.put("usrname", usrinfo[0][0]);
			bku.put("code", 200);
			return bku.toString();
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e);
		}
	}
	/**
	 * 函数说明：获取指定用户的权限表
	 * @param usrname	用户账号
	 * @return		JSON格式字符串，如{"purview":[{"module":"工作周报","list":[{"key":"weekreport_Page_Access","value":"y","text":"页面访问"},{},...]},{},...],"code":200}
	 * @throws Exception
	 */
	String LoadPurview(String usrname) throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
			JSONObject bku=new JSONObject();
			int row=dbd.check("sys_usr_purview", "usrname", usrname);
			if(row==0)throw new Exception("[info]404,用户"+usrname+"不存在！");
						
			String colname= "weekreport_Page_Access,weekreport_AddTWR,weekreport_CheckTWR,weekreport_RemoveTWR,"
					+"RDcloud_Page_Access,ReqMan_Page_Access,ReqMan_Add,ReqMan_Del,ReqMan_Import,projlist_Page_Access,projlist_ProductLine,"
					+"projlist_Product,projlist_AddProj,projlist_UpdateProj,projlist_CloseProj,projlist_DelProj,projlist_ApprovProj,TP_list_Page_Access,"
					+"TP_list_Addtp,TP_list_Closetp,TP_list_Submit,TP_list_CheckSubmit,TP_list_ApprovSubmit,TP_list_AddTR,TP_list_ApprovTR,TP_list_CheckTR,TestExce_Page_Access,"
					+"TestExce_Contractor,TestExce_Attendee,TestExce_AddTask,TestExce_UpdateTask,TestExce_DelTask,TestExce_ManReq,TestExce_AddDlog,"
					+"TestExce_UpdateDlog,TestExce_DelDlog,TestExce_ImportDlog,PQR_Page_Access,PQR_Add,PQR_Del,PQR_Import,Log_verify_Page_Access,"
					+"PerformanceTest_Page_Access,UsrMange_Page_Access,Purview_Page_Access";
			String[][] pruview=dbd.readDB("sys_usr_purview", "*", "id="+row);
			String[] keys=colname.split(",");
			String[][] modules={{"工作周报","0","3"},{"研发云","4","4"},{"需求管理","5","8"},{"项目管理","9","16"},{"功能测试","17","25"},{"测试实施","26","36"},
					{"质量审计","37","40"},{"日志分析工具","41","41"},{"性能测试","42","42"},{"用户管理","43","43"},{"权限管理","44","44"}};
			String[][] text=dbd.readDB("sys_usr_purview", "*", "id=1");
			JSONArray purv=new JSONArray();
			for(int i=0;i<modules.length;i++) {
				JSONObject module=new JSONObject();
				module.put("module", modules[i][0]);
				int tag1=Integer.parseInt(modules[i][1]);
				int tag2=Integer.parseInt(modules[i][2]);
				JSONArray list=new JSONArray();
				for(int j=tag1;j<=tag2;j++) {
					JSONObject item=new JSONObject();
					item.put("key", keys[j]);
					item.put("value", pruview[0][j+2]);
					item.put("text", text[0][j+2]);
					list.put(item);
				}
				module.put("list", list);
				purv.put(i, module);
			}
			bku.put("purview", purv);
			bku.put("code", 200);
			return bku.toString();
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e);
		}
	}
	/**
	 * 函数说明：更新指定用户的权限表
	 * @param usrname	要更新权限的用户
	 * @param body	权限数据
	 * @throws Exception 404，用户不存在
	 */
	void UpdatePurview(String usrname, String body) throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
			JSONArray purview=new JSONArray(body);
			int row=dbd.check("sys_usr_purview", "usrname", usrname);
			if(row==0)throw new Exception("[info]404,用户"+usrname+"不存在！");
			
			for(int i=0;i<purview.length();i++) {
				JSONObject item=purview.getJSONObject(i);
				dbd.UpdateSQl("sys_usr_purview", row, item.getString("key"), item.getString("value"));
			}
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e);
		}
	}
	/**
	 *  函数说明：初始化权限表
	 * @throws Exception
	 */
	void InitPurview() throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
			int count=dbd.checknum("sys_usr_purview", "id", "id>0");
			for(int i=count;i>1;i--)dbd.DelSQl("sys_usr_purview", i, 1, 1);
//			初始化字段
			String[] colname= {"usrname"};		
			String[] record= {""};
//			同步用户表
			String[][] usrlist=dbd.readDB("sys_usrdb", "usrname", "id>0");
			if(!usrlist[0][0].equals("")) {
				for(int i=0;i<usrlist.length;i++) {
					record[0]=usrlist[i][0];
					dbd.AppendSQl("sys_usr_purview", colname, record, 1, 1);
				}
			}
		}catch(Throwable e) {
			logger.error(e.getMessage(),e);
			throw new Exception(e);
		}
	}
	/**[Function] 				获取http请求报文中的参数值
	 * @author para		请求报文中的参数序列
	 * @author key			预期的参数名
	 * @return [String]		返回参数结果，如果请求的参数序列为空，或者没有要查询的参数，返回“”，否则返回查询到的参数值
	 */
	String checkpara(Map<String,String[]> para,String key){
		PropertyConfigurator.configure(logconf);
		String ba="";		
		if(para.size()>0){
			try{
				String[] val=para.get(key);
				if(null!=val)ba=val[0];
			}catch(NullPointerException e){
				logger.error(e.getMessage());
			}
		}	
		return ba;
	}
}