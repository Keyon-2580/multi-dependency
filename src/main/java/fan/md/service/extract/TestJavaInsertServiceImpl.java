package fan.md.service.extract;

import depends.entity.FileEntity;
import depends.entity.PackageEntity;
import depends.entity.repo.EntityRepo;
import fan.md.exception.LanguageErrorException;
import fan.md.model.Language;
import fan.md.model.node.code.CodeFile;
import fan.md.model.node.code.Package;

public class TestJavaInsertServiceImpl extends InsertServiceImpl {

	public TestJavaInsertServiceImpl(String projectPath, EntityRepo entityRepo, String databasePath, boolean delete,
			Language language) {
		super(projectPath, entityRepo, databasePath, delete, language);
	}

	@Override
	protected void insertNodesWithContainRelations() throws LanguageErrorException {
		entityRepo.getEntities().forEach(entity -> {
			if(entity instanceof PackageEntity) {
				Package pck = new Package();
				pck.setPackageName(entity.getQualifiedName());
				pck.setEntityId(entity.getId());
				pck.setDirectory(false);
				batchInserterService.insertNode(pck);
			} else if(entity instanceof FileEntity) {
				CodeFile file = new CodeFile();
				
				batchInserterService.insertNode(file);
			}
		});
	}

	@Override
	protected void insertRelations() throws LanguageErrorException {
		
	}

}
