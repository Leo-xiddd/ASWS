/**
 * 本模块提供的API包括：
 * 1. AddTP(String usr,String TPdata)
 * 2. ListTP(String filter)
 * 3. GetTP(String usr,String Projname,String Version)
 * 4. UpdateTP(String TPdata)
 * 5. CloseTP(String usr,String Projname,String Version)
 * 6. DelTP(String usr,String Projname,String Version)
 * 7. AddST(String usr,String STdata) 
 * 8. GetSB(String TSB_index)
 * 9. RejSB(String TSB_index)
 * 10. UpdateSB(String TSB_index,String STdata)
 * 11. DelSB(String TSB_index)
 * 12. ListSB(String Projname,String Version)
 * 13. Startwork(String usr,String TSB_index)
 * 14. ListST(String Projname,String Version)
 * 15. GetST(String ST_index)
 */
package tms;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.log4j.*;

import base.*;

public class TestTaskAPI {
	DBDriver dbd = new DBDriver();
	User user=new User();	
	Inform inf=new Inform();
//	配置日志属性文件位置
	String confpath=System.getProperty("user.dir").replace("\\bin", "");
	String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	String sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	
	Logger logger = Logger.getLogger(TestTaskAPI.class.getName());
	
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
//		获取当前操作用户
		String usr=checkpara(Param,"user");
		String backvalue="412,http 请求的参数缺失或无效";
		String proj=checkpara(Param,"proj");
		String ver=checkpara(Param,"version");
		String ST_index=checkpara(Param,"ST_index");
		String TSB_index=checkpara(Param,"TSB_index");
//		处理API
		try {
			switch(API){
//************这部分是测试项目管理相关的API***************			
			case "Add":   		
				if(!body.equals("")) {
					logger.info("创建新测试项目...");
					AddTP(usr,body);			//需要权限控制
					backvalue="200,ok";
				}				
				break;
			case "List":   	
				logger.info("列出所有测试项目...");
				String filter=checkpara(Param,"filter");
				String Hist=checkpara(Param,"HistSwitch");
				String page_count=checkpara(Param,"page_count");
				String page_num=checkpara(Param,"page_num");
				return ListTP(filter,Hist,page_count,page_num);		
			case "Get":   						
				if((!proj.equals(""))&&(!ver.equals(""))) {
					logger.info("获取测试项目"+proj+" "+ver+"详情...");
					return GetTP(usr,proj,ver);
				}
				break;
			case "Update":   						
				if(!body.equals("")) {
					logger.info("更新测试项目...");
					UpdateTP(body);
					backvalue="200,ok";
				}
				break;
			case "Close":   		
				if((!proj.equals(""))&&(!ver.equals(""))) {
					logger.info("关闭测试项目"+proj+" "+ver);
					CloseTP(usr,proj,ver);
					backvalue="200,ok";
				}
				break;	
			case "Delete":   		
				if((!proj.equals(""))&&(!ver.equals(""))) {
					logger.info("删除测试项目"+proj+" "+ver);
					DelTP(usr,proj,ver);
					backvalue="200,ok";
				}
				break;
//************下面是提测和子任务管理相关的API***************
			case "AddSubmit": 
				if(!body.equals("")) {
					logger.info("提交测试申请...");
					return AddSB(usr,body);
				}
				break;
			case "AddPTSubmit": 
				if(!body.equals("")) {
					logger.info("提交测试申请...");
					return AddSB_pt(usr,body);
				}
				break;
			case "GetSubmit": 
				if(!TSB_index.equals("")) {
					logger.info("获取测试申请...");
					return GetSB(TSB_index);
				}	
				break;
			case "RejectSubmit": 			//提测单被拒绝后只能重新提测，不能修改
				if(!TSB_index.equals("")) {
					logger.info("拒绝测试申请...");
					RejSB(usr,TSB_index);
					backvalue="200,ok";
				}				
				break;		
			case "UpdateSubmit": 		//只有未被处理的提测单可以修改，被拒绝和被批准的都不行
				if((!TSB_index.equals(""))&& (!body.equals(""))) {
					logger.info("修改测试申请...");
					UpdateSB(TSB_index,body);
					backvalue="200,ok";
				}
				break;
			case "DelSubmit": 				//只有未被处理的提测单可以删除，被拒绝和被批准的都不行
				if(!TSB_index.equals("")) {
					logger.info("删除测试申请...");
					DelSB(TSB_index);
					backvalue="200,ok";
				}	
				break;
			case "ListSubmit": 
				logger.info("列出所有测试申请...");
				return ListSB(proj,ver);
			case "StartTask": 
				if(!TSB_index.equals("")) {
					logger.info("接受测试申请，进入测试阶段...");
					Startwork(usr,TSB_index);
					backvalue="200,ok";
				}
				break;
			case "ListSubTask": 
				logger.info("列出所有项目下的测试任务...");
				String HistSwitch=checkpara(Param,"HistSwitch");
				return ListST(proj,ver,HistSwitch);
			case "GetSubTask": 
				logger.info("获取指定的测试任务信息..");
				return GetST(ST_index);
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
	 * 函数说明：创建一个新的测试项目
	 * @param 	usr			发起操作请求的用户名
	 * @param 	TPdata		用来创建新测试项目的数据JSON格式
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空
	 * @throws 	Exception 	412,参数不正确
	 * @throws Exception 	500,数据库故障
	 */
	void AddTP(String usr,String TPdata) throws Exception {
		PropertyConfigurator.configure(logconf);		
//		测试项目表的字段包括："proj","version","productline","pm","expectsttime","starttime","responsor","projstatus","priority","testengineer","teststatus","timecost","cycle","relativor"
		String[] colname= {"proj","version","productline","pm","expectsttime","responsor","projstatus","priority","testengineer","teststatus","timecost","cycle","relativor"};
		String[] record= new String[13];
		try {
//			只有测试经理才有权限创建新测试项目
			String usr_title=user.GetTitle(usr);
			if(!usr_title.equals("TM"))throw new Exception("[info]401,无权操作");
			
			JSONObject mpd=new JSONObject(TPdata);
			record[0]=mpd.getString("Proj_Name");
//			判断项目名是否包含非法字符
			String[] validchar= {"[","]","(",")"};
			for(int i=0;i<validchar.length;i++) {
				if(record[0].indexOf(validchar[i])>-1)throw new Exception("[info]412,项目名不能包含字符"+validchar[i]+"，请从新提交！");
			}
			record[1] =mpd.getString("Proj_Version");
			record[2] =mpd.getString("Proj_Productline");
			record[3] =mpd.getString("Proj_Manager");
			record[4] =mpd.getString("Expect_StartTime");
//			判断项目是否已存在
			int a=dbd.checknum("sys_test_proj", "id", "proj='"+record[0]+"' and version='"+record[1]+"'");
			if(a>0)throw new Exception("[info]409,已存在该项目，请确认项目名称和版本号是否正确。");
			
			if(record[0].equals("") || record[1].equals("") || record[4].equals(""))throw new Exception("[info]412,项目名、版本号和期望开始时间为必填项，请补充后从新提交。");
			
			record[5] =mpd.getString("Test_Manager");
			record[6] ="待提测";
			record[7] =mpd.getString("Proj_Priority");
			record[8] =mpd.getString("Test_Engineer");
			if(record[7].equals(""))record[7]="1";			//测试项目优先级如果为空，默认为最小值1
			
//			补全测试状态、测试周期和回归次数
			record[9] ="未开始";
			record[10] ="0";
			record[11] ="0";
			record[12] =mpd.getString("Others");
			dbd.AppendSQl("sys_test_proj", colname, record, 1, 1);			
			
		} catch(JSONException e) {
			logger.error("测试项目数据内容不完整"+e.toString());
			throw new Exception("[info]412,测试项目数据内容不完整,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：列出当前所有测试项目
	 * @param  filter	用于选择项目的过滤器，由前端设定
	 * @param  Hist		选择当前项目或是历史项目，为1表示当前项目，0表示显示所有项目
	 * @param  page_count		分页显示时，每页显示的条数
	 * @param  page_num		分页显示时，显示的页码
	 * @return	  JSONArray格式字符串
	 * @throws Exception 500,数据库故障
	 */
	String ListTP(String filter,String Hist,String page_count,String page_num) throws Exception {
		PropertyConfigurator.configure(logconf);	
//		检查参数是否有效
		try {
			if(filter.equals(""))filter="id>0";	
			if(Hist.equals("1"))filter=filter+" and priority>0 order by priority desc";	
			else filter=filter+" order by priority desc, expectsttime desc";
			
			JSONObject task_list=new JSONObject();
			JSONArray ja=new JSONArray();
			String[][] admin=dbd.readDB("sys_test_proj", "*", filter);
			int num=0;
			if(!admin[0][0].equals(""))num=admin.length;
			int last_num=num;
//			测试项目表的字段包括："id","proj","version","productline","pm","expectsttime","starttime","responsor","projstatus","priority","testengineer","teststatus","timecost","cycle","relativor"
			int timecost=0;
			int first_num=0;
			task_list.put("page_count", page_count);
			task_list.put("page_num", page_num);
			task_list.put("total_num", num);
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
								task_list.put("tp_list", ja);
								return task_list.toString();
							}
							else {
								first_num=(mm-1)*nn;						
								if(num>mm*nn)last_num=first_num+nn;
							}
						}
						else	last_num=first_num+nn;
					}
				}
				String[][] subtask;
				int j=0;
				for(int  i=first_num;i<last_num;i++) {
					JSONObject jsb=new JSONObject();
					jsb.put("id", i+1);
					jsb.put("Proj_Name", admin[i][1]);
					jsb.put("Proj_Version", admin[i][2]);
					jsb.put("Proj_Manager", admin[i][4]);
					if(admin[i][5]!=null)admin[i][5]=sdf_ymd.format(sdf_ymd.parse(admin[i][5]));
					jsb.put("Expect_StartTime", admin[i][5]);
					if(admin[i][6]!=null)admin[i][6]=sdf_ymd.format(sdf_ymd.parse(admin[i][6]));
					jsb.put("Fact_StartTime", admin[i][6]);
					jsb.put("Test_Manager", admin[i][7]);
//					提测、提测驳回、测试开始、测试结束都会改变项目状态
					jsb.put("Proj_Status", admin[i][8]);		
					jsb.put("Proj_Priority", admin[i][9]);
					jsb.put("Test_Engineer", admin[i][10]);
					jsb.put("Test_Status", admin[i][11]);
					if(!admin[i][12].equals("")) {
						timecost=Integer.parseInt(admin[i][12]);
						subtask=dbd.readDB("sys_test_task", "starttime", "proj='"+admin[i][1]+"' and version='"+admin[i][2]+"' and teststatus='run'");
						if(!subtask[0][0].equals("")) {
							Date st_time=sdf_ymd.parse(subtask[0][0]);
							Date now=new Date();
							long diff = now.getTime() - st_time.getTime();
						    int days = (int) (diff / (1000 * 60 * 60 * 24));
							timecost=timecost+days;
						}
					}
					jsb.put("Test_Time", timecost);
					jsb.put("Test_Cycle", admin[i][13]);
					ja.put(j,jsb);
					j++;
				}
			}					
			task_list.put("testproj_num", num);
			task_list.put("tp_list", ja);
			task_list.put("code",200);
			return task_list.toString();
		}catch (Throwable e) {
			String mess=e.getMessage();
			logger.error(mess,e);
			if(mess.indexOf("[info]")>-1)throw new Exception(e);
			else 	throw new Exception("500,"+mess);
		}		
	}
	
