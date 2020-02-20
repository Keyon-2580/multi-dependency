package cn.edu.fudan.se.multidependency.service.dynamic;

public class CppDynamicInserterForNeo4jService extends DynamicInserterForNeo4jService {

	@Override
	protected void extractNodesAndRelations() throws Exception {
		/*Map<String, List<Function>> functions = this.getNodes().allFunctionsByFunctionName();
		List<String> cppFiles = new ArrayList<>();
		List<File> functionCallFiles = new ArrayList<>();
		List<File> callgrindFiles = new ArrayList<>();
		for(File f : dynamicFunctionCallFiles) {
			String suffix = FileUtils.extractSuffix(f.getAbsolutePath());
			if(".dot".equals(suffix)) {
				functionCallFiles.add(f);
			} else if(".out".equals(suffix)) {
				callgrindFiles.add(f);
			}
		}
		for(File cppFile : callgrindFiles) {
			///FIXME
//			cppFiles.addAll(CppDynamicUtil.extractFile(cppFile, getNodes().getProject().getProjectPath()));
		}
		for(File dynamicFile : functionCallFiles) {
			Map<String, List<String>> result = CppDynamicUtil.extractFunctionCall(dynamicFile);
			for(String start : result.keySet()) {
				List<String> ends = result.get(start);
				List<Function> startFunctions = functions.get(start);
				Function startFunction = null;
				if(startFunctions != null && startFunctions.size() != 0) {
					if(startFunctions.size() == 1) {
						startFunction = startFunctions.get(0);
					} else {
						for(Function f : startFunctions) {
							for(String cppFile : cppFiles) {
								if(cppFile.lastIndexOf(f.getInFilePath()) >= 0) {
									startFunction = f;
								}
							}
						}
					}
				}
				if(startFunction == null) {
					if(startFunctions != null) {
						System.out.println("名为 " + start + " 的函数有 " + startFunctions.size() + " 个 " + startFunctions);
						for(Function f : startFunctions) {
							System.out.println(f.getInFilePath());
						}
					}
					continue;
				}
				for(String end : ends) {
					Function endFunction = null;
					List<Function> endFunctions = functions.get(end);
					if(endFunctions != null && endFunctions.size() != 0) {
						if(endFunctions.size() == 1) {
							endFunction = endFunctions.get(0);
						} else {
							for(Function f : endFunctions) {
								for(String cppFile : cppFiles) {
									if(cppFile.lastIndexOf(f.getInFilePath()) >= 0) {
										endFunction = f;
									}
								}
							}
						}
					}
					if(endFunction == null) {
						if(endFunctions != null) {
							System.out.println("名为 " + end + " 的函数有 " + endFunctions.size() + " 个 " + endFunctions);
							for(Function f : endFunctions) {
								System.out.println(f.getInFilePath());
							}
						}
						continue;
					} else {
						FunctionDynamicCallFunction dynamicCall = new FunctionDynamicCallFunction();
						dynamicCall.setFunction(startFunction);
						dynamicCall.setCallFunction(endFunction);
						addRelation(dynamicCall);
					}
				}
			}
		}*/
	}

}
