package com.nowcoder.community.config;

import com.nowcoder.community.quartz.AlphaJob;
import com.nowcoder.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

@Configuration
public class QuartzConfig {
    // 配置JobDetail
//    @Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        // 与Job建立联系
        jobDetailFactoryBean.setJobClass(AlphaJob.class);
        jobDetailFactoryBean.setName("alphaJob");
        jobDetailFactoryBean.setGroup("alphaJobGroup");
        jobDetailFactoryBean.setDurability(true);
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        // 与JobDetail建立联系
        triggerFactoryBean.setJobDetail(alphaJobDetail);
        triggerFactoryBean.setName("alphaTrigger");
        triggerFactoryBean.setGroup("alphaTriggerGroup");
        triggerFactoryBean.setRepeatInterval(5000);
        triggerFactoryBean.setJobDataMap(new JobDataMap());
        return triggerFactoryBean;
    }

    // 配置JobDetail
//    @Bean
    public JobDetailFactoryBean postScoreRefershJobDetail() {
        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        // 与Job建立联系
        jobDetailFactoryBean.setJobClass(PostScoreRefreshJob.class);
        jobDetailFactoryBean.setName("postScoreRefershJob");
        jobDetailFactoryBean.setGroup("postScoreRefershJobGroup");
        jobDetailFactoryBean.setDurability(true);
        jobDetailFactoryBean.setRequestsRecovery(true);
        return jobDetailFactoryBean;
    }

    // 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
//    @Bean
    public SimpleTriggerFactoryBean PostScoreRefershTrigger(JobDetail postScoreRefershJobDetail) {
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        // 与JobDetail建立联系
        triggerFactoryBean.setJobDetail(postScoreRefershJobDetail);
        triggerFactoryBean.setName("postScoreRefershTrigger");
        triggerFactoryBean.setGroup("postScoreRefershTriggerGroup");
        triggerFactoryBean.setRepeatInterval(1000*60*5);
        triggerFactoryBean.setJobDataMap(new JobDataMap());
        return triggerFactoryBean;
    }
}
