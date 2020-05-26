package cn.edu.fudan.se.multidependency.service.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.FunctionRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.NamespaceRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.PackageRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.ProjectRepository;
import cn.edu.fudan.se.multidependency.repository.node.code.TypeRepository;

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
	public Project queryProject(Long id) {
		Node node = cache.findNodeById(id);
		Project result = node == null ? projectRepository.findById(id).get() : (node instanceof Project ? (Project) node : projectRepository.findById(id).get());
		cache.cacheNodeById(result);
		return result;
	}
}
