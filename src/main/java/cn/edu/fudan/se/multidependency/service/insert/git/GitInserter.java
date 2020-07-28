package cn.edu.fudan.se.multidependency.service.insert.git;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

import cn.edu.fudan.se.multidependency.utils.config.GitConfig;
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
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitAddressIssue;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitInheritCommit;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperReportIssue;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperSubmitCommit;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

public class GitInserter extends ExtractorForNodesAndRelationsImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GitInserter.class);

    private GitExtractor gitExtractor;

    private Map<Integer, Issue> issues;

    private static final String[] SUFFIX = new String[]{".java", ".c", ".cpp", ".cc", ".h"};


    private GitConfig gitConfig;
    
    public GitInserter(GitConfig gitConfig) {
        this.gitConfig = gitConfig;
    }

    @Override
    public void addNodesAndRelations() throws Exception {
        gitExtractor = new GitExtractor(gitConfig.getPath());
        if(gitConfig.isAnalyseIssue()) {
            issues = new IssueExtractor(gitConfig.getIssueFilePath()).extract();
        }
        addBranchesAndIssues();
        addCommitsAndRelations();
        addProjectFileCoChangeRelation();
        gitExtractor.close();
    }

    public void addBranchesAndIssues() {
        //添加gitRepository节点和gitRepository到project的包含关系
        GitRepository gitRepository = new GitRepository(generateEntityId(), 
        		gitExtractor.getRepositoryName(), gitExtractor.getGitPath(), gitExtractor.getRepositoryPath());
        LOGGER.info(gitRepository.getPath());
        addNode(gitRepository, null);

        //添加branch节点和gitRepository到branch的包含关系
        List<Ref> branches = gitExtractor.getBranches();
        for (Ref branch : branches) {
            Branch branchNode = new Branch(generateEntityId(), branch.getObjectId().toString(), branch.getName());
            addNode(branchNode, null);
            addRelation(new Contain(gitRepository, branchNode));
        }

        //添加issue节点和gitRepository到issue的包含关系
        if (gitConfig.isAnalyseIssue()) {
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
        List<RevCommit> commits;
        if (!gitConfig.isSpecifyCommitRange()) {
            commits = gitExtractor.getAllCommits();
        } else {
            if (gitConfig.isSpecifyByCommitId()) {
                commits = gitExtractor.getARangeCommitsById(gitConfig.getCommitIdFrom(), gitConfig.getCommitIdTo());
            } else {
                commits = gitExtractor.getARangeCommitsByTime(gitConfig.getCommitTimeSince(), gitConfig.getCommitTimeUntil());
            }
        }
        LOGGER.info("commit 数量：" + commits.size());
//      Collections.reverse(commits);
        int i = 0;
        for (RevCommit revCommit : commits) {
        	
        	//添加commit节点
        	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.TIMESTAMP);
        	String authoredDate = simpleDateFormat.format(revCommit.getAuthorIdent().getWhen());
        	Commit commit = new Commit(generateEntityId(), revCommit.getName(), revCommit.getShortMessage(),
        	        revCommit.getFullMessage(), authoredDate);
//        	System.out.println(String.join(" ", commit.getCommitId(), String.valueOf(i++)));
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
            List<ProjectFile> files = null;
            if(gitConfig.isCalculateCochange()) {
            	files = new ArrayList<>();
            }
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
                        CommitUpdateFile update = addCommitUpdateFileRelation(path, commit, changeType);
                        if(gitConfig.isCalculateCochange() && update != null) {
                        	files.add(update.getFile());
                        }
                    }
                }
            } else {
                List<String> filesPath = gitExtractor.getCommitFilesPath(revCommit);
                for (String path : filesPath) {
                    if (FileUtil.isFiltered(path, SUFFIX)) continue;
                    path = "/" + gitExtractor.getRepositoryName() + "/" + path;
                    addCommitUpdateFileRelation(path, commit, "ADD");
                    CommitUpdateFile update = addCommitUpdateFileRelation(path, commit, "ADD");
                    if(gitConfig.isCalculateCochange() && update != null) {
                    	files.add(update.getFile());
                    }
                }
            }
            
            addProjectFileCoChangeRelation(files);

            //添加Commit到Issue的关系
            if (gitConfig.isAnalyseIssue()) {
                List<Integer> issuesNum = gitExtractor.getRelationBtwCommitAndIssue(revCommit);
                for (Integer issueNum : issuesNum) {
                    if (issues.containsKey(issueNum)) {
                        addRelation(new CommitAddressIssue(commit, issues.get(issueNum)));
                    }
                }
            }
        }
    }
    
    private Map<String, Map<String, CoChange>> fileCoChanges;
    private void addProjectFileCoChangeRelation() {
    	if(!gitConfig.isCalculateCochange() || fileCoChanges == null) {
    		return ;
    	}
    	LOGGER.info("开始添加文件cochange关系");
    	int count = 0;
    	for(Map<String, CoChange> values : fileCoChanges.values()) {
    		for(CoChange cc : values.values()) {
    			count++;
    			addRelation(cc);
    		}
    	}
    	LOGGER.info("添加文件cochange关系结束，关系数量：" + count);
    }
    
    private void addProjectFileCoChangeRelation(List<ProjectFile> files) {
    	if(!gitConfig.isCalculateCochange() || files == null) {
    		return ;
    	}
    	if(fileCoChanges == null) {
    		fileCoChanges = new HashMap<>();
    	}
        files.sort(Comparator.comparing(ProjectFile::getPath));
    	for(int i = 0; i < files.size(); i++) {
    		ProjectFile file1 = files.get(i);
    		Map<String, CoChange> file1ToCochanges = fileCoChanges.getOrDefault(file1.getPath(), new HashMap<>());
    		for(int j = i + 1; j < files.size(); j++) {
    			ProjectFile file2 = files.get(j);
    			CoChange cochange = file1ToCochanges.get(file2.getPath());
//    			LOGGER.info(file1.getPath() + " " + file2.getPath() + " " + file1ToCochanges.containsKey(file2.getPath()));
    			if(cochange == null) {
    				cochange = new CoChange(file1, file2);
    				cochange.setTimes(0);
    			}
    			cochange.addTimes();
    			file1ToCochanges.put(file2.getPath(), cochange);
    		}
    		fileCoChanges.put(file1.getPath(), file1ToCochanges);
    	}
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
}
