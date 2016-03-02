package org.bimserver.changecompare;
 
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.modelcompare.ModelCompare;
import org.bimserver.plugins.modelcompare.ModelCompareException;
import org.bimserver.plugins.modelcompare.ModelComparePlugin;
import org.bimserver.plugins.objectidms.ObjectIDMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructionChangeBasedModelComparePlugin implements ModelComparePlugin  {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConstructionChangeBasedModelComparePlugin.class);
	private boolean initialized;
	private PluginManager pluginManager;
	
	@Override
	public void init(PluginManager pluginManager) throws PluginException {
		this.pluginManager = pluginManager;
		initialized = true;
	}
	
	@Override
	public String getDescription() {
		return "Construction Change based compare";
	}
	
	@Override
	public String getDefaultName() {
		return "Construction Change based compare";
	}
	
	@Override
	public String getVersion() {
		return "0.1";
	}
	
	@Override
	public ObjectDefinition getSettingsDefinition() {
		return null;
	}
	
	@Override
	public boolean isInitialized() {
		return initialized;
	}
	
	@Override
	public ModelCompare createModelCompare(PluginConfiguration pluginConfiguration) throws ModelCompareException {
		try {
			return new ContructionChangeCompare(pluginManager.requireObjectIDM());
		} catch (ObjectIDMException e) {
			throw new ModelCompareException(e);
		}
	}

}