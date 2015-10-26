package czsem.gate.externalannotator;

import gate.util.InvalidOffsetException;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import czsem.gate.externalannotator.Annotator.Annotable;

public class RecursiveEntityAnnotator {
	
	public static interface SecondaryEntity extends Annotable {
		boolean annotate(AnnotatorInterface annotator) throws InvalidOffsetException;		
	}
	
	protected Deque<SecondaryEntity> entityQueue = new LinkedList<SecondaryEntity>();
	
		
	public void storeForLater(SecondaryEntity entity) {
		if (entity == null) return;
		entityQueue.addLast(entity);					
	}
	
	public void storeForLater(List<? extends SecondaryEntity> entities) {
		for (SecondaryEntity secondaryEntity : entities) {
			storeForLater(secondaryEntity);
		}
	}

	
	protected SecondaryEntity getNextUnprocessedEntity() {
		for(;;)
		{
			SecondaryEntity entity = entityQueue.pollFirst();
			if (entity == null) return null;
			return entity;
		}
	}
	
	public void annotateSecondaryEntities(AnnotatorInterface annotator) throws InvalidOffsetException
	{
		int lastSize = entityQueue.size();
		int unseenElements = lastSize; 
		for (;;) {
			
			SecondaryEntity entity = getNextUnprocessedEntity();
			if (entity == null) break;
			
			if (! entity.annotate(annotator))
			{
				storeForLater(entity);				
			}
			
			unseenElements--;
			
			if (unseenElements <= 0) {
				if (lastSize == entityQueue.size()) break;
				else { 
					unseenElements = lastSize = entityQueue.size();					 
				}
			}
		}			
	}
}
