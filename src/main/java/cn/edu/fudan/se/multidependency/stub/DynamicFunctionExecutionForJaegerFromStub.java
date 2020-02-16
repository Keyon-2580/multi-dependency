package cn.edu.fudan.se.multidependency.stub;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DynamicFunctionExecutionForJaegerFromStub extends DynamicFunctionExecutionFromStub {
	protected String traceId;
	protected String spanId;
}
