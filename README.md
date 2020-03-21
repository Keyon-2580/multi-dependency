在/src/main/resources中，复制一份application-example.yml，并改名，如：application-zhou.yml、application-huang.yml、application-fan.yml，然后修改里面的路径。

然后修改application.yml，将active的值改为对应的名字。

## 运行InsertDataMain添加的节点和关系

### 静态分析

节点：

- Project
- MicroService
- Package
- ProjectFile
- Type
- Function
- Variable

关系：

- Contain
  - MicroService -> Project
  - Project -> Package
  - Package -> ProjectFile
  - ProjectFile -> Type
  - ProjectFile -> Function
  - ProjectFile -> Variable
  - Type -> Function
  - Type -> Variable
  - Function -> Variable
  - ...
  - * Project *1-> Package
  - * Project *2-> ProjectFile
  - * Project *3-> Type
  - * Project *3..4-> Function
  - * Project *3..5-> Variable
  - * ProjectFile *1..2-> Function
- FileImportFunction
- FileImportType
- FileImportVariable
- FileIncludeFile
- FunctionCallFunction
- FunctionCastType
- FunctionParameterType
- FunctionReturnType
- FunctionThrowType
- NodeAnnotationType
  - Type -> Type
  - Function -> Type
  - Variable -> Type
- TypeCallFunction
- TypeInheritsType
- VariableIsType
- VariableTypeParameterType（类型参数，如List<Type>）

### 动态分析

#### 动态运行日志分析

节点：

- Trace
- Span

关系：

- Contain
  - Trace -> Span
- MicroServiceCreateSpan
- SpanCallSpan
- TraceRunWithFunction
- SpanStartWithFunction
- FunctionDynamicCallFunction

#### 特性-测试用-trace对应

节点：

- Feature
- TestCase

关系：

- Contain
  - Feature -> Feature （子特性）
- TestCaseExecuteFeature
- TestCaseRunTrace
