package org.bimserver.changecompare;
  
import org.bimserver.interfaces.objects.SPluginType;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.models.store.StoreFactory;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.modelcompare.ModelCompare;
import org.bimserver.plugins.modelcompare.ModelCompareException;
import org.bimserver.plugins.modelcompare.ModelComparePlugin;
import org.bimserver.plugins.objectidms.ObjectIDMException;
import org.bimserver.shared.exceptions.PluginException;

public class ConstructionChangeBasedModelComparePlugin implements ModelComparePlugin  {

	private PluginContext pluginContext;

	
	@Override
	public ModelCompare createModelCompare(PluginConfiguration pluginConfiguration) throws ModelCompareException {
		try {
			return new ContructionChangeCompare(pluginContext.getDefaultObjectIDM());
		} catch (ObjectIDMException e) {
			throw new ModelCompareException(e);
		}	}

	@Override
	public void init(PluginContext pluginContext) throws PluginException {
		this.pluginContext = pluginContext;
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		return null;
	}

}