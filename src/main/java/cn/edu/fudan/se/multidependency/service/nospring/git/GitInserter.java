package cn.edu.fudan.se.multidependency.service.nospring.git;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Branch;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitInheritCommit;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperSubmitCommit;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Collections;
import java.util.List;

public class GitInserter extends ExtractorForNodesAndRelationsImpl {

    private GitExtractor gitExtractor;

    public GitInserter(String gitProjectPath){
        gitExtractor = new GitExtractor(gitProjectPath);
    }


    @Override
    public void addNodesAndRelations() throws Exception {
        //添加gitRepository节点和gitRepository到project的包含关系
        GitRepository gitRepository = new GitRepository();
        gitRepository.setEntityId(generateEntityId());
        gitRepository.setName(gitExtractor.getRepositoryName());
        addNode(gitRepository,null);
        for(Project project : this.getNodes().findAllProjects()){
            addRelation(new Contain(gitRepository,project));
        }

        //添加branch节点和gitRepository到branches的包含关系
        List<Ref> branches = gitExtractor.getBranches();
        for(Ref branch : branches){
            Branch branchNode = new Branch();
            branchNode.setEntityId(generateEntityId());
            branchNode.setName(branch.getName());
            addNode(branchNode,null);
            addRelation(new Contain(gitRepository,branchNode));
        }

        List<RevCommit> commits = gitExtractor.getAllCommits();
        Collections.reverse(commits);
        for(RevCommit revCommit : commits){
            //添加commit节点
            Commit commit = new Commit();
            commit.setEntityId(generateEntityId());
            commit.setCommitId(revCommit.getName());
            commit.setShortMessage(revCommit.getShortMessage());
            commit.setFullMessage(revCommit.getFullMessage());
            commit.setAuthorName(revCommit.getAuthorIdent().getName());
            commit.setAuthoredDate(revCommit.getAuthorIdent().getWhen().toString());
            addNode(commit,null);

            //添加developer节点和developer到commit的关系
            Developer developer = this.getNodes().findDeveloperByName(revCommit.getAuthorIdent().getName());
            if(developer == null){
                developer = new Developer();
                developer.setEntityId(generateEntityId());
                developer.setName(revCommit.getAuthorIdent().getName());
                addNode(developer,null);
            }
            addRelation(new DeveloperSubmitCommit(developer,commit));

            //添加branch到commit的包含关系
            List<Ref> branchesOfCommit = gitExtractor.getBranchesByCommitId(revCommit);
            for(Ref branch : branchesOfCommit){
                Branch branchNode = this.getNodes().findBranchByName(branch.getName());
                if(branchNode == null) {
                    throw new Exception(branch.getName() + "is non-existent");
                }
                addRelation(new Contain(branchNode,commit));
            }

            //添加commit到commit的继承关系
            if(revCommit.getParentCount() > 0){
                RevCommit[] parentRevCommits = revCommit.getParents();
                for (RevCommit parentRevCommit : parentRevCommits) {
                    String parentCommitId = parentRevCommit.getName();
                    Commit parentCommit = this.getNodes().findCommitByCommitId(parentCommitId);
                    if(parentCommit == null) {
                        throw new Exception(parentCommitId + "is non-existent");
                    }
                    addRelation(new CommitInheritCommit(commit,parentCommit));

                    //添加commit到file的更新关系
                    List<DiffEntry> diffs = gitExtractor.getDiffBetweenCommits(revCommit,parentRevCommit);
                    if(diffs != null){
                        for(DiffEntry diff : diffs){
                            String newPath = diff.getNewPath();
                            String oldPath = diff.getOldPath();
                            String changeType = diff.getChangeType().name();
                            String path = DiffEntry.ChangeType.DELETE.name().equals(changeType) ? oldPath : newPath;
                            ProjectFile file = this.getNodes().findFileByPath(path);
                            if(file == null){
                                //与静态插入的file节点，命名有差异
                                file = new ProjectFile();
                                file.setEntityId(generateEntityId());
                                file.setName(FileUtil.extractFileName(path));
                                file.setPath(path);
                                file.setSuffix(FileUtil.extractSuffix(path));
                                addNode(file,null);
                            }
                            CommitUpdateFile.UpdateType updateType;
                            if(DiffEntry.ChangeType.ADD.name().equals(changeType)) updateType = CommitUpdateFile.UpdateType.ADD;
                            else if(DiffEntry.ChangeType.MODIFY.name().equals(changeType)) updateType = CommitUpdateFile.UpdateType.MODIFY;
                            else updateType = CommitUpdateFile.UpdateType.DELETE;
                            addRelation(new CommitUpdateFile(commit,file,updateType));
                        }
                    }
                }
            }else{
                List<String> filesPath = gitExtractor.getCommitFilesPath(revCommit);
                for(String path : filesPath){
                    ProjectFile file = this.getNodes().findFileByPath(path);
                    if(file == null){
                        //与静态插入的file节点，命名有差异
                        file = new ProjectFile();
                        file.setEntityId(generateEntityId());
                        file.setName(FileUtil.extractFileName(path));
                        file.setPath(path);
                        file.setSuffix(FileUtil.extractSuffix(path));
                        addNode(file,null);
                    }
                    CommitUpdateFile.UpdateType updateType = CommitUpdateFile.UpdateType.ADD;
                    addRelation(new CommitUpdateFile(commit,file,updateType));
                }
            }
        }
    }
}
