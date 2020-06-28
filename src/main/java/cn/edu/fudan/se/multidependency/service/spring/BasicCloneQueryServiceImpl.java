package cn.edu.fudan.se.multidependency.service.spring;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.repository.node.clone.CloneGroupRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;

@Service
public class BasicCloneQueryServiceImpl implements BasicCloneQueryService {

    @Autowired
    CloneRepository cloneRepository;
    
    @Autowired
    ContainRelationService containRelationService;
    
    @Autowired
    StaticAnalyseService staticAnalyseService;
    
    @Autowired
    CacheService cacheService;
    
    @Autowired
    CloneGroupRepository cloneGroupRepository;

    private Map<CloneRelationType, Collection<Clone>> cloneTypeToClones = new ConcurrentHashMap<>();
	@Override
	public Collection<Clone> findClonesByCloneType(CloneRelationType cloneType) {
		Collection<Clone> result = cloneTypeToClones.get(cloneType);
		if(result == null) {
			result = cloneRepository.findAllClonesByCloneType(cloneType.toString());
			cloneTypeToClones.put(cloneType, result);
		}
		return result;
	}

	private Map<CloneRelationType, Collection<CloneGroup>> cloneTypeToGroups = new ConcurrentHashMap<>();
	@Override
	public Collection<CloneGroup> findGroupsContainCloneTypeRelation(CloneRelationType cloneType) {
		Collection<CloneGroup> result = cloneTypeToGroups.get(cloneType);
		if(result == null) {
			result = cloneRepository.findGroupsByCloneType(cloneType.toString());
			result.removeIf(group -> {
				Collection<Clone> clones = findGroupContainCloneRelations(group);
				boolean flag = false;
				for(Clone clone : clones) {
					if(!clone.getCloneRelationType().equals(cloneType.toString())) {
						flag = true;
						break;
					}
				}
				return flag;
			});
			cloneTypeToGroups.put(cloneType, result);
		}
		return result;
	}
	
	@Override
	public CloneGroup queryCloneGroup(long id) {
		Node node = cacheService.findNodeById(id);
		CloneGroup result = node == null ? cloneGroupRepository.findById(id).get() : (node instanceof CloneGroup ? (CloneGroup) node : cloneGroupRepository.findById(id).get());
		cacheService.cacheNodeById(result);
		return result;
	}

	private Map<CloneGroup, Collection<Clone>> groupContainClonesCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Clone> findGroupContainCloneRelations(CloneGroup group) {
		Collection<Clone> result = groupContainClonesCache.get(group);
		if(result == null) {
			result = cloneRepository.findCloneGroupContainClones(group.getId());
			groupContainClonesCache.put(group, result);
		}
		return result;
	}

	private Map<String, CloneGroup> nameToGroupCache = new ConcurrentHashMap<>();
	@Override
	public CloneGroup queryCloneGroup(String name) {
		CloneGroup result = nameToGroupCache.get(name);
		if(result == null) {
			result = cloneGroupRepository.queryCloneGroup(name);
			nameToGroupCache.put(name, result);
			cacheService.cacheNodeById(result);
		}
		return result;
	}
    
}
