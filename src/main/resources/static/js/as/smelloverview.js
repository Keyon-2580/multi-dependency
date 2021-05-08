let smellOverview = function() {
	let _smellOverview = function(projects, smellOverviewMap) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<div>";
				html += "<div>";
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";
				html += "</div>";
				let projectSmellOverviewObject = smellOverviewMap[project.id];
				let projectSmellArray = projectSmellOverviewObject["ProjectSmell"];
				let projectTotalObject = projectSmellOverviewObject["ProjectTotal"];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Smell Type</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Smell Count</th>";
				html += "<th style='text-align: center; vertical-align: middle'>File Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>All Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Change Lines(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>All Change Lines(%)</th>";
				html += "</tr>";
				for(let projectSmellIndex in projectSmellArray) {
					if (projectSmellArray.hasOwnProperty(projectSmellIndex)) {
						let projectSmellObject = projectSmellArray[projectSmellIndex];
						let projectFileCountPercent = " (null)";
						let projectIssueCommitCountPercent = " (null)";
						let projectAllCommitCountPercent = " (null)";
						let projectIssueChangeLinesPercent = " (null)";
						let projectAllChangeLinesPercent = " (null)";
						if (projectTotalObject["FileCount"] !== 0) {
							projectFileCountPercent = " (" + (projectSmellObject["FileCount"] / projectTotalObject["FileCount"]).toFixed(2) + ")";
						}
						if (projectTotalObject["IssueCommitCount"] !== 0) {
							projectIssueCommitCountPercent = " (" + (projectSmellObject["IssueCommitCount"] / projectTotalObject["IssueCommitCount"]).toFixed(2) + ")";
						}
						if (projectTotalObject["AllCommitCount"] !== 0) {
							projectAllCommitCountPercent = " (" + (projectSmellObject["AllCommitCount"] / projectTotalObject["AllCommitCount"]).toFixed(2) + ")";
						}
						if (projectTotalObject["IssueChangeLines"] !== 0) {
							projectIssueChangeLinesPercent = " (" + (projectSmellObject["IssueChangeLines"] / projectTotalObject["IssueChangeLines"]).toFixed(2) + ")";
						}
						if (projectTotalObject["AllChangeLines"] !== 0) {
							projectAllChangeLinesPercent = " (" + (projectSmellObject["AllChangeLines"] / projectTotalObject["AllChangeLines"]).toFixed(2) + ")";
						}
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectSmellObject["SmellType"] + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectSmellObject["SmellCount"] + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectSmellObject["FileCount"] + projectFileCountPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectSmellObject["IssueCommitCount"] + projectIssueCommitCountPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectSmellObject["AllCommitCount"] + projectAllCommitCountPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectSmellObject["IssueChangeLines"] + projectIssueChangeLinesPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectSmellObject["AllChangeLines"] + projectAllChangeLinesPercent + "</td>";
						html += "</tr>";
					}
				}
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>Project Total</td>";
				html += "<td style='text-align: center; vertical-align: middle'></td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + projectTotalObject["FileCount"] + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + projectTotalObject["IssueCommitCount"] + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + projectTotalObject["AllCommitCount"] + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + projectTotalObject["IssueChangeLines"] + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + projectTotalObject["AllChangeLines"] + "</td>";
				html += "</tr>";
				html += "</table>";
				html += "</div>";
				html += "</div>";
			}
		}
		$("#content").html(html);
	}

	return {
		smellOverview: function(projects, smellOverviewMap) {
			_smellOverview(projects, smellOverviewMap);
		}
	}
}
