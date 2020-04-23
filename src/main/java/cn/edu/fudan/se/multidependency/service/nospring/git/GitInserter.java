package cn.edu.fudan.se.multidependency.service.nospring.git;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.*;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.git.*;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GitInserter extends ExtractorForNodesAndRelationsImpl {

    private GitExtractor gitExtractor;
    private IssueExtractor issueExtractor;

    public GitInserter(String gitProjectPath, String issueFilePath){
        gitExtractor = new GitExtractor(gitProjectPath);
        issueExtractor = new IssueExtractor(issueFilePath);
    }

    @Override
    public void addNodesAndRelations() throws Exception {
        Map<Integer, Issue> issues = issueExtractor.extract();
        addBranchesAndIssues(issues);
        addCommitsAndRelations(issues);
        gitExtractor.close();
    }

    public void addBranchesAndIssues(Map<Integer, Issue> issues) {
        //添加gitRepository节点和gitRepository到project的包含关系
        GitRepository gitRepository = new GitRepository(generateEntityId(),gitExtractor.getRepositoryName());
        addNode(gitRepository,null);
        for(Project project : this.getNodes().findAllProjects()){
            addRelation(new Contain(gitRepository,project));
        }

        //添加branch节点和gitRepository到branch的包含关系
        List<Ref> branches = gitExtractor.getBranches();
        for(Ref branch : branches){
            Branch branchNode = new Branch(generateEntityId(),branch.getName());
            addNode(branchNode,null);
            addRelation(new Contain(gitRepository,branchNode));
        }

        //添加issue节点和gitRepository到issue的包含关系
        for(Issue issue : issues.values()){
            issue.setEntityId(generateEntityId());
            addNode(issue, null);
            addRelation(new Contain(gitRepository,issue));

            //添加developer节点和developer到issue的关系
            Developer developer = this.getNodes().findDeveloperByName(issue.getDeveloperName());
            if (developer == null) {
                developer = new Developer(generateEntityId(), issue.getDeveloperName());
                addNode(developer, null);
            }
            addRelation(new DeveloperReportIssue(developer, issue));
        }
    }

    public void addCommitsAndRelations(Map<Integer, Issue> issues) throws Exception {
        List<RevCommit> commits = gitExtractor.getAllCommits();
        Collections.reverse(commits);
        for (RevCommit revCommit : commits) {
            //添加commit节点
            Commit commit = new Commit(generateEntityId(), revCommit.getName(), revCommit.getShortMessage(),
                    revCommit.getFullMessage(), revCommit.getAuthorIdent().getWhen().toString());
            addNode(commit, null);

            //添加developer节点和developer到commit的关系
            Developer developer = this.getNodes().findDeveloperByName(revCommit.getAuthorIdent().getName());
            if (developer == null) {
                developer = new Developer(generateEntityId(), revCommit.getAuthorIdent().getName());
                addNode(developer, null);
            }
            addRelation(new DeveloperSubmitCommit(developer, commit));

            //添加branch到commit的包含关系
            List<Ref> branchesOfCommit = gitExtractor.getBranchesByCommitId(revCommit);
            for (Ref branch : branchesOfCommit) {
                Branch branchNode = this.getNodes().findBranchByName(branch.getName());
                if (branchNode == null) {
                    throw new Exception(branch.getName() + "is non-existent");
                }
                addRelation(new Contain(branchNode, commit));
            }

            //添加commit到commit的继承关系
            if (revCommit.getParentCount() > 0) {
                RevCommit[] parentRevCommits = revCommit.getParents();
                for (RevCommit parentRevCommit : parentRevCommits) {
                    String parentCommitId = parentRevCommit.getName();
                    Commit parentCommit = this.getNodes().findCommitByCommitId(parentCommitId);
                    if (parentCommit != null) {
                        addRelation(new CommitInheritCommit(commit, parentCommit));
                    }

                    //添加commit到file的更新关系
                    List<DiffEntry> diffs = gitExtractor.getDiffBetweenCommits(revCommit, parentRevCommit);
                    if (diffs == null) continue;
                    for (DiffEntry diff : diffs) {
                        String newPath = diff.getNewPath();
                        String oldPath = diff.getOldPath();
                        String changeType = diff.getChangeType().name();
                        String path = DiffEntry.ChangeType.DELETE.name().equals(changeType) ? oldPath : newPath;
                        ProjectFile file = this.getNodes().findFileByPath(path);
                        if (file == null) {
                            //与静态插入的file节点，命名有差异
                            file = new ProjectFile(generateEntityId(), FileUtil.extractFileName(path), path, FileUtil.extractSuffix(path));
                            addNode(file, null);
                        }
                        CommitUpdateFile.UpdateType updateType;
                        if (DiffEntry.ChangeType.ADD.name().equals(changeType))
                            updateType = CommitUpdateFile.UpdateType.ADD;
                        else if (DiffEntry.ChangeType.MODIFY.name().equals(changeType))
                            updateType = CommitUpdateFile.UpdateType.MODIFY;
                        else updateType = CommitUpdateFile.UpdateType.DELETE;
                        addRelation(new CommitUpdateFile(commit, file, updateType));
                    }
                }
            } else {
                List<String> filesPath = gitExtractor.getCommitFilesPath(revCommit);
                for (String path : filesPath) {
                    ProjectFile file = this.getNodes().findFileByPath(path);
                    if (file == null) {
                        file = new ProjectFile(generateEntityId(), FileUtil.extractFileName(path), path, FileUtil.extractSuffix(path));
                        addNode(file, null);
                    }
                    CommitUpdateFile.UpdateType updateType = CommitUpdateFile.UpdateType.ADD;
                    addRelation(new CommitUpdateFile(commit, file, updateType));
                }
            }
            //添加Commit到Issue的关系
            List<Integer> issuesNum = gitExtractor.getRelationBtwCommitAndIssue(revCommit);
            for(Integer issueNum : issuesNum){
                if(issues.containsKey(issueNum)) {
                    addRelation(new CommitAddressIssue(commit, issues.get(issueNum)));
                }
            }
        }
    }
}
