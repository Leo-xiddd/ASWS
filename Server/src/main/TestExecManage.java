package main;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.*;

public class TestExecManage {
	User user=new User();	
	DBDriver dbd = new DBDriver();
	Inform inf=new Inform();
//	配置日志属性文件位置
	static String confpath=System.getProperty("user.dir").replace("\\bin", "");
	static String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	static String sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	Logger logger = Logger.getLogger(TestExecManage.class.getName());
	
	SimpleDateFormat sdf_y = new SimpleDateFormat("yyyy");
	SimpleDateFormat sdf_ym = new SimpleDateFormat("yyyy-MM");
	SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf_full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat sdf_full_2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	SimpleDateFormat sdf_index = new SimpleDateFormat("yyMMdd");
	
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
		String member_name=checkpara(Param,"member_name");
		String filter=checkpara(Param,"filter");
		String logID=checkpara(Param,"logID");
		String task_index=checkpara(Param,"et_index");
		try {
			switch(API){
			case "AddContractor":   				
				String in_time=checkpara(Param,"in_time");
				if(!member_name.equals("") && !usr.equals("") && !in_time.equals("")) {
					logger.info("添加新雇员"+member_name);
					AddContractor(usr,member_name,in_time);		//需要权限控制
					backvalue="200,ok";
				}
				break;				
			case "DelContractor":
				if(!member_name.equals("")&&(!usr.equals(""))) {
					logger.info("删除雇员"+member_name);
					DelContractor(usr,member_name);		//需要权限控制
					backvalue="200,ok";
				}
				break;	
			case "UpdateContractor":   
				if(!body.equals("")&&(!usr.equals(""))) {
					logger.info("更新雇员信息");
					UpdateContractor(usr,body);		//需要权限控制
					backvalue="200,ok";
				}
				break;		
			case "ListContractors":   		
				logger.info("获取雇员列表...");
				return ListContractors(filter);
			case "UpdateAttendance":	
				if(!body.equals("")&&(!usr.equals(""))) {
					logger.info("更新考勤信息...");
					UpdateAttendance(usr,body);			//需要权限控制
					backvalue="200,ok";
				}				
				break;
			case "GetAttendanceData": 
				String start_time=checkpara(Param,"st_date");
				String end_time=checkpara(Param,"end_date");
				if((!start_time.equals("")) && (!end_time.equals(""))) {
					logger.info("获取考勤数据");
					return GetAttendanceData(member_name,start_time,end_time);		
				}
			case "Update_ManPoReq":   	
				String st_time=checkpara(Param,"sttime");
				String ed_time=checkpara(Param,"edtime");
				String mp_req=checkpara(Param,"req");
				if((!usr.equals("")) && (!st_time.equals("")) && (!ed_time.equals("")) && (!mp_req.equals(""))) {
					logger.info("更新人员需求数据");
					Update_ManPoReq(usr,st_time,ed_time,mp_req);
					backvalue="200,ok";
				}
				break;	
			case "GetManPoReq":
				logger.info("获取人力资源信息...");
				return GetManPoReq();			
			case "Update_Daylog":  
				String opt=checkpara(Param,"opt");
				if(!body.equals("")&&(!usr.equals(""))&&(!opt.equals(""))) {
					if(opt.equals("new"))logger.info("新增日志项目...");
					else logger.info("更新日志项目...");
					Update_Daylog(usr,body,opt);
					backvalue="200,ok";
				}
				break;
			case "ListDlogs": 
				String page_count=checkpara(Param,"page_count");
				String page_num=checkpara(Param,"page_num");
				logger.info("列出所有日志记录");
				return ListDlogs(filter,page_count,page_num);	
			case "GetDlog":
				String logdate=checkpara(Param,"date");
				if(!member_name.equals("") && !logdate.equals("")) {
					logger.info("读取日志...");
					page_count="";
					page_num="";
					filter="name='"+member_name+"' and date like '"+logdate+"%'";
					return ListDlogs(filter,page_count,page_num);	
				}				
				break;
			case "DelDlog":
				if(!logID.equals("")) {
					logger.info("删除日志["+logID+"]...");
					DelDlog(usr,logID);	
					backvalue="200,ok";
				}				
				break;		
			case "DownloadDLG":
				logger.info("下载工作日志文件...");
				return download_worklogs(filter);			
			case "GetTCexeRate":   
				logger.info("列出最近12个月的测试用例执行效率...");
				return GetTCexeRate();	
			case "AddTask":   
				String proj=checkpara(Param,"proj");
				String subver=checkpara(Param,"subversion");
				if(!proj.equals("") && !subver.equals("") && !body.equals("")) {
					logger.info("为项目"+proj+"_"+subver+"添加子测试任务");
					AddTask(usr,proj,subver,body);	
					backvalue="200,ok";
				}
				break;
			case "ListTask":   
				logger.info("列出所有子测试任务");
				return ListTask(filter);	
			case "GetTask":   
				logger.info("列出指定子测试任务");
				return GetTask(task_index);	
			case "DelTask": 
				if(!task_index.equals("")) {
					logger.info("删除子测试任务"+task_index);
					DelTask(usr,task_index);
					backvalue="200,ok";
				}
				break;
			case "UpdateTask":   
				if(!task_index.equals("") && !body.equals("")) {
					logger.info("修改子测试任务"+task_index);
					UpdateTask(usr,task_index,body);
					backvalue="200,ok";
				}
				break;
			case "ChangeTaskStatus":   
				String status=checkpara(Param,"status");
				if(!task_index.equals("") && !status.equals("")) {
					logger.info("改变测试任务"+task_index+"状态为"+status);
					ChangeTaskStatus(usr,task_index,status);
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
	 * 函数说明：添加一个新雇员（不能重名）
	 * @param usr				发起操作请求的用户名，有权限限制
	 * @param name			人员姓名
	 * @param entry_date	入职时间
	 * @throws Exception 409，存在重名人员
	 */
	void AddContractor(String usr,String name, String entry_date)throws Exception{
		PropertyConfigurator.configure(logconf);
		String[] colname= {"name","status","sign_num","entry_date","quit_date","period"};
		String[] record=new String[colname.length];
		record[0]=name;
		record[1]="在职";
		record[2]="1";
		record[3]=entry_date;
		record[4]="2999-12-31";
		record[5]="0";
		try {
//			只有管理员、测试经理和TE才有权限创建新雇员
			String[] usrinfo=user.Get(usr);
			String[][] usr_role=dbd.readDB("sys_usr_role", "role", "usrname='"+usr+"'");
			if(!usrinfo[5].equals("sysadmin")&&!usr_role[0][0].equals("TM")&&!usr_role[0][0].equals("TE"))throw new Exception("[info]401,无权操作");	
			
//			检查是否存在重名的雇员
			int row=dbd.check("qa_contractors", "name", name);
			if(row>0)throw new Exception("[info]409,已经存在该员工，不能重复创建");
			
//			添加记录
			dbd.AppendSQl("qa_contractors", colname, record, 1, 1);	
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：删除指定的雇员
	 * @param usr			发起操作请求的用户名
	 * @param name		雇员名
	 * @throws Exception 401，操作权限不足
	 */
	void DelContractor(String usr,String name)throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
//			只有系统管理员或者测试经理和TE才有权限删除雇员
			String[] usrinfo=user.Get(usr);
			String[][] usr_role=dbd.readDB("sys_usr_role", "role", "usrname='"+usr+"'");
			if(!usrinfo[5].equals("sysadmin")&&!usr_role[0][0].equals("TM")&&!usr_role[0][0].equals("TE"))throw new Exception("[info]401,无权操作");			
			int row=dbd.check("qa_contractors", "name", name);
			if(row>0)dbd.DelSQl("qa_contractors", row, 1, 1);
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：更改雇员信息
	 * @param usr		操作用户，只有系统管理员、测试经理和TE才有操作权限
	 * @param body	新的雇员信息
	 * @throws Exception 401，操作权限不足
	 * @throws Exception 404，未找到用户数据
	 * @throws Exception 412，提交的项目数据不完整
	 */
	void UpdateContractor(String usr, String body)throws Exception{
		PropertyConfigurator.configure(logconf);		
		String[] colname= {"name","status","sign_num","entry_date","quit_date","period"};
		try {			
//			只有系统管理员或者测试经理和TE才有权限删除雇员
			String[] usrinfo=user.Get(usr);
			String[][] usr_role=dbd.readDB("sys_usr_role", "role", "usrname='"+usr+"'");
			if(!usrinfo[5].equals("sysadmin")&&!usr_role[0][0].equals("TM")&&!usr_role[0][0].equals("TE"))throw new Exception("[info]401,无权操作");		
			
			JSONObject info=new JSONObject(body);
			int row=dbd.check("qa_contractors", "name", info.getString("name"));
			if(row==0)throw new Exception("[info]404,未找到用户"+ info.getString("name")+"的数据");	
			for(int  i=0;i<colname.length;i++) {
				if(i==4) {
					String qt_date=info.getString(colname[i]);
					if(!qt_date.equals(""))dbd.UpdateSQl("qa_contractors", row, colname[i], info.getString(colname[i]));
				}
				else dbd.UpdateSQl("qa_contractors", row, colname[i], info.getString(colname[i]));
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
	 * 函数说明：列出所有雇员信息
	 * @param filter		过滤条件
	 * @return		JSON格式字符串，例如{"years":[2016,2017,...],"contractors":[{"name":"xx","status":"xx","sign_num":"2","entry_date":"2016-1-31","quit_date":"2016-3-1",
	 * "period":"2"},{},...],"code":200}
	 * @throws Exception
	 */
	String ListContractors(String filter) throws Exception{
		PropertyConfigurator.configure(logconf);		
		String[] colname= {"id","name","status","sign_num","entry_date","quit_date","period"};
		try {			
			JSONObject contrators=new JSONObject();
			JSONArray ctor_list=new JSONArray();
			JSONArray years=new JSONArray();
			if(filter.equals(""))filter="id>0 order by quit_date desc";
			else filter=filter+" order by entry_date desc";
			String[][] dats=dbd.readDB("qa_contractors", "*",filter);
			if(!dats[0][0].equals("")) {			
				int firyear=Integer.parseInt(sdf_y.format(new Date()));
				for(int i=0;i<dats.length;i++) {
					int tempyear=Integer.parseInt(sdf_y.format(sdf_full.parse(dats[i][4])));
					if(tempyear<firyear)firyear=tempyear;
					
					JSONObject info=new JSONObject();
					dats[i][0]="contractor"+dats[i][0];
					for(int k=0;k<colname.length;k++) {
						if(k==4)dats[i][k]=sdf_ymd.format(sdf_ymd.parse(dats[i][k]));
						if(k==5) {
							dats[i][k]=sdf_ymd.format(sdf_ymd.parse(dats[i][k]));
							if(dats[i][k].equals("2999-12-31"))dats[i][k]="";
						}
						info.put(colname[k], dats[i][k]);
					}
					ctor_list.put(i, info);				
				}			
				int curyear=Integer.parseInt(sdf_y.format(new Date()));
				for(int k=curyear;k>=firyear;k--)years.put(curyear-k, k);
			}
			contrators.put("code",200);
			contrators.put("years",years);
			contrators.put("contractors",ctor_list);
			return contrators.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}

	/**
	 * 函数说明：更改考勤信息
	 * @param usr		操作用户，只有系统管理员、测试经理和TE才有操作权限
	 * @param body	考勤数据
	 * @throws Exception 401，操作权限不足
	 * @throws Exception 412，提交的数据不完整
	 */
	void UpdateAttendance(String usr, String body)throws Exception{
		PropertyConfigurator.configure(logconf);		
		String[] colname= {"name","year","date","attendtype","attendtime","man_day"};
		String[] cols={"name","date"};
		String[] record=new String[colname.length];
		String[] filts=new String[2];
		try {			
//			只有系统管理员或者测试经理和TE才有权限更改雇员考勤
			String[] usrinfo=user.Get(usr);
			String[][] usr_role=dbd.readDB("sys_usr_role", "role", "usrname='"+usr+"'");
			if(!usrinfo[5].equals("sysadmin")&&!usr_role[0][0].equals("TM")&&!usr_role[0][0].equals("TE"))throw new Exception("[info]401,无权操作");		
			
			JSONObject info=new JSONObject(body);
			filts[0]=info.getString("contractor");
			filts[1]=info.getString("attend_date");
			int row=dbd.check("qa_attendance", cols, filts);
			
			record[0]=info.getString("contractor");			
			record[2]=info.getString("attend_date");
			record[1]=sdf_y.format(sdf_ymd.parse(record[2]));
			record[3]=info.getString("attendtype");
			record[4]=info.getString("attend_time");
			record[5]=info.getString("man_day");
			if(row==0) dbd.AppendSQl("qa_attendance", colname, record, 1, 1);
			else {
				dbd.UpdateSQl("qa_attendance", row, "attendtype", record[3]);
				dbd.UpdateSQl("qa_attendance", row, "attendtime", record[4]);
				dbd.UpdateSQl("qa_attendance", row, "man_day", record[5]);
			}
//			获取考勤记录中当天所有人的记录并求和				
			float newValue=0;
			String[][] mandays=dbd.readDB("qa_attendance", "man_day,name", "date='"+record[2]+"' order by name");
			if(!mandays[0][0].equals("")) {
				for(int i=0;i<mandays.length;i++) {
					if(!mandays[i][0].equals(""))newValue=newValue+Float.parseFloat(mandays[i][0]);
				}
			}
//			同步人员需求表
			String[][] manpo=dbd.readDB("qa_manpower", "*", "date='"+record[2]+"'");
			if(manpo[0][0].equals("")) {
//				新增记录
				String[] col_manpo= {"date","man_day","man_req"};
				String[] recod_mapo=new String[col_manpo.length];
				recod_mapo[0]=record[2];
				recod_mapo[1]= ""+newValue;
				recod_mapo[2]="0";
				dbd.AppendSQl("qa_manpower", col_manpo, recod_mapo, 1, 1);
			}
			else {
//				更新记录
				row=Integer.parseInt(manpo[0][0]);
				dbd.UpdateSQl("qa_manpower", row, "man_day", ""+newValue);
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
	 * 函数说明：获取考勤数据（整体）或考勤状态（个人）
	 * @param name	要获取考勤的用户，如果参数为空表示所有用户
	 * @param sttime	考勤起始时间
	 * @param edtime	考勤截止时间
	 * @return		JSON格式数组，如：{"attendance":[{"datetime":"2017-6-1","value":"1"},{}...],"maxvalue":6};
	 * @throws Exception	
	 */
	String GetAttendanceData(String name,String sttime,String edtime) throws Exception{
		PropertyConfigurator.configure(logconf);
		try {		
			JSONObject attendance=new JSONObject();
			JSONArray attd_dats=new JSONArray();
			int maxv=0;
			int tempn=0;
			String filter="date>='"+sttime+"' and date<='"+edtime+"'";
//			name不为空，表示获取个人考勤状态
			if(!name.equals("")) {
				filter=filter+" and name='"+name+"' order by date asc";
				String[][] attend=dbd.readDB("qa_attendance", "date,attendtype", filter);	
				if(!attend[0][0].equals("")) {
					for(int i=0;i<attend.length;i++) {
						JSONObject dats=new JSONObject();
						attend[i][0]=sdf_ymd.format(sdf_full.parse(attend[i][0]));
						dats.put("datetime", attend[i][0]);
						dats.put("value", attend[i][1]);
						attd_dats.put(i, dats);
					}
				}
			}
//			name为空，表示获取整体考勤状态
			else {
				filter=filter+" order by date asc";
				String[][] attend=dbd.readDB("qa_manpower", "date,man_day", filter);	
				if(!attend[0][0].equals("")) {
					for(int i=0;i<attend.length;i++) {
						JSONObject dats=new JSONObject();
						attend[i][0]=sdf_ymd.format(sdf_full.parse(attend[i][0]));
						dats.put("datetime", attend[i][0]);						
						tempn=(int) Math.ceil(Float.parseFloat(attend[i][1]))*8;
						dats.put("value", ""+tempn);
						if(tempn>maxv)maxv=tempn;
						attd_dats.put(i, dats);
					}
				}			
			}
			attendance.put("code", 200);
			attendance.put("maxvalue", maxv);
			attendance.put("attendance", attd_dats);
			return attendance.toString();
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：更新人员需求数据
	 * @param usr			操作用户
	 * @param sttime		起始时间
	 * @param edtime		截止时间
	 * @param req			人员需求数
	 * @throws Exception 401，权限不足
	 */
	void Update_ManPoReq(String usr,String sttime,String edtime,String req)throws  Exception { 
		PropertyConfigurator.configure(logconf);		
		try {	
			String[] colname= {"date","man_req"};
			String[] record=new String[colname.length];
			record[1]=req;
//			只有系统管理员或者测试经理和TE才有权限更新人员需求
			String[] usrinfo=user.Get(usr);
			String[][] usr_role=dbd.readDB("sys_usr_role", "role", "usrname='"+usr+"'");
			if(!usrinfo[5].equals("sysadmin")&&!usr_role[0][0].equals("TM")&&!usr_role[0][0].equals("TE"))throw new Exception("[info]401,无权操作");
//			获取时间差
			long time1=sdf_ymd.parse(sttime).getTime();
			long time2=sdf_ymd.parse(edtime).getTime();
			int durtime=(int)(time2-time1)/(1000*3600*24)+1;
			Date tt=sdf_ymd.parse(sttime);
			Calendar calendar=new GregorianCalendar(); 
			int row=0;
			for(int i=0;i<durtime;i++) {
				record[0]=sdf_ymd.format(tt);
				row=dbd.check("qa_manpower", "date", record[0]);
				if(row>0) dbd.UpdateSQl("qa_manpower", row, "man_req", req);
				else dbd.AppendSQl("qa_manpower", colname, record, 1, 1);
			    calendar.setTime(tt); 
			    calendar.add(Calendar.DATE,1);
			    tt=calendar.getTime();
			}		
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	
	/**
	 * 函数说明：获取人力资源信息，包括时间、需求人数，实际出勤人天
	 * @return		返回JSON格式字符串，如{"maxreq":6,"ManPoReq":[{"datetime":"2017-01-01","attend":"3","req":"5",},{}...]}
	 */
	String GetManPoReq()throws Exception{
		PropertyConfigurator.configure(logconf);
		try {
			JSONObject ManPo=new JSONObject();
			JSONArray dats=new JSONArray();
			JSONArray times=new JSONArray();
			int maxv=0;
			int temp1=0;
			String[][] MP_dats=dbd.readDB("qa_manpower", "*", "id>0 order by date desc");
			if(!MP_dats[0][0].equals("")) {
				for(int i=0;i<MP_dats.length;i++) {
					JSONObject temp_dat=new JSONObject();
					temp_dat.put("datetime", sdf_ymd.format(sdf_ymd.parse(MP_dats[i][1])));
					times.put(i, sdf_ymd.format(sdf_ymd.parse(MP_dats[i][1])));
					temp_dat.put("attend", MP_dats[i][2]);
					temp_dat.put("req", MP_dats[i][3]);
					dats.put(i, temp_dat);
					temp1=Integer.parseInt(MP_dats[i][3]);
					if(maxv<temp1)maxv=temp1;
				}
			}
			ManPo.put("times", times);
			ManPo.put("code", 200);
			ManPo.put("maxreq", maxv);
			ManPo.put("ManPoReq", dats);
			return ManPo.toString();
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：添加或更新日志（任何人可以添加新的，但是只能本人或管理员、TM、TE修改）
	 * @param 	daylogs	 	日志数据
	 * @param 	usr			操作用户
	 * @param 	opt			操作类型，new-新增，update-更新
	 * @throws Exception  401,权限不足
	 */
	void Update_Daylog(String usr, String daylogs, String opt) throws Exception{
		PropertyConfigurator.configure(logconf);		
		String[] colname= {"create_time","date","name","attendtime","worktype","content","testproj","projversion","tcexe","newbug","regbug"};
		String[] record= new String[colname.length];
		try {			
			JSONObject mpd=new JSONObject(daylogs);
//			update策略：查询某人某日是否有日志，如果有，删除全部，从新添加新的
			record[0]=sdf_full.format(new Date());
			record[1]=mpd.getString("logdate");
			record[2]=mpd.getString("contractor");
			record[3]=mpd.getString("attendance");
			JSONArray logs=mpd.getJSONArray("logs");
			
//			只有本人、系统管理员或者测试经理和TE才有权限更新
			String[] usrinfo=user.Get(usr);
			String[][] usr_role=dbd.readDB("sys_usr_role", "role", "usrname='"+usr+"'");
			if(!usrinfo[5].equals("sysadmin")&&!usrinfo[1].equals(record[2])&&!usr_role[0][0].equals("TM")&&!usr_role[0][0].equals("TE"))throw new Exception("[info]401,无权操作");
			
			String[][] rows=dbd.readDB("qa_worklogs", "id", "name='"+record[2]+"' and date='"+record[1]+"'");	
			if(!rows[0][0].equals("") && opt.equals("update")) {				
				for(int i=rows.length;i>0;i--)dbd.DelSQl("qa_worklogs", Integer.parseInt(rows[i-1][0]), 1, 1);
			}
//			如果是新建日志则同步考勤时间	
			else if(rows[0][0].equals("") && opt.equals("new")) {
				JSONObject attend=new JSONObject();
				attend.put("contractor", record[2]);
				attend.put("attend_date", record[1]);
				attend.put("attend_time", record[3]);
				attend.put("man_day", mpd.getString("man_day"));
				attend.put("attendtype", mpd.getString("attendtype"));
				UpdateAttendance("admin",attend.toString());
			}
//			添加新的日志记录
			for(int i=0;i<logs.length();i++) {
				for(int k=4;k<colname.length;k++)record[k]=logs.getJSONObject(i).getString(colname[k]);
				dbd.AppendSQl("qa_worklogs", colname, record, 1, 1);	
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
	 * 函数说明：读取日志列表，支持过滤查找，按日期和创建时间顺序倒序排列
	 * @param filter				过滤器，支持用户自定义
	 * @param page_count	指定的每页条目数
	 * @param page_num	指定的页码
	 * @return		返回Json格式字符串，如
	 * @throws Exception
	 */
	String ListDlogs(String filter, String page_count, String page_num)throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {			
			if(filter.equals(""))filter="id>0";	
			else if(filter.indexOf("=")==-1)filter=filter.replace("*", "%");
			filter=filter+" order by date desc, name desc, create_time desc";	
			String filter1=filter;
			if(!page_count.equals("")&&!page_num.equals("")) {
				int pg_count=Integer.parseInt(page_count);
				int pg_num=Integer.parseInt(page_num);
				int past_itemnum=pg_count*(pg_num-1);
				filter=filter+" limit "+past_itemnum+","+pg_count;
			}
			JSONObject daylog=new JSONObject();	
			JSONArray logs=new JSONArray();
			String[][] dlg=dbd.readDB("qa_worklogs", "*", filter);
			int num=dbd.checknum("qa_worklogs", "id", filter1);
			if(!dlg[0][0].equals("")){
				String[] dlg_colname={"id","create_time","date","name","attendtime","worktype","content","testproj","projversion","tcexe","newbug","regbug"};
				for(int i=0;i<dlg.length;i++) {
					JSONObject dlog=new JSONObject();	
					dlg[i][0]="dlog_"+dlg[i][0];
					dlg[i][2]=sdf_ymd.format(sdf_full.parse(dlg[i][2]));
					for(int k=0;k<dlg_colname.length;k++)dlog.put(dlg_colname[k], dlg[i][k]);
					logs.put(i, dlog);
				}
			}		
			daylog.put("total_num", num);
			daylog.put("Dlog_list", logs);
			daylog.put("code",200);
			return daylog.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}		
	}
	
	/**
	 * 函数说明：删除指定日志条目
	 * @param usr		操作用户
	 * @param logid	要删除的日志ID
	 * @throws Exception 401，操作人权限不足
	 * @throws Exception 404，日志条目不存在
	 */
	void DelDlog(String usr, String logid)throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {			
			String log_id=logid.replace("dlog_", "");
			int row=Integer.parseInt(log_id);			
			String[][] record=dbd.readDB("qa_worklogs", "name", "id="+row);
//			只有本人、系统管理员或者测试经理和TE才有权限删除
			String[] usrinfo=user.Get(usr);
			String[][] usr_role=dbd.readDB("sys_usr_role", "role", "usrname='"+usr+"'");
			if(!usrinfo[5].equals("sysadmin")&&!usrinfo[1].equals(record[0][0])&&!usr_role[0][0].equals("TM")&&!usr_role[0][0].equals("TE"))throw new Exception("[info]401,无权操作");	
			
			dbd.DelSQl("qa_worklogs", row, 1, 1);
		} catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}
	
	/**[Function] 				获取最近12个月（小于12个月返回所有）的测试用例执行效率
	 * @return [String]		返回JSON字符串，如{"times":['2017-1','2017-2',...],"TCexeRate":[23,...]}
	 * @exception
	 */
	String GetTCexeRate() throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {		
			Calendar cal = Calendar.getInstance();
//			查询日志表中最早日志的时间
			String filter="worktype='测试任务' order by date limit 0,1";
			String[][] dd=dbd.readDB("qa_worklogs", "date", filter);
			if(!dd[0][0].equals("")) cal.setTime(sdf_ymd.parse(dd[0][0])); 
			Date firday=cal.getTime();
			
//			查询用例效率表中的最新记录月份
			String[][] last_tcexe=dbd.readDB("qa_month_tcexe", "month", "id>0 order by month desc limit 0,1");
			if(!last_tcexe[0][0].equals("") && sdf_ymd.parse(last_tcexe[0][0]).getTime()>firday.getTime()) {
				cal.setTime(sdf_ymd.parse(last_tcexe[0][0]));
				firday=cal.getTime();
			}

//			设置获取日志数据的起始月
			String log_month=sdf_ym.format(firday);
			int days=0;
			int tc_num=0;
			String mons="";
			String tcexe="";
			Date lasttime=new Date();
			while(cal.getTime().getTime()<lasttime.getTime()) {
				String fday="";
				String loger="";
				mons=mons+log_month+"-01,";
//				获取当月所有测试任务日志, 按日期和人员排列
				filter="date like'"+log_month+"%' and worktype='测试任务' order by date,name";	
				String[][] tcs=dbd.readDB("qa_worklogs", "date,name,tcexe", filter);
				if(!tcs[0][0].equals("")) {
					for(int j=0;j<tcs.length;j++) {
						tc_num=tc_num+Integer.parseInt(tcs[j][2]);
						String temp=sdf_ymd.format(sdf_ymd.parse(tcs[j][0]));
						if(!fday.equals(temp)) {
							fday=temp;
							loger=tcs[j][1];
							days++;							
						}
						else if(!loger.equals(tcs[j][1])) {
							loger=tcs[j][1];
							days++;	
						}
					}
					tcexe=tcexe+tc_num/days+",";
				}
				else {
					tcexe=tcexe+"0,";
				}
				tc_num=0;
				days=0;
				cal.add(Calendar.MONTH, 1);
				log_month=sdf_ym.format(cal.getTime());		
			}
//			同步用例执行效率数据表
			String[] colname= {"month","tcexe"};
			String[] record=new String[colname.length];
			if(!mons.equals("") && !tcexe.equals("")) {
				String[] mos=mons.split(",");
				String[] tc_item=tcexe.split(",");
				for(int i=0;i<mos.length;i++) {
					int row=dbd.check("qa_month_tcexe", "month", mos[i]);
					if(row>0) dbd.UpdateSQl("qa_month_tcexe", row, "tcexe", tc_item[i]);
					else {
						record[0]= mos[i];
						record[1]=tc_item[i];
						dbd.AppendSQl("qa_month_tcexe", colname, record, 1, 1);
					}
				}
			}
//			获取用例效率表，返回
			JSONObject TCR=new JSONObject();	
			JSONArray tcexe_rate=new JSONArray();
			JSONArray tcexe_time=new JSONArray();
			String[][] tcrate=dbd.readDB("qa_month_tcexe", "month,tcexe", "id>0 order by month desc");
			if(!tcrate[0][0].equals("")) {
				int count=tcrate.length;
				if(count>12)count=12;
				int tag=count-1;
				for(int i=0;i<count;i++) {
					tcrate[i][0]=sdf_ym.format(sdf_ymd.parse(tcrate[i][0]));
					tcexe_time.put(tag, tcrate[i][0]);
					tcexe_rate.put(tag, tcrate[i][1]);
					tag--;
				}
			}
			TCR.put("times", tcexe_time);
			TCR.put("TCexeRate", tcexe_rate);
			TCR.put("code",200);
			return TCR.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}		
	}
	/**[Function] 				生成日志文件，并返回下载日志文件的地址
	 * @author filter		日志的过滤条件
	 * @return 		返回文件下载地址
	 */
	String download_worklogs(String filter) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {			
			String LRP_path=confpath+"\\webapps\\ASWS\\datareport\\worklog.csv";	
			File tempfile = new File(LRP_path);
			if (tempfile.exists())tempfile.delete();
			BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (tempfile), "GB2312"));
			bw.write("日期,人员,任务类别,时段,项目,版本,执行用例,新报Bug,回归Bug,说明");
			bw.newLine();
			
			if(filter.equals(""))filter="id>0";	
			else if(filter.indexOf("=")==-1)filter=filter.replace("*", "%");
			filter=filter+" order by date desc, create_time desc";			
			String[][] dlg=dbd.readDB("qa_worklogs", "date,name,worktype,attendtime,testproj,projversion,tcexe,newbug,regbug,content", filter);
			if(!dlg[0][0].equals("")){
				String linr="";
				for(int i=0;i<dlg.length;i++) {
					dlg[i][0]=sdf_ymd.format(sdf_full.parse(dlg[i][0]));
					linr=dlg[i][0]+","+dlg[i][1]+","+dlg[i][2]+","+dlg[i][3]+","+dlg[i][4]+","+dlg[i][5]+","+dlg[i][6]+","+dlg[i][7]+","+dlg[i][8]+","+dlg[i][9];
					bw.write(linr);
					bw.newLine();
				}
			}		
			bw.flush();
			bw.close();
			return "{\"code\":200,\"DownloadFile_url\":\"worklog.csv\"}";
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}				
	}
	/**
	 * [Function] 	添加新的子任务
	 * @param usr		操作用户，只有管理员、测试经理和工程师可以添加
	 * @param proj		要添加任务的项目
	 * @param subver	要添加任务的项目版本
	 * @param body	要添加的任务信息
	 * @throws Exception
	 */
	void AddTask(String usr, String proj, String subver, String body) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
//			权限判断
			String[] usrinfo=user.Get(usr);
			String titl=user.GetTitle(usr);
			if(!usrinfo[5].equals("sysadmin") && !titl.equals("TM") && !titl.equals("TE"))throw new Exception("[info]401,无权操作");	
			
//			添加测试任务
			String[] colname= {"et_index","proj","subversion","create_time","status","name","executor","endtime_exp","precondition","content"};
			String[] record=new String[colname.length];
			record[1]=proj;
			record[2]=subver;
			record[3]=sdf_full.format(new Date());
			record[4]="进行中";
			record[0]="exet"+sdf_index.format(new Date());
			int count=dbd.checknum("qa_tasks", "et_index", "et_index like '"+record[0]+"%'");
			record[0]=record[0]+(count+1);
			
			JSONObject task=new JSONObject(body);
			for(int i=5;i<colname.length;i++)record[i]=task.getString(colname[i]);
			dbd.AppendSQl("qa_tasks", colname, record, 1, 1);
			String taskcont="<table cellspacing=0 cellpadding=0 style='margin-top:15px;font-family:微软雅黑;font-size: 12px;'><tbody>";
			taskcont=taskcont+"<tr height='20px'><td><b>任务：</b>"+record[5]+"</td></tr>";
			taskcont=taskcont+"<tr height='20px'><td><b>期望完成时间：</b>"+record[7]+"</td></tr>";
			taskcont=taskcont+"<tr height='20px'><td><b>任务执行的必要条件：</b></td></tr>";
			taskcont=taskcont+"<tr><td style='padding-left:20px;line-height:20px;'>"+record[8]+"</td></tr>";
			taskcont=taskcont+"<tr height='20px'><td><b>任务描述：</b></td></tr>";
			taskcont=taskcont+"<tr><td style='padding-left:20px;line-height:20px;'>"+record[9]+"</td></tr>";
			taskcont=taskcont+"</tbody></table>";
//			发邮件通知任务执行人
			String Receivers=record[6];
			Receivers=user.getmail(Receivers);
			String Sender=user.getmail(usr);
			String Subject="【测试任务通知】您已被分配执行项目"+proj+"_"+subver+"的任务，请查看邮件详情";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>任务通知</title></head>";
			Context=Context+"\n"+"<body style=\"font-family:微软雅黑;font-size: 12px;\">";
			Context=Context+"<span>您好，您已被分配执行项目"+proj+"_"+subver+"的测试任务，请查看如下详情：</span><br>";
			Context=Context+taskcont;
			Context=Context+"<br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";			
			String Texttype="text/html;charset=UTF8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * [Function]	列出指定的子任务
	 * @param filter		过滤条件
	 * @return		JSON格式的字符串，如{"code":200, "task":[{"proj":"abc", "subversion":"1.0.1.2", "responsor":"tom", "starttime":"2017-12-01 12:30:00",
	 * 				 "subtasks":[{"et_index":"","name":"","executor":"","create_time":"","endtime_exp":"","status":"进行中","precondition":"","content":""},...]},{},...]}
	 * @throws Exception
	 */
	String ListTask(String filter) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			JSONObject tasklist=new JSONObject();
			JSONArray tasks=new JSONArray();
			String[] colname={"et_index","create_time","status","name","executor","endtime_exp","precondition","content"};
			String[][] testtasks=dbd.readDB("sys_test_task", "proj,subversion,responsor,starttime", "teststatus='running'");
			if(!filter.equals("")) {
				if(filter.indexOf("=")==-1)filter=filter.replace("*", "%");
				filter=" and "+filter;
			}
			filter=filter+" order by executor";
			String filt="";			
			if(!testtasks[0][0].equals("")) {
				for(int i=0;i<testtasks.length;i++) {
					JSONObject tt=new JSONObject();
					tt.put("proj", testtasks[i][0]);
					tt.put("subversion", testtasks[i][1]);
					tt.put("responsor", testtasks[i][2]);
					testtasks[i][3]=sdf_ymd.format(sdf_full.parse(testtasks[i][3]));
					tt.put("starttime", testtasks[i][3]);
					JSONArray subtasks=new JSONArray();
					filt="proj='"+testtasks[i][0]+"' and subversion='"+testtasks[i][1]+"'"+filter;
					String[][] subtt=dbd.readDB("qa_tasks", "et_index,create_time,status,name,executor,endtime_exp,precondition,content", filt);
					if(!subtt[0][0].equals("")) {
						for(int j=0;j<subtt.length;j++) {
							JSONObject stt=new JSONObject();
							for(int k=0;k<colname.length;k++) {
								if(k==1)subtt[j][k]=sdf_full.format(sdf_full.parse(subtt[j][k]));
								else if(k==5)subtt[j][k]=sdf_full_2.format(sdf_full.parse(subtt[j][k]));
								stt.put(colname[k], subtt[j][k]);
							}
							subtasks.put(j, stt);
						}
					}
					tt.put("subtasks", subtasks);
					tasks.put(i, tt);
				}
			}
			tasklist.put("task", tasks);
			tasklist.put("code", 200);
			return tasklist.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * [Function]获取指定的测试任务
	 * @param task_index
	 * @return		JSON格式字符串，如{"code":200,"proj":"xxx","subversion":"1.2.1","executor":"tom","name":"xxxxx","endtime_exp":"2017-07-21 12:30","status":"已完成",
	 * 												"precondition":"xxxxx","content":"xxxxxxx"}
	 * @throws Exception 404, 测试任务未找到
	 */
	String GetTask(String task_index) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			String[] colname= {"proj","subversion","executor","name","endtime_exp","status","precondition","content"};
			String[][] tasks=dbd.readDB("qa_tasks", "proj,subversion,executor,name,endtime_exp,status,precondition,content", "et_index='"+task_index+"'");
			if(tasks[0][0].equals(""))throw  new Exception("[info]404, 测试任务"+task_index+"不存在！");
			JSONObject task=new JSONObject();
			for(int i=0;i<colname.length;i++) {
				if(i==4) tasks[0][i]=sdf_full_2.format(sdf_full.parse( tasks[0][i]));
				task.put(colname[i], tasks[0][i]);
			}
			task.put("code", 200);
			return task.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * [Function]	删除测试任务，只有管理员、测试经理和工程师可以删除
	 * @param usr				操作用户的账户
	 * @param task_index	要删除的任务编号
	 * @throws Exception 404，任务不存在
	 */
	void DelTask(String usr, String task_index) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
//			权限判断
			String[] usrinfo=user.Get(usr);
			String titl=user.GetTitle(usr);
			if(!usrinfo[5].equals("sysadmin") && !titl.equals("TM") && !titl.equals("TE"))throw new Exception("[info]401,无权操作");	
//			删除任务
			String[][] info=dbd.readDB("qa_tasks", "id,proj,subversion,executor", "et_index='"+task_index+"'");
			if(info[0][0].equals(""))throw new Exception("[info]404, 测试任务"+task_index+"不存在！");
			dbd.DelSQl("qa_tasks", Integer.parseInt(info[0][0]), 1, 1);
//			发邮件通知任务执行人
			String Receivers=info[0][3];
			Receivers=user.getmail(Receivers);
			String Sender=user.getmail(usr);
			String Subject="【测试任务撤销通知】您被分配的测试任务"+task_index+"已撤销。";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>任务通知</title></head>";
			Context=Context+"\n"+"<body style='font-family:微软雅黑;font-size: 12px;'><span>您好，您之前被分配的项目"+info[0][1]+"_"+info[0][2]+
					"任务[编号："+task_index+"]已经被撤销，请知</span><br><br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";			
			String Texttype="text/html;charset=UTF8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * [Function]	修改测试任务内容，只有管理员、测试经理和工程师可以修改
	 * @param usr				操作用户的账户
	 * @param task_index	要修改的任务编号
	 * @param body			新的任务信息
	 * @throws Exception 404，任务不存在
	 * @throws Exception 412，任务信息参数错误
	 */
	void UpdateTask(String usr, String task_index, String body) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
//			权限判断
			String[] usrinfo=user.Get(usr);
			String titl=user.GetTitle(usr);
			if(!usrinfo[5].equals("sysadmin") && !titl.equals("TM") && !titl.equals("TE"))throw new Exception("[info]401,无权操作");	
//			更新任务  
			String[][] info=dbd.readDB("qa_tasks", "id,proj,subversion,name,executor,endtime_exp,precondition,content", "et_index='"+task_index+"'");
			if(info[0][0].equals(""))throw new Exception("[info]404, 测试任务"+task_index+"不存在！");
			int row=Integer.parseInt(info[0][0]);
			String[] colname= {"name","executor","endtime_exp","precondition","content"};
			String[] record=new String[colname.length];
			JSONObject subtask=new JSONObject(body);
			int tag=0;   //用来判断是否执行人发生变化
			String email_cont="";		//用来记录变化内容
			for(int i=0;i<colname.length;i++) {
				record[i]=subtask.getString(colname[i]);
				if(!record[i].equals(info[0][i+3])) {
					if(i==1)tag=1;
					email_cont=email_cont+"<span><b>"+colname[i]+"</b>从 "+info[0][i+3]+" 变更为 "+record[i]+",</span><br>";
					dbd.UpdateSQl("qa_tasks", row, colname[i], record[i]);
				}
			}		
//			发邮件通知任务执行人
			String Sender=user.getmail(usr);
			String Texttype="text/html;charset=UTF8";
			String Context_init="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>任务通知</title></head>";
			Context_init=Context_init+"\n"+"<body style='font-family:微软雅黑;font-size: 12px;line-height:20px;'>";
			String mailfoot="<br><br><hr width='300px' align='left'></hr>";
			mailfoot=mailfoot+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";		
			if(tag==0) {
//				执行人没有发生变化，则只发送变更内容
				String Receivers=record[1];
				Receivers=user.getmail(Receivers);				
				String Subject="【测试任务变更通知】您被分配的测试任务"+task_index+"已变更，请查看详情。";				
				String Context=Context_init+"<span>您好，您之前被分配的任务已变更，详情如下：</span><br>";
				Context=Context+email_cont;			
				Context=Context+mailfoot;	
				inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
			}
			else {
//				执行人发生变化，则需要向老执行人和新执行人分别发送撤销和新增任务邮件	
				String Receivers=info[0][4];
				Receivers=user.getmail(Receivers);
				String Subject="【测试任务撤销通知】您被分配的测试任务"+task_index+"已撤销。";
				String Context=Context_init+"<span>您好，您之前被分配的项目"+info[0][1]+"_"+info[0][2]+"任务[编号："+task_index+"]已经被撤销，请知</span><br>";				
				Context=Context+mailfoot;			
				inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
				
				info=dbd.readDB("qa_tasks", "id,proj,subversion,status,name,executor,endtime_exp,precondition,content", "et_index='"+task_index+"'");
				if(!info[0][0].equals("")) {
					String taskcont="<table cellspacing=0 cellpadding=0 style='margin-top:15px;font-family:微软雅黑;font-size: 12px;'><tbody>";
					taskcont=taskcont+"<tr height='20px'><td><b>任务：</b>"+info[0][4]+"</td></tr>";
					taskcont=taskcont+"<tr height='20px'><td><b>期望完成时间：</b>"+info[0][6]+"</td></tr>";
					taskcont=taskcont+"<tr height='20px'><td>任务执行的必要条件：</td></tr>";
					taskcont=taskcont+"<tr><td style='padding-left:10px;line-height:20px;'>"+info[0][7]+"</td></tr>";
					taskcont=taskcont+"<tr height='20px'><td>任务描述：</td></tr>";
					taskcont=taskcont+"<tr><td style='padding-left:10px;line-height:20px;'>"+info[0][8]+"</td></tr>";
					taskcont=taskcont+"</tbody></table>";
					
					Receivers=user.getmail(info[0][5]);
					Subject="【测试任务通知】您已被分配执行项目"+info[0][1]+"_"+info[0][2]+"的任务，请查看邮件详情";
					Context=Context_init+"<span>您好，您已被分配执行项目["+info[0][1]+"_"+info[0][2]+"]的测试任务，请查看如下详情：</span><br>";
					Context=Context+taskcont;
					Context=Context+mailfoot;			
					inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
				}			
			}
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}	
	}
	/**
	 * [Function]	修改测试任务状态来完成任务的提交、审核关闭、审核驳回操作，只有管理员、测试经理和工程师可以审核，执行者本人或更高权限者可以提交
	 * @param usr				操作用户的账户
	 * @param task_index	要操作的任务编号
	 * @param status			要修改的状态
	 * @throws Exception 404，任务不存在
	 */
	void ChangeTaskStatus(String usr, String task_index, String status) throws Exception{
		PropertyConfigurator.configure(logconf);		
		try {	
			String[][] info=dbd.readDB("qa_tasks", "id,status,executor", "et_index='"+task_index+"'");
			if(info[0][0].equals(""))throw new Exception("[info]404, 测试任务"+task_index+"不存在！");
//			判断权限
			String[] usrinfo=user.Get(usr);
			String titl=user.GetTitle(usr);
			if(status.equals("已完成")) {
				if(!usrinfo[5].equals("sysadmin") && !usrinfo[1].equals(info[0][2]) && !titl.equals("TM") && !titl.equals("TE"))throw new Exception("[info]401,无权操作");	
			}
			else if(status.equals("已关闭") || status.equals("已驳回")) {
				if(!usrinfo[5].equals("sysadmin") && !titl.equals("TM") && !titl.equals("TE"))throw new Exception("[info]401,无权操作");
			}
			dbd.UpdateSQl("qa_tasks", Integer.parseInt(info[0][0]), "status", status);		
//			发邮件通知
			String Sender=user.getmail(usr);
			String Texttype="text/html;charset=UTF8";
			String Context_init="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>任务通知</title></head>";
			Context_init=Context_init+"\n"+"<body style='font-family:微软雅黑;font-size: 12px;line-height:20px;'>";
			String mailfoot="<br><br><hr width='300px' align='left'></hr>";
			mailfoot=mailfoot+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";		
			String Receivers="yangmiao@auto-smart.com";
			String Subject="【测试任务通知】测试任务"+task_index+status+"，请查看邮件详情";
			String Context=Context_init+"<span>您好，任务"+task_index+"已完成，请登录系统补充确认状态</span><br>";
			if(status.equals("已驳回")) {
				Receivers=user.getmail(info[0][2]);
				Context=Context_init+"<span>您好，您提交的任务"+task_index+"已被驳回，请与任务发布人确认原因</span><br>";
			}
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
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
