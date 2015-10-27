package czsem.fs.query;

import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import czsem.fs.query.constants.MetaAttribute;
import czsem.fs.query.restrictions.ChildrenEvaluator;
import czsem.fs.query.restrictions.IterateSubtreeEvaluator;
import czsem.fs.query.restrictions.OptionalEvaluator;
import czsem.fs.query.restrictions.RestrictioinsConjunctionEvaluator;

public class FSQueryBuilderImpl implements FSQueryBuilder {
	private static final Logger logger = LoggerFactory.getLogger(FSQueryBuilderImpl.class);
	
	public FSQueryBuilderImpl() {
		curentParent = new QueryNode(); 
		curentNode = curentParent;
	}
	
	protected Stack<QueryNode> nodeStack = new Stack<QueryNode>();
	
	protected QueryNode curentParent; 
	protected QueryNode curentNode;

	@Override
	public void addNode() {
		logger.debug("addNode");

		curentNode = new QueryNode(RestrictioinsConjunctionEvaluator.restrictioinsConjunctionEvaluatorInstance);
		curentParent.addChild(curentNode);		
	}

	@Override
	public void beginChildren() {
		logger.debug("beginChildren");
		
		curentNode.setEvaluator(ChildrenEvaluator.childrenEvaluatorInstance);
		
		nodeStack.push(curentParent);
		curentParent = curentNode;		
	}

	@Override
	public void endChildren() {
		logger.debug("endChildren");
		
		curentParent = nodeStack.pop();
	}

	@Override
	public void addRestriction(String comparartor, String arg1,	String arg2) {
		logger.debug(String.format("addRestriction %s %s %s", arg1, comparartor, arg2));
		
		if (MetaAttribute.NODE_NAME.equals(arg1))
			curentNode.setName(arg2);
		else if (MetaAttribute.OPTIONAL.equals(arg1)){ 
			if (MetaAttribute.TRUE.equals(arg2))
				curentNode.setEvaluator(new OptionalEvaluator());
		}
		else if (IterateSubtreeEvaluator.META_ATTR_SUBTREE_DEPTH.equals(arg1))
		{
			int depth = Integer.parseInt(arg2); 
			curentNode.setEvaluator(new IterateSubtreeEvaluator(depth));
		}
		else
		{
			curentNode.addRestriction(comparartor, arg1, arg2);					
		}		
	}

	public QueryNode getRootNode() {
		return curentParent.children.iterator().next();
	}

}
