package cn.edu.fudan.se.multidependency.service.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.Language;
import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.clone.CloneGroupRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.NamespaceRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

@Service
public class NodeServiceImpl implements NodeService {
    
    @Autowired
    CacheService cache;
    
	@Autowired
	NamespaceRepository namespaceRepository;
	
	@Autowired
    PackageRepository packageRepository;

    @Autowired
    ProjectRepository projectRepository;
    
    @Autowired
    TypeRepository typeRepository;
    
	@Autowired
	FunctionRepository functionRepository;
	
	@Autowired
	ProjectFileRepository fileRepository;
	
	@Autowired
	CloneGroupRepository cloneGroupRepository;

	@Override
	public Package queryPackage(long id) {
		Node node = cache.findNodeById(id);
		Package result = node == null ? packageRepository.findById(id).get() : (node instanceof Package ? (Package) node : packageRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public ProjectFile queryFile(long id) {
		Node node = cache.findNodeById(id);
		ProjectFile result = node == null ? fileRepository.findById(id).get() : (node instanceof ProjectFile ? (ProjectFile) node : fileRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public Namespace queryNamespace(long id) {
		Node node = cache.findNodeById(id);
		Namespace result = node == null ? namespaceRepository.findById(id).get() : (node instanceof Namespace ? (Namespace) node : namespaceRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public Type queryType(long id) {
		Node node = cache.findNodeById(id);
		Type result = node == null ? typeRepository.findById(id).get() : (node instanceof Type ? (Type) node : typeRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public Function queryFunction(long id) {
		Node node = cache.findNodeById(id);
		Function result = node == null ? functionRepository.findById(id).get() : (node instanceof Function ? (Function) node : functionRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public Project queryProject(long id) {
		Node node = cache.findNodeById(id);
		Project result = node == null ? projectRepository.findById(id).get() : (node instanceof Project ? (Project) node : projectRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public ProjectFile queryFile(String path) {
		ProjectFile file = null;
		String newPath = path;
		while(file == null) {
			file = cache.findFileByPath(newPath);
			if(file != null) {
				return file;
			}
			file = fileRepository.findFileByPath(newPath);
			newPath = FileUtil.extractNextPath(newPath);
			if(StringUtils.isBlank(newPath)) {
				break;
			}
		}
		if(file != null) {
			cache.cacheNodeById(file);
			cache.cacheFileByPath(path, file);
			cache.cacheFileByPath(newPath, file);
		}
		return file;
	}

	@Override
	public Project queryProject(String name, Language language) {
		Project project = projectRepository.findProjectByNameAndLanguage(name, language.toString());
		cache.cacheNodeById(project);
		return project;
	}
	
	private Map<Language, Collection<Project>> languageToProjectsCache = new ConcurrentHashMap<>();
	@Override
	public Collection<Project> queryProjects(Language language) {
		if(language == null) {
			return new ArrayList<Project>();
		}
		Collection<Project> result = languageToProjectsCache.get(language);
		if(result == null) {
			List<Project> projects = projectRepository.findProjectsByLanguage(language.toString());
			projects.sort((p1, p2) -> {
				return p1.getName().compareTo(p2.getName());
			});
			for(Project project : projects) {
				cache.cacheNodeById(project);
			}
			result = projects;
			languageToProjectsCache.put(language, result);
		}
		return result;
	}

	@Override
	public CloneGroup queryCloneGroup(long id) {
		Node node = cache.findNodeById(id);
		CloneGroup result = node == null ? cloneGroupRepository.findById(id).get() : (node instanceof CloneGroup ? (CloneGroup) node : cloneGroupRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}

	@Override
	public Map<Long, Package> queryAllPackages() {
		Map<Long, Package> result = new HashMap<>();
		Iterable<Package> all = packageRepository.findAll();
		for(Package pck : all) {
			result.put(pck.getId(), pck);
		}
		return result;
	}

	@Override
	public List<ProjectFile> queryAllFiles() {
		List<ProjectFile> result = new ArrayList<>();
		Iterable<ProjectFile> files = fileRepository.findAll();
		for(ProjectFile file : files) {
			result.add(file);
		}
		return result;
	}

}
