package fan.md.model.relation.code;

import java.io.Serializable;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import fan.md.model.entity.code.Function;

@RelationshipEntity("FUNCTION_CALL_FUNCTION")
public class FunctionCallFunction implements Serializable {
	
	private static final long serialVersionUID = 5982413005555063698L;

	@StartNode
	private Function function;
	
	@EndNode
	private Function callFunction;
	
	@Id
    @GeneratedValue
    private Long id;
	

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public Function getCallFunction() {
		return callFunction;
	}

	public void setCallFunction(Function callFunction) {
		this.callFunction = callFunction;
	}
	
	
	
}
