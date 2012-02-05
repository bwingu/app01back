package controllers;
 
import java.security.SecureRandom;
import java.util.*;
import models.Check;
import models.Role;
import models.SecurityIdent;
import models.User;

import org.apache.commons.lang.StringUtils;

import parser.Parser;
import play.Play;
import play.data.validation.Required;
import play.libs.Crypto;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Cookie;

import com.google.gson.Gson;
 
public class SecurityPlugin extends Controller {
    	
	/**
	 * check failed.
	 * @return false
	 */
	private static void checkFailed(){
		if(request.format.equals("json")){
			renderJSON("forbidden");
		}
			
		//redirection vers l'url de login pour le desktop
		redirect(Play.configuration.getProperty("application.login"));
	}
	
	/**
	 * Méthode d'authentification .
	 */
    public static boolean authenticate(@Required String username, @Required String password) {	

		SecurityIdent securityIdent = new SecurityIdent();
		
		//Si c'est un user invité
		if(("guest").equalsIgnoreCase(username) && ("guest").equalsIgnoreCase(password)){
			securityIdent.identStatus = true;
			Role roleGuest = new Role("guest");
			User userGuest = new User("guest", "guest");
			userGuest.roles = new ArrayList<Role>();
			userGuest.roles.add(roleGuest);
			securityIdent.userConnect = userGuest;
		}else{
			String urlAuthenticate = Play.configuration.getProperty("application.urlAdmin") + "AuthenticateAction/login?username=" + username + "&password=" + password + "&application=" + Play.configuration.getProperty("application.name");

			//Appel en webService de la partie pour l'authentification
			HttpResponse res = WS.url(urlAuthenticate).get();
			if(res.getStatus() != 200){
				return false; //TODO 
			}
			securityIdent = new Gson().fromJson(res.getJson(), SecurityIdent.class);
		}
		
		if(securityIdent.identStatus){
			//On mets en session le user
			session.put("user", new Gson().toJson(securityIdent.userConnect).toString());
			//On enregistre dans un cookie le user pour une durée d'un mois
			response.setCookie("rememberme", Crypto.encryptAES(new Gson().toJson(securityIdent.userConnect).toString()), "30d");
			return true;
		}
		return false;
    }    

    /**
     * Méthode de vérification si le client est connecté
     * @return : le top.
     */
    @Before(unless={"login", "authenticate", "logout"})
    protected static void check() {
    	    	
       //Récupération de l'annotation sur la méthode	
       Check check = getActionAnnotation(Check.class);
    	
       if(check == null){
    	   //Récupération de l'annotation sur le controller
    	   check = getControllerInheritedAnnotation(Check.class);
       }
       
       //Récupération du user en session ou en cookie
       if(check != null){
    	   if (getUser() == null && !request.cookies.containsKey("rememberme")){
        	   checkFailed();
           }
    	       	   
           //Si le user en session n'existe pas on le récrée
           if(getUser() == null){
        	   	Cookie cookieCurrent = request.cookies.get("rememberme"); 
        		session.put("user", Crypto.decryptAES(cookieCurrent.value));
           }
           
           //Vérification que le user est autorisé à accéder à la méthode appelée	
           //Si check.roles == norole, on test pas les roles mais juste la connexion du user
           //L'utilisateur n'a pas le bon rôle, échec
           if(check.roles() != null && !check.roles().equals("norole")){
           	 if(!Parser.evaluate(getUser().roles, check.roles())){
        	   checkFailed();
           	}
           }
          
       }
    }
    
    
    //Permet d'obtenir le user en session
    @Check(roles="guest||members")
	public static User getUser(){
		return new Gson().fromJson(session.get("user"), User.class);
	}		
    	
	public static void login(@Required String username, @Required String password){
		if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
			renderJSON(false);
    	renderJSON(SecurityPlugin.authenticate(username, password));
    }

	public static void logout(){
	     session.clear();
		 response.removeCookie("rememberme");
		 renderJSON(true);
	}
	
	/**
	 * Permet de retourner un nombre aléatoire pour sécuriser le mdp.
	 */
	public static void secureRandom(){
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (Exception e) {
			//En cas de pb on retourne 0 pour ne pas bloquer l'ident
			renderJSON(0);
		}
		renderJSON(random.nextInt());
	}
	
}