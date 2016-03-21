package org.bimserver.changecompare;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bimserver.emf.IdEObject;
import org.bimserver.emf.IfcModelInterface;
import org.bimserver.models.ifc2x3tc1.Ifc2x3tc1Package;
import org.bimserver.models.ifc2x3tc1.IfcBuildingElement;
import org.bimserver.models.store.CompareItem;
/*import org.bimserver.models.ifc2x3tc1.IfcBuildingElementComponent;
import org.bimserver.models.ifc2x3tc1.IfcBuildingElementProxy;
import org.bimserver.models.ifc2x3tc1.IfcColumn;
import org.bimserver.models.ifc2x3tc1.IfcCovering;
import org.bimserver.models.ifc2x3tc1.IfcCurtainWall;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.models.ifc2x3tc1.IfcFooting;
import org.bimserver.models.ifc2x3tc1.IfcMember;
import org.bimserver.models.ifc2x3tc1.IfcPile;
import org.bimserver.models.ifc2x3tc1.IfcPlate;
import org.bimserver.models.ifc2x3tc1.IfcRailing;
import org.bimserver.models.ifc2x3tc1.IfcRamp;
import org.bimserver.models.ifc2x3tc1.IfcRampFlight;
import org.bimserver.models.ifc2x3tc1.IfcRelConnectsElements;
import org.bimserver.models.ifc2x3tc1.IfcRoof;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.models.ifc2x3tc1.IfcStair;
import org.bimserver.models.ifc2x3tc1.IfcStairFlight;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.models.ifc2x3tc1.IfcWindow;*/
import org.bimserver.models.store.CompareResult;
import org.bimserver.models.store.CompareType;
import org.bimserver.models.store.ObjectAdded;
import org.bimserver.models.store.ObjectModified;
import org.bimserver.models.store.ObjectRemoved;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.modelcompare.ModelCompareException;
import org.bimserver.plugins.objectidms.ObjectIDM;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
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
        List<IdEObject> addedList = new ArrayList<IdEObject>();
        List<IdEObject> removedList  = new ArrayList<IdEObject>();
		List<String> checkChangedGuidList = new ArrayList<String>();
		
		for (EClassifier eClassifier : Ifc2x3tc1Package.eINSTANCE.getEClassifiers()) {
			
			if (eClassifier instanceof EClass && Ifc2x3tc1Package.eINSTANCE.getIfcBuildingElement().isSuperTypeOf((EClass) eClassifier))
			{
				EClass eClass = (EClass) eClassifier;
				Set<String> objects1 =  model1.getGuids(eClass);
				Set<String> objects2 =  model2.getGuids(eClass);
       
				if (compareType == CompareType.ALL || compareType == CompareType.ADD || compareType == CompareType.MODIFY) 
				{
					for (String guid : objects1)
					{
						IdEObject eObject = model2.getByGuid(guid);
						
						if (eObject == null)
						{
						
							if (compareType == CompareType.ALL || compareType == CompareType.ADD) 
							{
								IdEObject addEObject = model1.getByGuid(guid);
								addedList.add(addEObject);	
							}
						}
						else
						{
							// for now add to list , later compare objects and if changed add to the list of remained objects
							if (compareType == CompareType.ALL || compareType == CompareType.MODIFY) 
							    checkChangedGuidList.add(guid);
						}
					}
				}
				if (compareType == CompareType.ALL || compareType == CompareType.DELETE) {
					 
					for (String guid : objects2)
					{
						IdEObject eObject = model1.getByGuid(guid);
						IdEObject eObject2 = model2.getByGuid(guid);
						if (eObject == null)
						{
							removedList.add(eObject2);	
						}
					}
				}
			}
		}
		
		// Add all to the CompareContainer
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

		// Loop changeList to compare object to object
		for (String guid : checkChangedGuidList)
		{
			IdEObject eObject1 = model1.getByGuid(guid);
			IdEObject eObject2 = model2.getByGuid(guid);
			compareEObjects(eObject1.eClass(), eObject1, eObject2, result, compareType);
		}		
		
	    return result;
	}

}
