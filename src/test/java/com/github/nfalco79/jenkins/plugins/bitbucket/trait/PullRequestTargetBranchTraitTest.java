/*
 * Copyright 2023 Falco Nikolas
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import org.eclipse.jgit.transport.RefSpec;
import org.junit.Test;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketGitSCMBuilder;
import com.cloudbees.jenkins.plugins.bitbucket.BitbucketGitSCMRevision;
import com.cloudbees.jenkins.plugins.bitbucket.BranchSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;

import jenkins.plugins.git.GitSCMBuilder;
import jenkins.scm.api.SCMHead;

public class PullRequestTargetBranchTraitTest {

    @Test
    public void verify_that_pull_request_target_branch_is_added_as_ref_spec() throws Exception {
        PullRequestSCMHead head = mock(PullRequestSCMHead.class);
        when(head.getTarget()).thenReturn(new SCMHead("support/1.x"));
        BitbucketGitSCMRevision revision = mock(BitbucketGitSCMRevision.class);
        GitSCMBuilder<BitbucketGitSCMBuilder> ctx = new GitSCMBuilder<>(head, revision, "origin", null);

        PullRequestTargetBranchTrait trait = new PullRequestTargetBranchTrait();
        trait.decorateBuilder(ctx);

        Assertions.assertThat(ctx.asRefSpecs()).contains(new RefSpec("+refs/heads/support/1.x:refs/remotes/origin/support/1.x"));
    }

    @Test
    public void verify_that_no_ref_spec_is_added_for_non_pull_request() throws Exception {
        BranchSCMHead head = mock(BranchSCMHead.class);
        when(head.getName()).thenReturn("support/1.x");
        BitbucketGitSCMRevision revision = mock(BitbucketGitSCMRevision.class); //new BitbucketGitSCMRevision(head, mock(BitbucketCommit.class));
        GitSCMBuilder<BitbucketGitSCMBuilder> ctx = new GitSCMBuilder<>(head, revision, "origin", null);

        PullRequestTargetBranchTrait trait = new PullRequestTargetBranchTrait();
        trait.decorateBuilder(ctx);

        Assertions.assertThat(ctx.asRefSpecs()).containsOnly(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
    }

}
