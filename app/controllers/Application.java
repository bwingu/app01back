package controllers;

import play.data.validation.Required;
import models.SecurityIdent;
import play.mvc.Controller;

public class Application extends ControllerParent {

	public static void index(){
		render();
	}
	
}
