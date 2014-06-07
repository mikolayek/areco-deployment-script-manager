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
package org.areco.ecommerce.deploymentscripts.core.impl;

import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.areco.ecommerce.deploymentscripts.core.ScriptExecutionDao;
import org.areco.ecommerce.deploymentscripts.model.ScriptExecutionModel;
import org.areco.ecommerce.deploymentscripts.model.ScriptExecutionResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;


/**
 * Default implementatio of the dao.
 * 
 * @author arobirosa
 * 
 */
@Repository
@Scope("tenant")
public class FlexibleSearchScriptExecutionDao implements ScriptExecutionDao
{

	private static final Logger LOG = Logger.getLogger(FlexibleSearchScriptExecutionDao.class);

	@Autowired
	FlexibleSearchService flexibleSearchService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.areco.ecommerce.deploymentscripts.core.ScriptExecutionDao#getSuccessfullyExecutedScripts(java.lang.String)
	 */
	@Override
	public List<ScriptExecutionModel> getSuccessfullyExecutedScripts(final String extensionName)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("Getting the executed scripts of the extension " + extensionName);
		}
		final StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append("SELECT {es.").append(ScriptExecutionModel.PK).append("}").append(" FROM {")
				.append(ScriptExecutionModel._TYPECODE).append(" as es ").append("JOIN ")
				.append(ScriptExecutionResultModel._TYPECODE).append(" as r ").append("ON {es.").append(ScriptExecutionModel.RESULT)
				.append("} = {r.").append(ScriptExecutionResultModel.PK).append("} AND {r.")
				.append(ScriptExecutionResultModel.CANBERUNNEDAGAIN).append("} = ?")
				.append(ScriptExecutionResultModel.CANBERUNNEDAGAIN).append(" } ").append("WHERE ").append(" {es.")
				.append(ScriptExecutionModel.EXTENSIONNAME).append("} = ?").append(ScriptExecutionModel.EXTENSIONNAME);

		final Map<String, Object> queryParams = new HashMap<String, Object>();
		queryParams.put(ScriptExecutionResultModel.CANBERUNNEDAGAIN, Boolean.FALSE);
		queryParams.put(ScriptExecutionModel.EXTENSIONNAME, extensionName);

		if (LOG.isTraceEnabled())
		{
			LOG.trace("Executing the query: '" + queryBuilder.toString() + "' with the parameters " + queryParams);
		}

		final FlexibleSearchQuery query = new FlexibleSearchQuery(queryBuilder.toString(), queryParams);

		final SearchResult<ScriptExecutionModel> result = this.flexibleSearchService.search(query);
		return result.getResult();
	}
}
