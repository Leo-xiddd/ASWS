/** 类说明：本模块用于所有的用户管理功能
 *  作   者：Leo
 *  时   间：2016/9/30
 *  方   法：本模块支持的方法包括：
 *  	1. Add					为系统添加一个新用户
 *  	2. Del					删除一个用户，admin用户不能删
 *  	3. Change				修改当前用户信息，admin用户的全名和角色不能改
 *  	4. List					获取系统所有用户信息，包括权限，不显示密码			
 *  	5. Get					获取当前用户信息，不包括密码和权限
 *  	6. AssignPrivil			为一个指定用户分配、更改权限
 *  	7. Authority			为用户校验密码
 *  	8. encrypt				进行数据加密（内部方法）
 *  	9. getmail				获取用户的邮件地址
 *  	10. getfname			获取用户的全名
 */
package base;

public class User {
	DBDriver dbd = new DBDriver();	
	/**[Function] 			为系统添加一个新用户
	 * @param	name - 		用户名
	 * @param	pwd - 		密码
	 * @param	fname - 	全名(支持中文)
	 * @param	dept1 - 		一级部门(支持中文)
	 * @param	dept2 - 		二级部门(支持中文)
	 * @param	dept3 - 		三级部门(支持中文)
	 * @param	role - 		角色(目前支持admin\owner\viewer)
	 * @param	email - 		电子邮件地址
	 * @param	mobile - 	手机号码
	 * @param	type - 		用户类型
	 * @throws Exception		404, 没有找到数据表
	 * @throws Exception		409, 新用户重名
	 * @throws Exception		412, 必选参数未赋值
	 * @throws Exception		500, 数据库操作失败
	 */
	public void Add(String name,String pwd,String fname,String dept1,String dept2,String dept3,String role,String email,String mobile,String type) throws Exception{
		String[] pattern={"usrname","passwd","fullname","dept1","dept2","dept3","role","email","mobile","privil","type"};
		String[] userinfo=new String[pattern.length];
		userinfo[9]="0";
		try{
//			检查用户名，必选
			if(name.equals(""))throw new Exception("[info]412,用户名为空");
			int row=dbd.check("sys_usrdb", "usrname", name);
			if(row>0)throw new Exception("[info]409,用户名["+name+"]已存在");
			userinfo[0]=name;
			
//			检查密码，必选
			if(pwd.equals(""))throw new Exception("[info]412,密码参数没有赋值,不支持空密码");
			userinfo[1]=encrypt(pwd);			//对密码进行加密处理
			
//			检查邮件信息，必选
			if(email.equals(""))throw new Exception("[info]412,email参数没有赋值");
			userinfo[7]=email;
			
//			角色默认为user，其他还有sysadmin、dept_admin
			if(role.equals(""))userinfo[6]="user";
			else userinfo[6]=role;
				
			userinfo[2]=fname;		
			userinfo[3]=dept1;	
			userinfo[4]=dept2;	
			userinfo[5]=dept3;	
			userinfo[8]=mobile;		
			userinfo[10]=type;
			dbd.AppendSQl("sys_usrdb", pattern,userinfo,1,1);
		}catch(Throwable e){
			throw new Exception(e);			
		}
	}
	
	/**[Function] 					删除指定的用户，如果系统中无此用户则不作任何操作，也不报错
	 * @param	name - 		用户名 
	 * @throws	Exception	404，没有找到数据表
	 * @throws	Exception	409，要删除的用户不可被删除
	 * @throws	Exception	412，必选参数未赋值
	 * @throws	Exception	500，数据库操作失败
	 */
	public void Del(String name) throws Exception{
		if(name.equals(""))throw new Exception("[info]412,name参数没有赋值");
		try{
			int row=dbd.check("sys_usrdb", "usrname", name);
			if(row>0){
				if(name.equals("admin"))throw new Exception("[info]409,系统管理员用户不可删除");
				dbd.DelSQl("sys_usrdb",row,1,1);
			}
		}catch(Throwable e){
			throw new Exception(e);
		}	
	}
	
