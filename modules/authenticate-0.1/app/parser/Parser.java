package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Role;

public class Parser {

	static public boolean evaluate(List<Role> rolesUser, String expression){
		List<Boolean> listResult = new ArrayList<Boolean>();
		
		//On traite les regoupement avant
		Pattern patternGroup = Pattern.compile("(\\([a-z&|]*\\)*)");
		Matcher m = patternGroup.matcher(expression);
		
		//Pas de groupement on regarde les && et ou
		if(expression.indexOf("(") == -1){
			return parseSimpleExpression(rolesUser, expression);
		}else{
			 while(m.find()){
				listResult.add(parseSimpleExpression(rolesUser, m.group().replaceAll("\\(", "").replaceAll("\\)", "")));
			 }
			 
			 //Nettoyage de la chaine pour ne garder que les signes
			 expression = expression.replaceAll("\\([a-z&|]*\\)", "").trim();
			 
			 if(expression.length() == 0){
				 return listResult.get(0);
			 }
			 
			 if(expression.indexOf("|") != -1){
				 //Il y a au moins un résultat positif, donc ok 
				return listResult.contains(true);
			 }
			 
			 if(expression.indexOf("&") != -1){
				 if(listResult.contains(false)){
					 return false; 
				 }
				 return true;
			 }
		}
		return false;
	}
	
	//Permet de tester si l'expression simple est correcte que des && ou ||
	/**
	 * Il ne peut y avoir que des && ou des || car sinon on mets des parenthèses .
	 * @param rolesUser
	 * @param expression
	 * @return
	 */
	static private boolean parseSimpleExpression(List<Role> rolesUser, String expression){
		//Pour tester les roles check
		Role roleCheck = new Role();
		
		//Si pas de signe
		if(expression.indexOf("&&") == -1 && expression.indexOf("||") == -1){
			roleCheck.name = expression.trim();
			if(rolesUser.contains(roleCheck)){
				return true;
			}
			return false;
		}
		
		
		
		//Pour les &&
		String[] tab = expression.split("&&");
		if(tab.length != 1){
			for(int i = 0; i < tab.length; i++){
				roleCheck.name = tab[i];
				if(!rolesUser.contains(roleCheck)){
					return false;
				}
			}
			return true;
		}
		
		//Pour les ||
		tab = expression.split("\\|\\|");	
		for(int i = 0; i < tab.length; i++){
			roleCheck.name = tab[i];
			if(rolesUser.contains(roleCheck)){
				return true;
			}
		}
		return false;
	}
	
}
