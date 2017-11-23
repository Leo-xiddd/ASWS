package servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/Downloadfile")
public class Downloadfile extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public Downloadfile() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//获得请求文件名  
		String filename = request.getParameter("filename");  
      
		//设置文件MIME类型  
		response.setContentType(getServletContext().getMimeType(filename));  
		//设置Content-Disposition  
		response.setHeader("Content-Disposition", "attachment;filename="+filename);  
		//读取目标文件，通过response将目标文件写到客户端  
		//获取目标文件的绝对路径  
		String fullFileName = getServletContext().getRealPath("/datareport/" + filename);  

		//读取文件  
		InputStream in = new FileInputStream(fullFileName);  
		OutputStream out = response.getOutputStream();  

		//写文件  
		int b;  
		while((b=in.read())!= -1)  
		{  
		    out.write(b);  
		}  		          
		in.close();  
		out.close(); 
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
