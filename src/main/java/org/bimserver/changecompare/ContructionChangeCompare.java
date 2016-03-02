package org.bimserver.changecompare;

import java.util.ArrayList;
import java.util.List;

import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.ifc.compare.AbstractModelCompare;
import org.bimserver.models.ifc2x3tc1.IfcBuildingElement;
import org.bimserver.models.ifc2x3tc1.IfcRelConnectsElements;
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

public class ContructionChangeCompare extends AbstractModelCompare{

	private static final Logger LOGGER = LoggerFactory.getLogger(ContructionChangeCompare.class);

	public ContructionChangeCompare(ObjectIDM objectIDM) {
		super(objectIDM);
	}
	
	public CompareResult compare(IfcModelInterface model1, IfcModelInterface model2, CompareType compareType) throws ModelCompareException {
		CompareResult result = StoreFactory.eINSTANCE.createCompareResult();
		
		LOGGER.info("Comparing started ...");

		// get all ifc elements from model 1
        List<IfcBuildingElement> elements1 = model1.getAll(IfcBuildingElement.class);
		
		// get all ifcelements from model 2 
        List<IfcBuildingElement> elements2 = model2.getAll(IfcBuildingElement.class);
		
        List<IfcBuildingElement> addedList = listAddedElements(elements1, elements2);
        List<IfcBuildingElement> removedList = listAddedElements(elements2, elements1);
       
		elements1.removeAll(addedList);
		elements2.removeAll(removedList);
		
		// now compare the 2 model without the added or removed objects
		List<IfcBuildingElement> changedList = compareComplete(elements1,elements2);
		
		for (IfcBuildingElement element : addedList)
		{		
			IdEObject eObject = model1.getByGuid(element.getGlobalId());
			ObjectAdded objectAdded = StoreFactory.eINSTANCE.createObjectAdded();
			objectAdded.setDataObject(makeDataObject(eObject));
			getCompareContainer(element.eClass()).getItems().add(objectAdded);
		}
		for (IfcBuildingElement element : removedList)
		{
			IdEObject eObject = model1.getByGuid(element.getGlobalId());
			ObjectRemoved objectRemoved = StoreFactory.eINSTANCE.createObjectRemoved();
			objectRemoved.setDataObject(makeDataObject(eObject));
			getCompareContainer(element.eClass()).getItems().add(objectRemoved);
		}
		for (IfcBuildingElement element : changedList)
		{
			IdEObject eObject = model1.getByGuid(element.getGlobalId());
			ObjectModified objectModified = StoreFactory.eINSTANCE.createObjectModified();
			objectModified.setDataObject(makeDataObject(eObject));
			getCompareContainer(element.eClass()).getItems().add(objectModified);
		}

		return result;
	}
	
	private List<IfcBuildingElement> listAddedElements(List<IfcBuildingElement> newList , List<IfcBuildingElement> oldList)
	{
		
		List<IfcBuildingElement> newElementsList = new ArrayList<IfcBuildingElement>();
        if (!oldList.retainAll(newList)) 
        {
        	return newElementsList;
        }
        else
        {
			for (IfcBuildingElement element : newList)
			{
				if (!oldList.contains(element))
				{
					newElementsList.add(element);
				}
			}
        }
		return newElementsList;
	}
	
	private List<IfcBuildingElement> compareComplete(List<IfcBuildingElement> list1, List<IfcBuildingElement> list2)
	{
		boolean changed = false;
		List<IfcBuildingElement> changedList = new ArrayList<IfcBuildingElement>(); 
		for (IfcBuildingElement element1 : list1)
		{
			
			IfcBuildingElement element2 = list2.get(list2.indexOf(element1));
			for (IfcRelConnectsElements connectedTo1 :  element1.getConnectedTo())
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
			}

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
