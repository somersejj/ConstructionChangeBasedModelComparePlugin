package org.bimserver.changecompare;

import java.util.ArrayList;
import java.util.List;

import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.models.ifc2x3tc1.IfcBuildingElement;
import org.bimserver.models.ifc2x3tc1.IfcRelConnectsElements;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.models.store.CompareResult;
import org.bimserver.models.store.CompareType;
import org.bimserver.models.store.ObjectAdded;
import org.bimserver.models.store.ObjectModified;
import org.bimserver.models.store.ObjectRemoved;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.modelcompare.ModelCompareException;
import org.bimserver.plugins.objectidms.ObjectIDM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;

public class ContructionChangeCompare extends AbstractModelCompare{

	private static final Logger LOGGER = LoggerFactory.getLogger(ContructionChangeCompare.class);

	public ContructionChangeCompare(ObjectIDM objectIDM) {
		super(objectIDM);
	}
	
	public CompareResult compare(IfcModelInterface model1, IfcModelInterface model2, CompareType compareType) throws ModelCompareException {
		CompareResult result = StoreFactory.eINSTANCE.createCompareResult();
		
		LOGGER.info("Comparing started ...");

		// get all ifc elements from model 1
        List<IdEObject> elements1 = new ArrayList<IdEObject>();
        BiMap<Long, ? extends IdEObject> objects1 = model1.getObjects();
        for (Long objectId : objects1.keySet())
        {
          IdEObject object = model1.get(objectId.longValue());
          if (object instanceof IfcBuildingElement) 
          {
        	  elements1.add(object);
          }
        }
        List<IdEObject> elements2 = new ArrayList<IdEObject>();
        BiMap<Long, ? extends IdEObject> objects2 = model2.getObjects();
        for (Long objectId : objects2.keySet())
        {
          IdEObject object = model2.get(objectId.longValue());
          if (object instanceof IfcBuildingElement) 
          {
        	  elements2.add(object);
          }
        }
		// get all ifcelements from model 2 
		
        List<IdEObject> addedList = listAddedElements(elements1, elements2);
        List<IdEObject> removedList = listAddedElements(elements2, elements1);
       
		elements1.removeAll(addedList);
		elements2.removeAll(removedList);
		
		// now compare the 2 model without the added or removed objects
		List<IdEObject> changedList = compareComplete(elements1,elements2);
		
		for (IdEObject element : addedList)
		{		
			IdEObject eObject = model1.get(element.getOid());
			ObjectAdded objectAdded = StoreFactory.eINSTANCE.createObjectAdded();
			objectAdded.setDataObject(makeDataObject(eObject));
			getCompareContainer(element.eClass()).getItems().add(objectAdded);
		}
		for (IdEObject element : removedList)
		{
			IdEObject eObject = model2.get(element.getOid());
			ObjectRemoved objectRemoved = StoreFactory.eINSTANCE.createObjectRemoved();
			objectRemoved.setDataObject(makeDataObject(eObject));
			getCompareContainer(element.eClass()).getItems().add(objectRemoved);
		}
		for (IdEObject element : changedList)
		{
			IdEObject eObject = model1.get(element.getOid());
			ObjectModified objectModified = StoreFactory.eINSTANCE.createObjectModified();
			objectModified.setDataObject(makeDataObject(eObject));
			getCompareContainer(element.eClass()).getItems().add(objectModified);
		}
		LOGGER.info("ComparResult");
		LOGGER.info("Added : ");
		for (IdEObject element : addedList)
		{	
			LOGGER.info("      " + model1.get(element.getOid()));
		}
		LOGGER.info("Removed : ");
		for (IdEObject element : removedList)
		{	
			LOGGER.info("      " + model2.get(element.getOid()));
		}
		LOGGER.info("Changed : ");
		for (IdEObject element : changedList)
		{	
			LOGGER.info("      " + model1.get(element.getOid()));
		}
		

		return result;
	}
	
	private List<IdEObject> listAddedElements(List<IdEObject> newList , List<IdEObject> oldList)
	{
		
		List<IdEObject> newElementsList = new ArrayList<IdEObject>();
		for (IdEObject element : newList)
		{
			if (!oldList.contains(element))
			{
				newElementsList.add(element);
			}
			else
			{
				this.LOGGER.info("Object is in oldlist :  " + element);
			}
		}
		return newElementsList;
	}
	
	private List<IdEObject> compareComplete(List<IdEObject> list1, List<IdEObject> list2)
	{
		boolean changed = false;
		List<IdEObject> changedList = new ArrayList<IdEObject>(); 
		for (IdEObject element1 : list1)
		{
			
			IdEObject element2 = list2.get(list2.indexOf(element1));
		/*for (IfcRelConnectsElements connectedTo1 :  element1.getConnectedTo())
			{
				for (IfcRelConnectsElements connectedTo2 :  element2.getConnectedTo())
				{ 
				  if (!connectedTo1.getRelatedElement().equals(connectedTo2.getRelatedElement()))
				  {
					  changed = true;
				  }
				  if (!connectedTo1.getRelatingElement().equals(connectedTo2.getRelatingElement()))
				  {
					  changed = true;				  
				  }
				  if (!connectedTo1.getConnectionGeometry().equals(connectedTo2.getConnectionGeometry()))
				  {
					  changed = true;					  
				  }
			    }
			}
			for (IfcRelConnectsElements connectedFrom1 :  element1.getConnectedFrom())
			{
				for (IfcRelConnectsElements connectedFrom2 :  element2.getConnectedFrom())
				{ 
				  if (!connectedFrom1.getRelatedElement().equals(connectedFrom2.getRelatedElement()))
				  {
					  changed = true;					  
				  }
				  if (!connectedFrom1.getRelatingElement().equals(connectedFrom2.getRelatingElement()))
				  {
					  changed = true;					  
				  }
				  if (!connectedFrom1.getConnectionGeometry().equals(connectedFrom2.getConnectionGeometry()))
				  {
					  changed = true;					  
				  }
			    }
			}*/

			if (!element1.equals(element2))
			{
				  changed = true;
			}
			
			if (changed)
			{
				changedList.add(element1);
			}
		}
		return changedList;
	}

}
