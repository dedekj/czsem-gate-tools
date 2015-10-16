package czsem.gate.plugins;

import java.util.HashSet;

import czsem.gate.AbstractAnnotationDependencyMarker;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Utils;
import gate.creole.ExecutionException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;

@CreoleResource(name = "czsem AnnotationDependencySubtreeMarker", comment = "Marks the whole subtree for the union of all 'tokens' inside a given annotation.")
public class AnnotationDependencySubtreeMarker extends AbstractAnnotationDependencyMarker
{
	private static final long serialVersionUID = 671833725416131750L;

	private int maxDepth = Integer.MAX_VALUE;

	@Override
	public void execute() throws ExecutionException
	{
		initBeforeExecute();
		
		AnnotationSet inputAnnotations = inputAS.get(new HashSet<String>(inputAnnotationTypeNames));
		for (Annotation annotation : inputAnnotations)
		{			
			try {
				markAnnotationDependencySubtree(annotation);
			} catch (InvalidOffsetException e) {
				throw new ExecutionException(e);
			}			
		}
	}

	public static class AnnotationOffsetMerge {
		public long start_offset;
		public long end_offset;		
		
		
		public AnnotationOffsetMerge(long startOffset, long endOffset) {
			start_offset = startOffset;
			end_offset = endOffset;
		}

		public AnnotationOffsetMerge()
		{
			this(Long.MAX_VALUE, Long.MIN_VALUE);
		}
		
		public AnnotationOffsetMerge(AnnotationSet as) {
			this(Utils.start(as), Utils.end(as));
		}

		public AnnotationOffsetMerge(Annotation annotation)
		{
			this(
					annotation.getStartNode().getOffset(),
					annotation.getEndNode().getOffset());			
		}

		public void mergeWith(AnnotationOffsetMerge other)
		{
			start_offset = Math.min(start_offset, other.start_offset);
			end_offset = Math.max(end_offset, other.end_offset);
		}
	}
	

	public static class SubtreeMarkInfo extends AnnotationOffsetMerge
	{
		public FeatureMap fm;
		
		public SubtreeMarkInfo(long startOffset, long endOffset) {
			super(startOffset, endOffset);
			this.fm = Factory.newFeatureMap();
		}

		public SubtreeMarkInfo(FeatureMap fm)
		{
			this();
			this.fm.putAll(fm);
		}

		public SubtreeMarkInfo()
		{
			super();
			this.fm = Factory.newFeatureMap();
		}
		
		public SubtreeMarkInfo(Annotation annotation)
		{
			this(
					annotation.getStartNode().getOffset(),
					annotation.getEndNode().getOffset());
			
			fm.put("root_id", annotation.getId());
			fm.put("root_type", annotation.getType());
		}


		public void mergeWith(SubtreeMarkInfo info)
		{
			fm.putAll(info.fm);
			super.mergeWith(info);
		}
	}
	
	protected void markAnnotationDependencySubtree(Annotation annotation) throws InvalidOffsetException
	{		
		SubtreeMarkInfo info = findAnnotationDependencySubtree(annotation);
		if (info == null) return;
		
		String orig_type_name = annotation.getType();
		
		String new_type_name = orig_type_name.endsWith("_root")	?
								orig_type_name.substring(0, orig_type_name.length() - 5)
								: orig_type_name + "_subtree";
				
		outputAS.add(
				info.start_offset,
				info.end_offset,
				new_type_name,
				info.fm);

		
	}

	public SubtreeMarkInfo findAnnotationDependencySubtree(Annotation annotation) throws InvalidOffsetException
	{
		AnnotationSet tokens = inputTokensAS.getContained(
				annotation.getStartNode().getOffset(),
				annotation.getEndNode().getOffset());
		
		if (tokens.isEmpty()) return null;
		
		SubtreeMarkInfo info = new SubtreeMarkInfo(annotation.getFeatures());
		for (Annotation token : tokens)
		{
			info.mergeWith(findAnnotationDependencySubtreeForSingleToken(token.getId(), 0));			
		}
		
		info.fm.put("origSubTrID", annotation.getId());
		info.fm.put("origSubTrType", annotation.getType());
		
		return info;
	}

	public SubtreeMarkInfo findAnnotationDependencySubtreeForSingleToken(Integer tokenID) {
		return findAnnotationDependencySubtreeForSingleToken(tokenID, 0);
	}

	protected SubtreeMarkInfo findAnnotationDependencySubtreeForSingleToken(Integer tokenID, int depth)
	{
		SubtreeMarkInfo this_token_info = new SubtreeMarkInfo(inputTokensAS.get(tokenID));
		
		Iterable<Integer> children = treeIndex.getChildren(tokenID);		
		if (depth >= getMaxDepth() || children == null) return this_token_info;
		
		for (Integer childID : children)
		{
			this_token_info.mergeWith(
					findAnnotationDependencySubtreeForSingleToken(childID, depth+1));
					//recursive call
		}
		
		return this_token_info;		
	}

	public Integer getMaxDepth() {
		return maxDepth;
	}

	@CreoleParameter(defaultValue="2147483647")
	@RunTime
	public void setMaxDepth(Integer maxDepth) {
		this.maxDepth = maxDepth;
	}

}
