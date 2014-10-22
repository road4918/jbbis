/**
 * ��java�У�����Ҫ��λĳЩ�ļ���λ�ã�Ϊ�����ó���������λ���޹أ���Ҫʹ�����·����
 * ��java��ʹ�����·���ܻ�����һЩ���鷳�����⣬���ǵ���������ĸ�����������⡣
 * ��Ϊ����ƽʱʹ�����·��������Ե�ǰ����Ŀ¼���Եģ�����ʱ���󲢷���ˡ����磬Ҫ��һ����������ʹ�����·����
 * ȴ��֪���������������������ʱ������·���������ر�����webӦ���У�����ȷ��ĳ���ļ�������Ӧ����
 * �����·����
 * 
 * ����ʹ�����·����õİ취������·����ԵĲ��������ҵĿ��������ҵ�Ӧ�ñ���Ķ�������õ�
 * �������ҿ������е����class�ļ���ֻҪ֪����ĳ��class�ļ��ľ���·�����Ϳ�������Ϊ�����
 * ʹ�����·������λ�����κ��ļ��ˡ�
 * 
 * Ϊ��ʵ������뷨����д�����Path�࣬������ṩ��������̬��������:
 *     һ��������λ���class�ļ���λ�ã�
 *     ��һ����ĳ����Ϊ����������λһ�����·����
 * ʹ�����������������ǿ�����ȫ�������Ӧ�õĵ�ǰ����·�������������ĸ����Լ���λ����Ѱ���κ��ļ���
 * �����ڱ�дĳ�������Կ�����ʱ���Ϳ�����ȫ���ùܵ��������������Ӧ�õ�·����������������ݿ�����
 * �����λ������λ�ļ��������ܺõ�ʵ���˷�װ�ԣ����ļ���·��������ȫ������˿���������֮�ڡ�
 * 
 */
package com.hzjbbis.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

//import org.apache.log4j.Logger;


/**
 * @author bhw
 *
 */
public class PathUtil {
	/**
	 * ��ȡһ�����class�ļ����ڵľ���·���� ����������JDK������࣬Ҳ�������û��Զ�����࣬�����ǵ���������������ࡣ
	 * ֻҪ���ڱ������п��Ա����ص��࣬�����Զ�λ������class�ļ��ľ���·����
	 * 
	 * @param cls
	 *            һ�������Class����
	 * @return ������class�ļ�λ�õľ���·���� ���û�������Ķ��壬�򷵻�null��
	 */
	public static String getPathFromClass(Class cls) throws IOException {
		String path = null;
		if (cls == null) {
			throw new NullPointerException();
		}
		URL url = getClassLocationURL(cls);
		if (url != null) {
			path = url.getPath();
			if ("jar".equalsIgnoreCase(url.getProtocol())) {
				try {
					path = new URL(path).getPath();
				} catch (MalformedURLException e) {
				}
				int location = path.indexOf("!/");
				if (location != -1) {
					path = path.substring(0, location);
				}
			}
			File file = new File(path);
			path = file.getCanonicalPath();
		}
		return path;
	}

	/**
	 * �����������ͨ����ĳ�����class�ļ������·������ȡ�ļ���Ŀ¼�ľ���·���� ͨ���ڳ����к��Ѷ�λĳ�����·�����ر�����B/SӦ���С�
	 * ͨ��������������ǿ��Ը������ǳ�����������ļ���λ������λĳ�����·����
	 * ���磺ĳ��txt�ļ�����ڳ����Test���ļ���·����../../resource/test.txt��
	 * ��ôʹ�ñ�����Path.getFullPathRelateClass("../../resource/test.txt",Test.class)
	 * �õ��Ľ����txt�ļ�����ϵͳ�еľ���·����
	 * 
	 * @param relatedPath
	 *            ���·��
	 * @param cls
	 *            ������λ����
	 * @return ���·������Ӧ�ľ���·��
	 * @throws IOException
	 *             ��Ϊ����������ѯ�ļ�ϵͳ�����Կ����׳�IO�쳣
	 */
	public static String getFullPathRelateClass(String relatedPath, Class cls)
			throws IOException {
		String path = null;
		if (relatedPath == null) {
			throw new NullPointerException();
		}
		String clsPath = getPathFromClass(cls);
		File clsFile = new File(clsPath);
		String tempPath = clsFile.getParent() + File.separator + relatedPath;
		File file = new File(tempPath);
		path = file.getCanonicalPath();
		return path;
	}

