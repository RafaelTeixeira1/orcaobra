package br.com.orcaobra.api;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/** Garante as regras da arquitetura hexagonal com testes automatizados (ArchUnit). */
class ArquiteturaTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("br.com.orcaobra");

    @Test
    void nucleoNaoDependeDeFrameworks() {
        noClasses().that().resideInAnyPackage("br.com.orcaobra.dominio..", "br.com.orcaobra.aplicacao..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "com.lowagie..", "br.com.orcaobra.api..")
                .check(classes);
    }

    @Test
    void dominioNaoDependeDaAplicacao() {
        noClasses().that().resideInAPackage("br.com.orcaobra.dominio..")
                .should().dependOnClassesThat().resideInAPackage("br.com.orcaobra.aplicacao..")
                .check(classes);
    }

    @Test
    void adaptadoresWebNaoAcessamPersistenciaDireto() {
        noClasses().that().resideInAPackage("..adaptador.entrada..")
                .should().dependOnClassesThat().resideInAPackage("..adaptador.saida..")
                .check(classes);
    }
}
