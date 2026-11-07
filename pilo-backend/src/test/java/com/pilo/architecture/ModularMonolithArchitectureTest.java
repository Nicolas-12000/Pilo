package com.pilo.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.pilo", importOptions = ImportOption.DoNotIncludeTests.class)
class ModularMonolithArchitectureTest {

	/**
	 * {@code integrations} is pure outbound HTTP; it must not depend on domain aggregates
	 * (cases, procedures, workflows, documents, users).
	 */
	@ArchTest
	static final ArchRule integrationsMustNotDependOnCoreDomain =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.resideInAPackage("..integrations..")
					.should()
					.dependOnClassesThat()
					.resideInAnyPackage(
							"..cases..", "..procedures..", "..workflows..", "..documents..", "..users..");

	/** Extraction adapters and config; no direct persistence or HTTP document boundary. */
	@ArchTest
	static final ArchRule aiMustNotDependOnDocumentsOrCases =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.resideInAPackage("..ai..")
					.should()
					.dependOnClassesThat()
					.resideInAnyPackage("..documents..", "..cases..", "..workflows..");

	/** Identity module does not call audit directly — use events (see {@code UserAccessChangeEvent}). */
	@ArchTest
	static final ArchRule usersMustNotDependOnAudit =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.resideInAPackage("..users..")
					.should()
					.dependOnClassesThat()
					.resideInAPackage("..audit..");

	/**
	 * Public REST controllers delegate to named application use cases instead of injecting domain
	 * services directly (keeps HTTP thin and flows grepable).
	 */
	@ArchTest
	static final ArchRule caseControllerDoesNotDependOnCaseService =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.haveSimpleName("CaseController")
					.should()
					.dependOnClassesThat()
					.haveSimpleName("CaseService");

	@ArchTest
	static final ArchRule documentControllerDoesNotDependOnDocumentService =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.haveSimpleName("DocumentController")
					.should()
					.dependOnClassesThat()
					.haveSimpleName("DocumentService");

	@ArchTest
	static final ArchRule documentControllerDoesNotDependOnDocumentContentService =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.haveSimpleName("DocumentController")
					.should()
					.dependOnClassesThat()
					.haveSimpleName("DocumentContentService");

	@ArchTest
	static final ArchRule workflowControllerDoesNotDependOnWorkflowService =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.haveSimpleName("WorkflowController")
					.should()
					.dependOnClassesThat()
					.haveSimpleName("WorkflowService");

	@ArchTest
	static final ArchRule workflowControllerDoesNotDependOnCaseService =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.haveSimpleName("WorkflowController")
					.should()
					.dependOnClassesThat()
					.haveSimpleName("CaseService");

	@ArchTest
	static final ArchRule authControllerDoesNotDependOnAuthService =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.haveSimpleName("AuthController")
					.should()
					.dependOnClassesThat()
					.haveSimpleName("AuthService");

	@ArchTest
	static final ArchRule authControllerDoesNotDependOnUserRepository =
			com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
					.that()
					.haveSimpleName("AuthController")
					.should()
					.dependOnClassesThat()
					.haveSimpleName("UserRepository");
}