	/**[Function] 					列出所有用户的信息，包括
	 * @return	  String[][] 		用户ID、用户名、全名、1级部门、2级部门、3级部门、角色、邮件、手机号以及权限表
	 * @param  filter			检索条件，为空则表示无条件
	 * @throws Throwable	404 - 没有找到数据表或者用户
	 * @throws Throwable	412 - 缺少必要参数或者密码错误
	 * @throws Throwable	500 - 数据库操作失败
	 */
	public String[][] List(String filter) throws Throwable{
		String[][] usrlist;
		try{
			if(filter.equals(""))filter="id>'0'";
			usrlist=dbd.readDB("sys_usrdb","id,usrname,fullname,dept1,dept2,dept3,role,email,mobile,type",filter);
		}catch(Throwable e){
			throw e;
		}
		return usrlist;
	}
	
	/**[Function] 					列出指定用户的信息
	 * @param  name - 		用户名 
	 * @return	  String[] 		账号、全名、1级部门、2级部门、3级部门、角色、邮件、手机号、用户类型
	 * @throws Throwable	404 - 没有找到数据表或者用户
	 * @throws Throwable	412 - 缺少必要参数
	 * @throws Throwable	500 - 数据库操作失败
	 */
	public String[] Get(String name) throws Throwable{
		String[] usrinfo;
		if(name.equals(""))throw new Exception("[info]412,user参数没有赋值");
		try{
			String[][] usrlist=dbd.readDB("sys_usrdb", "usrname,fullname,dept1,dept2,dept3,role,email,mobile,type", "usrname='"+name+"'");
			if(usrlist[0][0].equals(""))throw new Throwable("[info]404,用户"+name+"不存在");
			int len=usrlist[0].length;
			usrinfo=new String[len];
			for(int i=0;i<len;i++) 	usrinfo[i]=usrlist[0][i];
		}catch(Throwable e){
			throw e;
		}
		return usrinfo;
	}
	
	/**[Function] 					列出指定用户的研发职务
	 * @param  name - 		用户名 
	 * @return	  String 			职务
	 * @throws Throwable	404 - 没有找到数据表或者用户
	 * @throws Throwable	412 - 缺少必要参数
	 * @throws Throwable	500 - 数据库操作失败
	 */
	public String GetTitle(String name) throws Throwable{
		if(name.equals(""))throw new Exception("[info]412,user参数没有赋值");
		try{
			String[][] usrlist=dbd.readDB("sys_usr_role", "role", "usrname='"+name+"'");
			if(usrlist[0][0].equals(""))throw new Throwable("[info]404,用户"+name+"不存在");
			return usrlist[0][0];
		}catch(Throwable e){
			throw e;
		}
	}
	
	/**[Function] 					进行用户密码校验，多用于用户登录
	 * @param  name - 		用户名 
	 * @param  pwd - 			用户密码，经过加密处理
	 * @return	  int 				200普通用户，201管理员账户
	 * @throws Throwable	404 - 没有找到数据表
	 * @throws Throwable	412 - 必选参数未赋值或密码错误
	 * @throws Throwable	500 - 数据库操作失败
	 */
	public int Authority(String name,String pwd) throws Throwable{	
		int ra=200;
//		检查用户名和密码参数，必选
		if(name.equals(""))throw new Exception("[info]412,user参数没有赋值");
		if(pwd.equals(""))throw new Exception("[info]412,pwd参数没有赋值,不支持空密码");
		try{
			String[][] adm=dbd.readDB("sys_usrdb", "type,role,passwd","usrname='"+name+"'");
			if(adm[0][0].equals(""))throw new Exception("[info]404,用户不存在");
			
//			对密码进行解密
			pwd=decrypt(pwd);
//			进行鉴权认证
			if(adm[0][0].equals("local")) {
				if(auth(pwd,adm[0][2])) {
					ra=200;
					if(adm[0][1].equals("admin"))ra=201;
				}
				else throw new Exception("[info]412,用户名或密码错误");
			}
			else {
				Ldap ldap=new Ldap();
				if(ldap.authen(name, pwd)) {
					ra=200;
					if(adm[0][1].equals("admin"))ra=201;
				}
				else {
					throw new Exception("[info]412,用户名或密码错误");
				}
			}
			
		}catch (Throwable e){
			throw new Exception(e.getMessage());
		}
		return ra;
	}
	
