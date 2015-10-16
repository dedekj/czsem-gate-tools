package czsem.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import czsem.utils.Config;

public class ProjectSetup implements Serializable {

	private static final long serialVersionUID = 5087105660094979374L;
	
	public File working_directory;
	public String dir_for_projects = "C:\\workspace\\czsem\\src\\netgraph\\czsem\\ILP_serial_projects\\";
	public String current_project_dir = null;
	public String project_name = "serialized_exp";
//	private Serializer ser_bkg;		
	
	public ProjectSetup() throws URISyntaxException, IOException
	{
		dir_for_projects = Config.getConfig().getIlpProjestsPath()+'/';
	}
	
	public static String makeTimeStamp()
	{
        Calendar rightNow = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return df.format(rightNow.getTime());		
	}
	public void init_project() throws FileNotFoundException, UnsupportedEncodingException
	{
        String time_stamp = makeTimeStamp();
        
        StringBuilder file_strb = new StringBuilder();
        file_strb.append(dir_for_projects);
        file_strb.append(time_stamp);
        file_strb.append('/');
//        file_strb.append("pokkk");
        
        current_project_dir = file_strb.toString();
        
        working_directory = new File(current_project_dir);
        working_directory.mkdirs();    
	}
	
	public String renderProjectFileName(String extension)
	{
		StringBuilder sb = new StringBuilder(current_project_dir);
		sb.append(project_name);
		sb.append(extension);	
		return sb.toString();
	}

}
