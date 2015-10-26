package czsem.gate.plugins;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleResource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;

import czsem.Utils;
import czsem.gate.AbstractAnnotationDependencyMarker;

@CreoleResource(name = "czsem AnnotationDependencyRootMarker", comment = "Finds a nearest common father annotation for all 'tokens' inside a given annotation.")
public class AnnotationDependencyRootMarker extends AbstractAnnotationDependencyMarker
{
	private static final long serialVersionUID = 8357007815773883611L;
	static Logger logger = Logger.getLogger(AnnotationDependencyRootMarker.class);
	
	
	protected static class DependencyBFSnode
	{
		DependencyBFSnode current_root;
		Integer annotationID;
		int tree_level;
		int distance;
				
		DependencyBFSnode up(Integer parentID)
		{
			return new DependencyBFSnode(parentID, tree_level+1, distance+1);			
		}

		DependencyBFSnode down(Integer parentID)
		{
			return new DependencyBFSnode(parentID, tree_level-1, distance+1);			
		}

		public DependencyBFSnode(Integer parentID, int treeLevel, int distance, DependencyBFSnode current_root)
		{
			this(parentID, treeLevel, distance);
			this.current_root = current_root;
		}

		public DependencyBFSnode(Integer parentID, int treeLevel, int distance)
		{
			this.current_root = this;
			this.annotationID = parentID;
			tree_level = treeLevel;
			this.distance = distance;
		}
	}

	protected static class SearchQueue
	{
		protected Queue<DependencyBFSnode> queueBFS= new ArrayDeque<DependencyBFSnode>();
		protected Set<Integer> visited_ids = new HashSet<Integer>();
		
		public void add(DependencyBFSnode node)
		{
			if (visited_ids.contains(node.annotationID)) return;
			
			queueBFS.add(node);
			visited_ids.add(node.annotationID);			
		}

		public boolean isEmpty() {
			return queueBFS.isEmpty();
		}

		public DependencyBFSnode remove() {
			return queueBFS.remove();
		}		
	}

	
	
	protected Annotation findRootBFS(Collection<Annotation> tokens_to_find)
	{		
		Annotation token = tokens_to_find.iterator().next();
		tokens_to_find.remove(token);
		DependencyBFSnode confirmed_root = new DependencyBFSnode(token.getId(), 0, 0);
				
		SearchQueue queueBFS = new SearchQueue(); 
		
		queueBFS.add(confirmed_root);
		
		while (! tokens_to_find.isEmpty() && ! isInterrupted() && ! queueBFS.isEmpty())
		{
			DependencyBFSnode currentBFSnode = queueBFS.remove();
			Integer currentID = currentBFSnode.annotationID;
			
			for (Annotation not_found_token : tokens_to_find)
			{
				if (not_found_token.getId().equals(currentID))
				{
					tokens_to_find.remove(not_found_token);
					if (confirmed_root.tree_level < currentBFSnode.tree_level)
					{
						confirmed_root = currentBFSnode.current_root; 						
					}
					break;
				}				
			}
			
			Integer parentID = treeIndex.getParent(currentID);
			if (parentID != null) queueBFS.add(currentBFSnode.up(parentID));
			
			Iterable<Integer> children = treeIndex.getChildren(currentID);
			if (children != null)
			{
				for (Integer childID : children)
				{
					queueBFS.add(currentBFSnode.down(childID));
				}
			}
			
		}
		
		return inputTokensAS.get(confirmed_root.annotationID);
	}
	
	@Override
	public void execute() throws ExecutionException
	{
//		logger.debug("RootMarker: " + document.getName());
		initBeforeExecute();
		
		AnnotationSet inputAnnotations = inputAS.get(Utils.setFromList(inputAnnotationTypeNames));
		for (Annotation annotation : inputAnnotations)
		{			
//			logger.debug(String.format("ann root: %s, id: %d", annotation.getType(), annotation.getId()));
			markAnnotationDependencyRoot(annotation);
		}
	}
	
	protected void markAnnotationDependencyRoot(Annotation annotation)
	{
		AnnotationSet tokens = inputTokensAS.getContained(
				annotation.getStartNode().getOffset(),
				annotation.getEndNode().getOffset());
		
		if (tokens.isEmpty()) return;
				
		
		Annotation root_token = findRootBFS(new ArrayList<Annotation>(tokens));		
		
		FeatureMap fm = Factory.newFeatureMap();
		fm.putAll(annotation.getFeatures());
		fm.put("rootID", root_token.getId());
		fm.put("rootType", root_token.getType());
		fm.put("origRootID", annotation.getId());
		fm.put("origRootType", annotation.getType());
		
		String orig_type_name = annotation.getType();
		
		String new_type_name = orig_type_name.endsWith("_subtree")	?
								orig_type_name.substring(0, orig_type_name.length() - 8)
								: orig_type_name + "_root";

		
		outputAS.add(
				root_token.getStartNode(),
				root_token.getEndNode(),
				new_type_name,
				fm);
		
	}


/*
	public static void main(String[] args) throws Exception
	{
		gate.Main.main(args);		
	}
*/
}
