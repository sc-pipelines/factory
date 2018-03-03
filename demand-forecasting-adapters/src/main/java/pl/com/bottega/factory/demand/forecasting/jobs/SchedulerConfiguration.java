package pl.com.bottega.factory.demand.forecasting.jobs;

import lombok.AllArgsConstructor;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.jdbcjobstore.JobStoreCMT;
import org.quartz.impl.jdbcjobstore.PostgreSQLDelegate;
import org.quartz.simpl.PropertySettingJobFactory;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.spi.JobFactory;
import org.quartz.spi.ThreadPool;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.utils.ConnectionProvider;
import org.quartz.utils.DBConnectionManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Optional;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Configuration
@AllArgsConstructor
class SchedulerConfiguration {

    private final DataSource dataSource;
    private final ApplicationContext applicationContext;

    @Bean
    Scheduler scheduler() throws Exception {
        DBConnectionManager.getInstance()
                .addConnectionProvider("dataSource", connectionProvider());

        ThreadPool threadPool = new SimpleThreadPool(10, Thread.NORM_PRIORITY);
        JobStoreCMT jobStore = new JobStoreCMT();
        jobStore.setDriverDelegateClass(PostgreSQLDelegate.class.getName());
        jobStore.setDataSource("dataSource");
        jobStore.setNonManagedTXDataSource("dataSource");
        jobStore.setInstanceId("dataSource");

        DirectSchedulerFactory factory = DirectSchedulerFactory.getInstance();
        factory.createScheduler(threadPool, jobStore);
        Scheduler scheduler = factory.getScheduler();
        scheduler.setJobFactory(jobFactory());
        scheduler.start();

        ensureJobExists(scheduler);
        return scheduler;
    }

    public static class ImportDefinitionEntity {
        String name;
        String cron;
        String uri;
        String downloader;
        String transformer;
    }

    private void ensureJobExists(Scheduler scheduler) throws Exception {
        JobKey jobKey = JobKey.jobKey("import-demands", "import-demands");
        JobDetail job = Optional.ofNullable(scheduler.getJobDetail(jobKey))
                .orElse(newJob(ImportDemandsJob.class)
                        .withIdentity(jobKey)
                        .storeDurably()
                        .requestRecovery()
                        .build());

        TriggerKey triggerKey = TriggerKey.triggerKey("wh", "import-demands");
        scheduler.unscheduleJob(triggerKey);

        Trigger jobTrigger = newTrigger()
                .withIdentity(triggerKey)
                .withSchedule(cronSchedule("0 * * ? * * *"))
                .usingJobData("uri", "https://raw.githubusercontent.com/michal-michaluk/factory/master/README.md")
                .startNow()
                .build();

        scheduler.scheduleJob(job, Collections.singleton(jobTrigger), true);
    }

    private ConnectionProvider connectionProvider() throws SQLException {
        return new ConnectionProvider() {
            @Override
            public Connection getConnection() throws SQLException {
                return dataSource.getConnection();
            }

            @Override
            public void shutdown() throws SQLException {

            }

            @Override
            public void initialize() throws SQLException {

            }
        };
    }

    private JobFactory jobFactory() throws SchedulerException {
        return new PropertySettingJobFactory() {
            @Override
            public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
                Job job = applicationContext.getAutowireCapableBeanFactory()
                        .createBean(bundle.getJobDetail().getJobClass());
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.putAll(scheduler.getContext());
                jobDataMap.putAll(bundle.getJobDetail().getJobDataMap());
                jobDataMap.putAll(bundle.getTrigger().getJobDataMap());
                setBeanProps(job, jobDataMap);

                return job;
            }
        };
    }
}
