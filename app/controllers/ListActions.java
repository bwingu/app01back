package controllers;

import java.util.ArrayList;
import java.util.List;
import models.Element;
import models.ListElement;
import play.*;
import play.mvc.*;
import play.data.validation.*;

public class ListActions extends ControllerParent {

	//Permet de créer une nouvelle liste
    public static void createList(@Required String text) {
		
		if(validation.hasErrors()) {
			//TODO: Gestion des erreurs : à compléter.
			Logger.error("erreur ! ");
		}
	
    	ListElement newListElement = new ListElement(text);    	    	    
		//Enregistrement de la nouvelle Liste
    	newListElement.save();

        if(request.format.equals("json")){
			Logger.info("retour JSON");
          	renderJSON(newListElement);
        }
        showList(newListElement.uuid);
    }

    //Permet de visualiser la liste
    public static void showList(@Required String idList) {
		
		if(validation.hasErrors()) {
			//TODO: Gestion des erreurs : à compléter.
			Logger.error("erreur ! ");
		}
				
		ListElement list = ListElement.findById(idList);
	
		notFoundIfNull(list);
		
		Logger.info("list : " + list.toString());
		
		List<Element> elements = list.elements;
		
		if(request.format.equals("json")){
			Logger.info("retour JSON");
			renderJSON(list);
		}
		Logger.info("retour groovy");
		render(list, elements);

    }

	public static void showAllLists(){
		List<ListElement> lists = ListElement.find("order by dateUpdate").fetch();
		renderJSON(lists);
	}
    
    public static void showAll(){
    	renderJSON("Liste :" + ListElement.findAll() + "Element : " + Element.findAll());
    }
}