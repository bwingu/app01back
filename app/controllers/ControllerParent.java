package controllers;

import java.util.Properties;

import play.Play;
import play.mvc.Controller;
import play.mvc.With;
@With({RequestHttp.class, SecurityPlugin.class, OpenApi.class, Auditor.class})
public class ControllerParent extends Controller{
	
	//Ce champs sert seulement à avoir une meilleure compréhension lors de l'accès aux properties.
	protected static Properties properties = Play.configuration;		
}