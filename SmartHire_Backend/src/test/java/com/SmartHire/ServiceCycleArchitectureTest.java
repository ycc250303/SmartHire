package com.SmartHire;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(packages = "com.SmartHire")
public class ServiceCycleArchitectureTest {

    @ArchTest
    static void service_layers_of_each_module_should_not_depend_on_other_modules_service_layers(
            JavaClasses classes) {
        SlicesRuleDefinition.slices()
                // 👇 以 smarthire 下的“一级模块”作为 slice
                .matching("com.SmartHire.(*)..service..")
                .should()
                .notDependOnEachOther()
                .because(
                        "各业务模块的 Service 层必须保持隔离，不允许直接依赖其他模块的 Service 层")
                .check(classes);
    }
}