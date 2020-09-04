package cn.edu.fudan.se.multidependency.service.query.clone;

import java.util.Collection;
import java.util.Map;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneLevel;
import cn.edu.fudan.se.multidependency.model.relation.clone.Clone;
import cn.edu.fudan.se.multidependency.model.relation.clone.CloneRelationType;
import cn.edu.fudan.se.multidependency.repository.node.clone.CloneGroupRepository;
import cn.edu.fudan.se.multidependency.repository.relation.clone.CloneRepository;
import cn.edu.fudan.se.multidependency.service.query.CacheService;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;

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
			switch(cloneType) {
			case FILE_CLONE_FILE:
				result = cloneRepository.findAllFileClones();
				break;
			case FUNCTION_CLONE_FUNCTION:
				result = cloneRepository.findAllFunctionClones();
				break;
			case TYPE_CLONE_TYPE:
				result = cloneRepository.findAllTypeClones();
				break;
			case SNIPPET_CLONE_SNIPPET:
				result = cloneRepository.findAllSnippetClones();
				break;
			case FUNCTION_CLONE_SNIPPET:
			case TYPE_CLONE_FUNCTION:
			case TYPE_CLONE_SNIPPET:
				result = cloneRepository.findAllClonesByCloneType(cloneType.toString());
			default:
				break;
			}
			cloneTypeToClones.put(cloneType, result);
		}
		return result;
	}
	
	private Map<CloneLevel, Collection<CloneGroup>> cloneLevelToGroups = new ConcurrentHashMap<>();
	@Override
	public Collection<CloneGroup> findGroupsContainCloneTypeRelation(CloneLevel level) {
		Collection<CloneGroup> result = cloneLevelToGroups.get(level);
		if(result == null) {
			level = level == null ? CloneLevel.file : level;
			result = cloneGroupRepository.findGroups(level.toString());
			cloneLevelToGroups.put(level, result);
		}
		return result;
	}

	@Deprecated
	private Map<CloneRelationType, Collection<CloneGroup>> cloneTypeToGroups = new ConcurrentHashMap<>();
	@Deprecated
	@Override
	public Collection<CloneGroup> findGroupsContainCloneTypeRelation(CloneRelationType cloneType) {
		Collection<CloneGroup> result = cloneTypeToGroups.get(cloneType);
		if(result == null) {
			switch(cloneType) {
			case FILE_CLONE_FILE:
				result = cloneGroupRepository.findGroups(CloneLevel.file.toString());
				break;
			case FUNCTION_CLONE_FUNCTION:
				result = cloneGroupRepository.findGroups(CloneLevel.function.toString());
				break;
			case TYPE_CLONE_TYPE:
				result = cloneGroupRepository.findGroups(CloneLevel.type.toString());
				break;
			case SNIPPET_CLONE_SNIPPET:
				result = cloneGroupRepository.findGroups(CloneLevel.snippet.toString());
				break;
			case FUNCTION_CLONE_SNIPPET:
			case TYPE_CLONE_FUNCTION:
			case TYPE_CLONE_SNIPPET:
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
				break;
			default:
				break;
			}
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

	private Map<Long, Collection<Clone>> groupContainClonesCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Clone> findGroupContainCloneRelations(CloneGroup group) {
		Collection<Clone> result = groupContainClonesCache.get(group.getId());
		if(result == null) {
			result = cloneRepository.findCloneGroupContainClones(group.getId());
			groupContainClonesCache.put(group.getId(), result);
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

	Map<Project, Collection<ProjectFile>> projectContainCloneFilesCache = new ConcurrentHashMap<>();
	@Override
	public Collection<ProjectFile> findProjectContainCloneFiles(Project project) {
		Collection<ProjectFile> result = projectContainCloneFilesCache.getOrDefault(project, cloneRepository.findProjectContainCloneFiles(project.getId()));
		return result;
	}

}
