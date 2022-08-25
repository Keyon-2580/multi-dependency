package cn.edu.fudan.se.multidependency.service.coupling;

import cn.edu.fudan.se.multidependency.MultipleDependencyApp;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.repository.node.PackageRepository;
import cn.edu.fudan.se.multidependency.service.query.coupling.CouplingService;
import cn.edu.fudan.se.multidependency.service.query.coupling.HierarchicalClusteringService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = MultipleDependencyApp.class)
class HierarchicalClusteringServiceTest {
    @Autowired
    private final HierarchicalClusteringService hierarchicalClusteringService;

    @Autowired
    private final PackageRepository packageRepository;

    @Autowired
    public HierarchicalClusteringServiceTest(HierarchicalClusteringService hierarchicalClusteringService, PackageRepository packageRepository) {
        this.hierarchicalClusteringService = hierarchicalClusteringService;
        this.packageRepository = packageRepository;
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void calHierarchicalClustering(){
        List<Package> childPcks = packageRepository.findOneStepPackagesById(51182);
        for(Package pck: childPcks){
            System.out.println(pck.getId());
            hierarchicalClusteringService.calPackageComplexityByCluster(pck.getId());
        }
    }
}