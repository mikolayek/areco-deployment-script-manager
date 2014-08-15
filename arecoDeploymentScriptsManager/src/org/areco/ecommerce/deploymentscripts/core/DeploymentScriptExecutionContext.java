/**
 * Copyright 2014 Antonio Robirosa

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.areco.ecommerce.deploymentscripts.core;

import de.hybris.platform.core.Tenant;

import java.util.Set;

import org.areco.ecommerce.deploymentscripts.model.ScriptExecutionResultModel;


/**
 * Context sent to a script when it is run. TODO This class has many responsibilities. I should create a configuration
 * service and give him the responsibility to check the constraints of the configuration.
 * 
 * @author arobirosa
 * 
 */
public interface DeploymentScriptExecutionContext
{
	/**
	 * Returns the instance which represents an error.
	 * 
	 * @return ScriptExecutionResultModel Never null.
	 */
	ScriptExecutionResultModel getSuccessResult();

	/**
	 * Returns the instance which represents the ignored result because the deployment script can't be run in the current
	 * environment.
	 * 
	 * @return ScriptExecutionResultModel Never null.
	 */
	ScriptExecutionResultModel getIgnoredOtherEnvironmentResult();

	/**
	 * Returns the instance which represents the ignored result because the deployment script can't be run in the current
	 * tenant.
	 * 
	 * @return ScriptExecutionResultModel Never null.
	 */
	ScriptExecutionResultModel getIgnoredOtherTenantResult();

	/**
	 * Returns the current tenant
	 * 
	 * @return Never null
	 */
	Tenant getCurrentTenant();

	/**
	 * Determines of the current environment is in the given list of names.
	 * 
	 * @param allowedDeploymentEnvironmentNames
	 *           Required
	 * @return true if the current environment is present.
	 */
	boolean isCurrentEnvironmentIn(Set<String> allowedDeploymentEnvironmentNames);

}
