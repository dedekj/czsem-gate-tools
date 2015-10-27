package czsem.fs.query;

public interface FSQueryBuilder {

	void beginChildren();
	void endChildren();

	void addNode();
	void addRestriction(String comparartor, String arg1, String arg2);
	
}
