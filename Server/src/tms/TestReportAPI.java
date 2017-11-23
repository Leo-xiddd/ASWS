/**
 * 本模块提供的API包括：
 * 1. AddTR(String usr,String TRdata) 
 * 2. ListTR(String filter,String Page_count,String Page_num)
 * 3. GetTR(String TR_index)
 * 4. RejTR(String usr,String TR_index)
 * 5. ApprTR(String usr,String TR_index)
 * 6. UpdateTR(String usr,String TR_index,String body)
 * 7. DelTR(String usr,String TR_index)
 */
package tms;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.log4j.*;

import base.*;

public class TestReportAPI {
	DBDriver dbd = new DBDriver();
	User user=new User();
	Inform inf=new Inform();
//	配置日志属性文件位置
	String confpath=System.getProperty("user.dir").replace("\\bin", "");
	String logconf=confpath+"\\conf\\ASWS\\asws_log.properties";
	String sysconf=confpath+"\\conf\\ASWS\\Sys_config.xml";
	Logger logger = Logger.getLogger(TestReportAPI.class.getName());
	
	SimpleDateFormat sdf_ymd = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat sdf_full = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
//	设置测试报告字段
	String[] TRField_Json= {"Proj_Productline","Proj_Name","Proj_Version","Proj_Subversion","Report_time" ,"Reporter" ,"TestResult" ,"Num_tc_exe" ,"Num_tc_fail","Num_bug_unclose" ,"Num_bug_total",
			"Num_bug_ul3","Num_bug_reopen","Num_bug_close","Num_dqs" ,"Rate_bug_open" ,"Rate_bug_reopen" ,"Rate_tc_fail" ,"Time_start" ,"Time_end" ,"Timecost",
			"Cycles" ,"Responsor" ,"Testengineer" ,"issues" ,"Range" ,"Testconditions"};
	String[] TRField_table= {"TR_index","productline","proj","version","subversion","Tr_time" ,"Reporter" ,"testresult" ,"Num_tc_exe" ,"Num_tc_fail","Num_bug_unclose" ,"Num_bug_total",
			"Num_bug_ul3","Num_bug_reopen","Num_bug_close","Num_dqs" ,"Rate_bug_open" ,"Rate_bug_reopen" ,"Rate_tc_fail" ,"Time_start" ,"Time_end" ,"Timecost",
			"Cycles" ,"Responsor" ,"Testengineer" ,"issues" ,"TRange" ,"Testconditions"};
	
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
		String code="412";
		String backvalue="412,http 请求的参数缺失或无效";
		String TR_index=checkpara(Param,"TR_index");
//		处理API
		try {
			switch(API){
			case "Add":   		
				if(!body.equals("")) {
					logger.info("创建新测试报告...");
					AddTR(usr,body);			//需要权限控制
					backvalue="200,ok";
				}				
				break;	
			case "Get":   						
				if(!TR_index.equals("")) {
					logger.info("获取测试报告"+TR_index+"...");
					return GetTR(TR_index);
				}
				break;
			case "Get_PT_chart":
				String chart=checkpara(Param,"chart");
				if(!TR_index.equals("") && !chart.equals("")) {
					logger.info("获取性能测试报告"+TR_index+"的"+chart+"图表数据...");
					return Get_PT_chart(TR_index,chart);
				}
			case "Reject":   						
				TR_index=checkpara(Param,"TR_index");
				if(!TR_index.equals("")) {
					logger.info("拒绝测试报告"+TR_index+"...");
					RejTR(usr,TR_index);
					backvalue="200,ok";
				}
				break;
			case "Approve":   						
				TR_index=checkpara(Param,"TR_index");
				if(!TR_index.equals("")) {
					logger.info("批准测试报告"+TR_index+"...");
					ApprTR(usr,TR_index);
					backvalue="200,ok";
				}
				break;
			case "Update":   						
				TR_index=checkpara(Param,"TR_index");
				if((!TR_index.equals(""))&&(!body.equals(""))) {
					logger.info("更新测试报告"+TR_index+"...");
					UpdateTR(usr, TR_index,body);
					backvalue="200,ok";
				}
				break;
			case "Delete":   		
				TR_index=checkpara(Param,"TR_index");
				if(!TR_index.equals("")) {
					logger.info("删除测试报告"+TR_index+"...");
					DelTR(usr,TR_index);		//需要权限控制
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
		code=backvalue.substring(0,backvalue.indexOf(","));
		message=backvalue.substring(backvalue.indexOf(",")+1);
		backvalue="{\"code\":"+code+",\"message\":\""+message+"\"}";
		return backvalue;
	}
	
	/**
	 * 函数说明：创建一个新的测试报告
	 * @param usr				当前操作的用户
	 * @param TRdata			用来创建新测试报告的数据JSON格式
	 * @throws Exception 	401,权限不足
	 * @throws Exception 	412,参数错误
	 * @throws Exception 	500,数据库故障
	 */
	void AddTR(String usr,String TRdata) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			只有测试经理和测试工程师才有权限创建新测试报告
			String usr_title=user.GetTitle(usr);
			if((!usr_title.equals("TE")) && (!usr_title.equals("TM")))throw new Exception("[info]401,无权操作");

			int coun=TRField_Json.length;
			String[] record=new String[coun+1];
			JSONObject mpd=new JSONObject(TRdata);			
			for(int i=0;i<coun;i++)record[i+1]=mpd.getString(TRField_Json[i]);						
			record[0]="TR_"+record[2]+"_"+record[4];
			
//			进行去重验证
			int row=dbd.check("sys_testreport", "TR_index", record[0]);
			if(row>0)throw new Exception("[info]401,报告"+record[0]+"已存在");
			
//			获取项目所属产品线信息
			String[][] pl=dbd.readDB("sys_test_proj", "productline", "proj='"+record[2]+"' and version='"+record[3]+"'");
			record[1]=pl[0][0];
//			添加测试报告记录	
			dbd.AppendSQl("sys_testreport", TRField_table, record, 1, 1);
			
//			修改测试任务状态为finish，表示等待审核
			String[][] rr=dbd.readDB("sys_test_task", "id", "proj='"+record[2]+"' and subversion='"+record[4]+"'");
			row=Integer.parseInt(rr[0][0]);
			dbd.UpdateSQl("sys_test_task", row, "teststatus", "finish");
			
//			发邮件通知测试负责人审核报告
			String Receivers=user.getmail(record[23]);
			String Sender=user.getmail(usr);
			String Subject="[测试报告]"+record[2]+" "+record[4]+"测试报告审核申请";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>测试报告提交提醒</title>";
			Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，"+record[2]+" "+record[4]+"已经由"+usr+
					"完成并提交提测报告，具体内容请登录ASWS系统查看</span><br><br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";	
			String Texttype="text/html;charset=UTF-8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");			
		} catch(JSONException e) {
			logger.error("测试报告数据内容不完整"+e.toString());
			throw new Exception("[info]412,测试报告数据内容不完整,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：列出当前所有测试报告，只罗列测试报告简表
	 * @param  filter				过滤条件，目前只支持项目、版本、子版本、报告时间、报告人、测试经理、测试结论
	 * @param  Page_count		单页显示的记录数
	 * @param  Page_num		显示的页码	
	 * @return	  JSONArray格式字符串，包括字段TR_index, Tr_time, Reporter, testresult, Responsor
	 * @throws Exception 500,数据库故障
	 */
	String ListTR(String filter,String Page_count,String Page_num) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			配置过滤条件，返回的测试报告将按报告时间降序排列
			if(filter.equals(""))filter="id>0";	
			filter=filter+" order by Tr_time desc";
//			配置返回字段，获取数据表内容
			String col_table= "TR_index,Tr_time,Reporter,testresult,Responsor";	
			String[][] TRs=dbd.readDB("sys_testreport", col_table, filter);			
			int num=0;
			int first_num=0;
			if(!TRs[0][0].equals(""))num=TRs.length;
			int last_num=num;	
			
			JSONObject ja=new JSONObject();
			JSONArray trlist=new JSONArray();			
			ja.put("TR_count",num);
			ja.put("Page_count",Page_count);
			ja.put("Page_num",Page_num);
//			如果有符合条件的记录才返回数据
			if(num>0) {
//				如果每页记录数为空，则默认返回所有数据
				if(!Page_count.equals("")) {
					int nn=Integer.parseInt(Page_count);
//					如果每页记录数大于实际记录数，则全部返回，否则分页显示
					if(num>nn) {
//						如果页码为空，则默认返回第一页
						if(!Page_num.equals("")) {
							int mm=Integer.parseInt(Page_num);
//							如果页码*单页记录数大于实际记录则返回空，否则从新计算起始页
							if(mm<1 || (mm-1)*nn>=num) {
								ja.put("TRlist",trlist);
								return ja.toString();
							}
							else {
								first_num=(mm-1)*nn;						
								if(num>mm*nn)last_num=first_num+nn;
							}
						}
						else	last_num=first_num+nn;
					}
				}
			}
//			设置返回的数据字段
			String[] col_json= {"TestReport","Release_time","Reporter","Test_Result","Test_Manager"};			
			num=0;
			for(int i=first_num;i<last_num;i++) {
				JSONObject jsb=new JSONObject();
				for(int j=0;j<col_json.length;j++) {
					if(col_json[j].equals("Release_time"))TRs[i][j]=sdf_full.format(sdf_full.parse(TRs[i][j]));
					jsb.put(col_json[j], TRs[i][j]);
				}
				trlist.put(num,jsb);
				num++;
			}						
			ja.put("TRlist",trlist);
			ja.put("code",200);
			return ja.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：获取指定的测试报告详细内容
	 * @param 	TR_index	要获取的测试报告编号
	 * @throws 	Exception 	404,找不到测试报告
	 * @throws 	Exception 	500,系统错误
	 * @return		完整的JSON格式返回值
	 */
	String GetTR(String TR_index) throws Exception {
		PropertyConfigurator.configure(logconf);			
		try {
			JSONObject Tr=new JSONObject();
			String[][] trcont=dbd.readDB("sys_testreport", "*", "TR_index='"+TR_index+"'");
			if(trcont[0][0].equals(""))throw new Exception("[info]404,没有找到测试报告"+TR_index);
			for(int i=0;i<TRField_Json.length;i++) {
				if(TRField_Json[i].equals("Report_time"))trcont[0][i+2]=sdf_full.format(sdf_full.parse(trcont[0][i+2]));
				if(TRField_Json[i].equals("Time_start"))trcont[0][i+2]=sdf_ymd.format(sdf_ymd.parse(trcont[0][i+2]));
				if(TRField_Json[i].equals("Time_end"))trcont[0][i+2]=sdf_ymd.format(sdf_ymd.parse(trcont[0][i+2]));
				Tr.put(TRField_Json[i], trcont[0][i+2]);
			}
			Tr.put("code", 200);
			return Tr.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}	
	}
	
	String Get_PT_chart(String TR_index, String chart)throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
			JSONObject PTC=new JSONObject();
			JSONArray dat=new JSONArray();
			for(int i=0;i<10;i++) {
				JSONObject temp=new JSONObject();
				temp.put("xAxis", i);
				temp.put("yAxis", i*3);
				dat.put(i, temp);
			}		
			PTC.put("chartdata", dat);
			PTC.put("perc90", 15);
			PTC.put("code", 200);
			return PTC.toString();
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}	
	}
	