	/**[Function] 					修改用户信息(包括密码、全名、一级部门、二级部门、角色、邮件、手机号和用户类型)
	 * @param	name - 		用户名
	 * @param	pwd - 		密码
	 * @param	fname - 	全名(支持中文)
	 * @param	role - 		角色(目前支持admin\owner\viewer)
	 * @param	email - 		电子邮件地址
	 * @param	mobile - 	手机号码
	 * @throws Throwable	404 - 没有找到数据表或者用户
	 * @throws Throwable	412 - 缺少必要参数
	 * @throws Throwable	500 - 数据库操作失败
	 */
	public void Change(String name,String pwd,String fname,String dept1,String dept2,String role,String email,String mobile,String type) throws Throwable{
//		检查用户名参数，必选
		if(name.equals(""))throw new Exception("[info]412,user参数没有赋值");
		try{
			if(name.equals("admin")){
				fname="";
				role="";
			}
			int row=dbd.check("sys_usrdb", "usrname", name);
			if(!pwd.equals(""))dbd.UpdateSQl("sys_usrdb", row, "passwd", encrypt(pwd));
			if(!fname.equals(""))dbd.UpdateSQl("sys_usrdb", row, "fullname", fname); 
			if(!dept1.equals(""))dbd.UpdateSQl("sys_usrdb", row, "dept1", dept1);
			if(!dept2.equals(""))dbd.UpdateSQl("sys_usrdb", row, "dept2", dept2);
			if(!role.equals(""))dbd.UpdateSQl("sys_usrdb", row, "role", role);
			if(!email.equals(""))dbd.UpdateSQl("sys_usrdb", row, "email", email);
			if(!mobile.equals(""))dbd.UpdateSQl("sys_usrdb", row, "mobile", mobile);
			if(!type.equals(""))dbd.UpdateSQl("sys_usrdb", row, "type", type);
		}catch(Throwable e){
			throw e;
		}
	}
	
	/**[Function] 				对数据加密并返回加密后的结果，用于用户密码保存
	 * @return [String]		加密后的用户密码
	 */
	public String encrypt(String data){
		String ency="";
		char[] tcr;
		String pi="31415926535897932384626";
		char[] key=pi.toCharArray();
		int i=0;
		tcr=data.toCharArray();
		for(char a : tcr){
			tcr[i]=(char) (a+key[i]);			
			i=i+1;
		}
		ency=String.valueOf(tcr);
		return ency;
	}
	
	/**[Function] 				对加密数据进行解密并返回解密后的结果，用于用户密码传输
	 * @return [String]		解密后的用户密码
	 */
	public String decrypt(String data){
		String ency="";
		String pi="31415926535897932384626";
		char[] key=pi.toCharArray();	
		String[] num_dat=data.split(":");
		for(int i=0;i<num_dat.length;i++) {
			int asc_data=Integer.parseInt(num_dat[i]);
			int asc_pi=key[i];
			char tt=(char) (asc_data-asc_pi);
			ency=ency+tt;
		}
		return ency;
	}
	/**[Function] 				校验密码正确性
	 * @param Dpass		用来校验的用户密码
	 * @param Spass		数据库中用户的密文密码
	 * @return [Boolean]	返回校验结果，校验通过为True
	 */
	public Boolean auth(String Dpass, String Spass){
		boolean res=false;
		Dpass=encrypt(Dpass);
		if(Dpass.equals(Spass))res=true;
		return res;
	}
	
