package com.celements.search.provider;

import javax.inject.Singleton;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;

import com.celements.scheduler.job.PostJobAction;
import com.xpn.xwiki.plugin.lucene.searcherProvider.ISearcherProviderRole;

@Singleton
@Component("SearcherProviderManager")
public class JobSearchProviderCloser implements PostJobAction {

  @Requirement
  private ISearcherProviderRole searchProviderManager;

  private static final Logger LOGGER = LoggerFactory.getLogger(JobSearchProviderCloser.class);

  @Override
  public void accept(JobExecutionContext ctx) {
    LOGGER.info("close search providers for job: {}", ctx.getJobDetail());
    searchProviderManager.closeAllForCurrentThread();
  }

}
