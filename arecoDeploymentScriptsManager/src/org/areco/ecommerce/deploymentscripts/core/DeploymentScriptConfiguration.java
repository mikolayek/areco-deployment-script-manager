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
import de.hybris.platform.servicelayer.util.ServicesUtil;

import java.util.Set;

import org.areco.ecommerce.deploymentscripts.model.ScriptExecutionResultModel;


/**
 * It defines special properties of the deployment scripts like where they are allowed to run. It is inmutable.
 * 
 * @author arobirosa
 * 
 */
public class DeploymentScriptConfiguration
{
	/* The existent of the tenants is validated during the creation of the configuration. */
	private final Set<Tenant> allowedTenants;
	/*
	 * It contains the names of the environment because we validate the existenz of it just before running the script.
	 */
	private final Set<String> allowedDeploymentEnvironmentNames;

	public DeploymentScriptConfiguration()
	{
		this(null, null);
	}

	public DeploymentScriptConfiguration(final Set<Tenant> someTenants, final Set<String> someDeploymentEnvironmentNames)
	{
		this.allowedDeploymentEnvironmentNames = someDeploymentEnvironmentNames;
		this.allowedTenants = someTenants;
	}

	protected Set<Tenant> getAllowedTenants()
	{
		return allowedTenants;
	}

	protected Set<String> getAllowedDeploymentEnvironmentNames()
	{
		return allowedDeploymentEnvironmentNames;
	}

	/**
	 * Checks if this script is allowed to run in this server.
	 * 
	 * @param context
	 *           Required.
	 * @return null if it is allowed to run. Otherwise it returns the execution result.
	 */
	public ScriptExecutionResultModel isAllowedInThisServer(final DeploymentScriptExecutionContext context)
	{
		ServicesUtil.validateParameterNotNullStandardMessage("context", context);
		if (!this.isAllowedInThisTenant(context))
		{
			return context.getIgnoredOtherTenantResult();
		}
		if (!this.isAllowedInThisDeploymentEnvironment(context))
		{
			return context.getIgnoredOtherEnvironmentResult();
		}
		return null; //We can run this script
	}

	private boolean isAllowedInThisDeploymentEnvironment(final DeploymentScriptExecutionContext context)
	{
		if (this.getAllowedDeploymentEnvironmentNames() == null || this.getAllowedDeploymentEnvironmentNames().isEmpty())
		{
			return true;
		}
		return context.isCurrentEnvironmentIn(this.getAllowedDeploymentEnvironmentNames());
	}

	private boolean isAllowedInThisTenant(final DeploymentScriptExecutionContext context)
	{
		if (this.getAllowedTenants() == null || this.getAllowedTenants().isEmpty())
		{
			return true;
		}
		return this.getAllowedTenants().contains(context.getCurrentTenant());
	}



}
