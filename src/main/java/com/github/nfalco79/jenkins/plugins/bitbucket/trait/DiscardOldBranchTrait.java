/*
 * Copyright 2018 Falco Nikolas
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.nfalco79.jenkins.plugins.bitbucket.trait;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketGitSCMBuilder;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketBranch;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.trait.SCMBuilder;
import jenkins.scm.api.trait.SCMHeadFilter;
import jenkins.scm.api.trait.SCMSourceContext;
import jenkins.scm.api.trait.SCMSourceRequest;
import jenkins.scm.api.trait.SCMSourceTrait;
import jenkins.scm.api.trait.SCMSourceTraitDescriptor;

public class DiscardOldBranchTrait extends SCMSourceTrait {

    private int keepForDays = 1;

    @DataBoundConstructor
    public DiscardOldBranchTrait(@CheckForNull int keepForDays) {
        this.keepForDays = keepForDays;
    }

    public int getKeepForDays() {
        return keepForDays;
    }

    @Override
    protected void decorateContext(SCMSourceContext<?, ?> context) {
        context.withFilter(new ExcludeOldSCMHeadBranch());
    }

    public final class ExcludeOldSCMHeadBranch extends SCMHeadFilter {
        @Override
        public boolean isExcluded(SCMSourceRequest request, SCMHead head) throws IOException, InterruptedException {
            if (keepForDays > 0) {
                BitbucketSCMSourceRequest bbRequest = (BitbucketSCMSourceRequest) request;
                String branchName = head.getName();
                if (head instanceof PullRequestSCMHead) {
                    // getName return the PR-<id>, not the branch name
                    branchName = ((PullRequestSCMHead) head).getBranchName();
                }
    
                for (BitbucketBranch branch : bbRequest.getBranches()) {
                    if (branch.getName().equals(branchName)) {
                        Calendar c = Calendar.getInstance();

                        c.setTimeInMillis(branch.getDateMillis());
                        c.set(Calendar.HOUR, 0);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        c.set(Calendar.MILLISECOND, 0);
                        c.add(Calendar.DAY_OF_YEAR, keepForDays);
                        Date expiryDate = c.getTime();
                        return expiryDate.before(new Date());
                    }
                }
            }
            return false;
        }
    }

    /**
     * Our descriptor.
     */
    @Extension
    public static class DescriptorImpl extends SCMSourceTraitDescriptor {

        public FormValidation doCheckKeepForDays(@QueryParameter final int keepForDays) {
            if (keepForDays <= 0) {
                return FormValidation.error("Invalid value. Days must be greater than 0");
            }
            return FormValidation.ok();
        }

        @Override
        public String getDisplayName() {
            return Messages.DiscardOldBranchTrait_displayName();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicableToBuilder(@SuppressWarnings("rawtypes") @NonNull Class<? extends SCMBuilder> builderClass) {
            return BitbucketGitSCMBuilder.class.isAssignableFrom(builderClass);
        }
    }

}
