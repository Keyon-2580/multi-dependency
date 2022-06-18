package cn.edu.fudan.se.multidependency.service.query.coupling;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface CouplingService {
    void calCouplingValue() throws IOException;
}
