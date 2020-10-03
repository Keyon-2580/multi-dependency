package cn.edu.fudan.se.multidependency.service.insert.git;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.edu.fudan.se.multidependency.model.node.Project;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Branch;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitAddressIssue;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitInheritCommit;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperReportIssue;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperSubmitCommit;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.config.GitConfig;

public class EvolutionExtractor extends ExtractorForNodesAndRelationsImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionExtractor.class);

    private GitExtractor gitExtractor;

    private Map<Integer, Issue> issues;

    private static final String[] SUFFIX = new String[]{".java", ".c", ".cpp", ".cc", ".h"};

    private GitConfig gitConfig;
    
    private List<Ref> branches = null;
    
    private GitRepository gitRepository;
    
    private Commit currentVersionCommit = null;
    
    private String currentVersionCommitId;
    
    public EvolutionExtractor(GitConfig gitConfig) {
        this.gitConfig = gitConfig;
        this.gitExtractor = new GitExtractor(gitConfig.getPath());
        this.branches = gitExtractor.getBranches();
        this.currentVersionCommitId = gitConfig.getCurrentVersionCommitId();
    }

    @Override
    public void addNodesAndRelations() throws Exception {
        //添加gitRepository节点和gitRepository到project的包含关系
        gitRepository = new GitRepository(generateEntityId(), gitExtractor.getRepositoryName(), gitExtractor.getGitPath(), gitExtractor.getRepositoryPath());
        addNode(gitRepository, null);
        List<Project> projectList = this.getNodes().findProject(gitConfig.getProject());
        if(!projectList.isEmpty()) {
            projectList.forEach( project -> {
                addRelation(new Contain(gitRepository , project));
            });
        }

        LOGGER.info(gitExtractor.getGitPath() + " " + gitExtractor.getRepositoryPath() + " " + gitExtractor.getRepositoryName() + " " + gitRepository.getPath());
        
        addIssues();
        
        if(!gitConfig.getBranches().isEmpty()) {
        	addSpecificBranches();
        } else {
        	addAllBranches();
        }
        
        close();
    }
    
    private void addSpecificBranches() throws Exception {
        Set<String> branchNames = gitConfig.getBranches();
    	for(Ref branch : branches) {
    		String name = branch.getName();
    		name = name.substring("refs/heads/".length());
    		if(!branchNames.contains(name)) {
    			continue;
    		}
    		gitExtractor.checkout(branch);
    		Branch branchNode = new Branch(generateEntityId(), branch.getObjectId().toString(), branch.getName());
    		addNode(branchNode, null);
    		addRelation(new Contain(gitRepository, branchNode));
    		addCommitsAndRelations(branchNode);
    	}
    }
    
    private void addCommitsAndRelations(Branch branch) throws Exception {
        List<RevCommit> commits = null;
        if (!gitConfig.isSpecifyCommitRange()) {
            commits = gitExtractor.getAllCommits();
        } else {
            if (gitConfig.isSpecifyByCommitId()) {
                commits = gitExtractor.getARangeCommitsById(gitConfig.getCommitIdFrom(), gitConfig.getCommitIdTo(), true);
            } else {
                commits = gitExtractor.getARangeCommitsByTime(gitConfig.getCommitTimeSince(), gitConfig.getCommitTimeUntil(), true);
            }
        }
        int beforeReleaseCommits = 0;
        int afterReleaseCommits = 0;
        LOGGER.info(gitRepository.getName() + ", commit 数量：" + commits.size());
//      Collections.reverse(commits);
        for (RevCommit revCommit : commits) {
//        	System.out.println(revCommit.getName());
        	String authoredDate = new SimpleDateFormat(Constant.TIMESTAMP).format(revCommit.getAuthorIdent().getWhen());
        	boolean merge = revCommit.getParentCount() > 1;
        	Commit commit = null;
        	if(branch != null) {
        		commit = this.getNodes().findCommitByCommitId(revCommit.getName());
        		if(commit != null) {
        			addRelation(new Contain(branch, commit));
        			continue;
        		} 
        		//添加commit节点
        		commit = new Commit(generateEntityId(), revCommit.getName(), revCommit.getShortMessage(),
        				revCommit.getFullMessage(), authoredDate, merge, revCommit.getCommitTime());
        		addNode(commit, null);
        		addRelation(new Contain(branch, commit));
        	} else {
        		commit = new Commit(generateEntityId(), revCommit.getName(), revCommit.getShortMessage(),
        				revCommit.getFullMessage(), authoredDate, merge, revCommit.getCommitTime());
        		addNode(commit, null);
        		//添加branch到commit的包含关系
        		List<Ref> branchesOfCommit = gitExtractor.getBranchesByCommitId(revCommit);
        		for (Ref refBranch : branchesOfCommit) {
        			Branch branchNode = this.getNodes().findBranchByBranchId(refBranch.getObjectId().toString());
        			if (branchNode == null) {
        				throw new Exception(refBranch.getName() + "is non-existent");
        			}
        			addRelation(new Contain(branchNode, commit));
        		}
        	}
        	
        	if(commit.getCommitId().equals(this.currentVersionCommitId)) {
        		this.currentVersionCommit = commit;
        	}
        	
        	if(currentVersionCommit != null && currentVersionCommit.getCommitTime() > commit.getCommitTime()) {
        		commit.setUsingForIssue(false);
        		beforeReleaseCommits++;
        	} else {
        		afterReleaseCommits++;
        	}
            
            //添加developer节点和developer到commit的关系
            Developer developer = this.getNodes().findDeveloperByName(revCommit.getAuthorIdent().getName());
            if (developer == null) {
                developer = new Developer(generateEntityId(), revCommit.getAuthorIdent().getName());
                addNode(developer, null);
            }
            addRelation(new DeveloperSubmitCommit(developer, commit));

            //添加commit到commit的继承关系
            if (revCommit.getParentCount() > 0) {
                RevCommit[] parentRevCommits = revCommit.getParents();
                for (RevCommit parentRevCommit : parentRevCommits) {
                	Commit parentCommit = this.getNodes().findCommitByCommitId(parentRevCommit.getName());
                	if (parentCommit != null) {
                		addRelation(new CommitInheritCommit(commit, parentCommit));
                	}
                	
                	//添加commit到file的更新关系
                	for (DiffEntry diff : gitExtractor.getDiffBetweenCommits(revCommit, parentRevCommit)) {
                		String newPath = "/" + gitExtractor.getRepositoryName() + "/" + diff.getNewPath();
                		String oldPath = "/" + gitExtractor.getRepositoryName() + "/" + diff.getOldPath();
                		String changeType = diff.getChangeType().name();
                		String path = DiffEntry.ChangeType.DELETE.name().equals(changeType) ? oldPath : newPath;
                		if (FileUtil.isFiltered(newPath, SUFFIX)) {
                			continue;
                		}
                		addCommitUpdateFileRelation(path, commit, changeType);
                	}
                }
            } else {
                List<String> filesPath = gitExtractor.getCommitFilesPath(revCommit);
                for (String path : filesPath) {
                    if (FileUtil.isFiltered(path, SUFFIX)) continue;
                    path = "/" + gitExtractor.getRepositoryName() + "/" + path;
                    addCommitUpdateFileRelation(path, commit, DiffEntry.ChangeType.ADD.name());
                }
            }
            
            //添加Commit到Issue的关系
            if (issues != null && commit.isUsingForIssue() && !commit.isMerge()) {
                Collection<Integer> issuesNum = gitExtractor.getRelationBtwCommitAndIssue(revCommit);
                for (Integer issueNum : issuesNum) {
                    if (issues.containsKey(issueNum)) {
                        addRelation(new CommitAddressIssue(commit, issues.get(issueNum)));
                    }
                }
            }
        }
        System.out.println(gitRepository.getName() + " " + beforeReleaseCommits + ", " + afterReleaseCommits);
    }
    
    private CommitUpdateFile addCommitUpdateFileRelation(String filePath, Commit commit, String updateType) {
        ProjectFile file = this.getNodes().findFileByPathRecursion(filePath);
        CommitUpdateFile update = null;
        if (file != null) {
        	update = new CommitUpdateFile(commit, file, updateType);
            addRelation(update);
        }
        return update;
    }

    private void addAllBranches() throws Exception {
        //添加branch节点和gitRepository到branch的包含关系
        List<Ref> branches = gitExtractor.getBranches();
        for (Ref branch : branches) {
            Branch branchNode = new Branch(generateEntityId(), branch.getObjectId().toString(), branch.getName());
            addNode(branchNode, null);
            addRelation(new Contain(gitRepository, branchNode));
        }
    	addCommitsAndRelations(null);
    }
    
    private void addIssues() throws Exception {
    	//添加issue节点和gitRepository到issue的包含关系
    	IssueExtractor issueExtractor = new IssueExtractor(gitConfig.getIssueFilePathes());
    	issues = issueExtractor.extract();
    	Map<Integer, Issue> newIssues = issueExtractor.newIssues();
    	System.out.println("newIssues size: " + newIssues.size());
//    	for (Issue issue : issues.values()) {
    	for (Issue issue : newIssues.values()) {
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
    
    private void close() {
    	if(gitExtractor != null) {
    		gitExtractor.close();
    	}
    }
}
