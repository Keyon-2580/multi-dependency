let smellDetect = function() {
	const SMELL_LEVEL = {
		MULTIPLE_LEVEL: "MultipleLevel"
	};
	let _set = function() {
		let setHubLikeDependencyThreshold = function(projectId, minFileFanIn, minFileFanOut, minPackageFanIn, minPackageFanOut) {
			$.ajax({
				type: "post",
				url: "/as/hublikedependency/fanio/" + projectId
					+ "?minFileFanIn=" + minFileFanIn
					+ "&minFileFanOut=" + minFileFanOut
					+ "&minPackageFanIn=" + minPackageFanIn
					+ "&minPackageFanOut=" + minPackageFanOut,
				success: function(result) {
					if (result === true) {
						alert("修改成功");
					}
					else {
						alert("修改失败");
					}
				}
			});
		};
		$("#hubLikeDependencyThresholdSave").click(function() {
			let projectId = $("#hubLikeDependencyProject").val();
			let minFileFanIn = $("#hubLikeMinFileFanIn").val();
			let minFileFanOut = $("#hubLikeMinFileFanOut").val();
			let minPackageFanIn = $("#hubLikeMinPackageFanIn").val();
			let minPackageFanOut = $("#hubLikeMinPackageFanOut").val();
			setHubLikeDependencyThreshold(projectId, minFileFanIn, minFileFanOut, minPackageFanIn, minPackageFanOut);
		});

		let setUnstableDependencyThreshold = function(projectId, minFileFanOut, minPackageFanOut, minRatio) {
			$.ajax({
				type: "post",
				url: "/as/unstabledependency/threshold/instability/" + projectId
					+ "?minFileFanOut=" + minFileFanOut
					+ "&minPackageFanOut=" + minPackageFanOut
					+ "&minRatio=" + minRatio,
				success: function(result) {
					if (result === true) {
						alert("修改成功");
					}
					else {
						alert("修改失败");
					}
				}
			});
		};
		$("#unstableDependencyThresholdSave").click(function() {
			let projectId = $("#unstableDependencyProject").val();
			let minFileFanOut = $("#unstableMinFileFanOut").val();
			let minPackageFanOut = $("#unstableMinPackageFanOut").val();
			let minRatio = $("#unstableMinRatio").val();
			setUnstableDependencyThreshold(projectId, minFileFanOut, minPackageFanOut, minRatio);
		});

		let setImplicitCrossModuleDependencyThreshold = function(projectId, minFileCoChange, minPackageCoChange) {
			$.ajax({
				type: "post",
				url: "/as/implicitcrossmoduledependency/cochange/" + projectId
					+ "?minFileCoChange=" + minFileCoChange
					+ "&minPackageCoChange=" + minPackageCoChange,
				success: function(result) {
					if(result === true) {
						alert("修改成功");
					} else {
						alert("修改失败");
					}
				}
			});
		};
		$("#implicitCrossModuleDependencyThresholdSave").click(function() {
			let projectId = $("#implicitCrossModuleDependencyProject").val();
			let minFileCoChange = $("#icdMinFileCoChange").val();
			let minPackageCoChange = $("#icdMinPackageCoChange").val();
			setImplicitCrossModuleDependencyThreshold(projectId, minFileCoChange, minPackageCoChange);
		});
	};
	
	let _get = function() {
		let getHubLikeDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/hublikedependency/fanio/" + projectId,
				success: function(result) {
					$("#hubLikeMinFileFanIn").val(result[0]);
					$("#hubLikeMinFileFanOut").val(result[1]);
					$("#hubLikeMinPackageFanIn").val(result[2]);
					$("#hubLikeMinPackageFanOut").val(result[3]);
				}
			});
		};
		let hubLikeDependencyProject = $("#hubLikeDependencyProject");
		hubLikeDependencyProject.change(function() {
			getHubLikeDependencyThreshold($(this).val())
		});
		if (hubLikeDependencyProject.val() != null) {
			getHubLikeDependencyThreshold(hubLikeDependencyProject.val());
		}

		let getUnstableDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/unstabledependency/threshold/instability/" + projectId,
				success: function(result) {
					$("#unstableMinFileFanOut").val(result[0]);
					$("#unstableMinPackageFanOut").val(result[1]);
					$("#unstableMinRatio").val(result[2]);
				}
			});
		};
		let unstableDependencyProject = $("#unstableDependencyProject");
		unstableDependencyProject.change(function() {
			getUnstableDependencyThreshold($(this).val());
		});
		if (unstableDependencyProject.val() != null) {
			getUnstableDependencyThreshold(unstableDependencyProject.val());
		}

		let getImplicitCrossModuleDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/implicitcrossmoduledependency/cochange/" + projectId,
				success: function(result) {
					$("#icdMinFileCoChange").val(result[0]);
					$("#icdMinPackageCoChange").val(result[1]);
				}
			});
		};
		let implicitCrossModuleDependencyProject = $("#implicitCrossModuleDependencyProject");
		implicitCrossModuleDependencyProject.change(function() {
			getImplicitCrossModuleDependencyThreshold($(this).val());
		});
		if(implicitCrossModuleDependencyProject.val() != null) {
			getImplicitCrossModuleDependencyThreshold(implicitCrossModuleDependencyProject.val());
		}
	};

	let _smellQuery = function () {
		// Cyclic Dependency
		$("#cyclicDependencyQuery").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/cyclicdependency/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		$("#cyclicDependencyDetect").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/cyclicdependency/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Hub-Like Dependency
		$("#hubLikeDependencyQuery").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/hublikedependency/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		$("#hubLikeDependencyDetect").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/hublikedependency/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unstable Dependency
		$("#unstableDependencyQuery").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/unstabledependency/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		$("#unstableDependencyDetect").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/unstabledependency/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Implicit Cross Module Dependency
		$("#implicitCrossModuleDependencyQuery").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/implicitcrossmoduledependency/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		$("#implicitCrossModuleDependencyDetect").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/implicitcrossmoduledependency/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unutilized Abstraction
		$("#unutilizedAbstractionQuery").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/unutilizedabstraction/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		$("#unutilizedAbstractionDetect").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/unutilizedabstraction/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unused Include
		$("#unusedIncludeQuery").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/unusedinclude/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		$("#unusedIncludeDetect").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/unusedinclude/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Multiple Smell
		$("#multipleSmellQuery").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/multiplesmell/query?projectid=" + projectId);
		});
		$("#multipleSmellDetect").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/multiplesmell/detect?projectid=" + projectId);
		});
	};
	
	return {
		init : function() {
			_set();
			_get();
		},
		smellQuery : function () {
			_smellQuery();
		}
	};
};
