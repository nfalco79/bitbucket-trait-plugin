/*
 * Copyright 2018 Falco Nikolas
 *
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceContext;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSourceRequest;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketBranch;
import com.github.nfalco79.jenkins.plugins.bitbucket.trait.DiscardOldBranchTrait.ExcludeOldSCMHeadBranch;

import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMHeadObserver;
import jenkins.scm.api.trait.SCMHeadFilter;

public class DiscardOldBranchTraitTest {
    @Test
    public void verify_that_branch_is_not_excluded_if_has_recent_commits() throws Exception {
        DiscardOldBranchTrait trait = new DiscardOldBranchTrait(10);
        BitbucketSCMSourceContext ctx = new BitbucketSCMSourceContext(null, SCMHeadObserver.none());
        trait.decorateContext(ctx);
        assertThat(ctx.filters(), contains(instanceOf(ExcludeOldSCMHeadBranch.class)));

        SCMHead head = mock(SCMHead.class);
        when(head.getName()).thenReturn("feature/release");
        BitbucketBranch branch1 = mock(BitbucketBranch.class);
        when(branch1.getName()).thenReturn("feature/xyz");
        when(branch1.getDateMillis()).thenReturn(new Date().getTime());
        BitbucketBranch branch2 = mock(BitbucketBranch.class);
        when(branch2.getName()).thenReturn("feature/release");
        when(branch2.getDateMillis()).thenReturn(new Date().getTime());
        BitbucketSCMSourceRequest request = mock(BitbucketSCMSourceRequest.class);
        when(request.getBranches()).thenReturn(Arrays.asList(branch1, branch2));
        for (SCMHeadFilter filter : ctx.filters()) {
            assertThat(filter.isExcluded(request, head), equalTo(false));
        }
    }

    @Test
    public void verify_that_branch_is_excluded_if_has_head_commit_older_than_specified() throws Exception {
        DiscardOldBranchTrait trait = new DiscardOldBranchTrait(5);
        BitbucketSCMSourceContext ctx = new BitbucketSCMSourceContext(null, SCMHeadObserver.none());
        trait.decorateContext(ctx);
        assertThat(ctx.filters(), contains(instanceOf(ExcludeOldSCMHeadBranch.class)));

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -100);
        long lastCommit = c.getTimeInMillis();

        SCMHead head = mock(SCMHead.class);
        when(head.getName()).thenReturn("feature/release");
        BitbucketBranch branch1 = mock(BitbucketBranch.class);
        when(branch1.getName()).thenReturn("feature/xyz");
        when(branch1.getDateMillis()).thenReturn(lastCommit);
        BitbucketBranch branch2 = mock(BitbucketBranch.class);
        when(branch2.getName()).thenReturn("feature/release");
        when(branch2.getDateMillis()).thenReturn(lastCommit);
        BitbucketSCMSourceRequest request = mock(BitbucketSCMSourceRequest.class);
        when(request.getBranches()).thenReturn(Arrays.asList(branch1, branch2));
        for (SCMHeadFilter filter : ctx.filters()) {
            assertThat(filter.isExcluded(request, head), equalTo(true));
        }
    }

}
