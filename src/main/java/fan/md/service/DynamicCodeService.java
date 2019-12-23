package fan.md.service;

import fan.md.model.node.code.Function;
import fan.md.model.node.dynamic.CallNode;

public interface DynamicCodeService {
	
	
	public CallNode findCallTree(Function function, int depth);
	
}
