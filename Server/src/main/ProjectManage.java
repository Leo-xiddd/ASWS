package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.*;

public class ProjectManage {
	User user=new User();	
	DBDriver dbd = new DBDriver();
	Inform inf=new Inform();
//	配置日志属性文件位置
	static String confpath=System.getProperty("user.dir").replace("\\bin", "");
	static String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	static String sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	Logger logger = Logger.getLogger(ProjectManage.class.getName());
	
	SimpleDateFormat sdf_y = new SimpleDateFormat("yyyy");
	SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf_full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat sdf_short = new SimpleDateFormat("yyMMddHHmmss");
	
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
		String usr=checkpara(Param,"user");
		String pl=checkpara(Param,"Produline");
		String product=checkpara(Param,"Product");
		String product_line=checkpara(Param,"product_line");
		String pid=checkpara(Param,"proj_id");
		String filter=checkpara(Param,"filter");
		try {
			switch(API){
			case "Addpl":	
				if(!pl.equals("")) {
					logger.info("创建新产品线...");
					AddPL(usr,pl);			//需要权限控制
					backvalue="200,ok";
				}				
				break;
			case "Listpl":   	
				logger.info("列出所有产品线...");
				return ListPL();	
			case "Deletepl":   		
				if((!pl.equals(""))) {
					logger.info("删除产品线"+pl);
					DelPL(usr,pl);		//需要权限控制
					backvalue="200,ok";
				}
				break;
			case "AddProduct":
				if(!product.equals("")&&(!product_line.equals(""))) {
					logger.info("创建新产品...");
					AddProduct(usr,product_line,product);			//需要权限控制
					backvalue="200,ok";
				}				
				break;
			case "GetProduts":   
				if(!product_line.equals("")) {
					logger.info("列出"+product_line+"下的所有产品...");
					return ListProduct(product_line);	
				}
			case "DelProduct":   		
				if(!product.equals("")&&(!product_line.equals(""))) {
					logger.info("删除产品"+product);
					DelProduct(usr,product,product_line);		//需要权限控制
					backvalue="200,ok";
				}
				break;
			case "AddProj":
				if(!body.equals("")) {
					logger.info("创建新项目...");
					AddProj(usr,body);	
					backvalue="200,ok";
				}				
				break;
			case "AppliProj":
				if(!pid.equals("")) {
					logger.info("项目"+pid+"再次申请立项...");
//					把项目状态改为待审批
					int row=dbd.check("sys_projects", "proj_id", pid);
//					判断用户是否项目负责人、产品经理或创建人
					String[][] projinf=dbd.readDB("sys_projects", "pm,responsor,creator", "id="+row);
					String[] usrinfo=user.Get(usr);
					if(!usrinfo[1].equals(projinf[0][0]) && !usrinfo[1].equals(projinf[0][1]) && !usr.equals(projinf[0][2])) {
						return "{\"code\":401,\"message\":\"只有项目创建者、负责人或产品经理才能重新申请立项！\"}";
					}				
					dbd.UpdateSQl("sys_projects", row, "proj_status", "待审批");
					informApprover(usr,pid);	
					backvalue="200,ok";
				}				
				break;
			case "ListProj":   	
				logger.info("列出所有测试项目...");
				String page_count=checkpara(Param,"page_count");
				String page_num=checkpara(Param,"page_num");
				return ListProj(filter,page_count,page_num);	
			case "TreeViewProj":
				logger.info("返回项目树...");
				return TreeView_Proj();	
			case "GetProj":   	
				if(!pid.equals("")) {
					logger.info("获取项目"+pid+"详情...");
					return GetProj(pid);
				}
				break;
			case "DownloadProjList":
				logger.info("下载项目列表...");
				return download_projlist(filter);			
			case "UpdateProj":   						
				if(!pid.equals("")&&(!body.equals(""))) {
					logger.info("更新项目"+pid+"...");
					UpdateProj(usr,pid,body);
					backvalue="200,ok";
				}
				break;
			case "CloseProj":   		
				if(!pid.equals("")) {
					logger.info("关闭项目"+pid);
					CloseProj(usr, pid);
					backvalue="200,ok";
				}
				break;	
			case "DelProj":   		
				if(!pid.equals("")) {
					logger.info("删除项目"+pid);
					DelProj(usr,pid);
					backvalue="200,ok";
				}
				break;
			case "ApproveProj":   
				String opt=checkpara(Param,"opt");
				if(!pid.equals("")&&(!opt.equals(""))) {
					logger.info("审批项目"+pid);
					ApproveProj(usr,pid,opt);
					backvalue="200,ok";
				}
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
	
	/**
	 * 函数说明：添加一个新的产品线（不能重名）
	 * @param usr			发起操作请求的用户名
	 * @param prodline	产品线名称
	 * @throws Exception
	 */
	void AddPL(String usr,String prodline)throws Exception{
		PropertyConfigurator.configure(logconf);
		String[] colname= {"product_line,pl_owner,pl_tag"};
		String[] record=new String[3];
		record[0]=prodline;
		try {
//			只有管理员才有权限创建新产品线
			String[] usrinfo=user.Get(usr);
			if(!usrinfo[5].equals("sysadmin"))throw new Exception("[info]401,无权操作");	
			
//			检查是否存在重名的产品线
			int row=dbd.check("sys_prodline", "product_line", prodline);
			if(row>0)throw new Exception("[info]409,已经存在相同名称的产品线，不能重复创建");
			
//			获取部门主管
			String[][] leader=dbd.readDB("sys_usrdb", "fullname", "dept1='"+prodline+"' and dept2='"+prodline+"' and role='dept_admin'");
			record[1]=leader[0][0];
			record[2]="";
			dbd.AppendSQl("sys_prodline", colname, record, 1, 1);	
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：列出所有产品线
	 * @throws Exception
	 */
	String ListPL() throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {			
			JSONObject produline=new JSONObject();
			JSONArray pl=new JSONArray();
			String[][] pls=dbd.readDB("sys_prodline", "product_line","id>0");
			if(!pls[0][0].equals("")) {			
				for(int i=0;i<pls.length;i++)pl.put(i, pls[i][0]);				
			}
			produline.put("code",200);
			produline.put("productlines",pl);
			return produline.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	
	/**
	 * 函数说明：删除指定的空产品线（指没有下属产品的产品线）
	 * @param usr				发起操作请求的用户名
	 * @param prodline		产品线名称
	 * @throws Exception
	 */
	void DelPL(String usr,String prodline)throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
//			只有系统管理员才有权限删除产品线名称
			String[] usrinfo=user.Get(usr);
			if(!usrinfo[5].equals("sysadmin"))throw new Exception("[info]401,无权操作");		
			
//			检查该产品线下是否存在产品，如果有则不允许删除
			int row=dbd.checknum("sys_products", "id", "product_line='"+prodline+"'");
			if(row>0)throw new Exception("[info]409,产品线["+prodline+"]不是一个空产品线，不能删除！");	
				
			row=dbd.check("sys_prodline", "product_line", prodline);
			if(row>0)dbd.DelSQl("sys_prodline", row, 1, 1);
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：为指定产品线添加新的产品（跨产品线不能有重名的产品）
	 * @param usr				操作用户
	 * @param prodline		指定的产品线
	 * @param product		要添加的新产品
	 * @throws Exception	 401，权限不足
	 */
	void AddProduct(String usr,String prodline,String product) throws Exception{
		PropertyConfigurator.configure(logconf);
		String[] colname= {"product_line","product"};
		String[] record=new String[2];
		record[0]=prodline;
		record[1]=product;
		try {		
//			只有部门管理员才有权限添加新产品
			String[] usrinfo=user.Get(usr);
			if(!usrinfo[5].equals("dept_admin"))throw new Exception("[info]401,无权操作");	
			
//			检查是否存在重名的产品
			int row=dbd.check("sys_products", "product", product);
			if(row>0)throw new Exception("[info]409,已经存在相同名称的产品，不能重复创建");
			
			dbd.AppendSQl("sys_products", colname, record, 1, 1);		
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：获取指定产品线下的产品
	 * @param prodline	指定的产品线
	 * @return		一个Json字符串，如{"prodline":"ABC","products":["A1","A2","A3"],"code":200}
	 * @throws Exception
	 */
	String ListProduct(String prodline)throws  Exception {
		PropertyConfigurator.configure(logconf);		
		try {			
			JSONObject produtlist=new JSONObject();
			JSONArray ps=new JSONArray();
			String[][] pls=dbd.readDB("sys_products", "product","product_line='"+prodline+"'");
			if(!pls[0][0].equals("")) {			
				for(int i=0;i<pls.length;i++)ps.put(i, pls[i][0]);				
			}
			produtlist.put("code",200);
			produtlist.put("prodline",prodline);
			produtlist.put("products",ps);
			return produtlist.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	
	/**
	 * 函数说明：删除指定的空产品（指没有下属项目的产品）
	 * @param usr	操作用户
	 * @param prodline	指定的产品线
	 * @param product	要删除的产品
	 * @throws Exception	 401, 权限不足
	 * @throws Exception	 409, 产品下还存在项目
	 */
	void DelProduct(String usr, String product, String prodline)throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
//			只有部门管理员才有权限删除产品线名称
			String[] usrinfo=user.Get(usr);
			if(!usrinfo[5].equals("dept_admin"))throw new Exception("[info]401,无权操作");			
			
//			检查该产品下是否存在项目，如果有则不允许删除
			int row=dbd.checknum("sys_projects", "id", "product_line='"+prodline+"' and product='"+product+"'");
			if(row>0)throw new Exception("[info]409,产品["+product+"]下还有项目，不能删除！");	
				
			row=dbd.check("sys_products", "product", product);
			if(row>0)dbd.DelSQl("sys_products", row, 1, 1);
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：添加新项目
	 * @param projdat		新项目数据，包含：项目名proj_name,产品线product_line,产品product,项目来源customer,优先级priority,计划完成时间plan_end_time,
	 * 负责人responsor,产品经理pm,项目目标goals
	 * @param usr			操作用户，项目创建人
	 * @throws Exception  409,存在重名项目
	 * @throws Exception  412,项目名包含非法字符
	 */
	void AddProj(String usr, String projdat) throws Exception{
		PropertyConfigurator.configure(logconf);		
//		项目表的字段包括："proj_id","proj_name","product_line","product","customer","proj_status","approver","priority","start_time","plan_end_time","responsor",
//		"pm","team_member","git","goals","comments","approtype","creator"
//		新项目中自动生成的字段有：proj_id，proj_status，approver，approtype
//		新项目中不用填写的字段有：start_time，team_member，git，comments
		String[] colname= {"proj_name","product_line","product","customer","priority","plan_end_time","responsor",	"pm","goals","proj_id","proj_status","approver","approtype","creator"};
		String[] record= new String[14];
		try {			
			JSONObject mpd=new JSONObject(projdat);
//			1. 获取所有字段内容
			for(int i=0;i<mpd.length();i++)record[i] =mpd.getString(colname[i]);
//			2. 判断项目名是否包含非法字符
			String[] validchar= {"[","]","(",")"};
			for(int i=0;i<validchar.length;i++) {
				if(record[0].indexOf(validchar[i])>-1)throw new Exception("[info]412,项目名不能包含字符"+validchar[i]+"，请从新提交！");
			}
//			3. 判断项目是否已存在同名项目
			int a=dbd.checknum("sys_projects", "id", "proj_name='"+record[0]+"' and proj_status<>'已删除'");
			if(a>0)throw new Exception("[info]409,已存在重名项目");
//			4. 生成项目编号、状态、审核人和审核事由			
			String[][] plinfo=dbd.readDB("sys_prodline", "*", "product_line='"+record[1]+"'");
			int proj_num=dbd.checknum("sys_projects", "id", "product_line='"+record[1]+"'")+1;
			Date now=new Date();	
			String n="0";
			if(proj_num<10)n=n+n+proj_num;
			else if(proj_num<100)n=n+proj_num;
			else n=""+proj_num;
			record[9]=plinfo[0][3]+sdf_y.format(now)+n;
			record[10]="待审批";
			record[11]=plinfo[0][2];
			record[12]="立项申请";
			record[13]=usr;
			dbd.AppendSQl("sys_projects", colname, record, 1, 1);	
			informApprover(usr,record[9]);
		} catch(JSONException e) {
			logger.error("项目数据内容不完整"+e.toString());
			throw new Exception("[info]412,项目数据内容不完整,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：根据ID获取项目信息
	 * @param proj_id		项目ID
	 * @return		Json格式的字符串，包括所有项目信息
	 * @throws Exception 404，未找到项目
	 */
	String GetProj(String proj_id)throws Exception{
		PropertyConfigurator.configure(logconf);	
		String[] col_st= {"proj_id","proj_name","product_line","product","customer","proj_status","approver","priority","start_time","plan_end_time","responsor","pm",
			"team_member","git","goals","comments","approtype"};
		String colsname= "proj_id,proj_name,product_line,product,customer,proj_status,approver,priority,start_time,plan_end_time,responsor,pm,team_member,git,goals,comments,approtype";
		try {
			String[][] projinfo=dbd.readDB("sys_projects", colsname, "proj_id='"+proj_id+"'");	
			JSONObject pf=new JSONObject();	
			if(projinfo[0][0].equals(""))throw new Exception("[info]404,未找到项目"+proj_id+"数据。");			
			for(int i=0;i<col_st.length;i++) {
				String vau="";
				if(col_st[i].equals("start_time") || col_st[i].equals("plan_end_time")) {
					if(projinfo[0][i]!=null)vau=sdf_ymd.format(sdf_ymd.parse(projinfo[0][i]));
				}
				else if(projinfo[0][i]!=null)vau=projinfo[0][i];
				pf.put(col_st[i], vau);
			}
			pf.put("code", 200);
			return pf.toString();
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：更改指定的项目信息
	 * @param usr	操作用户，只有项目负责人、产品经理、审批人和系统管理员才能修改项目
	 * @param pid	要更改项目的ID
	 * @param projdat		要更改的项目数据
	 * @throws Exception 412，提交的项目数据不完整
	 */
	void UpdateProj(String usr, String pid, String projdat)throws Exception{
		PropertyConfigurator.configure(logconf);		
		String[] colname= {"proj_name","product_line","product","customer","proj_status","priority","start_time","plan_end_time","responsor","pm","team_member","goals",
				"comments","approver","git"};
		String[] record= new String[15];
		try {			
			int row=dbd.check("sys_projects", "proj_id", pid);
//			判断用户是否项目负责人、产品经理、审批人或者系统管理员
			String[][] projinf=dbd.readDB("sys_projects", "pm,responsor,approver", "id="+row);
			String[] usrinfo=user.Get(usr);
			if(!usrinfo[5].equals("sysadmin") && !usrinfo[1].equals(projinf[0][0]) && !usrinfo[1].equals(projinf[0][1]) && !usrinfo[1].equals(projinf[0][2]))throw new Exception("[info]401,无权操作");
			
			JSONObject mpd=new JSONObject(projdat);
//			1. 获取所有字段内容
			for(int i=0;i<mpd.length();i++)record[i] =mpd.getString(colname[i]);	
//			2. 核对项目审核人
			String[][] plinfo=dbd.readDB("sys_prodline", "*", "product_line='"+record[1]+"'");
			record[13]=plinfo[0][2];
			String cols= "proj_name,product_line,product,customer,proj_status,priority,start_time,plan_end_time,responsor,pm,team_member,goals,comments,approver,git";
			String[][] pinf=dbd.readDB("sys_projects",cols, "id="+row);
			for(int i=0;i<colname.length;i++) {
				if(pinf[0][i]==null)pinf[0][i]="";
				if(!pinf[0][i].equals(record[i]))dbd.UpdateSQl("sys_projects", row, colname[i], record[i]);	
			}
		} catch(JSONException e) {
			logger.error("项目数据内容不完整"+e.toString());
			throw new Exception("[info]412,项目数据内容不完整,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}	
	}
	
	/**
	 * 函数说明：关闭指定项目
	 * @param usr	操作用户
	 * @param pid	要关闭的项目
	 * @throws Exception 401，操作人权限不足
	 * @throws Exception 404，项目不存在
	 */
	void CloseProj(String usr, String pid)throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {			
//			判断项目是否存在		
			int row=dbd.check("sys_projects", "proj_id", pid);
			if(row==0)throw new Exception("[info]404,项目["+pid+"]不存在");

//			判断用户是否项目负责人、产品经理、审批人或者系统管理员
			String[][] projinf=dbd.readDB("sys_projects", "pm,responsor,approver", "id="+row);
			String[] usrinfo=user.Get(usr);
			if(!usrinfo[5].equals("sysadmin") && !usrinfo[1].equals(projinf[0][0]) && !usrinfo[1].equals(projinf[0][1]) && !usrinfo[1].equals(projinf[0][2]))throw new Exception("[info]401,无权操作");
			
//			修改项目状态
			dbd.UpdateSQl("sys_projects", row, "proj_status", "已关闭");
		} catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：删除指定项目
	 * @param usr	操作用户
	 * @param pid	要删除的项目
	 * @throws Exception 401，操作人权限不足
	 * @throws Exception 404，项目不存在
	 */
	void DelProj(String usr, String pid)throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {			
//			判断项目是否存在		
			int row=dbd.check("sys_projects", "proj_id", pid);
			if(row==0)throw new Exception("[info]404,项目["+pid+"]不存在");

//			判断用户是否项目负责人、产品经理、审批人或者系统管理员
			String[][] projinf=dbd.readDB("sys_projects", "pm,responsor,approver", "id="+row);
			String[] usrinfo=user.Get(usr);
			if(!usrinfo[5].equals("sysadmin") && !usrinfo[1].equals(projinf[0][0]) && !usrinfo[1].equals(projinf[0][1]) && !usrinfo[1].equals(projinf[0][2]))throw new Exception("[info]401,无权操作");
			
//			删除项目,不实际删除，只改状态
			dbd.UpdateSQl("sys_projects", row, "proj_status", "已删除");
		} catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：审批项目，只有数据库指定的产品线审批人才能审批项目
	 * @param usr	操作人
	 * @param pid	要审批的项目ID
	 * @param opt	审批操作，y表示通过，n表示驳回
	 * @throws Exception  401，权限不足
	 * @throws Exception  404，项目不存在
	 */
	void ApproveProj(String usr, String pid, String opt)throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {			
//			判断项目是否存在		
			int row=dbd.check("sys_projects", "proj_id", pid);
			if(row==0)throw new Exception("[info]404,项目["+pid+"]不存在");

//			判断用户是否项目审批人
			String[][] projinf=dbd.readDB("sys_projects", "approver,proj_status,creator,proj_name", "id="+row);
			String[] usrinfo=user.Get(usr);
			if(!usrinfo[1].equals(projinf[0][0]))throw new Exception("[info]401,无权操作");
			
//			审批项目，通过则改状态为审批通过，否则置为审批不通过，并通知项目提交人
			if(opt.equals("y"))dbd.UpdateSQl("sys_projects", row, "proj_status", "审批通过");
			else {
				dbd.UpdateSQl("sys_projects", row, "proj_status", "审批驳回");
//				通知项目创建人
				String Receivers=projinf[0][2];
				Receivers=user.getmail(Receivers);
				String Sender=user.getmail(usr);
				String Subject="项目["+projinf[0][3]+"]立项申请驳回通知";
				String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>立项驳回通知</title>";
				Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，您提交的项目["+projinf[0][3]+
						"]立项申请被驳回，请与审批人沟通驳回原因</span><br><br><br><hr width='300px' align='left'></hr>";				
				Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
						+ "ASWS 研发工作协作平台</a></span></body></html>";			
				String Texttype="text/html;charset=UTF8";
				inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
			}
		} catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：读取项目列表，支持过滤，按项目创建顺序倒序排列
	 * @param filter		过滤器，支持用户自定义
	 * @param page_count	指定的每页条目数
	 * @param page_num	指定的页码
	 * @return		返回Json格式字符串，用proj_list引导的数组
	 * @throws Exception
	 */
	String ListProj(String filter, String page_count, String page_num)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {			
			if(filter.equals(""))filter="proj_status<>'已删除'";	
			else filter=filter+" and proj_status<>'已删除' order by id desc";
			
			JSONObject projlist=new JSONObject();	
			JSONArray ja=new JSONArray();
			String[][] projs=dbd.readDB("sys_projects", "*", filter);
			int num=0;
			if(!projs[0][0].equals(""))num=projs.length;
			int last_num=num;
			String[] tab_colname={"id","proj_id","proj_name","product_line","product","customer","proj_status","approver","priority","start_time","plan_end_time","responsor",
			"pm","team_member","git","goals","comments","approtype"};
//			1. 开始计算分页参数
			int first_num=0;
			projlist.put("page_count", page_count);
			projlist.put("page_num", page_num);
			projlist.put("total_num", num);
			if(num>0) {
				if(!page_count.equals("")) {
					int nn=Integer.parseInt(page_count);
//					如果每页记录数大于实际记录数，则全部返回，否则分页显示
					if(num>nn) {
//						如果页码为空，则默认返回第一页
						if(!page_num.equals("")) {
							int mm=Integer.parseInt(page_num);
//							如果页码*单页记录数大于实际记录则返回空，否则从新计算起始页
							if(mm<1 || (mm-1)*nn>=num) {
								projlist.put("proj_list", ja);
								return projlist.toString();
							}
							else {
								first_num=(mm-1)*nn;						
								if(num>mm*nn)last_num=first_num+nn;
							}
						}
						else	last_num=first_num+nn;
					}
				}
//				2. 开始读取项目数据
				int j=0;
				for(int  i=first_num;i<last_num;i++) {
					JSONObject jsb=new JSONObject();
					for(int k=0;k<tab_colname.length;k++) {					
						String vau="";
						if(tab_colname[k].equals("start_time") || tab_colname[k].equals("plan_end_time")) {
							if(projs[i][k]!=null)vau=sdf_ymd.format(sdf_ymd.parse(projs[i][k]));
						}
						else {
							if(projs[i][k]!=null)vau=projs[i][k];
						}
						jsb.put(tab_colname[k], vau);
					}
					ja.put(j,jsb);
					j++;
				}
			}					
			projlist.put("proj_num", num);
			projlist.put("proj_list", ja);
			projlist.put("code",200);
			return projlist.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}		
	}
	/**[Function] 				生成项目列表文件，并返回下载文件的地址
	 * @author filter		项目的过滤条件
	 * @return 		返回文件下载地址
	 */
	String download_projlist(String filter) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {			
			String LRP_path=confpath+"\\webapps\\ASWS\\datareport\\projlist.csv";	
			File tempfile = new File(LRP_path);
			if (tempfile.exists())tempfile.delete();
			BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (tempfile), "GB2312"));
			bw.write("编号,项目,产品线,产品,来源,状态,核准人,优先级,开始时间,计划结束时间,Git项目空间,负责人,产品经理");
			bw.newLine();
			
			if(filter.equals(""))filter="id>0";	
			else if(filter.indexOf("=")==-1)filter=filter.replace("*", "%");	
			String[][] dlg=dbd.readDB("sys_projects", "proj_id,proj_name,product_line,product,customer,proj_status,approver,priority,start_time,plan_end_time,git,responsor,pm", filter);
			if(!dlg[0][0].equals("")){
				String linr="";
				for(int i=0;i<dlg.length;i++) {
					if(null==dlg[i][8])dlg[i][8]="";
					if(null==dlg[i][9])dlg[i][9]="";
					if(!dlg[i][8].equals(""))dlg[i][8]=sdf_ymd.format(sdf_full.parse(dlg[i][8]));
					if(!dlg[i][9].equals(""))dlg[i][9]=sdf_ymd.format(sdf_full.parse(dlg[i][9]));
					linr=dlg[i][0]+","+dlg[i][1]+","+dlg[i][2]+","+dlg[i][3]+","+dlg[i][4]+","+dlg[i][5]+","+dlg[i][6]+","+dlg[i][7]+","+dlg[i][8]+","+dlg[i][9]+","+dlg[i][10]+
							","+dlg[i][11]+","+dlg[i][12];
					bw.write(linr);
					bw.newLine();
				}
			}		
			bw.flush();
			bw.close();
			return "{\"code\":200,\"DownloadFile_url\":\"projlist.csv\"}";
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}				
	}
	/**
	 * 函数说明：读取项目树型表，按照产品线-产品-项目的格式排列，不能筛选，返回所有审批通过的项目，包括已关闭的
	 * @return Json格式字符串，{"pls":[{"plname":"xxxx","ps":[{"pname":"xxx","projs":[{"id":"aa","name":"xxx"},{"id":"aa","name":"xxx"}]},{...}]},{"plname":"xxxx","ps":[]}]}
	 * @throws Exception
	 */
	String TreeView_Proj()throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {
			JSONObject projtrv=new JSONObject();	
			JSONArray pls= new JSONArray();
			String[][] produ_lines=dbd.readDB("sys_prodline", "product_line", "id>0");
			if(!produ_lines[0][0].equals("")) {
				for(int i=0;i<produ_lines.length;i++) {
					JSONObject produline=new JSONObject();	
					JSONArray ps= new JSONArray();
					produline.put("plname", produ_lines[i][0]);
					String[][] products=dbd.readDB("sys_products", "product", "product_line='"+produ_lines[i][0]+"'");
					if(!products[0][0].equals("")) {
						for(int j=0;j<products.length;j++) {
							JSONObject product=new JSONObject();	
							JSONArray projs= new JSONArray();
							product.put("pname", products[j][0]);
							String filter="product_line='"+produ_lines[i][0]+"' and product='"+products[j][0]+"' and proj_status<>'已删除' and proj_status<>'待审批' and proj_status<>'审批驳回';";
							String[][] projnames=dbd.readDB("sys_projects", "proj_id,proj_name",filter );
							if(!projnames[0][0].equals("")) {
								for(int k=0;k<projnames.length;k++) {
									JSONObject proj=new JSONObject();	
									proj.put("id", projnames[k][0]);
									proj.put("name", projnames[k][1]);
									projs.put(k, proj);
								}								
							}
							product.put("projs", projs);
							ps.put(j,product);
						}						
					}		
					produline.put("ps", ps);  
					pls.put(i, produline);
				}
			}			
			projtrv.put("pls", pls);
			projtrv.put("code", 200);
			return projtrv.toString();
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：项目立项时，用来通知审批人审批
	 * @param usr	操作的用户，只有项目负责人、产品经理或创建者才能重新提出申请
	 * @param pid	立项的项目编号
	 * @throws Exception
	 */
	void informApprover(String usr, String pid)throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {
			String[][] projinf=dbd.readDB("sys_projects", "approver,creator,proj_name", "proj_id='"+pid+"'");	
			String Receivers=projinf[0][0];
			Receivers=user.getmail(Receivers);
			String Sender=user.getmail(projinf[0][1]);
			String Subject="项目["+projinf[0][2]+"]立项申请";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>立项申请</title>";
			Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，由"+projinf[0][1]+"提交了项目["+projinf[0][2]+
					"]的立项申请，请登录AWS-项目管理模块进行审批。</span><br><br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";			
			String Texttype="text/html;charset=UTF8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
		}catch (Throwable e) {
			logger.error(e.toString(),e);
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
