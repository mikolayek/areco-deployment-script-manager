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

import de.hybris.platform.constants.CoreConstants;
import de.hybris.platform.core.initialization.SystemSetup;
import de.hybris.platform.core.initialization.SystemSetupContext;

import org.apache.log4j.Logger;
import org.areco.ecommerce.deploymentscripts.systemsetup.ExtensionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * It triggers the execution of the deployment scripts.
 * 
 * @author arobirosa
 * 
 */
@Service
@Scope("tenant")
@SystemSetup(extension = "ALL_EXTENSIONS")
public class DeploymentScriptStarter {
    private static final Logger LOG = Logger.getLogger(DeploymentScriptStarter.class);

    @Autowired
    private DeploymentScriptService deploymentScriptService;

    @Autowired
    private ExtensionHelper extensionHelper;

    private boolean wasThereAnError = false;

    /**
     * @return the wasThereAnError
     */
    private boolean isWasThereAnError() {
        return wasThereAnError;
    }

    /**
     * @param wasThereAnError
     *            the wasThereAnError to set
     */
    private void setWasThereAnError(final boolean wasThereAnError) {
        this.wasThereAnError = wasThereAnError;
    }

    /**
     * This method is called by every extension during the update or init process.
     * 
     * We hook the essential data process. Due to this the deployment scripts could be run using "ant updatessystem".
     * 
     * @param context
     *            Required
     */

    @SystemSetup(type = SystemSetup.Type.ESSENTIAL, process = SystemSetup.Process.ALL)
    public void runUpdateDeploymentScripts(final SystemSetupContext hybrisContext) {
        final UpdatingSystemExtensionContext context = this.getUpdatingContext(hybrisContext);
        if (this.extensionHelper.isFirstExtension(context) && SystemSetup.Process.UPDATE.equals(context.getProcess())) {
            this.clearErrorFlag();
        }
        this.runDeploymentScripts(context, false);
    }

    /**
     * It receibes a SystemSetupContext and it converts it to a UpdatingSystemExtensionContext used by {@link DeploymentScriptService}
     * 
     * @param hybrisContext
     *            Required
     * @return SystemSetupContext Never null.
     */
    private UpdatingSystemExtensionContext getUpdatingContext(final SystemSetupContext hybrisContext) {
        if (hybrisContext.getJspContext() == null) {
            return new UpdatingSystemExtensionContext(hybrisContext.getExtensionName(), hybrisContext.getProcess());
        } else {
            return new UpdatingSystemExtensionContext(hybrisContext.getExtensionName(), hybrisContext.getProcess(), hybrisContext.getJspContext());
        }
    }

    /**
     * Runs the all the pending deployment scripts using the given context.
     * 
     * @param context
     *            Required.
     * @param runInitScripts
     *            Required.
     * 
     * @return true if there was an error.
     */

    public boolean runDeploymentScripts(final UpdatingSystemExtensionContext context, final boolean runInitScripts) {
        if (this.isWasThereAnError()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("There was an error running the deployment scripts of the previous extensions. "
                        + "Due to this the deployment scripts of the extension " + context.getExtensionName() + " will be ignored.");
            }
            return true;
        }
        return runDeploymentScriptsAndHandleErrors(context, runInitScripts);
    }

    /**
     * It removes any previous error. It is usually call during the initialization and the update process by the core extension.
     * 
     */
    private void clearErrorFlag() {
        this.setWasThereAnError(false);
        if (LOG.isDebugEnabled()) {
            LOG.debug("The error flag was cleared");
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    // This method must catch any error in the execution of the deployment scripts.
    private boolean runDeploymentScriptsAndHandleErrors(final UpdatingSystemExtensionContext context, final boolean runInitScripts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Running the deployment scripts of the extension: " + context.getExtensionName());
        }

        try {
            final boolean somethingWentWrong = this.deploymentScriptService.runDeploymentScripts(context, runInitScripts);
            this.setWasThereAnError(somethingWentWrong);
            return somethingWentWrong;
        } catch (final RuntimeException re) {
            this.setWasThereAnError(true);
            // We improve the error logging in case of a runtime exception.
            LOG.error("There was an error running the deployment scripts: " + re.getLocalizedMessage(), re);
            throw re;
        }
    }

    /**
     * Runs all the pending UPDATE deployment scripts.
     * 
     * @return boolean if there was an error.
     */

    public boolean runAllPendingScripts() {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running all pending update deployment scripts.");
        }
        return this.runAllPendingScripts(false);
    }

    private boolean runAllPendingScripts(final boolean runInitScripts) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running all deployment scripts. RunInitScripts? " + runInitScripts);
        }
        this.clearErrorFlag();
        for (final String extensionName : this.extensionHelper.getExtensionNames()) {
            final UpdatingSystemExtensionContext aContext = new UpdatingSystemExtensionContext(extensionName, (runInitScripts ? SystemSetup.Process.INIT
                    : SystemSetup.Process.UPDATE));
            final boolean somethingWentWrong = this.runDeploymentScripts(aContext, runInitScripts);
            if (somethingWentWrong) {
                return true;
            }
        }
        return this.isWasThereAnError();
    }

    /**
     * This method is only called once during the initialization of the core extension. It runs all the INIT deployment scripts sequentially.
     * 
     * @param context
     *            Required. Describes the current update system process.
     */
    @SystemSetup(type = SystemSetup.Type.ESSENTIAL, process = SystemSetup.Process.INIT, extension = CoreConstants.EXTENSIONNAME)
    public void runInitDeploymentScripts(final SystemSetupContext context) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Running all INIT deployment scripts.");
        }
        this.runAllPendingScripts(true);
    }
}