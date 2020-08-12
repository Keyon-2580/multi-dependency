package cn.edu.fudan.se.multidependency.service.insert.git;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.collect.Lists;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.utils.FileUtil;

public class GitExtractor implements Closeable {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(GitExtractor.class);

    private String gitProjectPath;

    private Repository repository;

    private Git git;
    
    public Ref checkout(Ref branch) {
    	try {
			git.checkout().setName(branch.getName()).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
    	return branch;
    }

    public GitExtractor(String gitProjectPath) {
        this.gitProjectPath = gitProjectPath;
        try {
            repository = FileRepositoryBuilder.create(new File(gitProjectPath, ".git"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        git = new Git(repository);
    }

    public List<Ref> getBranches() {
        try {
            return git.branchList().call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public String getRepositoryName() {
        return FileUtil.extractFileName(gitProjectPath);
    }
    
    public String getGitPath() {
    	return repository.getDirectory().getAbsolutePath();
    }
    
    public String getRepositoryPath() {
    	return gitProjectPath;
    }

    public List<RevCommit> getAllCommits() {
        try{
            Iterable<RevCommit> commits = git.log().setRevFilter(RevFilter.NO_MERGES).call();
//            Iterable<RevCommit> commits = git.log().call();
            return Lists.newArrayList(commits.iterator());
        }catch (GitAPIException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<RevCommit> getARangeCommitsById(String from, String to) {
        try {
            Iterable<RevCommit> commits = git.log().setRevFilter(RevFilter.NO_MERGES).addRange(repository.resolve(from),repository.resolve(to)).call();
//            Iterable<RevCommit> commits = git.log().addRange(repository.resolve(from),repository.resolve(to)).call();
            return Lists.newArrayList(commits.iterator());
        }catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<RevCommit> getARangeCommitsByTime(String since, String until) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.TIMESTAMP);
        try {
            Date sinceDate = simpleDateFormat.parse(since);
            Date untilDate = simpleDateFormat.parse(until);
            RevFilter between = CommitTimeRevFilter.between(sinceDate, untilDate);
            Iterable<RevCommit> commits = git.log().setRevFilter(AndRevFilter.create(between, RevFilter.NO_MERGES)).call();
//            Iterable<RevCommit> commits = git.log().setRevFilter(between).call();
            return Lists.newArrayList(commits.iterator());
        } catch (ParseException | GitAPIException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Ref> getBranchesByCommitId(RevCommit revCommit) {
        try{
            return git.branchList().setContains(revCommit.getName()).call();
        }catch (GitAPIException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<DiffEntry> getDiffBetweenCommits(RevCommit revCommit, RevCommit parentRevCommit) {
        AbstractTreeIterator currentTreeParser = prepareTreeParser(revCommit.getName());
        AbstractTreeIterator prevTreeParser = prepareTreeParser(parentRevCommit.getName());
        try {
        	return git.diff().setNewTree(currentTreeParser).setOldTree(prevTreeParser).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private CanonicalTreeParser prepareTreeParser(String objectId) {
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(ObjectId.fromString(objectId));
            RevTree tree = revWalk.parseTree(commit.getTree().getId());
            ObjectReader oldReader = repository.newObjectReader();
            treeParser.reset(oldReader, tree.getId());
            revWalk.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return treeParser;
    }

    public List<String> getCommitFilesPath(RevCommit commit) {
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
            List<String> result = new ArrayList<>();
            while (treeWalk.next()) {
                String path = treeWalk.getPathString();
                result.add(path);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Integer> getRelationBtwCommitAndIssue(RevCommit commit) {
        String issueNumRegex = "#[1-9][0-9]*";
        List<Integer> issueNumFromShort = getMatcher(issueNumRegex, commit.getShortMessage());
        List<Integer> issueNumFromFull = getMatcher(issueNumRegex, commit.getFullMessage());
        issueNumFromShort.addAll(issueNumFromFull);
        return issueNumFromShort;
    }

    public List<Integer> getMatcher(String regex, String source) {
        List<Integer> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            result.add(Integer.parseInt(matcher.group().substring(1)));
        }
        return result;
    }

    public void close() {
    	if(git != null) {
    		git.close();
    	}
    	if(repository != null) {
    		repository.close();
    	}
    }
}
