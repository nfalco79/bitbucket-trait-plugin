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
package com.github.nfalco79.jenkins.plugins.bitbucket.strategy;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketGitSCMRevision;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMRevision;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource.MercurialRevision;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.TaskListener;
import jenkins.branch.BranchBuildStrategy;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceDescriptor;

public class SkipCommitBuildStrategy extends BranchBuildStrategy {

    /**
     * The message filter.
     */
    @NonNull
    private String message;
    /**
     * The author filter.
     */
    @NonNull
    private String author;

    @DataBoundConstructor
    public SkipCommitBuildStrategy(@CheckForNull String message, @CheckForNull String author) {
        this.message = StringUtils.defaultIfBlank(message, "");
        this.author = StringUtils.defaultIfBlank(author, "");
    }

    /**
     * Returns the message pattern of the filter.
     *
     * @return the message filter.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the author pattern filter.
     *
     * @return the author pattern.
     */
    public String getAuthor() {
        return author;
    }


    @Override
    public boolean isAutomaticBuild(SCMSource source, SCMHead head, SCMRevision currRevision, SCMRevision lastBuiltRevision, SCMRevision lastSeenRevision, TaskListener listener) {
        return isAutomaticBuild(source, head, currRevision, lastSeenRevision);
    }

    @Override
    public boolean isAutomaticBuild(SCMSource source, SCMHead head, SCMRevision currRevision, SCMRevision prevRevision) {
        SCMRevision revision = currRevision;

        if (currRevision instanceof PullRequestSCMRevision) {
            PullRequestSCMRevision<?> pr = (PullRequestSCMRevision<?>) currRevision;
            revision = pr.getPull();
        }

        String commitAuthor = null;
        String commitMessage = null;
        if (revision instanceof BitbucketGitSCMRevision) {
            BitbucketGitSCMRevision bbRevision = (BitbucketGitSCMRevision) revision;
            commitAuthor = Util.fixEmpty(bbRevision.getAuthor());
            commitMessage = Util.fixEmpty(bbRevision.getMessage());
        } else if (revision instanceof MercurialRevision) {
            MercurialRevision bbRevision = (MercurialRevision) revision;
            commitAuthor = Util.fixEmpty(bbRevision.getAuthor());
            commitMessage = Util.fixEmpty(bbRevision.getMessage());
        }

        if (commitAuthor != null || commitMessage != null) {
            return !(matches(this.message, commitMessage) || matches(this.author, commitAuthor));
        }

        return true;
    }

    private boolean matches(@NonNull String pattern, @CheckForNull String value) {
        String fixValue = Util.fixEmpty(value);
        return fixValue != null && Pattern.matches(getPattern(pattern), fixValue);
    }

    /**
     * Returns the pattern corresponding to the branches containing wildcards.
     *
     * @param wildcardPatterns the names wildcards to create a pattern for
     * @return pattern corresponding to the branches containing wildcards
     */
    protected String getPattern(String wildcardPatterns) {
        StringBuilder quotedBranches = new StringBuilder();
        for (String wildcardPattern : wildcardPatterns.split(" ")) {
            StringBuilder quotedPattern = new StringBuilder();
            for (String pattern : wildcardPattern.split("(?=[*])|(?<=[*])")) {
                if (pattern.equals("*")) {
                    quotedPattern.append(".*");
                } else if (!pattern.isEmpty()) {
                    quotedPattern.append(Pattern.quote(pattern));
                }
            }
            if (quotedBranches.length() > 0) {
                quotedBranches.append("|");
            }
            quotedBranches.append(quotedPattern);
        }
        return quotedBranches.toString();
    }

    @Extension
    public static class DescriptorImpl extends BranchBuildStrategyDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.SkipCommitBuildStrategy_displayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isApplicable(SCMSourceDescriptor sourceDescriptor) {
            return sourceDescriptor instanceof BitbucketSCMSource.DescriptorImpl;
        }

    }

}