	/**
	 * ��ȡ���class�ļ�λ�õ�URL����������Ǳ���������ķ������������������á�
	 */
	private static URL getClassLocationURL(final Class cls) {
		if (cls == null)
			throw new IllegalArgumentException("null input: cls");
		URL result = null;
		final String clsAsResource = cls.getName().replace('.', '/').concat(
				".class");
		final ProtectionDomain pd = cls.getProtectionDomain();
		// java.lang.Class contract does not specify
		// if 'pd' can ever be null;
		// it is not the case for Sun's implementations,
		// but guard against null
		// just in case:
		if (pd != null) {
			final CodeSource cs = pd.getCodeSource();
			// 'cs' can be null depending on
			// the classloader behavior:
			if (cs != null)
				result = cs.getLocation();

			if (result != null) {
				// Convert a code source location into
				// a full class file location
				// for some common cases:
				if ("file".equals(result.getProtocol())) {
					try {
						if (result.toExternalForm().endsWith(".jar")
								|| result.toExternalForm().endsWith(".zip"))
							result = new URL("jar:".concat(
									result.toExternalForm()).concat("!/")
									.concat(clsAsResource));
						else if (new File(result.getFile()).isDirectory())
							result = new URL(result, clsAsResource);
					} catch (MalformedURLException ignore) {
					}
				}
			}
		}

		if (result == null) {
			// Try to find 'cls' definition as a resource;
			// this is not
			// document��d to be legal, but Sun's
			// implementations seem to //allow this:
			final ClassLoader clsLoader = cls.getClassLoader();
			result = clsLoader != null ? clsLoader.getResource(clsAsResource)
					: ClassLoader.getSystemResource(clsAsResource);
		}
		return result;
	}
	
	public static String getRootPath(Class cls){
		try{
			if( null == cls )
				cls = PathUtil.class;
			String classPath = getPathFromClass(cls);
			if( null == classPath )
				return null;
			String lowClassPath = classPath.toLowerCase();
			if( lowClassPath.endsWith(".jar")||
					lowClassPath.endsWith(".zip")){
				File file = new File(classPath);
				return file.getParent();
			}
			else{
				String className = cls.getName().replace('.',File.separatorChar);
				int index = classPath.lastIndexOf(className);
				if( index<0 )
					return null;
				return classPath.substring(0,index);
			}
		}
		catch( Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getConfigFilePath(String filename){
		try{
			//��⵱ǰ����Ŀ¼
			File file = new File(filename);
			if( file.exists() )
				return file.getCanonicalPath();
			
			//��⵱ǰ����Ŀ¼����Ŀ¼
			String curPath = System.getProperty("user.dir")+File.separator;
			file = new File(curPath+"config"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();
			file = new File(curPath+"configuration"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();
			file = new File(curPath+"cfg"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();
			
			//���·��
			String rootPath = getRootPath(null);
			if( null == rootPath )
				return null;
			if( rootPath.charAt(rootPath.length()-1) != File.separatorChar)
				rootPath += File.separator;
			String path = rootPath + filename;
			file = new File(path);
			if( file.exists() )
				return file.getCanonicalPath();
			//�����ļ�����class�ļ���Ŀ¼�¡���Ҫ���config��cfg��configurationĿ¼
			file = new File(rootPath+"config"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();
			file = new File(rootPath+"configuration"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();
			file = new File(rootPath+"cfg"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();

			//��⵱ǰ���Ŀ¼����һ��Ŀ¼
			file = new File(rootPath);
			rootPath = file.getParent()+File.separator;
			file = new File(rootPath+filename);
			if( file.exists() )
				return file.getCanonicalPath();
			
			//��⵱ǰ���Ŀ¼����һ��Ŀ¼���¼���Ŀ¼��config cfg configuration)
			file = new File(rootPath+"config"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();
			file = new File(rootPath+"configuration"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();
			file = new File(rootPath+"cfg"+File.separator+filename);
			if( file.exists() )
				return file.getCanonicalPath();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		try {
			System.out.println("path from class 'PathUtil.class'="+getPathFromClass(PathUtil.class));
			System.out.println("PathUtil's class root path="+getRootPath(PathUtil.class));
//			System.out.println("Logger's class root path="+getRootPath(Logger.class));
			System.out.println(getConfigFilePath("fas.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
