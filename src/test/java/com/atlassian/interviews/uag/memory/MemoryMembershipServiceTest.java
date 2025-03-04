package com.atlassian.interviews.uag.memory;

import com.atlassian.interviews.uag.api.Group;
import com.atlassian.interviews.uag.api.GroupService;
import com.atlassian.interviews.uag.api.MembershipService;
import com.atlassian.interviews.uag.api.User;
import com.atlassian.interviews.uag.core.ServiceFactory;
import com.atlassian.interviews.uag.core.Services;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class MemoryMembershipServiceTest {
    private static final User OMAR = new User("omar");
    private static final User RITA = new User("rita");
    private static final User NOBODY = new User("nobody");
    private static final Group HACKERS = new Group("hackers");
    private static final Group ADMINS = new Group("admins");
    private static final Group NOGROUP = new Group("nogroup");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MembershipService membershipService;

    private GroupService groupService;

    @Before
    public void setUp() {
        final Services services = ServiceFactory.createServices();
        services.getUserService().create(OMAR);
        services.getUserService().create(RITA);
        services.getGroupService().create(ADMINS);
        services.getGroupService().create(HACKERS);
        membershipService = services.getMembershipService();
        groupService = services.getGroupService();
    }

    @Test
    public void addUserToGroup_duplicate() {
        membershipService.addUserToGroup(OMAR, HACKERS);
        membershipService.addUserToGroup(RITA, HACKERS);

        final Set<User> expected = new HashSet<>();
        expected.add(OMAR);
        expected.add(RITA);
        assertEquals(sorted(expected), sorted(membershipService.getUsersInGroup(HACKERS)));

        membershipService.addUserToGroup(OMAR, HACKERS);
        assertEquals(sorted(expected), sorted(membershipService.getUsersInGroup(HACKERS)));
    }

    @Test
    public void addUserToGroup_noSuchGroup() {
        thrown.expect(IllegalArgumentException.class);
        membershipService.addUserToGroup(OMAR, NOGROUP);
    }

    @Test
    public void addUserToGroup_noSuchUser() {
        thrown.expect(IllegalArgumentException.class);
        membershipService.addUserToGroup(NOBODY, HACKERS);
    }

    @Test
    public void addUserToGroup_npeGroup() {
        thrown.expect(NullPointerException.class);
        membershipService.addUserToGroup(OMAR, null);
    }

    @Test
    public void addUserToGroup_npeUser() {
        thrown.expect(NullPointerException.class);
        membershipService.addUserToGroup(null, HACKERS);
    }

    @Test
    public void testRemoveUserFromGroup() {
        membershipService.addUserToGroup(OMAR, ADMINS);
        membershipService.addUserToGroup(RITA, HACKERS);
        assertTrue("omar is an admin", membershipService.isUserInGroup(OMAR, ADMINS));

        membershipService.removeUserFromGroup(OMAR, ADMINS);
        assertFalse("omar is not an admin anymore", membershipService.isUserInGroup(OMAR, ADMINS));
    }

    @Test
    public void testIsUserInGroup_no() {
        membershipService.addUserToGroup(OMAR, ADMINS);
        membershipService.addUserToGroup(RITA, HACKERS);

        assertFalse("omar is not a hacker", membershipService.isUserInGroup(OMAR, HACKERS));
        assertFalse("rita is not an admin", membershipService.isUserInGroup(RITA, ADMINS));
    }

    @Test
    public void testIsUserInGroup_yes() {
        membershipService.addUserToGroup(OMAR, ADMINS);
        membershipService.addUserToGroup(RITA, HACKERS);

        assertTrue("omar is an admin", membershipService.isUserInGroup(OMAR, ADMINS));
        assertTrue("rita is a hacker", membershipService.isUserInGroup(RITA, HACKERS));
    }

    @Test
    public void removeUserFromAnEmptyGroupShouldPass(){
        membershipService.removeUserFromGroup(OMAR, ADMINS);
    }

    @Test
    public void recreatingAGroupShouldCreateAnEmptyGroup(){
        /**
         * Step-1 : Add users to group
         */
        membershipService.addUserToGroup(OMAR, ADMINS);
        membershipService.addUserToGroup(RITA, ADMINS);

        /**
         * Delete the group
         */
        groupService.delete(ADMINS);

        /**
         * Recreate the Group and check that it should be empty
         */
        groupService.create(ADMINS);

        Collection<User> users = membershipService.getUsersInGroup(ADMINS);
        assertEquals(users.size(),0);
    }

    private static <T extends Comparable<T>> List<T> sorted(Collection<T> items) {
        final List<T> list = new ArrayList<>(items);
        Collections.sort(list);
        return list;
    }
}
