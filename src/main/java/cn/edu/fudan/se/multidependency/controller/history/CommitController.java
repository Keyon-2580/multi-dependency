package cn.edu.fudan.se.multidependency.controller.history;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.service.query.history.CommitQueryService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.IssueQueryService;

import java.util.ArrayList;

@Controller
@RequestMapping("/commit")
public class CommitController {

	@Autowired
	GitAnalyseService gitAnalyseService;

	@Autowired
	private GitAnalyseService gitService;
	
	@Autowired
	private CommitQueryService commitService;
	
	@Autowired
	private IssueQueryService issueService;

	@GetMapping("")
	public String index(HttpServletRequest request) {
		request.setAttribute("commits", commitService.queryAllCommits());
		return "history/commits";
	}

	@GetMapping("/cochange")
	public String indexByCochange(HttpServletRequest request, @RequestParam("cochangeId") long id) {
		CoChange cochange = null;
		if(id != -1) {
			cochange = gitAnalyseService.findCoChangeById(id);
		}
		if(cochange == null) {
			request.setAttribute("commits", new ArrayList<>());
		}
		else {
			request.setAttribute("commits", gitAnalyseService.findCommitsByCoChange(cochange));
		}
		return "history/commits";
	}

	@GetMapping("/{commitGraphId}")
	public String commitIndex(HttpServletRequest request, @PathVariable("commitGraphId") long id) {
		request.setAttribute("commit", commitService.queryCommit(id));
		return "history/commit";
	}
	
	@GetMapping("/{commitGraphId}/files")
	@ResponseBody
	public Object commitFiles(@PathVariable("commitGraphId") long id) {
		Commit commit = commitService.queryCommit(id);
		return gitService.queryCommitUpdateFilesGroupByUpdateType(commit);
	}
	
	@GetMapping("/{commitGraphId}/issues")
	@ResponseBody
	public Object addressIssues(@PathVariable("commitGraphId") long id) {
		Commit commit = commitService.queryCommit(id);
		return issueService.queryIssuesAddressedByCommit(commit);
	}
}
