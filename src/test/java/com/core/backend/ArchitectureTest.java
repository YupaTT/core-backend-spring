package com.core.backend;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.core.backend", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllersNoUsanRepositorios = noClasses()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .dependOnClassesThat()
            .areAssignableTo(Repository.class);

    @ArchTest
    static final ArchRule commonNoDependeDeModulos = noClasses()
            .that()
            .resideInAPackage("..common..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..modules..", "..integration..");

    @ArchTest
    static final ArchRule controllersNoExponenEntidades = noClasses()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .dependOnClassesThat()
            .areAnnotatedWith(jakarta.persistence.Entity.class);
}