	void RejTR(String usr,String TR_index) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			只有测试经理才有权限拒绝测试报告
			String usr_title=user.GetTitle(usr);
			if(!usr_title.equals("TM"))throw new Exception("[info]401,无权操作");
			
			int row=dbd.check("sys_testreport", "TR_index", TR_index);
			if(row==0)throw new Exception("[info]404,测试报告["+TR_index+"]不存在");
			
//			发邮件通知报告人
			String[][] record=dbd.readDB("sys_testreport", "Reporter", "id="+row);
			String Receivers=user.getmail(record[0][0]);
			String Sender=user.getmail(usr);
			String Subject="测试报告"+TR_index+"驳回通知";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>测试报告驳回提醒</title>";
			Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，您提交的测试报告"+TR_index+
					"由于内容不完整或者其他原因被测试负责人驳回，请尽快与测试负责人沟通并重新提交。</span><br><br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";	
			String Texttype="text/html;charset=UTF-8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	
	void ApprTR(String usr,String TR_index) throws Exception {
		PropertyConfigurator.configure(logconf);	
		try {
//			1. 权限审核，只有测试经理才有权限批准测试报告 
			String usr_title=user.GetTitle(usr);
			if(!usr_title.equals("TM"))throw new Exception("[info]401,无权操作");
			
			int row=dbd.check("sys_testreport", "TR_index", TR_index);
			if(row==0)throw new Exception("[info]404,测试报告["+TR_index+"]不存在");
			String[][] record=dbd.readDB("sys_testreport", "proj,subversion,testresult,version,Time_end,Timecost,Testengineer,Time_start,Responsor", "id="+row);
			
//			2. 修改测试任务状态、开始时间（考虑到测试报告中间被修改的可能）、结束时间、测试经理（考虑到测试报告中间被修改的可能）和耗时
			String[][] rr=dbd.readDB("sys_test_task", "id", "proj='"+record[0][0]+"' and subversion='"+record[0][1]+"'");
			row=Integer.parseInt(rr[0][0]);
			dbd.UpdateSQl("sys_test_task", row, "teststatus", record[0][2]);
			dbd.UpdateSQl("sys_test_task", row, "endtime", record[0][4]);
			dbd.UpdateSQl("sys_test_task", row, "timecost", record[0][5]);
			dbd.UpdateSQl("sys_test_task", row, "starttime", record[0][7]);
			dbd.UpdateSQl("sys_test_task", row, "responsor", record[0][8]);
			
//			2.1 每结束一次测试任务应该更新一次项目的测试周期
			int TimeCost_add=Integer.parseInt(record[0][5]);
			String[][] tp_tc=dbd.readDB("sys_test_proj", "timecost,id", "proj='"+record[0][0]+"' and version='"+record[0][3]+"'");
			if(!tp_tc[0][0].equals(""))TimeCost_add=TimeCost_add+Integer.parseInt(tp_tc[0][0]);
//			3. 修改测试项目状态和测试周期
			row=Integer.parseInt(tp_tc[0][1]);
			if(record[0][2].equals("pass"))record[0][2]="通过";
			if(record[0][2].equals("fail"))record[0][2]="失败";	
			if(record[0][2].equals("reject"))record[0][2]="驳回";	
			dbd.UpdateSQl("sys_test_proj", row, "teststatus", record[0][2]);
			dbd.UpdateSQl("sys_test_proj", row, "timecost", ""+TimeCost_add);
			
//			4. 发邮件通知报告人	(产品经理、提测人、测试工程师、开发人员、高级测试经理、其他)			
			String TES=record[0][6]+",";
			String TSB_index=TR_index.replace("TR", "ST");
			String[][] projinfo=dbd.readDB("sys_submit", "*", "TSB_index like '"+TSB_index+"%' and Status='接受'");
			String PM=",";
			String Submitter=",";			
			String Devs=",";
			if(!projinfo[0][0].equals("")) {
				PM=projinfo[0][5]+PM;
				Submitter=projinfo[0][7]+Submitter;			
				Devs=projinfo[0][8]+Devs;
			}
			
			String ADMS="";
			String[][] adm=dbd.readDB("sys_usrdb", "usrname", "role='ATM'");
			if(!adm[0][0].equals("")) {
				for(int k=0;k<adm.length;k++)ADMS=ADMS+adm[k][0]+",";
			}	
			String[][] tp=dbd.readDB("sys_test_proj", "*","id="+row); 
			String others=tp[0][14]+",";	
			String Receivers=PM+Submitter+TES+Devs+ADMS+others;
			Receivers=user.getmail(Receivers);
			String Sender=user.getmail(usr);
			String Subject="【测试报告】"+record[0][0]+"_"+record[0][1];
			String Context=""; 
			
//			4.1 配置测试报告字段和模板
			String[] TR_field= {"<span id=\"Proj_Name\">","<span id=\"Version\">","<span id=\"Proj_Subversion\">","<span id=\"Report_time\">" ,"<span id=\"Reporter\">" ,
					"<span id=\"TestResult\">" ,"<span id=\"Num_tc_exe\">" ,"<span id=\"Num_tc_fail\">","<span id=\"Num_bug_unclose\">" ,"<span id=\"Num_bug_total\">","<span id=\"Num_bug_ul3\">",
					"<span id=\"Num_bug_reopen\">","<span id=\"Num_bug_close\">","<span id=\"Num_dqs\">" ,"<span id=\"Rate_bug_open\">" ,"<span id=\"Rate_bug_reopen\">" ,
					"<span id=\"Rate_tc_fail\">" ,"<span id=\"Time_start\">" ,"<span id=\"Time_end\">" ,"<span id=\"Timecost\">","<span id=\"Cycles\">" ,"<span id=\"Responsor\">" ,
					"<span id=\"Testengineer\">" ,"<span id=\"issues\">","<span id=\"Range\">" ,"<span id=\"Testconditions\">"};			
			
//			4.2 判断模板文件是否存在，如果不存在返回404
			String TempFN=confpath+"\\conf\\ASWS\\TR_temp.html";
			String Atta=confpath+"\\conf\\ASWS\\passlogo.png;";
			if(record[0][2].equals("驳回")) {
				TempFN=confpath+"\\conf\\ASWS\\refuse_temp.html";
				Subject="【版本驳回】"+record[0][0]+"_"+record[0][1]+"冒烟测试不通过";
				Atta="";
			}
			File tempfile = new File(TempFN);
			String fn=TempFN.substring(TempFN.lastIndexOf("\\")+1, TempFN.length());
			if (!tempfile.exists()) throw new Exception("[info]404,报告模板"+fn+"不存在！");
			
//			4.3 将报告内容写入邮件
			String[][] records=dbd.readDB("sys_testreport", "*", "TR_index='"+TR_index+"'");
			BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (tempfile), "UTF-8"));
			String line;
			if(records[0][8].equals("pass"))records[0][8]="通过";
			if(records[0][8].equals("fail"))records[0][8]="不通过";	
			if(records[0][8].equals("reject"))records[0][8]="驳回";	
			while ((line = br.readLine()) != null) {
				for(int i=0;i<TR_field.length;i++) {					
					if(line.indexOf(TR_field[i])>-1) {
						if(TR_field[i].equals("<span id=\"Report_time\">"))records[0][i+3]=sdf_full.format(sdf_full.parse(records[0][i+3]));
						if(TR_field[i].equals("<span id=\"Time_start\">"))records[0][i+3]=sdf_ymd.format(sdf_ymd.parse(records[0][i+3]));
						if(TR_field[i].equals("<span id=\"Time_end\">"))records[0][i+3]=sdf_ymd.format(sdf_ymd.parse(records[0][i+3]));
						line=line.replace(TR_field[i], TR_field[i]+records[0][i+3]);						
					}
				}
				if(line.indexOf("passlogo.png")>-1 && record[0][2].equals("失败")) {
					line=line.replace("passlogo.png", "failtag.png");
					Atta=confpath+"\\conf\\ASWS\\failtag.png;";
				}
				Context=Context+line+"\n";
			} 
			br.close();			
			String Texttype="text/html;charset=UTF-8";
			Atta=Atta+confpath+"\\conf\\ASWS\\logo.png";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, Atta);
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new Exception("[info]500,"+e.toString());
		}catch (Throwable e) {
			logger.error(e.toString());
			throw new Exception(e);
		}
	}
	
	/**
	 * 函数说明：更新测试报告，只有测试经理和测试工程师才有权限修改测试报告，已发布报告不能修改和删除
	 * @param usr				当前操作的用户
	 * @param TR_index		要更新的测试报告编号
	 * @param body			用来更新测试报告的数据，JSON格式
	 * @throws Exception 	404,测试项目不存在
	 * @throws Exception 	409,测试项目不存在
	 * @throws Exception 	500,数据库故障
	 */
	void UpdateTR(String usr,String TR_index,String body) throws Exception {
		PropertyConfigurator.configure(logconf);			
		try {
//			只有测试经理和测试工程师才有权限修改测试报告
			String usr_title=user.GetTitle(usr);
			if((!usr_title.equals("TE")) && (!usr_title.equals("TM")))throw new Exception("[info]401,无权操作");
			
			int row=dbd.check("sys_testreport", "TR_index", TR_index);
			if(row==0)throw new Exception("[info]404,没有找到测试报告"+TR_index);
			
//			审核测试任务状态，已发布报告不能修改和删除
			String[][] trinfo=dbd.readDB("sys_testreport", "proj,subversion,Responsor", "id="+row);
			String[][] tt_status=dbd.readDB("sys_test_task", "teststatus", "proj='"+trinfo[0][0]+"' and subversion='"+trinfo[0][1]+"'");
			if((!tt_status[0][0].equals("running"))&&(!tt_status[0][0].equals("finish")))throw new Exception("[info]409,测试任务"+trinfo[0][0]+trinfo[0][1]+"已结束，不能修改测试报告");
			
//			更新测试报告内容
			JSONObject mpd=new JSONObject(body);			
			for(int i=4;i<TRField_Json.length;i++) {
				dbd.UpdateSQl("sys_testreport", row, TRField_table[i+1], mpd.getString(TRField_Json[i]));
			}
			
//			发邮件通知测试负责人审核报告
			String Receivers=user.getmail(trinfo[0][2]);
			String Sender=user.getmail(usr);
			String Subject="[测试报告]"+trinfo[0][0]+" "+trinfo[0][1]+"测试报告审核申请";
			String Context="<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'><title>测试报告审核提醒</title>";
			Context=Context+"\n"+"</head><body style='font-family:''微软雅黑';font-size: 10px;'><span>您好，"+trinfo[0][0]+" "+trinfo[0][1]+"测试报告已经由"
			+usr+"修改并重新提交，具体内容请登录ASWS系统查看</span><br><br><br><hr width='300px' align='left'></hr>";				
			Context=Context+"\n"+"<span>一个车慧自己的研发协作平台&nbsp;-&nbsp;<a href='http://tms.tech.bitauto.com:8080/ASWS/login.html'>"
					+ "ASWS 研发工作协作平台</a></span></body></html>";	
			String Texttype="text/html;charset=UTF-8";
			inf.toemail(Receivers, Sender,Subject, Context, Texttype, "");
		}catch (Throwable e) {
			logger.error(e.toString(),e);
			throw new Exception(e);
		}		
	}
	
	/**
	 * 函数说明：删除指定的测试报告，只有测试经理和测试工程师才有权限修改测试报告，已发布报告不能修改和删除
	 * @param 	usr			当前操作的用户
	 * @param 	TR_index	要删除的测试报告编号
	 * @throws 	Exception	404,测试项目不存在
	 * @throws 	Exception	500,数据库故障
	 */
	void DelTR(String usr,String TR_index) throws Exception  {
		PropertyConfigurator.configure(logconf);	
		try {
//			只有测试经理和测试工程师才有权限删除测试报告
			String usr_title=user.GetTitle(usr);
			if((!usr_title.equals("TE")) && (!usr_title.equals("TM")))throw new Exception("[info]401,无权操作");
			
			int row=dbd.check("sys_testreport", "TR_index", TR_index);
			if(row==0)throw new Exception("[info]404,没有找到测试报告"+TR_index);
			
//			审核测试任务状态，已发布报告不能修改和删除
			String[][] trinfo=dbd.readDB("sys_testreport", "proj,subversion", "id="+row);
			String[][] tt_status=dbd.readDB("sys_test_task", "teststatus,id", "proj='"+trinfo[0][0]+"' and subversion='"+trinfo[0][1]+"'");
			if(!tt_status[0][0].equals("running")&&!tt_status[0][0].equals("finish"))throw new Exception("[info]409,测试任务"+trinfo[0][0]+trinfo[0][1]+"已结束，不能删除测试报告");
			
//			删除数据库记录
			dbd.DelSQl("sys_testreport", row, 1, 1);
			dbd.UpdateSQl("sys_test_task", Integer.parseInt(tt_status[0][1]), "teststatus", "running");
		} catch (Throwable e) {
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