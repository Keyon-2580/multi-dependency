package cn.edu.fudan.se.multidependency.model.relation.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.lib.Library;
import cn.edu.fudan.se.multidependency.model.node.lib.LibraryAPI;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author fan
 * 一个节点调用了哪些三方库，分别调用了三方库的什么API
 */
public class CallLibrary {

	@Getter
	@Setter
	private Node caller;
	
	private Map<Library, Set<LibraryAPI>> callLibraryToAPIs = new HashMap<>();

	private Map<LibraryAPI, Integer> callAPITimes = new HashMap<>();
	
	public void addLibraryAPI(LibraryAPI api, Library belongToLibrary) throws Exception {
		addLibraryAPI(api, belongToLibrary, 1);
	}
	
	public void addLibraryAPI(LibraryAPI api, Library belongToLibrary, int times) throws Exception {
		if(times <= 0) {
			throw new Exception("error times: " + times);
		}
		Set<LibraryAPI> apis = callLibraryToAPIs.getOrDefault(api, new HashSet<>());
		apis.add(api);
		this.callLibraryToAPIs.put(belongToLibrary, apis);
		
		Integer previousTimes = callAPITimes.getOrDefault(apis, 0);
		callAPITimes.put(api, previousTimes + times);
	}
	
}