	/**[Function] 					为一个指定用户分配权限
	 * @param  name			用户名
	 * @param  privil			用户权限序列
	 * @throws Throwable	404 - 没有找到数据表或者用户
	 * @throws Throwable	412 - 缺少必要参数
	 * @throws Throwable	500 - 数据库操作失败
	 */
	public void AssignPrivil(String name,String privil) throws Throwable{
		if(name.equals(""))throw new Exception("[info]412,user参数没有赋值");
		try{
			int row=dbd.check("sys_usrdb", "usrname", name);
			row=dbd.UpdateSQl("sys_usrdb",row,"privil", privil);
		}catch(Throwable e){
			throw e;
		}
	}
	
	/**
	 * 函数说明：用于获取用户邮件地址列表
	 * @param usrlist		要获取邮件地址的用户名，可以是多个用户，用分号分隔
	 * @return					字符串，邮件地址，用分号分隔多个地址
	 */
	public String getmail(String usrlist) {
		String usrmail_list="";
		String filter="usrname='"+usrlist+"'";
		if(usrlist.indexOf(",")>-1) {
			String[] usrs=usrlist.split(",");
			filter="";
			for(int i=0;i<usrs.length;i++) filter=filter+"usrname='"+usrs[i]+"' or ";
			filter=filter.substring(0, filter.lastIndexOf(" or"));
		}
		try {
			String[][] mails=dbd.readDB("sys_usrdb", "email", filter);
			if(mails[0][0].equals("")) {
				filter=filter.replace("usrname", "fullname");
				mails=dbd.readDB("sys_usrdb", "email", filter);
			}
			for(int i=0;i<mails.length;i++)usrmail_list=usrmail_list+mails[i][0]+",";
			usrmail_list=usrmail_list.substring(0, usrmail_list.length()-1);
		}catch(Throwable e) {
			e.printStackTrace();			
		}
		return usrmail_list;
	}
	
	/**
	 * 函数说明：用于获取用户手机号列表
	 * @param usrlist		要获取手机号的用户名，可以是多个用户，用分号分隔
	 * @return					字符串，手机号，用逗号分隔多个地址
	 */
	public String getmobile(String usrlist) {
		String usrmb_list="";
		String filter="usrname='"+usrlist+"'";
		if(usrlist.indexOf(",")>-1) {
			String[] usrs=usrlist.split(",");
			filter="";
			for(int i=0;i<usrs.length;i++) filter=filter+"usrname='"+usrs[i]+"' or ";
			filter=filter.substring(0, filter.lastIndexOf(" or"));
		}
		try {
			String[][] mails=dbd.readDB("sys_usrdb", "mobile", filter);
			for(int i=0;i<mails.length;i++)usrmb_list=usrmb_list+mails[i][0]+",";
			usrmb_list=usrmb_list.substring(0, usrmb_list.length()-1);
		}catch(Throwable e) {
			e.printStackTrace();			
		}
		return usrmb_list;
	}
	
	/**
	 * 函数说明：用于获取用户全名列表
	 * @param usrlist		要获取全名的用户名，可以是多个用户，用分号分隔
	 * @return					字符串，全名，用分号分隔多个地址
	 */
	public String getfname(String usrlist) {
		String usrmail_list="";
		String filter="usrname='"+usrlist+"'";
		if(usrlist.indexOf(",")>-1) {
			String[] usrs=usrlist.split(",");
			filter="";
			for(int i=0;i<usrs.length;i++) filter=filter+"usrname='"+usrs[i]+"' or ";
			filter=filter.substring(0, filter.lastIndexOf(" or"));
		}
		try {
			String[][] mails=dbd.readDB("sys_usrdb", "fullname", filter);
			for(int i=0;i<mails.length;i++)usrmail_list=usrmail_list+mails[i][0]+",";
			usrmail_list=usrmail_list.substring(0, usrmail_list.length()-1);
		}catch(Throwable e) {
			e.printStackTrace();			
		}
		return usrmail_list;
	}
}
