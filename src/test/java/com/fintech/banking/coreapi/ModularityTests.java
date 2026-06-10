package com.fintech.banking.coreapi;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Verifies the Spring Modulith module structure and generates documentation.
 *
 * <p>
 * {@link ApplicationModules#verify()} enforces that no module accesses another
 * module's internal sub-packages. With {@code account}, {@code user}, and
 * {@code transaction} declared as closed modules (no {@code Type.OPEN}), only
 * the packages listed in each module's {@code exposedPackages} are accessible
 * cross-module. {@code common} remains open as a shared kernel.
 *
 * <p>
 * Run {@code mvn test -Dtest=ModularityTests#createModuleDocumentation} to
 * regenerate PlantUML diagrams under {@code target/modulith-docs/}.
 */
class ModularityTests {

	ApplicationModules modules = ApplicationModules.of(CoreApiApplication.class);

	@Test
	void verifiesModularStructure() {
		modules.verify();
	}

	@Test
	void createModuleDocumentation() {
		new Documenter(modules).writeDocumentation().writeIndividualModulesAsPlantUml();
	}
}
