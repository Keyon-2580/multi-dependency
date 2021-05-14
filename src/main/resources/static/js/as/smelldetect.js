let smellDetect = function() {
	let _set = function() {
		let setHubLikeDependencyThreshold = function(projectId, minFileFanIn, minFileFanOut, minPackageFanIn, minPackageFanOut) {
			$.ajax({
				type: "post",
				url: "/as/hublike/fanio/" + projectId
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
			setHubLikeDependencyThreshold(projectId, minFileFanIn, minFileFanOut, minPackageFanIn, minPackageFanOut)
		});

		let setUnstableDependencyThreshold = function(projectId, minFileFanOut, minPackageFanOut, minRatio) {
			$.ajax({
				type: "post",
				url: "/as/unstable/threshold/instability/" + projectId
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
		})

		let setImplicitCrossModuleDependencyThreshold = function(projectId, minFileCoChange, minPackageCoChange) {
			$.ajax({
				type: "post",
				url: "/as/icd/cochange/" + projectId
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
		})
	}
	
	let _get = function() {
		let getHubLikeDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/hublike/fanio/" + projectId,
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
		})
		if (hubLikeDependencyProject.val() != null) {
			getHubLikeDependencyThreshold(hubLikeDependencyProject.val());
		}

		let getUnstableDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/unstable/threshold/instability/" + projectId,
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
		})
		if (unstableDependencyProject.val() != null) {
			getUnstableDependencyThreshold(unstableDependencyProject.val());
		}

		let getImplicitCrossModuleDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/icd/cochange/" + projectId,
				success: function(result) {
					$("#icdMinFileCoChange").val(result[0]);
					$("#icdMinPackageCoChange").val(result[1]);
				}
			});
		};
		let implicitCrossModuleDependencyProject = $("#implicitCrossModuleDependencyProject");
		implicitCrossModuleDependencyProject.change(function() {
			getImplicitCrossModuleDependencyThreshold($(this).val());
		})
		if(implicitCrossModuleDependencyProject.val() != null) {
			getImplicitCrossModuleDependencyThreshold(implicitCrossModuleDependencyProject.val());
		}
	}
	
	return {
		init : function() {
			_set();
			_get();
		}
	}
}
