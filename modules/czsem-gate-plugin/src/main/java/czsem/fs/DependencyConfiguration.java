package czsem.fs;

import java.util.Arrays;
import java.util.Collection;

import czsem.fs.FSSentenceWriter.TokenDependecy;

public class DependencyConfiguration {
	private Collection<String> dependencyNames;
	private Iterable<TokenDependecy> tokenDepDefs;

	public DependencyConfiguration(Collection<String> dependencyNames,	Iterable<TokenDependecy> tokenDepDefs) {
		this.setDependencyNames(dependencyNames);
		this.setTokenDepDefs(tokenDepDefs);
	}
	
	public static DependencyConfiguration defaultConfigSelected = 
		new DependencyConfiguration (
			Arrays.asList(new String [] {
					"tDependency", "a/lex.rf", "Dependency", }), 
			Arrays.asList(new TokenDependecy [] {
					new TokenDependecy("tToken", "lex.rf"),}));

	public static DependencyConfiguration defaultConfigAvailable = 
			new DependencyConfiguration (
				Arrays.asList(new String [] {
						"aDependency", "nDependency", "a/aux.rf", "auxRfDependency", "a.rf", "coref_gram.rf" }), 
				Arrays.asList(new TokenDependecy [0]));
	
	/*
	public void putToConfig(DependencySetting setting) {
		setting.clear();
		
		setting.getDependencyTypes().addAll(getDependencyNames());
		Set<String> tocs = setting.getTokenDependencies();
		for (TokenDependecy tocDep :getTokenDepDefs())
		{
			tocs.add(tocDep.tokenTypeName +"."+ tocDep.depFeatureName);				
		}
		
	}

	public static DependencyConfig getDependencyConfig() {
		DependencyConfig depsCfg;
		Config cfg = null; 
		
		try {
			cfg = Config.getConfig();
			depsCfg = cfg.getDependencyConfig();
			if (depsCfg != null) return depsCfg;
		} catch (ConfigLoadException e) {}
		
		depsCfg = new DependencyConfig();

		if (cfg != null) {
			cfg.setDependencyConfig(depsCfg);			
		}
		
		defaultConfigSelected.putToConfig(depsCfg.getSelected());		
		return depsCfg;
	}

	*/

	public static DependencyConfiguration getSelectedConfigurationFromConfigOrDefault() {
		/*
		DependencyConfig depsCfg = getDependencyConfig();
		List<TokenDependecy> tokenDepDefs = new ArrayList<FSSentenceWriter.TokenDependecy>(depsCfg.getSelected().getTokenDependencies().size());
		
		for (String s : depsCfg.getSelected().getTokenDependencies()) {
			String[] split = s.split("\\.", 2);
			if (split.length < 2) continue;
			tokenDepDefs.add(new TokenDependecy(split[0], split[1]));
		}
		return new DependencyConfiguration(depsCfg.getSelected().getDependencyTypes(), tokenDepDefs);
		*/
		return defaultConfigSelected;
	}

	public Collection<String> getDependencyNames() {
		return dependencyNames;
	}

	public void setDependencyNames(Collection<String> dependencyNames) {
		this.dependencyNames = dependencyNames;
	}

	public Iterable<TokenDependecy> getTokenDepDefs() {
		return tokenDepDefs;
	}

	public void setTokenDepDefs(Iterable<TokenDependecy> tokenDepDefs) {
		this.tokenDepDefs = tokenDepDefs;
	}
}