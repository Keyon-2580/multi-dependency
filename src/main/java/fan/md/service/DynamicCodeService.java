package fan.md.service;

import fan.md.model.entity.code.Function;
import fan.md.model.entity.dynamic.CallNode;

public interface DynamicCodeService {
	
	
	public CallNode findCallTree(Function function, int depth);
	
}