	/**
	 * 函数说明：获取指定测试项目内容,只有测试负责人可以打开并编辑，其他人只能看表
	 * @param 	usr			发起操作请求的用户名
	 * @param 	Projname	测试项目名称
	 * @param 	Version		测试项目版本
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空或请求的项目不存在
	 * @throws 	Exception 	412,参数不正确
	 * @throws 	Exception 	500,系统错误
	 * @return		完整的JSON格式返回值
	 */
	String GetTP(String usr,String Projname,String Version) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			判断用户是否测试负责人
			String usr_title=user.GetTitle(usr);
			if(usr_title.equals("TE") || usr_title.equals("DEV"))throw new Exception("[info]401,无权操作");
//			测试项目表的字段包括："id","proj","version","productline","pm","expectsttime","starttime","responsor","projstatus","priority","testengineer","teststatus","timecost","cycle","relativor"
			String[][] Tproj=dbd.readDB("sys_test_proj", "*", "proj='"+Projname+"' and version='"+Version+"'");		
			if(Tproj[0][0].equals(""))throw new Exception("[info]404,测试项目["+Projname+" "+Version+"]不存在");
			JSONObject jsb=new JSONObject();			
			jsb.put("Proj_Name", Tproj[0][1]);
			jsb.put("Proj_Version", Tproj[0][2]);
			jsb.put("Proj_Productline", Tproj[0][3]);
			jsb.put("Proj_Manager", Tproj[0][4]);
			if(Tproj[0][5]!=null)Tproj[0][5]=sdf_ymd.format(sdf_ymd.parse(Tproj[0][5]));
			jsb.put("Expect_StartTime", Tproj[0][5]);
			if(Tproj[0][6]!=null)Tproj[0][6]=sdf_full.format(sdf_full.parse(Tproj[0][6]));
			jsb.put("Fact_StartTime", Tproj[0][6]);
			jsb.put("Test_Manager", Tproj[0][7]);
			jsb.put("Proj_Status", Tproj[0][8]);		
			jsb.put("Proj_Priority", Tproj[0][9]);
			jsb.put("Test_Engineer", Tproj[0][10]);
			jsb.put("Others", Tproj[0][14]);
			jsb.put("code", 200);
			return jsb.toString();
		}catch (JSONException e) {
			throw new Exception("[info]500,JSON语法错误："+e.toString());
		}
		catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}	
	}
	
	/**
	 * 函数说明：更新测试项目，只能更改基础信息，不能修改数据结构
	 * @param 	TPdata		要更新的测试任务数据
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空或测试项目不存在
	 * @throws 	Exception 	412,参数不正确
	 * @throws 	Exception 	500,系统错误
	 */
	void UpdateTP(String TPdata) throws Exception {
		PropertyConfigurator.configure(logconf);	
//		测试项目表的字段包括："proj","version","pm","expectsttime","starttime","responsor","projstatus","priority","testengineer","teststatus","timecost","cycle","relativor"
		String[] colname= {"pm","productline","expectsttime","starttime","responsor","testengineer","priority","relativor"};		
		String[] keyname= {"Proj_Manager","Proj_Productline","Expect_StartTime","Fact_StartTime","Test_Manager","Test_Engineer","Proj_Priority","Others"};
		try {			
			JSONObject mpd=new JSONObject(TPdata);
			String[] cols= {"proj","version"};
			String[] record= {"",""};
			record[0]=mpd.getString("Proj_Name");
			record[1]=mpd.getString("Proj_Version");
			int row = dbd.check("sys_test_proj", cols, record);
			if(row==0)throw new Exception("[info]404,测试项目["+record[0]+" "+record[1]+"]不存在");
			String val="";
			int len=colname.length;
			for(int i=0;i<len;i++) {
				val=mpd.getString(keyname[i]);
				if(!val.equals(""))dbd.UpdateSQl("sys_test_proj", row, colname[i], val);
			}
		} catch(JSONException e) {
			logger.error("测试项目数据内容不完整"+e.toString());
			throw new Exception("[info]412,测试项目数据内容不完整,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：关闭指定的测试项目
	 * @param 	usr			发起操作请求的用户名
	 * @param 	Projname	测试项目名称
	 * @param 	Version		测试项目版本
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空或测试项目不存在
	 * @throws 	Exception 	412,参数不正确
	 * @throws 	Exception	500,数据库故障
	 */
	void CloseTP(String usr,String Projname,String Version)throws Exception  {
		PropertyConfigurator.configure(logconf);	
		try {
//			判断用户是否测试负责人
			String usr_title=user.GetTitle(usr);
			if(!usr_title.equals("TM"))throw new Exception("[info]401,无权操作");
			
//			删除数据库记录
			String[] colname= {"proj","version"};
			String[] record= {"",""};
			record[0]=Projname;
			record[1]=Version;			
			int row = dbd.check("sys_test_proj", colname, record);	
			if(row==0)throw new Exception("[info]404,测试项目["+Projname+" "+Version+"]不存在");
			
//			判断项目测试状态是否为测试中，如果是不能关闭，提示应先结束项目
			String[][] tps=dbd.readDB("sys_test_proj", "teststatus", "id="+row);
			if(tps[0][0].equals("测试中"))throw new Exception("[info]409,测试项目["+Projname+" "+Version+"]还有未关闭的任务，请先关闭任务！");

//			关闭项目，给项目优先级置0		
			dbd.UpdateSQl("sys_test_proj", row, "priority", "0");
//			修改项目状态和测试状态
			dbd.UpdateSQl("sys_test_proj", row, "projstatus", "已结束");

//			判断当前项目是否优先级最高项目，如果是则调整其他项目优先级，否则不动
			String[][] proi=dbd.readDB("sys_test_proj", "priority", "id="+row);	
			row=dbd.checknum("sys_test_proj", "id", "priority>="+proi[0][0]);			
			int tolare=500-Integer.parseInt(proi[0][0]);
			if(row==0 && tolare>0) {				
				if(tolare>100)tolare=10;
				else if(tolare>50)tolare=5;
				else tolare=1;
				String[][] ids=dbd.readDB("sys_test_proj", "*", "priority>0 and priority<"+proi[0][0]);
				if(!ids[0][0].equals("")) {
					String newValue="";
					for(int i=0;i<ids.length;i++) {
						row=Integer.parseInt(ids[i][0]);
						newValue=newValue+(Integer.parseInt(ids[i][8])+tolare);
						dbd.UpdateSQl("sys_test_proj", row, "priority", newValue);
					}
				}
			}
		} catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}	
	
	/**
	 * 函数说明：删除指定的测试项目
	 * @param 	usr			发起操作请求的用户名
	 * @param 	Projname	测试项目名称
	 * @param 	Version		测试项目版本
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空或测试项目不存在
	 * @throws 	Exception 	412,参数不正确
	 * @throws 	Exception	500,数据库故障
	 */
	void DelTP(String usr,String Projname,String Version)throws Exception  {
		PropertyConfigurator.configure(logconf);	
		try {
//			判断用户是否测试负责人
			String usr_title=user.GetTitle(usr);
			if(!usr_title.equals("TM"))throw new Exception("[info]401,无权操作");
//			删除数据库记录
			String[] colname= {"proj","version"};
			String[] record= {"",""};
			record[0]=Projname;
			record[1]=Version;			
			int row = dbd.check("sys_test_proj", colname, record);	
			if(row==0)throw new Exception("[info]404,测试项目["+Projname+" "+Version+"]不存在");
//			如果项目已经进入提测阶段是不能删除的
			String[][] projstatus=dbd.readDB("sys_test_proj", "projstatus", "id="+row);
			if(!projstatus[0][0].equals("待提测"))throw new Exception("[info]412,测试项目["+Projname+" "+Version+"]已提测，不能删除");
			dbd.DelSQl("sys_test_proj", row, 1, 1);
		} catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}
	}	
	
	/**
	 * 函数说明：创建一个新的功能提测申请
	 * @param 	usr			发起操作请求的用户名
	 * @param 	STdata		用来创建项目提测单的数据JSON格式
	 * @return		String		提测单编号
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空，测试项目未找到
	 * @throws 	Exception 	412,参数不正确
	 * @throws Exception 	500,数据库故障
	 */
	String AddSB(String usr,String STdata) throws Exception {
		PropertyConfigurator.configure(logconf);		
//		用来写入数据库的字段
		String[] colname= {"TSB_index","proj","version","subversion","pm","Submittime","Submitter","Developer","FunctionDes","TRange","Note","UED_codeurl","front_codeurl","Other_codeurl","Wikiurl","Status"};
//		用来获取下传数据表单的字段
		String[] keyname= {"Proj_Name","Proj_Version","Proj_Subversion","Proj_Manager","Submittime","Submitter","Developer","FunctionDes","Range","Note","UED_codeurl","front_codeurl","Other_codeurl","Wikiurl"};
		try {
//			1. 权限判断，只有*****才有权限提测
//			String usr_title=user.GetTitle(usr);
//			if(!usr_title.equals("TM"))throw new Exception("[info]401,无权操作");
			
			int len=keyname.length;
			String[] record= new String[len+2];
			JSONObject mpd=new JSONObject(STdata);
			for(int i=0;i<len;i++)record[i+1]=mpd.getString(keyname[i]);
			
//			2. 判断项目是否已关闭，已关闭项目不能提测
			String[][] proj_priority=dbd.readDB("sys_test_proj", "priority", "proj='"+record[1]+"' and version='"+record[2]+"'");
			if(proj_priority[0][0].equals("0"))throw new Exception("[info]409,项目"+record[1]+record[2]+"已关闭，不能再提交测试。");
			
//			3. 判断是否存在重名提测（状态为'驳回'的不算）
			String filter="proj='"+record[1]+"' and subversion='"+record[3]+"' and Status<>'驳回'";
			int count=dbd.checknum("sys_submit", "id", filter);
			if(count>0)throw new Exception("[info]409,提测单"+record[1]+record[3]+"已存在，请勿重复提交。");

//			4. 添加提测表单			
			int cycle=dbd.checknum("sys_test_task", "ST_index", " proj='"+record[1]+"' and version='"+record[2]+"'");
			record[0]="ST_"+record[1]+"_"+record[3]+"_"+(cycle+1)+"_"+sdf_short.format(new Date());
			record[len+1]="待处理";
			dbd.AppendSQl("sys_submit", colname, record, 1, 1);		
			
//			5. 修改测试项目状态(如果项目处于'待提测'才可以变更)
			filter="proj='"+record[1]+"' and version='"+record[2]+"'";
			String[][] pt_proj=dbd.readDB("sys_test_proj", "projstatus,id", filter);
			if(pt_proj[0][0].equals(""))throw new Exception("[info]404,测试项目"+record[1]+record[2]+"未找到");
			else if(pt_proj[0][0].equals("待提测")){
				dbd.UpdateSQl("sys_test_proj", Integer.parseInt(pt_proj[0][1]), "projstatus", "已提测");
			}
			
//			6. 发送提醒邮件给测试经理、产品经理、开发人员和其他（总监、高级测试经理）
			String[][] tp=dbd.readDB("sys_test_proj", "*", "id="+pt_proj[0][1]);
			String TM=tp[0][7]+",";
			String PM=tp[0][4]+",";			
			String others= tp[0][14]+",";	
			String Receivers=PM+TM+others+record[7];
			Receivers=user.getmail(Receivers);	
			String Sender=user.getmail(usr);
			String Subject="[提测申请]"+record[1]+"_"+record[3]+"提交测试说明";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>添加提测提醒</title>";
			Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，"+record[1]+"_"+record[3]+
					"已经由"+record[6]+"提交提测申请，请您尽快登录ASWS查看</span><br><br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";			
			String Texttype="text/html;charset=UTF8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
			return "{\"code\":200,\"TSB_index\":\""+record[0]+"\"}";
		} catch(JSONException e) {
			logger.error("提测信息不完整"+e.toString());
			throw new Exception("[info]412,提测信息不完整,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	/**
	 * 函数说明：创建一个新的性能提测申请
	 * @param 	usr			发起操作请求的用户名
	 * @param 	STdata		用来创建项目提测单的数据JSON格式
	 * @return		String		提测单编号
	 * @throws 	Exception 	401,操作权限不足
	 * @throws 	Exception 	404,用户名参数为空，测试项目未找到
	 * @throws 	Exception 	412,参数不正确
	 * @throws Exception 	500,数据库故障
	 */
	String AddSB_pt(String usr,String STdata) throws Exception {
		PropertyConfigurator.configure(logconf);		
//		用来写入数据库的字段
		String[] colname= {"TSB_index","proj","version","subversion","pm","Submittime","Submitter","Developer","","","","","","","","Status"};
//		用来获取下传数据表单的字段
		String[] keyname= {"Proj_Name","Proj_Version","Proj_Subversion","Proj_Manager","Submittime","Submitter","Developer","","","","","","",""};
		try {
//			1. 权限判断，只有*****才有权限提测
//			String usr_title=user.GetTitle(usr);
//			if(!usr_title.equals("TM"))throw new Exception("[info]401,无权操作");
			
			int len=keyname.length;
			String[] record= new String[len+2];
			JSONObject mpd=new JSONObject(STdata);
			for(int i=0;i<len;i++)record[i+1]=mpd.getString(keyname[i]);
			
//			2. 判断项目是否已关闭，已关闭项目不能提测
			String[][] proj_priority=dbd.readDB("sys_test_proj", "priority", "proj='"+record[1]+"' and version='"+record[2]+"'");
			if(proj_priority[0][0].equals("0"))throw new Exception("[info]409,项目"+record[1]+record[2]+"已关闭，不能再提交测试。");
			
//			3. 判断是否存在重复提测（状态为'待处理'的或者为'接受'且任务状态为running的不能提测）
			String filter="proj='"+record[1]+"' and subversion='"+record[3]+"' order by Submittime desc";
			String[][] sb_info=dbd.readDB("sys_submit_pt", "Status,TSB_index", filter);
			if(sb_info[0][0].equals("待处理"))throw new Exception("[info]409,项目"+record[1]+record[3]+"已提测，请勿重复提交。");
			else if(sb_info[0][0].equals("接受")){
				String[][] pt_task=dbd.readDB("sys_test_task", "teststatus", "ST_index='"+sb_info[0][1]+"'");
				if(pt_task[0][0].equals("running"))throw new Exception("[info]409,项目"+record[1]+record[3]+"已提测，请勿重复提交。");
			}
				
//			4. 添加提测表单			
			int cycle=dbd.checknum("sys_test_task", "ST_index", " proj='"+record[1]+"' and version='"+record[2]+"'");
			record[0]="ST_"+record[1]+"_"+record[3]+"_"+(cycle+1)+"_"+sdf_short.format(new Date());
			record[len+1]="待处理";
			dbd.AppendSQl("sys_submit_pt", colname, record, 1, 1);		
			
//			5. 修改测试项目状态(如果项目处于'待提测'才可以变更)
			filter="proj='"+record[1]+"' and version='"+record[2]+"'";
			String[][] pt_proj=dbd.readDB("sys_test_proj", "projstatus,id", filter);
			if(pt_proj[0][0].equals(""))throw new Exception("[info]404,测试项目"+record[1]+record[2]+"未找到");
			else if(pt_proj[0][0].equals("待提测")){
				dbd.UpdateSQl("sys_test_proj", Integer.parseInt(pt_proj[0][1]), "projstatus", "已提测");
			}
			
//			6. 发送提醒邮件给测试经理、产品经理、开发人员和其他（总监、高级测试经理）
			String[][] tp=dbd.readDB("sys_test_proj", "*", "id="+pt_proj[0][1]);
			String TM=tp[0][7]+",";
			String PM=tp[0][4]+",";			
			String others= tp[0][14]+",";	
			String Receivers=PM+TM+others+record[7];
			Receivers=user.getmail(Receivers);	
			String Sender=user.getmail(usr);
			String Subject="[提测申请]"+record[1]+"_"+record[3]+"提交测试说明";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>添加提测提醒</title>";
			Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，"+record[1]+"_"+record[3]+
					"已经由"+record[6]+"提交提测申请，请您尽快登录ASWS查看</span><br><br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";			
			String Texttype="text/html;charset=UTF8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
			return "{\"code\":200,\"TSB_index\":\""+record[0]+"\"}";
		} catch(JSONException e) {
			logger.error("提测信息不完整"+e.toString());
			throw new Exception("[info]412,提测信息不完整,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：返回一个提测表单信息
	 * @param TSB_index		提测表单编号
	 * @return	 返回JSON格式字符串数据，如：
	 * 		功能测试：{"code":200,"Submitt":{"TSB_index":xxxxxxx,"Proj_Name":xxxxxx,"Proj_Version":xxxxxx,"Proj_Subversion","Proj_Manager","Submittime","Submitter","Developer","FunctionDes","Range","Note","UED_codeurl","front_codeurl","Other_codeurl","Wikiurl","Status"}}
	 * 		性能测试：{"code":200,"Submitt":{"TSB_index":xxxxxxx,"Proj_Name":xxxxxx,"Proj_Version":xxxxxx,"Proj_Subversion","Proj_Manager","Submittime","Submitter","Developer","","","","","","","","Status"}}
	 * @throws Exception		404，提测表单不存在
	 * @throws Exception		500，系统故障
	 */
	String GetSB(String TSB_index) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			功能测试字段{"id","TSB_index","proj","version","subversion","pm","Submittime","Submitter","Developer","FunctionDes","TRange","Note","UED_codeurl","front_codeurl","Other_codeurl","Wikiurl","Status"};
//			性能测试字段{"id","TSB_index","proj","version","subversion","pm","Submittime","Submitter","Developer","","","","","","","","Status"};
			String[] keyname= {"TSB_index","Proj_Name","Proj_Version","Proj_Subversion","Proj_Manager","Submittime","Submitter","Developer","FunctionDes","Range","Note","UED_codeurl","front_codeurl","Other_codeurl","Wikiurl","Status"};		
			String[] keyname_pt= {"TSB_index","Proj_Name","Proj_Version","Proj_Subversion","Proj_Manager","Submittime","Submitter","Developer","","","","","","","","Status"};		
			
			String[][] SB=dbd.readDB("sys_submit", "*", "TSB_index='"+TSB_index+"'");	
			int Tag=0;
			if(SB[0][0].equals("")) {
				Tag=1;
				SB=dbd.readDB("sys_submit_pt", "*", "TSB_index='"+TSB_index+"'");	
				if(SB[0][0].equals("")) throw new Exception("[info]404,提测表单["+TSB_index+"]不存在");
			}
			
			JSONObject jsb=new JSONObject();			
			JSONObject jsa=new JSONObject();
			if(Tag==0) {
				for(int i=0;i<keyname.length;i++) {
					if(keyname[i].equals("Submittime"))SB[0][i+1]=sdf_full.format(sdf_full.parse(SB[0][i+1]));
					jsa.put(keyname[i], SB[0][i+1]);
				}
			}
			else {
				for(int i=0;i<keyname_pt.length;i++) {
					if(keyname_pt[i].equals("Submittime"))SB[0][i+1]=sdf_full.format(sdf_full.parse(SB[0][i+1]));
					jsa.put(keyname_pt[i], SB[0][i+1]);
				}
			}
			jsb.put("Submitt", jsa);
			jsb.put("code", 200);
			return jsb.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}	
	}
	
	/**
	 * 函数说明：拒绝提测申请并发送提醒邮件，提测表单保留，只变更状态
	 * @param usr				当前操作的用户账号
	 * @param TSB_index		提测表单编号
	 * @throws Exception		404，提测表单不存在
	 * @throws Exception		500，系统故障
	 */
	void RejSB(String usr,String TSB_index) throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {
			String dbname="sys_submit";
			String testtype="功能";
			int row=dbd.check(dbname, "TSB_index", TSB_index);
			if(row==0) {
				dbname="sys_submit_pt";
				testtype="性能";
				row=dbd.check(dbname, "TSB_index", TSB_index);
				if(row==0) throw new Exception("[info]404,提测表单["+TSB_index+"]不存在");
			}
//			1. 更改提测单状态
			dbd.UpdateSQl(dbname, row, "Status", "驳回");
			
//			2.发邮件通知提测人、产品经理、总监
			String[][] record=dbd.readDB(dbname, "proj,version,subversion,pm,Submitter", "id="+row);
			String PM=record[0][3]+",";
			String Submitter=record[0][4]+",";

//			String filter="proj='"+record[0][0]+"' and version='"+record[0][1]+"'";
//			String[][] tp=dbd.readDB("sys_test_proj", "*", filter);
//			String others=tp[0][14]+",";	
			String Receivers=PM+Submitter;
			Receivers=user.getmail(Receivers);
			String Sender=user.getmail(usr);
			String Subject="项目"+record[0][0]+"_"+record[0][2]+testtype+"提测申请驳回通知";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>拒绝提测通知</title>";
			Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，由"+record[0][4]+"提交的"+record[0][0]+
					"_"+record[0][2]+testtype+"提测申请，由于内容不完整或者其他原因，被测试负责人驳回，请尽快与测试负责人沟通</span><br><br><br>"
							+ "<hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";			
			String Texttype="text/html;charset=UTF8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：修改提测申请，但不发送提醒邮件，只有未被处理的提测单可以修改，被拒绝和被批准的都不行
	 * @param  TSB_index	提测表单编号
	 * @param  STdata		要修改的提测单内容
	 * @throws Exception		404，提测表单不存在
	 * @throws Exception		409，提测单状态为已处理，不能再修改
	 * @throws Exception		500，系统故障
	 */
	void UpdateSB(String TSB_index,String STdata) throws Exception{
		PropertyConfigurator.configure(logconf);	
//		用来写入数据库的字段
		String[] colname= {"TSB_index","proj","version","subversion","pm","Submittime","Submitter","Developer","FunctionDes","TRange","Note","UED_codeurl","front_codeurl","Other_codeurl","Wikiurl","Status"};
		String[] colname_pt= {"TSB_index","proj","version","subversion","pm","Submittime","Submitter","Developer","","","","","","","","Status"};

//		用来获取下传数据表单的字段
		String[] keyname= {"Proj_Name","Proj_Version","Proj_Subversion","Proj_Manager","Submittime","Submitter","Developer","FunctionDes","Range","Note","UED_codeurl","front_codeurl","Other_codeurl","Wikiurl"};
		String[] keyname_pt= {"Proj_Name","Proj_Version","Proj_Subversion","Proj_Manager","Submittime","Submitter","Developer","","","","","","","Wikiurl"};

		try {
//			1. 确认提测单状态，只有未被处理的提测单可以修改，被拒绝和被批准的都不行
			String dbname="sys_submit";
			String[][] SB=dbd.readDB(dbname, "Status,id", "TSB_index='"+TSB_index+"'");
			if(SB[0][0].equals("")) {
				dbname="sys_submit_pt";
				SB=dbd.readDB(dbname, "Status,id", "TSB_index='"+TSB_index+"'");
				if(SB[0][0].equals("")) throw new Exception("[info]404,提测表单["+TSB_index+"]不存在");
			}
			if(!SB[0][0].equals("待处理"))throw new Exception("[info]409,提测表单已处理，不能被修改，请重新提测。");
			
//			2. 更改提测单
			int len=keyname.length;
			if(dbname.equals("sys_submit_pt"))len=keyname_pt.length;
			String[] record= new String[len+2];
			JSONObject mpd=new JSONObject(STdata);
			if(dbname.equals("sys_submit_pt")) {
				for(int i=0;i<len;i++)record[i+1]=mpd.getString(keyname_pt[i]);
			}
			else {
				for(int i=0;i<len;i++)record[i+1]=mpd.getString(keyname[i]);
			}
			record[0]=TSB_index;
			record[len+1]="待处理";
			dbd.DelSQl(dbname, Integer.parseInt(SB[0][1]), 1, 1);
			if(dbname.equals("sys_submit_pt")) dbd.AppendSQl(dbname, colname_pt, record, 1, 1);
			else dbd.AppendSQl(dbname, colname, record, 1, 1);
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：删除提测申请，但不发送提醒邮件，只有未被处理的提测单可以删除，被拒绝和被批准的都不行
	 * @param  TSB_index	提测表单编号
	 * @throws Exception		404，提测表单不存在
	 * @throws Exception		409，提测单状态为已处理，不能再删除
	 * @throws Exception		500，系统故障
	 */
	void DelSB(String TSB_index) throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {
//			1. 确认提测单状态，只有未被处理的提测单可以修改，被拒绝和被批准的都不行
			String dbname="sys_submit";
			String[][] SB=dbd.readDB(dbname, "Status,proj,version,id", "TSB_index='"+TSB_index+"'");
			if(SB[0][0].equals("")) {
				dbname="sys_submit_pt";
				SB=dbd.readDB(dbname, "Status,proj,version,id", "TSB_index='"+TSB_index+"'");
				if(SB[0][0].equals(""))throw new Exception("[info]404,提测表单["+TSB_index+"]不存在");
			}
			if(!SB[0][0].equals("待处理"))throw new Exception("[info]409,提测表单已处理，不能被删除，请重新提测。");
			
//			2. 删除提测单
			dbd.DelSQl(dbname, Integer.parseInt(SB[0][3]), 1, 1);	
			
//			3. 修改测试项目状态，如果测试项目状态为'已提测'且项目下无其他待处理的提测，则修改
			String[][] proj_info=dbd.readDB("sys_test_proj", "projstatus,id", "proj='"+SB[0][1]+"' and version='"+SB[0][2]+"'");
			if(proj_info[0][0].equals("已提测")) {
				if(dbname.equals("sys_submit"))dbname="sys_submit_pt";
				else dbname="sys_submit";
				int tasks=dbd.checknum(dbname, "id", "proj='"+SB[0][1]+"' and version='"+SB[0][2]+"' and Status='待处理'");
				if(tasks==0)dbd.UpdateSQl("sys_test_proj", Integer.parseInt(proj_info[0][1]), "projstatus", "待提测");
			}		
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}	
	}
	
	/**
	 * 函数说明：获取提测单列表，可以按项目过滤
	 * @param Projname		测试项目名称，过滤条件
	 * @param Version		测试项目的版本，过滤条件
	 * @return  返回JSON格式字符串的数据，如{"Submitts":[{"TSB_index":"xxxxxxxxx","Proj_Subversion":"1.4.3.1","Submittime":"2017-01-01 12:23:30","Submitter":"Leo","Status":"接受","testtype":"功能"},...],"code":200}
	 * @throws Exception		500，系统故障
	 */
	String ListSB(String Projname,String Version) throws Exception {
		String filter="id>0";
//		1. 设置过滤条件，全部列表，或者项目名过滤，或者项目+版本号过滤
		if(!Projname.equals("")) {
			filter="proj='"+Projname+"'";
			if(!Version.equals(""))filter=filter+" and version='"+Version+"'";
		}
		filter=filter+" order by Submittime desc";		//按提测时间倒序排序
//		用来回传数据表单的字段
		String[] keyname= {"TSB_index","Proj_Subversion","Submittime","Submitter","Status"};
		try {
			JSONObject SB=new JSONObject();
			JSONArray sb_list=new JSONArray();
//			2. 读取提测单列表并打包回传
			int index=0;
			String DBname="sys_submit";
			String testtype="功能";
			for(int k=0;k<2;k++) {
				if(k==1) {
					DBname="sys_submit_pt";
					testtype="性能";
				}
				String[][] SB_list=dbd.readDB(DBname, "TSB_index,subversion,Submittime,Submitter,Status", filter);
				if(!SB_list[0][0].equals("")) {
					for(int  i=0;i<SB_list.length;i++) {
						JSONObject sb=new JSONObject();
						for(int j=0;j<keyname.length;j++) {
							if(keyname[j].equals("Submittime"))SB_list[i][j]=sdf_full.format(sdf_full.parse(SB_list[i][j]));
							sb.put(keyname[j], SB_list[i][j]);
						}
						sb.put("testtype", testtype);
						sb_list.put(index, sb);
						index++;
					}
				}
			}
			SB.put("Submitts", sb_list);
			SB.put("code", 200);
			return SB.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：接受提测申请，添加新的测试任务，向测试人员发送通知邮件，并修改测试项目状态
	 * @param  usr				当前操作用户
	 * @param  TSB_index	提测表单编号
	 * @throws Exception		401，权限不足
	 * @throws Exception		404，提测表单不存在
	 * @throws Exception		500，系统故障
	 */
	void Startwork(String usr,String TSB_index) throws Exception{
		PropertyConfigurator.configure(logconf);	
		try {
//			1. 权限判断，只有测试经理才有权限接受提测
			String usr_title=user.GetTitle(usr);
			if(!usr_title.equals("TM"))throw new Exception("[info]401,无权操作");
			
			String dbname="sys_submit";
			String testtype="功能";
			int row=dbd.check(dbname, "TSB_index", TSB_index);
			if(row==0) {
				dbname="sys_submit_pt";
				testtype="性能";
				row=dbd.check(dbname, "TSB_index", TSB_index);
				if(row==0)throw new Exception("[info]404,提测表单["+TSB_index+"]不存在");
			}
//			2. 更改提测单状态
			dbd.UpdateSQl(dbname, row, "Status", "接受");
			
//			3. 修改测试项目状态（如果项目当前状态为已提测或待提测，可以改为测试中）
			String[][] record=dbd.readDB(dbname, "proj,version,subversion,pm", "id="+row);
			String[][] proj_info=dbd.readDB("sys_test_proj", "id,projstatus,teststatus", "proj='"+record[0][0]+"' and version='"+record[0][1]+"'");
			if(!proj_info[0][0].equals("")) {
				row=Integer.parseInt(proj_info[0][0]);
				if(record[0][1].equals("待提测") || record[0][1].equals("已提测"))dbd.UpdateSQl("sys_test_proj", row, "projstatus", "测试中");
//				如果项目的测试状态不为'测试中'则更改
				if(!record[0][2].equals("测试中") && !record[0][1].equals("已结束") )dbd.UpdateSQl("sys_test_proj", row, "teststatus", "测试中");
			}
		
//			4. 如果是第一个提测任务，就需要为测试项目变更开始测试时间（性能测试不变更）
			int cycle=dbd.checknum("sys_test_task", "id", "proj='"+record[0][0]+"' and version='"+record[0][1]+"'");
			if(dbname.equals("sys_submit")) {
				if(cycle==0)dbd.UpdateSQl("sys_test_proj", row, "starttime", sdf_full.format(new Date()));
//				5. 为测试项目变更cycle（性能测试不变更）
				cycle++;
				dbd.UpdateSQl("sys_test_proj", row, "cycle", ""+cycle);			
			}
			
//			6. 添加测试任务
			String[] colname= {"ST_index" ,"proj" ,"version" ,"subversion" ,"pm" ,"starttime"  ,"responsor" ,"teststatus" ,"timecost","cycle","testtype"};
			String[] testtask=new String[colname.length];
			testtask[0]=TSB_index;
			testtask[1]=record[0][0];
			testtask[2]=record[0][1];
			testtask[3]=record[0][2];
			testtask[4]=record[0][3];			
			testtask[5]=sdf_full.format(new Date());
			testtask[6]=user.getfname(usr);
			testtask[7]="running";
			testtask[8]="0";
			testtask[9]=""+cycle;
			testtask[10]=testtype;
			dbd.AppendSQl("sys_test_task", colname, testtask, 1, 1);
			
//			4.发邮件通知测试工程师和产品经理
			String[][] ters=dbd.readDB("sys_test_proj", "testengineer,pm,relativor", "id="+row);
			String Receivers=ters[0][0]+","+ters[0][1]+","+ters[0][2];
			Receivers=user.getmail(Receivers);
			String Sender=user.getmail(usr);
			String Subject="[测试任务开始通知]"+record[0][0]+"_"+record[0][2]+testtype+"测试开始";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>测试开始通知</title>";
			Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，"+record[0][0]+"_"+record[0][2]
					+"可以开始测试，请尽快完成环境准备并开始测试。</span><br><br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";	
			String Texttype="text/html;charset=UTF8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	/**
	 * 函数说明：获取测试任务列表，可以按项目过滤
	 * @param Projname		测试项目名称，过滤条件
	 * @param Version		测试项目的版本，过滤条件
	 * @param HistSwitch		是否返回历史测试项目的过滤条件，1只显示当前项目，0显示所有项目
	 * @return	 返回JSON格式字符串的数据
	 * @throws Exception		500，系统故障
	 */
	String ListST(String Projname,String Version,String HistSwitch) throws Exception {
		PropertyConfigurator.configure(logconf);	
		String filter="id>'0'";
		String[] col_st= {"ST_index","Proj_Subversion","Start_time","End_time","TestStatus","Test_Time","Test_Cycle"};
//		设置过滤条件，全部列表，或者项目名过滤，或者项目+版本号过滤
		if(!Projname.equals("")) {
			filter="proj='"+Projname+"'";
			if(!Version.equals(""))filter=filter+" and version='"+Version+"'";		
		}
		if(HistSwitch.equals("1"))filter=filter+" and priority>0 order by priority desc";	
		else filter=filter+" order by priority desc";	
		try {
			JSONObject task_list=new JSONObject();
			JSONArray ja=new JSONArray();
			String[][] admin=dbd.readDB("sys_test_proj", "proj,version,testengineer", filter);
			int num=0;
			if(!admin[0][0].equals(""))num=admin.length;
			for(int i=0;i<num;i++) {
				JSONObject jsb=new JSONObject();
				jsb.put("Proj_Name", admin[i][0]);
				jsb.put("Proj_Version", admin[i][1]);		
				jsb.put("tester", admin[i][2]);	
//				开始读取项目的功能子任务
				filter="proj='"+admin[i][0]+"' and version='"+admin[i][1]+"' and testtype='功能'";				
				String[][] STs=dbd.readDB("sys_test_task", "ST_index,subversion,starttime,endtime,teststatus,timecost,cycle", filter);				
				JSONArray st_list=new JSONArray();
				int coun=0;
				if(!STs[0][0].equals("")) {
					coun=STs.length;
					for(int k=0;k<coun;k++) {
						JSONObject Subtas=new JSONObject();
						for(int j=0;j<col_st.length;j++) {
							if(col_st[j].equals("Start_time")) {
								if(STs[k][j]!=null)STs[k][j]=sdf_ymd.format(sdf_ymd.parse(STs[k][j]));
								else STs[k][j]="待补充";
							}
							else if(col_st[j].equals("End_time")) {
								if(STs[k][j]!=null)STs[k][j]=sdf_ymd.format(sdf_ymd.parse(STs[k][j]));
								else STs[k][j]="待补充";
							}
							Subtas.put(col_st[j], STs[k][j]);
						}
						st_list.put(k, Subtas);
					}
				}		
				
//				开始读取项目的性能子任务
				filter="proj='"+admin[i][0]+"' and version='"+admin[i][1]+"' and testtype='性能'";				
				String[][] STs_pt=dbd.readDB("sys_test_task", "ST_index,subversion,starttime,endtime,teststatus,timecost,cycle", filter);				
				JSONArray st_list_pt=new JSONArray();
				if(!STs_pt[0][0].equals("")) {
					coun=coun+STs_pt.length;
					for(int k=0;k<STs_pt.length;k++) {
						JSONObject Subtas=new JSONObject();
						for(int j=0;j<col_st.length;j++) {
							if(col_st[j].equals("Start_time")) {
								if(STs_pt[k][j]!=null)STs_pt[k][j]=sdf_full.format(sdf_full.parse(STs_pt[k][j]));
								else STs_pt[k][j]="待补充";
							}
							else if(col_st[j].equals("End_time")) {
								if(STs_pt[k][j]!=null)STs_pt[k][j]=sdf_full.format(sdf_full.parse(STs_pt[k][j]));
								else STs_pt[k][j]="待补充";
							}
							Subtas.put(col_st[j], STs_pt[k][j]);
						}
						Subtas.put("index",STs_pt[k][1]+"_"+STs_pt[k][6]);
						st_list_pt.put(k, Subtas);
					}
				}		
				
				jsb.put("SubTask_count", coun);
				jsb.put("SubTask", st_list);		
				jsb.put("SubTask_pt", st_list_pt);	
				ja.put(i,jsb);
			}		
			task_list.put("testproj_num", num);
			task_list.put("tp_list", ja);
			task_list.put("code",200);
			
			return task_list.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：获取测试任务内容，用于修改和编辑
	 * @param  ST_index		测试任务编号
	 * @throws Exception		404，测试任务不存在
	 * @throws Exception		500，系统故障
	 */
	String GetST(String ST_index) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			测试任务表会在测试任务开始和结束时更新状态、时间（开始、结束、消耗）
//			测试任务表的字段包括：id ,ST_index ,proj ,version ,subversion ,pm ,starttime ,endtime ,responsor ,teststatus ,timecost, cycle
			String[] col_st= {"ST_index","Proj_Name","Proj_Version","Proj_Subversion","Proj_Manager","Start_time","End_time","Test_Manager","TestStatus","Test_Time","Test_Cycle"};
			String[][] STs=dbd.readDB("sys_test_task", "*", "ST_index='"+ST_index+"'");
			JSONObject ST=new JSONObject();					
			if(!STs[0][0].equals("")) {
//				1. 如果当前任务还未结束则更新当前测试任务的测试周期Timecost
				if(null==STs[0][7]) {
					Date st_time=sdf_ymd.parse(STs[0][6]);
					Date now=new Date();
					long diff = now.getTime() - st_time.getTime();
				    int testcost = (int) (diff / (1000 * 60 * 60 * 24));
					dbd.UpdateSQl("sys_test_task", Integer.parseInt(STs[0][0]), "timecost", ""+testcost);
				}				
				
//				2. 获取测试任务详情并返回
				for(int j=0;j<col_st.length;j++) {
					if(col_st[j].equals("Start_time"))STs[0][j+1]=sdf_ymd.format(sdf_ymd.parse(STs[0][j+1]));
					else if(col_st[j].equals("End_time")) {
						if(null==STs[0][j+1])STs[0][j+1]="";
						else STs[0][j+1]=sdf_ymd.format(sdf_ymd.parse(STs[0][j+1]));
					}
					ST.put(col_st[j], STs[0][j+1]);				
				}
			}
			else throw new Exception("[info]404,测试任务"+ST_index+"不存在。");
			ST.put("code", 200);	
			return ST.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
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