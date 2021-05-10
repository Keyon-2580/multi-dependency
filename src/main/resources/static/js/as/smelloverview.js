let smellOverview = function() {
	let _smellOverview = function(projects, projectTotalMap, fileSmellOverviewMap, packageSmellOverviewMap) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<div>";
				html += "<div>";
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";
				html += "</div>";

				let projectTotalObject = projectTotalMap[project.id];
				let fileCount = projectTotalObject["FileCount"];
				let issueCommits = projectTotalObject["IssueCommits"];
				let commits = projectTotalObject["Commits"];
				let issueChangeLines = projectTotalObject["IssueChangeLines"];
				let changeLines = projectTotalObject["ChangeLines"];

				let projectFileSmellOverviewObject = fileSmellOverviewMap[project.id];
				let projectFileSmellArray = projectFileSmellOverviewObject["ProjectFileSmell"];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>File Smell Type</th>";
				html += "<th style='text-align: center; vertical-align: middle'>File Smell Count</th>";
				html += "<th style='text-align: center; vertical-align: middle'>File Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Change Lines(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Change Lines(%)</th>";
				html += "</tr>";
				for(let projectFileSmellIndex in projectFileSmellArray) {
					if (projectFileSmellArray.hasOwnProperty(projectFileSmellIndex)) {
						let projectFileSmellObject = projectFileSmellArray[projectFileSmellIndex];
						let projectFileSmellFileCountPercent = " (null)";
						let projectFileSmellIssueCommitsPercent = " (null)";
						let projectFileSmellCommitsPercent = " (null)";
						let projectFileSmellIssueChangeLinesPercent = " (null)";
						let projectFileSmellChangeLinesPercent = " (null)";
						if (fileCount !== 0) {
							projectFileSmellFileCountPercent = " (" + (projectFileSmellObject["FileCount"] / fileCount).toFixed(2) + ")";
						}
						if (issueCommits !== 0) {
							projectFileSmellIssueCommitsPercent = " (" + (projectFileSmellObject["IssueCommits"] / issueCommits).toFixed(2) + ")";
						}
						if (commits !== 0) {
							projectFileSmellCommitsPercent = " (" + (projectFileSmellObject["Commits"] / commits).toFixed(2) + ")";
						}
						if (issueChangeLines !== 0) {
							projectFileSmellIssueChangeLinesPercent = " (" + (projectFileSmellObject["IssueChangeLines"] / issueChangeLines).toFixed(2) + ")";
						}
						if (changeLines !== 0) {
							projectFileSmellChangeLinesPercent = " (" + (projectFileSmellObject["ChangeLines"] / changeLines).toFixed(2) + ")";
						}
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["SmellType"] + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["SmellCount"] + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["FileCount"] + projectFileSmellFileCountPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["IssueCommits"] + projectFileSmellIssueCommitsPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["Commits"] + projectFileSmellCommitsPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["IssueChangeLines"] + projectFileSmellIssueChangeLinesPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["ChangeLines"] + projectFileSmellChangeLinesPercent + "</td>";
						html += "</tr>";
					}
				}
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>Project Total</td>";
				html += "<td style='text-align: center; vertical-align: middle'></td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + fileCount + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + issueCommits + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + commits + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + issueChangeLines + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + changeLines + "</td>";
				html += "</tr>";
				html += "</table>";
				html += "</div>";

				let projectPackageSmellOverviewObject = packageSmellOverviewMap[project.id];
				let projectPackageSmellArray = projectPackageSmellOverviewObject["ProjectPackageSmell"];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Package Smell Type</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Package Smell Count</th>";
				html += "<th style='text-align: center; vertical-align: middle'>File Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Change Lines(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Change Lines(%)</th>";
				html += "</tr>";
				for(let projectPackageSmellIndex in projectPackageSmellArray) {
					if (projectPackageSmellArray.hasOwnProperty(projectPackageSmellIndex)) {
						let projectPackageSmellObject = projectPackageSmellArray[projectPackageSmellIndex];
						let projectPackageSmellFileCountPercent = " (null)";
						let projectPackageSmellIssueCommitsPercent = " (null)";
						let projectPackageSmellCommitsPercent = " (null)";
						let projectPackageSmellIssueChangeLinesPercent = " (null)";
						let projectPackageSmellChangeLinesPercent = " (null)";
						if (fileCount !== 0) {
							projectPackageSmellFileCountPercent = " (" + (projectPackageSmellObject["FileCount"] / fileCount).toFixed(2) + ")";
						}
						if (issueCommits !== 0) {
							projectPackageSmellIssueCommitsPercent = " (" + (projectPackageSmellObject["IssueCommits"] / issueCommits).toFixed(2) + ")";
						}
						if (commits !== 0) {
							projectPackageSmellCommitsPercent = " (" + (projectPackageSmellObject["Commits"] / commits).toFixed(2) + ")";
						}
						if (issueChangeLines !== 0) {
							projectPackageSmellIssueChangeLinesPercent = " (" + (projectPackageSmellObject["IssueChangeLines"] / issueChangeLines).toFixed(2) + ")";
						}
						if (changeLines !== 0) {
							projectPackageSmellChangeLinesPercent = " (" + (projectPackageSmellObject["ChangeLines"] / changeLines).toFixed(2) + ")";
						}
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["SmellType"] + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["SmellCount"] + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["FileCount"] + projectPackageSmellFileCountPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["IssueCommits"] + projectPackageSmellIssueCommitsPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["Commits"] + projectPackageSmellCommitsPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["IssueChangeLines"] + projectPackageSmellIssueChangeLinesPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["ChangeLines"] + projectPackageSmellChangeLinesPercent + "</td>";
						html += "</tr>";
					}
				}
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>Project Total</td>";
				html += "<td style='text-align: center; vertical-align: middle'></td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + fileCount + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + issueCommits + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + commits + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + issueChangeLines + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + changeLines + "</td>";
				html += "</tr>";
				html += "</table>";
				html += "</div>";
				html += "</div>";
			}
		}
		$("#content").html(html);
	}

	return {
		smellOverview: function(projects, projectTotalMap, fileSmellOverviewMap, packageSmellOverviewMap) {
			_smellOverview(projects, projectTotalMap, fileSmellOverviewMap, packageSmellOverviewMap);
		}
	}
}
