package cn.edu.fudan.se.multidependency.service.nospring.git;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.*;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.git.*;
import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GitInserter extends ExtractorForNodesAndRelationsImpl {

    private GitExtractor gitExtractor;

    private boolean isAnalyseIssue;

    private Map<Integer, Issue> issues;

    private static final String[] SUFFIX = new String[]{".java", ".c", ".cpp"};

    private boolean selectCommitRange;

    private String commitIdFrom;

    private String commitIdTo;

    public GitInserter(String gitProjectPath, String issueFilePath,
                       String commitIdFrom, String commitIdTo) throws Exception {
        gitExtractor = new GitExtractor(gitProjectPath);
        if (!issueFilePath.equals("")) {
            this.isAnalyseIssue = true;
            issues = new IssueExtractor(issueFilePath).extract();
        }
        if (!commitIdFrom.equals("") && !commitIdTo.equals("")) {
            this.selectCommitRange = true;
            this.commitIdFrom = commitIdFrom;
            this.commitIdTo = commitIdTo;
        }
    }

    @Override
    public void addNodesAndRelations() throws Exception {
        addBranchesAndIssues();
        addCommitsAndRelations();
        gitExtractor.close();
    }

    public void addBranchesAndIssues() {
        //添加gitRepository节点和gitRepository到project的包含关系
        GitRepository gitRepository = new GitRepository(generateEntityId(), gitExtractor.getRepositoryName());
        addNode(gitRepository, null);
//        for (Project project : projects) {
//            addRelation(new Contain(gitRepository, project));
//        }

        //添加branch节点和gitRepository到branch的包含关系
        List<Ref> branches = gitExtractor.getBranches();
        for (Ref branch : branches) {
            Branch branchNode = new Branch(generateEntityId(), branch.getObjectId().toString(), branch.getName());
            addNode(branchNode, null);
            addRelation(new Contain(gitRepository, branchNode));
        }

        //添加issue节点和gitRepository到issue的包含关系
        if (isAnalyseIssue) {
            for (Issue issue : issues.values()) {
                issue.setEntityId(generateEntityId());
                addNode(issue, null);
                addRelation(new Contain(gitRepository, issue));

                //添加developer节点和developer到issue的关系
                Developer developer = this.getNodes().findDeveloperByName(issue.getDeveloperName());
                if (developer == null) {
                    developer = new Developer(generateEntityId(), issue.getDeveloperName());
                    addNode(developer, null);
                }
                addRelation(new DeveloperReportIssue(developer, issue));
            }
        }
    }

    public void addCommitsAndRelations() throws Exception {
        List<RevCommit> commits = selectCommitRange ? gitExtractor.getARangeCommits(commitIdFrom, commitIdTo) : gitExtractor.getAllCommits();
//        Collections.reverse(commits);
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
                Branch branchNode = this.getNodes().findBranchByBranchId(branch.getObjectId().toString());
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
                        String newPath = "/" + gitExtractor.getRepositoryName() + "/" + diff.getNewPath();
                        String oldPath = "/" + gitExtractor.getRepositoryName() + "/" + diff.getOldPath();
                        String changeType = diff.getChangeType().name();
                        String path = DiffEntry.ChangeType.DELETE.name().equals(changeType) ? oldPath : newPath;
                        if (FileUtil.isFiltered(newPath, SUFFIX)) continue;
                        addCommitUpdateFileRelation(path, commit, changeType);
                    }
                }
            } else {
                List<String> filesPath = gitExtractor.getCommitFilesPath(revCommit);
                for (String path : filesPath) {
                    if (FileUtil.isFiltered(path, SUFFIX)) continue;
                    path = "/" + gitExtractor.getRepositoryName() + "/" + path;
                    addCommitUpdateFileRelation(path, commit, "ADD");
                }
            }

            //添加Commit到Issue的关系
            if (isAnalyseIssue) {
                List<Integer> issuesNum = gitExtractor.getRelationBtwCommitAndIssue(revCommit);
                for (Integer issueNum : issuesNum) {
                    if (issues.containsKey(issueNum)) {
                        addRelation(new CommitAddressIssue(commit, issues.get(issueNum)));
                    }
                }
            }
        }
    }

    public void addCommitUpdateFileRelation(String filePath, Commit commit, String updateType) {
        ProjectFile file = this.getNodes().findFileByPathRecursion(filePath);
        if (file != null) {
            addRelation(new CommitUpdateFile(commit, file, updateType));
        }
    }
}
