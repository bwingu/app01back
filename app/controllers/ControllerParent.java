package controllers;

import java.util.ArrayList;
import java.util.Properties;

import models.User;
import play.Play;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Http.Request;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import play.mvc.ActionInvoker;
import play.utils.Java;
import java.util.List;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Finally;
import play.mvc.With;

public class ControllerParent extends SecurityPlugin {
	
	//Ce champs sert seulement à avoir une meilleure compréhension lors de l'accès aux properties.
	protected static Properties properties = Play.configuration;
			
	@Before 
	private static void setCORS() {		
		//if(Play.mode.isDev()){
			Http.Header hd = new Http.Header(); 
			hd.name = "Access-Control-Allow-Origin"; 
			hd.values = new ArrayList<String>(); 
			hd.values.add("*"); 
			Http.Response.current().headers.put("Access-Control-Allow-Origin",hd);		
		//}
	}
	
	private static class ClassObject {
		String name;
		List<MethodObject> methods = new ArrayList<MethodObject>();
	}
	
	private static class MethodObject {
		String name;
		String returnType;
		List<ArgObject> args = new ArrayList<ArgObject>();		
	}
	
	private static class ArgObject {
		String name;
		String type;
	}
	
	public static void api(){
		
		ClassObject classObject = new ClassObject();
		Class currentController = getControllerClass();
		classObject.name = currentController.getName();
		List<Method> methods = findActionMethods(currentController);
	
		for (Method method : methods) {
		  	MethodObject methodObject = new MethodObject();
			methodObject.name = method.getName();
			methodObject.returnType = Java.rawJavaType(method.getReturnType());
			//Get args :
			for (Class clazz : method.getParameterTypes()) {
		        ArgObject args = new ArgObject();
				//args.name = variable.getName();
				args.type = Java.rawJavaType(clazz);
				methodObject.args.add(args);
		    }
			
			classObject.methods.add(methodObject);
		}
		renderJSON(classObject);
	}
	
	private static List<Method> findActionMethods(Class clazz) {
		List<Method> methods = new ArrayList<Method>();
        while (!clazz.getName().equals("java.lang.Object")) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (Modifier.isPublic(m.getModifiers())) {
                    // Check that it is not an intercepter
                    if (!m.isAnnotationPresent(Before.class) && !m.isAnnotationPresent(After.class) && !m.isAnnotationPresent(Finally.class)) {
                        methods.add(m);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return methods;
    }
		
}