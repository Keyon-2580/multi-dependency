package cn.edu.fudan.se.multidependency.service.nospring.git;

import cn.edu.fudan.se.multidependency.utils.FileUtil;
import com.google.common.collect.Lists;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitExtractor {

    private String gitProjectPath;

    private Repository repository;

    private Git git;

    public GitExtractor(String gitProjectPath){
        this.gitProjectPath = gitProjectPath;
        try {
            repository = FileRepositoryBuilder.create(new File(gitProjectPath, ".git"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        git = new Git(repository);
    }

    public List<Ref> getBranches(){
        try {
            return git.branchList().call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    //应该改为通过API来获取
    public String getRepositoryName() {
        return FileUtil.extractFileName(gitProjectPath);
    }

    public List<RevCommit> getAllCommits() {
        try{
            Iterable<RevCommit> commits = git.log().call();
            return Lists.newArrayList(commits.iterator());
        }catch (GitAPIException e){
            e.printStackTrace();
        }
        return null;
    }

    public List<Ref> getBranchesByCommitId(RevCommit revCommit){
        List<Ref> refs = null;
        try{
            refs = git.branchList().setContains(revCommit.getName()).call();
        }catch (GitAPIException e){
            e.printStackTrace();
        }
        return refs;
    }

    public List<DiffEntry> getDiffBetweenCommits(RevCommit revCommit, RevCommit parentRevCommit){
        AbstractTreeIterator currentTreeParser = prepareTreeParser(revCommit.getName());
        AbstractTreeIterator prevTreeParser = prepareTreeParser(parentRevCommit.getName());
        List<DiffEntry> diffs = null;
        try {
            diffs = git.diff().setNewTree(currentTreeParser).setOldTree(prevTreeParser).call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return diffs;
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

    public void close(){
        git.close();
        repository.close();
    }
}
