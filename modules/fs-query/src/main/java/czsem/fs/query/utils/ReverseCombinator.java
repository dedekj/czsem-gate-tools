package czsem.fs.query.utils;


public class ReverseCombinator extends Combinator {

	public ReverseCombinator(int size) {
		super(size);
	}

	@Override
	protected void init() {
		groupSize = size;
		initGroup(groupSize);
	}

	@Override
	public boolean tryMove() {
		if (groupSize < 1) return false;
		
		if (tryMove(groupSize-1)) return true;
		
		
		if (groupSize <= 1) return false;
		initGroup(groupSize-1);
		
		return tryMove();
	}


}
